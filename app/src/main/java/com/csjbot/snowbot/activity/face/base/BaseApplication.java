package com.csjbot.snowbot.activity.face.base;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.android.core.entry.Static;

import mobile.ReadFace.YMUtil;

/**
 * Created by mac on 16/8/15.
 */
public class BaseApplication extends Application {

    private static BaseApplication instence;


    //绘制左右翻转
    public static final boolean yu = true;

    public static boolean reverse_180 = false;
    public static boolean reverse_180_front = false;

    //是否显示logo
    public static boolean useLogo = true;
    public static int screenOri = Configuration.ORIENTATION_LANDSCAPE;
//    public static int  screenOri = Configuration.ORIENTATION_PORTRAIT;

    @Override
    public void onCreate() {
        super.onCreate();
        instence = this;
//        DLog.mSwitch = false;
//        DLog.mWrite = true;
        YMUtil.setDebug(true);

    }

    public static Context getAppContext() {
        return Static.CONTEXT;
    }

}
