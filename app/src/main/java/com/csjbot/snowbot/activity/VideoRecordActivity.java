package com.csjbot.snowbot.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Message;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.core.util.SharedUtil;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.bean.aiui.entity.CsjSynthesizerListener;
import com.csjbot.snowbot.services.CsjSpeechSynthesizer2;
import com.csjbot.snowbot.utils.CommonTool;
import com.csjbot.snowbot.utils.FileUtil;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot.utils.SpeechStatus;
import com.csjbot.snowbot_rogue.camera.CameraInterface;
import com.csjbot.snowbot_rogue.utils.CSJToast;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.csjbot.snowbot_rogue.utils.WeakReferenceHandler;
import com.iflytek.cloud.SpeechError;

import net.steamcrafted.loadtoast.LoadToast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import at.markushi.ui.CircleButton;

/**
 * RK3288 OK
 */
public class VideoRecordActivity extends CsjUIActivity implements OnClickListener {
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private CircleButton startButton;
    private boolean mIsRecording = false;
    private MediaRecorder mediaRecorder;
    private LoadToast loadToast = null;
    private int recordTime = 0;
    private VideoRecordActivityHandler mHandler = new VideoRecordActivityHandler(this);
    private String currentFileName = "";
    private int autoTakePhotoCD = 5;
    private boolean isInAutoTakePhoto = false;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        final boolean autoTakePhoto = intent.getBooleanExtra("autoTakePhoto", false);
        if (autoTakePhoto) {
            SpeechStatus.getIstance().setAiuiResponse(false);
            autoTakePhoto();
        }
    }

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        setupBack(Color.WHITE);
        loadToast = new LoadToast(this);
        loadToast.setText("录制中");

        final boolean autoTakePhoto = getIntent().getBooleanExtra("autoTakePhoto", false);
        final boolean autoRecord = getIntent().getBooleanExtra("autoRecord", false);

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        mSurfaceHolder = mSurfaceView.getHolder();

        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
