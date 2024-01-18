package com.qinyun.bbs.action;

import com.alibaba.fastjson.JSONObject;
import com.qinyun.bbs.common.WebUtils;
import com.qinyun.bbs.config.BbsConfig;
import com.qinyun.bbs.model.BbsUser;
import com.qinyun.bbs.service.BbsUserService;
import com.qinyun.bbs.util.AddressUtil;
import com.qinyun.bbs.util.HashKit;
import com.qinyun.bbs.util.VerifyCodeUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;





@Controller
@RequestMapping({"/bbs/user","/user"})
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class LoginController {

    BbsUserService bbsUserService;
    BbsConfig bbsConfig;
    WebUtils webUtils;
    HttpServletRequest  request;
    HttpServletResponse response;

    private static final String CODE_NAME      = "verCode";
    static final         String POST_CODE_NAME = "postVerCode";

    /**
     * 登录方法改为ajax方式登录
     */
    @ResponseBody
    @PostMapping("/login")
    public JSONObject login(String userName, String password) {
        JSONObject result = new JSONObject();
        result.put("err", 1);
        if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(password)) {
            result.put("msg", "请输入正确的内容！");
        } else {
            password = HashKit.md5(password);
            BbsUser user = bbsUserService.getUserAccount(userName, password);
            if (user == null) {
                result.put("msg", "用户不存在或密码错误");
            } else {
                webUtils.loginUser(user, true);
                result.put("msg", "index");
                result.put("err", 0);
            }
        }
        return result;
    }


    /**
     * 登出方法改为ajax方式登出
     */
    @ResponseBody
    @PostMapping("/logout")
    public void logout() {
        webUtils.logoutUser();
    }

    /**
     * 注册改为 ajax 方式注册
     */
    @ResponseBody
    @PostMapping("/doRegister")
    public JSONObject register(BbsUser user, String code) {
        JSONObject result = new JSONObject();
        result.put("err", 1);
        HttpSession session = request.getSession(true);

        String ip    = AddressUtil.getIPAddress(request);
        int    count = bbsUserService.countByIp(ip);
        if (count >= bbsConfig.getRegisterSameIp()) {
            result.put("msg", "同一个IP同一天注册用户过多，禁止注册");
            return result;
        }

        String verCode = (String) session.getAttribute(CODE_NAME);
        if (count != 0 && !verCode.equalsIgnoreCase(code)) {
            result.put("msg", "验证码输入错误");
        } else if (bbsUserService.hasUser(user.getUserName())) {
            result.put("msg", "用户已经存在");
        } else {

            String password = HashKit.md5(user.getPassword());
            user.setPassword(password);
            user.setBalance(10);
            user.setLevel(1);
            user.setScore(10);
            user.setIp(ip);
            user.setRegisterTime(new Date());
            user = bbsUserService.setUserAccount(user);
            webUtils.loginUser(user, true);
            result.put("err", 0);
            result.put("msg", "index");
        }
        return result;
    }

    @RequestMapping("/authImage")
    public void authImage() throws IOException {
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        //生成随机字串
        String verifyCode = VerifyCodeUtils.generateVerifyCode(4);
        //存入会话session
        HttpSession session = request.getSession(true);
        //删除以前的
        session.removeAttribute(CODE_NAME);
        session.setAttribute(CODE_NAME, verifyCode.toLowerCase());
        //生成图片
        int w = 100, h = 30;
        VerifyCodeUtils.outputImage(w, h, response.getOutputStream(), verifyCode);

    }

    @RequestMapping("/postAuthImage")
    public void pousAuthImage() throws IOException {
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        //生成随机字串
        String verifyCode = VerifyCodeUtils.generateVerifyCode(4);
        //存入会话session
        HttpSession session = request.getSession(true);
        //删除以前的
        session.removeAttribute(POST_CODE_NAME);
        session.setAttribute(POST_CODE_NAME, verifyCode.toLowerCase());
        //生成图片
        int w = 100, h = 30;
        VerifyCodeUtils.outputImage(w, h, response.getOutputStream(), verifyCode);

    }


}
