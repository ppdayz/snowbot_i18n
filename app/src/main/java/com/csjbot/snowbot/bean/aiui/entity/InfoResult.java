package com.csjbot.snowbot.bean.aiui.entity;

import android.content.Context;

import com.csjbot.csjbase.event.IBus;

import org.json.JSONObject;

/**
 * 信息类语义结果，包括天气、火车和航班。
 *
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年8月18日 上午10:43:17
 */
public class InfoResult extends SemanticResult {

    public InfoResult(String service, JSONObject json) {
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
