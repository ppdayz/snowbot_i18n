package com.csjbot.snowbot.activity.aiui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;

import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.views.aiui.CircleProgress;
import com.csjbot.snowbot_rogue.servers.serials.SnowBotSerialServer;
import com.csjbot.snowbot_rogue.servers.slams.SnowBotMoveServer;
import com.csjbot.snowbot_rogue.utils.CSJToast;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.csjbot.snowbot_rogue.utils.VoiceParserCallBack;
import com.csjbot.snowbot_rogue.utils.voiceParser;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUnderstander;
import com.iflytek.cloud.SpeechUnderstanderListener;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.UnderstanderResult;
import com.slamtec.slamware.action.MoveDirection;

import java.util.Random;

public class SpeechActivityBack extends Activity implements VoiceParserCallBack {
    private CircleProgress mProgressView;
    // 语义理解对象（语音到语义）。
    private SpeechUnderstander mSpeechUnderstander;
    // 语音合成对象
    private SpeechSynthesizer mSpeechSynthesizer;

    private LocalBroadcastManager localBroadcastManager;
    private SnowBotSerialServer snowBotAction = SnowBotSerialServer.getOurInstance();
    private String[] wakeupTalk, touchTalk, errorTalk, firstTouchGet;
    private boolean isVoiceBusy = false, snowBotAngry;
    private int listenTimes = 0;
    private int touchCount = 0;
    private String touchedSayString;
    private static final int ANGRY_TOUCHED_COUNT = 5;
    private SnowBotMoveServer moveServer = SnowBotMoveServer.getInstance();

    private Intent expressionIntent = new Intent(Constant.Expression.ACTION_EXPRESSION_FACE);

    private Runnable touchCountPP = new Runnable() {
        @Override
        public void run() {
            touchCount = 0;
        }
    };

