package com.qinyun.bbs.service;

import com.qinyun.bbs.model.BbsMessage;
import com.qinyun.bbs.model.BbsModule;
import com.qinyun.bbs.model.BbsPost;
import com.qinyun.bbs.model.BbsReply;
import com.qinyun.bbs.model.BbsTopic;
import com.qinyun.bbs.model.BbsUser;
import org.beetl.sql.core.engine.PageQuery;
import org.beetl.sql.core.page.PageResult;

import java.util.Date;
import java.util.List;

public interface BBSService {
    BbsTopic getTopic(Integer topicId);

    BbsPost getPost(int postId);

    BbsReply getReply(int replyId);

    PageQuery getTopics(PageQuery query);

    List<BbsTopic> getMyTopics(int userId);

    Integer getMyTopicsCount(int userId);

    void updateMyTopic(int msgId, int status);

    BbsMessage makeOneBbsMessage(int userId, int topicId, int statu);


    void notifyParticipant(int topicId, int ownerId);

    PageQuery getHotTopics(PageQuery query);

    PageQuery getNiceTopics(PageQuery query);

    PageQuery getPosts(PageQuery query);

    void saveUser(BbsUser user);

    BbsUser login(BbsUser user);

    void saveTopic(BbsTopic topic, BbsPost post, BbsUser user);

    /**
     * 一定时间内的提交Topic总数
     */
    int getTopicCount(BbsUser user, Date date);

    /**
     * 一定时间内的
     */
    int getPostCount(BbsUser user, Date date);

    /**
     * 一定时间内的提交总数
     */
    int getReplyCount(BbsUser user, Date date);

    void savePost(BbsPost post, BbsUser user);


    void saveReply(BbsReply reply);

    void deleteTopic(int id);

    void deletePost(int id);

    void deleteReplay(int id);

    void updateTopic(BbsTopic topic);

    void updatePost(BbsPost post);

    Date getLatestPost(int userId);

    BbsPost getFirstPost(Integer topicId);

    List<BbsModule> allModule();

    BbsModule getModule(Integer id);

	PageResult<BbsPost> queryPostByContent(String keyWord, long pageNum, long pageSize);

}
