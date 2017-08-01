package com.csjbot.snowbot.activity.aiui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.aiui.base.AIUIActivity;
import com.csjbot.snowbot_rogue.Events.AIUIEvent;
import com.csjbot.snowbot_rogue.Events.EventsConstants;

public class DateTimeActivity extends AIUIActivity {
    private TextView timeDateText;
    private String timeDate;
    Handler mHandler = new Handler();

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        timeDateText = (TextView) findViewById(R.id.timeDateText);
        timeDate = getIntent().getStringExtra("answerText");
        timeDateText.setText(timeDate);
    }

    /**
     * 处于activity时，调用此方法
     *
     * @param intent
     */
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        timeDate = intent.getStringExtra("answerText");
        timeDateText.setText(timeDate);
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_time_date;
    }

    @Override
    public boolean onAIUIEvent(AIUIEvent event) {
        if (super.onAIUIEvent(event)) {
            return true;
        }
        switch (event.getTag()) {
            case EventsConstants.AIUIEvents.AIUI_EVENT_VOICE_END:
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 3000);
                break;
            default:
                Csjlogger.debug("event unCaptured   -》" + event.getTag());
                break;
        }
        return false;
    }

}
