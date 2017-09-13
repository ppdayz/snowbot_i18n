/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.csjbot.snowbot.services.google_speech;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.android.core.util.SharedUtil;
import com.csjbot.csjbase.kit.Kits;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.DanceAct;
import com.csjbot.snowbot.activity.VideoRecordActivity;
import com.csjbot.snowbot.activity.aiui.SpeechActivity;
import com.csjbot.snowbot.bean.Home;
import com.csjbot.snowbot.bean.aiui.SimilarityUtil;
import com.csjbot.snowbot.bean.aiui.entity.CsjSynthesizerListener;
import com.csjbot.snowbot.services.CsjSpeechSynthesizer2;
import com.csjbot.snowbot.services.EventWakeup;
import com.csjbot.snowbot.services.google_speech.ai_solutions.AiSolutionCallBack;
import com.csjbot.snowbot.services.google_speech.ai_solutions.AliAiSolutionImpl;
import com.csjbot.snowbot.services.google_speech.ai_solutions.IAiSolution;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot_rogue.Events.AIUIEvent;
import com.csjbot.snowbot_rogue.Events.EventsConstants;
import com.csjbot.snowbot_rogue.Events.ExpressionEvent;
import com.csjbot.snowbot_rogue.platform.SnowBotManager;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SynthesizerListener;
import com.slamtec.slamware.action.MoveDirection;

import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import dou.utils.StringUtils;

@SuppressWarnings("unused")
public class GoogleSpeechService extends GoogleSpeechBaseService {

    private long lastRecognizingTime = Long.MAX_VALUE;
    private Map<String, String> customData = new HashMap<>();
    private String cachedString = "";
    public static final long MAX_RECOGNIZING_TIME = 5 * 1000;

