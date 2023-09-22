package com.qinyun.bbs.util;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.beetl.core.Context;
import org.beetl.core.Function;
/**
 * 获取翻页的url尾部的参数，比如:
 * url/1.html?keyword="123"
 * ?keyword="123"
 * 
 */
public class PageUrlParameterFunction implements Function {

	@Override
	public String call(Object[] paras, Context ctx) {
			HttpServletRequest  req = (HttpServletRequest)ctx.getGlobal("request");
			String qs = req.getQueryString();
			String para = "";
			if(!StringUtils.isEmpty(qs)){
				para = "?"+qs;
			}
			return para;
	}

}
