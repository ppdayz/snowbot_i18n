package com.csjbot.snowbot.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.FaceDetector;
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

import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.utils.CamParaUtil;
import com.csjbot.snowbot_rogue.camera.ui.FaceView;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.csjbot.snowbot_rogue.utils.WeakReferenceHandler;
import com.orhanobut.logger.Logger;

import net.steamcrafted.loadtoast.LoadToast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import at.markushi.ui.CircleButton;
import butterknife.BindView;

/**
 * Copyright (c) 2016, SuZhou CsjBot. All Rights Reserved.
 * www.csjbot.com
 * <p>
 * Created by 浦耀宗 at 2016/12/18 0018-14:32.
 * Email: puyz@csjbot.com
 */
public class VideoRecordActivity1 extends CsjUIActivity implements OnClickListener, Camera.PreviewCallback {
    private static final int CAMERA_ID = CameraInfo.CAMERA_FACING_BACK;
    private static final int MAX_FACE_NUM = 10;//最大可以检测出的人脸数量
    public static final int UPDATE_FACE_RECT = 0;

    private Camera.Size mPreviewSize = null;
    private CircleButton startButton;
    private LoadToast loadToast = null;
    private MediaRecorder mediaRecorder;
    private Paint localPaint1;
    private Paint localPaint2;
    private String currentFileName = "";
    private SurfaceHolder mSurfaceHolder;
    private SurfaceView mSurfaceView;

    @BindView(R.id.faceView)
    FaceView faceView;

    private VideoRecordActivityHandler mHandler = new VideoRecordActivityHandler(this);

    private boolean mIsRecording = false;
    private byte[] mPreBuffer = null;
    private int orientionOfCamera;
    private int recordTime = 0;
    private int autoTakePhotoCD = 3;

    private Camera mCamera;

    private static class VideoRecordActivityHandler extends WeakReferenceHandler<VideoRecordActivity1> {
        public VideoRecordActivityHandler(VideoRecordActivity1 reference) {
            super(reference);
        }

        @Override
        protected void handleMessage(VideoRecordActivity1 reference, Message msg) {
            switch (msg.what) {
                case UPDATE_FACE_RECT:
                    FaceDetector.Face[] faces = (FaceDetector.Face[]) msg.obj;
                    int faceCount = msg.arg1;
                    reference.faceView.setFaces(faces);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        setupBack(Color.WHITE);
        loadToast = new LoadToast(this);
        loadToast.setText("录制中");

        final boolean autoTakePhoto = getIntent().getBooleanExtra("autoTakePhoto", false);
        final boolean autoRecord = getIntent().getBooleanExtra("autoRecord", false);

        localPaint1 = new Paint();
        localPaint2 = new Paint();
        localPaint1.setDither(true);
        localPaint2.setColor(-65536);
        localPaint2.setStyle(Paint.Style.STROKE);
        localPaint2.setStrokeWidth(2.0F);


        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                releaseCamera();
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initPreview();

                if (autoTakePhoto) {
                    autoTakePhoto();
                }

                if (autoRecord) {
                    loadToast.setText("准备录制");
                    loadToast.show();

                    mHandler.postDelayed(() -> startmediaRecorder(), 1000);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }
        });

        startButton = (CircleButton) findViewById(R.id.start);
        startButton.setOnClickListener(this);
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_videorecord;
    }

