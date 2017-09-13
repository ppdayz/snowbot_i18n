package com.csjbot.snowbot.services.google_speech;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.csjbot.csjbase.base.CsjBaseService;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.services.serial.Old5MicSerialManager;
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
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import com.google.protobuf.ByteString;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

public abstract class GoogleSpeechBaseService extends CsjBaseService implements StreamObserver<StreamingRecognizeResponse> {
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
    private volatile AccessTokenTask mAccessTokenTask;

    private Handler mHandler = new Handler();
    private StreamObserver<StreamingRecognizeRequest> mRequestObserver;

    protected SpeechGrpc.SpeechStub mApi;
    protected Old5MicSerialManager micSerialManager = Old5MicSerialManager.getInstance();
    protected VoiceRecorder mVoiceRecorder;

    @Override
    public void onCreate() {
        super.onCreate();
        fetchAccessToken();
        mHandler = new Handler();
    }

    /*
     * *********************************************************************************************
     * **********************************  begin of recognize   ************************************
     * **********************************  begin of recognize   ************************************
     * **********************************  begin of recognize   ************************************
     * *********************************************************************************************
     */
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
        mRequestObserver = mApi.streamingRecognize(this);
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
        try {
            mRequestObserver.onNext(StreamingRecognizeRequest.newBuilder()
                    .setAudioContent(ByteString.copyFrom(data, 0, size))
                    .build());
        } catch (IllegalStateException e) {
            Csjlogger.error(e);
        }
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
    /*
     * *********************************************************************************************
     * **********************************  end of recognize   **************************************
     * **********************************  end of recognize   **************************************
     * **********************************  end of recognize   **************************************
     * *********************************************************************************************
     */


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

    protected void startVoiceRecorder() {
        Log.w(TAG, "startVoiceRecorder");
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
        }
        mVoiceRecorder = new VoiceRecorder(mVoiceCallback);
        mVoiceRecorder.start();
    }

    protected void stopVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
            mVoiceRecorder = null;
        }
    }

    /**
     * *********************************************************************************************
     * **********************************  begin of   AccessToken   ********************************
     * **********************************  begin of   AccessToken   ********************************
     * **********************************  begin of   AccessToken   ********************************
     * *********************************************************************************************
     */
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
//                if (mSpeechSynthesizer != null) {
//                    mSpeechSynthesizer.startSpeaking("credential file not found", null);
//                }
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
            if (accessToken != null) {
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
            } else {
//                if (mSpeechSynthesizer != null) {
//                    mSpeechSynthesizer.startSpeaking("Access Token is null, please check the credential file or network", speechSynthesizerListener);
//                }
                Csjlogger.error("Access Token is null, please check the credential file or network");
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
                protected void checkedStart(ClientCall.Listener<RespT> responseListener, Metadata headers)
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

    protected void fetchAccessToken() {
        if (mAccessTokenTask != null) {
            return;
        }
        mAccessTokenTask = new AccessTokenTask();
        mAccessTokenTask.execute();
    }

    /**
     * *********************************************************************************************
     * ************************************  end of   AccessToken   ********************************
     * ************************************  end of   AccessToken   ********************************
     * ************************************  end of   AccessToken   ********************************
     * *********************************************************************************************
     */


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

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
}
