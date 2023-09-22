package com.qinyun.bbs.service.impl;

import com.qinyun.bbs.dao.BbsUserDao;
import com.qinyun.bbs.model.BbsUser;
import com.qinyun.bbs.service.BbsUserService;
import com.qinyun.bbs.util.DateUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.beetl.sql.core.SQLManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BbsUserServiceImpl implements BbsUserService {


    BbsUserDao userDao;
    SQLManager sqlManager;

    /**
     * 分5个级别
     */
    private int getLevel(int score) {
        if (score >= BbsUserService.PRESIDENT_THRESHOLD) {
            return 5;
        } else if (score >= BbsUserService.DIRECTOR_THRESHOLD) {
            return 4;
        } else if (score >= BbsUserService.TEACHER_THRESHOLD) {
            return 3;
        }
        if (score >= BbsUserService.OLD_THRESHOLD) {
            return 2;
        } else {
            return 1;
        }
    }


    @Override
    public BbsUser setUserAccount(BbsUser user) {
        userDao.insert(user);
        return user;

    }

    @Override
    public int countByIp(String ip) {
        Date   date  = new Date();
        String today = DateUtil.format(date);
        return userDao.getIpCount(ip, today);
    }


    @Override
    public BbsUser getUserAccount(String userName, String password) {
        BbsUser query = new BbsUser();
        query.setUserName(userName);
        query.setPassword(password);
        return userDao.template(query)
                .stream()
                .findFirst()
                .orElse(null);
    }


    @Override
    public void addTopicScore(long userId) {
        addScore(userId, BbsUserService.BBS_TOPIC_SCORE);

    }

    @Override
    public void addPostScore(long userId) {
        addScore(userId, BbsUserService.BBS_POST_SCORE);

    }

    @Override
    public void addReplayScore(long userId) {
        addScore(userId, BbsUserService.BBS_REPLAY_SCORE);

    }

    private void addScore(long userId, int total) {
        BbsUser user    = userDao.unique(userId);
        int     score   = user.getScore() + total;
        int     balance = user.getBalance() + total;
        user.setScore(score);
        user.setBalance(balance);
        user.setLevel(getLevel(score));
        userDao.updateById(user);
    }


    @Override
    public boolean hasUser(String userName) {
        BbsUser user = new BbsUser();
        user.setUserName(userName);
        return !userDao.template(user).isEmpty();
    }


    @Override
    public BbsUser getUser(Integer id) {
        return sqlManager.unique(BbsUser.class, id);
    }

    @Override
    public void removeUser(Integer id) {
        BbsUser user = userDao.unique(id);
        user.setPassword("delete");
        userDao.updateById(user);
    }


}