//                releaseCamera();
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initpreview();

                if (autoTakePhoto) {
                    SpeechStatus.getIstance().setAiuiResponse(false);
                    autoTakePhoto();
                }

                if (autoRecord) {
                    loadToast.setText("准备录制111");
                    loadToast.show();

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startmediaRecorder();
                        }
                    }, 1000);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }
        });

        startButton = (CircleButton) findViewById(R.id.start);
        if (autoTakePhoto) {
            findViewById(R.id.start_btn_bg).setVisibility(View.GONE);
            startButton.setVisibility(View.GONE);
        }
        startButton.setOnClickListener(this);
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_videorecord;
    }

    private class VideoRecordActivityHandler extends WeakReferenceHandler<VideoRecordActivity> {
        public VideoRecordActivityHandler(VideoRecordActivity reference) {
            super(reference);
        }

        @Override
        protected void handleMessage(VideoRecordActivity reference, Message msg) {

        }
    }


    private Runnable autoTakePhotoRunnable = new Runnable() {
        @Override
        public void run() {
            autoTakePhotoCD--;
            loadToast.setText(String.valueOf(autoTakePhotoCD));

            if (mCamera != null & autoTakePhotoCD == 0) {
                autoTakePhotoCD = 3;
                // TODO 这里加个声音
                mCamera.takePicture(null, null, null, new Camera.PictureCallback() {

                    /**
                     * Called when image data is available after a picture is taken.
                     * The format of the data depends on the context of the callback
                     * and {@link Camera.Parameters} settings.
                     *
                     * @param data   a byte array of the picture data
                     * @param camera the Camera service object
                     */
                    @Override
                    public void onPictureTaken(final byte[] data, Camera camera) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                                try {
                                    createFolder(Constant.SDCARD_VIDEO_PATH);
                                    saveFile(bitmap, Constant.SDCARD_VIDEO_PATH + System.currentTimeMillis() + ".jpg");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                });
                loadToast.success();

//                mHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        isInAutoTakePhoto = false;
//                        ClientService.setIsAutoTakePhoto(Boolean.FALSE);
//                        SharedUtil.setPreferInt(SharedKey.AIUISERVICESWITCH, 1);
//                        onBackPressed();
//                    }
//                }, 1000);
            } else if (mCamera != null) {
                mHandler.postDelayed(this, 1000);
            } else {
                autoTakePhotoCD = 3;
                mHandler.removeCallbacks(autoTakePhotoRunnable);
            }
        }
    };

    private void autoTakePhoto() {
        if (mCamera != null) {
//            ClientService.setIsAutoTakePhoto(true);
            SharedUtil.setPreferInt(SharedKey.AIUISERVICESWITCH, 0);
            isInAutoTakePhoto = true;
//            EventBus.getDefault().post(new RobotStatusUpdateEvent(15, false, 70));

            CsjSpeechSynthesizer2.getSynthesizer().startSpeaking("请摆个美美的pose，开始拍照", new CsjSynthesizerListener() {
                @Override
                public void onSpeakBegin() {
                    loadToast.setText(String.valueOf(autoTakePhotoCD));
                    mHandler.postDelayed(autoTakePhotoRunnable, 1000);
                    loadToast.show();
                    SpeechStatus.getIstance().setSpeakFinished(false);
                }

                @Override
                public void onCompleted(SpeechError speechError) {
                    SpeechStatus.getIstance().setSpeakFinished(true);
                }
            });
        } else {
            loadToast.setText("摄像机打开错误");
            loadToast.show();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadToast.error();
                }
            }, 1000);
        }
    }


    protected void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpeechStatus.getIstance().setAiuiResponse(true);
    }

    protected void initpreview() {
        mCamera = CameraInterface.getInstance().getCameraDevice();
//        try {
//            mCamera = Camera.open(CameraInfo.CAMERA_FACING_FRONT);
//            mCamera.setPreviewDisplay(mSurfaceHolder);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        if (mCamera != null) {
//            setCameraDisplayOrientation(this, CameraInfo.CAMERA_FACING_FRONT, mCamera);

//            mCamera.setAutoFocusMoveCallback(new Camera.AutoFocusMoveCallback() {
//                @Override
//                public void onAutoFocusMoving(boolean start, Camera camera) {
//                    if (start) {
//                        mCamera.setOneShotPreviewCallback(null);
//                        Csjlogger.debug("自动聚焦成功");
//                    }
//                }
//            });
//            mCamera.startPreview();
        } else {
            loadToast.setText("摄像机打开错误");
            loadToast.show();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadToast.error();
                }
            }, 1000);
        }
    }

    private void setCameraDisplayOrientation(Context contex, int paramInt, Camera paramCamera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(paramInt, info);
        int rotation = ((WindowManager) contex.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();  //获得显示器件角度
        int degrees = 0;
        Csjlogger.info("puyz", "getRotation's rotation is " + String.valueOf(rotation));
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

//        orientionOfCamera = info.orientation;      //获得摄像头的安装旋转角度
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        //注意前后置的处理，前置是映象画面，该段是SDK文档的标准DEMO
        paramCamera.setDisplayOrientation(result);
    }

    @Override
    protected void onPause() {
        stopmediaRecorder();
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.start:
                if (CommonTool.isFastDoubleClick(1500)) {
                    CSJToast.showToast(this, getString(R.string.pls_not_op_fast), 2000);
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
        if (mediaRecorder != null) {
            if (mIsRecording) {

                if(recordTime <= 1){
                    return;
                }

                try {
                    //下面三个参数必须加，不加的话会奔溃，在mediarecorder.stop();
                    //报错为：RuntimeException:stop failed
                    mediaRecorder.setOnErrorListener(null);
                    mediaRecorder.setOnInfoListener(null);
                    mediaRecorder.setPreviewDisplay(null);
                    mediaRecorder.stop();
                } catch (IllegalStateException e) {
                } catch (RuntimeException e) {
                }
                //mCamera.lock();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
                mIsRecording = false;

                if (mCamera != null) {
                    try {
                        mCamera.reconnect();
                    } catch (IOException e) {
                        Toast.makeText(this, "reconect fail", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
                loadToast.success();
                mHandler.removeCallbacks(recordRunnable);
                recordTime = 0;
            }
        }
    }

    /**
     * 保存文件
     *
     * @param bm
     * @param fileName
     * @throws IOException
     */
    public void saveFile(Bitmap bm, String fileName) throws IOException {
        if (FileUtil.getSDFreeSize() > 10) {
            File myCaptureFile = new File(fileName);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
            bm.compress(Bitmap.CompressFormat.JPEG, 20, bos);
            bos.flush();
            bos.close();
        } else {
            CSJToast.showToast(VideoRecordActivity.this, getResources().getString(R.string.insufficient_memory));
            stopmediaRecorder();
        }
    }

    private void startmediaRecorder() {
        mCamera.unlock();

        if (mediaRecorder == null) {
            Csjlogger.debug("initVideoRecord");
            mIsRecording = true;

            CamcorderProfile mProfile = CamcorderProfile.get(CameraInterface.getInstance().getCameraId()
                    , CamcorderProfile.QUALITY_480P);

            //1st. Initial state
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setCamera(mCamera);

            //2st. Initialized state

            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

            //3st. config
            mediaRecorder.setOutputFormat(mProfile.fileFormat);
            mediaRecorder.setAudioEncoder(mProfile.audioCodec);
            mediaRecorder.setVideoEncoder(mProfile.videoCodec);
            mediaRecorder.setOutputFile(getName());
            mediaRecorder.setVideoSize(640, 480);
//            mediaRecorder.setVideoSize(mProfile.videoFrameWidth, mProfile.videoFrameHeight);
            mediaRecorder.setVideoFrameRate(mProfile.videoFrameRate);
            mediaRecorder.setVideoEncodingBitRate(mProfile.videoBitRate);
            mediaRecorder.setAudioEncodingBitRate(mProfile.audioBitRate);
            mediaRecorder.setAudioChannels(mProfile.audioChannels);
            mediaRecorder.setAudioSamplingRate(mProfile.audioSampleRate);

            mediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        }
//        mIsRecording = true;
//        mediaRecorder = new MediaRecorder();
//        mediaRecorder.reset();
//        mediaRecorder.setCamera(mCamera);
//        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
//        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
//        mediaRecorder.setOutputFile(getName());
//        mediaRecorder.setVideoFrameRate(10);
//        mediaRecorder.setVideoSize(640, 480);
////        CamcorderProfile mCamcorderProfile = CamcorderProfile.get(CameraInfo.CAMERA_FACING_BACK, CamcorderProfile.QUALITY_LOW);
////        mediaRecorder.setProfile(mCamcorderProfile);
//        mediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
//
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();

//			mediaRecorder.set
        } catch (Exception e) {

            mIsRecording = false;
            Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            mCamera.lock();
        }


        if (mIsRecording) {
            startRecordHandler();
            loadToast.show();
            mCamera.takePicture(null, null, null, new Camera.PictureCallback() {

                /**
                 * Called when image data is available after a picture is taken.
                 * The format of the data depends on the context of the callback
                 * and {@link Camera.Parameters} settings.
                 *
                 * @param data   a byte array of the picture data
                 * @param camera the Camera service object
                 */
                @Override
                public void onPictureTaken(final byte[] data, Camera camera) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                            try {
                                saveFile(bitmap, currentFileName + ".jpg");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (isInAutoTakePhoto) {
            return;
        }

        try {
            if (mediaRecorder != null) {
                if (recordTime <= 1) {
                    return;
                }

                mediaRecorder.stop();
                if (mIsRecording) {
                    loadToast.success();
                }
            }
        } catch (IllegalStateException e) {
            Csjlogger.error("error", e);
        }

        mHandler.postDelayed(VideoRecordActivity.super::onBackPressed, 200);
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

}
