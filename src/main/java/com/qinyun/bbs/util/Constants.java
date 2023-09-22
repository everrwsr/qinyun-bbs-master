package com.qinyun.bbs.util;

import java.util.HashSet;
import java.util.Set;

public class Constants {
    static Set<String> ui = new HashSet<String>();

    static {
        ui.add("jpg");
        ui.add("gif");
        ui.add("png");
    }

    static Set<String> client = new HashSet<String>();

    static {
        client.add("js");
        client.add("html");
        client.add("css");
        client.add("htm");
    }


    static Set<String> server = new HashSet<String>();

    static {
        server.add("java");
        server.add("xml");
        server.add("properties");
        server.add("jsp");
        server.add("sql");
    }

    public static final int UI     = 1;
    public static final int CLIENT = 2;
    public static final int SEVER  = 3;
    public static final int DOC    = 4;

    public static int getFileType(String file) {

        int index = file.lastIndexOf('.');
        if (index == -1) return DOC;
        String fileType = file.substring(index + 1);
        if (ui.contains(fileType)) {
            return UI;
        } else if (client.contains(fileType)) {
            return CLIENT;
        } else if (server.contains(fileType)) {
            return SEVER;
        } else {
            return DOC;
        }

    }

    public static String getCmdTypeName(String type) {
        if (type.equals("push")) {
            return "推送代码";
        } else if (type.equals("thanks")) {
            return "感谢";
        } else if (type.equals("reviewedBy")) {
            return "代码审查";
        } else {
            return type;
        }
    }
}
