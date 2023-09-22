package com.qinyun.bbs.config;

import com.qinyun.bbs.common.WebUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

@Configuration
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebMvcConfig implements WebMvcConfigurer {

    WebUtils webUtils;

    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptorAdapter() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                                     Object handler) throws Exception {
                if (webUtils.currentUser() == null) {
                    response.sendRedirect(request.getContextPath());
                    return false;
                }
                return true;
            }
        }).addPathPatterns(
                "/bbs/topic/add","/topic/add",
                "/bbs/myMessage","/myMessage",
                "/bbs/admin/**","/admin/**"
        );
    }

    /**
     * 跨域访问
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("", "")
                .allowedMethods("*");

    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //外部静态资源的处理（用于图片上传后的URL映射）
        registry.addResourceHandler("/bbs/showPic/**", "/showPic/**")
                .addResourceLocations("file:upload" + File.separator);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        //无逻辑处理的控制器路由
        registry.setOrder(-1);//解决 => /bbs/topic/{id}导致匹配不到此处路由的问题
        registry.addViewController("/topic/add").setViewName("/post.html");
        registry.addViewController("/bbs/topic/add").setViewName("/post.html");
        registry.addViewController("/bbs/share").setViewName("forward:/bbs/topic/module/1");
    }


    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        //开启允许URL添加后缀也能访问到相应的Controller，以便兼容旧版本
        configurer.setUseSuffixPatternMatch(Boolean.TRUE);
    }
}
