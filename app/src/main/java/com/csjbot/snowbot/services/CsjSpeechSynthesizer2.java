package com.csjbot.snowbot.services;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.csjbot.csjbase.event.BusFactory;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot_rogue.Events.ExpressionEvent;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

import java.lang.ref.WeakReference;

/**
 * Copyright (c) 2016, SuZhou CsjBot. All Rights Reserved.
 * www.csjbot.com
 * <p>
 * Created by 浦耀宗 at 2016/12/22 0022-21:13.
 * Email: puyz@csjbot.com
 */
public class CsjSpeechSynthesizer2 {
    private static CsjSpeechSynthesizer2 ourInstance;
    // 语音合成对象
    private SpeechSynthesizer mSpeechSynthesizer;

    private CsjSpeechSynthesizer2(Context ctx, InitListener initListener) {
        WeakReference<Context> mContext = new WeakReference<>(ctx.getApplicationContext());
        mSpeechSynthesizer = SpeechSynthesizer.createSynthesizer(mContext.get(), initListener);
        setParams();
    }

    public static CsjSpeechSynthesizer2 createSynthesizer(@NonNull Context ctx, InitListener initListener) {
        if (ourInstance == null) {
            try {
                ourInstance = new CsjSpeechSynthesizer2(ctx, initListener);
            } catch (Exception e) {

            }
        }

        return ourInstance;
    }

    public static CsjSpeechSynthesizer2 getSynthesizer() {
        return ourInstance;
    }

    public void setParams() {
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
        mSpeechSynthesizer.setParameter(SpeechConstant.VOLUME, "100");
    }

    public int startSpeaking(String string, SynthesizerListener synthesizerListener) {

//        if (SharedUtil.getPreferInt(SharedKey.TTSSWITCH,1) == 0){
//            return -1;
//        }
        if (synthesizerListener == null) {
            synthesizerListener = new DefaultSynthesizerListener();
        }

        return mSpeechSynthesizer.startSpeaking(string, synthesizerListener);
    }


    public void setParameter(String var1, String var2) {
        if (null != mSpeechSynthesizer) {
            mSpeechSynthesizer.setParameter(var1, var2);
        }
    }

    public boolean isSpeaking() {
        if (null != mSpeechSynthesizer) {
            return mSpeechSynthesizer.isSpeaking();
        }

        return false;
    }

    public void stopSpeaking() {
        if (null != mSpeechSynthesizer) {
            mSpeechSynthesizer.stopSpeaking();
        }
    }

    public void pauseSpeaking() {
        if (null != mSpeechSynthesizer) {
            mSpeechSynthesizer.pauseSpeaking();
        }
    }

    public void resumeSpeaking() {
        if (null != mSpeechSynthesizer) {
            mSpeechSynthesizer.resumeSpeaking();
        }
    }

    public void destroy() {
        if (null != mSpeechSynthesizer) {
            mSpeechSynthesizer.destroy();
        }
        BusFactory.getBus().unregister(this);
    }

    private class DefaultSynthesizerListener implements SynthesizerListener {
        @Override
        public void onSpeakBegin() {
            BusFactory.getBus().post(new ExpressionEvent(Constant.Expression.EXPRESSION_SPEAK));
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
            BusFactory.getBus().post(new ExpressionEvent(Constant.Expression.EXPRESSION_NORMAL));
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    }
}
