package com.qinyun.bbs.es.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.qinyun.bbs.es.annotation.EsOperateType;
import com.qinyun.bbs.es.entity.BbsIndex;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.engine.PageQuery;
import org.beetl.sql.core.page.PageResult;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.qinyun.bbs.es.annotation.EntityType;
import com.qinyun.bbs.es.annotation.EsFallback;
import com.qinyun.bbs.es.config.EsConfig;
import com.qinyun.bbs.es.vo.IndexObject;
import com.qinyun.bbs.model.BbsModule;
import com.qinyun.bbs.model.BbsPost;
import com.qinyun.bbs.model.BbsReply;
import com.qinyun.bbs.model.BbsTopic;
import com.qinyun.bbs.model.BbsUser;
import com.qinyun.bbs.service.BBSService;

import lombok.extern.slf4j.Slf4j;

//@Service
@Slf4j
public class EsService implements SearchService{

	@Autowired
    BBSService    bbsService;
	@Autowired
    SQLManager    sqlManager;
	@Autowired
    EsConfig      esConfig;
	@Autowired
    GroupTemplate beetlTemplate;
    Executor      executor = Executor.newInstance();


    /**
     * 公共操作方法
     */
    @EsFallback
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

    public void editEsIndexFallback(EntityType entityType, EsOperateType operateType, Object id) {
    }

