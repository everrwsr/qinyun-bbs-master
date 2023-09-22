package com.qinyun.bbs.config;

import com.qinyun.bbs.es.service.EsService;
import com.qinyun.bbs.es.service.SearchService;
import com.qinyun.bbs.lucene.LuceneService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SearchConfig {
	
	
	@Bean
	@ConditionalOnProperty(prefix = "search" , name = "type",havingValue = "es",matchIfMissing = false)
	public SearchService getEs() {
		return new EsService();
	}
	
	@Bean
	@ConditionalOnProperty(prefix = "search" , name = "type",havingValue = "lucene",matchIfMissing = false)
	public SearchService getLucene() {
		return new LuceneService();
	}

}
