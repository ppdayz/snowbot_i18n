package com.csjbot.snowbot.activity.aiui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.aiui.base.AIUIActivity;
import com.csjbot.snowbot.bean.aiui.MusicBean;
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
import com.csjbot.snowbot.views.aiui.playerview.MusicPlayerView;
import com.csjbot.snowbot_rogue.Events.AIUIEvent;
import com.csjbot.snowbot_rogue.Events.EventsConstants;
import com.csjbot.snowbot_rogue.Events.ExpressionEvent;
import com.csjbot.snowbot_rogue.utils.CSJToast;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecUtil;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.util.Util;
import com.iflytek.cloud.SpeechError;

import java.util.List;

public class MusicActivity extends AIUIActivity implements ExoPlayerBean.Listener {
    private TextView prompt;//提示
    private TextView songInfo;
    private MusicPlayerView musicPlayerView;
    private ExoPlayerBean player;
    private Uri uri;     //音乐播放路径
    private String data, singer, name, answerText;
    private List<MusicBean.ResultBean> musicList = null;
    private long playerPosition;
    private int contentType;//流媒体传输协议类型
    private Handler mHandler = new Handler();

    //oncreat()--mediaPlayer初始状态为stop,pauseCmd是否为暂停指令
    private boolean playerNeedsPrepare, pause = true, first = true, pauseCmd = false;

    private int Duration;//视频的大小
    private int index = 0;

    // 准备播放的状态
    private boolean speakFinished = false;

    // 单曲循环和列表循环
    private boolean isLoopPlay = false, isCycle = false;

