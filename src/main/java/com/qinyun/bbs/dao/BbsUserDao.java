package com.qinyun.bbs.dao;

import com.qinyun.bbs.model.BbsUser;
import org.beetl.sql.mapper.BaseMapper;
import org.beetl.sql.mapper.annotation.Sql;


import java.util.List;


public interface BbsUserDao extends BaseMapper<BbsUser> {


    List<BbsUser> getScoreTop(Integer max);

    List<BbsUser> getLevelTop(Integer max);

    @Sql("select count(1) from bbs_user where ip=? "
            + "and DATE_FORMAT(register_time,'%Y-%m-%d') = ?")
    public int getIpCount(String ip, String date);
}
