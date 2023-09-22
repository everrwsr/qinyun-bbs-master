package com.qinyun.bbs.es.entity;

import java.util.Date;

import com.qinyun.bbs.es.annotation.EntityType;
import com.qinyun.bbs.util.EsUtil;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class BbsIndex {

    transient String id;
    Integer topicId;
    Integer postId;
    Integer replyId;
    Integer userId;
    String  content;
    Date    createTime;

    Integer pros     = 0;//顶次数
    Integer cons     = 0;//踩次数
    Integer isAccept = 0;//0：未采纳，1：采纳
    Integer pv       = 0;//访问量

    EntityType entityType;
    
    public String getId() {
        if (this.id == null) {
            this.id = EsUtil.getEsKey(topicId, postId, replyId);
        }
        return id;
    }

    public BbsIndex(Integer topicId, Integer postId, Integer replyId, Integer userId, String content, Date createTime, Integer pros, Integer cons, Integer isAccept, Integer pv) {
        super();
        this.topicId = topicId;
        this.postId = postId;
        this.replyId = replyId;
        this.userId = userId;
        this.content = content;
        this.createTime = createTime;
        this.pros = pros;
        this.cons = cons;
        this.isAccept = isAccept;
        this.pv = pv;
        this.id = EsUtil.getEsKey(topicId, postId, replyId);
    }

}
