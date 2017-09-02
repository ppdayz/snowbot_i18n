package com.csjbot.snowbot.activity.aiui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.bean.aiui.MusicBean;
import com.csjbot.snowbot.services.CsjSpeechSynthesizer2;
import com.csjbot.snowbot.views.aiui.playerview.MusicPlayerView;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SynthesizerListener;

import java.util.Date;
import java.util.List;

/*
{
    "content": {
        "result": {
            "sid": "cid6f190742@ch00ba0b57fed3010073",
            "intent": {
                "data": {
                    "result": [
                        {
                            "singeraliasnames": [
                                "Jacky",
                                "二谦",
                                "谦谦"
                            ],
                            "songname": "丑八怪",
                            "publishtime": 1381161600,
                            "tagnames": [
                                "流行",
                                "90后",
                                "国语",
                                "孤独",
                                "安静",
                                "放松",
                                "伤感",
                                "下午茶",
                                "清新",
                                "思念",
                                "大陆",
                                ""
                            ],
                            "neatsongname": [
                                "丑八怪"
                            ],
                            "singerids": [
                                "207099"
                            ],
                            "programname": "",
                            "itemid": "4816888",
                            "albumname": "意外",
                            "singernames": [
                                "薛之谦"
                            ],
                            "audiopath": "http://vbox.hf.openstorage.cn/ctimusic/128/2015-07-23/%E8%96%9B%E4%B9%8B%E8%B0%A6/%E6%84%8F%E5%A4%96/%E4%B8%91%E5%85%AB%E6%80%AA.mp3",
                            "movienames": []
                        },
                        {
                            "singeraliasnames": [
                                ""
                            ],
                            "songname": "丑八怪",
                            "publishtime": 1388505600,
                            "tagnames": [
                                "放松",
                                "流行",
                                "90后",
                                "小语种",
                                ""
                            ],
                            "neatsongname": [
                                "丑八怪"
                            ],
                            "singerids": [
                                "2694698"
                            ],
                            "programname": "",
                            "itemid": "57274949",
                            "albumname": "",
                            "singernames": [
                                "余泽雅"
                            ],
                            "audiopath": "http://vbox.hf.openstorage.cn/ctimusic/128/2016-05-03/%E4%BD%99%E6%B3%BD%E9%9B%85/%E5%9C%A8%E7%BA%BF%E7%83%AD%E6%90%9C%EF%BC%88%E5%8D%8E%E8%AF%AD%EF%BC%89%E7%B3%BB%E5%88%97125/%E4%B8%91%E5%85%AB%E6%80%AA1462280438.mp3",
                            "movienames": []
                        },
                        {
                            "singeraliasnames": [
                                ""
                            ],
                            "songname": "丑八怪",
                            "publishtime": 1457452800,
                            "tagnames": [
                                ""
                            ],
                            "neatsongname": [
                                "丑八怪"
                            ],
                            "singerids": [
                                "2359917",
                                "187568",
                                "335380"
                            ],
                            "programname": "",
                            "itemid": "55494042",
                            "albumname": "Wifi DJ",
                            "singernames": [
                                "六月",
                                "小精灵",
                                "寐加岛"
                            ],
                            "audiopath": "http://vbox.hf.openstorage.cn/ctimusic/128/2016-03-17/%E5%AF%90%E5%8A%A0%E5%B2%9B%E5%B0%8F%E7%B2%BE%E7%81%B5%E5%85%AD%E6%9C%88/Wifi%20DJ/%E4%B8%91%E5%85%AB%E6%80%AA%20%28Remix%291458195184.mp3",
                            "movienames": []
                        },
                    ],
                    "sem_score": {
                        "song": {
                            "lcs": 1,
                            "txt": "丑八怪",
                            "pos": "ps"
                        },
                        "top": 0
                    },
                    "inherit": 0,
                    "isCached": 0,
                    "priority": 0
                },
                "answer": {
                    "text": "请欣赏薛之谦的歌曲丑八怪"
                },
            }
        },
        "arg2": 0,
        "eventType": 1,
        "arg1": 0,
        "info": {
            "data": [
                {
                    "content": [
                        {
                            "dtf": "json",
                            "cnt_id": "0",
                            "dte": "utf8"
                        }
                    ],
                    "params": {
                        "rstid": 1,
                        "sub": "nlp",
                        "lrst": false
                    }
                }
            ]
        }
    },
    "type": "aiui_event"
}
*/
public class MusicActivityBack extends Activity {
    private MusicPlayerView musicPlayerView;
    private MediaPlayer mediaPlayer = new MediaPlayer();        //媒体播放器对象
    private Handler mHandler = new Handler();
    private LocalBroadcastManager lbm;
    private Intent expressionIntent = new Intent(Constant.Expression.ACTION_EXPRESSION_FACE);

