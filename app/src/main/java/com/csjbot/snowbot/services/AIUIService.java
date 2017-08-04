package com.csjbot.snowbot.services;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.android.core.util.SharedUtil;
import com.android.core.util.StrUtil;
import com.baidu.duersdk.DuerSDKFactory;
import com.baidu.duersdk.message.IReceiveMessageListener;
import com.baidu.duersdk.message.ISendMessageFinishListener;
import com.baidu.duersdk.message.SendMessageData;
import com.csjbot.csjbase.base.CsjBaseService;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.Broadcast.AiuiBroadcast;
import com.csjbot.snowbot.activity.aiui.CookActivity;
import com.csjbot.snowbot.activity.aiui.CountActivity;
import com.csjbot.snowbot.activity.aiui.DateTimeActivity;
import com.csjbot.snowbot.activity.aiui.JokeActivity;
import com.csjbot.snowbot.activity.aiui.MusicActivity;
import com.csjbot.snowbot.activity.aiui.NewsActivityBean;
import com.csjbot.snowbot.activity.aiui.PoetryActivity;
import com.csjbot.snowbot.activity.aiui.SpeechActivity;
import com.csjbot.snowbot.activity.aiui.StoryActivity;
import com.csjbot.snowbot.activity.aiui.StoryActivityDemo;
import com.csjbot.snowbot.activity.aiui.WeatherAcitvity;
import com.csjbot.snowbot.bean.Home;
import com.csjbot.snowbot.bean.aiui.AIUIErrorCode;
import com.csjbot.snowbot.bean.aiui.AlertingTone.TonePlayer;
import com.csjbot.snowbot.bean.aiui.AsrResultHandler;
import com.csjbot.snowbot.bean.aiui.ContentBean;
import com.csjbot.snowbot.bean.aiui.DuerBean;
import com.csjbot.snowbot.bean.aiui.SemanticResultParser;
import com.csjbot.snowbot.bean.aiui.SimilarityUtil;
import com.csjbot.snowbot.bean.aiui.TopResultBean;
import com.csjbot.snowbot.bean.aiui.TouchWeather;
import com.csjbot.snowbot.bean.aiui.entity.CsjSynthesizerListener;
import com.csjbot.snowbot.bean.aiui.entity.MusicResult;
import com.csjbot.snowbot.bean.aiui.entity.SemanticResult;
import com.csjbot.snowbot.utils.FileUtil;
import com.csjbot.snowbot.utils.LocationUtil.LocationUtil;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot.utils.SpeechStatus;
import com.csjbot.snowbot.utils.UUIDGenerator;
import com.csjbot.snowbot_rogue.Events.AIUIEvent;
import com.csjbot.snowbot_rogue.Events.EventsConstants;
import com.csjbot.snowbot_rogue.Events.ExpressionEvent;
import com.csjbot.snowbot_rogue.R;
import com.csjbot.snowbot_rogue.app.CsjSpeechSynthesizer;
import com.csjbot.snowbot_rogue.platform.SnowBotManager;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.csjbot.snowbot_rogue.utils.SharePreferenceTools;
import com.iflytek.aiui.uartkit.UARTAgent;
import com.iflytek.aiui.uartkit.constant.AIUIMessage;
import com.iflytek.aiui.uartkit.constant.UARTConstant;
import com.iflytek.aiui.uartkit.entity.AIUIPacket;
import com.iflytek.aiui.uartkit.entity.MsgPacket;
import com.iflytek.aiui.uartkit.entity.WIFIConfPacket;
import com.iflytek.aiui.uartkit.util.PacketBuilder;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechError;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android_serialport_api.SerialPort;

public class AIUIService extends CsjBaseService {
    public static final int EVENT_RESULT = 1;
    public static final int EVENT_ERROR = 2;
    public static final int EVENT_STATE = 3;
    public static final int EVENT_WAKEUP = 4;
    public static final int EVENT_SLEEP = 5;
    public static final int EVENT_VAD = 6;

    // 唤醒角度
    private final static String KEY_ANGLE = "angle";
    private String device;

    // 判断是不是错误已经处理了
    private CsjSpeechSynthesizer mSpeechSynthesizer;
    private Handler mHander = new Handler();
    private SnowBotManager snowBot;
    private TouchWeather touchWeather = new TouchWeather();
//    private SimilarityUtil similarityUtil = new SimilarityUtil();

    private boolean mIsHandleError = false;

