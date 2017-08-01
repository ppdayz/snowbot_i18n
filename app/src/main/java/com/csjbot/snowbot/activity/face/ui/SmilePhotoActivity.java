package com.csjbot.snowbot.activity.face.ui;

import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.face.base.BaseCameraActivity;
import com.csjbot.snowbot.activity.face.util.CountDownAnimation;
import com.csjbot.snowbot.activity.face.util.TrackUtil;

import java.util.List;

import mobile.ReadFace.YMFace;


public class SmilePhotoActivity extends BaseCameraActivity implements CountDownAnimation.CountDownListener {

    TextView tips1;
    Button resumeto;
    private TextView page_title;
    public TextView count_time;
    public View page_cancle;
    public View page_camera;
    public TextView tips;
    public LinearLayout resume_content;

    private CountDownAnimation countDownAnimation;

    private long errorTime = 0;
    private long smailTime = 0;

    private boolean isTake = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.takephoto_activity);
        setCamera_max_width(-1);
        initCamera();


        tips1 = (TextView) findViewById(R.id.tips1);
        resumeto = (Button) findViewById(R.id.resumeto);
        count_time = (TextView) findViewById(R.id.count_time);
        page_cancle = findViewById(R.id.page_cancle);
        page_camera = findViewById(R.id.page_camera);
        tips = (TextView) findViewById(R.id.tips);
        resume_content = (LinearLayout) findViewById(R.id.resume_content);

    }

    public void initView() {

        resume_content.getLayoutParams().width = sw * 5 / 7;
        tips1.setVisibility(View.GONE);
        ((RelativeLayout.LayoutParams) tips1.getLayoutParams()).topMargin = sh / 2 + 100;
        tips.setText(R.string.smile_take);
        countDownAnimation = new CountDownAnimation(count_time, 3);
        countDownAnimation.setCountDownListener(this);
        page_title = (TextView) findViewById(R.id.page_title);
        page_title.setText(R.string.smile_take);
    }


    @Override
    protected void drawAnim(List<YMFace> faces, SurfaceView draw_view, float scale_bit, int cameraId, String fps) {
        TrackUtil.drawAnim(faces, draw_view, scale_bit, cameraId);
    }

    @Override
    protected List<YMFace> analyse(byte[] bytes, int iw, int ih) {
        if (isTake) return null;
        final List<YMFace> faces = faceTrack.trackMulti(bytes, iw, ih);
//        final List<YMFace> faces = new ArrayList<>();
//        final YMFace ymFace = faceTrack.track(bytes, iw, ih);
//        if (ymFace != null) {
//            faces.add(ymFace);
//        }
        if (faces != null && faces.size() > 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doHasFace(faces.get(0));
                }
            });
        }


        return faces;
    }

    private void doHasFace(YMFace face) {

        if (face != null) {
            errorTime = 0;
            float[] emotion = faceTrack.getEmotion(0);
            face.setEmotions(emotion);
            TrackUtil.addFace(face);

            if (smailTime == 0) {
                smailTime = System.currentTimeMillis();
                tips.setText(R.string.give_big_smile);
            }
            if (System.currentTimeMillis() - smailTime >= 3000) {
                tips.setText(R.string.come_on_smile);
            }
        } else {
            smailTime = 0;
            if (errorTime == 0)
                errorTime = System.currentTimeMillis();
            if (System.currentTimeMillis() - errorTime >= 3000) {
                tips.setText(R.string.take_cant_see);
                errorTime = 0;
            }
        }

        if (TrackUtil.isSmile()) {
            isTake = true;
            TrackUtil.cleanFace();
            tips1.setVisibility(View.VISIBLE);
            tips1.setText(R.string.count_three);
            TrackUtil.startCountDownAnimation(countDownAnimation);
        }
    }


    @Override
    public void onCountDownEnd(CountDownAnimation animation) {
        if (mContext != null) {
            tips1.setVisibility(View.GONE);
            resume_content.setVisibility(View.VISIBLE);
            stopPreview();
        }
    }

    private void reTake() {
        resume_content.setVisibility(View.GONE);
        TrackUtil.cleanFace();
        startPreview();
        isTake = false;
        tips.setText(R.string.re_take);
    }


    public void topClick(View view) {

        switch (view.getId()) {
            case R.id.backto:
            case R.id.page_cancle:
                onBackPressed();
                break;
            case R.id.page_camera:
                switchCamera();
                break;
            case R.id.resumeto:
                reTake();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        stopCamera();
        finishAll();
    }

    private void finishAll() {
        TrackUtil.cleanFace();
        finish();
    }
}
