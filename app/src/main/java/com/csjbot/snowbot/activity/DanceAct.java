package com.csjbot.snowbot.activity;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.android.core.entry.Static;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.bean.Dance;
import com.csjbot.snowbot.bean.DirectorDance;
import com.csjbot.snowbot.utils.DanceUtil;
import com.csjbot.snowbot_rogue.Events.AIUIEvent;
import com.csjbot.snowbot_rogue.Events.EventsConstants;
import com.csjbot.snowbot_rogue.platform.SnowBotManager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static com.csjbot.snowbot.R.id.play2_iv;

/**
 * @author: jl
 * @Time: 2017/2/8
 * @Desc:
 */
public class DanceAct extends CsjUIActivity {

    @BindView(R.id.play1_iv)
    ImageView play1Iv;
    @BindView(play2_iv)
    ImageView play2Iv;
    @BindView(R.id.play3_iv)
    ImageView play3Iv;
    private Dance dance1;
    private Dance dance2;
    private Dance dance3;
    private List<Dance> dances = new ArrayList();
    private MediaPlayer mp;
    private Handler mHandler;
    private int startTime = 0;
    private Runnable mRunnable;
    private boolean turn = false;//false 右转

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        setupBack();
        initDance();
        init();
    }

    private void init() {

    }

    private void initDance() {
        DirectorDance directorDance1 = new DirectorDance();
        DirectorDance directorDance2 = new DirectorDance();
        DirectorDance directorDance3 = new DirectorDance();
        dance1 = directorDance1.getDance1();
        dance2 = directorDance2.getDance2();
        dance3 = directorDance3.getDance3();
        dances.add(dance1);
        dances.add(dance2);
        dances.add(dance3);
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.dance_act;
    }


    @Override
    protected void onDestroy() {
        stop(dances);
        try {
            if (mp != null) {
                mp.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @OnClick(R.id.play1_iv)
    public void play1_iv() {
        if (!dance1.isPlaying()) {
            stop(dances);
            play(dance1);
        } else {
            stop(dances);
        }
        setPlayIco();

    }

    @OnClick(play2_iv)
    public void play2_iv() {
        if (!dance2.isPlaying()) {
            stop(dances);
            play(dance2);
        } else {
            stop(dances);
        }
        setPlayIco();
    }

    @OnClick(R.id.play3_iv)
    public void play3_iv() {
        if (!dance3.isPlaying()) {
            stop(dances);
            play(dance3);
        } else {
            stop(dances);
        }
        setPlayIco();

    }


    private void play(Dance dance) {
        AssetFileDescriptor fileDescriptor;
        try {
            fileDescriptor = DanceAct.this.getAssets().openFd(dance.getMusicPath());
            mp = new MediaPlayer();
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            fileDescriptor.close();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stop(dances);
                }
            });
            mp.prepare();
            mp.start();//开始播放
            startDance((mp.getDuration() / 1000) * 1000, dance);
            dance.setPlaying(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void stop(List<Dance> dances) {
        for (Dance dance : dances) {
            if (dance.isPlaying() && mp != null) {
                mp.stop();
                stopDance();
                dance.setPlaying(false);
            }
        }
        play1Iv.setImageDrawable(Static.CONTEXT.getResources().getDrawable(R.drawable.play));
        play2Iv.setImageDrawable(Static.CONTEXT.getResources().getDrawable(R.drawable.play));
        play3Iv.setImageDrawable(Static.CONTEXT.getResources().getDrawable(R.drawable.play));
    }

    private void stopDance() {
        mHandler.removeCallbacks(mRunnable);
    }

    private void startDance(int time, Dance dance) {
        startTime = time / 1000;
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                String tempSr = startTime + "";
                String lastStr = tempSr.substring(tempSr.length() - 1);
                Csjlogger.debug("dance", "startTime:  " + startTime + " lastStr: " + lastStr);


                if (startTime % 15 == 0) {
                    if (!turn) {
                        DanceUtil.RightCircle(dance);
                        Csjlogger.info("dance", " RightCircle");
                        turn = true;
                    } else {
                        DanceUtil.LeftCircle(dance);
                        Csjlogger.info("dance", " LeftCircle");
                        turn = false;
                    }
                }


                if (lastStr.equals("6")) {
                    DanceUtil.LeftHand(dance);
                    Csjlogger.info("dance", " LeftHand");

                }

                if (lastStr.equals("3")) {
                    DanceUtil.RightHand(dance);
                    Csjlogger.info("dance", " RightHand");
                }

                if (lastStr.equals("9")) {
                    DanceUtil.LRHand(dance);
                    Csjlogger.info("dance", " LRHand");
                }
                startTime--;
                if (startTime == 0) {
                    mHandler.removeCallbacks(mRunnable);
                } else {
                    mHandler.postDelayed(this, 1000);
                }
            }
        };
        mHandler.post(mRunnable);
    }


    private void setPlayIco() {
        if (!dance1.isPlaying()) {
            play1Iv.setImageDrawable(Static.CONTEXT.getResources().getDrawable(R.drawable.play));
        } else {
            play1Iv.setImageDrawable(Static.CONTEXT.getResources().getDrawable(R.drawable.stop));
        }

        if (!dance2.isPlaying()) {
            play2Iv.setImageDrawable(Static.CONTEXT.getResources().getDrawable(R.drawable.play));
        } else {
            play2Iv.setImageDrawable(Static.CONTEXT.getResources().getDrawable(R.drawable.stop));
        }

        if (!dance3.isPlaying()) {
            play3Iv.setImageDrawable(Static.CONTEXT.getResources().getDrawable(R.drawable.play));
        } else {
            play3Iv.setImageDrawable(Static.CONTEXT.getResources().getDrawable(R.drawable.stop));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAiuiEvent(AIUIEvent event) {
        switch (event.getTag()) {
            case EventsConstants.AIUIEvents.AIUI_EVENT_WAKEUP:
                finish();
                SnowBotManager.getInstance().cancelAction();
                break;
            default:
                break;
        }
    }
}
