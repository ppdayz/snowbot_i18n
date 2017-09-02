package com.csjbot.snowbot.activity.aiui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.media.SmoothStreamingTestMediaDrmCallback;
import com.csjbot.snowbot.media.WidevineTestMediaDrmCallback;
import com.csjbot.snowbot.media.exoplayer.DashRendererBuilder;
import com.csjbot.snowbot.media.exoplayer.ExoPlayerBean;
import com.csjbot.snowbot.media.exoplayer.ExtractorRendererBuilder;
import com.csjbot.snowbot.media.exoplayer.HlsRendererBuilder;
import com.csjbot.snowbot.media.exoplayer.SmoothStreamingRendererBuilder;
import com.csjbot.snowbot.services.CsjSpeechSynthesizer2;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecUtil;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.util.Util;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SynthesizerListener;

public class NewsActivityBack extends Activity implements ExoPlayerBean.Listener {
    private TextView prompt;//提示

    private ExoPlayerBean player;
    private int contentType;//流媒体传输协议类型
    private Uri uri;     //音乐播放路径
    private String answerText, imageUrl;

    private long playerPosition;

    //oncreat()--mediaPlayer初始状态为stop
    private boolean playerNeedsPrepare, first = true;

    private LocalBroadcastManager lbm;
    private Intent expressionIntent = new Intent(Constant.Expression.ACTION_EXPRESSION_FACE);
    private Intent isMusicInt = new Intent(Constant.ACTION_IN_ACTIVITY);

    // 准备播放的状态
    private boolean speakFinished = false, readyToPlay = false;

    // 单曲循环和列表循环
    private boolean isLoopPlay = false, isCycle = false;

    private MediaPlayer mediaPlayer = new MediaPlayer();        //媒体播放器对象
    private Handler mHandler = new Handler();
    private ImageView imageView_news;
    //暂停状态
    private boolean isPause;

    //唤醒，退出界面（并令ismusic=false）
    private BroadcastReceiver wakeupReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            isMusicInt.putExtra("ismusic", false);
            lbm.sendBroadcast(isMusicInt);
            finishMedia();
        }
    };
    //休眠指令，退出界面（MusicX）
    private BroadcastReceiver sleepReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isMusicInt.putExtra("ismusic", false);
            lbm.sendBroadcast(isMusicInt);
            finishMedia();
        }
    };

    private BroadcastReceiver touchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isMusicInt.putExtra("ismusic", false);
            lbm.sendBroadcast(isMusicInt);
            finishMedia();
        }
    };

    //除播放故事外的任何指令，都退出界面
    private BroadcastReceiver otherCmdReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isMusicInt.putExtra("ismusic", false);
            lbm.sendBroadcast(isMusicInt);
            finishMedia();
        }
    };

    //换一条新闻时，退出
    private BroadcastReceiver restartActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isMusicInt.putExtra("ismusic", false);
            lbm.sendBroadcast(isMusicInt);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finishMedia();
                }
            }, 200);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        imageView_news = (ImageView) findViewById(R.id.imageView_news);
        prompt = (TextView) findViewById(R.id.prompt_news);

        uri = Uri.parse(getIntent().getStringExtra("url"));
        answerText = getIntent().getStringExtra("title");
        imageUrl = getIntent().getStringExtra("imageUrl");

        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(NewsActivityBack.this)
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.loading)    // 缺省的占位图片，一般可以设置成一个加载中的进度GIF图
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(imageView_news);
        }

        onShown(uri);
        speakAndPreparePlay(answerText);

        lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(wakeupReceiver, new IntentFilter(Constant.ACTION_WAKEUP));
        lbm.registerReceiver(touchReceiver, new IntentFilter(Constant.ACTION_TOUCH_GET));
        lbm.registerReceiver(sleepReceive, new IntentFilter(Constant.ACTION_SLEEP));
        lbm.registerReceiver(otherCmdReceiver, new IntentFilter(Constant.ACTION_OTHER_CMD));
        lbm.registerReceiver(restartActivity, new IntentFilter(Constant.ACTION_RESTART_CMD));
    }

    private void changeExpression(int expression) {
        expressionIntent.putExtra(Constant.Expression.EXTRA_EXPRESSION_FACE, expression);
        lbm.sendBroadcast(expressionIntent);
    }

    /**
     * 说完话后播放新闻
     */
    private void speakAndPreparePlay(final String text) {
        CsjSpeechSynthesizer2.getSynthesizer().startSpeaking(text, new SynthesizerListener() {
            @Override
            public void onSpeakBegin() {
                speakFinished = false;
                changeExpression(Constant.Expression.EXPRESSION_SPEAK);
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
                speakFinished = true;
                preparePlayer(true);
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        changeExpression(Constant.Expression.EXPRESSION_NORMAL);
        CsjSpeechSynthesizer2.getSynthesizer().stopSpeaking();
        if (player != null) {
            try {
                if (player.getPlayWhenReady()) {
                    player.setPlayWhenReady(false);
                }
                releasePlayer();
            } catch (IllegalStateException e) {
                Csjlogger.debug("IllegalStateException");
            }
        }
        this.finish();
    }

    public void finishMedia() {
        changeExpression(Constant.Expression.EXPRESSION_NORMAL);
        releasePlayer();
        finish();
    }

    @Override
    public void onDestroy() {
        lbm.unregisterReceiver(wakeupReceiver);
        lbm.unregisterReceiver(touchReceiver);
        lbm.unregisterReceiver(sleepReceive);
        lbm.unregisterReceiver(otherCmdReceiver);
        super.onDestroy();
        releasePlayer();
    }


    //释放player
    private void releasePlayer() {
        first = true;
        if (player != null) {
            playerPosition = 0;
            player.release();
            player = null;
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
        if (speakFinished && readyToPlay) {
            player.setPlayWhenReady(playWhenReady);
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
                readyToPlay = false;
                prompt.setVisibility(View.VISIBLE);
                prompt.setText(getResources().getString(R.string.exoplayer_buffering) + player.getBufferedPercentage());
                text += "buffering";
                playerPosition = player.getCurrentPosition();
                break;
            case ExoPlayer.STATE_ENDED:
                readyToPlay = false;
                text += "ended";
                changeExpression(Constant.Expression.EXPRESSION_NORMAL);
                break;
            case ExoPlayer.STATE_IDLE://空的
                readyToPlay = false;
                prompt.setVisibility(View.VISIBLE);
                prompt.setText(getResources().getString(R.string.exoplayer_check_wifi));
                text += "idle";
                break;
            case ExoPlayer.STATE_PREPARING:
                readyToPlay = false;
                prompt.setVisibility(View.VISIBLE);
                prompt.setText(getResources().getString(R.string.exoplayer_preparing));
                text += "preparing";
                playerPosition = player.getCurrentPosition();
                Csjlogger.debug("onStateChanged " + "准备中");
                break;
            case ExoPlayer.STATE_READY:
                readyToPlay = true;
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
//        Csjlogger.debug(text);
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
