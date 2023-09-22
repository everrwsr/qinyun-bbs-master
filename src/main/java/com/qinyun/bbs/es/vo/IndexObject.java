package com.qinyun.bbs.es.vo;

import com.qinyun.bbs.model.BbsModule;
import com.qinyun.bbs.model.BbsUser;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Date;

/**
 * 索引对象
 *
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class IndexObject {

    Integer topicId;
    Integer isUp;
    Integer isNice;
    Integer userId;
    String  userName;
    Date    createTime;
    Integer postCount;
    Integer pv;
    Integer moduleId;
    String  moduleName;

    String  topicContent;
    String  postContent;// 可能为postContent或replayContent的内容
    Integer indexType;        //用于判断该索引对象是主题贴还是回复贴:	1：主题帖，2：回复贴，3：对回复的回复贴
    double  score;//相似度


    public IndexObject(Integer topicId, Integer isUp, Integer isNice, BbsUser user, Date createTime,
                       Integer postCount, Integer pv, BbsModule module, String topicContent, String postContent,
                       Integer indexType, double score) {
        super();
        this.topicId = topicId;
        this.isUp = isUp;
        this.isNice = isNice;
        if (user != null) {
            this.userId = user.getId();
            this.userName = user.getUserName();
        } else {
            this.userId = -1;
            this.userName = "未知";
        }
        if (module != null) {
            //不太可能物理删除moudle
            this.moduleId = module.getId();
            this.moduleName = module.getName();
        }

        this.createTime = createTime;
        this.postCount = postCount;
        this.pv = pv;
        this.topicContent = topicContent;
        this.postContent = postContent;
        this.indexType = indexType;
        this.score = score;
    }


}
