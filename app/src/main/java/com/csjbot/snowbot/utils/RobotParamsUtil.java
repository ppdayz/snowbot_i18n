package com.csjbot.snowbot.utils;

import com.android.core.util.SharedUtil;

/**
 * Created by 孙秀艳 on 2017/5/17.
 * 机器人基本参数
 */

public final class RobotParamsUtil {

    //导航
    public static void setNavigation(String slamSN) {
        SharedUtil.setPreferStr(SharedKey.NAVIGATION, slamSN);
    }

    public static String getNavigation() {
        return SharedUtil.getPreferStr(SharedKey.NAVIGATION);
    }

    //上位机
    public static void setUpComputorID(String upComputorID) {
        SharedUtil.setPreferStr(SharedKey.UPCOMPUTER, upComputorID);
    }

    public static String getUpComputorID() {
        return SharedUtil.getPreferStr(SharedKey.UPCOMPUTER);
    }

    //上身板
    public static void setUpPlateID(String upPlateID) {
        SharedUtil.setPreferStr(SharedKey.UPPLATE, upPlateID);
    }

    public static String getUpPlateID() {
        return SharedUtil.getPreferStr(SharedKey.UPPLATE);
    }

    //下身板
    public static void setDownPlateID(String downPlateID) {
        SharedUtil.setPreferStr(SharedKey.DOWNPLATE, downPlateID);
    }

    public static String getDownPlateID() {
        return SharedUtil.getPreferStr(SharedKey.DOWNPLATE);
    }

    //机器人唯一识别码
    public static void setSn(String sn) {
        SharedUtil.setPreferStr(SharedKey.SN, sn);
    }

    public static String getSn() {
        return SharedUtil.getPreferStr(SharedKey.SN);
    }
}