    // 判断是不是处于唤醒状态
    private boolean mIsWakeUp = false;

    //判断是否是客户定制语句
    private boolean customerTalk = false;

    private String speakText;

    public UARTAgent mAgent;
    private String[] wakeupTalk, errorTalk;
    private SharePreferenceTools wifiContent;
    private AiuiBroadcast aiuiBroadcast = null;

    private final static String GRAMMAR_FILE_PATH = "grammar/grammar.bnf";
    //离线命令词处理
    private AsrResultHandler mAsrHandler;

    private Handler mHandler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        // 注册广播接收器
        aiuiBroadcast = new AiuiBroadcast(ibus);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AiuiBroadcast.RECEIVE_ACTION);
        registerReceiver(aiuiBroadcast, intentFilter);

        // 获取两个资源字符串
        wakeupTalk = getResources().getStringArray(R.array.wakeup_array);
        errorTalk = getResources().getStringArray(R.array.speak_error_array);

        // 新建wifi保存/获取的 SharePreference
        wifiContent = new SharePreferenceTools(this);
        mAsrHandler = new AsrResultHandler(this, ibus);

        // 设置度秘的回调监听
        DuerSDKFactory.getDuerSDK().getMessageEngine().setReceiveMessageListener(messageListener);

        //获取AIUI 串口号
        SharedPreferences tools = getSharedPreferences("aiui_uart", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = tools.edit();

        device = tools.getString("aiui_uart", "/dev/ttyS4");

        if (!device.equalsIgnoreCase("/dev/ttyS4")
                && !device.equalsIgnoreCase("/dev/ttyS3")
                && !device.equalsIgnoreCase("/dev/ttyS2")
                && !device.equalsIgnoreCase("/dev/ttyS1")
                && !device.equalsIgnoreCase("/dev/ttyS0")) {
            device = "/dev/ttyS4";
        }

        try {
            SerialPort fakeSeiral = new SerialPort(new File(device), 115200, 0, "激活AIUI");
            fakeSeiral = null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        //  初始化 AIUI 的 agent
        mHandler.postDelayed(() -> mAgent = UARTAgent.createAgent(AIUIService.this, device, 115200, event -> {
            switch (event.eventType) {
                case UARTConstant.EVENT_INIT_SUCCESS:
                    Csjlogger.info("AIUI Init Success");
                    mAgent.sendMessage(PacketBuilder.obtainWIFIStatusReqPacket());
                    editor.putString("aiui_uart", device);
                    editor.apply();

//                    buildGrammar();
                    buildSpeechType();
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
        }), 1000);


        // 获取 snowbot 机器人实例
        snowBot = SnowBotManager.getInstance();

//        mAgent.sendMessage(PacketBuilder.obtainHandShakeMsg());
        touchWeather();
    }

    private void autoConnectWifi() {
        try {
            String uuid = UUIDGenerator.getInstance().getDeviceUUID();
            String defaultSsid = "SnowBot_" + uuid.substring((uuid.length() - 5));
            String defaultPwd = "";

            UARTAgent agent = UARTAgent.getUARTAgent();
            agent.sendMessage(PacketBuilder.obtainWIFIConfPacket(WIFIConfPacket.WIFIStatus.CONNECTED,
                    wifiContent.getString("wifiType", "WPA").contains("WPA") ? WIFIConfPacket.EncryptMethod.WPA :
                            WIFIConfPacket.EncryptMethod.OPEN, wifiContent.getString("ssid", defaultSsid), wifiContent.getString("password", defaultPwd)));
        } catch (NullPointerException e) {
            Csjlogger.debug("UARTAgent is Null");
        }
    }

    private void buildGrammar() {
        String grammar = FileUtil.readAssetsFile(this, GRAMMAR_FILE_PATH);
        mAgent.sendMessage(PacketBuilder.obtainAIUICtrPacket(AIUIMessage.CMD_BUILD_GRAMMAR, 0, 0, grammar));

//        mHander.post(wifiRunnable);
    }

    private void buildSpeechType() {
        String speechType = "{\"speech\":{\"data_source\":\"sdk\", \"wakeup_mode\":\"cae\", \"intent_engine_type\":\"cloud\"}}";
        mAgent.sendMessage(PacketBuilder.obtainAIUICtrPacket(AIUIMessage.CMD_SET_PARAMS, 0, 0, speechType));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSpeechSynthesizer = CsjSpeechSynthesizer.createSynthesizer(this, new InitListener() {
            @Override
            public void onInit(int resault) {
                Csjlogger.info("init resault " + resault);
            }
        });

        startGuGuo();
        return super.onStartCommand(intent, flags, startId);
    }

    /* 启动谷果service*/
    private void startGuGuo() {
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName("com.guogu.testvoicebox", "com.guogu.testvoicebox.GuGuoService");
        intent.setComponent(componentName);
        startService(intent);
    }

    private void processPacket(MsgPacket packet) {
        switch (packet.getMsgType()) {
            case MsgPacket.AIUI_PACKET_TYPE:
                processAIUIPacket(((AIUIPacket) packet).content);
//                Csjlogger.debug( "recv aiui result" + ((AIUIPacket) packet).content);
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

    private void speechAndHandle(JSONObject jsonObject, SemanticResult semanticResult) throws JSONException {
        String service = semanticResult.getService();
        if (!Constant.ISVOLCHANGE && !service.equals("cmd")) {
            postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_EVENT_SERVICE_COMMING, service));
            postEvent(new AIUIEvent(EventsConstants.AIUIEvents.MUSICX_NEWSBEAN_RESTART));
            postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_EVENT_SUB_NLP));
        }

        if (!TextUtils.isEmpty(service)) {
            SemanticResult.ServiceType serviceType = SemanticResult.getServiceType(service);
            switch (serviceType) {
                case WEATHER:
                    mSpeechSynthesizer.stopSpeaking();
                    if (semanticResult.getAnswerText() != null) {
                        Intent weaIntent = new Intent(this, WeatherAcitvity.class);
                        weaIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        weaIntent.putExtra("data", jsonObject.getString("data"));
                        weaIntent.putExtra("semantic", jsonObject.getString("semantic"));
                        weaIntent.putExtra("serviceType", service);
                        startActivity(weaIntent);
                        AIUIVoice(semanticResult.getAnswerText());
                    }
                    break;
                case TRAIN:
                case FLIGHT:
                case TVCHANNEL:
                case EPG:
                case PM25:
                case TELEPHONE:
                case RADIO:
                case PATTERN:
                case CHAT:
                case NUMBER_MASTER:  //数字大师
                    AIUIVoice(semanticResult.getAnswerText());
                    break;
                case SMARTHOME:
                    if (SharedUtil.getPreferInt(SharedKey.AIUISERVICETYPE, 0) == 1) {
                        return;
                    } else {
                        AIUIVoice(semanticResult.getAnswerText());
                    }
                    break;
                case DATETIME:
                    mSpeechSynthesizer.stopSpeaking();
                    if (speakText != null && semanticResult.getAnswerText() != null) {
                        Intent intent = new Intent(this, DateTimeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("answerText", semanticResult.getAnswerText());
                        intent.putExtra("serviceType", service);
                        startActivity(intent);
                    }
                    AIUIVoice(semanticResult.getAnswerText());
                    break;
                case JOKE:
                    if (!jsonObject.getJSONObject("data").isNull("result")) {
                        Intent intent = new Intent(this, JokeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("data", jsonObject.getString("data"));
                        intent.putExtra("serviceType", service);
                        startActivity(intent);
                    }
                    break;
                case CALC:
                    if (speakText != null && semanticResult.getAnswerText() != null) {
                        Intent intent = new Intent(this, CountActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("speakText", speakText);
                        intent.putExtra("answerText", semanticResult.getAnswerText());
                        intent.putExtra("serviceType", service);
                        startActivity(intent);
                    }
                    break;
                case POETRY:
                    if (speakText != null && semanticResult.getAnswerText() != null) {
                        Intent intent = new Intent(this, PoetryActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("speakText", speakText);
                        intent.putExtra("answerText", semanticResult.getAnswerText());
                        intent.putExtra("serviceType", service);
                        startActivity(intent);
                    }
                    break;
                case MUSICX:
                    MusicResult musicResult = (MusicResult) semanticResult;
                    if (!musicResult.isSleepType() && !Constant.ISVOLCHANGE && !jsonObject.getJSONObject("data").isNull("result")) {
                        if (!jsonObject.getString("data").contains("\"result\":[]")) {
                            Intent intent = new Intent(this, MusicActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("data", jsonObject.getString("data"));
                            intent.putExtra("serviceType", service);
                            startActivity(intent);
                        }
//                        Csjlogger.info(jsonObject.getString("data"));
                    } else if (Constant.ISVOLCHANGE) {
                        Constant.ISVOLCHANGE = false;
                    }
                    break;
                case COOKBOOK:
                    if (!jsonObject.isNull("data")) {
                        if (jsonObject.has("error")) {
                            JSONObject error = new JSONObject(jsonObject.getJSONObject("data").getString("message"));
                            String errorMessage = error.getString("message");
                            AIUIVoice(errorMessage);
                        } else if (!jsonObject.getJSONObject("data").isNull("result")) {
                            Intent intent = new Intent(this, CookActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("data", jsonObject.getString("data"));
                            intent.putExtra("serviceType", service);
                            startActivity(intent);
                        }
                    }
                    break;
                case TV_SMARTH:
                    break;
                case STORY:
                    if (jsonObject.getString("data").contains("乌鸦喝水") && !jsonObject.getJSONObject("data").isNull("result")) {
                        postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_EVENT_OTHER_CMD));
                        Intent intent = new Intent(this, StoryActivityDemo.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("storyImage", R.drawable.story_crow);
                        intent.putExtra("data", jsonObject.getString("data"));
                        startActivity(intent);
                    } else if (!jsonObject.getJSONObject("data").isNull("result")) {
                        Intent intent = new Intent(this, StoryActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("data", jsonObject.getString("data"));
                        intent.putExtra("serviceType", service);
                        startActivity(intent);
                    } else {
                        otherVoice("小雪未找到相关内容，请换个故事听一下");
                    }
                    break;
                case CMD:
                    break;
                case NEWS:
                    if (!jsonObject.getJSONObject("data").isNull("result")) {
                        Intent intent = new Intent(this, NewsActivityBean.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("data", jsonObject.getString("data"));
                        intent.putExtra("serviceType", service);
                        startActivity(intent);
                    } else {
                        AIUIVoice(semanticResult.getAnswerText());
                    }
                    break;
                case ROBOTACTION:
                    break;
                case OPENQA:
                    break;
                default:
                    break;
            }
        }
    }

    private void stopAllVoice() {
        postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_NORMAL));
        mSpeechSynthesizer.stopSpeaking();
    }

    private void handleResult(JSONObject intentJSObj) throws JSONException {
        SemanticResult semanticResult = SemanticResultParser.parse(intentJSObj);
        // TODO 这里打印 semanticResult.getAnswerText();
        if (null != semanticResult) {
            semanticResult.handleResult(ibus, this);
            speechAndHandle(intentJSObj, semanticResult);
        }
    }

    private void otherAction(String content) {
        boolean needInterrupt = false;
        String homeName;
        if (content.contains("带我去") || (content.contains("我想去"))) {
            boolean homeExist = false;
            if (content.contains("带我去")) {
                homeName = content.replace("带我去", "");
            } else {
                homeName = content.replace("我想去", "");
            }

            // deal with result(stt)
            List<Home> homeLists = SharedUtil.getListObj(SharedKey.HOMEDATAS, Home.class);
            if (homeLists != null) {
                for (Home home : homeLists) {
                    if (home.getHomename().equalsIgnoreCase(homeName)) {
                        otherVoice("小雪这就带您去" + homeName);
                        snowBot.moveTo(home.getmOffsetX(), home.getmOffsetY());
                        homeExist = true;
                        break;
                    }
                }

                if (!homeExist) {
                    otherVoice("该房间不存在，请确定房间名称是否正确");
                }
            } else {
                otherVoice("未设置房间，请先设置房间");
            }
            needInterrupt = true;
        } else {
//            postAsynHttp(speakText);
            if (activityIsOnTop(this, "com.csjbot.snowbot.activity.aiui.SpeechActivity")) {
                String str = errorTalk[new Random().nextInt(errorTalk.length)];
//                otherVoice(str);
            }
        }

        sendDuerMessage(content);

        if (needInterrupt) {
            postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_EVENT_OTHER_CMD));
            postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_EVENT_SUB_NLP));
        }
    }

    private void wakeupReceiver(Context context) {

        if (!activityIsOnTop(context, "com.csjbot.snowbot.activity.aiui.SpeechActivity")) {
            Intent it = new Intent(context, SpeechActivity.class);
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(it);
        }
    }

    private boolean activityIsOnTop(Context context, String activityName) {
        if (context == null || TextUtils.isEmpty(activityName)) {
            return false;
        }

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (activityName.equals(cpn.getClassName())) {
                return true;
            }
        }

        return false;
    }

    private void processAIUIPacket(String content) {
        if (StrUtil.isNotBlank(content)) {
            sendBroadcast(content);
        }

        if (SharedUtil.getPreferInt(SharedKey.AIUISERVICESWITCH, 1) == 0
                || !SpeechStatus.getIstance().isAiuiResponse()) {
//            postEvent(new HWTestEvent("AIUI 数据 " + StringUtils.formatJson(content)));
            return;
        }

        if (content.contains("wifi_status")) {
            postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_EVENT_WIFI_GET, content));
            return;
        }

        JSONTokener tokener = new JSONTokener(content);
        try {
            JSONObject joResult = (JSONObject) tokener.nextValue();
//            JSONObject joResult = new JSONObject(tokener);

            String contentJson = joResult.getString("content");
            ContentBean contentBean = JSON.parseObject(contentJson, ContentBean.class);

            switch (contentBean.getEventType()) {
                case EVENT_WAKEUP:
                    try {
                        // 获得唤醒角度
                        stopAllVoice();
                        wakeupAndPlay(wakeupTalk[new Random().nextInt(wakeupTalk.length)]);
                        // Fixme PUYZ ADD
                        postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_EVENT_WAKEUP));
                        wakeupReceiver(this);
                        JSONObject wakeInfo = new JSONObject(contentBean.getInfo());
                        int wakeAngle = wakeInfo.getInt(KEY_ANGLE);
                        postEvent(new EventWakeup(0, wakeAngle));
//                        CsjSpeechSynthesizer2.getSynthesizer().startSpeaking("旋转 " + String.valueOf(wakeAngle) + " 度", null);
                        Csjlogger.info("wakeup Angle {}", wakeAngle);
                        mIsHandleError = false;
                        mIsWakeUp = true;
                        snowBot.stopPartol();
                        snowBot.turnRound((short) wakeAngle);
                    } catch (JSONException e) {
                        Csjlogger.error(e.getMessage());
                    }
                    break;
                case EVENT_RESULT:
                    if (!mIsWakeUp) {
                        return;
                    }

                    try {
                        JSONObject bizParamJson = new JSONObject(contentBean.getInfo());
                        JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
                        JSONObject params = data.getJSONObject("params");
                        JSONObject contentObject = data.getJSONArray("content").getJSONObject(0);

                        if (contentObject.has("cnt_id")) {
                            TopResultBean topResultBean = JSON.parseObject(contentBean.getResult(), TopResultBean.class);
                            String sub = params.optString("sub");
                            JSONObject intentObj = new JSONObject(topResultBean.getIntent());
                            if ("nlp".equals(sub)) {
                                int rc = 0;
                                //获取说话的speakText
                                if (intentObj.has("text") && intentObj.getString("text") != null) {
                                    speakText = intentObj.getString("text");
                                    postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_SPEAKTEXT_DATA, speakText));
                                    //获取rc
                                    if (intentObj.has("rc")) {
                                        rc = intentObj.getInt("rc");
                                        postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_SPEAKTEXT_RC, rc));
                                    }
                                    customerTalk = false;
                                    commissionTalk(speakText);
                                    //判断当rc=4,且无其他拓展行为时
                                    if (rc == 4 && !customerTalk) {
                                        otherAction(speakText);
                                    }
