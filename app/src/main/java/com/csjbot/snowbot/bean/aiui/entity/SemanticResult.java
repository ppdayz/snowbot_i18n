package com.csjbot.snowbot.bean.aiui.entity;

import android.content.Context;
import android.text.TextUtils;

import com.csjbot.csjbase.event.IBus;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.services.CsjSpeechSynthesizer2;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * 语义结果抽象类。
 *
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年8月18日 上午10:40:00
 */
public abstract class SemanticResult {
    // 演示的service类型
    public enum ServiceType {
        WEATHER,
        TRAIN,
        FLIGHT,
        MUSICX,                // 音乐播放&控制
        TV_SMARTH,             //智能电视？
        EPG,                   //网络，电子节目指南
        TVCHANNEL,
        NUMBER_MASTER,         // 数字纠错
        CMD,                   //命令控制，包括声音
        CHAT,
        PATTERN,
        PERSONAL_NAME,
        POETRY,
        SMARTHOME,
        OTHER,
        STORY,
        COOKBOOK,
        NEWS,
        JOKE,
        CALC,
        PM25,
        DATETIME,
        TELEPHONE,
        RADIO,
        ROBOTACTION,
        OPENQA
    }

    static HashMap<String, ServiceType> serviceMap = new HashMap<>();

    static {
        serviceMap.put("weather", ServiceType.WEATHER);
        serviceMap.put("train", ServiceType.TRAIN);
        serviceMap.put("flight", ServiceType.FLIGHT);
        serviceMap.put("musicX", ServiceType.MUSICX);
        serviceMap.put("tv_smartH", ServiceType.TV_SMARTH);
        serviceMap.put("epg", ServiceType.EPG);
        serviceMap.put("tvchannel", ServiceType.TVCHANNEL);
        serviceMap.put("numberMaster", ServiceType.NUMBER_MASTER);
        serviceMap.put("cmd", ServiceType.CMD);
        serviceMap.put("chat", ServiceType.CHAT);
        serviceMap.put("pattern", ServiceType.PATTERN);
        serviceMap.put("personalName", ServiceType.PERSONAL_NAME);
        serviceMap.put("poetry", ServiceType.POETRY);
        serviceMap.put("smartHome", ServiceType.SMARTHOME);
        serviceMap.put("story", ServiceType.STORY);
        serviceMap.put("cookbook", ServiceType.COOKBOOK);
        serviceMap.put("news", ServiceType.NEWS);
        serviceMap.put("joke", ServiceType.JOKE);
        serviceMap.put("calc", ServiceType.CALC);
        serviceMap.put("pm25", ServiceType.PM25);
        serviceMap.put("datetime", ServiceType.DATETIME);
        serviceMap.put("telephone", ServiceType.TELEPHONE);
        serviceMap.put("radio", ServiceType.RADIO);
        serviceMap.put("robotAction", ServiceType.ROBOTACTION);
        serviceMap.put("openQA", ServiceType.OPENQA);
    }

    public final static String KEY_TEXT = "text";
    public final static String KEY_ANSWER = "answer";
    protected String service;
    protected String answerText = "";
    protected JSONObject json = null;
    protected Context mContext;

    protected IBus mIbus;

    public static ServiceType getServiceType(String service) {
        ServiceType type = serviceMap.get(service);
        if (null == type) {
            type = ServiceType.OTHER;
        }

        if (-1 != service.lastIndexOf("smartHome")) {
            return ServiceType.SMARTHOME;
        }

        return type;
    }

    public SemanticResult(String service, JSONObject json) {
        this.service = service;
        this.json = json;
    }

    public void postEvent(IBus.IEvent event) {
        if (mIbus != null) {
            Csjlogger.debug("postEvent  " + event.getTag());
            mIbus.post(event);
        }
    }


    public void handleResult(IBus iBus, Context context) {
        mContext = context;
        mIbus = iBus;
        if (null != json) {
            if (json.has(KEY_ANSWER)) {
                // 合成answer中的text字段
                try {
                    JSONObject answer = json.getJSONObject(KEY_ANSWER);
                    String text = answer.getString(KEY_TEXT);
                    answerText = text;
                    if (!TextUtils.isEmpty(text)) {
                        int readDigit = 1;
                        final ServiceType serviceType = getServiceType();

                        if (serviceType == ServiceType.NUMBER_MASTER
                                || serviceType == ServiceType.TRAIN
                                || serviceType == ServiceType.FLIGHT
                                || serviceType == ServiceType.TELEPHONE
                                ) {
                            // 数字纠错、火车、航班业务中的数字分开读
                            readDigit = 2;
                        }

                        CsjSpeechSynthesizer2 tts = CsjSpeechSynthesizer2.getSynthesizer();
                        if (null != tts) {
                            tts.setParameter("rdn", readDigit + "");
                        }

//						AIUIPlayerKitVer player = SemanticResultHandler.getAIUIPlayer();
//						if (null != player) {
//							PlayItem item = new PlayItem(ContentType.TEXT, text, null);
//							List<PlayItem> itemList = new ArrayList<PlayItem>();
//							itemList.add(item);
//
//							player.playItems(itemList, null);
//						}
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected abstract void doAfterTTS();

    /**
     * 获取业务名称
     *
     * @return 文字描述
     */
    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getAnswerText() {
        return answerText;
    }

    /**
     * 获取业务类型
     *
     * @return 便于编程的enum类型，非文字描述，与业务名称一一对应
     */
    public ServiceType getServiceType() {
        return getServiceType(service);
    }

    public JSONObject getJson() {
        return json;
    }

    public void setJson(JSONObject json) {
        this.json = json;
    }

}
