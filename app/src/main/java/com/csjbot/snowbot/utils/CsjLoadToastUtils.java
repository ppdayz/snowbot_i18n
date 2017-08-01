package com.csjbot.snowbot.utils;

import android.content.Context;
import android.os.Handler;

import net.steamcrafted.loadtoast.LoadToast;

/**
 * Created by Administrator on 2016/9/5 0005.
 */
public class CsjLoadToastUtils {
    private static Handler mHandler = new Handler();
    private static boolean mSuccess = true;
    private static LoadToast mLoadTost;

    private static Runnable run = new Runnable() {
        @Override
        public void run() {
            if (mSuccess) {
                mLoadTost.success();
            } else {
                mLoadTost.error();
            }
        }
    };

    public static void showLoadToast(Context ctx, String text, int dismissTime, boolean success) {
        if (mLoadTost == null) {
            mLoadTost = new LoadToast(ctx);
        }
        mLoadTost.setText(text);
        mLoadTost.show();
        mSuccess = success;

        mHandler.postDelayed(run, dismissTime);
    }


}
