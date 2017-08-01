package com.csjbot.snowbot.activity.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot_rogue.Events.ExpressionEvent;
import com.csjbot.snowbot_rogue.platform.SnowBotManager;
import com.csjbot.snowbot_rogue.utils.Constant;


public class SettingsTestActivity extends CsjUIActivity {
    private SnowBotManager snowBotManager = SnowBotManager.getInstance();
    private TextView textView;
    private ScrollView scrollView;

    private BroadcastReceiver SerialSendOkRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            textView.append("发送成功\n");
            scrollView.fullScroll(View.FOCUS_DOWN);
        }
    };

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_settings_hw_test);
//
//        textView = (TextView) findViewById(R.id.TextView);
//        scrollView = (ScrollView) findViewById(R.id.ScrollView);
//
//        lbm = LocalBroadcastManager.getInstance(this);
//        lbm.registerReceiver(SerialSendOkRec, new IntentFilter(Constant.ACTION_SERIAL_SEND_OK));
//        localBroadcastManager = LocalBroadcastManager.getInstance(this);
//    }

    private Handler mHandler = new Handler();
    private int faceType = 0;
    Intent intent = new Intent(Constant.Expression.ACTION_EXPRESSION_FACE);


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void changeExpression(View view) {
//        mHandler.post(changeFace);
        faceType += 1;
        if (faceType > 9) {
            faceType = 0;
        }
//        intent.putExtra(Constant.Expression.EXTRA_EXPRESSION_FACE, faceType);
//        localBroadcastManager.sendBroadcast(intent);
        postEvent(new ExpressionEvent(faceType));
    }

    public void changeFaceColor(View view) {

    }

    public void pageFinished(View view) {
        finish();
    }

    public void sendSensorAll(View view) {
    }

    public void turnOffAirCon(View view) {
    }

    public void turnOnAirCon(View view) {
//        localBroadcastManager.sendBroadcast(new Intent(Constant.ACTION_TOUCH_GET));
    }

    public void swingDoubleArm(View view) {
        snowBotManager.swingDoubleArm((byte) 0x04);
    }

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        textView = (TextView) findViewById(R.id.TextView);
        scrollView = (ScrollView) findViewById(R.id.ScrollView);
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_settings_hw_test;
    }
}
