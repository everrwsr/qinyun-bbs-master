package com.qinyun.bbs.es.config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qinyun.bbs.es.annotation.EsOperateType;
import com.qinyun.bbs.es.entity.BbsIndex;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.qinyun.bbs.es.annotation.EsFallback;
import com.qinyun.bbs.es.annotation.EsIndexType;
import com.qinyun.bbs.es.service.SearchService;
import com.qinyun.bbs.es.vo.EsIndexTypeData;
import com.qinyun.bbs.util.EsUtil;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Aspect
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AOPConfig {

	SearchService searchService;


    /**
     * ES的切入点
     */
    @Pointcut("@annotation(com.qinyun.bbs.es.annotation.EsIndexType) || @annotation(com.qinyun.bbs.es.annotation.EsIndexs)")
    private void anyMethod() {
    }

    @Around("anyMethod()")
    public Object simpleAop(ProceedingJoinPoint pjp) throws Throwable {
        try {
            Signature     sig    = pjp.getSignature();
            Object        target = pjp.getTarget();//代理类
            Method        method = ((MethodSignature) sig).getMethod();//代理方法
            EsIndexType[] types  = method.getAnnotationsByType(EsIndexType.class);

            Map<String, Object> parameters = this.getParameterNames(pjp);

            //获取索引的数据集合
            List<EsIndexTypeData> typeDatas = new ArrayList<>();

            //当操作为删除时，需要提前获取id
            for (EsIndexType index : types) {
                if (index.operateType() == EsOperateType.DELETE) {
                    String key = index.key();

                    Integer id = (Integer) parameters.get(key);
                    if (id == null) {
                        log.error(target.getClass().getName() + "$" + method.getName() + "：未获取到主键，无法更新索引");
                    } else {
                        BbsIndex bbsIndex = searchService.createBbsIndex(index.entityType(), id);
                        String          md5Id    = EsUtil.getEsKey(bbsIndex.getTopicId(), bbsIndex.getPostId(), bbsIndex.getReplyId());
                        EsIndexTypeData data     = new EsIndexTypeData(index.entityType(), index.operateType(), md5Id);
                        typeDatas.add(data);
                    }
                }
            }
            //调用原方法
            Object o = pjp.proceed();
            //当操作为更新时，可以从返回值中获取id
            for (EsIndexType index : types) {
                if (index.operateType() != EsOperateType.DELETE) {
                    Integer id  = null;
                    String  key = index.key();
                    id = (Integer) parameters.get(key);
                    boolean resultErr = false;
                    if (id == null) {
                        if (o instanceof ModelAndView) {
                            ModelAndView modelAndView = (ModelAndView) o;
                            id = (Integer) modelAndView.getModel().get(key);
                        } else if (o instanceof JSONObject) {
                            JSONObject json = (JSONObject) o;
                            id = json.getInteger(key);
                            resultErr = 1 == json.getInteger("err");
                        }
                    }
                    if (id == null) {
                        if (!resultErr) {
                            log.error(target.getClass().getName() + "$" + method.getName() + "：未获取到主键，无法更新索引");
                        }

                    } else {
                        EsIndexTypeData data = new EsIndexTypeData(index.entityType(), index.operateType(), id);
                        typeDatas.add(data);
                    }

                }
            }

            //更新索引
            for (EsIndexTypeData esIndexTypeData : typeDatas) {
            	searchService.editEsIndex(esIndexTypeData.getEntityType(), esIndexTypeData.getOperateType(), esIndexTypeData.getId());
            }

            return o;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 调用ES失败降级的处理（如ES服务挂了）
     */
    @Around("@annotation(com.qinyun.bbs.es.annotation.EsFallback)")
    public Object fallback(ProceedingJoinPoint pjp) {

        Signature sig    = pjp.getSignature();
        Object    target = pjp.getTarget();//代理类
        Method    method = ((MethodSignature) sig).getMethod();//代理方法

        //调用原方法
        try {
            return pjp.proceed();
        } catch (Throwable throwable) {
            EsFallback fallback   = method.getAnnotation(EsFallback.class);
            String     methodName = fallback.fallbackMethod();
            if (StringUtils.isBlank(methodName)) {
                methodName = method.getName() + "Fallback";
            }
            try {
                log.warn("ES服务[{}]调用失败;{}，开始进行降级处理...",method.getName(),throwable.getMessage());
                Method fallbackMethod = target.getClass().getMethod(methodName, method.getParameterTypes());
                if (fallbackMethod.getReturnType() == method.getReturnType()) {
                    method.setAccessible(Boolean.TRUE);
                    return fallbackMethod.invoke(target, pjp.getArgs());
                } else {
                    throw new RuntimeException(throwable);
                }
            } catch (NoSuchMethodException e) {
                //找不到Fallback方法时抛出原异常
                throw new RuntimeException(throwable);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * 获取方法的参数
     *
     * @param pjp
     * @return
     * @throws Exception
     */
    private Map<String, Object> getParameterNames(ProceedingJoinPoint pjp) throws Exception {

        String[] names = null;//参数名
        Object[] args  = pjp.getArgs();//参数值

        Signature       signature       = pjp.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;

        String   jv  = System.getProperty("java.version");
        String[] jvs = jv.split("\\.");
        if (Integer.parseInt(jvs[0] + jvs[1]) >= 18) {//jdk8直接获取参数名
            names = methodSignature.getParameterNames();
        } else {
            LocalVariableTableParameterNameDiscoverer localVariableTableParameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
            Object                                    target                                    = pjp.getTarget();//代理类
            Method                                    currentMethod                             = target.getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
            names = localVariableTableParameterNameDiscoverer.getParameterNames(currentMethod);
        }
        if (names == null) {
            log.error("{}${}：未获取到参数名称列表", pjp.getTarget().getClass().getName(), signature.getName());
            return Collections.emptyMap();
        } else if (names.length != args.length) {
            log.error("{}${}：参数名称列表长度与参数值列表长度不相等", pjp.getTarget().getClass().getName(), signature.getName());
            return Collections.emptyMap();
        } else {
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < names.length; i++) {
                map.put(names[i], args[i]);
            }
            return map;
        }
    }


}
