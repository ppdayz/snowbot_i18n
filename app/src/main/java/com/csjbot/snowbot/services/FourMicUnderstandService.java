package com.csjbot.snowbot.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.baidu.duersdk.DuerSDK;
import com.baidu.duersdk.DuerSDKFactory;
import com.baidu.duersdk.datas.DuerMessage;
import com.baidu.duersdk.message.IReceiveMessageListener;
import com.baidu.duersdk.message.ISendMessageFinishListener;
import com.baidu.duersdk.message.SendMessageData;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.bean.aiui.DuerBean;
import com.csjbot.snowbot_rogue.app.CsjSpeechSynthesizer;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SynthesizerListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import dou.utils.StringUtils;

public class FourMicUnderstandService extends Service {
    private CsjSpeechSynthesizer mSpeechSynthesizer;
    // 语音听写对象
    private SpeechRecognizer mIat;
    //    private Handler mHandler = new Handler();
    private EventBus eventBus;
    private boolean isWakeup = false;

    private void initDuer() {
        DuerSDK duerSDK = DuerSDKFactory.getDuerSDK();

        //测试appid,appkey
        String appid = "dm5271A8CE3E098F39";
        String appkey = "F351C0E11CF2813EF80F0C92D1FC0D41";

        //初始化sdk
        duerSDK.addSDKErrorLisener(new DuerSDK.SDKErrorLisner() {
            @Override
            public void onError(int i) {
                Csjlogger.warn("error code = {}", i);
            }
        });
        duerSDK.initSDK(this.getApplication(), appid, appkey);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initDuer();

        DuerSDKFactory.getDuerSDK().getMessageEngine().setReceiveMessageListener(messageListener);

        eventBus = EventBus.getDefault();
        eventBus.register(this);
        mIat = SpeechRecognizer.createRecognizer(FourMicUnderstandService.this.getApplicationContext(), mInitListener);

        mSpeechSynthesizer = CsjSpeechSynthesizer.createSynthesizer(this.getApplicationContext(), new InitListener() {
            @Override
            public void onInit(int resault) {
                if (resault == 0) {
                    CsjSpeechSynthesizer.getSynthesizer().startSpeaking("初始化语音成功", null);
                }
                Csjlogger.info("init resault " + resault);

            }
        });

        Csjlogger.debug("onCreate");
//        setParam();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressLint("unused")
    public void startUnderstand( ) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                int ret = mSpeechUnderstander.startUnderstanding(mSpeechUnderstanderListener);
//                Csjlogger.info("ret = {}", ret);
//            }
//        }).start();

        if (!isWakeup) {
            int ret = mIat.startListening(mRecognizerListener);
            if (ret == 0) {
                isWakeup = true;
            }
            Csjlogger.info("ret = {}", ret);
        } else {
            Csjlogger.warn("已经被唤醒了");
        }

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                // 设置音频来源为外部文件
//                mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
//                int ret = mIat.startListening(mRecognizerListener);
//
//                byte[] audioData = FucUtil.readAudioFile(FourMicUnderstandService.this, "iattest.wav");
//
//                if (null != audioData) {
//                    Csjlogger.debug("开始读取音频流");
//                    // 一次（也可以分多次）写入音频文件数据，数据格式必须是采样率为8KHz或16KHz（本地识别只支持16K采样率，云端都支持），位长16bit，单声道的wav或者pcm
//                    // 写入8KHz采样的音频时，必须先调用setParameter(SpeechConstant.SAMPLE_RATE, "8000")设置正确的采样率
//                    // 注：当音频过长，静音部分时长超过VAD_EOS将导致静音后面部分不能识别
//                    mIat.writeAudio(audioData, 0, audioData.length);
//                    mIat.stopListening();
//                } else {
//                    mIat.cancel();
//                    Csjlogger.debug("读取音频流失败");
//                }
//                Csjlogger.info("ret = {}", ret);
//            }
//        }).start();
    }

