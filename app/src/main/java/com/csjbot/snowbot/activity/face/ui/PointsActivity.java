package com.csjbot.snowbot.activity.face.ui;

import android.os.Bundle;
import android.support.v4.util.SimpleArrayMap;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.face.base.BaseApplication;
import com.csjbot.snowbot.activity.face.base.BaseCameraActivity;
import com.csjbot.snowbot.activity.face.util.Accelerometer;
import com.csjbot.snowbot.activity.face.util.RetrofitHelp;
import com.csjbot.snowbot.activity.face.util.TrackUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

import dou.utils.DLog;
import mobile.ReadFace.YMFace;


/**
 * Created by mac on 16/7/4.
 */
public class PointsActivity extends BaseCameraActivity {


    public static final String SHOW_TAG = "show";
    private TextView page_title, page_right;
    boolean showPoint = false;
    private View pop_rotate, _logo;
    private Accelerometer acc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.points_activity);
        setCamera_max_width(-1);
        initCamera();
        showFps(true);
        savePath = getCacheDir().getAbsolutePath();
        trackingMap = new SimpleArrayMap<>();

        View page_cancle = findViewById(R.id.page_cancle);
        page_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void initView() {

        pop_rotate = findViewById(R.id.pop_rotate);
        _logo = findViewById(R.id._logo);
        if (BaseApplication.useLogo) _logo.setVisibility(View.VISIBLE);
        page_title = (TextView) findViewById(R.id.page_title);
        page_right = (TextView) findViewById(R.id.page_right);
        page_right.setText(R.string.points_switch);
        if (getIntent().getBooleanExtra(SHOW_TAG, false)) {
            page_title.setText(R.string.points);
            showPoint = true;
        } else {
            page_title.setText(R.string.start_4);
            showPoint = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        start = false;
    }

    boolean start = false;

    @Override
    protected void onResume() {
        super.onResume();
        start = true;
    }

    public void cropNormFace(YMFace ymFace, byte[] yuvBytes) {
        //camera id = CAMERA_FACING_FRONT
        //screenOritation landscape
        //人脸角度作为判定条件 , 可进行调整
        float facialOri[] = ymFace.getHeadpose();
        int x = (int) facialOri[0];
        int y = (int) facialOri[1];
        int z = (int) facialOri[2];

        boolean notCompare = false;
        if (Math.abs(z) >= 25) notCompare = true;
        if (y > 10 || y < -30) notCompare = true;
        if (Math.abs(x) > 15) notCompare = true;
        if (notCompare) return;
        long time = System.currentTimeMillis();
        int imgQuality = faceTrack.getFaceQuality(0);
        DLog.d(x + " : " + y + " : " + z + "  imgQuality : " + imgQuality + " cost" + (System.currentTimeMillis() - time));

        time = System.currentTimeMillis();

        if (imgQuality > 85) {
            File file = new File("/sdcard/img/head");
            if (!file.exists()) file.mkdirs();
            faceTrack.cropFace(yuvBytes, iw, ih, ymFace.getRect(), "/sdcard/img/head/" + System.currentTimeMillis() + ".bmp");
            DLog.d("save image cost : " + (System.currentTimeMillis() - time));
        }
    }

    private SimpleArrayMap<Integer, YMFace> trackingMap;
    boolean threadStart = false;

    @Override
    protected List<YMFace> analyse(final byte[] bytes, final int iw, final int ih) {


        if (faceTrack == null) return null;
        List<YMFace> faces = faceTrack.faceDetect(bytes, iw, ih);
        if (faces != null && faces.size() > 0) {


            if (!threadStart) {
                threadStart = true;

                if (trackingMap.size() > 50) trackingMap.clear();
                //找到最大人脸框
                int maxIndex = 0;
                for (int i = 1; i < faces.size(); i++) {
                    if (faces.get(maxIndex).getRect()[2] <= faces.get(i).getRect()[2]) {
                        maxIndex = i;
                    }
                }
                final YMFace ymFace = faces.get(maxIndex);
                final int anaIndex = maxIndex;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final int trackId = ymFace.getTrackId();
                            if (!trackingMap.containsKey(trackId)) {
                                float[] headposes = ymFace.getHeadpose();
                                if (!(Math.abs(headposes[0]) > 30
                                        || Math.abs(headposes[1]) > 30
                                        || Math.abs(headposes[2]) > 30)) {

//                                    long time = System.currentTimeMillis();
                                    int gender = faceTrack.getGender(anaIndex);
//                                    DLog.d("gender : " + gender + " -- " + (System.currentTimeMillis() - time));
//                                    time = System.currentTimeMillis();
                                    //有可能获取性别失败，需重新获取

                                    if (gender >= 0) {
                                        ymFace.setAge(faceTrack.getAge(anaIndex));
//                                        DLog.d("age :  -- " + (System.currentTimeMillis() - time));
                                        ymFace.setGender(gender);
//                                        postImage(ymFace, bytes, iw, ih);
                                        trackingMap.put(trackId, ymFace);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            threadStart = false;
                        }
                    }
                }).start();
            }

            for (int i = 0; i < faces.size(); i++) {
                YMFace ymFace = faces.get(i);
//                ymFace.setAge(faceTrack.getAge(i));
//                ymFace.setGender(faceTrack.getGender(i));
                int trackId = ymFace.getTrackId();
                if (trackingMap.containsKey(trackId)) {
                    YMFace face = trackingMap.get(trackId);
                    ymFace.setAge(face.getAge());
                    ymFace.setGender(face.getGender());
//                    ymFace.setBeautyScore(face.getBeautyScore());
//                    ymFace.setEmotions(faceTrack.getEmotion(i));
//                    ymFace.setGlassScore(faceTrack.getGlassScore(i));
                }

            }
        }
        return faces;
    }

    String savePath = "";

    boolean startRecord = false;

    public void topClick(View view) throws IOException {
        switch (view.getId()) {
            case R.id.get:
                if (!startRecord) {
                    startRecord = true;

                        mCameraHelper.startRecord("/sdcard/img/record.mp4");

                } else {
                    startRecord = false;
                    mCameraHelper.stopRecord();
                }
                break;
            case R.id.page_right:
                switchCamera();
                break;
        }
    }

    @Override
    protected void drawAnim(List<YMFace> faces, SurfaceView draw_view, float scale_bit, int cameraId, String fps) {
        TrackUtil.drawAnim(faces, draw_view, scale_bit, cameraId, fps, showPoint);
    }

