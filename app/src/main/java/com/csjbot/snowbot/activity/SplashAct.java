package com.csjbot.snowbot.activity;

import android.os.Bundle;
import android.os.Handler;

import com.android.core.util.SharedUtil;
import com.android.core.util.StrUtil;
import com.csjbot.snowbot.BuildConfig;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.utils.IntentUtil;
import com.csjbot.snowbot.utils.RobotStatus;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot.utils.SharedPreferencesSDUtil;
import com.csjbot.snowbot.utils.UrlUtil;

/**
 * @Author: jl
 * @Date: 2016/12/16
 * @Desc:欢迎页面，程序的第一个页面
 */

public class SplashAct extends CsjUIActivity {
    private int ROBOTSTATUS = 0;
    /**
     * 是否开发模式
     */
    private static final boolean isDebug = BuildConfig.DEBUG;


    /**
     * 跳到主页面
     */
    public void initMainAct() {
        new Handler().postDelayed(() -> {
            IntentUtil.startActivity(SplashAct.this, LauncherActivity.class);
            SplashAct.this.finish();
        }, 1000);

    }


    /**
     * 跳转到引导页面
     */

    private void initGUideAct() {
        new Handler().postDelayed(() -> {
            IntentUtil.startActivity(SplashAct.this, GuideAct.class);
            SplashAct.this.finish();
        }, 1000);
    }


    @Override
    public void afterViewCreated(Bundle savedInstanceState) {

    }


    @Override
    protected void onResume() {
        super.onResume();
        urlInit();
        initMainAct();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    private void init() {
        ROBOTSTATUS = SharedUtil.getPreferInt(SharedKey.ROBOTREGISTERSTATUS, 0);
        switch (ROBOTSTATUS) {
            case RobotStatus.NONOUTWARE:
            case RobotStatus.ALREADYOUTWARE:
            case RobotStatus.NONREGISTER:
                initGUideAct();
                break;
            case RobotStatus.ALREADLYREGISTER:
                initMainAct();
                break;
            default:
                initMainAct();
                break;
        }
    }

    /**
     * 将阿里云、后台接口保存本地
     */
    private void urlInit() {
        if (StrUtil.isBlank((String) SharedPreferencesSDUtil.get(this, "urlUtil", SharedKey.OUTWAREHOUSE, ""))) {
            SharedPreferencesSDUtil.put(this, "urlUtil", SharedKey.OUTWAREHOUSE, UrlUtil.OUTWAREHOUSE);
        }
        if (StrUtil.isBlank((String) SharedPreferencesSDUtil.get(this, "urlUtil", SharedKey.MASTERREGISTER, ""))) {
            SharedPreferencesSDUtil.put(this, "urlUtil", SharedKey.MASTERREGISTER, UrlUtil.MASTERREGISTER);
        }
        if (StrUtil.isBlank((String) SharedPreferencesSDUtil.get(this, "urlUtil", SharedKey.UPDATEROBOT, ""))) {
            SharedPreferencesSDUtil.put(this, "urlUtil", SharedKey.UPDATEROBOT, UrlUtil.UPDATEROBOT);
        }
        if (StrUtil.isBlank((String) SharedPreferencesSDUtil.get(this, "urlUtil", SharedKey.GETAUTHCODE, ""))) {
            SharedPreferencesSDUtil.put(this, "urlUtil", SharedKey.GETAUTHCODE, UrlUtil.GETAUTHCODE);
        }
        if (StrUtil.isBlank((String) SharedPreferencesSDUtil.get(this, "urlUtil", SharedKey.REGISTERFACE, ""))) {
            SharedPreferencesSDUtil.put(this, "urlUtil", SharedKey.REGISTERFACE, UrlUtil.REGISTERFACE);
        }
        if (StrUtil.isBlank((String) SharedPreferencesSDUtil.get(this, "urlUtil", SharedKey.DETECTFACE, ""))) {
            SharedPreferencesSDUtil.put(this, "urlUtil", SharedKey.DETECTFACE, UrlUtil.DETECTFACE);
        }
        if (StrUtil.isBlank((String) SharedPreferencesSDUtil.get(this, "urlUtil", SharedKey.FACEIDENTIFY, ""))) {
            SharedPreferencesSDUtil.put(this, "urlUtil", SharedKey.FACEIDENTIFY, UrlUtil.FACEIDENTIFY);
        }
        if (StrUtil.isBlank((String) SharedPreferencesSDUtil.get(this, "urlUtil", SharedKey.REGISTERALIYUN, ""))) {
            SharedPreferencesSDUtil.put(this, "urlUtil", SharedKey.REGISTERALIYUN, UrlUtil.REGISTERALIYUN);
        }
        if (StrUtil.isBlank((String) SharedPreferencesSDUtil.get(this, "urlUtil", SharedKey.GETADMIN, ""))) {
            SharedPreferencesSDUtil.put(this, "urlUtil", SharedKey.GETADMIN, UrlUtil.GETADMIN);
        }
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.act_splash_act;
    }
}
