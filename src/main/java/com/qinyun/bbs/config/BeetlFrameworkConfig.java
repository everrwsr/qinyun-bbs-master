package com.qinyun.bbs.config;

import com.qinyun.bbs.common.Const;
import com.qinyun.bbs.dao.BbsModuleDao;
import com.qinyun.bbs.util.Functions;
import com.ibeetl.starter.BeetlTemplateCustomize;
import com.zaxxer.hikari.HikariDataSource;
import org.beetl.core.GroupTemplate;
import org.beetl.core.resource.ClasspathResourceLoader;
import org.beetl.ext.spring.BeetlGroupUtilConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class BeetlFrameworkConfig {

    @Bean
    public BeetlTemplateCustomize beetlTemplateCustomize(@Qualifier("functions") Functions fn, final BbsModuleDao moduleDao) {
        return groupTemplate -> {
            groupTemplate.registerFunctionPackage("c", fn);
            Map<String, Object> vars = new HashMap<String, Object>() {{
                put("v", Const.TIMESTAMP);
                put("moduleList", moduleDao.all());
            }};
            groupTemplate.setSharedVars(vars);

        };
    }


    @Bean(name = "dataSource")
    public DataSource datasource(Environment env) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(env.getProperty("spring.datasource.url"));
        ds.setUsername(env.getProperty("spring.datasource.username"));
        ds.setPassword(env.getProperty("spring.datasource.password"));
        ds.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));
        return ds;
    }

    /**
     * ES内容模板配置
     */
    @Bean
    public GroupTemplate beetlTemplate() {
        BeetlGroupUtilConfiguration beetlGroupUtilConfiguration = new BeetlGroupUtilConfiguration();
        ResourcePatternResolver     patternResolver             = ResourcePatternUtils.getResourcePatternResolver(new DefaultResourceLoader());
        beetlGroupUtilConfiguration.setConfigFileResource(patternResolver.getResource("classpath:beetl.properties"));
        ClasspathResourceLoader cploder = new ClasspathResourceLoader("/elasticsearch");
        beetlGroupUtilConfiguration.setResourceLoader(cploder);
        beetlGroupUtilConfiguration.init();
        return beetlGroupUtilConfiguration.getGroupTemplate();
    }


}
