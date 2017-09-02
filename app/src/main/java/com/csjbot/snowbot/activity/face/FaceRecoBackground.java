package com.csjbot.snowbot.activity.face;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.android.core.util.SharedUtil;
import com.csjbot.csjbase.event.BusFactory;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.face.base.BaseApplication;
import com.csjbot.snowbot.activity.face.model.User;
import com.csjbot.snowbot.activity.face.util.BackGroundDrawUtils;
import com.csjbot.snowbot.bean.aiui.entity.CsjSynthesizerListener;
import com.csjbot.snowbot.services.CsjSpeechSynthesizer2;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot_rogue.Events.ExpressionEvent;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.iflytek.cloud.SpeechError;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dou.helper.CameraHelper;
import dou.helper.CameraParams;
import dou.utils.DLog;
import dou.utils.DeviceUtil;
import dou.utils.DisplayUtil;
import mobile.ReadFace.YMFace;
import mobile.ReadFace.YMFaceTrack;

/**
 * Created by xiasuhuei321 on 2017/6/15.
 * author:luo
 * e-mail:xiasuhuei321@163.com
 * desc:后台识别人脸
 */

public class FaceRecoBackground implements CameraHelper.PreviewFrameListener, BackGroundDrawUtils.FindRegisteredUserInterface {
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

    Context mContext;
    private int sh;
    private int sw;

    // 定义浮动窗口布局
    private LinearLayout mFloatLayout;
    private WindowManager.LayoutParams wmParams;
    // 创建浮动窗口设置布局参数的对象
    private WindowManager mWindowManager;
    private View contentView;

    private volatile boolean onSecondAfter = false;
    private boolean isSend = false;

    public void init(Context context) {
        this.mContext = context;
        sw = DisplayUtil.getScreenWidthPixels(mContext);
        sh = DisplayUtil.getScreenHeightPixels(mContext);
        // 在这里创建view和window
        BackGroundDrawUtils.setFindRegisteredUserInterface(this);

        contentView = LayoutInflater.from(mContext).inflate(R.layout.face_reco_2, null);

        // 设置悬浮窗的参数
        wmParams = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager) mContext.getSystemService(mContext.WINDOW_SERVICE);
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.TOP | Gravity.LEFT;

        // 设置长宽
//        wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
//        wmParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        wmParams.width = 1;
        wmParams.height = 1;


        contentView = LayoutInflater.from(mContext).inflate(R.layout.face_reco_2, null);

        mWindowManager.addView(contentView, wmParams);
        initCamera(contentView);
        showFps(true);
        /**
         * 初始化
         */
        SharedUtil.setPreferInt(SharedKey.AIUISERVICESWITCH, 0);