    // 准备播放的状态
    private boolean preparedPlay = false, speakFinished = false;

    // 单曲循环和列表循环
    private boolean isLoopPlay = false, isCycle = false;

    //暂停状态
    private boolean isPause;

    private Uri uri;     //音乐播放路径
    private String data, singer, name, answerText;
    private List<MusicBean.ResultBean> musicList = null;
    private int index = 0;
    private Intent isMusicInt = new Intent(Constant.ACTION_IN_ACTIVITY);

    //准备好，开始播放
    private BroadcastReceiver playBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            speakFinished = true;
            if (preparedPlay) {
                mediaPlayer.start();    //开始播放
                musicPlayerView.start();
            }
        }
    };
    //唤醒，退出界面
    private BroadcastReceiver wakeupReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            isMusicInt.putExtra("ismusic", false);
            lbm.sendBroadcast(isMusicInt);
            Csjlogger.debug("结束音乐wakeupReceiver");
            finishMedia();
        }
    };
    //休眠指令，退出界面（MusicX）
    private BroadcastReceiver sleepReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finishMedia();
        }
    };

    private BroadcastReceiver touchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
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
            Csjlogger.debug("结束音乐otherCmdReceiver");
            finishMedia();
        }
    };

    private BroadcastReceiver musicDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            data = intent.getStringExtra("data");
            MusicBean musicBean = JSON.parseObject(data, MusicBean.class);
            musicList = musicBean.getResult();

            uri = Uri.parse(musicList.get(0).getAudiopath());
            singer = musicList.get(0).getSingernames().get(0);
            name = musicList.get(0).getSongname();
            answerText = "请欣赏" + singer + "的歌曲" + name;
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

    private BroadcastReceiver musicControl = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("action");
            switch (action) {
                case Constant.Music.INSTYPE_PAUSE:
                    mediaPlayer.pause();
                    break;
                case Constant.Music.INSTYPE_NEXT:
                    playNext();
                    break;
                case Constant.Music.INSTYPE_PAST:
                    playPast();
                    break;
                case Constant.Music.INSTYPE_CYCLE:
                    // 单曲循环和列表循环不能同时
                    if (!isCycle) {
                        isCycle = true;
                        isLoopPlay = false;
                    } else {
                        isCycle = false;
                    }
                    break;
                case Constant.Music.INSTYPE_LOOP:
                    // 单曲循环和列表循环不能同时
                    if (!isLoopPlay) {
                        isLoopPlay = true;
                        isCycle = false;
                    } else {
                        isLoopPlay = false;
                    }
                    break;
                case Constant.Music.INSTYPE_REPEAT:
//                    Csjlogger.debug("重新播放");
                    musicPlayerView.setProgress(0);
                    musicPlayerView.start();
                    mediaPlayer.seekTo(0);
                    mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                        @Override
                        public void onSeekComplete(MediaPlayer mp) {
                            mp.start();
                        }
                    });
                    play();
                    break;
                case Constant.Music.INSTYPE_REPLAY:
                    continueToPlay();
                    break;
                default:
                    break;
            }
        }
    };


    private void changeExpression(int expression) {
        expressionIntent.putExtra(Constant.Expression.EXTRA_EXPRESSION_FACE, expression);
        lbm.sendBroadcast(expressionIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        musicPlayerView = (MusicPlayerView) findViewById(R.id.mpv);
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
        MusicBean musicBean = JSON.parseObject(data, MusicBean.class);
        musicList = musicBean.getResult();

        uri = Uri.parse(musicList.get(0).getAudiopath());
        singer = musicList.get(0).getSingernames().get(0);
        name = musicList.get(0).getSongname();
        answerText = "请欣赏" + singer + "的歌曲" + name;

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                play();
            }
        }, 200);

        speakAndPreparePlay(answerText);

        lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(playBroadcastReceiver, new IntentFilter(Constant.ACTION_MUSIC_READY_BEGIN));
        lbm.registerReceiver(wakeupReceiver, new IntentFilter(Constant.ACTION_WAKEUP));
        lbm.registerReceiver(touchReceiver, new IntentFilter(Constant.ACTION_TOUCH_GET));