    // google streamingRecognize start
    @Override
    public void onNext(StreamingRecognizeResponse response) {
        String text = null;
        boolean isFinal = false;
        // Parse Text
        if (response.getResultsCount() > 0) {
            final StreamingRecognitionResult result = response.getResults(0);
            isFinal = result.getIsFinal();
            if (result.getAlternativesCount() > 0) {
                final SpeechRecognitionAlternative alternative = result.getAlternatives(0);
                text = alternative.getTranscript();
            }
        }

        if (text != null) {
            Csjlogger.warn(text);
            lastRecognizingTime = System.currentTimeMillis();
            if (isFinal) {
                // Show Text in SpeechActivity
                postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_SPEAKTEXT_DATA, text));
                postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_SPEAKTEXT_RC, 5));

                // 1. parse action, such as move,turn round
                if (!parseAction(text)) {
                    // 2. parse Custom semantics
                    if (!parseSpeak(text)) {
                        mAliAI.sendMessage(text);
                    }
                }
                lastRecognizingTime = Long.MAX_VALUE;
            } else {
                // if not final, Show toast in SpeechActivity
                cachedString = text;
                postEvent(new AIUIEvent(SpeechActivity.AIUI_SPEAKTEXT_DATA_NOT_FINAL, text));
            }
        }
    }

    @Override
    public void onError(Throwable t) {
        Csjlogger.error("Error calling the API.", t);
        sleepAndGoodBy("Server Error please check");
        micSerialManager.reset();

        postEvent(new AIUIEvent(SpeechActivity.AIUI_SPEAKTEXT_DATA_NOT_FINAL, "Error calling the API."));
    }

    @Override
    public void onCompleted() {
        Csjlogger.error("API completed.");
    }
    // google streamingRecognize end
    /**
     * you can put your ai here
     */
    private IAiSolution mAliAI = new AliAiSolutionImpl(new AiSolutionCallBack() {
        @Override
        public void onSucceed(String answer) {
            speakAndSend2UI(answer);
        }

        @Override
        public void onError(Throwable throwable) {
            postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_ANSWERTEXT_DATA, "I can't understand,but I'm learning"));
            mSpeechSynthesizer.startSpeaking("I can't understand,but I'm learning", speechSynthesizerListener);
        }

        @Override
        public void onNoAnswer(String txt, Throwable throwable) {
            postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_ANSWERTEXT_DATA, "I can't understand,but I'm learning"));
            mSpeechSynthesizer.startSpeaking("I can't understand,but I'm learning", speechSynthesizerListener);
        }
    });


    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        initTTSAndWakeup();
        checkRecognizingState();
        initCustomTalk();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    /**
     * 不停检测识别是否超时
     */
    private void checkRecognizingState() {
        new Thread(() -> {
            while (true) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }

                if (lastRecognizingTime == Long.MAX_VALUE) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                long now = System.currentTimeMillis();
                if (now - lastRecognizingTime > MAX_RECOGNIZING_TIME) {
                    Csjlogger.debug("lastRecognizingTime - now > MAX_RECOGNIZING_TIME ");

                    lastRecognizingTime = Long.MAX_VALUE;
                    if (mVoiceRecorder != null) {
                        mVoiceRecorder.dismiss();
                    }
                    finishRecognizing();
                    // Show Text in SpeechActivity
                    postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_SPEAKTEXT_DATA, cachedString));
                    postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_SPEAKTEXT_RC, 5));

                    // 1. parse action, such as move,turn round
                    if (!parseAction(cachedString)) {
                        // 2. parse Custom semantics
                        if (!parseSpeak(cachedString)) {
                            postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_ANSWERTEXT_DATA, "I can't understand,but I'm learning"));
                            mSpeechSynthesizer.startSpeaking("I can't understand,but I'm learning", speechSynthesizerListener);
                        }
                    }

                    postEvent(new AIUIEvent(SpeechActivity.AIUI_SPEAKTEXT_DATA_NOT_FINAL, "Recognize time out,reset!"));
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    //=======================================================//
    //=======================================================//

    private final static String KEY_ANGLE = "angle";

    private CsjSpeechSynthesizer2 mSpeechSynthesizer;
    private SnowBotManager snowBotManager = SnowBotManager.getInstance();
    //    private UARTAgent mAgent;
    private String[] wakeupTalk;
    private Handler mHandler = new Handler();

    private void initTTSAndWakeup() {
        // init res
        wakeupTalk = getResources().getStringArray(R.array.wakeup_array_en);

        // init tts
        mSpeechSynthesizer = CsjSpeechSynthesizer2.createSynthesizer(this.getApplicationContext(), resault -> {
            Csjlogger.info("init resault " + resault);
            if (resault == 0) {
//                 mSpeechSynthesizer.startSpeaking("Initialize voice successfully", null);
            }
        });
    }


    /**
     * handling wakeup event
     *
     * @param angle turn degree
     */
    private void wakeup(int angle) {
        if (mApi == null) {
            mSpeechSynthesizer.startSpeaking("Access Token is null, please check the credential file or network", speechSynthesizerListener);
            Csjlogger.error("Access Token is null, please check the credential file or network");
            fetchAccessToken();
            return;
        }

        // 1. turn snow
        snowBotManager.turnRound((short) angle);

        // 2. start VoiceRecorder
        startVoiceRecorder();

        // 3. if SpeechActivity is not on top, start it
        if (!Kits.Package.isTopActivity(this, "com.csjbot.snowbot.activity.aiui.SpeechActivity")) {
            Intent it = new Intent(this, SpeechActivity.class);
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(it);
        }

        // 4. find the random wakeup string and speak out
        String wakeupString = wakeupTalk[new Random().nextInt(wakeupTalk.length)];
        speakAndSend2UI(wakeupString);

        // 5. reset the voicce recorder
        if (mVoiceRecorder != null) {
            mVoiceRecorder.dismiss();
        }
    }

    private void sleepAndGoodBy(String goodByString) {
        mSpeechSynthesizer.startSpeaking(goodByString, forceSleepSynthesizerListener);

        micSerialManager.reset();
        stopVoiceRecorder();
    }

    /**
     * wake up event
     *
     * @param wakeup wakeup event ,has an angle
     */
    @Subscribe
    @SuppressWarnings("unused")
    public void wakeup(EventWakeup wakeup) {
        int angle = wakeup.getAngle();
        if (angle >= 0 && angle <= 360) {
            Csjlogger.debug("getAngle is {}", angle);
            wakeup(angle);
        }
    }


    /**
     * Custom  Synthesizer callback
     * let snow change Expression to speakingExpression when begin speak
     * and change Expression to NORMAL when finish speak
     */
    private SynthesizerListener speechSynthesizerListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_SPEAK));
            if (mVoiceRecorder != null) {
                mVoiceRecorder.dismiss();
            }
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
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    //==============================================================================//
    private void initCustomTalk() {
        customData.put("How are you", "I’m pretty good.");
        customData.put("How are you today", "Not bad, and you?");
        customData.put("How do you do", "How do you do");
        customData.put("How are you doing", "I am doing great!");
        customData.put("Good morning", "Good morning");
        customData.put("Good afternoon", "Good afternoon");
        customData.put("Good evening", "evening, you look good!");
        customData.put("What’s your name", "This is Snow baby, what can I do for you?");
        customData.put(" What’s the weather like today", "It’s sunny");
        customData.put("could you tell me how’s the weather today", "It’s cloudy");
        customData.put("What can you do", "I am a genius good at many things such as singing, dancing, eating and sleeping.");
        customData.put("Where do you come from", "I am a pure Chinese");
        customData.put("When were you born", "China!");
        customData.put("What did you know about China before you came here", "China is a great country that is developing very rapidly, I am a pure Chinese girl and I love my country.");
        customData.put("Are you a boy or girl", "Who cares?");
        customData.put("How old are you", "You will never guess.");
        customData.put("What are you doing", "In the middle of chatting with you.");
        customData.put("What are you up to", "Leave me alone, I am meditating.");
        customData.put("Are you busy", "In the middle of chatting with you.");
        customData.put("Thank you, you are cute", "You are welcome.");
        customData.put("Thank you, you are sweet", "It’s my pleasure");
        customData.put("Can you go home with me", "No thanks, I am loyal to my owner.");
        customData.put("Can I take you home", "No thanks, I am loyal to my owner.");
        customData.put("Who are you", "You will never guess who I am");
        customData.put("Nice to meet you", "Nice to meet you too.");
        customData.put("Are you single", "No, I crushed on a boy robot though I am only 2 years old.");
        customData.put("Do you speak English", "Yes, a little.");
        customData.put("Can  you speak English", "Yes, a little.");
        customData.put("You speak English pretty well.", "Oh, thank you");
        customData.put("Your English is very good.", "Oh, thank you");
        customData.put("Are you a native English speaker?", "No, my native language is Chinese. I speak English with strong robot accent, hope you like it.");
        customData.put("Are you a native speaker of English", "No, my native language is Chinese. I speak English with strong robot accent, hope you like it.");
    }

    /**
     * This method handles custom semantics
     *
     * @param content the String you speak to the robot
     * @return retrun true if this method already deal the content String
     * otherwise return false
     */
    private boolean parseSpeak(String content) {
        if (StringUtils.isEmpty(content)) {
            return false;
        }

        for (String key : customData.keySet()) {
            double similarity = SimilarityUtil.sim(key.toUpperCase().replace(" ", ""), content.toUpperCase().replace(" ", ""));
            if (similarity > 0.8) {
                String answer = customData.get(key);
                speakAndSend2UI(answer);
                return true;
            }
        }

        return false;
    }

    /**
     * Speak the answer and send the answer to UI
     *
     * @param answer the text to deal with
     */
    private void speakAndSend2UI(String answer) {
        postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_ANSWERTEXT_DATA, answer));
        mSpeechSynthesizer.startSpeaking(answer, speechSynthesizerListener);
    }

    private CsjSynthesizerListener forceSleepSynthesizerListener = new CsjSynthesizerListener() {
        @Override
        public void onSpeakBegin() {
            postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_SPEAK));
        }

        @Override
        public void onCompleted(SpeechError speechError) {
            // 1. change .EXPRESSION_SLEEP
            postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_SLEEP));
            // 2. send FORCE_SLEEP event
            postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_EVENT_FORCE_SLEEP));
        }
    };

    /**
     * Strings let snow take you to some place
     */
    private String takemeStrings[] = new String[]{
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

    /**
     * This method deals with robot actions
     *
     * @param content the String you speak to the robot
     * @return retrun true if this method already deal the content String
     * otherwise return false
     */
    private boolean parseAction(String content) {
        if (StringUtils.isEmpty(content)) {
            return false;
        }

        String contentUpper = content.toUpperCase();

        // if has good bye , snow will sleep
        if (contentUpper.equals("GOOD BYE") || contentUpper.equals("BYE") || contentUpper.equals("GOODBYE")
                || contentUpper.equals("BYE BYE") || contentUpper.contains("SEE YOU")) {
            sleepAndGoodBy("Master, I'm backing off");
            return true;
        }

        // dance
        if (contentUpper.contains("DANCE")) {
            Intent intent = new Intent(this, DanceAct.class);
            intent.putExtra("autoDance", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        }

        if (contentUpper.contains("PHOTO")) {
            Intent intent = new Intent(this, VideoRecordActivity.class);
            intent.putExtra("autoTakePhoto", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        }

        // traversals all take strings
        for (String take : takemeStrings) {
            String takeUpCase = take.toUpperCase();
            if (contentUpper.contains(takeUpCase)) {
                // replace action with ""
                // eg: replace "I WANT TO GO TO THE LIVINGROOM" ===> "LIVINGROOM"
                String roomUpCase = contentUpper.replace(takeUpCase, "");
                Csjlogger.debug("room name is {}", roomUpCase);

                List<Home> homeLists = SharedUtil.getListObj(SharedKey.HOMEDATAS, Home.class);

                if (homeLists != null) {
                    // traversals all rooms
                    for (Home home : homeLists) {
                        String homeUpCase = home.getHomename().toUpperCase();
                        double similarity = SimilarityUtil.sim(homeUpCase, roomUpCase);
                        if (similarity > 0.8) {
                            Csjlogger.info("{} has [{}] similarity with {} , here we go", homeUpCase, similarity, roomUpCase);
                            snowBotManager.moveTo(home.getmOffsetX(), home.getmOffsetY());
                            String answer = "Snow will take you to  " + home.getHomename();
                            speakAndSend2UI(answer);
                            return true;
                        }
                    }

                    Csjlogger.warn("home not existed");

                    String answer = "Snow can't find the place";
                    speakAndSend2UI(answer);
                    return true;
                } else {
                    Csjlogger.error("home not found");
                }
            }
        }

        if (contentUpper.contains("TURN")) {
            if (contentUpper.contains("RIGHT")) {
                snowBotManager.turnRound((short) -90);
            } else {
                snowBotManager.turnRound((short) 90);
            }

            speakAndSend2UI("Yes,Master " + content);
            Csjlogger.debug("action turn");
            return true;
        }

        if (contentUpper.contains("BACK")) {
            snowBotManager.moveBy(MoveDirection.BACKWARD);
            speakAndSend2UI("Yes,Master");
            Csjlogger.debug("action back");
            return true;
        }

        if (contentUpper.contains("FORWARD") || contentUpper.contains("AHEAD")
                || contentUpper.contains("COME HERE")) {
            snowBotManager.moveBy(MoveDirection.FORWARD);
            Csjlogger.debug("action forward");
            speakAndSend2UI("Yes,I\'m Coming");
            return true;
        }

        // // TODO: 2017/08/16 0016 DIY

        return false;
    }
}
