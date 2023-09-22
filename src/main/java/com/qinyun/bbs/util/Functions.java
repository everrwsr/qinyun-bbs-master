package com.qinyun.bbs.util;

import com.qinyun.bbs.common.WebUtils;
import com.qinyun.bbs.model.BbsModule;
import com.qinyun.bbs.model.BbsUser;
import com.qinyun.bbs.service.BBSService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * beetl 自定义函数
 *
 * @author L.cm
 * email: 596392912@qq.com
 * site:http://www.dreamlu.net
 * date 2015年7月4日下午6:05:07
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Functions {

    WebUtils webUtils;
    BBSService bbsService;

    /**
     * 继续encode URL (url,传参tomcat会自动解码)
     * 要作为参数传递的话，需要再次encode
     */
    public String encodeUrl(String url) {
        try {
            url = URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // ignore
        }
        return url;
    }


    /**
     * 模版中拿取cookie中的用户信息
     */
    public BbsUser currentUser(HttpServletRequest request, HttpServletResponse response) {
        return webUtils.currentUser();
    }


    public boolean allowPost(BbsModule module, HttpServletRequest request, HttpServletResponse response) {
        BbsUser user = currentUser(request, response);
        if (user == null) {
            return false;
        }
        if (user.getUserName().equals("admin")) {
            return true;
        }

        if (module == null) {
            return true;
        }
        if (module.getReadonly() == 0) {
            return true;
        }

        if (module.contains(user.getUserName())) {
            return true;
        }

        return false;


    }

    public Integer myMessageCount(Integer userId) {
        return bbsService.getMyTopicsCount(userId);
    }

}
