package com.csjbot.snowbot.utils;

import java.sql.Timestamp;
import java.util.Calendar;

public class TimestampUtil {
    public final static int YEAR = 1;
    public final static int MONTH = 2;
    public final static int DAY = 3;
    public final static int HOUR = 4;
    public final static int MINUTE = 5;
    public final static int SECOND = 6;
    public final static int WEEK = 7;
    public final static int MILLISECOND = 8;
    public final static int CENTURY = 9;
    public final static int SUNDAY = Calendar.SUNDAY;
    public final static int MONDAY = Calendar.MONDAY;
    public final static int WEDNESDAY = Calendar.WEDNESDAY;
    public final static int TUESDAY = Calendar.TUESDAY;
    public final static int THURSDAY = Calendar.THURSDAY;
    public final static int FRIDAY = Calendar.FRIDAY;
    public final static int SATURDAY = Calendar.SATURDAY;

    //获取若干偏移量的时间戳(Timestamp),参数一为操作时间戳(Timestamp),参数二为偏移单位,参数三为偏移值
    public static Timestamp getTiemstamp(Timestamp operTime, int unit, int value) {
        if (operTime == null) return null;
        Calendar c = Calendar.getInstance();
        c.setTime(operTime);
        Timestamp newTime = null;
        switch (unit) {
            case YEAR:
                c.set(Calendar.YEAR, c.get(Calendar.YEAR) + value);
                newTime = new Timestamp(c.getTimeInMillis());
                break;
            case MONTH:
                c.set(Calendar.MONTH, c.get(Calendar.MONTH) + value);
                newTime = new Timestamp(c.getTimeInMillis());
                break;
            case DAY:
                c.set(Calendar.DATE, c.get(Calendar.DATE) + value);
                newTime = new Timestamp(c.getTimeInMillis());
                break;
            case HOUR:
                c.set(Calendar.HOUR, c.get(Calendar.HOUR) + value);
                newTime = new Timestamp(c.getTimeInMillis());
                break;
            case MINUTE:
                c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + value);
                newTime = new Timestamp(c.getTimeInMillis());
                break;
            case SECOND:
                c.set(Calendar.SECOND, c.get(Calendar.SECOND) + value);
                newTime = new Timestamp(c.getTimeInMillis());
                break;
            case WEEK:
                c.set(Calendar.DATE, c.get(Calendar.DATE) + 7 * value);
                newTime = new Timestamp(c.getTimeInMillis());
                break;
            case MILLISECOND:
                c.set(Calendar.MILLISECOND, c.get(Calendar.MILLISECOND) + value);
                newTime = new Timestamp(c.getTimeInMillis());
                break;
            case CENTURY:
                c.set(Calendar.YEAR, c.get(Calendar.YEAR) + 100 * value);
                newTime = new Timestamp(c.getTimeInMillis());
                break;
            default:
                break;
        }
        return newTime;
    }


    //获取若干偏移量的时间戳(Timestamp),参数一为操作时间戳(Timestamp),参数二为偏移单位,参数三为偏移值,参数四标记是否获取该天开始那一刻,参数五标记是否获取该天结束那一刻(参数四优先参数五)
    public static Timestamp getTiemstamp(Timestamp operTime, int unit, int value,
                                         boolean fromStart, boolean toEnd) {
        if (operTime == null) return null;
        Calendar c = Calendar.getInstance();
        c.setTime(getTiemstamp(operTime, unit, value));
        if (fromStart) {
            c.set(Calendar.HOUR, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
        } else {
            if (toEnd) {
                c.set(Calendar.HOUR, 23);
                c.set(Calendar.MINUTE, 59);
                c.set(Calendar.SECOND, 59);
            }
        }
        return new Timestamp(c.getTimeInMillis());
    }

    //获取精度到指定单位的时间戳字符串
    public static String getTimestampString(Timestamp operTime, int unit) {
        if (operTime != null) {
            String str = "N/A";
            switch (unit) {
                case YEAR:
                    str = operTime.toString().substring(0, 4);
                case MONTH:
                    str = operTime.toString().substring(0, 7);
                    break;
                case DAY:
                    str = operTime.toString().substring(0, 10);
                    break;
                case HOUR:
                    str = operTime.toString().substring(0, 13);
                    break;
                case MINUTE:
                    str = operTime.toString().substring(0, 16);
                    break;
                case SECOND:
                    str = operTime.toString().substring(0, 19);
                    break;
                case MILLISECOND:
                    str = operTime.toString();
                    break;
                default:
                    break;
            }
            return str;
        }
        return "N/A";
    }

    //判断两个时间戳(Timestamp)是否在同一天
    public static boolean isTheSameDate(Timestamp time1, Timestamp time2) {
        if (time1 != null && time2 != null) {
            Calendar c1 = Calendar.getInstance();
            c1.setTime(time1);
            int y1 = c1.get(Calendar.YEAR);
            int m1 = c1.get(Calendar.MONTH);
            int d1 = c1.get(Calendar.DATE);
            Calendar c2 = Calendar.getInstance();
            c2.setTime(time2);
            int y2 = c2.get(Calendar.YEAR);
            int m2 = c2.get(Calendar.MONTH);
            int d2 = c2.get(Calendar.DATE);
            if (y1 == y2 && m1 == m2 && d1 == d2) {
                return true;
            }
        } else {
            if (time1 == null && time2 == null) {
                return true;
            }
        }
        return false;
    }

    //获取操作时间戳(Timestamp)所在周的一天，其顺序为：星期天,一,二,三,四,五,六
    public static Timestamp getDayOfWeek(Timestamp operTime, int day) {
        if (operTime != null && day > 0 && day <= 7) {
            Calendar c = Calendar.getInstance();
            c.setTime(operTime);
            c.set(Calendar.DAY_OF_WEEK, day);
            return new Timestamp(c.getTimeInMillis());
        }
        return null;

    }
}