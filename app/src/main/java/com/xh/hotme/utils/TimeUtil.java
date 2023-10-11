package com.xh.hotme.utils;

import android.text.TextUtils;

import androidx.annotation.Keep;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Create by zhaozhihui on 2018/11/28
 **/

@Keep
public class TimeUtil {
    public static final int SECONDS_IN_DAY = 60 * 60 * 24;
    public static final long MILLIS_IN_DAY = 1000L * SECONDS_IN_DAY;

    private static final long ONE_MINUTE = 60;
    private static final long ONE_HOUR = 3600;
    private static final long ONE_DAY = 86400;
    private static final long ONE_MONTH = 2592000;
    private static final long ONE_YEAR = 31104000;

    public static boolean isSameDayOfMillis(final long ms1, final long ms2) {
        final long interval = ms1 - ms2;
        return interval < MILLIS_IN_DAY
                && interval > -1L * MILLIS_IN_DAY
                && toDay(ms1) == toDay(ms2);
    }

    private static long toDay(long millis) {
        return (millis + TimeZone.getDefault().getOffset(millis)) / MILLIS_IN_DAY;
    }

    public static boolean isThirtyBetween(long oldTime, long currentTime) {
        long interval = currentTime - oldTime;
        return interval <= 60 * 30 * 1000;

    }

    public static boolean isSevenDayBetween(long oldTime, long currentTime) {
        long interval = currentTime - oldTime;
        return interval <= 7 * 24 * 60 * 60 * 1000;

    }

