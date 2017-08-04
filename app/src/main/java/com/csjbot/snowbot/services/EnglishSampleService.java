package com.csjbot.snowbot.services;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.android.core.util.SharedUtil;
import com.csjbot.csjbase.base.CsjBaseService;
import com.csjbot.csjbase.kit.Kits;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.aiui.SpeechActivity;
import com.csjbot.snowbot.bean.Home;
import com.csjbot.snowbot.bean.aiui.ContentBean;
import com.csjbot.snowbot.bean.aiui.SimilarityUtil;
import com.csjbot.snowbot.bean.aiui.entity.CsjSynthesizerListener;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot.utils.SpeechStatus;
import com.csjbot.snowbot_rogue.Events.AIUIEvent;
import com.csjbot.snowbot_rogue.Events.EventsConstants;
import com.csjbot.snowbot_rogue.Events.ExpressionEvent;
import com.csjbot.snowbot_rogue.platform.SnowBotManager;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.iflytek.aiui.uartkit.UARTAgent;
import com.iflytek.aiui.uartkit.constant.AIUIMessage;
import com.iflytek.aiui.uartkit.constant.UARTConstant;
import com.iflytek.aiui.uartkit.entity.AIUIPacket;
import com.iflytek.aiui.uartkit.entity.MsgPacket;
import com.iflytek.aiui.uartkit.entity.WIFIConfPacket;
import com.iflytek.aiui.uartkit.util.PacketBuilder;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SynthesizerListener;
import com.slamtec.slamware.action.MoveDirection;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import dou.utils.StringUtils;

import static com.csjbot.snowbot.services.AIUIService.EVENT_WAKEUP;

public class EnglishSampleService extends CsjBaseService {
    private final static String KEY_ANGLE = "angle";

    private CsjSpeechSynthesizer2 mSpeechSynthesizer;
    // 语音听写对象
    private SpeechRecognizer mIat;
    //    private Handler mHandler = new Handler();
    private boolean isWakeup = false;
    private SnowBotManager snowBotManager = SnowBotManager.getInstance();
    private UARTAgent mAgent;
    private String[] wakeupTalk;
    private boolean isSpeechRecognizerInit = false;


    /**
     * Very important, Without this method, EventBus won't work
     *
     * @return if return true , EventBus works
     */
    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // init res
        wakeupTalk = getResources().getStringArray(R.array.wakeup_array_en);

        // init SpeechRecognizer
        mIat = SpeechRecognizer.createRecognizer(EnglishSampleService.this.getApplicationContext(), mInitListener);

        // init wake up listener
        mAgent = UARTAgent.createAgent(this, "/dev/ttyS4", 115200, event -> {
            switch (event.eventType) {
                case UARTConstant.EVENT_INIT_SUCCESS:
                    Csjlogger.info("AIUI init success");
                    mAgent.sendMessage(PacketBuilder.obtainWIFIStatusReqPacket());
                    break;
                case UARTConstant.EVENT_INIT_FAILED:
                    Csjlogger.error("Init UART Failed");
                    break;
                case UARTConstant.EVENT_MSG:
                    MsgPacket recvPacket = (MsgPacket) event.data;
                    processPacket(recvPacket);
                    break;
                case UARTConstant.EVENT_SEND_FAILED:
                    MsgPacket sendPacket = (MsgPacket) event.data;
                    mAgent.sendMessage(sendPacket);
                default:
                    break;
            }
        });

        // init tts
        mSpeechSynthesizer = CsjSpeechSynthesizer2.createSynthesizer(this.getApplicationContext(), resault -> {
            Csjlogger.info("init resault " + resault);
            if (resault == 0) {
//                mSpeechSynthesizer.startSpeaking("初始化语音成功", null);
            }
        });

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (isSpeechRecognizerInit) {
//                    wakeup();
//                } else {
//                    Csjlogger.error("SpeechRecognizer is not init");
//                }
//            }
//        }, 2000);

        Csjlogger.debug("onCreate");
    }


