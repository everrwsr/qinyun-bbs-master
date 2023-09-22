package com.qinyun.bbs.lucene;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.qinyun.bbs.es.annotation.EntityType;
import com.qinyun.bbs.es.annotation.EsOperateType;
import com.qinyun.bbs.es.entity.BbsIndex;
import com.qinyun.bbs.es.service.SearchService;
import com.qinyun.bbs.es.vo.IndexObject;
import com.qinyun.bbs.lucene.dto.jieba.JiebaAnalyzer;
import com.qinyun.bbs.service.BBSService;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.engine.PageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;

import com.qinyun.bbs.lucene.dto.ik.IKAnalyzer5x;
import com.qinyun.bbs.model.BbsModule;
import com.qinyun.bbs.model.BbsPost;
import com.qinyun.bbs.model.BbsReply;
import com.qinyun.bbs.model.BbsTopic;
import com.qinyun.bbs.model.BbsUser;
import com.qinyun.bbs.util.ExecutorUtil;

import lombok.extern.slf4j.Slf4j;

//@Service
@Slf4j
public class LuceneService implements SearchService {

	@Autowired
	private BBSService bbsService;
	@Autowired
	private SQLManager sqlManager;

	private Directory directory = null;
	private Analyzer analyzerUseSmart = null;
	private Analyzer analyzerNotUseSmart = null;
	private String indexDer = "/lucene";// 索引存放目录

	private Analyzer getAnalyzerUseSmart() {
		if (analyzerUseSmart == null) {
			synchronized (this) {
				if (analyzerUseSmart == null) {
					// analyzer = new SmartChineseAnalyzer();//中文分词器
					// 创建IKAnalyzer中文分词对象
//					analyzerUseSmart = new IKAnalyzer5x(true);
					analyzerUseSmart = new JiebaAnalyzer(true);
				}
			}
		}
		return analyzerUseSmart;
	}

	private Analyzer getAnalyzerNotUseSmart() {
		if (analyzerNotUseSmart == null) {
			synchronized (this) {
				if (analyzerNotUseSmart == null) {
					// analyzer = new SmartChineseAnalyzer();//中文分词器
					// 创建IKAnalyzer中文分词对象
					analyzerNotUseSmart = new IKAnalyzer5x(false);
//					analyzerNotUseSmart = new JiebaAnalyzer(false);
				}
			}
		}
		return analyzerNotUseSmart;
	}

	private Directory getDirectory() throws IOException {
		if (directory == null) {
			synchronized (this) {
				if (directory == null) {
					ApplicationHome home = new ApplicationHome(getClass());
					File jarFile = home.getSource();
					String rootPath = jarFile.getParentFile().getAbsolutePath();
					File indexrepository_file = new File(rootPath + this.indexDer);

					Path path = indexrepository_file.toPath();
					directory = FSDirectory.open(path);
				}
			}

		}
		return directory;
	}

	/**
	 * 公共操作方法
	 */
	public void editEsIndex(EntityType entityType, EsOperateType operateType, Object id) {
		if (operateType == EsOperateType.ADD || operateType == EsOperateType.UPDATE) {
			BbsIndex bbsIndex = this.createBbsIndex(entityType, (Integer) id);
			if (bbsIndex != null) {
				this.saveBbsIndex(bbsIndex);
			}
		} else if (operateType == EsOperateType.DELETE) {
			this.deleteBbsIndex((String) id);
		}
	}

