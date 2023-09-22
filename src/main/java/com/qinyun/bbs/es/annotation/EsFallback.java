package com.qinyun.bbs.es.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ES服务调用失败后的降级处理
 *
 * @author everr
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EsFallback {
    /**
     * 失败后调用的方法名称，如果不设置，则默认为当前方法名称+Fallback
     */
    String fallbackMethod() default "";
}
