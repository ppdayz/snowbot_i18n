package com.csjbot.snowbot.activity.face.ui;

import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.util.SimpleArrayMap;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import com.android.core.util.SharedUtil;
import com.csjbot.csjbase.event.BusFactory;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.face.base.BaseCameraActivity;
import com.csjbot.snowbot.activity.face.model.User;
import com.csjbot.snowbot.activity.face.ui.icount.ManageFaceActivity;
import com.csjbot.snowbot.activity.face.ui.icount.RegisterImageCameraActivity;
import com.csjbot.snowbot.activity.face.util.DrawUtil;
import com.csjbot.snowbot_rogue.app.CsjSpeechSynthesizer;
import com.csjbot.snowbot.bean.aiui.entity.CsjSynthesizerListener;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot_rogue.Events.ExpressionEvent;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.iflytek.cloud.SpeechError;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dou.utils.DLog;
import mobile.ReadFace.YMFace;

public class FaceRecoActivity extends BaseCameraActivity implements DrawUtil.FindRegisteredUserInterface {
    private SimpleArrayMap<Integer, YMFace> trackingMap;
    boolean threadStart = false;
    boolean threadBusy = false;
    int trackSize, trackCount;
    boolean saveImage = false;

    // test
    Handler mHandler = new Handler();
    private long enterTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_reco_2);
        DrawUtil.setFindRegisteredUserInterface(this);
        setCamera_max_width(1080);
        initView();
        initCamera();
        showFps(true);
    }


    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - enterTime > 1500) {
            super.onBackPressed();
            trackingMap.clear();
            stopCamera();
        }
    }

    public void initView() {
        ImageView back = (ImageView) findViewById(R.id.totleBack);
        enterTime = System.currentTimeMillis();
        back.setOnClickListener(v -> onBackPressed());

//        TextView title = (TextView) findViewById(R.id.page_title);
//        Button page_right = (Button) findViewById(R.id.page_right);
//        title.setText(R.string.start_1);
//        page_right.setText(R.string.unlock_insert_face);
//        page_right.setVisibility(View.GONE);

        findViewById(R.id.manage_face).setOnClickListener(view ->
                startActivityForResult(new Intent(FaceRecoActivity.this, ManageFaceActivity.class), 100));

    }

    @Override
    protected void drawAnim(List<YMFace> faces, SurfaceView draw_view, float scale_bit, int cameraId, String fps) {
        DrawUtil.drawAnim(faces, draw_view, scale_bit, cameraId, fps, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedUtil.setPreferInt(SharedKey.AIUISERVICESWITCH, 0);

        if (trackingMap != null && trackingMap.size() != 0) {
            trackingMap.clear();
        }
        trackingMap = new SimpleArrayMap<>();
        if (trackingMapAttr != null && trackingMapAttr.size() != 0) {
            trackingMapAttr.clear();
        }
        trackingMapAttr = new SimpleArrayMap<>();
        DrawUtil.updateDataSource();

        faceTrack.setRecognitionConfidence(75);
    }

    private Thread thread;
    private SimpleArrayMap<Integer, YMFace> trackingMapAttr;

    @Override
    protected List<YMFace> analyse(byte[] bytes, final int iw, final int ih) {
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
                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            threadBusy = true;

                            for (int i = 0; i < faces.size(); i++) {
                                final YMFace ymFace = faces.get(i);
                                final int trackId = ymFace.getTrackId();
                                if (!trackingMap.containsKey(trackId) ||
                                        trackingMap.get(trackId).getPersonId() <= 0) {
                                    long time = System.currentTimeMillis();
                                    int identifyPerson = faceTrack.identifyPerson(i);
                                    int confidence = faceTrack.getRecognitionConfidence();
                                    DLog.d("identify end " + identifyPerson + " time :" + (System.currentTimeMillis() - time) + " con = " + confidence);
                                    saveImageFromCamera(identifyPerson, data);
                                    if (identifyPerson > 0) {
                                        ymFace.setIdentifiedPerson(identifyPerson, confidence);
                                        trackingMap.put(trackId, ymFace);
                                    }
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
                    }
                });
                thread.start();
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

    public void topClick(View view) {
        switch (view.getId()) {
            case R.id.page_cancle:
                finish();
                break;
            case R.id.page_right://TODO 录入人脸
                stopCamera();
                trackingMap.clear();
                startActivity(new Intent(this, RegisterImageCameraActivity.class));
                break;
        }
    }

    @Override
    protected void onPause() {
        //等待线程结束再执行super中释放检测器
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread = null;
        }
        super.onPause();
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
                CsjSpeechSynthesizer.getSynthesizer().startSpeaking("你好 " + user.getName(), new CsjSynthesizerListener() {
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
}