	/**
	 * 重构索引
	 */
	public void initIndex() {
		IndexWriter indexWriter = null;
		try {
			// 创建一个分析器对象
			// 创建一个IndexwriterConfig对象
			IndexWriterConfig config = new IndexWriterConfig(getAnalyzerNotUseSmart());
			// 创建一个IndexWriter对象，对于索引库进行写操作
			indexWriter = new IndexWriter(getDirectory(), config);
			// 删除以前的索引
			indexWriter.deleteAll();

			indexWriter.commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				indexWriter.rollback();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				indexWriter.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		batchSaveBbsIndex(BbsTopic.class);
		batchSaveBbsIndex(BbsPost.class);
		batchSaveBbsIndex(BbsReply.class);
	}

	/**
	 * 批量插入索引
	 */
	private <T> void batchSaveBbsIndex(Class<T> clazz) {
		int curPage = 1;
		long pageSize = 500;

		while (true) {
			long startRow = 1 + (curPage - 1) * pageSize;
			List<T> list = sqlManager.all(clazz, startRow, pageSize);
			if (list != null && list.size() > 0) {
				List<BbsIndex> indexList = new ArrayList<>();
				for (T t : list) {
					BbsIndex bbsIndex = null;

					if (t instanceof BbsTopic) {
						BbsTopic topic = (BbsTopic) t;
						BbsPost firstPost = bbsService.getFirstPost(topic.getId());
						bbsIndex = new BbsIndex(topic.getId(), null, null, topic.getUserId(), topic.getContent(),
								topic.getCreateTime(), 0, 0, firstPost != null ? firstPost.getIsAccept() : 0,
								topic.getPv());
						bbsIndex.setEntityType(EntityType.BbsTopic);
					} else if (t instanceof BbsPost) {
						BbsPost post = (BbsPost) t;
						BbsTopic topic = bbsService.getTopic(post.getTopicId());
						if(topic!=null){
							bbsIndex = new BbsIndex(post.getTopicId(), post.getId(), null, post.getUserId(),
									post.getContent(), post.getCreateTime(), post.getPros(), post.getCons(),
									post.getIsAccept(), topic.getPv());
							bbsIndex.setEntityType(EntityType.BbsPost);
						}

					} else if (t instanceof BbsReply) {
						BbsReply reply = (BbsReply) t;
						bbsIndex = new BbsIndex(reply.getTopicId(), reply.getPostId(), reply.getId(), reply.getUserId(),
								reply.getContent(), reply.getCreateTime(), 0, 0, 0, 0);
						bbsIndex.setEntityType(EntityType.BbsReply);
					}
					if (bbsIndex == null) {
						log.error("未定义类型转换");
					} else {
						indexList.add(bbsIndex);
					}

				}
				saveBbsIndexList(indexList);
				curPage++;
			} else {
				break;
			}
		}
	}

	/**
	 * 创建索引对象
	 */
	public BbsIndex createBbsIndex(EntityType entityType, Integer id) {

		BbsIndex bbsIndex = null;
		if (entityType == EntityType.BbsTopic) {
			BbsTopic topic = bbsService.getTopic(id);
			BbsPost firstPost = bbsService.getFirstPost(topic.getId());
			bbsIndex = new BbsIndex(topic.getId(), null, null, topic.getUserId(), topic.getContent(),
					topic.getCreateTime(), 0, 0, firstPost != null ? firstPost.getIsAccept() : 0, topic.getPv());
		} else if (entityType == EntityType.BbsPost) {
			BbsPost post = bbsService.getPost(id);
			BbsTopic topic = bbsService.getTopic(post.getTopicId());
			bbsIndex = new BbsIndex(post.getTopicId(), post.getId(), null, post.getUserId(), post.getContent(),
					post.getCreateTime(), post.getPros(), post.getCons(), post.getIsAccept(), topic.getPv());
		} else if (entityType == EntityType.BbsReply) {
			BbsReply reply = bbsService.getReply(id);
			bbsIndex = new BbsIndex(reply.getTopicId(), reply.getPostId(), reply.getId(), reply.getUserId(),
					reply.getContent(), reply.getCreateTime(), 0, 0, 0, 0);
		}

		if (bbsIndex == null) {
			log.error("未定义类型转换");
		} else {
			bbsIndex.setEntityType(entityType);
		}
		return bbsIndex;
	}

	/**
	 * 保存或更新索引
	 */
	private void saveBbsIndex(BbsIndex bbsIndex) {
		List<BbsIndex> bbsIndexList = new ArrayList<>();
		bbsIndexList.add(bbsIndex);
		saveBbsIndexList(bbsIndexList);
	}

	/**
	 * 保存或更新索引,异步线程池执行
	 */
	private void saveBbsIndexList(List<BbsIndex> bbsIndexList) {
		ExecutorUtil.LUCENE_EXECUTOR_POOL.execute(new Runnable() {

			@Override
			public void run() {

				IndexWriter indexWriter = null;
				try {
					// 创建一个分析器对象
					// 创建一个IndexwriterConfig对象
					IndexWriterConfig config = new IndexWriterConfig(getAnalyzerUseSmart());
					// 创建一个IndexWriter对象，对于索引库进行写操作
					indexWriter = new IndexWriter(getDirectory(), config);
					// 删除以前的索引
					// indexWriter.deleteAll();

					for (BbsIndex t : bbsIndexList) {
						// 创建一个Document对象
						Document document = new Document();

						Field contentField = null;

						if (StringUtils.isBlank(t.getContent())) { // 如果为空结束该次循环
							continue;
						}

						// 向Document对象中添加域信息
						// 参数：1、域的名称；2、域的值；3、是否存储；
						contentField = new TextField("content", labelformat(t.getContent()), Store.YES);
						
						Field id = new StringField("id", t.getId(),Store.YES);
						
						// storedFiled默认存储
						Field topicIdField = new StoredField("topicId", t.getTopicId());
						if (t.getPostId() != null) {
							Field postIdField = new StoredField("postId", t.getPostId());
							document.add(postIdField);
						}
						if (t.getReplyId() != null) {
							Field replyIdField = new StoredField("replyId", t.getReplyId());
							document.add(replyIdField);
						}
						Field indexTypeField = new StoredField("indexType", t.getEntityType().ordinal() + 1);
						Field createTimeField = new StoredField("createTime", t.getCreateTime().getTime());
						Field pvField = new StoredField("pv", t.getPv());

						// 将域添加到document对象中
						document.add(id);
						document.add(topicIdField);
						document.add(indexTypeField);
						document.add(contentField);
						document.add(createTimeField);
						document.add(pvField);

						// 将信息写入到索引库中
						indexWriter.updateDocument(new Term("id", t.getId()), document);
					}

					// 刷新
					indexWriter.flush();
					indexWriter.commit();
				} catch (Exception e) {
					e.printStackTrace();
					try {
						indexWriter.rollback();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} finally {
					try {
						indexWriter.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}

			}
		});

	}

	private List<Document> getDocumentList(IndexReader reader, Query query) {
		List<Document> list = new ArrayList<>();
		try {
			IndexSearcher searcher = new IndexSearcher(reader);
			// 搜索：第一个参数：查询语句对象 第二个参数：指定显示记录数
			TopDocs search = searcher.search(query, Integer.MAX_VALUE);
			// 从搜索结果对象中获取结果集
			ScoreDoc[] scoreDocs = search.scoreDocs;
			for (ScoreDoc scoreDoc : scoreDocs) {
				// 获取Id
				int docId = scoreDoc.doc;
				// 通过文档ID从硬盘上获取文档
				Document document = reader.document(docId);
				list.add(document);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return list;
	}

	/**
	 * 删除索引,异步处理
	 */
	private void deleteBbsIndex(String id) {
		ExecutorUtil.LUCENE_EXECUTOR_POOL.execute(new Runnable() {

			@Override
			public void run() {
				IndexWriter indexWriter = null;
				try {
					// 创建一个分析器对象
					// 创建一个IndexwriterConfig对象
					IndexWriterConfig config = new IndexWriterConfig(getAnalyzerUseSmart());
					// 创建一个IndexWriter对象，对于索引库进行写操作
					indexWriter = new IndexWriter(getDirectory(), config);

					indexWriter.deleteDocuments(new Term("id", id));

					indexWriter.commit();

				} catch (Exception e) {
					e.printStackTrace();
					try {
						indexWriter.rollback();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} finally {
					try {
						indexWriter.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

	}

	/**
	 * 创建所有并返回搜索结果
	 */
	public PageQuery<IndexObject> getQueryPage(String keyword, int p) {
		if (p <= 0) {
			p = 1;
		}
		int pageNumber = p;
		Integer pageSize = Integer.parseInt(PageQuery.DEFAULT_PAGE_SIZE + "");

		if (keyword != null) {
			keyword = this.string2Json(keyword);
		}
		if (pageSize == 0)
			pageSize = 10;
		IndexReader indexReader = null;
		PageQuery<IndexObject> pageQuery = null;
		List<IndexObject> searchResults = new ArrayList<>();
		try {
			// 打开索引库
			indexReader = DirectoryReader.open(getDirectory());
			// 创建一个IndexSearcher对象
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			// 建立查询解析器
			// 第一个参数是要查询的字段；
			// 第二个参数是分析器Analyzer
			QueryParser parser = new QueryParser("content", getAnalyzerUseSmart());
			// 创建一个查询对象
			// 特殊字符转义
			keyword = QueryParser.escape(keyword);
			// 根据传进来的par查找
			Query query = parser.parse(keyword);
			// 执行查询
			// 返回的最大值，在分页的时候使用
			// TopDocs topDocs = indexSearcher.search(query, 5);

			// 获取上一页的最后一个元素
			ScoreDoc lastSd = getLastScoreDoc(pageNumber, pageSize, query, indexSearcher);
			// 通过最后一个元素去搜索下一页的元素
			TopDocs topDocs = indexSearcher.searchAfter(lastSd, query, pageSize);

			// 高亮显示
			Highlighter highlighter = addStringHighlighter(query);

			// 取查询结果总数量
			// System.out.println("总共的查询结果：" + topDocs.totalHits);
			// 查询结果，就是documentID列表
			for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
				// 取对象document的对象id
				int docID = scoreDoc.doc;
				// 相关度得分
				float score = scoreDoc.score;
				// 根据ID去document对象
				Document document = indexSearcher.doc(docID);
				String content = stringFormatHighlighterOut(getAnalyzerNotUseSmart(), highlighter, document, "content");

				BbsTopic topic = bbsService.getTopic(Integer.valueOf(document.get("topicId")));
				if (topic == null) {
					continue;
				}
				BbsUser user = topic.getUser();
				BbsModule module = topic.getModule();

				if (document.get("indexType").equals("1")) { // 主题贴
					BbsPost firstPost = bbsService.getFirstPost(topic.getId());

					searchResults.add(new IndexObject(topic.getId(), topic.getIsUp(), topic.getIsNice(), user,
							new Date(Long.valueOf(document.get("createTime"))), topic.getPostCount(), topic.getPv(),
							module, content, firstPost != null ? firstPost.getContent() : "", 1, score));
				} else if (document.get("indexType").equals("2")) { // 回复贴
					searchResults.add(new IndexObject(topic.getId(), topic.getIsUp(), topic.getIsNice(), user,
							new Date(Long.valueOf(document.get("createTime"))), topic.getPostCount(), topic.getPv(),
							module, topic.getContent(), content, 2, score));
				}
			}

			pageQuery = new PageQuery<>(pageNumber, null);
			pageQuery.setPageSize(pageSize);
			pageQuery.setTotalRow(Integer.parseInt(topDocs.totalHits.value + ""));
			Collections.sort(searchResults, new Comparator<IndexObject>() {

				@Override
				public int compare(IndexObject o1, IndexObject o2) {
					return Double.valueOf(o2.getScore()).compareTo(Double.valueOf(o1.getScore()));
				}
			});
			pageQuery.setList(searchResults);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				indexReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return pageQuery;
	}

	/**
	 * JSON字符串特殊字符处理
	 */
	private String string2Json(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '\"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '/':
				sb.append("\\/");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			default:
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * 根据页码和分页大小获取上一次的最后一个scoredoc
	 * 
	 * @param currentPage
	 * @param pageSize
	 * @param query
	 * @param searcher
	 * @return ScoreDoc
	 * @throws IOException
	 */
	private ScoreDoc getLastScoreDoc(Integer currentPage, Integer pageSize, Query query, IndexSearcher searcher)
			throws IOException {
		if (currentPage == 1)
			return null;// 如果是第一页就返回空
		int num = pageSize * (currentPage - 1);// 获取上一页的最后数量
		TopDocs tds = searcher.search(query, num);
		return tds.scoreDocs[num - 1];
	}

	/**
	 * 设置字符串高亮
	 * 
	 * @param query
	 * @return
	 */
	private Highlighter addStringHighlighter(Query query) {
		// 算分
		QueryScorer scorer = new QueryScorer(query);
		// 显示得分高的片段
		Fragmenter fragmenter = new SimpleSpanFragmenter(scorer);
		// 设置标签内部关键字的颜色
		// 第一个参数：标签的前半部分；第二个参数：标签的后半部分。
		SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<font color='red'>", "</font>");
		// 第一个参数是对查到的结果进行实例化；第二个是片段得分（显示得分高的片段，即摘要）
		Highlighter highlighter = new Highlighter(simpleHTMLFormatter, scorer);
		// 设置片段
		highlighter.setTextFragmenter(fragmenter);
		return highlighter;
	}

	private String stringFormatHighlighterOut(Analyzer analyzer, Highlighter highlighter, Document document,
			String field) throws Exception {
		String fieldValue = document.get(field);
		if (fieldValue != null) {
			// 把全部得分高的摘要给显示出来
			// 第一个参数是对哪个参数进行设置；第二个是以流的方式读入
			TokenStream tokenStream = analyzer.tokenStream(field, new StringReader(fieldValue));
			// 获取最高的片段
			return highlighter.getBestFragment(tokenStream, fieldValue);
		}
		return null;
	}

	/**
	 * 转译a标签
	 * 
	 * @param content
	 * @return
	 */
	private String labelformat(String content) {
		if (StringUtils.isBlank(content))
			return "";
		return content.replaceAll("<a", "&lt;a").replaceAll("</a>", "&lt;/a&gt;");
	}
}
