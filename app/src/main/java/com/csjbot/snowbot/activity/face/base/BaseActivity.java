package com.csjbot.snowbot.activity.face.base;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;

import dou.utils.DLog;
import dou.utils.DeviceUtil;
import dou.utils.DisplayUtil;

/**
 * Created by mac on 2017/2/7 下午3:59.
 */

public abstract class BaseActivity extends Activity {

    protected int sw;
    protected int sh;
    protected Context mContext;
    protected int orientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        sw = DisplayUtil.getScreenWidthPixels(this);
        sh = DisplayUtil.getScreenHeightPixels(this);
//        sh = 800;

        int rotation = getWindowManager().getDefaultDisplay().getRotation();

        DLog.d("onCreate : sw = " + sw + "  sh = " + sh + "  " + DeviceUtil.getModel());


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        sw = DisplayUtil.getScreenWidthPixels(this);
        sh = DisplayUtil.getScreenHeightPixels(this);
        DLog.d("onConfigurationChanged : sw = " + sw + "  sh = " + sh +
                " newConfig = " + newConfig.orientation + ":" + getResources().getConfiguration().orientation);
        onResume();
    }

    @Override
    protected void onResume() {
        super.onResume();
        orientation = BaseApplication.screenOri;


//
//        if ((this instanceof FaceRecoActivity || this instanceof PointsActivity) &&
//                getResources().getConfiguration().orientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
////            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        } else if (orientation == Configuration.ORIENTATION_PORTRAIT &&
//                getResources().getConfiguration().orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
//            DLog.d("set portrait now = "+getResources().getConfiguration().orientation);
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE &&
//                getResources().getConfiguration().orientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
//            DLog.d("set landscape now = "+getResources().getConfiguration().orientation);
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        }
        sw = DisplayUtil.getScreenWidthPixels(this);
        sh = DisplayUtil.getScreenHeightPixels(this);
//        sh = 800;
        DLog.d("onResume : sw = " + sw + "  sh = " + sh);

        initView();
    }

    public int getDoom(int tar) {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return tar * sw / 1080;
        }
        return tar * sh / 1080;
    }

    public abstract void initView();
}


