package com.csjbot.snowbot.activity.settings;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.core.util.SharedUtil;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot_rogue.app.CsjSpeechSynthesizer;
import com.csjbot.snowbot.bean.aiui.entity.CsjSynthesizerListener;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot_rogue.Events.ExpressionEvent;
import com.csjbot.snowbot_rogue.Events.HWTestEvent;
import com.csjbot.snowbot_rogue.platform.SnowBotManager;
import com.csjbot.snowbot_rogue.servers.slams.events.RobotStatusUpdateEvent;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.iflytek.cloud.SpeechError;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.OnClick;

public class HardwareTest extends CsjUIActivity {
    @BindView(R.id.setSnowBotWifi)
    Button setSnowBotWifi;
    @BindView(R.id.SnowBotBoubleArm)
    Button SnowBotBoubleArm;
    @BindView(R.id.SnowBotLeftArm)
    Button SnowBotLeftArm;
    @BindView(R.id.SnowBotRightArm)
    Button SnowBotRightArm;
    @BindView(R.id.SnowBotSpeechTest)
    Button SnowBotSpeechTest;
    @BindView(R.id.SnowBotFaceChange)
    Button SnowBotFaceChange;
    @BindView(R.id.SnowBotGetDownSN)
    Button SnowBotGetDownSN;
    @BindView(R.id.SnowBotGoHome)
    Button SnowBotGoHome;
    @BindView(R.id.testEnd)
    Button testEnd;
    @BindView(R.id.hw_showText)
    TextView hwShowText;
    @BindView(R.id.hw_ScrollView)
    ScrollView hwScrollView;
    @BindView(R.id.SnowSelfCheck)
    Button SnowSelfCheck;
    @BindView(R.id.activity_hardware_test)
    LinearLayout activityHardwareTest;

    private Handler mHandler = new Handler();

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
//        Csjlogger.setOutPutEnable(true);
        SharedUtil.setPreferInt(SharedKey.AIUISERVICESWITCH, 0);
    }

    @Override
    public void setListener() {

    }


    @Override
    protected void onPause() {
        SharedUtil.setPreferInt(SharedKey.AIUISERVICESWITCH, 1);
        super.onPause();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_hardware_test;
    }

    private void speech(String text) {
        CsjSpeechSynthesizer.getSynthesizer().startSpeaking(text, new CsjSynthesizerListener() {
            @Override
            public void onSpeakBegin() {
                postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_SPEAK));
            }

            @Override
            public void onCompleted(SpeechError speechError) {
                postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_NORMAL));
            }
        });
    }

    int expression = Constant.Expression.EXPRESSION_NORMAL;

    public void expressionTest() {
        postEvent(new ExpressionEvent(expression));

        expression++;
        if (expression > Constant.Expression.EXPRESSION_QUERIES) {
            expression = Constant.Expression.EXPRESSION_NORMAL;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void debugMessageRec(HWTestEvent event) {
        hwShowText.append(event.logMsg + "\n");
        mHandler.post(() -> hwScrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }


    @OnClick({R.id.setSnowBotWifi, R.id.SnowBotBoubleArm, R.id.SnowBotLeftArm,
            R.id.SnowBotRightArm, R.id.SnowBotSpeechTest, R.id.SnowBotFaceChange,
            R.id.SnowBotGetDownSN, R.id.SnowBotGoHome, R.id.testEnd, R.id.SnowSelfCheck})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.setSnowBotWifi:
                SnowBotManager.getInstance().getRobotSN();
                break;
            case R.id.SnowSelfCheck:
                SnowBotManager.getInstance().requireSelfCheck();
                break;
            case R.id.SnowBotBoubleArm:
                SnowBotManager.getInstance().swingDoubleArm((byte) 0x01);
                break;
            case R.id.SnowBotLeftArm:
                SnowBotManager.getInstance().swingLeftArm((byte) 0x02);
                break;
            case R.id.SnowBotRightArm:
                SnowBotManager.getInstance().swingRightArm((byte) 0x02);
                break;
            case R.id.SnowBotSpeechTest:
                speech("欢迎使用苏州穿山甲机器人产品");
                break;
            case R.id.SnowBotFaceChange:
                expressionTest();
                break;
            case R.id.SnowBotGetDownSN:
                SnowBotManager.getInstance().getRobotDownSN();
                break;
            case R.id.SnowBotGoHome:
                SnowBotManager.getInstance().goHome();
                break;
            case R.id.testEnd:
                finish();
                break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void robotStatusUpdated(RobotStatusUpdateEvent event) {
        hwShowText.append("Robot is isCharging [" + String.valueOf(event.isCharging()) + "], BatteryPercentage is 【"
                + String.valueOf(event.getBatteryPercentage()) + "】 \n");
        mHandler.post(() -> hwScrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }
}