    public void processPacket(MsgPacket packet) {
        switch (packet.getMsgType()) {
            case MsgPacket.AIUI_PACKET_TYPE: {
                Csjlogger.debug(" MsgPacket.AIUI_PACKET_TYPE");
                String content = ((AIUIPacket) packet).content;
                JSONTokener tokener = getJSONTokener(content);

                if (tokener == null) {
                    return;
                }

                try {
                    JSONObject joResult = (JSONObject) tokener.nextValue();
                    String contentJson = joResult.getString("content");
                    ContentBean contentBean = JSON.parseObject(contentJson, ContentBean.class);
                    Csjlogger.debug("ontentBean.getEventType() {}", contentBean.getEventType());

                    switch (contentBean.getEventType()) {
                        case EVENT_WAKEUP:
                            // get wakeup Angle
                            JSONObject wakeInfo = new JSONObject(contentBean.getInfo());
                            int wakeAngle = wakeInfo.getInt(KEY_ANGLE);
                            Csjlogger.debug("wake up angle is {}", wakeAngle);
                            wakeupAndPlay(wakeupTalk[new Random().nextInt(wakeupTalk.length)]);

                            mAgent.sendMessage(PacketBuilder.obtainAIUICtrPacket(AIUIMessage.CMD_RESET_WAKEUP, 0, 0, ""));

                            if (isSpeechRecognizerInit) {
                                wakeup();
                                if (!Kits.Package.isTopActivity(this, "com.csjbot.snowbot.activity.aiui.SpeechActivity")) {
                                    Intent it = new Intent(this, SpeechActivity.class);
                                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(it);
                                }
                            } else {
                                Csjlogger.error("SpeechRecognizer is not init");
                            }
                            break;
                        default:
                            break;
                    }
                } catch (JSONException e) {
                    Csjlogger.error(e.getMessage());
                }
            }
            break;
            case MsgPacket.HANDSHAKE_REQ_TYPE:
                Csjlogger.debug("recv HANDSHAKE_REQ_TYPE result" + ((AIUIPacket) packet).content);
                break;
            case MsgPacket.WIFI_CONF_TYPE:
                Csjlogger.debug("recv WIFI_CONF_TYPE result" + ((WIFIConfPacket) packet).status);
                break;
            case MsgPacket.AIUI_CONF_TYPE:
                Csjlogger.debug("recv AIUI_CONF_TYPE result" + ((AIUIPacket) packet).content);
                break;
            case MsgPacket.CTR_PACKET_TYPE:
                Csjlogger.debug("recv CTR_PACKET_TYPE result" + ((AIUIPacket) packet).content);
                break;
            default:
                break;
        }
    }


