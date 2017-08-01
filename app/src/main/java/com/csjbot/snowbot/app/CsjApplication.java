package com.csjbot.snowbot.app;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.android.core.entry.Static;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot.utils.SharedPreferencesSDUtil;
import com.csjbot.snowbot.utils.UUIDGenerator;
import com.iflytek.cloud.SpeechUtility;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by Administrator on 2016/8/31 0031.
 */
public class CsjApplication extends MultiDexApplication {
    private static CsjApplication mcontext;
    public static boolean USE_RK3288 = true;

    @Override
    public void onCreate() {
        super.onCreate();
        SpeechUtility.createUtility(this, "appid=" + getString(com.csjbot.snowbot_rogue.R.string.app_id));

        setDefaultUncaughtExceptionHandler();
        CrashReport.initCrashReport(getApplicationContext());
        Static.CONTEXT = this;
        mcontext = this;
//        FreelineCore.init(this);

//        SharedUtil.setPreferStr(SharedKey.DEVICEUUID, UUIDGenerator.getInstance().getDeviceUUID());
        SharedPreferencesSDUtil.put(this, this.getPackageName(), SharedKey.DEVICEUUID, UUIDGenerator.getInstance().getDeviceUUID());
    }

    private void setDefaultUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Csjlogger.error("UncaughtException", ex);
            }
        });
    }

    public static Context getAppContext() {
        return mcontext;
    }
}