    public String parseIatResult(String json) {
        StringBuilder ret = new StringBuilder();
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);

            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                // 转写结果词，默认使用第一个结果
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                JSONObject obj = items.getJSONObject(0);
                ret.append(obj.getString("w"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret.toString();
    }

    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            Csjlogger.debug("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            Csjlogger.debug(error.getPlainDescription(true));

            if (error.getErrorCode() == 20001) {
                mSpeechSynthesizer.startSpeaking(error.getPlainDescription(true), null);
            }

            isWakeup = false;
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            if (isWakeup) {
                int ret = mIat.startListening(mRecognizerListener);
                Csjlogger.info("ret = {}", ret);
            }
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String text = parseIatResult(results.getResultString());

            if (text.contains("退下吧") || text.contains("跪安吧")) {
                isWakeup = false;
                Csjlogger.debug("sleep");
                return;
            }

            Csjlogger.info(text);
            sendDuerMessage(text);
            if (isLast) {
                // TODO 最后的结果

            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
//            Csjlogger.debug("当前正在说话，音量大小：" + volume);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
//             以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
//             若使用本地能力，会话id为null
            if (SpeechEvent.EVENT_SESSION_ID == eventType) {
                String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
                Csjlogger.error("RecognizerListener ， sid is {} ", sid);
//            		Log.d(TAG, "session id =" + sid);
            }
        }
    };

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                Csjlogger.error("初始化失败，错误码：" + code);
            } else {
                Csjlogger.warn("初始化成功 init() code = " + code);
                setIatParam();
            }
        }
    };

    private void setIatParam() {
        // 引擎类型
        String mEngineType = SpeechConstant.TYPE_CLOUD;
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        // 设置语言
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号，默认：1（有标点）
        mIat.setParameter(SpeechConstant.ASR_PTT, "0");
    }

    private void sendDuerMessage(String duerText) {
//        DuerSDKFactory.getDuerSDK().getMessageEngine().setReceiveMessageListener(messageListener);
        if (!TextUtils.isEmpty(duerText)) {

            SendMessageData sendMessageData = new SendMessageData();
            //需要查下的 query
            sendMessageData.setQuery(duerText);

            // TODO: 2017/07/20 0020 这里要换成真正的地理位置
            sendMessageData.setLocalSystemName("wgs84");
            //经度
            sendMessageData.setLocalLongitude(116.388171f);
            //纬度
            sendMessageData.setLocalLatitude(39.931535f);

            DuerSDKFactory.getDuerSDK().getMessageEngine().sendMessage(sendMessageData, new ISendMessageFinishListener() {
                @Override
                public void messageSendStatus(MSG_SENDSTATUS msg_sendstatus, DuerMessage duerMessage, JSONObject jsonObject) {
                    try {
                        if (msg_sendstatus == ISendMessageFinishListener.MSG_SENDSTATUS.MSG_SENDFAILURE) {
                            Csjlogger.warn("Duer MSG_SENDFAILURE");
                        }
                    } catch (Exception e) {
                        Csjlogger.error(e);
                    }
                }
            });
        }
    }

    SynthesizerListener speechSynthesizerListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {

        }

        @Override
        public void onBufferProgress(int i, int i1, int i2, String s) {

        }

        @Override
        public void onSpeakPaused() {
            mSpeechSynthesizer.resumeSpeaking();
        }

        @Override
        public void onSpeakResumed() {

        }

        @Override
        public void onSpeakProgress(int i, int i1, int i2) {

        }

        @Override
        public void onCompleted(SpeechError speechError) {

        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    final IReceiveMessageListener messageListener = new IReceiveMessageListener() {

        @Override
        public void messageReceive(String megSourceString) {
            if (!TextUtils.isEmpty(megSourceString)) {
                DuerBean duerBean = JSON.parseObject(megSourceString, DuerBean.class);
                String answer = duerBean.getResult().getSpeech().getContent();
                if (StringUtils.isEmpty(answer)) {
                    answer = "对不起，我正在学习，请问点别的吧";
                }

                if (!answer.contains("为你找到")) {
                     mSpeechSynthesizer.startSpeaking(answer, speechSynthesizerListener);
                    Csjlogger.debug(answer);
                }

                Csjlogger.debug(megSourceString);
            }
        }
    };

    @Subscribe
    public void wakeup(EventWakeup wakeup) {
        int angle = wakeup.getAngle();
        if (angle >= 0 && angle <= 360) {
            if (!isWakeup) {
                int ret = mIat.startListening(mRecognizerListener);
                if (ret == 0) {
                    isWakeup = true;
                }
                Csjlogger.info("ret = {}", ret);
            } else {
                Csjlogger.warn("已经被唤醒了");
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
