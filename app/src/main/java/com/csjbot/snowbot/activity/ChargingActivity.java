package com.csjbot.snowbot.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.ImageView;

import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.views.WaterWaveView;
import com.csjbot.snowbot_rogue.platform.SnowBotManager;
import com.csjbot.snowbot_rogue.servers.slams.events.RobotStatusUpdateEvent;
import com.slamtec.slamware.action.MoveDirection;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;

/**
 * Created by xiasuhuei321 on 2017/7/11.
 * author:luo
 * e-mail:xiasuhuei321@163.com
 */

public class ChargingActivity extends CsjUIActivity {
    volatile int mProgress = 0;

    @BindView(R.id.waterWaveView)
    WaterWaveView waterWaveView;
    private Handler mHandler;
    private boolean isRunning;

    @Override
    public boolean useEventBus() {
        return true;
    }


    private void setupBackIcon() {
        ImageView back = (ImageView) findViewById(R.id.totleBack);
        if (back != null) {
            back.setImageResource(R.mipmap.back_white);

            back.setOnClickListener(v -> {
                EventBus.getDefault().post(new GoHomeEvent(GoHomeEvent.EXIT_CHARGING_PAGE, ""));
                finish();
            });
        }
    }

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
//        postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_EVENT_NO_SPEAK_TEXT, null));
        setupBackIcon();

        isRunning = true;
        new Thread(() -> {
            while (isRunning) {
                runOnUiThread(() -> {
                    mProgress += 2;
                    waterWaveView.setProgress(mProgress);
                    if (mProgress >= 100) {
                        mProgress = 0;
                    }
                });
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        mHandler = new Handler();
//        mHandler.postDelayed(() -> {
//            SnowBotManager manager = SnowBotManager.getInstance();
//            manager.moveBy(MoveDirection.FORWARD);
//            Csjlogger.info("==调用了moveBy");
//        }, 5000);

        waterWaveView.setOnClickListener(view -> {
            Csjlogger.debug("向前运动");
            SnowBotManager.getInstance().moveBy(MoveDirection.FORWARD);
        });
        // TODO: 2017/7/12 屏蔽 语音
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_charge;
    }

    @Override
    protected void onResume() {
        super.onResume();
        waterWaveView.startWave();
    }

    @Override
    protected void onPause() {
        super.onPause();
        waterWaveView.stopWave();
    }

    @Override
    protected void onDestroy() {
        isRunning = false;
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void robotStatusUpdated(RobotStatusUpdateEvent event) {
        boolean charging = event.isCharging();
        if (!charging) {
            Csjlogger.info("==调用了moveBy");
            this.finish();
        }
    }

    //屏蔽返回键的代码:
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_HOME:
                return true;
            case KeyEvent.KEYCODE_BACK:
                return true;
            case KeyEvent.KEYCODE_CALL:
                return true;
            case KeyEvent.KEYCODE_SYM:
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                return true;
            case KeyEvent.KEYCODE_STAR:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