        if (trackingMap != null && trackingMap.size() != 0) {
            trackingMap.clear();
        }
        trackingMap = new SimpleArrayMap<>();
        if (trackingMapAttr != null && trackingMapAttr.size() != 0) {
            trackingMapAttr.clear();
        }
        trackingMapAttr = new SimpleArrayMap<>();
        BackGroundDrawUtils.updateDataSource();
        startTrack();
    }

    public void initCamera(View view) {
        camera_view = (SurfaceView) view.findViewById(R.id.camera_preview);
        draw_view = (SurfaceView) view.findViewById(R.id.pointView);
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
        mCameraHelper = new CameraHelper(mContext, params);
    }

    private byte nv21[];
    private byte temp[];
    private boolean isNV21ready = false;
    private Thread thread;

    int camera_fps;
    int camera_count;
    long camera_long = 0;

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
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

    public void runTrack(byte[] data) {
        try {
            long time = System.currentTimeMillis();
            final List<YMFace> faces = analyse(data, iw, ih);
            if (faces != null && faces.size() > 0) {
                if (!onSecondAfter) {
                    if(!isSend) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> onSecondAfter = true, 1000);
                        isSend = true;
                    }
                } else {
                    onSecondAfter = false;
                    isSend = false;
                    if (callback != null) {
                        callback.callback(faces.get(0).getGender());
                    }
                }
            }
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
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    drawAnim(faces, draw_view, scale_bit, getCameraId(), fps1);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void showFps(boolean show) {
        showFps = show;
    }

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
            DLog.d(mContext.getResources().getConfiguration().orientation + " : " + Configuration.ORIENTATION_PORTRAIT);

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

    private SimpleArrayMap<Integer, YMFace> trackingMap;
    boolean threadStart = false;
    boolean threadBusy = false;
    int trackSize, trackCount;
    boolean saveImage = false;
    private Thread athread;
    private SimpleArrayMap<Integer, YMFace> trackingMapAttr;

    protected List<YMFace> analyse(byte[] bytes, int iw, int ih) {
        if (faceTrack == null) return null;
//        final List<YMFace> faces = new ArrayList<>();
        final List<YMFace> faces = faceTrack.trackMulti(bytes, iw, ih);

        //一直进行识别
//        if (faces != null && faces.size() > 0)
//            for (int i = 0; i < faces.size(); i++) {
//                long time = System.currentTimeMillis();
//                YMFace ymFace = faces.get(i);
//
//                int gender = faceTrack.getGender(i);
//                DLog.d("gender end " + gender + " time :" + (System.currentTimeMillis() - time));
//                time = System.currentTimeMillis();
//
//                int age = faceTrack.getAge(i);
//                ymFace.setAge(age);
//                DLog.d("age end " + age + " time :" + (System.currentTimeMillis() - time));
//                time = System.currentTimeMillis();
//
//                ymFace.setGender(gender);
//                ymFace.setBeautyScore(faceTrack.getFaceBeautyScore(i));
//                DLog.d("beautyScore end " + age + " time :" + (System.currentTimeMillis() - time));
//                faces.add(i, ymFace);
//                faces.remove(i + 1);
//            }

        final byte[] data = bytes;

        //不需要一直识别
        final int mintCOunt = 10;
        trackCount++;
        if (trackCount >= mintCOunt && threadStart && !threadBusy) {
            threadStart = false;
            trackCount = 0;
        }
        if (faces != null && faces.size() > 0) {
            if (!threadStart && !stop) {//此处需要判断stop，，避免onPause释放时崩溃
                threadStart = true;

                if (trackingMap.size() > 50) trackingMap.clear();
                if (trackingMapAttr.size() > 50) trackingMapAttr.clear();
                //找到最大人脸框
                int maxIndex = 0;
                for (int i = 1; i < faces.size(); i++) {
                    if (faces.get(maxIndex).getRect()[2] <= faces.get(i).getRect()[2]) {
                        maxIndex = i;
                    }
                }
                final YMFace ymFace = faces.get(maxIndex);
                final int anaIndex = maxIndex;
                final int trackId = ymFace.getTrackId();
                athread = new Thread(() -> {
                    try {
                        threadBusy = true;

                        for (int i = 0; i < faces.size(); i++) {
                            final YMFace ymFace1 = faces.get(i);
                            final int trackId1 = ymFace1.getTrackId();
                            if (!trackingMap.containsKey(trackId1) ||
                                    trackingMap.get(trackId1).getPersonId() <= 0) {
                                long time = System.currentTimeMillis();
                                int identifyPerson = -1;
                                try {
                                    // 人脸识别的第三方bug
                                    // try catch依然会crash
                                    identifyPerson = faceTrack.identifyPerson(i);
                                    int confidence = faceTrack.getRecognitionConfidence();
                                    DLog.d("identify end " + identifyPerson + " time :" + (System.currentTimeMillis() - time) + " con = " + confidence);
                                    saveImageFromCamera(identifyPerson, data);
                                    if (identifyPerson > 0) {
                                        ymFace1.setIdentifiedPerson(identifyPerson, confidence);
                                        trackingMap.put(trackId1, ymFace1);
                                    }
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                }
//                                int confidence = faceTrack.getRecognitionConfidence();
//                                DLog.d("identify end " + identifyPerson + " time :" + (System.currentTimeMillis() - time) + " con = " + confidence);
//                                saveImageFromCamera(identifyPerson, data);
//                                if (identifyPerson > 0) {
//                                    ymFace1.setIdentifiedPerson(identifyPerson, confidence);
//                                    trackingMap.put(trackId1, ymFace1);
//                                }
                            }
                        }

                        if (!trackingMap.containsKey(trackId) && !trackingMapAttr.containsKey(trackId)) {
                            float[] headposes = ymFace.getHeadpose();
                            if (!(Math.abs(headposes[0]) > 30
                                    || Math.abs(headposes[1]) > 30
                                    || Math.abs(headposes[2]) > 30)) {

                                long time = System.currentTimeMillis();
                                int gender = faceTrack.getGender(anaIndex);
                                DLog.d("gender : " + gender + " -- " + (System.currentTimeMillis() - time));
                                time = System.currentTimeMillis();
                                //有可能获取性别失败，需重新获取
                                if (gender >= 0) {
                                    ymFace.setAge(faceTrack.getAge(anaIndex));
                                    DLog.d("age :  -- " + (System.currentTimeMillis() - time));
                                    ymFace.setGender(gender);
                                    trackingMapAttr.put(trackId, ymFace);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        threadBusy = false;
//                            trackCount = mintCOunt;
                    }
                });
                athread.start();
            }

            for (int i = 0; i < faces.size(); i++) {
                final YMFace ymFace = faces.get(i);
                final int trackId = ymFace.getTrackId();
                if (trackingMap.containsKey(trackId)) {
                    if (trackingMapAttr.containsKey(trackId))
                        trackingMapAttr.remove(trackId);
                    YMFace face = trackingMap.get(trackId);
                    ymFace.setIdentifiedPerson(face.getPersonId(), face.getConfidence());
                }
                if (trackingMapAttr.containsKey(trackId)) {
                    YMFace face = trackingMapAttr.get(trackId);
                    ymFace.setAge(face.getAge());
                    ymFace.setGender(face.getGender());
                }
            }
        } else {
            trackSize = mintCOunt;
        }
        return faces;
    }

    public int getCameraId() {
        return mCameraHelper.getCameraId();
    }

    protected void drawAnim(List<YMFace> faces, SurfaceView draw_view, float scale_bit, int cameraId, String fps) {
        BackGroundDrawUtils.drawAnim(faces, draw_view, scale_bit, cameraId, fps, true);
    }

    public void saveImageFromCamera(int personId, byte[] yuvBytes) {
        if (!saveImage) return;
        File tmpFile = new File("/sdcard/img/fr/out");
        if (!tmpFile.exists()) tmpFile.mkdirs();
        tmpFile = new File("/sdcard/img/fr/out" + "/img_" + System.currentTimeMillis() + "_" + personId + ".jpg");
        saveImage(tmpFile, yuvBytes);
    }

    private void saveImage(File file, byte[] yuvBytes) {

        FileOutputStream fos = null;
        try {
            YuvImage image = new YuvImage(yuvBytes, ImageFormat.NV21, iw, ih, null);
            fos = new FileOutputStream(file);
            image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 90, fos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert fos != null;
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void startTrack() {
        if (faceTrack != null) {
            DLog.d("already init track");
            return;
        }

        stop = false;
        iw = 0;//重新调用initCameraMsg的开关
        faceTrack = new YMFaceTrack();

        /**此处默认初始化，initCameraMsg()处会根据设备设置自动更改设置
         *人脸识别数据库之前保存在应用目录的cache目录下，可以通过另一个初始化检测器的函数
         *public boolean initTrack(Context mContext, int orientation, int resizeScale, String db_dir)
         *通过指定保存db的目录来自定义
         **/
        faceTrack.initTrack(mContext, YMFaceTrack.FACE_0, YMFaceTrack.RESIZE_WIDTH_640);

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
            thread = new Thread(() -> {
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
            });
            thread.start();
        }
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

    private boolean isSpeeakingOver = true;
    private Map<String, Long> userFindTime = new HashMap<>();

    @Override
    public void RegisteredUserFind(User user) {
        long time = System.currentTimeMillis();
        Long lastfind = userFindTime.get(user.getPersonId());


        if (lastfind == null || time - lastfind > 5000) {
            Csjlogger.debug("user find   {}", user.toString());
            userFindTime.put(user.getPersonId(), System.currentTimeMillis());

            if (isSpeeakingOver) {
                Log.e("FaceRecoBackground", "识别成功~~~~~" + user.getName());
                CsjSpeechSynthesizer2.getSynthesizer().startSpeaking("你好 " + user.getName(), new CsjSynthesizerListener() {
                    @Override
                    public void onSpeakBegin() {
                        isSpeeakingOver = false;
                        BusFactory.getBus().post(new ExpressionEvent(Constant.Expression.EXPRESSION_SPEAK));
                    }

                    @Override
                    public void onCompleted(SpeechError speechError) {
                        isSpeeakingOver = true;
                        BusFactory.getBus().post(new ExpressionEvent(Constant.Expression.EXPRESSION_NORMAL));
                    }
                });
            }
        }
    }

    public void onPause() {
        // 等待线程结束再执行super中释放检测器
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread = null;
        }
        // 释放资源
        stopTrack();
    }

    public void stopCamera() {
        mCameraHelper.stopCamera();
    }

    public void stopPreview() {
        mCameraHelper.stopPreview();
    }


    /**
     * 拍照 & 录视频 （注册人脸和视频）
     */
    public PersonRecoCallback callback;

    public interface PersonRecoCallback {
        void callback(int gender);
    }

    public void setPersonRecoCallback(PersonRecoCallback p) {
        this.callback = p;
    }
}
