package com.qinyun.bbs.common;

import com.qinyun.bbs.dao.BbsUserDao;
import com.qinyun.bbs.model.BbsUser;
import com.qinyun.bbs.util.HashKit;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Web相关工具类,同时用spring session，因此用户信息也放到session好cookie里
 * 
 */
@Component
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public  class WebUtils {

	BbsUserDao userDao;

	HttpServletRequest request;
	HttpServletResponse response;


	/**
	 * 密码:md5hex
	 */
	public static String pwdEncode(String password) {
		return HashKit.md5(password);
	}

	
	/**
	 * 返回当前用户
	 * @return GitUserModel
	 */
	public BbsUser currentUser() {
		Object loginUser = request.getSession().getAttribute("user");
		if(loginUser!=null){
			return (BbsUser) loginUser;
		}
		String cookieKey = Const.USER_COOKIE_KEY;
		// 获取cookie信息
		String userCookie = getCookie(request, cookieKey);
		// 1.cookie为空，直接清除
		if (StringUtils.isEmpty(userCookie)) {
			removeCookie(cookieKey);
			return null;
		}
		// 2.解密cookie
		String cookieInfo = null;
		// cookie 私钥
		String secret = Const.USER_COOKIE_SECRET;
		try {
			cookieInfo = new AESUtils(secret).decryptString(userCookie);
		} catch (RuntimeException e) {
			// ignore
		}
		// 3.异常或解密问题，直接清除cookie信息
		if (StringUtils.isEmpty(cookieInfo)) {
			removeCookie(cookieKey);
			return null;
		}
		String[] userInfo = cookieInfo.split("~");
		// 4.规则不匹配
		if (userInfo.length < 4) {
			removeCookie(cookieKey);
			return null;
		}
		String userId   = userInfo[0];
		String oldTime  = userInfo[1];
		String maxAge   = userInfo[2];
		String password    = userInfo[3];
		// 5.判定时间区间，超时的cookie清理掉
		if (!"-1".equals(maxAge)) {
			long now  = System.currentTimeMillis();
			long time = Long.parseLong(oldTime) + (Long.parseLong(maxAge) * 1000);
			if (time <= now) {
				removeCookie(cookieKey);
				return null;
			}
		}
		if(userId == null || "null".equals(userId)){
			removeCookie(cookieKey);
			return null;
		}
		if(password == null || "".equals(password.trim())){
			removeCookie(cookieKey);
			return null;
		}
		BbsUser user =  userDao.unique(Integer.valueOf(userId));
		
		if(!HashKit.md5(user.getPassword()).equals(password)) {
			removeCookie(cookieKey);
			return null;
		}
		
		request.getSession().setAttribute("user", user);
		return user;
	}

	/**
	 * 用户登陆状态维持
	 * 
	 * cookie设计为: des(私钥).encode(userId~time~maxAge~password~ip)
	 * 
	 * @param remember   是否记住密码、此参数控制cookie的 maxAge，默认为-1（只在当前会话）<br>
	 *                   记住密码默认为30天
	 * @return void
	 */
	public void loginUser(BbsUser user, boolean... remember) {
		
		request.setAttribute("user", user);
		// 获取用户的id、nickName
		String uid     = user.getId()+"";
		String password     = user.getPassword();
		// 当前毫秒数
		long   now      = System.currentTimeMillis();
		// 超时时间
		int    maxAge   = -1;
		if (remember.length > 0 && remember[0]) {
			maxAge      = 60 * 60 * 24 * 30; // 30天
		}
		// 用户id地址
		String ip		= getIP();
		// 构造cookie

		// cookie 私钥
		String secret = Const.USER_COOKIE_SECRET;
		// 加密cookie
		String cookieBuilder = uid + "~" +
				now + "~" +
				maxAge + "~" +
				HashKit.md5(password) + "~" +
				ip;
		String userCookie = new AESUtils(secret).encryptString(cookieBuilder);
		String cookieKey  = Const.USER_COOKIE_KEY;
		// 设置用户的cookie、 -1 维持成session的状态
		setCookie(cookieKey, userCookie, maxAge);
	}

	/**
	 * 退出即删除用户信息
	 */
	public void logoutUser() {
		request.getSession().removeAttribute("user");
		removeCookie(Const.USER_COOKIE_KEY);
		
	}

	/**
	 * 读取cookie
	 */
	public static String getCookie(HttpServletRequest request, String key) {
		Cookie[] cookies = request.getCookies();
		if(null != cookies){
			for (Cookie cookie : cookies) {
				if (key.equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * 清除 某个指定的cookie 
	 */
	public void removeCookie(String key) {
		setCookie(key, null, 0);
	}

	/**
	 * 设置cookie
	 */
	public void setCookie(String name, String value, int maxAgeInSeconds) {
		Cookie cookie = new Cookie(name, value);
		cookie.setPath("/");
		cookie.setMaxAge(maxAgeInSeconds);
		// 指定为httpOnly保证安全性
		cookie.setHttpOnly(true);
		response.addCookie(cookie);
	}



	/**
	 * 获取浏览器信息
	 */
	public String getUserAgent() {
		return request.getHeader("User-Agent");
	}

	/**
	 * 获取ip
	 */
	public String getIP() {
		String ip = request.getHeader("X-Requested-For");
		if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("X-Forwarded-For");
		}
		if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
	
	public  boolean isAdmin() {
		BbsUser user = this.currentUser();
		if(user==null){
			throw new RuntimeException("未登陆用户");
		}
		return user.getUserName().equals("admin");
			
	}

}