    /**
     * Called as preview frames are displayed.  This callback is invoked
     * on the event thread {@link #open(int)} was called from.
     * <p>
     * <p>If using the {@link ImageFormat#YV12} format,
     * refer to the equations in {@link Camera.Parameters#setPreviewFormat}
     * for the arrangement of the pixel data in the preview callback
     * buffers.
     *
     * @param data   the contents of the preview frame in the format defined
     *               by {@link ImageFormat}, which can be queried
     *               with {@link Camera.Parameters#getPreviewFormat()}.
     *               If {@link Camera.Parameters#setPreviewFormat(int)}
     *               is never called, the default will be the YCbCr_420_SP
     *               (NV21) format.
     * @param camera the Camera service object.
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mPreBuffer == null) {
            int size = mPreviewSize.width * mPreviewSize.height *
                    ImageFormat.getBitsPerPixel(mParams.getPreviewFormat()) / 8;
            mPreBuffer = new byte[size];
        }
        mCamera.addCallbackBuffer(mPreBuffer);

        Camera.Size localSize = camera.getParameters().getPreviewSize();  //获得预览分辨率
        YuvImage localYuvImage = new YuvImage(mPreBuffer, ImageFormat.NV21, localSize.width, localSize.height, null);
        ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
        //把摄像头回调数据转成YUV，再按图像尺寸压缩成JPEG，从输出流中转成数组
        localYuvImage.compressToJpeg(new Rect(0, 0, localSize.width, localSize.height), 80, localByteArrayOutputStream);
        byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
//        cameraRelease();   //及早释放camera资源，避免影响camera设备的正常调用
        StoreByteImage(arrayOfByte);
    }


    private void StoreByteImage(byte[] paramArrayOfByte) {
        Bitmap localBitmap1 = BitmapFactory.decodeByteArray(paramArrayOfByte, 0, paramArrayOfByte.length);
        int i = localBitmap1.getWidth();
        int j = localBitmap1.getHeight();   //从上步解出的JPEG数组中接出BMP，即RAW->JPEG->BMP
        Matrix localMatrix = new Matrix();

        Bitmap localBitmap2 = null;
        FaceDetector localFaceDetector = null;

        //根据前置安装旋转的角度来重新构造BMP
        switch (orientionOfCamera) {
            case 0:
                localFaceDetector = new FaceDetector(i, j, MAX_FACE_NUM);
                localMatrix.postRotate(0.0F, i / 2, j / 2);
                localBitmap2 = Bitmap.createBitmap(i, j, Bitmap.Config.RGB_565);

                break;
            case 90:
                localFaceDetector = new FaceDetector(j, i, MAX_FACE_NUM);   //长宽互换
                localMatrix.postRotate(-270.0F, j / 2, i / 2);  //正90度的话就反方向转270度，一样效果
                localBitmap2 = Bitmap.createBitmap(i, j, Bitmap.Config.RGB_565);
                break;
            case 180:
                localFaceDetector = new FaceDetector(i, j, MAX_FACE_NUM);
                localMatrix.postRotate(-180.0F, i / 2, j / 2);
                localBitmap2 = Bitmap.createBitmap(i, j, Bitmap.Config.RGB_565);
                break;
            case 270:
                localFaceDetector = new FaceDetector(j, i, MAX_FACE_NUM);
                localMatrix.postRotate(-90.0F, j / 2, i / 2);
                localBitmap2 = Bitmap.createBitmap(j, i, Bitmap.Config.RGB_565);  //localBitmap2应是没有数据的
                break;
            default:
                break;
        }

        FaceDetector.Face[] arrayOfFace = new FaceDetector.Face[MAX_FACE_NUM];

        Canvas localCanvas = new Canvas();
        localCanvas.setBitmap(localBitmap2);
        localCanvas.setMatrix(localMatrix);
        localCanvas.drawBitmap(localBitmap1, 0.0F, 0.0F, localPaint1); //该处将localBitmap1和localBitmap2关联（可不要？）

        if (localBitmap2 == null) {
            return;
        }

        int numberOfFaceDetected = localFaceDetector.findFaces(localBitmap2, arrayOfFace);
        localBitmap2.recycle();
        localBitmap1.recycle();   //释放位图资源

        Message msg = mHandler.obtainMessage();
        msg.what = UPDATE_FACE_RECT;
        msg.arg1 = numberOfFaceDetected;
        msg.obj = arrayOfFace;
        mHandler.sendMessage(msg);
//        if (faceDetectorListener != null) {
//            faceDetectorListener.onFaceDetected(arrayOfFace, numberOfFaceDetected);
//        }
//        FaceDetectDeal(numberOfFaceDetected);
    }

    private Runnable autoTakePhotoRunnable = new Runnable() {
        @Override
        public void run() {
            autoTakePhotoCD--;
            loadToast.setText(String.valueOf(autoTakePhotoCD));

            if (autoTakePhotoCD == 0) {
                autoTakePhotoCD = 3;
                // TODO 这里加个声音
                mCamera.takePicture(null, null, null, (data, camera) -> new Thread(() -> {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    try {
                        saveFile(bitmap, Constant.SDCARD_IMAGE_PATH + System.currentTimeMillis() + ".jpg");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start());
                loadToast.success();
                mHandler.postDelayed(() -> onBackPressed(), 1000);
            } else {
                mHandler.postDelayed(this, 1000);
            }
        }
    };

    private void autoTakePhoto() {
        if (mCamera != null) {
            loadToast.setText(String.valueOf(autoTakePhotoCD));
            mHandler.postDelayed(autoTakePhotoRunnable, 1000);
            loadToast.show();
        } else {
            loadToast.setText("摄像机打开错误");

            loadToast.show();
            mHandler.postDelayed(() -> loadToast.error(), 1000);
        }
    }

    protected void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera.Parameters mParams;

    private void setupCameraParam() {
        mParams = mCamera.getParameters();
//            mParams.setPictureFormat(ImageFormat.JPEG);//设置拍照后存储的图片格式
        CamParaUtil.getInstance().printSupportPictureSize(mParams);
        CamParaUtil.getInstance().printSupportPreviewSize(mParams);
//            //设置PreviewSize和PictureSize
        Camera.Size pictureSize = CamParaUtil.getInstance().getPropPictureSize(
                mParams.getSupportedPictureSizes(), -1f, 640);
        mParams.setPictureSize(pictureSize.width, pictureSize.height);
        Camera.Size previewSize = CamParaUtil.getInstance().getPropPreviewSize(
                mParams.getSupportedPreviewSizes(), -1f, 640);
        mParams.setPreviewSize(previewSize.width, previewSize.height);

        // TODO 提高fps
        mParams.setRecordingHint(true);
        mParams.setAutoExposureLock(true);
        mParams.setAutoWhiteBalanceLock(true);

        setCameraDisplayOrientation(context, CAMERA_ID, mCamera);

        CamParaUtil.getInstance().printSupportFocusMode(mParams);
//            List<String> focusModes = mParams.getSupportedFocusModes();
//            if (focusModes.contains("continuous-video")) {
//                mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
//            }
        mCamera.setParameters(mParams);
    }

    protected void initPreview() {
        mCamera = Camera.open(CAMERA_ID);
        setupCameraParam();
        mCamera.setAutoFocusMoveCallback(new Camera.AutoFocusMoveCallback() {
            @Override
            public void onAutoFocusMoving(boolean start, Camera camera) {
                if (start) {
                    mCamera.setOneShotPreviewCallback(null);
                    Csjlogger.debug("自动聚焦成功");
                }
            }
        });

        mPreviewSize = mParams.getPreviewSize();
//            mCamera.setPreviewCallback(this);
//            mCamera.setPreviewCallbackWithBuffer(this);
        if (mPreBuffer == null) {
            int size = mPreviewSize.width * mPreviewSize.height *
                    ImageFormat.getBitsPerPixel(mParams.getPreviewFormat()) / 8;
            mPreBuffer = new byte[size];
        }
        mCamera.addCallbackBuffer(mPreBuffer);
        mCamera.setPreviewCallbackWithBuffer(this);

        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setCameraDisplayOrientation(Context contex, int paramInt, Camera paramCamera) {
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(paramInt, info);
        int rotation = ((WindowManager) contex.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();  //获得显示器件角度
        int degrees = 0;
        Csjlogger.debug("getRotation's rotation is " + String.valueOf(rotation));
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

        orientionOfCamera = info.orientation;      //获得摄像头的安装旋转角度
        int result;
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
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
        stopMediaRecorder();
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.start:
                if (mIsRecording == false) {
                    startmediaRecorder();
                } else {
                    stopMediaRecorder();
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

    private void stopMediaRecorder() {
        if (mediaRecorder != null) {
            if (mIsRecording) {
                mediaRecorder.stop();
                //mCamera.lock();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
                mIsRecording = false;
                try {
                    mCamera.reconnect();
                } catch (IOException e) {
                    Toast.makeText(this, "reconect fail", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
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
        File myCaptureFile = new File(fileName);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        bos.flush();
        bos.close();
    }

    private void startmediaRecorder() {
        mCamera.unlock();
        mIsRecording = true;
        mediaRecorder = new MediaRecorder();
        mediaRecorder.reset();
        mediaRecorder.setCamera(mCamera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        mediaRecorder.setOutputFile(getName());
        mediaRecorder.setVideoFrameRate(10);
        mediaRecorder.setVideoSize(640, 480);
//        CamcorderProfile mCamcorderProfile = CamcorderProfile.get(CameraInfo.CAMERA_FACING_BACK, CamcorderProfile.QUALITY_LOW);
//        mediaRecorder.setProfile(mCamcorderProfile);
        mediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();

        } catch (Exception e) {

            mIsRecording = false;
            Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            mCamera.lock();
        }
        startRecordHandler();
        loadToast.show();
        mCamera.takePicture(null, null, null, (data, camera) -> new Thread(() -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            try {
                saveFile(bitmap, currentFileName + ".jpg");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start());
    }

    @Override
    public void onBackPressed() {
        if (mediaRecorder != null) {
            if (mIsRecording) {
                loadToast.success();

                mHandler.postDelayed(() -> VideoRecordActivity1.super.onBackPressed(), 200);
            }
        }
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
