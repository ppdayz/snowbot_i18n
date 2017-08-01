package com.csjbot.snowbot.activity;

import android.hardware.Camera;
import android.os.Bundle;
import android.os.Message;
import android.view.SurfaceView;
import android.view.View;

import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.face.base.BaseApplication;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.utils.CommonTool;
import com.csjbot.snowbot.utils.SpeechStatus;
import com.csjbot.snowbot_rogue.utils.CSJToast;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.csjbot.snowbot_rogue.utils.WeakReferenceHandler;

import net.steamcrafted.loadtoast.LoadToast;

import java.io.File;
import java.util.Locale;

import at.markushi.ui.CircleButton;
import dou.helper.CameraHelper;
import dou.helper.CameraParams;
import dou.utils.DeviceUtil;

public class VideoRecordActivity2 extends CsjUIActivity implements View.OnClickListener {
    private SurfaceView camera_view;
    protected CameraHelper mCameraHelper;
    protected int camera_max_width = 640;
    private String currentFileName = "";
    private CircleButton startButton;
    private boolean mIsRecording;
    private LoadToast loadToast = null;
    private int recordTime = 0;

    private VideoRecordActivity2Handler mHandler = new VideoRecordActivity2Handler(this);

    private class VideoRecordActivity2Handler extends WeakReferenceHandler<VideoRecordActivity2> {
        public VideoRecordActivity2Handler(VideoRecordActivity2 reference) {
            super(reference);
        }

        @Override
        protected void handleMessage(VideoRecordActivity2 reference, Message msg) {

        }
    }

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        startButton = (CircleButton) findViewById(R.id.start);
        startButton.setOnClickListener(this);
        initCamera();
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_video_record2;
    }

    public void initCamera() {
        camera_view = (SurfaceView) findViewById(R.id.video_record_surfaceview);
        //预设Camera参数，方便扩充
        CameraParams params = new CameraParams();
        //优先使用的camera Id,
        params.firstCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        params.surfaceView = camera_view;
        params.preview_width = camera_max_width;
        params.preview_width = 960;
        params.preview_height = 540;
//        params.pre_rate = 7;
        // 若强行设置此值，CameraHelper中camera.setDisplayOrientation(result);失效
        // 对于机器人用户横屏状态建议设置，手机用户不需要设置
        params.camera_ori = 0;
        if (DeviceUtil.getModel().equals("Nexus 6")) {
            params.camera_ori_front = 180;
            BaseApplication.reverse_180_front = true;
        }

        mCameraHelper = new CameraHelper(this, params);
//        mCameraHelper.startPreview();
    }

    public void createFolder(String folderPath) {
        File file = new File(folderPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    private String getName() {
        createFolder(Constant.SDCARD_VIDEO_PATH);
        // 如 {SDCARD_PATH}/csjbot/video/1472475030.3pg
        currentFileName = Constant.SDCARD_VIDEO_PATH + System.currentTimeMillis();
        String fileName = currentFileName + ".3gp";
        return fileName;
    }

    private Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            recordTime++;
            loadToast.setText(String.format(Locale.getDefault(), "%02d : %02d", recordTime / 60, recordTime % 60));
            mHandler.postDelayed(this, 1000);
        }
    };

    private void startRecordHandler() {
        mHandler.removeCallbacks(recordRunnable);
        mHandler.post(recordRunnable);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.start:
                if (CommonTool.isFastDoubleClick(1500)) {
                    CSJToast.showToast(this, "请您不要操作过快!", 2000);
                    return;
                }




                if (!mIsRecording) {
                    SpeechStatus.getIstance().setAiuiResponse(false);
                    startmediaRecorder();
                } else {
                    SpeechStatus.getIstance().setAiuiResponse(true);
                    stopmediaRecorder();
                }

                if (mIsRecording) {
                    startButton.setImageResource(R.mipmap.stop_record);
                } else {
                    startButton.setImageResource(R.mipmap.start_record);
                }

                break;
            default:
                break;
        }
    }

    private void stopmediaRecorder() {
        mCameraHelper.stopRecord();
    }

    private void startmediaRecorder() {
        try {
            mCameraHelper.startRecord(getName());
            mIsRecording = true;
        } catch (Exception e) {
            mIsRecording = false;
            Csjlogger.error("error", e);
        }

        if (mIsRecording) {
            startRecordHandler();
            loadToast.show();
        }
    }
}
