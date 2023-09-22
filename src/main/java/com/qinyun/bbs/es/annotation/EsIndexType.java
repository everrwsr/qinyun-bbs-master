package com.qinyun.bbs.es.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 创建索引的注解
 *
 * @author yangkebiao
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Repeatable(EsIndexs.class)
public @interface EsIndexType {

    EntityType entityType();                    //实体类型

    EsOperateType operateType();            //操作类型

    String key() default "id";                    //获取主键的名称

}
