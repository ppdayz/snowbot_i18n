/*
 * Copyright (c) 2012, Bravowhale Corporation, All Rights Reserved
 */
package com.csjbot.snowbot.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;


public final class IntentUtil {
    /**
     * @param context
     * @param clazz
     * @description 启动跳转的Act
     * @author Andy.fang
     * @createDate 2016-10-11
     */
    public static void startActivity(Context context, Class<?> clazz) {
        Intent mIntent = new Intent(context, clazz);
        context.startActivity(mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    /**
     * @param context
     * @param clazz
     * @description 启动跳转的Act
     * @author Andy.fang
     * @createDate 2016-10-11
     */
    public static void startActivityForResult(Activity context, Class<?> clazz, int request) {
        Intent mIntent = new Intent(context, clazz);
        context.startActivityForResult(mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), request);
    }

    /**
     * @param context
     * @description 启动跳转的Act
     * @author Andy.fang
     * @createDate 2016-10-11
     */
    public static void startActivityForResult(Activity context, Intent intent, int request) {
        context.startActivityForResult(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), request);
    }

    /**
     * @param context
     * @description 启动跳转的Act
     * @author Andy.fang
     * @createDate 2016-10-11
     */
    public static void startActivityForResult(Fragment context, Intent intent, int request) {
        context.startActivityForResult(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), request);
    }

    /**
     * @param context
     * @param mIntent
     * @description 启动跳转的Act
     * @author Andy.fang
     * @createDate 2015-10-12
     */
    public static void startActivity(Context context, Intent mIntent) {
        context.startActivity(mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    /**
     * @param context
     * @param clazz
     * @param mBundle
     * @description
     * @author Andy.fang
     * @createDate 2015-4-26
     */
    public static void startActivity(Context context, Class<?> clazz, Bundle mBundle) {
        if (context != null) {
            Intent mIntent = new Intent(context, clazz);
            mIntent.putExtras(mBundle);
            context.startActivity(mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
    }


}