    /**
     * 重构索引
     */
    @EsFallback
    public void initIndex() {
        try {
            executor.execute(Request.Delete(esConfig.getUrl())).discardContent();
            batchSaveBbsIndex(BbsTopic.class);
            batchSaveBbsIndex(BbsPost.class);
            batchSaveBbsIndex(BbsReply.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void initIndexFallback() {
    }

    /**
     * 批量插入索引
     */
    private <T> void batchSaveBbsIndex(Class<T> clazz) {
        int curPage  = 1;
        long pageSize = 500;

        while (true) {
            long     startRow = 1 + (curPage - 1) * pageSize;
            List<T> list     = sqlManager.all(clazz, startRow, pageSize);
            if (list != null && list.size() > 0) {
                List<BbsIndex> indexList = new ArrayList<>();
                for (T t : list) {
                    BbsIndex bbsIndex = null;

                    if (t instanceof BbsTopic) {
                        BbsTopic topic     = (BbsTopic) t;
                        BbsPost  firstPost = bbsService.getFirstPost(topic.getId());
                        bbsIndex = new BbsIndex(topic.getId(), null, null, topic.getUserId(), topic.getContent(), topic.getCreateTime(), 0, 0, firstPost != null ? firstPost.getIsAccept() : 0, topic.getPv());
                    }
                    if (t instanceof BbsPost) {
                        BbsPost  post  = (BbsPost) t;
                        BbsTopic topic = bbsService.getTopic(post.getTopicId());
                        bbsIndex = new BbsIndex(post.getTopicId(), post.getId(), null, post.getUserId(), post.getContent(), post.getCreateTime(), post.getPros(), post.getCons(), post.getIsAccept(), topic.getPv());
                    }
                    if (t instanceof BbsReply) {
                        BbsReply reply = (BbsReply) t;
                        bbsIndex = new BbsIndex(reply.getTopicId(), reply.getPostId(), reply.getId(), reply.getUserId(), reply.getContent(), reply.getCreateTime(), 0, 0, 0, 0);
                    }
                    if (bbsIndex == null) {
                        log.error("未定义类型转换");
                    } else {
                        indexList.add(bbsIndex);
                    }

                }
                indexList.forEach(this::saveBbsIndex);
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
            BbsTopic topic     = bbsService.getTopic(id);
            BbsPost  firstPost = bbsService.getFirstPost(topic.getId());
            bbsIndex = new BbsIndex(topic.getId(), null, null, topic.getUserId(), topic.getContent(), topic.getCreateTime(), 0, 0, firstPost != null ? firstPost.getIsAccept() : 0, topic.getPv());
        } else if (entityType == EntityType.BbsPost) {
            BbsPost  post  = bbsService.getPost(id);
            BbsTopic topic = bbsService.getTopic(post.getTopicId());
            bbsIndex = new BbsIndex(post.getTopicId(), post.getId(), null, post.getUserId(), post.getContent(), post.getCreateTime(), post.getPros(), post.getCons(), post.getIsAccept(), topic.getPv());
        } else if (entityType == EntityType.BbsReply) {
            BbsReply reply = bbsService.getReply(id);
            bbsIndex = new BbsIndex(reply.getTopicId(), reply.getPostId(), reply.getId(), reply.getUserId(), reply.getContent(), reply.getCreateTime(), 0, 0, 0, 0);
        }
        if (bbsIndex == null) {
            log.error("未定义类型转换");
        }
        return bbsIndex;
    }


    /**
     * 保存或更新索引
     */
    private void saveBbsIndex(BbsIndex bbsIndex) {
        try {
            executor.execute(Request.Put(esConfig.getContentUrl()+bbsIndex.getId())
                    .bodyString(JSON.toJSONString(bbsIndex), ContentType.APPLICATION_JSON))
                    .discardContent();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除索引
     */
    private void deleteBbsIndex(String id) {
        try {
            executor.execute(Request.Delete(esConfig.getContentUrl() + id))
                    .discardContent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 创建所有并返回搜索结果
     */
    @EsFallback
    public PageQuery<IndexObject> getQueryPage(String keyword, int p) {
        if (p <= 0) {
            p = 1;
        }
        int  pageNumber = p;
        long pageSize   = PageQuery.DEFAULT_PAGE_SIZE;

        if (keyword != null) {
            keyword = this.string2Json(keyword);
        }
        PageQuery<IndexObject> pageQuery = new PageQuery<>(pageNumber, pageSize);
        try {
            Template template = beetlTemplate.getTemplate("/bssContent.html");
            template.binding("pageSize", pageSize);
            template.binding("pageNumber", pageNumber);
            template.binding("keyword", keyword);
            String result = executor.execute(Request.Post(esConfig.getContentSearchUrl())
                    .bodyString(template.render(), ContentType.APPLICATION_JSON))
                    .returnContent()
                    .asString(StandardCharsets.UTF_8);

            List<IndexObject> indexObjectList = new ArrayList<>();
            JSONObject root = JSON.parseObject(result);
            Object totalObj = JSONPath.eval(root, "$.hits.total");
            long     total;
            if (totalObj instanceof JSONObject) {
                //兼容ES7(最后测试通过版本:v)
                total = ((JSONObject) totalObj).getLongValue("value");
            } else {
                total = Long.parseLong(totalObj.toString());
            }
            JSONArray  arrays = (JSONArray) JSONPath.eval(root, "$.hits.hits");
            for (int i = 0; i < arrays.size(); i++) {
                JSONObject node = arrays.getJSONObject(i);
                double   score = node.getDoubleValue("_score");
                BbsIndex index = node.getObject("_source",BbsIndex.class);

                index.setContent(node.getJSONObject("highlight").getJSONArray("content").toJavaList(String.class).get(0));
                if (index.getTopicId() != null) {
                    IndexObject indexObject = null;

                    BbsTopic topic = bbsService.getTopic(index.getTopicId());

                    BbsUser   user   = topic.getUser();
                    BbsModule module = topic.getModule();

                    if (index.getReplyId() != null) {
                        indexObject = new IndexObject(topic.getId(), topic.getIsUp(), topic.getIsNice(), user,
                                topic.getCreateTime(), topic.getPostCount(), topic.getPv(), module,
                                topic.getContent(), index.getContent(), 3, score);

                    } else if (index.getPostId() != null) {
                        indexObject = new IndexObject(topic.getId(), topic.getIsUp(), topic.getIsNice(), user,
                                topic.getCreateTime(), topic.getPostCount(), topic.getPv(), module,
                                topic.getContent(), index.getContent(), 2, score);

                    } else if (index.getTopicId() != null) {
                        String  postContent = "";
                        BbsPost firstPost   = bbsService.getFirstPost(index.getTopicId());
                        if (firstPost != null) {
                            postContent = firstPost.getContent();
                        }
                        indexObject = new IndexObject(topic.getId(), topic.getIsUp(), topic.getIsNice(), user,
                                topic.getCreateTime(), topic.getPostCount(), topic.getPv(), module,
                                index.getContent(), postContent, 1, score);
                    }

                    indexObjectList.add(indexObject);
                }
            }
            pageQuery.setTotalRow(total);
            pageQuery.setList(indexObjectList);
            return pageQuery;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public PageQuery<IndexObject> getQueryPageFallback(String keyword, int p) {
        if (p <= 0) {
            p = 1;
        }
        int                    pageNumber = p;
        long                   pageSize   = PageQuery.DEFAULT_PAGE_SIZE;
        String                 kw         = keyword.trim().replaceAll("</?\\w+[^>]>", "");
        PageQuery<IndexObject> pageQuery  = new PageQuery<>(pageNumber, pageSize);

        PageResult<BbsPost> postPage = bbsService.queryPostByContent(kw, pageNumber, pageSize);
        List<IndexObject> indexObjects = Optional.ofNullable(postPage.getList())
                .orElse(Collections.emptyList())
                .stream()
                .peek(post -> post.setContent(post.getContent().replaceAll("</?\\w+[^>]*>", "").toLowerCase()))
                .filter(post -> StringUtils.isNotBlank(post.getContent()))
                .map(post -> {
                    String content = post.getContent();
                    int    index   = Math.max(0, content.indexOf(kw));
                    int    start   = Math.max(index - 100, 0);
                    int    end     = Math.min(index + kw.length() + 100, content.length());
                    return new IndexObject(post.getTopicId(), 0, 0, new BbsUser(post.getUserId(), "注册用户"),
                            post.getCreateTime(), post.getCons(), post.getPros(), null,
                            content.substring(0, Math.min(50, content.length())) + "...",
                            content.substring(start, index) +
                                    "<font color=\"red\">" + kw + "</font>" +
                                    content.substring(index + kw.length(), end),
                            1,
                            1);
                })
                .collect(Collectors.toList());
        pageQuery.setTotalRow(postPage.getTotalRow());
        pageQuery.setList(indexObjects);
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

}
