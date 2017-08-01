package com.csjbot.snowbot.bean.aiui.entity;

import android.os.Bundle;

import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SynthesizerListener;

/**
 * Copyright (c) 2016, SuZhou CsjBot. All Rights Reserved.
 * www.csjbot.com
 * <p>
 * Created by 浦耀宗 at 2016/12/22 0022-21:38.
 * Email: puyz@csjbot.com
 */

public abstract class CsjSynthesizerListener implements SynthesizerListener {
    @Override
    public abstract void onSpeakBegin();

    @Override
    public abstract void onCompleted(SpeechError speechError);

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
    public void onEvent(int i, int i1, int i2, Bundle bundle) {

    }
}