    private void musicDoControl(String action) {
        if (action != null) {
            switch (action) {
                case Constant.Music.INSTYPE_PAUSE:
                    pauseCmd = true;
                    pause();
                    break;
                case Constant.Music.INSTYPE_NEXT:
                    pauseCmd = false;
                    playNext();
                    break;
                case Constant.Music.INSTYPE_PAST:
                    pauseCmd = false;
                    playPast();
                    break;
                case Constant.Music.INSTYPE_CYCLE://单曲循环
                    pauseCmd = false;
                    // 单曲循环和列表循环不能同时
                    if (!isCycle) {
                        isCycle = true;
                        isLoopPlay = false;
                        CSJToast.showToast(this, getResources().getString(R.string.open_cycle));
                    } else {
                        isCycle = false;
                        CSJToast.showToast(this, getResources().getString(R.string.close_cycle));
                    }
                    break;
                case Constant.Music.INSTYPE_LOOP:
                    pauseCmd = false;
                    // 单曲循环和列表循环不能同时
                    if (!isLoopPlay) {
                        isLoopPlay = true;
                        isCycle = false;
                        CSJToast.showToast(this, getResources().getString(R.string.open_loop));
                    } else {
                        isLoopPlay = false; //——人工取消循环，说两遍，暂定
                        CSJToast.showToast(this, getResources().getString(R.string.close_loop));
                    }
                    break;
                case Constant.Music.INSTYPE_REPEAT:
                    pauseCmd = false;
                    releasePlayer();
                    onShown(uri);
                    speakAndPreparePlay(answerText);
                    break;
                case Constant.Music.INSTYPE_REPLAY:
                    pauseCmd = true;
                    continueToPlay();
                    break;
                default:
                    break;
            }
        } else {
            Csjlogger.debug("music x action is null");
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        //常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initView();

        data = getIntent().getStringExtra("data");
        MusicBean musicBean = JSON.parseObject(data, MusicBean.class);
        musicList = musicBean.getResult();
        musicDoData(0);
    }

    private void initView() {
        prompt = (TextView) findViewById(R.id.prompt);
        songInfo = (TextView) findViewById(R.id.songInfo);
        musicPlayerView = (MusicPlayerView) findViewById(R.id.exo_mpv);
        musicPlayerView.setAutoProgress(true);
        musicPlayerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseCmd = true;
                if (player != null) {
                    if (player.getPlayWhenReady()) {
                        player.setPlayWhenReady(false);
                        musicPlayerView.stop();
                        pause = true;
                    } else {
                        player.setPlayWhenReady(true);
                        musicPlayerView.start();
                        pause = false;
                    }
                }
            }
        });
    }

    /**
     * 播放上一首
     */
    private void playPast() {
        index--;
        if (index < 0) {
            index = musicList.size() - 1;
        }
        musicDoData(index);
    }

    /**
     * 播放下一首
     */
    private void playNext() {
        index++;
        if (index >= musicList.size()) {
            index = 0;
        }
        musicDoData(index);
    }

    /**
     * 继续播放
     */
    private void continueToPlay() {
        Csjlogger.debug("continueToPlay");

        player.setPlayWhenReady(true);
        musicPlayerView.start();
        pause = false;
    }

    /**
     * 说完话后播放音乐
     */
    private void speakAndPreparePlay(final String text) {
        postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_ANSWERTEXT_DATA, text));
        CsjSpeechSynthesizer2.getSynthesizer().startSpeaking(text, new CsjSynthesizerListener() {
            @Override
            public void onSpeakBegin() {
                speakFinished = false;
                SpeechStatus.getIstance().setSpeakFinished(false);
                postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_SPEAK));
            }

            @Override
            public void onCompleted(SpeechError speechError) {
                speakFinished = true;
                preparePlayer(true);
            }
        });
    }

    /**
     * 暂停音乐
     */
    private void pause() {
        if (player != null && player.getPlayWhenReady()) {
            player.setPlayWhenReady(false);
            musicPlayerView.stop();
            pause = true;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releasePlayer();
    }

    //释放player
    private void releasePlayer() {
        first = true;
        if (player != null) {
            try {
                if (player.getPlayWhenReady()) {
                    player.setPlayWhenReady(false);
                    musicPlayerView.stop();
                    pause = true;
                }
                musicPlayerView.setProgress(0);
                player.release();
                player = null;
            } catch (Exception e) {
                Csjlogger.debug(e.toString());
            } finally {
                musicPlayerView.stop();
                if (player != null) {
                    player.release();
                    player = null;
                }
            }
        }
    }

    // Internal methods 内部方法--分配适配器?
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
        Csjlogger.debug("播放第[" + (index + 1) + "]首歌 [" + name + "], 总共(" + musicList.size() + ")首歌");

        contentType = inferContentType(uri);
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
                if (!pause) {
                    musicPlayerView.stop();
                    pause = true;
                }
                break;
            case ExoPlayer.STATE_ENDED:
                text += "ended";
                if (!pause) {
                    musicPlayerView.stop();
                    pause = true;
                }
                SpeechStatus.getIstance().setSpeakFinished(true);
                postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_NORMAL));
                //说完话后，播放音乐处于pause状态
                if (isLoopPlay) {
                    pauseCmd = false;
                    playNext();
                    return;
                } else if (isCycle) {
                    pauseCmd = false;
                    //                    musicDoData(index);
                    cyclePlay();
                    return;

                } else {
                    releasePlayer();
                    finish();
                }
                break;
            case ExoPlayer.STATE_IDLE://空的
                prompt.setVisibility(View.VISIBLE);
                prompt.setText(getResources().getString(R.string.exoplayer_check_wifi));
                text += "idle";
                break;
            case ExoPlayer.STATE_PREPARING:
                if (!pause) {
                    musicPlayerView.stop();
                    pause = true;
                }
                prompt.setVisibility(View.VISIBLE);
                prompt.setText(getResources().getString(R.string.exoplayer_preparing));
                text += "preparing";
                playerPosition = player.getCurrentPosition();
                break;
            case ExoPlayer.STATE_READY:
                text += "ready";
                prompt.setText(getResources().getString(R.string.exoplayer_ready));
                prompt.setVisibility(View.GONE);
                if (first) {
                    first = false;
                    //进度条的时间设置
                    Duration = (int) player.getDuration();
                    musicPlayerView.setMax((int) (player.getDuration() / 1000));
                    preparePlayer(true);
                }
                if (!first && speakFinished && !pauseCmd) {
                    musicPlayerView.start();
                    pause = false;
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

    @Override
    public boolean onAIUIEvent(AIUIEvent event) {
        if (super.onAIUIEvent(event)) {
            return true;
        }
        switch (event.getTag()) {
            case EventsConstants.AIUIEvents.MUSICX_MUSIC_CRTL:
                musicDoControl((String) event.data);
                break;
            default:
                Csjlogger.debug("event unCaptured   -》" + event.getTag());
                break;
        }
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Csjlogger.debug("onNewIntent");
        String tmpdata = intent.getStringExtra("data");
        MusicBean musicBean = JSON.parseObject(tmpdata, MusicBean.class);
        List<MusicBean.ResultBean> tempMusicList = musicBean.getResult();

        if (tempMusicList.size() > 0) {
            musicList = tempMusicList;
            data = tmpdata;
            musicDoData(0);
        }
    }

    private void cyclePlay() {
        releasePlayer();

        mHandler.postDelayed(() -> {
            uri = Uri.parse(musicList.get(index).getAudiopath());
            singer = musicList.get(index).getSingernames().get(0);
            name = musicList.get(index).getSongname();
            answerText = "请欣赏" + singer + "的歌曲" + name;
            songInfo.setText(singer + " : " + name);

            onShown(uri);

            preparePlayer(true);
        }, 500);

    }

    private void musicDoData(int index) {
        pauseCmd = false;
        speakFinished = false;
        releasePlayer();

        try {
            uri = Uri.parse(musicList.get(index).getAudiopath());
            singer = musicList.get(index).getSingernames().get(0);
            name = musicList.get(index).getSongname();
            answerText = "请欣赏" + singer + "的歌曲" + name;
            songInfo.setText(singer + " : " + name);

            onShown(uri);
            speakAndPreparePlay(answerText);
        } catch (Exception e) {
            CsjSpeechSynthesizer2.getSynthesizer().startSpeaking("获取歌曲错误，请重试", null);
            Csjlogger.error(e);
            this.finish();
        }
    }

    @Override
    public boolean wakeup() {
        postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_NORMAL));
        releasePlayer();
        return super.wakeup();
    }

    @Override
    public boolean forceSleep() {
        releasePlayer();
        return super.forceSleep();
    }

    @Override
    public boolean otherCmd() {
        releasePlayer();
        return super.otherCmd();
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_exoplayer;
    }
}