//                                    break;
                                }

                                if (customerTalk) {
                                    return;
                                } else {
                                    //获取说话的answerText
                                    if (intentObj.has("answer") && intentObj.getJSONObject("answer").getString("text") != null) {
//                                        String answerText = intentObj.getJSONObject("answer").getString("text");
//                                        postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_ANSWERTEXT_DATA_SPEECH, answerText));
                                    }
                                    handleResult(intentObj);
                                }
                            } else if ("iat".equals(sub)) {
                                // TODO 这里处理语音听写结果 ,打印查看
                            } else if ("asr".equals(sub)) {
                                // 处理离线语法结果
                                mAsrHandler.handleResult(intentObj);
                            }
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    break;
                case EVENT_ERROR:
                    stopAllVoice();
                    int errorCode = contentBean.getArg1();
                    handleError(errorCode);
                    break;
                case EVENT_SLEEP:
                    if (SpeechStatus.getIstance().isSpeakFinished()) {
                        goodByeAndSleep("那我走辣~");
                        postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_EVENT_TIME_OUT_SLEEP));
//                        mAgent.sendMessage(PacketBuilder.obtainAIUICtrPacket(AIUIMessage.CMD_WAKEUP, 0, 0, ""));
                    }
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            Csjlogger.error(e.getMessage());
        }

