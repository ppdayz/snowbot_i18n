package com.csjbot.snowbot.activity.aiui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot_rogue.app.CsjSpeechSynthesizer;
import com.csjbot.snowbot.bean.aiui.StoryBean;
import com.csjbot.snowbot.views.aiui.playerview.MusicPlayerView;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SynthesizerListener;

import java.util.Date;
import java.util.List;

public class StoryActivityBack extends Activity {
    private MusicPlayerView musicPlayerView;
    private MediaPlayer mediaPlayer = new MediaPlayer();        //媒体播放器对象
    private Handler mHandler = new Handler();
    private LocalBroadcastManager lbm;
    private Intent expressionIntent = new Intent(Constant.Expression.ACTION_EXPRESSION_FACE);

    // 准备播放的状态
    private boolean preparedPlay = false, speakFinished = false;

    //暂停状态
    private boolean isPause;

    private String url;     //音乐播放路径
    private String data, name, answerText;
    private List<StoryBean.ResultBean> storyList = null;
    private int index = 0;
    private Intent isMusicInt = new Intent(Constant.ACTION_IN_ACTIVITY);

    //唤醒，退出界面
    private BroadcastReceiver wakeupReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isMusicInt.putExtra("ismusic", false);
                    lbm.sendBroadcast(isMusicInt);
                    finishMedia();
                }
            }, 200);
        }
    };
    //休眠指令，退出界面（MusicX）
    private BroadcastReceiver sleepReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isMusicInt.putExtra("ismusic", false);
                    lbm.sendBroadcast(isMusicInt);
                    finishMedia();
                }
            }, 200);
        }
    };

    private BroadcastReceiver touchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isMusicInt.putExtra("ismusic", false);
                    lbm.sendBroadcast(isMusicInt);
                    finishMedia();
                }
            }, 200);
        }
    };
    //除播放故事外的任何指令，都退出界面
    private BroadcastReceiver otherCmdReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isMusicInt.putExtra("ismusic", false);
            lbm.sendBroadcast(isMusicInt);
            finishMedia();
        }
    };
    private BroadcastReceiver storyDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            data = intent.getStringExtra("data");
            StoryBean storyBean = JSON.parseObject(data, StoryBean.class);
            storyList = storyBean.getResult();
//            uri = Uri.parse(storyList.get(0).getPlayUrl());
            url = storyList.get(0).getPlayUrl();
            Csjlogger.debug("url =" + url);
            name = storyList.get(0).getName();
            answerText = "请欣赏" + name;
            preparedPlay = false;
            speakFinished = false;
            musicPlayerView.stop();
            musicPlayerView.setProgress(0);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    play();
                }
            }, 200);
            speakAndPreparePlay(answerText);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        musicPlayerView = (MusicPlayerView) findViewById(R.id.mpv_story);
        musicPlayerView.setAutoProgress(true);
        musicPlayerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    musicPlayerView.stop();
                } else {
                    mediaPlayer.start();
                    musicPlayerView.start();
                }
            }
        });

        if (mediaPlayer.isPlaying()) {
            stop();
        }

        data = getIntent().getStringExtra("data");
        Csjlogger.debug("data " + data);
        StoryBean stroyBean = JSON.parseObject(data, StoryBean.class);
        storyList = stroyBean.getResult();