    public static long dateToStamp(String s) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = simpleDateFormat.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (date != null) {
            return date.getTime();
        }
        return 0;
    }

    public static long dateToStamp(String s, SimpleDateFormat format) {
        Date date = null;
        try {
            date = format.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (date != null) {
            return date.getTime();
        }
        return 0;
    }

    /*
     * 将时间戳转换为时间
     */
    public static String stampToDate(String s) {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = Long.valueOf(s);
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }

    /*
     * 将时间戳转换为时间
     * format : e.g.  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
     */
    public static String stampToDate(String s, SimpleDateFormat format) {
        String res;
        long lt = Long.valueOf(s);
        Date date = new Date(lt);
        res = format.format(date);
        return res;
    }


    /*
     * 将时间戳转换为时间
     * format : e.g.  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
     */
    public static String stampToDate(String s, SimpleDateFormat format, int days) {
        String res;
        long lt = Long.valueOf(s);
        Date date = new Date(lt);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);//1表示明天,-1表示昨天
        date = calendar.getTime();
        res = format.format(date);
        return res;
    }

    /**
     * 距离今天多少天
     *
     * @param date
     * @return
     */
    public static long fromToday(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        long time = date.getTime() / 1000;
        long now = new Date().getTime() / 1000;
        long ago = now - time;

        long day = ago / ONE_DAY;
        if (day < 0) {
            day = -day;
        }
        return day;

    }


    /**
     * 距离今天多少天
     *
     * @param timeStamp
     * @return
     */
    public static long fromToday(String timeStamp) {

        Date date;
        date = new Date(timeStamp);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        long time = date.getTime() / 1000;
        long now = new Date().getTime() / 1000;
        long ago = now - time;

        long day = ago / ONE_DAY;
        if (day < 0) {
            day = -day;
        }
        return day;

    }


    /**
     * 距离当前时间戳相差多少天
     *
     * @param timeStamp
     * @return
     */
    public static long fromTodays(long timeStamp) {

        Date date;
        date = new Date(timeStamp);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        long time = date.getTime() / 1000;
        long now = new Date().getTime() / 1000;
        long ago = now - time;

        long day = ago / ONE_DAY;
        if (day < 0) {
            day = -day;
        }
        return day;

    }


    public static String stringToTime_new(long seconds) {
        String time = "";
        if (seconds <= 0) {
//            return "0天00小时00分";
            return "1秒";
        }
        if (seconds < 60) {
            time = seconds + "秒";
            return time;
        }


        long d = seconds / (3600 * 24);
        if (d > 0)
            time = time + d + "天";

        long h = seconds / 3600 - d * 24;
        if (h > 0) {
            if (h < 10) {
                time = time + "0" + h + "小时";
            } else {
                time = time + h + "小时";
            }
        }


        long min = seconds / 60 - d * 24 * 60 - h * 60;
        if (min > 0) {
            if (min < 10) {
                time = time + "0" + min + "分";
            } else {
                time = time + min + "分";
            }
        }

        long millisecond = seconds / 60 - d * 24 * 60 - h * 60 - min * 60;
        if (millisecond > 0) {
            if (millisecond < 10) {
                time = time + "0" + millisecond + "秒";
            } else {
                time = time + millisecond + "秒";
            }
        }

        if (TextUtils.isEmpty(time))
            time = "0秒";
        return time;
    }


    /**
     * 距离当前时间戳相差多少时间
     *
     * @param timeStamp
     * @return
     */
    public static String fromToday(long timeStamp) {

        Date date;
        date = new Date(timeStamp);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        long time = date.getTime() / 1000;
        long now = new Date().getTime() / 1000;
        long ago = now - time;

        if (ago < 0) {
            ago = -ago;
        }

        if (ago <= ONE_MINUTE)
            return ago + "秒";
        else if (ago <= ONE_HOUR)
            return ago / ONE_MINUTE + "分钟";
        else if (ago <= ONE_DAY)
            return ago / ONE_HOUR + "小时";
        else {
            long day = ago / ONE_DAY;
            return day + "天";
        }
    }


    public static String getHour() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        String hour;
        hour = dateString.substring(11, 13);
        return hour;
    }

    public static boolean isSameDay(long currentTime, long lastTime) {
        try {
            Calendar nowCal = Calendar.getInstance();
            Calendar dataCal = Calendar.getInstance();
            SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
            SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
            Long nowLong = Long.valueOf(currentTime);
            Long dataLong = Long.valueOf(lastTime);
            String data1 = df1.format(nowLong);
            String data2 = df2.format(dataLong);
            Date now = df1.parse(data1);
            Date date = df2.parse(data2);
            nowCal.setTime(now);
            dataCal.setTime(date);
            return isSameDay(nowCal, dataCal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isSameDay(String currentTime, String lastTime) {
        try {
            Calendar nowCal = Calendar.getInstance();
            Calendar dataCal = Calendar.getInstance();
            SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
            SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
            Long nowLong = Long.valueOf(currentTime);
            Long dataLong = Long.valueOf(lastTime);
            String data1 = df1.format(nowLong);
            String data2 = df2.format(dataLong);
            Date now = df1.parse(data1);
            Date date = df2.parse(data2);
            nowCal.setTime(now);
            dataCal.setTime(date);
            return isSameDay(nowCal, dataCal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 != null && cal2 != null) {
            return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)
                    && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                    && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        } else {
            return false;
        }
    }


    public static int getStartDuration(int start_time) {
        int duration = 0;
        long end_time = System.currentTimeMillis();
        if (start_time != 0) {
            duration = Math.round((end_time - start_time) / 1000);
        }

        return duration;
    }

    public static long getStartDuration(long start_time) {
        int duration = 0;
        long end_time = System.currentTimeMillis();
        if (start_time != 0) {
            duration = Math.round((end_time - start_time) / 1000);
        }

        return duration;
    }

    public static long getStartDurationMs(String clientKey) {
        long duration = 0;
        long start_time = 0;
        long end_time = System.currentTimeMillis();
        try {
            start_time = Long.parseLong(clientKey);
        } catch (Exception e) {

        }
        if (start_time != 0) {
            duration = end_time - start_time;
        }

        return duration;
    }

    public static long getStartDuration(String clientKey) {
        int duration = 0;
        long start_time = 0;
        long end_time = System.currentTimeMillis();
        try {
            start_time = Long.parseLong(clientKey);
        } catch (Exception e) {

        }
        if (start_time != 0) {
            duration = Math.round((end_time - start_time) / 1000);
        }

        return duration;
    }

    /**
     * 判断当前时间是否在间隔天数以内
     *
     * @param timeStamp
     * @param day
     * @return
     */
    public static boolean isOnTime(long timeStamp, int day) {

        long curTimestamp = System.currentTimeMillis();
        return timeStamp + (long) day * 24 * 60 * 60 * 1000 > curTimestamp;
    }

    /***
     * 判断字符串是否是yyyyMMdd格式
     * @param mes 字符串
     * @return boolean 是否是日期格式
     */
    public static boolean isRqFormat(String mes) {
        String format = "([0-9]{4})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])";
        Pattern pattern = Pattern.compile(format);
        Matcher matcher = pattern.matcher(mes);
        if (matcher.matches()) {
            pattern = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");
            matcher = pattern.matcher(mes);
            if (matcher.matches()) {
                int y = Integer.valueOf(matcher.group(1));
                int m = Integer.valueOf(matcher.group(2));
                int d = Integer.valueOf(matcher.group(3));
                if (d > 28) {
                    Calendar c = Calendar.getInstance();
                    c.set(y, m - 1, 1);
                    //每个月的最大天数
                    int lastDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);
                    return (lastDay >= d);
                }
            }
            return true;
        }
        return false;

    }

    /*
     * 求两个日期（时间戳）相差多少天，
     */
    public static long diffDate(long startTimeStamp, long endTimeStamp) {

        Calendar nowCal = Calendar.getInstance();
        Calendar dataCal = Calendar.getInstance();
        SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
        String data1 = df1.format(startTimeStamp);
        String data2 = df2.format(endTimeStamp);
        try {
            Date startDate = df1.parse(data1);
            Date endDate = df2.parse(data2);
            nowCal.setTime(startDate);
            dataCal.setTime(startDate);
            long daysBetween = (endDate.getTime() - startDate.getTime() + 1000000) / (60 * 60 * 24 * 1000);
            return daysBetween;
        } catch (ParseException e) {

        }

        return 0;
    }

    public static String getCountDownTime(long seconds) {
        String countDownContent = "";
        int day = 0;
        int hour = 0;
        int minute = 0;
        long temp = seconds;
        if (seconds > 24 * 60 * 60) {
            day = (int) seconds / (24 * 60 * 60);
            countDownContent += day + ":";
            temp = seconds % (24 * 60 * 60);
        } else {
            temp = seconds;
        }
        long hours = temp % 3600;
        if (hours > 3600) {
            hour = (int) hours / 3600;
            if (hour < 10) {
                countDownContent += "0" + hour + ":";
            } else {
                countDownContent += hour + ":";
            }
        } else {
            if (day > 0) {
                countDownContent += "00:";
            }
        }
        long minutes = hours / 60;
        if (minutes > 0) {
            if (minutes < 10) {
                countDownContent += "0" + minutes + ":";
            } else {
                countDownContent += minutes + ":";
            }
        } else {
            countDownContent += "00:";
        }
        long second = hours % 60;
        if (second < 10) {
            countDownContent += "0" + second;
        } else {
            countDownContent += String.valueOf(second);
        }

        return countDownContent;
    }

    /**
     * 判断是否为今天(效率比较高)
     *
     * @param day 传入的 时间  "2016-06-28 10:10:30" "2016-06-28" 都可以
     * @return true今天 false不是
     * @throws ParseException
     */
    public static boolean isToday(String day) throws ParseException {

        Calendar pre = Calendar.getInstance();
        Date predate = new Date(System.currentTimeMillis());
        pre.setTime(predate);
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
        Date date = df1.parse(day);
        cal.setTime(date);
        if (cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR))) {
            int diffDay = cal.get(Calendar.DAY_OF_YEAR)
                    - pre.get(Calendar.DAY_OF_YEAR);

            return diffDay == 0;
        }
        return false;
    }

    public static String getCurrentTime(long timeStamp) {
        Date date = new Date(timeStamp);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = simpleDateFormat.format(date);
        return dateStr;
    }


    public static String intToTimeMin(int seconds) {
        if (seconds <= 0) {
            return "00:00";
        }
        String time = "";

        int min = seconds / 60;
        if (min < 10) {
            time = time + "0" + min + ":";
        } else {
            time = time + min + ":";
        }

        int sec = seconds - min * 60;
        if (sec < 10) {
            time = time + "0" + sec;
        } else {
            time = time + sec;
        }

        return time;
    }

    public static String intToTimeHour(int seconds) {
        if (seconds <= 0) {
            return "00:00:00";
        }
        String time = "";

        int hour = seconds / 3600;
        if (hour < 10) {
            time = time + "0" + hour + ":";
        } else {
            time = time + hour + ":";
        }
        seconds = seconds % 3600;
        int min = seconds / 60;
        if (min < 10) {
            time = time + "0" + min + ":";
        } else {
            time = time + min + ":";
        }

        int sec = seconds - min * 60;
        if (sec < 10) {
            time = time + "0" + sec;
        } else {
            time = time + sec;
        }

        return time;
    }

}
