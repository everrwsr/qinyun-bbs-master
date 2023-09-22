package com.qinyun.bbs.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    static String FORMAT = "yyyy-MM-dd";

    //2016-04-21T16:29:40+0800
    static String SONAR_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    public static Date parse(String day) {
        if (day == null || day.length() == 0) return null;
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT);
        try {
            return sdf.parse(day);
        } catch (ParseException e) {
            return null;
        }


    }

    public static Date parseSonarDate(String day) {
        SimpleDateFormat sdf = new SimpleDateFormat(SONAR_FORMAT);
        try {
            return sdf.parse(day);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static String format(Date date) {

        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT);
        return sdf.format(date);
    }

    public static Date getDate(Date date, int min) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, -min);
        return cal.getTime();
    }


    public static Date[] getLastMonth() {
        return null;
    }

    public static void main(String[] args) {

        System.out.println(getDate(new Date(), 12));

    }
}
