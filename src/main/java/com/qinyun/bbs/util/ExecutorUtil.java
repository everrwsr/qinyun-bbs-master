package com.qinyun.bbs.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorUtil {

	/**单线程*/
	public static final ExecutorService LUCENE_EXECUTOR_POOL = new ThreadPoolExecutor(
			  1,1,30, TimeUnit.SECONDS, 
	          new ArrayBlockingQueue<>(1000000),
	          new ThreadPoolExecutor.AbortPolicy());
	  
	  
}
