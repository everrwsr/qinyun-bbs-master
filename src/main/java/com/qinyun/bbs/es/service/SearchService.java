package com.qinyun.bbs.es.service;

import com.qinyun.bbs.es.annotation.EsOperateType;
import com.qinyun.bbs.es.entity.BbsIndex;
import org.beetl.sql.core.engine.PageQuery;

import com.qinyun.bbs.es.annotation.EntityType;
import com.qinyun.bbs.es.vo.IndexObject;


public interface SearchService {

    /**
     * 公共操作方法
     */

    public void editEsIndex(EntityType entityType, EsOperateType operateType, Object id);

    public default void editEsIndexFallback(EntityType entityType, EsOperateType operateType, Object id) {
    	throw new RuntimeException("操作失败");
    }

    /**
     * 重构索引
     */
    public void initIndex() ;

    public default void initIndexFallback() {
    	throw new RuntimeException("操作失败");
    }

    /**
     * 创建索引对象
     */
    public BbsIndex createBbsIndex(EntityType entityType, Integer id) ;

    /**
     * 创建所有并返回搜索结果
     */
    public PageQuery<IndexObject> getQueryPage(String keyword, int p) ;

    public default  PageQuery<IndexObject> getQueryPageFallback(String keyword, int p){
    	if (p <= 0) {
            p = 1;
        }
        int                    pageNumber = p;
        long                   pageSize   = PageQuery.DEFAULT_PAGE_SIZE;
        PageQuery<IndexObject> pageQuery  = new PageQuery<>(pageNumber, pageSize);
        return pageQuery;
    }


}
