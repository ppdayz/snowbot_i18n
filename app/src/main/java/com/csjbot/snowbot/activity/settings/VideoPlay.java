package com.csjbot.snowbot.activity.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot_rogue.Events.AIUIEvent;
import com.csjbot.snowbot_rogue.Events.EventsConstants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import fm.jiecao.jcvideoplayer_lib.JCMediaManager;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;


/**
 * @author: jl
 * @Time: 2017/1/6
 * @Desc:
 */

public class VideoPlay extends Activity implements JCMediaManager.JCMediaPlayerListener {
    private JCVideoPlayerStandard jcVideoPlayerStandard;
    private String url;
    private String title;
    private boolean loop;
    protected int mCurrentState = -1;//-1相当于null
    protected static final int CURRENT_STATE_NORMAL = 0;
    protected static final int CURRENT_STATE_PREPAREING = 1;
    protected static final int CURRENT_STATE_PLAYING = 2;
    protected static final int CURRENT_STATE_PLAYING_BUFFERING_START = 3;
    protected static final int CURRENT_STATE_PAUSE = 5;
    protected static final int CURRENT_STATE_AUTO_COMPLETE = 6;
    protected static final int CURRENT_STATE_ERROR = 7;

    private int videoType = USER_VIDEO;
    public static final String VIDEO_TYPE = "video_type";
    // 用户视频
    public static final int USER_VIDEO = 10086;
    // 录像视频
    public static final int RECODE_VIDEO = 10087;

    private EventBus ibus = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_video_play);


        ibus = EventBus.getDefault();

        if (ibus != null) {
            ibus.register(this);
        }
        init();
    }

    private void init() {
        jcVideoPlayerStandard = (JCVideoPlayerStandard) findViewById(R.id.videoView);
        Bundle bundle;
        if (null != getIntent().getBundleExtra("VIDEODATA")) {
            bundle = getIntent().getBundleExtra("VIDEODATA");
            url = bundle.getString("url");
            title = bundle.getString("title");
            loop = bundle.getBoolean("loop", false);
            videoType = bundle.getInt(VIDEO_TYPE);
            jcVideoPlayerStandard.setUp(url, title);
            jcVideoPlayerStandard.setLoop(loop);
        }
        if (url != null) {
            File file = new File(url);
            if (file.exists()) {
                long size = file.length();
                if (size <= 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(getResources().getString(R.string.file_notexist_bad));
                    builder.setNegativeButton(getResources()
                            .getString(R.string.make_sure), (dialog, which) -> {
                        dialog.dismiss();
                        this.finish();
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                    //                        Toast.makeText(this,getResources().getString(R.string.file_notexist_bad),Toast.LENGTH_SHORT).show();
                    //                        this.finish();
                }

            }
        }
        if (videoType == RECODE_VIDEO) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        switch (mCurrentState) {
            case CURRENT_STATE_PAUSE:
            case CURRENT_STATE_AUTO_COMPLETE:
                break;
            default:
                jcVideoPlayerStandard.startButton.performClick();
                break;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        this.mCurrentState = jcVideoPlayerStandard.getPlayStatus();
        if (null != jcVideoPlayerStandard && null != jcVideoPlayerStandard.startButton && mCurrentState == CURRENT_STATE_PLAYING) {
            jcVideoPlayerStandard.startButton.performClick();
        }
    }

    private long getFileSize(File file) {
        long size = 0;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            size = fis.available();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return size;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAIUIEvent(AIUIEvent event) {
        switch (event.getTag()) {
            case EventsConstants.AIUIEvents.AIUI_EVENT_FORCE_SLEEP:
                finish();
            case EventsConstants.AIUIEvents.AIUI_EVENT_WAKEUP:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (ibus != null) {
            ibus.unregister(this);
        }
        super.onDestroy();
    }

    @Override
    public void onPrepared() {

    }

    @Override
    public void onAutoCompletion() {

    }

    @Override
    public void onCompletion() {

    }

    @Override
    public void onBufferingUpdate(int percent) {

    }

    @Override
    public void onSeekComplete() {

    }

    @Override
    public void onError(int what, int extra) {
        switch (what) {
        }
    }

    @Override
    public void onInfo(int what, int extra) {

    }

    @Override
    public void onVideoSizeChanged() {

    }

    @Override
    public void onBackFullscreen() {

    }
}
