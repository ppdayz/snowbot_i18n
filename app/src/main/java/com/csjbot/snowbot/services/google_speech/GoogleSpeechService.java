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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

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
import com.csjbot.snowbot.services.AIUIService;
import com.csjbot.snowbot.services.CsjSpeechSynthesizer2;
import com.csjbot.snowbot.services.EventWakeup;
import com.csjbot.snowbot.services.serial.Old5MicSerialManager;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot_rogue.Events.AIUIEvent;
import com.csjbot.snowbot_rogue.Events.EventsConstants;
import com.csjbot.snowbot_rogue.Events.ExpressionEvent;
import com.csjbot.snowbot_rogue.platform.SnowBotManager;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.google.auth.Credentials;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognizeRequest;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechGrpc;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import com.google.protobuf.ByteString;
import com.iflytek.aiui.uartkit.entity.AIUIPacket;
import com.iflytek.aiui.uartkit.entity.MsgPacket;
import com.iflytek.aiui.uartkit.entity.WIFIConfPacket;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SynthesizerListener;
import com.slamtec.slamware.action.MoveDirection;

import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import dou.utils.StringUtils;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.internal.DnsNameResolverProvider;
import io.grpc.okhttp.OkHttpChannelProvider;
import io.grpc.stub.StreamObserver;

@SuppressWarnings("unused")
public class GoogleSpeechService extends CsjBaseService {

    public interface Listener {

        /**
         * Called when a new piece of text was recognized by the Speech API.
         *
         * @param text    The text.
         * @param isFinal {@code true} when the API finished processing audio.
         */
        void onSpeechRecognized(String text, boolean isFinal);
    }

    public static final long MAX_RECOGNIZING_TIME = 10 * 1000;

    private static final String TAG = "GoogleSpeechService";
    private static final String PREFS = "GoogleSpeechService";
    private static final String PREF_ACCESS_TOKEN_VALUE = "access_token_value";


    private static final String PREF_ACCESS_TOKEN_EXPIRATION_TIME = "access_token_expiration_time";
    /**
     * We reuse an access token if its expiration time is longer than this.
     */
    private static final int ACCESS_TOKEN_EXPIRATION_TOLERANCE = 30 * 60 * 1000; // thirty minutes

    /**
     * We refresh the current access token before it expires.
     */
    private static final int ACCESS_TOKEN_FETCH_MARGIN = 60 * 1000; // one minute
    public static final List<String> SCOPE =
            Collections.singletonList("https://www.googleapis.com/auth/cloud-platform");
    private static final String HOSTNAME = "speech.googleapis.com";

    private static final int PORT = 443;
    private final SpeechBinder mBinder = new SpeechBinder();
    private final ArrayList<Listener> mListeners = new ArrayList<>();
    private volatile AccessTokenTask mAccessTokenTask;
    private SpeechGrpc.SpeechStub mApi;
    private Old5MicSerialManager micSerialManager = Old5MicSerialManager.getInstance();
    private long lastRecognizingTime = Long.MAX_VALUE;
    private Map<String, String> customData = new HashMap<>();

    private final StreamObserver<StreamingRecognizeResponse> mResponseObserver
            = new StreamObserver<StreamingRecognizeResponse>() {
        @Override
        public void onNext(StreamingRecognizeResponse response) {
            String text = null;
            boolean isFinal = false;
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

                    postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_SPEAKTEXT_DATA, text));
                    postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_SPEAKTEXT_RC, 5));

                    if (!parseAction(text)) {
                        if (!parseSpeak(text)) {
                            postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_ANSWERTEXT_DATA, "I can't understand,but I'm learning"));
                            mSpeechSynthesizer.startSpeaking("I can't understand,but I'm learning", speechSynthesizerListener);
                        }
                    }
                    lastRecognizingTime = Long.MAX_VALUE;
                } else {
                    postEvent(new AIUIEvent(SpeechActivity.AIUI_SPEAKTEXT_DATA_NOT_FINAL, text));
                }
