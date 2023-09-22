package com.qinyun.bbs;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;


import com.qinyun.bbs.lucene.LuceneService;
import com.qinyun.bbs.lucene.dto.ik.IKAnalyzer5x;
import com.qinyun.bbs.lucene.dto.jieba.JiebaAnalyzer;

public class SearchTest {

	public static void main(String[] args) throws IOException {
		
//		test1() ;
		test2();
	}

	
	public static void test2() {
		LuceneService service = new LuceneService();
//		service.get
		
	}
	
	public static void test1() throws IOException {
		String etext = "Analysis is one of the main causes of slow indexing. Simply put, ";
		// String chineseText = "张三说的确实在理。";
		String chineseText = "我的新书，我买了一本书,厉害了我的国一经播出，受到各方好评，强烈激发了国人的爱国之情、自豪感！";

		LuceneService service = new LuceneService();

		// IKAnalyzer 细粒度切分
		try (Analyzer ik = new IKAnalyzer5x(false);) {
			TokenStream ts = ik.tokenStream("content", etext);
			System.out.println("IKAnalyzer中文分词器 细粒度切分，英文分词效果：");
			doToken(ts);
			ts = ik.tokenStream("content", chineseText);
			System.out.println("IKAnalyzer中文分词器 细粒度切分，中文分词效果：");
			doToken(ts);
		}
		// IKAnalyzer 智能切分
		try (Analyzer ik = new IKAnalyzer5x(true);) {
			TokenStream ts = ik.tokenStream("content", etext);
			System.out.println("IKAnalyzer中文分词器 智能切分，英文分词效果：");
			doToken(ts);
			ts = ik.tokenStream("content", chineseText);
			System.out.println("IKAnalyzer中文分词器 智能切分，中文分词效果：");
			doToken(ts);
		}

		// JiebaAnalyzer 细粒度切分
		try (Analyzer jieba = new JiebaAnalyzer(false);) {
			TokenStream ts = jieba.tokenStream("content", etext);
			System.out.println("JiebaAnalyzer中文分词器 细粒度切分，英文分词效果：");
			doToken(ts);
			ts = jieba.tokenStream("content", chineseText);
			System.out.println("JiebaAnalyzer中文分词器 细粒度切分，中文分词效果：");
			doToken(ts);
		}
		// JiebaAnalyzer 智能切分
		try (Analyzer jieba = new JiebaAnalyzer(true);) {
			TokenStream ts = jieba.tokenStream("content", etext);
			System.out.println("JiebaAnalyzer中文分词器 智能切分，英文分词效果：");
			doToken(ts);
			ts = jieba.tokenStream("content", chineseText);
			System.out.println("JiebaAnalyzer中文分词器 智能切分，中文分词效果：");
			doToken(ts);
		}
	}
	private static void doToken(TokenStream ts) throws IOException {
		ts.reset();
		CharTermAttribute cta = ts.getAttribute(CharTermAttribute.class);
		while (ts.incrementToken()) {
			System.out.print(cta.toString() + "|");
		}
		System.out.println();
		ts.end();
		ts.close();
	}

}
