package com.csjbot.snowbot.bean.aiui.entity;

import android.content.Context;

import com.csjbot.csjbase.event.IBus;

import org.json.JSONObject;

/**
 * 家具控制语义结果。
 *
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年6月21日 下午4:30:45
 */
public class SmartHomeResult extends SemanticResult {
    private final static String TAG = "SmartHomeResult";

    public SmartHomeResult(String service, JSONObject json) {
        super(service, json);
    }

    @Override
    public void handleResult(IBus iBus, Context context) {
        super.handleResult(iBus, context);
    }

    @Override
    protected void doAfterTTS() {

    }

}
