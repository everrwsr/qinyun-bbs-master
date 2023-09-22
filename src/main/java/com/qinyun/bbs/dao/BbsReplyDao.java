package com.qinyun.bbs.dao;

import com.qinyun.bbs.model.BbsReply;
import org.beetl.sql.mapper.BaseMapper;
import org.beetl.sql.mapper.annotation.Param;
import org.beetl.sql.mapper.annotation.Update;


import java.util.List;

public interface BbsReplyDao extends BaseMapper<BbsReply> {

    List<BbsReply> allReply(@Param("postId") Integer postId);
    @Update
    void deleteByTopicId(@Param("topicId") int topicId);
    @Update
    void deleteByPostId(@Param("postId") int postId);
}
