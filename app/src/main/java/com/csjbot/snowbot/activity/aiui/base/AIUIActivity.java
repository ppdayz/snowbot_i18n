package com.csjbot.snowbot.activity.aiui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.csjbot.csjbase.base.CsjBaseActivity;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.utils.SpeechStatus;
import com.csjbot.snowbot_rogue.Events.AIUIEvent;
import com.csjbot.snowbot_rogue.Events.EventsConstants;
import com.csjbot.snowbot_rogue.Events.ExpressionEvent;
import com.csjbot.snowbot_rogue.app.CsjSpeechSynthesizer;
import com.csjbot.snowbot_rogue.utils.Constant;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Copyright (c) 2016, SuZhou CsjBot. All Rights Reserved.
 * www.csjbot.com
 * <p>
 * Created by 浦耀宗 at 2016/12/18 0018-14:32.
 * Email: puyz@csjbot.com
 */

public abstract class AIUIActivity extends CsjBaseActivity {
    protected String currentService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentService = getIntent().getStringExtra("serviceType");
        Csjlogger.debug("currentService is " + currentService);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    abstract public int getLayoutId();


    @Subscribe(threadMode = ThreadMode.MAIN)
    public boolean onAIUIEvent(AIUIEvent event) {
        Csjlogger.debug("AIUIEvent " + event.getTag());
        boolean isCaptured = true;
        switch (event.getTag()) {
            case EventsConstants.AIUIEvents.AIUI_EVENT_WAKEUP:
                wakeup();
                break;
            case EventsConstants.AIUIEvents.AIUI_EVENT_FORCE_SLEEP:
                forceSleep();
                break;
            case EventsConstants.AIUIEvents.AIUI_EVENT_TIME_OUT_SLEEP:
                timeOutSleep();
                break;
            /**
             * 在讲故事的时候,跟他说话会中断讲故事,跳到聊天页面,注释掉
             */
            //            case EventsConstants.AIUIEvents.AIUI_EVENT_SERVICE_COMMING:
//                serviceComing((String) event.data);
//                break;
            case EventsConstants.AIUIEvents.AIUI_EVENT_TOUCH_GET:
                touchGet();
                break;
            case EventsConstants.AIUIEvents.AIUI_EVENT_OTHER_CMD:
                otherCmd();
            default:
                isCaptured = false;
                break;
        }
        return isCaptured;
    }

    public boolean otherCmd() {
        finish();
        return false;
    }

    public boolean touchGet() {
        return false;
    }

    public boolean timeOutSleep() {
//        finish();
        return false;
    }

    public boolean forceSleep() {
        finish();
        return false;
    }

    public boolean wakeup() {
        postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_NORMAL));
        finish();
        return false;
    }

    public boolean serviceComing(String service) {
        if (service != null && !currentService.equalsIgnoreCase(service)) {

            Csjlogger.debug("service is" + service);
            CsjSpeechSynthesizer.getSynthesizer().stopSpeaking();
            finish();
            return true;
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        SpeechStatus.getIstance().setSpeakFinished(true);
    }
}
