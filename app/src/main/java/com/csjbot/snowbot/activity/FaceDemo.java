package com.csjbot.snowbot.activity;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot_rogue.camera.CameraInterface;
import com.csjbot.snowbot_rogue.camera.listener.CsjFaceDetectorListener;
import com.csjbot.snowbot_rogue.camera.preview.CameraSurfaceView;
import com.csjbot.snowbot_rogue.camera.ui.FaceView;
import com.csjbot.snowbot_rogue.camera.util.DisplayUtil;
import com.csjbot.snowbot_rogue.camera.util.EventUtil;
import com.csjbot.snowbot_rogue.utils.WeakReferenceHandler;

import net.steamcrafted.loadtoast.LoadToast;

import java.io.IOException;

import butterknife.BindView;



/**
 * Copyright (c) 2016, SuZhou CsjBot. All Rights Reserved.
 * www.csjbot.com
 * <p>
 * Created by 浦耀宗 at 2016/12/18 0018-14:32.
 * Email: puyz@csjbot.com
 */
public class FaceDemo extends CsjUIActivity implements CsjFaceDetectorListener {


    private static class FaceDemoHandler extends WeakReferenceHandler<FaceDemo> {
        FaceDemoHandler(FaceDemo reference) {
            super(reference);
        }

        @Override
        protected void handleMessage(FaceDemo reference, Message msg) {
            switch (msg.what) {
                case EventUtil.UPDATE_FACE_RECT:
                    FaceDetector.Face[] faces = (FaceDetector.Face[]) msg.obj;
                    int faceCount = msg.arg1;
                    reference.faceView.setFaces(faces);
                    break;
                case EventUtil.CAMERA_HAS_STARTED_PREVIEW:
                    reference.startCsjFaceDetect();
                    break;
                case EventUtil.UPDATE_FACE_RECT_IMAGEBITMAP:
                    reference.cropedImageView.setImageBitmap((Bitmap) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

    @BindView(R.id.surfaceview)
    CameraSurfaceView surfaceView = null;

    @BindView(R.id.faceView)
    FaceView faceView;

    @BindView(R.id.cropedImageView)
    ImageView cropedImageView;


    private float previewRate = -1f;
    private FaceDemoHandler mMainHandler;
    private LoadToast loadToast = null;
    private int recordTime = 0;

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        initViewParams();
        mMainHandler = new FaceDemoHandler(this);

        CameraInterface.getInstance().startFaceDetection();
//        csjFaceDetect = new CsjFaceDetect(getApplicationContext(), mMainHandler);
        mMainHandler.sendEmptyMessageDelayed(EventUtil.CAMERA_HAS_STARTED_PREVIEW, 1500);
        loadToast = new LoadToast(this);
        loadToast.setText("录制中");
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_face_demo;
    }

    @Override
    protected void onPause() {
        try {
            CameraInterface.getInstance().doStopVideoRecord();
        } catch (IOException e) {
            Csjlogger.error(e.getMessage());
        }
        super.onPause();
    }

    @Override
    public void onFaceDetected(FaceDetector.Face[] faces, int faceCount, Bitmap oriBitmap) {
        Message m = mMainHandler.obtainMessage();
        m.what = EventUtil.UPDATE_FACE_RECT;
        m.arg1 = faceCount;
        m.obj = faces;
        m.sendToTarget();

        if (faceCount == 1) {
            FaceDetector.Face face = faces[0];
            PointF myMidPoint = new PointF();
            face.getMidPoint(myMidPoint);
            float myEyesDistance = face.eyesDistance();
            try {
                Bitmap faceBitmap = Bitmap.createBitmap(oriBitmap,
                        (int) (myMidPoint.x - myEyesDistance * 2),
                        (int) (myMidPoint.y - myEyesDistance * 2),
                        (int) myEyesDistance * 4,
                        (int) myEyesDistance * 4);

                Message bmpMsg = mMainHandler.obtainMessage();
                bmpMsg.what = EventUtil.UPDATE_FACE_RECT_IMAGEBITMAP;
                bmpMsg.obj = faceBitmap;

                mMainHandler.sendMessage(bmpMsg);
            } catch (IllegalArgumentException e) {

            }
        }
        oriBitmap.recycle();
    }

    private void initViewParams() {
        ViewGroup.LayoutParams params = surfaceView.getLayoutParams();
        Point p = DisplayUtil.getScreenMetrics(this);
        params.width = 640;
        params.height = 480;
        previewRate = DisplayUtil.getScreenRate(this); //默认全屏的比例预览
        surfaceView.setLayoutParams(params);
        faceView.setLayoutParams(params);
    }

    private void startCsjFaceDetect() {
        if (faceView != null) {
            faceView.clearFaces();
            faceView.setVisibility(View.VISIBLE);
        }

        CameraInterface.getInstance().setCsjFaceDetectorListener(this);
    }


}
