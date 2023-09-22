package com.qinyun.bbs.es.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author everr
 */
@ConfigurationProperties(prefix = "elasticsearch.bbs", ignoreInvalidFields = true)
@Configuration
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EsConfig {

    String url              = "http://127.0.0.1:9200/bbs";
    String contentUrl       = "http://127.0.0.1:9200/bbs/content/";
    String contentSearchUrl = "http://127.0.0.1:9200/bbs/content/_search";
}