//    UploadManager uploadManager;
//
//    private String token = "";
//
//    public void upload(final String path) {
//
//        if (StringUtils.isNullOrEmpty(token)) {
//            getToken(path);
//            return;
//        }
//
//        if (uploadManager == null)
//            uploadManager = new UploadManager();
//
//        uploadManager.put(
//                path,
//                "alpha_face_" + System.currentTimeMillis() + ".jpg",
//                token,
//                new UpCompletionHandler() {
//                    @Override
//                    public void complete(String key, ResponseInfo info, JSONObject response) {
//                        DLog.d("qiniu", key + ",\r\n " + info + ",\r\n " + response);
//                        Toast.makeText(mContext, "上传成功", Toast.LENGTH_SHORT).show();
//                        File file = new File(path);
//                        file.delete();
//                    }
//                }, null);
//
//
//    }
//
//    private void getToken(final String data) {
//        String api = "http://test.fashionyear.net/uploads/uptoken";
//        VolleyHelper.doGet(api, new VolleyHelper.HelpListener() {
//            @Override
//            public void onResponse(String response) {
//                try {
//                    token = new JSONObject(response).getString("uptoken");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                upload(data);
//            }
//
//            @Override
//            public void onError(VolleyError error) {
//
//            }
//        });
//
//    }


    boolean isPost = false;

    void postImage(final YMFace ymFace, final byte[] bytes, final int iw, final int ih) {
        if (!isPost) {
            isPost = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        File file = new File("/sdcard/img/head");
                        if (!file.exists()) file.mkdirs();
                        String path_to_image = file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".bmp";
                        final File file_image = new File(path_to_image);
                        faceTrack.cropFace(bytes, iw, ih, ymFace.getRect(), path_to_image);


                        RetrofitHelp.getInstance("http://121.42.141.249:8899/").postHeadToServer(file_image
                                , new RetrofitHelp.ApiListener() {
                                    @Override
                                    public void onError(String var1) {
                                        DLog.d("error result = " + var1);
                                        isPost = false;
                                    }

                                    @Override
                                    public void onCompleted(String var1) {
                                        DLog.d("result = " + var1);
//                                file_image.delete();
                                        isPost = false;
                                    }
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                        isPost = false;
                    }
                }
            }).start();

        }
    }
}