//        Csjlogger.warn("content " + StringUtils.formatJson(content));
    }

    private void commissionTalk(String speakText) {
        Map<String, String> customData = SharedUtil.getMap("CUSTOMVOICE");
        if (customData != null) {
            for (String key : customData.keySet()) {
                double similarity = SimilarityUtil.sim(SimilarityUtil.getPingYin(key), SimilarityUtil.getPingYin(speakText));
                if (similarity > 0.8) {
                    customerTalk = true;
                    otherVoice(customData.get(key));
                }
//                if (key.equals(speakText)) {
//                    customerTalk = true;
//                    otherVoice(customData.get(key));
//                }
                if (customerTalk) {
                    postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_EVENT_OTHER_CMD));
                    postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_EVENT_SUB_NLP));
                }
            }
        }
    }

    /**
     * 错误处理函数
     *
     * @param errorCode 错误码
     */
    private void handleError(int errorCode) {
        if (mIsHandleError) {
            return;
        }

        mIsWakeUp = false;

        Csjlogger.debug("error = " + errorCode);
        String errorTip = "";
        TonePlayer.stopPlay();

        mIsHandleError = true;
        // 错误提示
        switch (errorCode) {
            case AIUIErrorCode.MSP_ERROR_TIME_OUT:
                errorTip = "网络有点问题我去休息了，请稍后再试！";
                break;
            case AIUIErrorCode.ERROR_NO_NETWORK:
                errorTip = "网络未连接，请连接网络！";
                break;
//            case AIUIErrorCode.MSP_ERROR_NO_RESPONSE_DATA:            // 结果超时
//                errorTip = "没有返回数据，请查看网络";
//                break;
            case AIUIErrorCode.MSP_ERROR_NOT_FOUND:
                errorTip = "场景参数设置出错！";
                break;
            case AIUIErrorCode.ERROR_SERVICE_BINDER_DIED:
                errorTip = "AIUI服务已断开！";
                break;
            case AIUIErrorCode.MSP_ERROR_LMOD_RUNTIME_EXCEPTION:    // 16005

            case AIUIErrorCode.MSP_ERROR_DB_INVALID_APPID:          // appid校验不通过
                errorTip = "appid校验不通过";
                break;
            default:
                errorTip = "出错拉，请联系客服，错误码是" + errorCode;
                break;
        }
        otherVoice(errorTip);
    }

    /**
     * rc=0 时，小雪说话调用此方法
     *
     * @param text
     */
    private void AIUIVoice(String text) {
        postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_ANSWERTEXT_DATA, text));
        mSpeechSynthesizer.startSpeaking(text, new CsjSynthesizerListener() {
            @Override
            public void onSpeakBegin() {
                SpeechStatus.getIstance().setSpeakFinished(false);
                postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_EVENT_VOICE_START));
                postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_SPEAK));
            }

            @Override
            public void onCompleted(SpeechError speechError) {
                SpeechStatus.getIstance().setSpeakFinished(true);
                postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_NORMAL));
                postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_EVENT_VOICE_END));
            }
        });
    }

    /**
     * rc=4 时，小雪说话调用此方法；自定义语音时，调用此方法
     *
     * @param text
     */
    private void otherVoice(String text) {
        postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_ANSWERTEXT_DATA, text));
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
//                mAgent.sendMessage(PacketBuilder.obtainAIUICtrPacket(AIUIMessage.CMD_SET_BEAM, 0, 0, null));
            }
        });
    }

    private void goodByeAndSleep(String text) {
        if (!mIsWakeUp) {
            return;
        }
        mIsWakeUp = false;

        postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_ANSWERTEXT_DATA, text));
        mSpeechSynthesizer.startSpeaking(text, new CsjSynthesizerListener() {
            @Override
            public void onSpeakBegin() {
                SpeechStatus.getIstance().setSpeakFinished(false);
                postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_SPEAK));
            }

            @Override
            public void onCompleted(SpeechError speechError) {
                SpeechStatus.getIstance().setSpeakFinished(true);
                postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_SLEEP));
            }
        });
    }

    private void touchWeather() {
        mHander.post(new Runnable() {
            @Override
            public void run() {
                touchWeather.getWeatherData(null);
                mHander.postDelayed(this, 7200000);
            }
        });
    }

    @Override
    public void onDestroy() {
        if (null != aiuiBroadcast) {
            unregisterReceiver(aiuiBroadcast);
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public boolean onAIUIEvent(AIUIEvent event) {
        switch (event.getTag()) {
            case EventsConstants.AIUIEvents.AIUI_EVENT_FORCE_SLEEP:
                goodByeAndSleep("那我走辣~");
                break;
            case EventsConstants.AIUIEvents.AIUI_EVENT_TOUCH_GET:
                touchWeatherSpeak();
                break;
            case EventsConstants.AIUIEvents.AIUI_EVENT_WIFI_GET:
                try {
                    JSONObject wifiType = new JSONObject((String) event.data);
                    boolean wifiContent = wifiType.getJSONObject("content").getBoolean("connected");
                    if (wifiContent) {
                        // 如果连上了就跳出switch
                        break;
                    }
                } catch (JSONException e) {
                    Csjlogger.error(e);
                }
                autoConnectWifi();

                break;
        }
        return false;
    }

    private void touchWeatherSpeak() {
        touchWeather.getWeatherData(new TouchWeather.WeatherGetListener() {
            @Override
            public void weatherGet(String weather) {
                snowBot.swingDoubleArm((byte) 0x04);
                if (SharedUtil.getPreferBool(SharedKey.WEATHERSWITCH, false) && SpeechStatus.getIstance().isSpeakFinished()) {
                    stopAllVoice();
                    if (StrUtil.isNotBlank(weather)) {
                        otherVoice(weather);
                    }
                }
            }

            @Override
            public void onError() {
                otherVoice("网络有些问题，请稍后再试");
            }
        });
    }

    private void sendBroadcast(String str) {
        if (StrUtil.isBlank(str)) {
            return;
        }
        Intent intent = new Intent();
        intent.setAction(AiuiBroadcast.SEND_ACTION);
        Bundle bundle = new Bundle();
        bundle.putString(SharedKey.AIUICONTENT, str);
        intent.putExtras(bundle);
        sendBroadcast(intent);
    }

    private String[] uncivilizedSpeech = new String[]{"大爷", "劳资", "尼玛", "靠", " 挂 ", "操", " 日", "尼玛", "装逼", "SB", "二逼", "神经病", "你妈", "你妹", "屌", "他妈的", "卧槽", "特么的", "傻逼", "蛋疼"};

    private boolean checkUncivilizedSpeech(String speakText) {
        for (String string : uncivilizedSpeech) {
            if (speakText.contains(string)) {
                return true;
            }
        }

        return false;
    }

    final IReceiveMessageListener messageListener = megSourceString -> {
        DuerBean duerBean = JSON.parseObject(megSourceString, DuerBean.class);

        String answer = duerBean.getResult().getSpeech().getContent();

        if (checkUncivilizedSpeech(answer)) {
            answer = "小雪正在学习，还不会回答！";
        }

        CsjSpeechSynthesizer.getSynthesizer().startSpeaking(answer, null);
        postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_ANSWERTEXT_DATA, answer));
    };

    private void sendDuerMessage(String message) {

        if (!TextUtils.isEmpty(message)) {
            Csjlogger.debug(message);

            SendMessageData sendMessageData = new SendMessageData();
            //需要查下的 query
            sendMessageData.setQuery(message);

            //设置经纬度信息,坐标系名称
            sendMessageData.setLocalSystemName("wgs84");
            //经度
            sendMessageData.setLocalLongitude(LocationUtil.getLongitude().floatValue());
            //纬度
            sendMessageData.setLocalLatitude(LocationUtil.getLatitude().floatValue());

            DuerSDKFactory.getDuerSDK().getMessageEngine().sendMessage(sendMessageData,
                    (status, duerMessage, errorJson) -> {
                        Csjlogger.debug("duerMessage: " + message + " messageSendStatus: " + status
                                + " errorJson:" + errorJson);
                        try {
                            if (status == ISendMessageFinishListener.MSG_SENDSTATUS.MSG_SENDFAILURE) {
                                Csjlogger.warn("Duer MSG_SENDFAILURE");
                            }
                        } catch (Exception e) {
                            Csjlogger.error(e);
                        }
                    });
        } else {
            Csjlogger.debug(message);
            Toast.makeText(getApplicationContext(), "消息不能为空", Toast.LENGTH_SHORT).show();
        }
    }
}
