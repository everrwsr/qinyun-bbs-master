package com.qinyun.bbs.util;

import org.beetl.core.Context;
import org.beetl.core.Function;

public class LevelInfoFunction implements Function {

	@Override
	public String call(Object[] paras, Context ctx) {
		Integer level = (Integer) paras[0];
		switch(level){
		case 1: return "新手爱好者";
		case 2: return "初级爱好者";
		case 3: return "中级爱好者";
		case 4: return "高级爱好者";
		default: return "骨灰级爱好者";
		
		}
		
	}

}
