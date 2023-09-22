package com.qinyun.bbs.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.beetl.sql.annotation.entity.AutoID;
import org.beetl.sql.core.TailBean;
import org.beetl.sql.fetch.annotation.Fetch;
import org.beetl.sql.fetch.annotation.FetchOne;

import java.util.Date;
import java.util.List;

/*
 *
 * gen by beetlsql 2016-06-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Fetch
public class BbsPost extends TailBean {
	@AutoID
    Integer id;
    Integer hasReply;
    Integer topicId;
    Integer userId;
    String  content;
    Date    createTime;
    Date    updateTime;

    Integer pros     = 0;//顶次数
    Integer cons     = 0;//踩次数
    Integer isAccept = 0;//0：未采纳，1：采纳


    List<BbsReply> replys;
    @FetchOne("userId")
    BbsUser bbsUser;
	@FetchOne("topicId")
	BbsTopic bbsTopic;

}
