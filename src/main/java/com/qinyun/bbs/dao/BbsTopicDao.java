package com.qinyun.bbs.dao;

import com.qinyun.bbs.model.BbsTopic;

import org.beetl.sql.core.engine.PageQuery;
import org.beetl.sql.mapper.BaseMapper;
import org.beetl.sql.mapper.annotation.Sql;


import java.util.Date;
import java.util.List;

public interface BbsTopicDao extends BaseMapper<BbsTopic> {
    void queryTopic(PageQuery query);

    List<BbsTopic> queryMyMessageTopic(int userId);

    Integer queryMyMessageTopicCount(int userId);

    List<Integer> getParticipantUserId(Integer topicId);

    BbsTopic getTopic(Integer topicId);

    @Sql("SELECT count(1) FROM bbs_topic where user_id =? and create_time>? ")
    int getTopicCount(Integer userId, Date before);

}
