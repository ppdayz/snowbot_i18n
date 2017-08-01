package com.csjbot.snowbot.activity.face.ui;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.face.base.BaseCameraActivity;
import com.csjbot.snowbot.activity.face.util.TrackUtil;

import java.io.IOException;
import java.util.List;

import dou.utils.DLog;
import mobile.ReadFace.YMFace;


/**
 * Created by mac on 16/7/4.
 */
public class DetactPersonActivity extends BaseCameraActivity {

    public TextView tips;
    private TextView page_title;
    private View myLine;
    private ImageView popView;
    boolean showPoint = false;
    private View icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_detect_activity);
        setCamera_max_width(-1);
        initCamera();
        showFps(false);
        showPoint = false;
        tips = (TextView) findViewById(R.id.tips);
        myLine = findViewById(R.id.myLine);
        icon = findViewById(R.id.icon);
        popView = (ImageView) findViewById(R.id.popView);
    }

    public void initView() {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            popView.setBackgroundResource(R.drawable.bg_detect_land);
        } else if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
            popView.setImageResource(R.drawable.bg_detect_pori);
        }

        myLine.setVisibility(View.VISIBLE);
        tips.setVisibility(View.VISIBLE);
        int length = sw > sh ? sh : sw;
        tips.getLayoutParams().width = length * 6 / 7;

        page_title = (TextView) findViewById(R.id.page_title);
        page_title.setText(R.string.more_detect_person);


        animator = ValueAnimator.ofInt(sw, 0);
        animator.setTarget(myLine);
        animator.setDuration(5000);

        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                int value = (int) animation.getAnimatedValue();
                myLine.getLayoutParams().width = value;
                myLine.requestLayout();
                if (isAlive) {
                    myLine.getLayoutParams().width = 0;
                    myLine.requestLayout();
                    animator.cancel();
                    showResultDialog();
                }
                if (value == 0) {
                    showResultDialog();
                }
            }

        });
    }

    void showResultDialog() {

        if (!showDialog) {
            start_detect = false;
            showDialog = true;
            tips.setVisibility(View.GONE);

            int length = sw > sh ? sh : sw;
            final Dialog dialog = new Dialog(mContext, R.style.Dialog);
            View dialog_view = getLayoutInflater().inflate(R.layout.dialog_detect_person, null);
            dialog_view.setLayoutParams(new ViewGroup.LayoutParams(length, length));
            dialog.setContentView(dialog_view);
            dialog.setCancelable(false);

            View dialog_parent = dialog_view.findViewById(R.id.dialog_parent);
            int dialog_pareng_length = length * 3 / 5;
            dialog_parent.getLayoutParams().width = dialog_pareng_length;
            dialog_parent.getLayoutParams().height = dialog_pareng_length * 4 / 5;

            View dialog_head = dialog_view.findViewById(R.id.dialog_head);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) dialog_head.getLayoutParams();
            layoutParams.width = dialog_pareng_length / 3;
            layoutParams.height = dialog_pareng_length / 3;
            dialog_head.setY(dialog_pareng_length / 15);
            dialog_head.setBackgroundResource(isAlive ? R.drawable.detect_success
                    : R.drawable.detect_failed);

            TextView detect_result = (TextView) dialog_view.findViewById(R.id.detect_result);
            detect_result.setY(dialog_pareng_length * 2 / 15 + dialog_pareng_length / 3);
            detect_result.setText(isAlive ? R.string.verify_success : R.string.verify_failed);

            final View dialog_cancle = dialog_view.findViewById(R.id.dialog_cancle);
            final View dialog_again = dialog_view.findViewById(R.id.dialog_again);
            layoutParams = (RelativeLayout.LayoutParams) dialog_cancle.getLayoutParams();
            layoutParams.width = dialog_pareng_length / 3;
            layoutParams.height = dialog_pareng_length * 36 / (3 * 88);
            dialog_cancle.setX(dialog_pareng_length * 2 / 15);
            layoutParams.setMargins(0, 0, 0, dialog_pareng_length / 15);
            layoutParams = (RelativeLayout.LayoutParams) dialog_again.getLayoutParams();
            layoutParams.width = dialog_pareng_length / 3;
            layoutParams.height = dialog_pareng_length * 36 / (3 * 88);
            dialog_again.setX(dialog_pareng_length * 3 / 15 + dialog_pareng_length / 3);
            layoutParams.setMargins(0, 0, 0, dialog_pareng_length / 15);

            dialog_cancle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                    tips.setVisibility(View.VISIBLE);
                    showDialog = false;
                    finish();
                }
            });

            dialog_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                    tips.setVisibility(View.VISIBLE);
                    start_detect = true;
                    showDialog = false;
                    trackingId = -1;
                }
            });

            dialog.show();

        }
    }


    int count = 0;
    boolean isAlive = false;
    int trackingId = -1;
    ValueAnimator animator;
    boolean start_detect = true;
    boolean showDialog = false;
    long start_time = 0;

    @Override
    protected List<YMFace> analyse(byte[] bytes, int iw, int ih) {
        if (faceTrack == null) return null;
        List<YMFace> faces = faceTrack.trackMulti(bytes, iw, ih);

        if (faces != null && faces.size() > 0 && start_detect) {
            //此处展示检测多人脸时，对第一个人脸操作

            for (int i = 0; i < faces.size(); i++) {
                if (i > 0) faces.remove(i);
            }
            YMFace face = faces.get(0);
            if (trackingId != face.getTrackId()) {
                DLog.d("reset detect");
                trackingId = face.getTrackId();
                faceTrack.resetLivenessDetect(0);
                resetDetect(true);
                start_time = System.currentTimeMillis();
            }

            if (!isAlive) isAlive = faceTrack.livenessDetect(0);
            if (count == 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!animator.isRunning() || !animator.isStarted())
                            animator.start();
                    }
                });
            }
            count++;
        } else {
            resetDetect(false);
        }
        if (System.currentTimeMillis() - start_time <= 1000)
            faces = null;
        return faces;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (animator != null && (animator.isStarted() || animator.isRunning()))
            animator.cancel();
    }

    void resetDetect(final boolean detect) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (detect) {
                    tips.setText(R.string.liveness_try_more);
                } else {
                    tips.setText(R.string.liveness_facing_screen);
                }
                count = 0;
                isAlive = false;
                showDialog = false;
                if (animator.isStarted() || animator.isRunning())
                    animator.cancel();
                myLine.getLayoutParams().width = sw;
                myLine.requestLayout();
            }
        });
    }


    boolean startRecord = false;

    public void topClick(View view) throws IOException {
        switch (view.getId()) {
            case R.id.page_cancle:
                finish();
                break;
            case R.id.get:
                if (!startRecord) {
                    startRecord = true;

                        mCameraHelper.startRecord("/sdcard/img/record.mp4");

                } else {
                    startRecord = false;
                    mCameraHelper.stopRecord();
                }
                break;
        }
    }

    @Override
    protected void drawAnim(List<YMFace> faces, SurfaceView draw_view, float scale_bit, int cameraId, String fps) {
        TrackUtil.drawAnim(faces, draw_view, scale_bit, cameraId, fps, showPoint);
    }

}
