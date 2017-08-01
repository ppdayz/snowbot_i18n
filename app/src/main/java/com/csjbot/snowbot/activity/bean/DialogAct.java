package com.csjbot.snowbot.activity.bean;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot.utils.PowerStatus;
import com.csjbot.snowbot.utils.SpeechStatus;

public class DialogAct extends Activity {
//    private TimeUtil timeUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //去除这个Activity的标题栏
        setContentView(R.layout.activity_dialog);
//        timeUtil = TimeUtil.getInterface();
//        timeUtil.getTime(10, new TimeUtil.TimeListener() {
//            @Override
//            public void getTime(int i) {
//                if (i == 0) {
//                    PowerStatus.getIstance().setPowerLowWarn(true);
//                    finish();
//                }
//            }
//        });
    }

    public void cancelRemind(View view) {
        PowerStatus.getIstance().setPowerLowWarn(false);
//        timeUtil.stop();
        finish();
    }

    public void ensureRemind(View view) {
        PowerStatus.getIstance().setPowerLowWarn(true);
//        timeUtil.stop();
        finish();
    }

    @Override
    protected void onDestroy() {
        SpeechStatus.getIstance().setAiuiResponse(true);
        super.onDestroy();
    }
}
