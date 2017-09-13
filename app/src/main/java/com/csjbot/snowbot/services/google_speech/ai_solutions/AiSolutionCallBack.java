package com.csjbot.snowbot.services.google_speech.ai_solutions;

/**
 * Copyright (c) 2017, SuZhou CsjBot. All Rights Reserved.
 * www.csjbot.com
 * <p>
 * Created by 浦耀宗 at 2017/09/11 0011-15:34.
 * Email: puyz@csjbot.com
 */

public interface AiSolutionCallBack {
    void onSucceed(String answer);

    void onError(Throwable throwable);

    void onNoAnswer(String txt, Throwable throwable);
}
