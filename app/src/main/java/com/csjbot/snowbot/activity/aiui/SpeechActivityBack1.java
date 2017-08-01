package com.csjbot.snowbot.activity.aiui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.csjbot.csjbase.base.CsjBaseActivity;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.views.aiui.CircleProgress;
import com.csjbot.snowbot_rogue.Events.AIUIEvent;
import com.csjbot.snowbot_rogue.Events.EventsConstants;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SpeechActivityBack1 extends CsjBaseActivity {
    private CircleProgress mProgressView;
    private String text;
    private int rc;
    private TextView textView;
    private boolean speakTextRec = false, rcRec = false;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
        }
    };

    private BroadcastReceiver speakText = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            text = intent.getStringExtra("speakText");
            rc = intent.getIntExtra("rc", 0);
            textView.setText("rc=" + rc + "\n" + text);
        }
    };

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public boolean onAIUIEvent(AIUIEvent event) {
        Csjlogger.debug("AIUIEvent " + event.getTag());
        boolean isCaptured = true;
        switch (event.getTag()) {
            case EventsConstants.AIUIEvents.AIUI_SPEAKTEXT_DATA:
                text = (String) event.data;
                speakTextRec = true;
                if (rcRec) {
                    speakTextRec = false;
                    rcRec = false;
                    textView.setText("rc=" + rc + "\n" + text);
                }
                break;
            case EventsConstants.AIUIEvents.AIUI_SPEAKTEXT_RC:
                rc = (int) event.data;
                rcRec = true;
                if (speakTextRec) {
                    speakTextRec = false;
                    rcRec = false;
                    textView.setText("rc=" + rc + "\n" + text);
                }
                break;
            default:
                isCaptured = false;
                break;
        }
        return isCaptured;
    }

    public void closeSpeech(View view) {
        onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        lbm.unregisterReceiver(speakText);
    }

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        mProgressView = (CircleProgress) findViewById(R.id.progress);
        mProgressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        textView = (TextView) findViewById(R.id.speakText);
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_speech;
    }
}
