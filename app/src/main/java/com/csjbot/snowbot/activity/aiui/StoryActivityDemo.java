package com.csjbot.snowbot.activity.aiui;

import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.aiui.base.AIUIActivity;
import com.csjbot.snowbot.bean.aiui.StoryBean;
import com.csjbot.snowbot.bean.aiui.entity.CsjSynthesizerListener;
import com.csjbot.snowbot.media.SmoothStreamingTestMediaDrmCallback;
import com.csjbot.snowbot.media.WidevineTestMediaDrmCallback;
import com.csjbot.snowbot.media.exoplayer.DashRendererBuilder;
import com.csjbot.snowbot.media.exoplayer.ExoPlayerBean;
import com.csjbot.snowbot.media.exoplayer.ExtractorRendererBuilder;
import com.csjbot.snowbot.media.exoplayer.HlsRendererBuilder;
import com.csjbot.snowbot.media.exoplayer.SmoothStreamingRendererBuilder;
import com.csjbot.snowbot.services.CsjSpeechSynthesizer2;
import com.csjbot.snowbot.utils.SpeechStatus;
import com.csjbot.snowbot_rogue.Events.AIUIEvent;
import com.csjbot.snowbot_rogue.Events.EventsConstants;
import com.csjbot.snowbot_rogue.Events.ExpressionEvent;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecUtil;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.util.Util;
import com.iflytek.cloud.SpeechError;

import java.util.List;

public class StoryActivityDemo extends AIUIActivity implements ExoPlayerBean.Listener {
    private TextView prompt;//提示
    private ExoPlayerBean player;
    private int contentType;//流媒体传输协议类型
    private long playerPosition;

    //oncreat()--mediaPlayer初始状态为stop
    private boolean playerNeedsPrepare, first = true;

    // 准备播放的状态
    private boolean speakFinished = false;

    private ImageView imageView;
    private AnimationDrawable animaition;

    private Uri uri;     //故事播放路径
    private String data, name, answerText;
    private List<StoryBean.ResultBean> storyList = null;
    private int res;

