package com.qinyun.bbs.util;

public class EsUtil {

	public static String getEsKey(Integer topicId,Integer postId,Integer replyId) {
		StringBuilder key = new StringBuilder();
		key.append(topicId != null?topicId.toString():"").append(":");
		key.append(postId != null?postId.toString():"").append(":");
		key.append(replyId != null?replyId.toString():"").append(":");
		
		return HashKit.md5(key.toString());
	}
}
