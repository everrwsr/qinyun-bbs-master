package com.qinyun.bbs.service.impl;

import com.qinyun.bbs.dao.BbsModuleDao;
import com.qinyun.bbs.dao.BbsPostDao;
import com.qinyun.bbs.dao.BbsReplyDao;
import com.qinyun.bbs.dao.BbsTopicDao;
import com.qinyun.bbs.dao.BbsUserDao;
import com.qinyun.bbs.model.BbsMessage;
import com.qinyun.bbs.model.BbsModule;
import com.qinyun.bbs.model.BbsPost;
import com.qinyun.bbs.model.BbsReply;
import com.qinyun.bbs.model.BbsTopic;
import com.qinyun.bbs.model.BbsUser;
import com.qinyun.bbs.service.BBSService;
import com.qinyun.bbs.service.BbsUserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.engine.PageQuery;
import org.beetl.sql.core.page.PageResult;
import org.beetl.sql.core.query.LambdaQuery;
import org.beetl.sql.core.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BBSServiceImpl implements BBSService {
    BbsTopicDao topicDao;
    BbsPostDao postDao;
    BbsUserDao userDao;
    BbsModuleDao moduleDao;
    BbsReplyDao replyDao;
    SQLManager     sql;
    BbsUserService gitUserService;

    @Cacheable(cacheNames = "bbsTopic", key = "#topicId")
    @Override
    public BbsTopic getTopic(Integer topicId) {
        return topicDao.getTopic(topicId);
    }

    @Cacheable(cacheNames = "bbsPost", key = "#postId")
    @Override
    public BbsPost getPost(int postId) {



        return
                postDao.unique(postId);
    }

    @Cacheable(cacheNames = "bbsReply", key = "#replyId")
    @Override
    public BbsReply getReply(int replyId) {
        return replyDao.unique(replyId);
    }


    @Cacheable(cacheNames = "bbsTopicPage", keyGenerator = "pageQueryKeyGenerator")
    @Override
    public PageQuery getTopics(PageQuery query) {
        topicDao.queryTopic(query);
        return query;
    }

    @Cacheable(cacheNames = "bbsTopicMessageList", key = "#userId")
    @Override
    public List<BbsTopic> getMyTopics(int userId) {
        return topicDao.queryMyMessageTopic(userId);
    }

    @Cacheable(cacheNames = "bbsTopicMessageCount", key = "#userId")
    @Override
    public Integer getMyTopicsCount(int userId) {
        return topicDao.queryMyMessageTopicCount(userId);
    }

    @CacheEvict(cacheNames = {"bbsTopicMessageList", "bbsTopicMessageCount", "bbsTopic"}, allEntries = true)
    @Override
    public void updateMyTopic(int msgId, int status) {
        BbsMessage msg = new BbsMessage();
        msg.setStatus(status);
        msg.setId(msgId);
        sql.updateTemplateById(msg);
    }

    @CacheEvict(cacheNames = {"bbsTopicMessageList", "bbsTopicMessageCount"}, allEntries = true)
    @Override
    public BbsMessage makeOneBbsMessage(int userId, int topicId, int status) {
        BbsMessage msg = new BbsMessage();
        msg.setUserId(userId);
        msg.setTopicId(topicId);
        List<BbsMessage> list = sql.template(msg);
        if (list.isEmpty()) {
            msg.setStatus(status);
            sql.insert(msg);
            return msg;
        } else {
            msg = list.get(0);
            if (msg.getStatus() != status) {
                msg.setStatus(status);
                sql.updateById(msg);
            }
            return msg;
        }

    }

    @Override
    public void notifyParticipant(int topicId, int ownerId) {
        List<Integer> userIds = topicDao.getParticipantUserId(topicId);
        for (Integer userId : userIds) {
            if (userId == ownerId) {
                continue;
            }
            makeOneBbsMessage(userId, topicId, 1);
        }
    }

    @Cacheable(cacheNames = "bbsHotTopicPage", keyGenerator = "pageQueryKeyGenerator")
    @Override
    public PageQuery getHotTopics(PageQuery query) {
        query.setPara("type", "hot");
        topicDao.queryTopic(query);
        return query;
    }

    @Cacheable(cacheNames = "bbsNiceTopicPage", keyGenerator = "pageQueryKeyGenerator")
    @Override
    public PageQuery getNiceTopics(PageQuery query) {
        query.setPara("type", "nice");
        topicDao.queryTopic(query);
        return query;
    }

//    @Cacheable(cacheNames = "bbsPostPage", keyGenerator = "pageQueryKeyGenerator")
    @Override
    public PageQuery getPosts(PageQuery query) {
        postDao.getPosts(query);
        if (query.getList() != null) {
            for (Object topicObj : query.getList()) {
                final BbsPost  post   = (BbsPost) topicObj;
                List<BbsReply> replys = replyDao.allReply(post.getId());
                post.setReplys(replys);

            }
        }
        return query;
    }

    @Override
    public void saveUser(BbsUser user) {
        userDao.insert(user);
    }

    @Override
    public BbsUser login(BbsUser user) {
        List<BbsUser> users = sql.template(user);
        if (CollectionUtils.isEmpty(users)) {
            return null;
        }
        return users.get(0);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = {"bbsTopic", "bbsTopicPage", "bbsHotTopicPage", "bbsNiceTopicPage"}, allEntries = true),
            @CacheEvict(cacheNames = {"bbsPost", "bbsPostPage", "bbsFirstPost", "bbsLatestPost"}, allEntries = true)
    })
    public void saveTopic(BbsTopic topic, BbsPost post, BbsUser user) {
        topic.setUserId(user.getId());
        topic.setCreateTime(new Date());
        topicDao.insert(topic);
        post.setUserId(user.getId());
        post.setTopicId(topic.getId());
        post.setCreateTime(new Date());
        postDao.insert(post);
        gitUserService.addTopicScore(user.getId());
    }

    @Override
    public int getTopicCount(BbsUser user, Date date) {
        return this.topicDao.getTopicCount(user.getId(), date);
    }

    @Override
    public int getPostCount(BbsUser user, Date date) {
        return 0;
    }

    @Override
    public int getReplyCount(BbsUser user, Date date) {
        return 0;
    }


    @Override
    @CacheEvict(cacheNames = {"bbsPost", "bbsPostPage", "bbsFirstPost", "bbsLatestPost"}, allEntries = true)
    public void savePost(BbsPost post, BbsUser user) {
        post.setUserId(user.getId());
        postDao.insert(post);
        gitUserService.addPostScore(user.getId());
    }


    @CacheEvict(cacheNames = {"bbsReply", "bbsPostPage"}, allEntries = true)
    @Override
    public void saveReply(BbsReply reply) {
        replyDao.insert(reply);
        gitUserService.addReplayScore(reply.getUserId());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = {"bbsTopic", "bbsTopicPage", "bbsHotTopicPage", "bbsNiceTopicPage"}, allEntries = true),
            @CacheEvict(cacheNames = {"bbsPost", "bbsPostPage", "bbsFirstPost", "bbsLatestPost"}, allEntries = true),
            @CacheEvict(cacheNames = {"bbsReply"}, allEntries = true)
    })
    public void deleteTopic(int id) {
        sql.deleteById(BbsTopic.class, id);
        postDao.deleteByTopicId(id);
        replyDao.deleteByTopicId(id);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = {"bbsTopic", "bbsTopicPage", "bbsHotTopicPage", "bbsNiceTopicPage"}, allEntries = true),
            @CacheEvict(cacheNames = {"bbsPost", "bbsPostPage", "bbsFirstPost", "bbsLatestPost"}, allEntries = true),
            @CacheEvict(cacheNames = {"bbsReply"}, allEntries = true)
    })
    public void deletePost(int id) {
        BbsPost postTopic = postDao.createLambdaQuery()
                .andEq(BbsPost::getId, id)
                .single("topic_id");
        //删除当前post
        sql.deleteById(BbsPost.class, id);
        //删除当前post下的所有 reply
        replyDao.deleteByPostId(id);
        //检查当前post所在的topic下是否还有其他post，如果没有，则删除当前topic
        if (postDao.templateCount(postTopic) == 0) {
            topicDao.deleteById(postTopic.getTopicId());
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = {"bbsTopic", "bbsTopicPage", "bbsHotTopicPage", "bbsNiceTopicPage"}, allEntries = true),
            @CacheEvict(cacheNames = {"bbsPost", "bbsPostPage", "bbsFirstPost", "bbsLatestPost"}, allEntries = true),
            @CacheEvict(cacheNames = {"bbsReply"}, allEntries = true)
    })
    public void deleteReplay(int id) {
        sql.deleteById(BbsReply.class, id);
    }

    @Override
    @Cacheable(cacheNames = "bbsLatestPost", key = "#userId")
    public Date getLatestPost(int userId) {
        return postDao.getLatestPostDate(userId);
    }

    @CacheEvict(cacheNames = {"bbsTopic", "bbsTopicPage", "bbsHotTopicPage", "bbsNiceTopicPage"}, allEntries = true)
    public void updateTopic(BbsTopic topic) {
        sql.updateById(topic);
    }

    @CacheEvict(cacheNames = {"bbsPost", "bbsPostPage", "bbsFirstPost", "bbsLatestPost"}, allEntries = true)
    public void updatePost(BbsPost post) {
        sql.updateById(post);
    }

    @Override
    @Cacheable(cacheNames = "bbsFirstPost", key = "#topicId")
    public BbsPost getFirstPost(Integer topicId) {
        Query<BbsPost> query = sql.query(BbsPost.class);
        return query.andEq("topic_id", topicId)
                .orderBy("create_time asc")
                .select()
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<BbsModule> allModule() {
        return this.moduleDao.all();
    }

    @Override
    @Cacheable(cacheNames = "module")
    public BbsModule getModule(Integer id) {
        return allModule()
                .stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElse(null);
    }


    @Override
    @Cacheable(cacheNames = "fallbackQuery", key = "#keyWord.concat(#pageNum)")
    public PageResult<BbsPost> queryPostByContent(String keyWord, long pageNum, long pageSize) {
        LambdaQuery<BbsPost> query = sql.lambdaQuery(BbsPost.class);
        if (StringUtils.isNotBlank(keyWord)) {
            query.andLike(BbsPost::getContent, String.format("%%%s%%", keyWord));
        }
        return query.page(pageNum, pageSize);
    }
}
