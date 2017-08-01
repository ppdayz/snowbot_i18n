package com.csjbot.snowbot.activity.aiui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.MediaController;
import android.widget.VideoView;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot_rogue.utils.Constant;


public class VideoActivity extends Activity implements MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private VideoView videoContent = null;
    private Uri mUri;
    private MediaController mMediaController;
    private int mPositionWhenPaused = -1;
    private LocalBroadcastManager localBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

//        String url = getIntent().getStringExtra("url");
//        mUri = Uri.parse(url);
        String url = Environment.getExternalStorageDirectory().getPath() + "/fl1234.mp4";
        mUri = Uri.parse(url);
        videoContent = (VideoView) findViewById(R.id.videoView);

        //Create media controller
        mMediaController = new MediaController(this);
        videoContent.setMediaController(mMediaController);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(Constant.ACTION_CUSTOMER));
    }

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

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onPause();
            finish();
        }
    };

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
    protected void onDestroy() {
        localBroadcastManager.unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
}
