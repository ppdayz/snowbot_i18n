package com.csjbot.snowbot.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.face.base.BaseCameraActivity;
import com.csjbot.snowbot.utils.CommonTool;
import com.csjbot.snowbot.utils.FileUtil;
import com.csjbot.snowbot_rogue.utils.CSJToast;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.csjbot.snowbot_rogue.utils.WeakReferenceHandler;

import net.steamcrafted.loadtoast.LoadToast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import at.markushi.ui.CircleButton;
import mobile.ReadFace.YMFace;

public class VideoRecordActivity3 extends BaseCameraActivity implements View.OnClickListener {
    protected int camera_max_width = 640;
    private String currentFileName = "";
    private CircleButton startButton;
    private boolean mIsRecording;
    private LoadToast loadToast = null;
    private int recordTime = 0;
    private SurfaceHolder mHolder;
    private VideoRecordActivity3Handler mHandler = new VideoRecordActivity3Handler(this);
    private Camera mCamera;
    private MediaRecorder mediaRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record3);
        setCamera_max_width(1080);
        initView();
        initCamera();


        startButton = (CircleButton) findViewById(R.id.start);
        startButton.setOnClickListener(this);
    }

    @Override
    public void initView() {

    }

    private static class VideoRecordActivity3Handler extends WeakReferenceHandler<VideoRecordActivity3> {
        public VideoRecordActivity3Handler(VideoRecordActivity3 reference) {
            super(reference);
        }

        @Override
        protected void handleMessage(VideoRecordActivity3 reference, Message msg) {

        }
    }


    @Override
    protected void drawAnim(List<YMFace> faces, SurfaceView draw_view, float scale_bit, int cameraId, String fps) {

    }

    @Override
    protected List<YMFace> analyse(byte[] bytes, int iw, int ih) {
        return null;
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



//                if (!mIsRecording) {
//                    SpeechStatus.getIstance().setAiuiResponse(false);
//                    startmediaRecorder();
//                } else {
//                    SpeechStatus.getIstance().setAiuiResponse(true);
//                    stopmediaRecorder();
//                }
//
//                if (mIsRecording) {
//                    startButton.setImageResource(R.mipmap.stop_record);
//                } else {
//                    startButton.setImageResource(R.mipmap.start_record);
//                }

                break;
            default:
                break;
        }
    }

    private void startmediaRecorder() {
        mHolder = mCameraHelper.getSurfaceHolder();
        mCamera = mCameraHelper.getCamera();
        mCamera.unlock();

        if (mediaRecorder == null) {
            Csjlogger.debug("initVideoRecord");
            mIsRecording = true;

            CamcorderProfile mProfile = CamcorderProfile.get(0
                    , CamcorderProfile.QUALITY_LOW);

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
            mediaRecorder.setVideoSize(mProfile.videoFrameWidth, mProfile.videoFrameHeight);
            mediaRecorder.setVideoFrameRate(mProfile.videoFrameRate);
            mediaRecorder.setVideoEncodingBitRate(mProfile.videoBitRate);
            mediaRecorder.setAudioEncodingBitRate(mProfile.audioBitRate);
            mediaRecorder.setAudioChannels(mProfile.audioChannels);
            mediaRecorder.setAudioSamplingRate(mProfile.audioSampleRate);

            mediaRecorder.setPreviewDisplay(mHolder.getSurface());
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
//            loadToast.show();
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
            CSJToast.showToast(VideoRecordActivity3.this, getResources().getString(R.string.insufficient_memory));
            stopmediaRecorder();
        }
    }

    private void stopmediaRecorder() {
        if (mediaRecorder != null) {
            if (mIsRecording) {
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
//                loadToast.success();
                mHandler.removeCallbacks(recordRunnable);
                recordTime = 0;
            }
        }
    }
}