//        lbm.registerReceiver(musicControl, new IntentFilter(Constant.ACTION_MUSIC_CONTROL));
        lbm.registerReceiver(sleepReceive, new IntentFilter(Constant.ACTION_SLEEP));
        lbm.registerReceiver(musicDataReceiver, new IntentFilter(Constant.ACTION_MUSIC_DATA));
        lbm.registerReceiver(otherCmdReceiver, new IntentFilter(Constant.ACTION_OTHER_CMD));

    }


    /**
     * 播放上一首
     */
    private void playPast() {
        index--;

        if (index < 0) {
            index = musicList.size() - 1;
        }

        MusicBean.ResultBean past = musicList.get(index);
        uri = Uri.parse(past.getAudiopath());
        singer = past.getSingernames().get(0);
        name = past.getSongname();
        answerText = "请欣赏" + singer + "的歌曲" + name;

        preparedPlay = false;
        speakFinished = false;
        musicPlayerView.stop();
        musicPlayerView.setProgress(0);
        play();
        speakAndPreparePlay(answerText);
    }

    /**
     * 播放下一首
     */
    private void playNext() {
        index++;

        if (index > musicList.size()) {
            index = 0;
        }

        MusicBean.ResultBean next = musicList.get(index);
        uri = Uri.parse(next.getAudiopath());
        singer = next.getSingernames().get(0);
        name = next.getSongname();
        answerText = "请欣赏" + singer + "的歌曲" + name;

        preparedPlay = false;
        speakFinished = false;
        musicPlayerView.stop();
        musicPlayerView.setProgress(0);
        play();
        speakAndPreparePlay(answerText);
    }

    /**
     * 继续播放
     */
    private void continueToPlay() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            musicPlayerView.start();
        }
    }

    /**
     * 说完话后播放音乐
     */
    private void speakAndPreparePlay(String text) {
        CsjSpeechSynthesizer2.getSynthesizer().startSpeaking(text, new SynthesizerListener() {
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

    private void mediaError() {
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Csjlogger.debug("OnError - Error code: " + what + " Extra code: " + extra);
                switch (what) {
                    case -1004:
                        Csjlogger.debug("MEDIA_ERROR_IO");
                        break;
                    case -1007:
                        Csjlogger.debug("MEDIA_ERROR_MALFORMED");
                        break;
                    case 200:
                        Csjlogger.debug("MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK");
                        break;
                    case 100:
                        Csjlogger.debug("MEDIA_ERROR_SERVER_DIED");
                        break;
                    case -110:
                        Csjlogger.debug("MEDIA_ERROR_TIMED_OUT");
                        break;
                    case 1:
                        Csjlogger.debug("MEDIA_ERROR_UNKNOWN");
                        break;
                    case -1010:
                        Csjlogger.debug("MEDIA_ERROR_UNSUPPORTED");
                        break;
                }
                switch (extra) {
                    case 800:
                        Csjlogger.debug("MEDIA_INFO_BAD_INTERLEAVING");
                        break;
                    case 702:
                        Csjlogger.debug("MEDIA_INFO_BUFFERING_END");
                        break;
                    case 701:
                        Csjlogger.debug("MEDIA_INFO_METADATA_UPDATE");
                        break;
                    case 802:
                        Csjlogger.debug("MEDIA_INFO_METADATA_UPDATE");
                        break;
                    case 801:
                        Csjlogger.debug("MEDIA_INFO_NOT_SEEKABLE");
                        break;
                    case 1:
                        Csjlogger.debug("MEDIA_INFO_UNKNOWN");
                        break;
                    case 3:
                        Csjlogger.debug("MEDIA_INFO_VIDEO_RENDERING_START");
                        break;
                    case 700:
                        Csjlogger.debug("MEDIA_INFO_VIDEO_TRACK_LAGGING");
                        break;
                }
                return false;
            }
        });
    }

    /**
     * 播放音乐
     */
    private void play() {
        Csjlogger.debug("播放第[" + (index + 1) + "]首歌 [" + name + "], 总共(" + musicList.size() + ")首歌");

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.prepare();    //进行缓冲
            mediaPlayer.setOnPreparedListener(new PreparedListener());//注册一个监听器

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if (isLoopPlay) {
                        playNext();
                        return;
                    }
                    if (isCycle) {
                        speakAndPreparePlay(answerText);
                        play();
                        return;
                    }
                }
            });

            mediaError();

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
            musicPlayerView.stop();
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
            CsjSpeechSynthesizer2.getSynthesizer().stopSpeaking();
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
        lbm.unregisterReceiver(playBroadcastReceiver);
        lbm.unregisterReceiver(musicControl);
        lbm.unregisterReceiver(sleepReceive);
        lbm.unregisterReceiver(musicDataReceiver);
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
