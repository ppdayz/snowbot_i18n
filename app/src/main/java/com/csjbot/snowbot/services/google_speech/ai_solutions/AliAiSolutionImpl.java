package com.csjbot.snowbot.services.google_speech.ai_solutions;

import com.alibaba.nls.QaAnswer;
import com.alibaba.nls.QaClient;
import com.alibaba.nls.QaRequest;
import com.alibaba.nls.QaResponse;

/**
 * Copyright (c) 2017, SuZhou CsjBot. All Rights Reserved.
 * www.csjbot.com
 * <p>
 * Created by 浦耀宗 at 2017/09/11 0011-15:37.
 * Email: puyz@csjbot.com
 */

public class AliAiSolutionImpl implements IAiSolution {

    private class OptionalJson {
        /**
         * domains : theme1;theme2;theme3
         * top : 3
         */

        private String domains;
        private int top;

        public String getDomains() {
            return domains;
        }

        public void setDomains(String domains) {
            this.domains = domains;
        }

        public int getTop() {
            return top;
        }

        public void setTop(int top) {
            this.top = top;
        }
    }

    private static final String APP_KEY = "nui-" + "iSJHq7lij6sJ";

    private AiSolutionCallBack mCallBack;
    private OptionalJson optionalJson = new OptionalJson();

    public AliAiSolutionImpl(AiSolutionCallBack callback) {
        mCallBack = callback;
    }

    @Override
    public boolean sendMessage(final String question) {
        if (mCallBack == null) {
            return false;
        }

        new Thread(new Runnable() {
            /**
             * When an object implementing interface <code>Runnable</code> is used
             * to create a thread, starting the thread causes the object's
             * <code>run</code> method to be called in that separately executing
             * thread.
             * <p>
             * The general contract of the method <code>run</code> is that it may
             * take any action whatsoever.
             *
             * @see Thread#run()
             */
            @Override
            public void run() {
                QaClient client = new QaClient();
                QaRequest request = new QaRequest();
                request.setApp_key(APP_KEY);
                request.setQuestion(question);
                request.setVersion("2.0");
                optionalJson.setDomains("Monica-CA");
                request.setOptional(optionalJson);
                QaResponse response = client.sendRequest(request);
                boolean scored = false;
                if (response.getSuccess()) {
                    for (QaAnswer answer : response.getAnswers()) {
                        float score = answer.getScore();
                        if (score > 0.7f) {
                            mCallBack.onSucceed(answer.getAnswer());
                            scored = true;
                            break;
                        }
                    }

                    if (!scored) {
                        mCallBack.onNoAnswer("not scored ", null);
                    }
                } else {
                    mCallBack.onNoAnswer(response.getError_message(), null);
                }
            }
        }).start();

        return true;
    }
}