    /**
     * @Description: 逻辑是这样的， 在{@link touchCount} < {@link ANGRY_TOUCHED_COUNT } 之前摸小雪头，会随机在 {@link touchTalk}
     * 中找一句，并且会有个重置时间，当 5-10秒没有摸小雪头的时候，就会清除这个 {@link touchCount};
     * 如果是{@link touchCount}  = {@link ANGRY_TOUCHED_COUNT }，小雪就会暴走，5-10秒不理你，摸头也没用
     * @param
     * @author Administrator
     * @time 2016/8/17 0017
     */
    private BroadcastReceiver touchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            moveServer.connect();
            if (!isVoiceBusy && !snowBotAngry) {
                Csjlogger.debug("有人摸我！！！！！！");
                stopAllVoice();
                moveServer.connect();

                snowBotAction.swingDoubleArm((byte) 0x04);

                mHandler.removeCallbacks(touchCountPP);
                mHandler.postDelayed(touchCountPP, new Random().nextInt(5000) + 5000);
                /**
                 *   @see  {@link  R.array.touch_get_array}
                 */
                touchedSayString = touchTalk[new Random().nextInt(touchTalk.length)];

                if (touchCount < ANGRY_TOUCHED_COUNT) {
                    touchCount++;
                } else {
                    snowBotAngry = true;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            snowBotAngry = false;
                            touchCount = 0;
                            Csjlogger.debug("小雪不生气了！");
                        }
                    }, new Random().nextInt(5000) + 8000);

                    touchedSayString = SpeechActivityBack.this.getString(R.string.touch_angry_say);
                }


                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mProgressView.startSpeaking();
                        touchSpeak(touchedSayString);
                    }
                }, 50);
            }

        }
    };

    private BroadcastReceiver wakeupReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if (!isVoiceBusy) {
                Csjlogger.debug("我醒了！！！！！");
                stopAllVoice(false);
                moveServer.connect();
                snowBotAction.swingDoubleArm((byte) 0x02);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mProgressView.startSpeaking();
//                        short angle = intent.getShortExtra("angle", (short) 0);
//
//                        moveServer.turnRound(angle);
//                        wakeupSpeak(wakeupTalk[new Random().nextInt(wakeupTalk.length)]);
//                    wakeupSpeak("转身 " + angle);
                    }
                }, 100);
            }
        }
    };

    private Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);

        mProgressView = (CircleProgress) findViewById(R.id.progress);
        touchTalk = getResources().getStringArray(R.array.touch_get_array);
        wakeupTalk = getResources().getStringArray(R.array.wakeup_array);
        errorTalk = getResources().getStringArray(R.array.speak_error);
        firstTouchGet = getResources().getStringArray(R.array.first_touch_get_array);

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
//        localBroadcastManager.registerReceiver(touchReceiver, new IntentFilter(Constant.ACTION_TOUCH_GET));
        localBroadcastManager.registerReceiver(wakeupReceiver, new IntentFilter(Constant.ACTION_WAKEUP));

        mProgressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startListening();
            }
        });
        moveServer.connect();

        // 初始化对象
        mSpeechUnderstander = SpeechUnderstander.createUnderstander(this, null);
        // 初始化合成对象
        mSpeechSynthesizer = SpeechSynthesizer.createSynthesizer(this, new InitListener() {
            @Override
            public void onInit(int resault) {
                Csjlogger.debug("resault = " + resault);
            }
        });

        setupVoiceParam();

        String action = getIntent().getStringExtra("Action");
        final short angle = getIntent().getShortExtra("angle", (short) 0);

        if (action != null) {
            switch (action) {
                case Constant.ACTION_TOUCH_GET:
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mProgressView.startSpeaking();
//                            touchSpeak(firstTouchGet[new Random().nextInt(firstTouchGet.length)]);
                        }
                    }, 100);
                    break;
                case Constant.ACTION_WAKEUP:
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mProgressView.startSpeaking();
//                            short angle = getIntent.getShortExtra("angle", (short) 0);
//                            moveServer.turnRound(angle);
//                            wakeupSpeak(wakeupTalk[new Random().nextInt(wakeupTalk.length)]);
                        }
                    }, 100);
                    break;
                default:
                    break;
            }
        }
    }

    private void contiueListening() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                startUnderstand();
            }
        });
    }

    private void startListening() {
        stopAllVoice();
        mProgressView.startSpeaking();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                startUnderstand();
            }
        });
    }

    private void stopAllVoice(boolean changeExp) {
        Csjlogger.debug("stopAllVoice");
        mSpeechUnderstander.stopUnderstanding();
        mSpeechUnderstander.cancel();
        // 语音合成对象
        mSpeechSynthesizer.stopSpeaking();
        mProgressView.stopSpeaking();
        clearKedaWakeUp();
    }

    private void stopAllVoice() {
        Csjlogger.debug("stopAllVoice");
        expressionIntent.putExtra(Constant.Expression.EXTRA_EXPRESSION_FACE,
                Constant.Expression.EXPRESSION_NORMAL);
        localBroadcastManager.sendBroadcast(expressionIntent);

        mSpeechUnderstander.stopUnderstanding();
        mSpeechUnderstander.cancel();
        // 语音合成对象
        mSpeechSynthesizer.stopSpeaking();
        mProgressView.stopSpeaking();
        clearKedaWakeUp();
    }

    private void setupVoiceParam() {
        Csjlogger.debug("setup understand");
        mSpeechUnderstander.setParameter(SpeechConstant.PARAMS, null);
        // 设置语言
        mSpeechUnderstander.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mSpeechUnderstander.setParameter(SpeechConstant.ACCENT, "mandarin");
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mSpeechUnderstander.setParameter(SpeechConstant.VAD_BOS, "10000");
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mSpeechUnderstander.setParameter(SpeechConstant.VAD_EOS, "3000");

        mSpeechUnderstander.setParameter(SpeechConstant.ASR_PTT, "0");
        mSpeechUnderstander.setParameter(SpeechConstant.SAMPLE_RATE, "16000");

        Csjlogger.debug("setup Synthesizer");
        mSpeechSynthesizer.setParameter(SpeechConstant.PARAMS, null);
        //设置云端合成引擎
        mSpeechSynthesizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        // 设置在线合成发音人:xiaoyan
        mSpeechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, "nannan");
        //设置合成语速:50
        mSpeechSynthesizer.setParameter(SpeechConstant.SPEED, "70");
        //设置合成音调:50
        mSpeechSynthesizer.setParameter(SpeechConstant.PITCH, "70");
        //设置合成音量:50
        mSpeechSynthesizer.setParameter(SpeechConstant.VOLUME, "80");
    }

    private void startUnderstand() {
        Csjlogger.debug("startUnderstand");
//        long time = System.currentTimeMillis();//long now = android.os.SystemClock.uptimeMillis();
//        mSpeechUnderstander.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
//        mSpeechUnderstander.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/xue/xiaoxue" + time + ".wav");
        mSpeechUnderstander.startUnderstanding(mSpeechUnderstanderListener);
    }

    /**
     * @Description: 唤醒，需要播放一段俏皮的话，播放完成之后进行语音识别
     * @author Administrator
     * @time 2016/8/9 0009 下午 1:32
     */
    private void wakeupSpeak(String text) {
        mSpeechSynthesizer.startSpeaking(text, new SynthesizerListener() {
            @Override
            public void onSpeakBegin() {
                expressionIntent.putExtra(Constant.Expression.EXTRA_EXPRESSION_FACE, Constant.Expression.EXPRESSION_SPEAK);
                localBroadcastManager.sendBroadcast(expressionIntent);
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
                isVoiceBusy = false;
                expressionIntent.putExtra(Constant.Expression.EXTRA_EXPRESSION_FACE, Constant.Expression.EXPRESSION_NORMAL);
                localBroadcastManager.sendBroadcast(expressionIntent);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        contiueListening();
                    }
                }, 200);
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        });
    }


    /**
     * @Description: 摸头，需要播放一段俏皮的话，播放完成之后进行语音识别
     * @author Administrator
     * @time 2016/8/9 0009 下午 1:32
     */
    private void touchSpeak(String text) {
        isVoiceBusy = true;
        mSpeechSynthesizer.startSpeaking(text, new SynthesizerListener() {
            @Override
            public void onSpeakBegin() {
                expressionIntent.putExtra(Constant.Expression.EXTRA_EXPRESSION_FACE, Constant.Expression.EXPRESSION_SPEAK);
                localBroadcastManager.sendBroadcast(expressionIntent);

                isVoiceBusy = true;
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
                isVoiceBusy = false;
                expressionIntent.putExtra(Constant.Expression.EXTRA_EXPRESSION_FACE, Constant.Expression.EXPRESSION_NORMAL);
                localBroadcastManager.sendBroadcast(expressionIntent);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        contiueListening();
                    }
                }, 200);
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        });
    }

    /**
     * 语义理解回调。
     */
    private SpeechUnderstanderListener mSpeechUnderstanderListener = new SpeechUnderstanderListener() {

        @Override
        public void onResult(final UnderstanderResult result) {
            if (null != result) {
//                Csjlogger.debug(TAG, result.getResultString());
                // 显示
                String text = result.getResultString();
                Csjlogger.debug(text);
                if (!TextUtils.isEmpty(text)) {
                    voiceParser.parserVoice(text, SpeechActivityBack.this);
                }
            } else {
                CSJToast.showToast(SpeechActivityBack.this, "识别结果不正确。", 2000);
            }
        }

        @Override
        public void onVolumeChanged(int i, byte[] b) {
            CSJToast.showToast(SpeechActivityBack.this, "音量：" + i, 2000);
        }

        @Override
        public void onEndOfSpeech() {
            isVoiceBusy = false;
        }

        @Override
        public void onBeginOfSpeech() {
            isVoiceBusy = true;
        }

        @Override
        public void onError(SpeechError error) {
            if (error.getErrorCode() == 10118) {
//                startSynthesize("小雪没有听清");

                mProgressView.stopSpeaking();

                if (listenTimes < 0) {
                    contiueListening();
                    CSJToast.showToast(SpeechActivityBack.this, "没有听到东西， 继续识别");
                    listenTimes++;
                } else {
                    listenTimes = 0;
                }
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };

    private void startSynthesize(String text) {
        if (text.contains("angry")) {
            expressionIntent.putExtra(Constant.Expression.EXTRA_EXPRESSION_FACE, Constant.Expression.EXPRESSION_ANGRY);
        } else if (text.contains("happy")) {
            expressionIntent.putExtra(Constant.Expression.EXTRA_EXPRESSION_FACE, Constant.Expression.EXPRESSION_HAPPY);
        } else {
            expressionIntent.putExtra(Constant.Expression.EXTRA_EXPRESSION_FACE, Constant.Expression.EXPRESSION_SPEAK);
        }
        localBroadcastManager.sendBroadcast(expressionIntent);
        Csjlogger.debug(text);
//        isVoiceBusy = true;
        // 开始合成流程
        mSpeechSynthesizer.startSpeaking(text, mTtsListener);
    }

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            Csjlogger.debug("开始播放");
            expressionIntent.putExtra(Constant.Expression.EXTRA_EXPRESSION_FACE, Constant.Expression.EXPRESSION_SPEAK);
            localBroadcastManager.sendBroadcast(expressionIntent);
            //            CSJToast.showToast(SpeechActivity.this, "开始播放", 2000);
        }

        @Override
        public void onSpeakPaused() {
            CSJToast.showToast(SpeechActivityBack.this, "暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            CSJToast.showToast(SpeechActivityBack.this, "继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
        }

        @Override
        public void onCompleted(SpeechError error) {
            expressionIntent.putExtra(Constant.Expression.EXTRA_EXPRESSION_FACE, Constant.Expression.EXPRESSION_NORMAL);
            localBroadcastManager.sendBroadcast(expressionIntent);
            if (error == null) {
                Csjlogger.debug("播放完成");
            } else {
                Csjlogger.debug(error.getPlainDescription(true));
            }

            clearKedaWakeUp();

            mProgressView.stopSpeaking();
            localBroadcastManager.sendBroadcast(new Intent(Constant.ACTION_SPEACH_END));
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };

    private void clearKedaWakeUp() {
//        snowBotAction.reset();
    }

    public void closeSpeech(View view) {
        onBackPressed();
    }

    @Override
    protected void onDestroy() {
        stopAllVoice();
//        localBroadcastManager.unregisterReceiver(touchReceiver);
        localBroadcastManager.unregisterReceiver(wakeupReceiver);
        super.onDestroy();
    }

    @Override
    public void paserAndSay(String sayString) {
        startSynthesize(sayString);
    }

    @Override
    public void parserWeather(String sayString, String url) {
        startSynthesize(sayString);
        Intent intent = new Intent(this, WebActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("autoBack", true);
        startActivity(intent);
    }

    @Override
    public void parserWebService(String sayString, String url) {
        startSynthesize(sayString);
        Intent intent = new Intent(this, WebActivity.class);
        intent.putExtra("url", url);
        startActivity(intent);
        Csjlogger.debug(url);
    }

    @Override
    public void parserMusic(boolean success, String url, String singer, String name) {
        Csjlogger.debug(url);
        isVoiceBusy = true;
        if (success) {
            mSpeechSynthesizer.startSpeaking("小雪这就为您播放" + singer + "的 " + name, new SynthesizerListener() {
                @Override
                public void onSpeakBegin() {
                    expressionIntent.putExtra(Constant.Expression.EXTRA_EXPRESSION_FACE, Constant.Expression.EXPRESSION_SPEAK);
                    localBroadcastManager.sendBroadcast(expressionIntent);
                    isVoiceBusy = true;
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
                    stopAllVoice();
                    isVoiceBusy = false;
                    expressionIntent.putExtra(Constant.Expression.EXTRA_EXPRESSION_FACE, Constant.Expression.EXPRESSION_NORMAL);
                    localBroadcastManager.sendBroadcast(expressionIntent);
                    localBroadcastManager.sendBroadcast(new Intent(Constant.ACTION_MUSIC_READY_BEGIN));
                }

                @Override
                public void onEvent(int i, int i1, int i2, Bundle bundle) {

                }
            });

            Intent intent = new Intent(this, MusicActivity.class);
            intent.putExtra("url", url);
            intent.putExtra("singer", singer);
            intent.putExtra("name", name);
            startActivity(intent);
        } else {
            startSynthesize("小雪没有为您找到" + singer + "的 " + name);
        }
    }

    @Override
    public void startActivity(Intent intent) {
        isVoiceBusy = true;
        super.startActivity(intent);
    }

    @Override
    protected void onResume() {
        isVoiceBusy = false;
        super.onResume();
    }

    @Override
    public void parserError(int errorCode) {
        switch (errorCode) {
            case 1:
                stopAllVoice();
                break;
            case 2:
                stopAllVoice();
                break;
            case 3:
                stopAllVoice();
                break;
            case 4:
                startSynthesize(errorTalk[new Random().nextInt(errorTalk.length)]);
                break;
            case 6:
                stopAllVoice();
                // TODO json 解析错误
                break;
            default:
                break;
        }
    }

    @Override
    public void parserSchedule(String sayString) {
        startSynthesize(sayString);
    }

    @Override
    public void parserBaike(String sayString) {
        startSynthesize(sayString);
    }

    @Override
    public void parseMap(String sayString) {
        startSynthesize(sayString);
    }

    @Override
    public void parseFlower(String sayString, String url) {
        startSynthesize(sayString);
        Intent intent = new Intent(this, WebActivity.class);
        intent.putExtra("url", url);
        startActivity(intent);
        Csjlogger.debug(url);
    }


    public class MyThread implements Runnable {
        MoveDirection direction;

        public MyThread(MoveDirection direction) {
            this.direction = direction;
        }

        /**
         * Starts executing the active part of the class' code. This method is
         * called when a thread is started that has been created with a class which
         * implements {@code Runnable}.
         */
        @Override
        public void run() {
            moveServer.moveBy(direction);
        }
    }

    @Override
    public void parseMoveAction(int action) {
        MoveDirection direction = MoveDirection.FORWARD;
        String sayString = "小雪来辣";
        switch (action) {
            case 5:
//                snowBotAction.swingDoubleArm((byte) 0x03);
//                moveServer.turnRound(MoveDirection.TURN_LEFT, 1);
                startActivity(new Intent(this, DacneActivity.class));
                return;
            case 0:
                return;
            case 2:
                direction = MoveDirection.BACKWARD;
                sayString = "小雪向后退辣";
                break;
            case 3:
                direction = MoveDirection.TURN_LEFT;
                break;
            case 4:
                direction = MoveDirection.TURN_RIGHT;
                break;
            default:
                break;
        }

        startSynthesize(sayString);
        new Thread(new MyThread(direction)).start();

    }

    @Override
    public void parseStock(String sayString, String url) {
        startSynthesize(sayString);
        Intent intent = new Intent(this, WebActivity.class);
        intent.putExtra("url", url);
        startActivity(intent);
        Csjlogger.debug(url);
    }

    @Override
    public void paresApp(String sayString) {
        Csjlogger.debug("Speech activity app");
        startSynthesize(sayString);
        Csjlogger.debug("拍照");
    }
}
