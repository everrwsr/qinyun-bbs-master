package com.qinyun.bbs.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.beetl.sql.annotation.entity.AutoID;

/*
 *
 * gen by beetlsql 2016-12-27
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BbsMessage {
	@AutoID
    Integer id;
    Integer status;
    Integer topicId;
    Integer userId;

}
