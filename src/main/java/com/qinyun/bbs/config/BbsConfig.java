package com.qinyun.bbs.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 每天用户能做的操作个数，如果大于配置个数，需要验证码
 */
@ConfigurationProperties(prefix = "bbs.user")
@Configuration
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BbsConfig {

    Integer registerSameIp    = 2;
    Integer topicCount        = 2;
    Integer topicCountMinutes = 1;
    Integer postCount         = 2;

}
