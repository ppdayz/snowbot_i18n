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

import com.csjbot.snowbot.R;
import com.csjbot.snowbot.views.aiui.playerview.MusicPlayerView;
import com.csjbot.snowbot_rogue.servers.serials.SnowBotSerialServer;
import com.csjbot.snowbot_rogue.servers.slams.SnowBotMoveServer;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.slamtec.slamware.action.MoveDirection;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DacneActivity extends Activity {

    private MusicPlayerView mpv;
    private MediaPlayer mediaPlayer = new MediaPlayer();        //媒体播放器对象
    private String path, singer, name;                        //音乐文件路径
    private boolean isPause;                    //暂停状态
    private Handler mHandler = new Handler();
    private LocalBroadcastManager localBroadcastManager;
    private boolean preparedPlay = false, speakFinished = true;
    private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    private SnowBotMoveServer moveServer = SnowBotMoveServer.getInstance();
    private SnowBotSerialServer snowBotAction = SnowBotSerialServer.getOurInstance();

    private BroadcastReceiver playBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            speakFinished = true;
            if (preparedPlay) {
                mediaPlayer.start();    //开始播放
                mpv.start();
            }
        }
    };

    private BroadcastReceiver wakeupReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onBackPressed();
                }
            }, 200);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        mpv = (MusicPlayerView) findViewById(R.id.mpv);
        mpv.setAutoProgress(true);

        mpv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    mpv.stop();
                } else {
                    mediaPlayer.start();
                    mpv.start();
                }
            }
        });

        if (mediaPlayer.isPlaying()) {
            stop();
        }
        path = Constant.SDCARD_MUSIC_PATH + "dacne_music.mp3";

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                play(0);
            }
        }, 200);

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(playBroadcastReceiver, new IntentFilter(Constant.ACTION_MUSIC_READY_BEGIN));
        localBroadcastManager.registerReceiver(wakeupReceiver, new IntentFilter(Constant.ACTION_WAKEUP));

        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                while (!singleThreadExecutor.isShutdown()) {
                    snowBotAction.swingDoubleArm((byte) 20);

                    int count = new Random().nextInt(2);
                    int i = 0;

                    for (int j = 0; j < count; j++) {
                        // 前进
                        moveServer.moveBy(MoveDirection.FORWARD);

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // 后退
                        moveServer.moveBy(MoveDirection.BACKWARD);

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    count = new Random().nextInt(3);

                    for (int j = 0; j < count; j++) {
                        // 左转
                        while (i < 20) {
                            moveServer.moveBy(MoveDirection.TURN_LEFT);
                            i++;
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    count = new Random().nextInt(2);
                    for (int j = 0; j < count; j++) {
                        // 前进
                        moveServer.moveBy(MoveDirection.FORWARD);
                        try {

                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // 后退
                        moveServer.moveBy(MoveDirection.BACKWARD);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    count = new Random().nextInt(3);
                    for (int j = 0; j < count; j++) {
                        // 右转
                        i = 0;
                        while (i < 20) {
                            moveServer.moveBy(MoveDirection.TURN_RIGHT);
                            i++;

                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
    }


    /**
     * 播放音乐
     *
     * @param position
     */
    private void play(int position) {
        try {
            mediaPlayer.reset();//把各项参数恢复到初始状态
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();    //进行缓冲
            mediaPlayer.setOnPreparedListener(new PreparedListener(position));//注册一个监听器
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    onBackPressed();
                    /*Message msg = mHandler.obtainMessage(EVENT_PLAY_OVER);
                    msg.sendToTarget();*/
                }
            });
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

    @Override
    public void onBackPressed() {
        singleThreadExecutor.shutdownNow();
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        } catch (IllegalStateException e) {

        }

        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        localBroadcastManager.unregisterReceiver(wakeupReceiver);
        localBroadcastManager.unregisterReceiver(playBroadcastReceiver);
        super.onDestroy();
    }

    /**
     * 实现一个OnPrepareLister接口,当音乐准备好的时候开始播放
     */
    private final class PreparedListener implements MediaPlayer.OnPreparedListener {
        private int positon;

        public PreparedListener(int positon) {
            this.positon = positon;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            preparedPlay = true;
            mpv.setMax(mediaPlayer.getDuration() / 1000);

            if (speakFinished) {
                mediaPlayer.start();    //开始播放
                mpv.start();
            }

            if (positon > 0) {    //如果音乐不是从头播放
                mediaPlayer.seekTo(positon);
            }
        }
    }
}
