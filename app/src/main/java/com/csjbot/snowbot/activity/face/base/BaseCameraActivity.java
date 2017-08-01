package com.csjbot.snowbot.activity.face.base;

import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.csjbot.snowbot.R;

import java.util.ArrayList;
import java.util.List;

import dou.helper.CameraHelper;
import dou.helper.CameraParams;
import dou.utils.DLog;
import dou.utils.DeviceUtil;
import mobile.ReadFace.YMFace;
import mobile.ReadFace.YMFaceTrack;

/**
 * Created by mac on 16/7/13.
 */
public abstract class BaseCameraActivity extends BaseActivity implements CameraHelper.PreviewFrameListener {
    private SurfaceView camera_view;
    private SurfaceView draw_view;
    protected CameraHelper mCameraHelper;
    protected YMFaceTrack faceTrack;

    protected int iw = 0, ih;
    private float scale_bit;
    private boolean showFps = false;
    private List<Float> timeList = new ArrayList<>();
    protected boolean stop = false;
    //camera_max_width值为-1时, 找大于640分辨率为屏幕宽高等比
    protected int camera_max_width = 640;

    private boolean inThread = false;//两种从摄像头拿数据方式


    public void initCamera() {
        camera_view = (SurfaceView) findViewById(R.id.camera_preview);
        draw_view = (SurfaceView) findViewById(R.id.pointView);
        draw_view.setZOrderOnTop(true);
        draw_view.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        //预设Camera参数，方便扩充
        CameraParams params = new CameraParams();
        //优先使用的camera Id,
        params.firstCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
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

        params.previewFrameListener = this;
        mCameraHelper = new CameraHelper(this, params);
    }

    public synchronized void stopTrack() {

        if (faceTrack == null) {
            DLog.d("already release track");
            return;
        }
        stop = true;
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread = null;
        }
        faceTrack.onRelease();
        faceTrack = null;
        DLog.d("release track success");
    }


    public synchronized void startTrack() {
        if (faceTrack != null) {
            DLog.d("already init track");
            return;
        }

        stop = false;
        mContext = this;
        iw = 0;//重新调用initCameraMsg的开关
        faceTrack = new YMFaceTrack();

        /**此处默认初始化，initCameraMsg()处会根据设备设置自动更改设置
         *人脸识别数据库之前保存在应用目录的cache目录下，可以通过另一个初始化检测器的函数
         *public boolean initTrack(Context mContext, int orientation, int resizeScale, String db_dir)
         *通过指定保存db的目录来自定义
         **/
        faceTrack.initTrack(this, YMFaceTrack.FACE_0, YMFaceTrack.RESIZE_WIDTH_640);

        if (inThread) {
            if (thread != null) {
                stop = true;
                try {
                    thread.join();
                    thread = null;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                stop = false;
            }
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!stop) {
                        if (!isNV21ready) {
//                            try {
//                                Thread.sleep(50);//功耗降低
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                                DLog.d("Interrupted to die");
//                                break;
//                            }
                            continue;
                        }
                        synchronized (nv21) {
                            System.arraycopy(nv21, 0, temp, 0, nv21.length);
                            isNV21ready = false;
                        }
                        runTrack(temp);
                    }
                }
            });
            thread.start();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        startTrack();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTrack();
    }


    private byte nv21[];
    private byte temp[];
    private boolean isNV21ready = false;
    private Thread thread;

    int camera_fps;
    int camera_count;
    long camera_long = 0;

    @Override
    public void onPreviewFrame(final byte[] bytes, Camera camera) {
        if (camera_long == 0) camera_long = System.currentTimeMillis();
        camera_count++;
        if (System.currentTimeMillis() - camera_long > 1000) {
            camera_fps = camera_count;
            camera_count = 0;
            camera_long = 0;
        }
        initCameraMsg();
        if (!stop) {
            if (inThread) {
                synchronized (nv21) {
                    System.arraycopy(bytes, 0, nv21, 0, bytes.length);
                    isNV21ready = true;
                }
            } else {
                runTrack(bytes);
            }
        }
    }


    protected abstract void drawAnim(List<YMFace> faces, SurfaceView draw_view, float scale_bit, int cameraId, String fps);

    protected abstract List<YMFace> analyse(byte[] bytes, int iw, int ih);

    private void initCameraMsg() {
        if (iw == 0) {

            int surface_w = camera_view.getLayoutParams().width;
            int surface_h = camera_view.getLayoutParams().height;

            iw = mCameraHelper.getPreviewSize().width;
            ih = mCameraHelper.getPreviewSize().height;
            nv21 = new byte[iw * ih * 2];
            temp = new byte[iw * ih * 2];
            int orientation = 0;
            ////注意横屏竖屏问题
            DLog.d(getResources().getConfiguration().orientation + " : " + Configuration.ORIENTATION_PORTRAIT);

            if (sw < sh) {
                scale_bit = surface_w / (float) ih;
                if (mCameraHelper.getCameraId() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    orientation = YMFaceTrack.FACE_270;
                } else {
                    orientation = YMFaceTrack.FACE_90;
                }
            } else {
                scale_bit = surface_h / (float) ih;
                orientation = YMFaceTrack.FACE_0;

                if (mCameraHelper.getCameraId() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    if (BaseApplication.reverse_180_front) {
                        orientation += 180;
                    }
                }
            }

            if (faceTrack == null) {
                iw = 0;
                return;
            }

            faceTrack.setOrientation(orientation);
            ViewGroup.LayoutParams params = draw_view.getLayoutParams();
            params.width = surface_w;
            params.height = surface_h;
            draw_view.requestLayout();

        }
    }


    public int getCameraId() {
        return mCameraHelper.getCameraId();
    }

    public int switchCamera() {
        int result = mCameraHelper.switchCameraId();
        iw = 0;
        return result;
    }

    public void stopCamera() {
        mCameraHelper.stopCamera();
    }

    public void stopPreview() {
        mCameraHelper.stopPreview();
    }

    public void startPreview() {
        mCameraHelper.startPreview();
    }

    protected void showFps(boolean show) {
        showFps = show;
    }

    protected int getDoomW(int tar) {
        if (sw >= 1080) return tar;
        return sw * tar / 1080;
    }

    public void setCamera_max_width(int width) {
        camera_max_width = width;
    }

    public void runTrack(byte[] data) {
        try {
            long time = System.currentTimeMillis();
            final List<YMFace> faces = analyse(data, iw, ih);
            String str = "";
            StringBuilder fps = new StringBuilder();
            if (showFps) {
                fps.append("fps = ");
                long now = System.currentTimeMillis();
                float than = now - time;
                timeList.add(than);
                if (timeList.size() >= 20) {
                    float sum = 0;
                    for (int i = 0; i < timeList.size(); i++) {
                        sum += timeList.get(i);
                    }
                    fps.append(String.valueOf((int) (1000f * timeList.size() / sum)))
                            .append(" camera ")
                            .append(camera_fps);
                    timeList.remove(0);
                }
            }
            final String fps1 = fps.toString() + str;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    drawAnim(faces, draw_view, scale_bit, getCameraId(), fps1);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
