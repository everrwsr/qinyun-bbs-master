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
public class BbsTopic extends TailBean {

	@AutoID
    Integer   id;
    Integer   emotion;
    Integer   isNice;
    Integer   isUp;
    Integer   moduleId;
    Integer   postCount;
    Integer   pv;
    Integer   replyCount;
    Integer   userId;
    String    content;
    Date      createTime;
    @FetchOne("userId")
    BbsUser   user;
	@FetchOne("moduleId")
    BbsModule module;
}
