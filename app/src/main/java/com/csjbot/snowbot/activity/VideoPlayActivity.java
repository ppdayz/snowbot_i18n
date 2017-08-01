package com.csjbot.snowbot.activity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot_rogue.app.CsjSpeechSynthesizer;
import com.csjbot.snowbot_rogue.Events.AIUIEvent;
import com.csjbot.snowbot_rogue.Events.EventsConstants;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class VideoPlayActivity extends CsjUIActivity implements MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {
    private VideoView videoContent = null;
    private Uri mUri;
    private MediaController mMediaController;
    private int mPositionWhenPaused = -1;

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_video_play);
//        setupBack();
//
//        String url = getIntent().getStringExtra("url");
//        mUri = Uri.parse(url);
//
//        videoContent = (VideoView) findViewById(R.id.videoPlayContent);
//        //设置播放全屏
//        RelativeLayout.LayoutParams layoutParams =
//                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//        videoContent.setLayoutParams(layoutParams);
//
//        //Create media controller
//        mMediaController = new MediaController(this);
//        videoContent.setMediaController(mMediaController);
//    }

    public void onCompletion(MediaPlayer mp) {
        this.finish();
    }

    /**
     * Called to indicate an error.
     *
     * @param mp    the MediaPlayer the error pertains to
     * @param what  the type of error that has occurred:
     *              <ul>
     *              <li>{@link #MEDIA_ERROR_UNKNOWN}
     *              <li>{@link #MEDIA_ERROR_SERVER_DIED}
     *              </ul>
     * @param extra an extra code, specific to the error. Typically
     *              implementation dependent.
     *              <ul>
     *              <li>{@link #MEDIA_ERROR_IO}
     *              <li>{@link #MEDIA_ERROR_MALFORMED}
     *              <li>{@link #MEDIA_ERROR_UNSUPPORTED}
     *              <li>{@link #MEDIA_ERROR_TIMED_OUT}
     *              <li><code>MEDIA_ERROR_SYSTEM (-2147483648)</code> - low-level system error.
     *              </ul>
     * @return True if the method handled the error, false if it didn't.
     * Returning false, or not having an OnErrorListener at all, will
     * cause the OnCompletionListener to be called.
     */
    @Override
    public boolean onError(MediaPlayer player, int arg1, int arg2) {
        return false;
    }

    public void onStart() {
        // Play Video
        videoContent.setVideoURI(mUri);
        videoContent.start();

        super.onStart();
    }

    public void onPause() {
        // Stop video when the activity is pause.
        mPositionWhenPaused = videoContent.getCurrentPosition();
        videoContent.stopPlayback();

        super.onPause();
    }

    public void onResume() {
        // Resume video player
        if (mPositionWhenPaused >= 0) {
            videoContent.seekTo(mPositionWhenPaused);
            mPositionWhenPaused = -1;
        }

        super.onResume();
    }


    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        setupBack();
        String url = getIntent().getStringExtra("url");
        mUri = Uri.parse(url);
//        if (url != null) {
//            File file = new File(url);
//            if (file.exists()) {
//                long size = getFileSize(file);
//                if (size <= 0) {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                    builder.setMessage(getResources().getString(R.string.file_notexist_bad));
//                    builder.setNegativeButton(getResources()
//                            .getString(R.string.make_sure), (dialog, which)
//                            -> dialog.dismiss());
//
//                    AlertDialog dialog = builder.create();
//                    dialog.show();
//                }
//
//            }
//        }
        videoContent = (VideoView) findViewById(R.id.videoPlayContent);
        //设置播放全屏
        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        videoContent.setLayoutParams(layoutParams);

        //Create media controller
        mMediaController = new MediaController(this);
        videoContent.setMediaController(mMediaController);
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


    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_video_play;
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public boolean onAIUIEvent(AIUIEvent event) {
        switch (event.getTag()) {
            case EventsConstants.AIUIEvents.AIUI_EVENT_WAKEUP:
                finish();
                break;
            case EventsConstants.AIUIEvents.AIUI_EVENT_SUB_NLP:
                CsjSpeechSynthesizer.getSynthesizer().stopSpeaking();
                finish();
            case EventsConstants.AIUIEvents.AIUI_EVENT_FORCE_SLEEP:
                CsjSpeechSynthesizer.getSynthesizer().stopSpeaking();
                onPause();
                finish();
                break;
        }
        return false;
    }
}
