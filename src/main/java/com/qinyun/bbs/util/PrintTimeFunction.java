package com.qinyun.bbs.util;


import org.beetl.core.Context;
import org.beetl.core.Function;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Author: StevenChow
 * Date: 13-4-30
 */
public class PrintTimeFunction implements Function {
    private final static ThreadLocal<DateFormat> TIME_STAMP_FORMAT = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    private final static ThreadLocal<DateFormat> MY_DATE_FORMAT    = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy年MM月dd日"));

    @Override
    public String call(Object[] params, Context context) {
        if (params.length != 1) {
            throw new RuntimeException("length of params must be 1 !");
        }
        if (params[0].toString().length() == 0) {
            return null;
        }

        return getNiceDate((Date) params[0]);
    }

    public static String getNiceDate(Date date) {
        if (null == date) return "";
        String result      = null;
        long   currentTime = new Date().getTime() - date.getTime();
        int    time        = (int) (currentTime / 1000);
        if (time < 60) {
            result = "刚刚";
        } else if (time < 3600) {
            result = time / 60 + "分钟前";
        } else if (time < 86400) {
            result = time / 3600 + "小时前";
        } else if (time < 864000) {
            result = time / 86400 + "天前";
        } else {
            result = MY_DATE_FORMAT.get().format(date);
        }
        return result;
    }
}
