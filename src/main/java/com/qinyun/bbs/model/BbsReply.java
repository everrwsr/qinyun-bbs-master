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

/*
 *
 * gen by beetlsql 2016-06-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Fetch
public class BbsReply extends TailBean{
	@AutoID
    Integer  id;
    Integer  postId;
    Integer  topicId;
    Integer  userId;
    String   content;
    Date     createTime;

    @FetchOne("userId")
    BbsUser  user;
    @FetchOne("topicId")
    BbsTopic topic;
}
