package com.xh.hotme.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExpUtil {

    public static boolean isMatchAccount(String data){
        Pattern pattern = Pattern.compile("[1234567890A-Za-z]{6,16}");
        return pattern.matcher(data).matches();
    }
    public static boolean isMatchPassword(String data){
        Pattern pattern = Pattern.compile("[1234567890A-Za-z]{6,16}");
        return pattern.matcher(data).matches();
    }
    /**
     * 判断是否是手机号码
     *
     * @param mobiles
     * @return
     */
    public static boolean isMobileNumber(String mobiles) {
        Pattern p = Pattern.compile("^((1[0-9][0-9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    /**
     *
     * @param pathName 8位数字 20130623
     * @return
     */
    //6位数字
    public static boolean isDateDir(String pathName) {
        String format = "\\d{8}";
        String name = pathName.trim();
        boolean matches = Pattern.matches(format, name);  // 通过为true 验证不通过为false

        return matches;
    }
}