    /**
     * out wakeup event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressLint("unused")
    public void startUnderstand() {
        wakeup();
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
                if (mSpeechSynthesizer.isSpeaking()) {
                    Csjlogger.warn("is Speaking ...");
                } else {
                    int ret = mIat.startListening(mRecognizerListener);
                    Csjlogger.info("ret = {}", ret);
                }
            }
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String text = parseIatResult(results.getResultString());

            if (text.toUpperCase().contains("BYE")) {
                isWakeup = false;
                Csjlogger.debug("sleep");
                return;
            }

            Csjlogger.info(text);
            postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_SPEAKTEXT_DATA, text));
            postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_SPEAKTEXT_RC, 5));

            if (!parseAction(text)) {
                parseSpeak(text);
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            if (SpeechEvent.EVENT_SESSION_ID == eventType) {
                String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
                Csjlogger.error("RecognizerListener ， sid is {} ", sid);
            }
        }
    };


    String takemeStrings[] = new String[]{
            "let's go to the ",
            "let us go to the ",
            "let's go to ",
            "let us go to ",
            "I want to go to the ",
            "I want to go to ",
            "I want to go ",
            "go to the ",
            "go to ",
            "take me to the ",
            "take me to  "
    };

    List<Home> homeLists = SharedUtil.getListObj(SharedKey.HOMEDATAS, Home.class);

    private boolean parseSpeak(String content) {
        if (StringUtils.isEmpty(content)) {
            return false;
        }
        Map<String, String> customData = new HashMap<>();
        customData.put("what's your name", "My name is Snow");
        customData.put("How old are you", "I was just born");
        customData.put("Where are you from", "I'm from Kunshan");
        customData.put("Do you love italy", " Yes, i love very much");

        for (String key : customData.keySet()) {
            double similarity = SimilarityUtil.sim(key.toUpperCase().replace(" ", ""), content.toUpperCase().replace(" ", ""));
            if (similarity > 0.8) {
                String answer = customData.get(key);
                postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_ANSWERTEXT_DATA, answer));
                mSpeechSynthesizer.startSpeaking(answer, speechSynthesizerListener);
                return true;
            }
        }

        return false;
    }

    private boolean parseAction(String content) {
        if (StringUtils.isEmpty(content)) {
            return false;
        }

        // traversals all take strings
        for (String take : takemeStrings) {
            String takeUpCase = take.toUpperCase();
            String contentUpCase = content.toUpperCase();
            if (contentUpCase.contains(takeUpCase)) {
                // replace action with ""
                // eg: replace "I WANT TO GO TO THE LIVINGROOM" ===> "LIVINGROOM"
                String roomUpCase = contentUpCase.replace(takeUpCase, "");
                Csjlogger.debug("room name is {}", roomUpCase);

                if (homeLists != null) {
                    // traversals all rooms
                    for (Home home : homeLists) {
                        String homeUpCase = home.getHomename().toUpperCase();
                        double similarity = SimilarityUtil.sim(homeUpCase, roomUpCase);
                        if (similarity > 0.8) {
                            Csjlogger.info("{} has [{}] similarity with {} , here we go", homeUpCase, similarity, roomUpCase);
                            snowBotManager.moveTo(home.getmOffsetX(), home.getmOffsetY());
                            return true;
                        }
                    }
                } else {
                    Csjlogger.error("home not found");
                    // TODO: 2017/08/02 0002 speak out not found
                }
            }
        }

        if (content.contains("turn") || content.contains("Turn")) {
            if (content.contains("right") || content.contains("Right")) {
                snowBotManager.turnRound((short) -90);
            } else {
                snowBotManager.turnRound((short) 90);
            }

            Csjlogger.debug("action turn");
            return true;
        }

        if (content.contains("back")) {
            snowBotManager.moveBy(MoveDirection.BACKWARD);
            Csjlogger.debug("action back");
            return true;
        }

        if (content.contains("forward") || content.contains("ahead")
                || content.contains("come") || content.contains("Come")) {
            snowBotManager.moveBy(MoveDirection.FORWARD);
            Csjlogger.debug("action forward");
            return true;
        }

        if (content.contains("") || content.contains("")) {

        }

        return false;
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = code -> {
        if (code != ErrorCode.SUCCESS) {
            Csjlogger.error("Initialization failed, error code：" + code);
        } else {
            Csjlogger.warn("Initialize successfully  init code = " + code);
            isSpeechRecognizerInit = true;
            setIatParam();
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
        mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
        // 设置语言区域

        /**
         *
         * must not set this if is English
         */
//        mIat.setParameter(SpeechConstant.ACCENT, "en_us");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号，默认：1（有标点）
        mIat.setParameter(SpeechConstant.ASR_PTT, "0");
    }


    SynthesizerListener speechSynthesizerListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_SPEAK));
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
            postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_NORMAL));

            if (isWakeup) {
                if (mSpeechSynthesizer.isSpeaking()) {
                    Csjlogger.warn("is Speaking ...");
                } else {
                    int ret = mIat.startListening(mRecognizerListener);
                    Csjlogger.info("ret = {}", ret);
                }
            }
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    private void wakeup() {
        if (!isWakeup) {
            int ret = mIat.startListening(mRecognizerListener);
            if (ret == 0) {
                isWakeup = true;

                if (!Kits.Package.isTopActivity(this, "com.csjbot.snowbot.activity.aiui.SpeechActivity")) {
                    Intent it = new Intent(this, SpeechActivity.class);
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(it);
                }
            }
            Csjlogger.info("ret = {}", ret);
        } else {
            Csjlogger.warn("Is already awakened ");
        }
    }

    @Subscribe
    public void wakeup(EventWakeup wakeup) {
        int angle = wakeup.getAngle();
        if (angle >= 0 && angle <= 360) {
            wakeup();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    //==========================================================//
    @Nullable
    private JSONTokener getJSONTokener(String content) {
        if (content.contains("wifi_status")) {
            return null;
        }

        JSONTokener tokener = new JSONTokener(content);
        return tokener;
    }


    /**
     * @param text
     */
    private void wakeupAndPlay(String text) {
        mSpeechSynthesizer.startSpeaking(text, new CsjSynthesizerListener() {
            @Override
            public void onSpeakBegin() {
                SpeechStatus.getIstance().setSpeakFinished(false);
                postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_SPEAK));
            }

            @Override
            public void onCompleted(SpeechError speechError) {
                SpeechStatus.getIstance().setSpeakFinished(true);
                postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_NORMAL));
            }
        });
    }
}