//        uri = Uri.parse(storyList.get(0).getPlayUrl());
        url = storyList.get(0).getPlayUrl();
        name = storyList.get(0).getName();
        answerText = "请欣赏" + name;

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                play();
            }
        }, 200);

        speakAndPreparePlay(answerText);

        lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(otherCmdReceiver, new IntentFilter(Constant.ACTION_OTHER_CMD));
        lbm.registerReceiver(wakeupReceiver, new IntentFilter(Constant.ACTION_WAKEUP));
        lbm.registerReceiver(touchReceiver, new IntentFilter(Constant.ACTION_TOUCH_GET));
        lbm.registerReceiver(sleepReceive, new IntentFilter(Constant.ACTION_SLEEP));
        lbm.registerReceiver(storyDataReceiver, new IntentFilter(Constant.ACTION_STORY_DATA));
    }

    /**
     * 说完话后播放音乐
     */
    private void speakAndPreparePlay(String text) {
        CsjSpeechSynthesizer.getSynthesizer().startSpeaking(text, new SynthesizerListener() {
            @Override
            public void onSpeakBegin() {
                changeExpression(Constant.Expression.EXPRESSION_SPEAK);
            }

            @Override
            public void onBufferProgress(int i, int i1, int i2, String s) {

            }

            @Override
            public void onSpeakPaused() {

            }

            @Override
            public void onSpeakResumed() {

            }

            @Override
            public void onSpeakProgress(int i, int i1, int i2) {

            }

            @Override
            public void onCompleted(SpeechError speechError) {
                speakFinished = true;
//                Csjlogger.debug("preparedPlay " + preparedPlay);
                if (preparedPlay) {
                    mediaPlayer.start();    //开始播放
                    musicPlayerView.start();
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            changeExpression(Constant.Expression.EXPRESSION_NORMAL);
                        }
                    });
                }
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {
            }
        });
    }

    private void changeExpression(int expression) {
        expressionIntent.putExtra(Constant.Expression.EXTRA_EXPRESSION_FACE, expression);
        lbm.sendBroadcast(expressionIntent);
    }

    /**
     * 播放故事
     */
    private void play() {
        Csjlogger.debug("播放第[" + index + "]个故事 [" + name + "], 总共(" + storyList.size() + ")个故事");

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();    //进行缓冲
            mediaPlayer.setOnPreparedListener(new PreparedListener());//注册一个监听器

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 暂停音乐
     */
    private void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPause = true;
        }
    }

    /**
     * 停止音乐
     */
    private void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
//            try {
//                mediaPlayer.prepare(); // 在调用stop后如果需要再次通过start进行播放,需要之前调用prepare函数
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
    }

    private long lastClickTim = 0;

    @Override
    public void onBackPressed() {
        long curTime = (new Date()).getTime();//本地单击的时间
        if (curTime - lastClickTim > 10000 || lastClickTim == 0) {    //第一次，初始化为本次接收的时间
            lastClickTim = curTime;
            Toast.makeText(this, "再按一次后退键退出应用程序", Toast.LENGTH_SHORT).show();
        } else if (preparedPlay) {
            changeExpression(Constant.Expression.EXPRESSION_NORMAL);
            CsjSpeechSynthesizer.getSynthesizer().stopSpeaking();
            if (mediaPlayer != null) {
                try {
                    if (mediaPlayer.isPlaying()) {
                        this.pause();
                    }
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                } catch (IllegalStateException e) {
                    Csjlogger.debug("IllegalStateException");
                }
            }
            this.finish();
        }
    }


    public void finishMedia() {
        changeExpression(Constant.Expression.EXPRESSION_NORMAL);
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    this.pause();
                }
                mediaPlayer.stop();
                mediaPlayer.release();
            } catch (IllegalStateException e) {
                Csjlogger.debug("IllegalStateException");
            }
        }
        this.finish();
    }


    @Override
    public void onDestroy() {
        lbm.unregisterReceiver(wakeupReceiver);
        lbm.unregisterReceiver(touchReceiver);
        lbm.unregisterReceiver(sleepReceive);
        lbm.unregisterReceiver(storyDataReceiver);
        lbm.unregisterReceiver(otherCmdReceiver);
        super.onDestroy();
    }

    /**
     * 实现一个OnPrepareLister接口,当音乐准备好的时候开始播放
     */
    private final class PreparedListener implements MediaPlayer.OnPreparedListener {

        public PreparedListener() {

        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            preparedPlay = true;
            musicPlayerView.setMax(mediaPlayer.getDuration() / 1000);

            if (speakFinished) {
                mediaPlayer.start();    //开始播放
                musicPlayerView.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        changeExpression(Constant.Expression.EXPRESSION_NORMAL);
                    }
                });

            }
        }
    }
}
