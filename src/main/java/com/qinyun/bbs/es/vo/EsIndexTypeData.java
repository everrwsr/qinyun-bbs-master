package com.qinyun.bbs.es.vo;

import com.qinyun.bbs.es.annotation.EntityType;
import com.qinyun.bbs.es.annotation.EsOperateType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * EsIndexType注解的数据
 *
 * @author yangkebiao
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class EsIndexTypeData {

    EntityType entityType;             //实体类型
    EsOperateType operateType;            //操作类型
    Object        id;                     //获取主键


}
