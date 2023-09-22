package com.qinyun.bbs.dao;

import com.qinyun.bbs.model.BbsPost;

import org.beetl.sql.core.engine.PageQuery;
import org.beetl.sql.mapper.BaseMapper;
import org.beetl.sql.mapper.annotation.Param;
import org.beetl.sql.mapper.annotation.Sql;
import org.beetl.sql.mapper.annotation.Update;

import java.util.Date;

public interface BbsPostDao extends BaseMapper<BbsPost> {

    void getPosts(PageQuery query);

    @Update
    void deleteByTopicId(@Param("topicId") int topicId);

    @Sql(value = "select max(create_time) from bbs_post where user_id=? order by id desc ")
    Date getLatestPostDate(int userId);

}