    @Override
    public boolean onAIUIEvent(AIUIEvent event) {
        if (super.onAIUIEvent(event)) {
            return true;
        }
        switch (event.getTag()) {
            case EventsConstants.AIUIEvents.MUSICX_NEWSBEAN_RESTART:
                CsjSpeechSynthesizer2.getSynthesizer().stopSpeaking();
                postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_NORMAL));
                releasePlayer();
                finish();
                break;
            default:
                Csjlogger.debug("event unCaptured   -》" + event.getTag());
                break;
        }
        return false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        playAnim();
    }

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        prompt = (TextView) findViewById(R.id.prompt_storyDemo);

        data = getIntent().getStringExtra("data");
        StoryBean stroyBean = JSON.parseObject(data, StoryBean.class);
        storyList = stroyBean.getResult();

        uri = Uri.parse(storyList.get(0).getPlayUrl());
        name = storyList.get(0).getName();
        answerText = "请欣赏" + name;

        onShown(uri);
        speakAndPreparePlay(answerText);
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_photo;
    }


    private void playAnim() {
        imageView = (ImageView) findViewById(R.id.imageView);
        res = getIntent().getIntExtra("storyImage", 0);
        imageView.setBackgroundResource(res);
        animaition = (AnimationDrawable) imageView.getBackground();
    }

    /**
     * 说完话后播放新闻
     */
    private void speakAndPreparePlay(final String text) {
        postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_ANSWERTEXT_DATA, text));
        CsjSpeechSynthesizer2.getSynthesizer().startSpeaking(text, new CsjSynthesizerListener() {
            @Override
            public void onSpeakBegin() {
                SpeechStatus.getIstance().setSpeakFinished(false);
                postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_SPEAK));
                speakFinished = false;
            }

            @Override
            public void onCompleted(SpeechError speechError) {
                speakFinished = true;
                preparePlayer(true);
            }

        });
    }

    @Override
    public boolean otherCmd() {
        releasePlayer();
        return super.otherCmd();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }


    //释放player
    private void releasePlayer() {
        first = true;
        if (player != null) {
            try {
                if (player.getPlayWhenReady()) {
                    player.setPlayWhenReady(false);
                }
                player.release();
                player = null;
            } catch (IllegalStateException e) {
                Csjlogger.debug("IllegalStateException");
            } finally {
                if (player != null) {
                    player.release();
                    player = null;
                }
            }
        }
    }

    // Internal methods 内部方法--分配适配器
    private ExoPlayerBean.RendererBuilder getRendererBuilder() {
        String userAgent = Util.getUserAgent(this, "MyExoPlayer");
        switch (contentType) {
            case Util.TYPE_SS:
                return new SmoothStreamingRendererBuilder(this, userAgent, uri.toString(),
                        new SmoothStreamingTestMediaDrmCallback());
            case Util.TYPE_DASH:
                return new DashRendererBuilder(this, userAgent, uri.toString(),
                        new WidevineTestMediaDrmCallback(null, null));
            case Util.TYPE_HLS:
                return new HlsRendererBuilder(this, userAgent, uri.toString());
            case Util.TYPE_OTHER:
                return new ExtractorRendererBuilder(this, userAgent, uri);
            default:
                throw new IllegalStateException("Unsupported type: " + contentType);
        }
    }

    /**
     * 获取视频数据
     * contentUri 是视频的uri播放地址
     * contentType 是流媒体传输协议类型，可以支持DASH和HLS以及OTHER类型
     */
    private void onShown(Uri uri) {
        contentType = inferContentType(uri);
//        Log.e("TAG", "contentUri" + contentUri + "contentType" + contentType);
        if (player == null) {
            //if (!maybeRequestPermission()) { 检查权限，6.0以上可以动态获取权限
            preparePlayer(true);
            //}
        } else {
            player.setBackgrounded(false);
        }
    }

    /**
     * 根据uri判断出媒体类型,//根据uri获取流媒体传输协议
     * Makes a best guess to infer the type from a media {@link Uri} and an optional overriding file
     * extension.使最佳猜测推断出从一个媒体类型{ @link Uri }和一个可选的最重要的文件扩展。
     *
     * @return The inferred type.推断出来的视频类型
     */
    private static int inferContentType(Uri uri) {
        String u = uri.toString();
        String lastPathSegment = u.substring(u.lastIndexOf("."));
        return Util.inferContentType(lastPathSegment);
    }

    /**
     * 初始化准备player
     * addListener添加监听，如播放的状态onStateChanged，视频大小变化onVideoSizeChanged， 出现错误onError等
     *
     * @param playWhenReady
     */
    private void preparePlayer(boolean playWhenReady) {
        if (player == null) {
            player = new ExoPlayerBean(getRendererBuilder());
            player.addListener(this);
            player.seekTo(playerPosition);//播放进度的设置，一开始为0
            playerNeedsPrepare = true; //是否立即准备播放器
        }
        if (playerNeedsPrepare) {
            player.prepare();
            playerNeedsPrepare = false;
        }
        if (speakFinished) {
            player.setPlayWhenReady(playWhenReady);
            animaition.start();
//            musicPlayerView.start();
        }
    }


    /**
     * 视频的播放状态
     * STATE_IDLE 播放器空闲，既不在准备也不在播放
     * STATE_PREPARING 播放器正在准备
     * STATE_BUFFERING 播放器已经准备完毕，但无法立即播放。此状态的原因有很多，但常见的是播放器需要缓冲更多数据才能开始播放
     * STATE_PAUSE 播放器准备好并可以立即播放当前位置
     * STATE_PLAY 播放器正在播放中
     * STATE_ENDED 播放已完毕
     */
    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        String text = "playWhenReady=" + playWhenReady + ", playbackState=";
        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                prompt.setVisibility(View.VISIBLE);
                prompt.setText(getResources().getString(R.string.exoplayer_buffering) + player.getBufferedPercentage());
                text += "buffering";
                playerPosition = player.getCurrentPosition();
                break;
            case ExoPlayer.STATE_ENDED:
                text += "ended";
                SpeechStatus.getIstance().setSpeakFinished(true);
                postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_NORMAL));
                break;
            case ExoPlayer.STATE_IDLE://空的
                prompt.setVisibility(View.VISIBLE);
                prompt.setText(getResources().getString(R.string.exoplayer_check_wifi));
                text += "idle";
                break;
            case ExoPlayer.STATE_PREPARING:
                prompt.setVisibility(View.VISIBLE);
                prompt.setText(getResources().getString(R.string.exoplayer_preparing));
                text += "preparing";
                playerPosition = player.getCurrentPosition();
                break;
            case ExoPlayer.STATE_READY:
                text += "ready";
                prompt.setVisibility(View.GONE);
                prompt.setText(getResources().getString(R.string.exoplayer_ready));
                if (first) {
                    first = false;
                    preparePlayer(true);
                }
                break;
            default:
                text += "unknown";
                break;
        }
        Csjlogger.debug(text);
    }

    @Override
    public void onError(Exception e) {
        String errorString = null;
        if (e instanceof UnsupportedDrmException) {
            // 特殊情况DRM的失败
            UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
            errorString = getString(Util.SDK_INT < 18 ? R.string.error_drm_not_supported
                    : unsupportedDrmException.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                    ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown);
        } else if (e instanceof ExoPlaybackException
                && e.getCause() instanceof MediaCodecTrackRenderer.DecoderInitializationException) {
            // Special case for decoder initialization failures.
            MediaCodecTrackRenderer.DecoderInitializationException decoderInitializationException =
                    (MediaCodecTrackRenderer.DecoderInitializationException) e.getCause();
            if (decoderInitializationException.decoderName == null) {
                if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                    errorString = getString(R.string.error_querying_decoders);
                } else if (decoderInitializationException.secureDecoderRequired) {
                    errorString = getString(R.string.error_no_secure_decoder,
                            decoderInitializationException.mimeType);
                } else {
                    errorString = getString(R.string.error_no_decoder,
                            decoderInitializationException.mimeType);
                }
            } else {
                errorString = getString(R.string.error_instantiating_decoder,
                        decoderInitializationException.decoderName);
            }
        }
        if (errorString != null) {
            Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_LONG).show();
        }
        playerNeedsPrepare = true;
    }
}

