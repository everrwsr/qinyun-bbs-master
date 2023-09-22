package com.qinyun.bbs.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/*
 *
 * gen by beetlsql 2016-06-13
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BbsModule {
    Integer id;
    Integer turn;
    String  detail;
    String  name;
    Integer readonly;
    String  adminList;

    //只允许特定用法发帖的，通常用于新闻，广告
	Set<String> adminSet;

    public void setAdminList(String adminList) {
        this.adminList = adminList;
        if (StringUtils.isNotBlank(adminList)) {
            this.adminSet = Arrays.stream(adminList.split(","))
                    .collect(Collectors.toSet());
        }
    }

    public boolean contains(String userName) {
        return adminSet.contains(userName);
    }
}