//                for (Listener listener : mListeners) {
//                    listener.onSpeechRecognized(text, isFinal);
//                }
            }
        }

        @Override
        public void onError(Throwable t) {
            Csjlogger.error("Error calling the API.", t);
            sleepAndGoodBy("Server Error please check");

            postEvent(new AIUIEvent(SpeechActivity.AIUI_SPEAKTEXT_DATA_NOT_FINAL, "Error calling the API."));
        }

        @Override
        public void onCompleted() {
            Csjlogger.error("API completed.");
        }

    };

    private final StreamObserver<RecognizeResponse> mFileResponseObserver
            = new StreamObserver<RecognizeResponse>() {
        @Override
        public void onNext(RecognizeResponse response) {
            String text = null;
            if (response.getResultsCount() > 0) {
                final SpeechRecognitionResult result = response.getResults(0);
                if (result.getAlternativesCount() > 0) {
                    final SpeechRecognitionAlternative alternative = result.getAlternatives(0);
                    text = alternative.getTranscript();
                }
            }
            if (text != null) {
                for (Listener listener : mListeners) {
                    listener.onSpeechRecognized(text, true);
                }
            }
        }

        @Override
        public void onError(Throwable t) {
            Log.e(TAG, "Error calling the API.", t);
        }

        @Override
        public void onCompleted() {
            Log.i(TAG, "API completed.");
        }

    };


    private StreamObserver<StreamingRecognizeRequest> mRequestObserver;

    public static GoogleSpeechService from(IBinder binder) {
        return ((SpeechBinder) binder).getService();
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        fetchAccessToken();
        initTTSAndWakeup();
        checkRecognizingState();
        initCustomTalk();
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
                    mVoiceRecorder.dismiss();
                    finishRecognizing();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mFetchAccessTokenRunnable);
        mHandler = null;
        // Release the gRPC channel.
        if (mApi != null) {
            final ManagedChannel channel = (ManagedChannel) mApi.getChannel();
            if (channel != null && !channel.isShutdown()) {
                try {
                    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Error shutting down the gRPC channel.", e);
                }
            }
            mApi = null;
        }
    }

    private void fetchAccessToken() {
        if (mAccessTokenTask != null) {
            return;
        }
        mAccessTokenTask = new AccessTokenTask();
        mAccessTokenTask.execute();
    }

    private String getDefaultLanguageCode() {
        final Locale locale = Locale.getDefault();
        final StringBuilder language = new StringBuilder(locale.getLanguage());
        final String country = locale.getCountry();
        if (!TextUtils.isEmpty(country)) {
            language.append("-");
            language.append(country);
        }
        return language.toString();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

//    public void addListener(@NonNull Listener listener) {
//        mListeners.add(listener);
//    }
//
//    public void removeListener(@NonNull Listener listener) {
//        mListeners.remove(listener);
//    }

    /**
     * Starts recognizing speech audio.
     *
     * @param sampleRate The sample rate of the audio.
     */
    public void startRecognizing(int sampleRate) {
        if (mApi == null) {
            Log.w(TAG, "API not ready. Ignoring the request.");
            return;
        }
        // Configure the API
        mRequestObserver = mApi.streamingRecognize(mResponseObserver);
        mRequestObserver.onNext(StreamingRecognizeRequest.newBuilder()
                .setStreamingConfig(StreamingRecognitionConfig.newBuilder()
                        .setConfig(RecognitionConfig.newBuilder()
                                .setLanguageCode(getDefaultLanguageCode())
//                                .setLanguageCode("en-US")
                                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                                .setSampleRateHertz(sampleRate)
                                .build())
                        .setInterimResults(true)
                        .setSingleUtterance(true)
                        .build())
                .build());
    }

    /**
     * Recognizes the speech audio. This method should be called every time a chunk of byte buffer
     * is ready.
     *
     * @param data The audio data.
     * @param size The number of elements that are actually relevant in the {@code data}.
     */
    public void recognize(byte[] data, int size) {
        if (mRequestObserver == null) {
            return;
        }
        // Call the streaming recognition API
        mRequestObserver.onNext(StreamingRecognizeRequest.newBuilder()
                .setAudioContent(ByteString.copyFrom(data, 0, size))
                .build());
    }

    /**
     * Finishes recognizing speech audio.
     */
    public void finishRecognizing() {
        if (mRequestObserver == null) {
            return;
        }
        mRequestObserver.onCompleted();
        mRequestObserver = null;
    }

    /**
     * Recognize all data from the specified {@link InputStream}.
     *
     * @param stream The audio data.
     */
    @SuppressWarnings("unused")
    public void recognizeInputStream(InputStream stream) {
        try {
            mApi.recognize(
                    RecognizeRequest.newBuilder()
                            .setConfig(RecognitionConfig.newBuilder()
                                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                                    .setLanguageCode("en-US")
                                    .setSampleRateHertz(16000)
                                    .build())
                            .setAudio(RecognitionAudio.newBuilder()
                                    .setContent(ByteString.readFrom(stream))
                                    .build())
                            .build(),
                    mFileResponseObserver);
        } catch (IOException e) {
            Log.e(TAG, "Error loading the input", e);
        }
    }

    private class SpeechBinder extends Binder {
        GoogleSpeechService getService() {
            return GoogleSpeechService.this;
        }
    }

    private final Runnable mFetchAccessTokenRunnable = this::fetchAccessToken;

    private class AccessTokenTask extends AsyncTask<Void, Void, AccessToken> {

        @Override
        protected AccessToken doInBackground(Void... voids) {
            final SharedPreferences prefs =
                    getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String tokenValue = prefs.getString(PREF_ACCESS_TOKEN_VALUE, null);
            long expirationTime = prefs.getLong(PREF_ACCESS_TOKEN_EXPIRATION_TIME, -1);

            // Check if the current token is still valid for a while
            if (tokenValue != null && expirationTime > 0) {
                if (expirationTime
                        > System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TOLERANCE) {
                    return new AccessToken(tokenValue, new Date(expirationTime));
                }
            }

            // ***** WARNING *****
            // In this sample, we load the credential from a JSON file stored in a raw resource
            // folder of this client app. You should never do this in your app. Instead, store
            // the file in your server and obtain an access token from there.
            // *******************
            String file = Environment.getExternalStorageDirectory().getAbsolutePath() + "/credential.json";
            try {
                FileInputStream fis = new FileInputStream(file);

                final GoogleCredentials credentials = GoogleCredentials.fromStream(fis)
                        .createScoped(SCOPE);
                final AccessToken token = credentials.refreshAccessToken();
                prefs.edit()
                        .putString(PREF_ACCESS_TOKEN_VALUE, token.getTokenValue())
                        .putLong(PREF_ACCESS_TOKEN_EXPIRATION_TIME,
                                token.getExpirationTime().getTime())
                        .apply();
                return token;
            } catch (FileNotFoundException e) {
//                Csjlogger.error("FileNotFoundException ", e);
                Csjlogger.warn("check the credential file [{}] existed", file);

                if (mSpeechSynthesizer != null) {
                    mSpeechSynthesizer.startSpeaking("credential file not found", null);
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to obtain access token.", e);
            }

//            final InputStream stream = getResources().openRawResource(credential);
//            try {
//                final GoogleCredentials credentials = GoogleCredentials.fromStream(stream)
//                        .createScoped(SCOPE);
//                final AccessToken token = credentials.refreshAccessToken();
//                prefs.edit()
//                        .putString(PREF_ACCESS_TOKEN_VALUE, token.getTokenValue())
//                        .putLong(PREF_ACCESS_TOKEN_EXPIRATION_TIME,
//                                token.getExpirationTime().getTime())
//                        .apply();
//                return token;
//            } catch (IOException e) {
//                Log.e(TAG, "Failed to obtain access token.", e);
//            }
            return null;
        }

        @Override
        protected void onPostExecute(AccessToken accessToken) {
            mAccessTokenTask = null;
            final ManagedChannel channel = new OkHttpChannelProvider()
                    .builderForAddress(HOSTNAME, PORT)
                    .nameResolverFactory(new DnsNameResolverProvider())
                    .intercept(new GoogleCredentialsInterceptor(new GoogleCredentials(accessToken)
                            .createScoped(SCOPE)))
                    .build();
            mApi = SpeechGrpc.newStub(channel);
            // Schedule access token refresh before it expires
            if (mHandler != null) {
                mHandler.postDelayed(mFetchAccessTokenRunnable,
                        Math.max(accessToken.getExpirationTime().getTime()
                                - System.currentTimeMillis()
                                - ACCESS_TOKEN_FETCH_MARGIN, ACCESS_TOKEN_EXPIRATION_TOLERANCE));
            }
        }
    }

    /**
     * Authenticates the gRPC channel using the specified {@link GoogleCredentials}.
     */
    private static class GoogleCredentialsInterceptor implements ClientInterceptor {

        private final Credentials mCredentials;

        private Metadata mCached;

        private Map<String, List<String>> mLastMetadata;

        GoogleCredentialsInterceptor(Credentials credentials) {
            mCredentials = credentials;
        }

        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                final MethodDescriptor<ReqT, RespT> method, CallOptions callOptions,
                final Channel next) {
            return new ClientInterceptors.CheckedForwardingClientCall<ReqT, RespT>(
                    next.newCall(method, callOptions)) {
                @Override
                protected void checkedStart(Listener<RespT> responseListener, Metadata headers)
                        throws StatusException {
                    Metadata cachedSaved;
                    URI uri = serviceUri(next, method);
                    synchronized (this) {
                        Map<String, List<String>> latestMetadata = getRequestMetadata(uri);
                        if (mLastMetadata == null || mLastMetadata != latestMetadata) {
                            mLastMetadata = latestMetadata;
                            mCached = toHeaders(mLastMetadata);
                        }
                        cachedSaved = mCached;
                    }
                    headers.merge(cachedSaved);
                    delegate().start(responseListener, headers);
                }
            };
        }

        /**
         * Generate a JWT-specific service URI. The URI is simply an identifier with enough
         * information for a service to know that the JWT was intended for it. The URI will
         * commonly be verified with a simple string equality check.
         */
        private URI serviceUri(Channel channel, MethodDescriptor<?, ?> method)
                throws StatusException {
            String authority = channel.authority();
            if (authority == null) {
                throw Status.UNAUTHENTICATED
                        .withDescription("Channel has no authority")
                        .asException();
            }
            // Always use HTTPS, by definition.
            final String scheme = "https";
            final int defaultPort = 443;
            String path = "/" + MethodDescriptor.extractFullServiceName(method.getFullMethodName());
            URI uri;
            try {
                uri = new URI(scheme, authority, path, null, null);
            } catch (URISyntaxException e) {
                throw Status.UNAUTHENTICATED
                        .withDescription("Unable to construct service URI for auth")
                        .withCause(e).asException();
            }
            // The default port must not be present. Alternative ports should be present.
            if (uri.getPort() == defaultPort) {
                uri = removePort(uri);
            }
            return uri;
        }

        private URI removePort(URI uri) throws StatusException {
            try {
                return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), -1 /* port */,
                        uri.getPath(), uri.getQuery(), uri.getFragment());
            } catch (URISyntaxException e) {
                throw Status.UNAUTHENTICATED
                        .withDescription("Unable to construct service URI after removing port")
                        .withCause(e).asException();
            }
        }

        private Map<String, List<String>> getRequestMetadata(URI uri) throws StatusException {
            try {
                return mCredentials.getRequestMetadata(uri);
            } catch (IOException e) {
                throw Status.UNAUTHENTICATED.withCause(e).asException();
            }
        }

        private static Metadata toHeaders(Map<String, List<String>> metadata) {
            Metadata headers = new Metadata();
            if (metadata != null) {
                for (String key : metadata.keySet()) {
                    Metadata.Key<String> headerKey = Metadata.Key.of(
                            key, Metadata.ASCII_STRING_MARSHALLER);
                    for (String value : metadata.get(key)) {
                        headers.put(headerKey, value);
                    }
                }
            }
            return headers;
        }

    }


    //=======================================================//
    //=======================================================//

    private VoiceRecorder mVoiceRecorder;
    private final static String KEY_ANGLE = "angle";

    private CsjSpeechSynthesizer2 mSpeechSynthesizer;
    private SnowBotManager snowBotManager = SnowBotManager.getInstance();
    //    private UARTAgent mAgent;
    private String[] wakeupTalk;
    private Handler mHandler = new Handler();


    private void startVoiceRecorder() {
        Log.w(TAG, "startVoiceRecorder");
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
        }
        mVoiceRecorder = new VoiceRecorder(mVoiceCallback);
        mVoiceRecorder.start();
    }

    private void stopVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
            mVoiceRecorder = null;
        }
    }

    private final VoiceRecorder.Callback mVoiceCallback = new VoiceRecorder.Callback() {

        @Override
        public void onVoiceStart() {
            startRecognizing(mVoiceRecorder.getSampleRate());
        }

        @Override
        public void onVoice(byte[] data, int size) {
            recognize(data, size);
        }

        @Override
        public void onVoiceEnd() {
            finishRecognizing();
        }
    };

    private void initTTSAndWakeup() {
        // init res
        wakeupTalk = getResources().getStringArray(R.array.wakeup_array_en);

        // init SpeechRecognizer

        // init wake up listener
//        mAgent = UARTAgent.createAgent(this, "/dev/ttyS4", 115200, event -> {
//            switch (event.eventType) {
//                case UARTConstant.EVENT_INIT_SUCCESS:
//                    Csjlogger.info("AIUI init success");
//                    mAgent.sendMessage(PacketBuilder.obtainWIFIStatusReqPacket());
//                    break;
//                case UARTConstant.EVENT_INIT_FAILED:
//                    Csjlogger.error("Init UART Failed");
//                    break;
//                case UARTConstant.EVENT_MSG:
//                    MsgPacket recvPacket = (MsgPacket) event.data;
//                    processPacket(recvPacket);
//                    break;
//                case UARTConstant.EVENT_SEND_FAILED:
//                    MsgPacket sendPacket = (MsgPacket) event.data;
//                    mAgent.sendMessage(sendPacket);
//                default:
//                    break;
//            }
//        });

        // init tts
        mSpeechSynthesizer = CsjSpeechSynthesizer2.createSynthesizer(this.getApplicationContext(), resault -> {
            Csjlogger.info("init resault " + resault);
            if (resault == 0) {
                // // FIXME: 2017/08/16 0016  TEST only
                mSpeechSynthesizer.startSpeaking("Initialize voice successfully", null);
            }
        });

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                wakeup();
//            }
//        }, 2000);
//        Csjlogger.debug("onCreate");
    }

    @Nullable
    private JSONTokener getJSONTokener(String content) {
        if (content.contains("wifi_status")) {
            return null;
        }

        return new JSONTokener(content);
    }

    @SuppressWarnings("unused")
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
                        case AIUIService.EVENT_WAKEUP:
                            // get wakeup Angle
                            JSONObject wakeInfo = new JSONObject(contentBean.getInfo());
                            int wakeAngle = wakeInfo.getInt(KEY_ANGLE);
                            Csjlogger.debug("wake up angle is {}", wakeAngle);
//                            mAgent.sendMessage(PacketBuilder.obtainAIUICtrPacket(AIUIMessage.CMD_RESET_WAKEUP, 0, 0, ""));

                            wakeup(wakeAngle);
                            if (!Kits.Package.isTopActivity(this, "com.csjbot.snowbot.activity.aiui.SpeechActivity")) {
                                Intent it = new Intent(this, SpeechActivity.class);
                                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(it);
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
     * handling wakeup event
     *
     * @param angle turn degree
     */
    private void wakeup(int angle) {
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
        mVoiceRecorder.dismiss();
    }

    private void sleepAndGoodBy(String goodByString){
        mSpeechSynthesizer.startSpeaking("goodByString", forceSleepSynthesizerListener);

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
            mVoiceRecorder.dismiss();
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
