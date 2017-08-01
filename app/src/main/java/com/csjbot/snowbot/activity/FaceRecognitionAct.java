package com.csjbot.snowbot.activity;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.core.entry.Static;
import com.android.core.util.SharedUtil;
import com.android.core.util.StrUtil;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.bean.FaceIdentify;
import com.csjbot.snowbot.bean.FaceIdentifyErrorMsg;
import com.csjbot.snowbot.bean.FaceRecognitionData;
import com.csjbot.snowbot.bean.IdentifyItem;
import com.csjbot.snowbot.bean.RegisterFaceBean;
import com.csjbot.snowbot_rogue.app.CsjSpeechSynthesizer;
import com.csjbot.snowbot.utils.DialogUtil;
import com.csjbot.snowbot.utils.OkHttp.DisposeDataListener;
import com.csjbot.snowbot.utils.OkHttp.HttpUtil;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot.utils.SharedPreferencesSDUtil;
import com.csjbot.snowbot.utils.UrlUtil;
import com.csjbot.snowbot_rogue.camera.CameraInterface;
import com.csjbot.snowbot_rogue.camera.listener.CsjFaceDetectorListener;
import com.csjbot.snowbot_rogue.camera.preview.CameraSurfaceView;
import com.csjbot.snowbot_rogue.camera.ui.FaceView;
import com.csjbot.snowbot_rogue.camera.util.DisplayUtil;
import com.csjbot.snowbot_rogue.camera.util.EventUtil;
import com.csjbot.snowbot_rogue.utils.CSJToast;
import com.csjbot.snowbot_rogue.utils.WeakReferenceHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author: jl
 * @Time: 2017/1/11
 * @Desc:人脸识别页面
 */

public class FaceRecognitionAct extends CsjUIActivity implements CsjFaceDetectorListener {
    @BindView(R.id.register_btn)
    Button registerBtn;
    @BindView(R.id.recognition_btn)
    Button recognitionBtn;
    @BindView(R.id.name_tv)
    TextView nameTv;
    @BindView(R.id.age_tv)
    TextView ageTv;
    @BindView(R.id.sex_tv)
    TextView sexTv;
    @BindView(R.id.appearance_tv)
    TextView appearanceTv;
    @BindView(R.id.smile_tv)
    TextView smileTv;
    @BindView(R.id.surfaceview)
    CameraSurfaceView surfaceView;
    @BindView(R.id.faceView)
    FaceView faceView;
    @BindView(R.id.cropedImageView)
    ImageView cropedImageView;


    private String name;
    private int age;
    private int sex;
    private int appearance;
    private int smile;
    private float previewRate = -1f;
    private FaceHandler mMainHandler;
    private Bitmap bitmap = null;
    private String faceString = "";
    private int Status = 0;
    private final int NONSTATUS = 0;//0为不在扫描和识别状态
    private final int REGISTERSTATUS = 1;//1为注册
    private final int RECOGNITIONSTATUS = 2;//2为识别
    private boolean bInHandelFaceCallBack;

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        setupBack();
        initViewParams();
        mMainHandler = new FaceHandler(this);
        CameraInterface.getInstance().setFaceDetection(true);
        CameraInterface.getInstance().setCsjFaceDetectorListener(this);
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.face_regognition_act;
    }


    /**
     * 人脸注册和识别的点击事件
     *
     * @param view
     */
    @OnClick({R.id.register_btn, R.id.recognition_btn})
    public void onClick(View view) {
        if (!SharedUtil.getPreferBool(SharedKey.NETWORKSTATUS, false)) {
            CSJToast.showToast(Static.CONTEXT, Static.CONTEXT.getString(R.string.tip_network_overtime));
            return;
        }
        clearMesg();
        setBtnEnableFalse();
        CameraInterface.getInstance().setFaceDetection(true);
        switch (view.getId()) {
            case R.id.register_btn:
                Status = REGISTERSTATUS;
                bInHandelFaceCallBack = false;
                mMainHandler.sendEmptyMessage(EventUtil.CAMERA_HAS_STARTED_PREVIEW);
                break;
            case R.id.recognition_btn:
                Status = RECOGNITIONSTATUS;
                bInHandelFaceCallBack = false;
                mMainHandler.sendEmptyMessage(EventUtil.CAMERA_HAS_STARTED_PREVIEW);
                break;
        }
    }

    private void setBtnEnableFalse() {
        registerBtn.setEnabled(false);
        recognitionBtn.setEnabled(false);
    }

    private void setBtnEnable() {
        Status = NONSTATUS;
        registerBtn.setEnabled(true);
        recognitionBtn.setEnabled(true);
    }

    /**
     * 人脸检测到后弹出dialog
     */
    private void createDialog() {
        Csjlogger.debug(getClass().getSimpleName(), "createDialog !");
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.pup_window_addhome, null);
        EditText editText = (EditText) view.findViewById(R.id.room_name_ed);
        editText.setHint(Static.CONTEXT.getString(R.string.input_name));
        AlertDialog.Builder builder = new AlertDialog.Builder(FaceRecognitionAct.this);
        builder.setView(view);
        builder.setPositiveButton(Static.CONTEXT.getString(R.string.ensure), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DialogUtil.canCloseDialog(dialog, false);
                String name = editText.getText().toString();
                if (StrUtil.isNotBlank(name)) {
                    registerFace(name);
                    setBtnEnable();
                    DialogUtil.canCloseDialog(dialog, true);
                }
            }
        });
        builder.setNeutralButton(Static.CONTEXT.getString(R.string.identify_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DialogUtil.canCloseDialog(dialog, true);
                clearMesg();
                setBtnEnable();
                CameraInterface.getInstance().setFaceDetection(true);
                bInHandelFaceCallBack = false;
                mMainHandler.sendEmptyMessage(EventUtil.CAMERA_HAS_STARTED_PREVIEW);
            }
        });
        builder.setNegativeButton(Static.CONTEXT.getString(R.string.detection_again), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DialogUtil.canCloseDialog(dialog, true);
                if (!SharedUtil.getPreferBool(SharedKey.NETWORKSTATUS, false)) {
                    CSJToast.showToast(Static.CONTEXT, Static.CONTEXT.getString(R.string.tip_network_overtime));
                    return;
                }
                clearMesg();
                setBtnEnableFalse();
                CameraInterface.getInstance().setFaceDetection(true);
                bInHandelFaceCallBack = false;
                mMainHandler.sendEmptyMessage(EventUtil.CAMERA_HAS_STARTED_PREVIEW);
            }
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

    }


    /**
     * 人脸注册
     */
    private void registerFace(String name) {
        this.name = name;
        Map<String, String> map = new HashMap<>();
        map.put("person_name", name);
        map.put("image", faceString);
        String registerFaceUrl = (String) SharedPreferencesSDUtil.get(context, "urlUtil", SharedKey.REGISTERFACE, UrlUtil.REGISTERFACE);
        HttpUtil.postJson(this, registerFaceUrl, map, RegisterFaceBean.class, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                RegisterFaceBean registerFaceBean = (RegisterFaceBean) responseObj;
                if (registerFaceBean.getCode() == 0) {
                    CSJToast.showToast(FaceRecognitionAct.this, Static.CONTEXT.getString(R.string.register_suc));
//                    detectionFace();
                } else {
                    String errorMsg = String.format(getResources().getString(R.string.faceregisterror), getResources().getString(FaceIdentifyErrorMsg.getErrorString(registerFaceBean.getCode())));
                    CSJToast.showToast(FaceRecognitionAct.this, errorMsg);
                    setBtnEnable();
                }
                Csjlogger.info("face", "registerFace onSuccess:" + "code" + registerFaceBean.getCode());
            }

            @Override
            public void onFail(Object reasonObj) {
                Csjlogger.info("face", "registerFace onFail:" + "reasonObj " + reasonObj.toString());
                CSJToast.showToast(FaceRecognitionAct.this, Static.CONTEXT.getString(R.string.register_fail));
                setBtnEnable();

            }
        });
    }

    /**
     * 人脸识别
     */
    private void detectionFace() {
        Csjlogger.debug(getClass().getSimpleName(), "detectionFace !");
        int mode = 1;//检测模式：0-所有人脸，1-最大的人脸
        Map<String, String> map = new HashMap<>();
        map.put("mode", mode + "");
        map.put("image", faceString);
        String detectFaceUrl = (String) SharedPreferencesSDUtil.get(context, "urlUtil", SharedKey.DETECTFACE, UrlUtil.DETECTFACE);
        HttpUtil.postJson(null, detectFaceUrl, map, FaceRecognitionData.class, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                FaceRecognitionData faceRecognitionData = (FaceRecognitionData) responseObj;
                if (faceRecognitionData.getCode() == 0) {
                    if (null != faceRecognitionData.getData().getFace().get(0)) {
                        FaceRecognitionData.DataBean.FaceBean faceBean = faceRecognitionData.getData().getFace().get(0);
                        age = faceBean.getAge();
                        sex = faceBean.getGender();
                        appearance = faceBean.getBeauty();
                        smile = faceBean.getExpression();
                        if (Status == RECOGNITIONSTATUS) {
                            faceIdentify();
                        } else if (Status == REGISTERSTATUS) {
                            setBtnEnable();
                            updateMesg();
                            CSJToast.showToast(FaceRecognitionAct.this, Static.CONTEXT.getString(R.string.detection_suc));
                        }
                    }
                } else {
                    setBtnEnable();
                    String errorMsg = String.format(getResources().getString(R.string.facedetecterror), getResources().getString(FaceIdentifyErrorMsg.getErrorString(faceRecognitionData.getCode())));
                    CSJToast.showToast(FaceRecognitionAct.this, errorMsg);
                }

                Csjlogger.info("face", "detectionFace onSuccess:" + "code:" + faceRecognitionData.getCode() + " faceData:" + faceRecognitionData.getData().getFace().toString());
            }

            @Override
            public void onFail(Object reasonObj) {
                Csjlogger.info("face", "detectionFace onFail:" + reasonObj.toString());
                CSJToast.showToast(FaceRecognitionAct.this, Static.CONTEXT.getString(R.string.detection_fail));
                setBtnEnable();
            }
        });

    }

    /**
     * 人脸检索
     */
    private void faceIdentify() {
        Map<String, String> map = new HashMap<>();
        map.put("image", faceString);
        String faceIdentifyUrl = (String) SharedPreferencesSDUtil.get(context, "urlUtil", SharedKey.FACEIDENTIFY, UrlUtil.FACEIDENTIFY);
        HttpUtil.postJson(null, faceIdentifyUrl, map, FaceIdentify.class, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                setBtnEnable();
                FaceIdentify faceIdentify = (FaceIdentify) responseObj;
                if (faceIdentify.getCode() == 0 && null != faceIdentify.getCandidates().get(0)) {
                    CSJToast.showToast(FaceRecognitionAct.this, Static.CONTEXT.getString(R.string.identify_suc));
                    IdentifyItem identifyItem = faceIdentify.getCandidates().get(0);
                    name = identifyItem.getPerson_name();
                    updateMesg();
                } else {
                    String errorMsg = String.format(getResources().getString(R.string.faceidentifyerror), getResources().getString(FaceIdentifyErrorMsg.getErrorString(faceIdentify.getCode())));
                    CSJToast.showToast(FaceRecognitionAct.this, errorMsg);
                }

                Csjlogger.info("face", "faceIdentify onSuccess:" + "code：" + faceIdentify.getCode() + " Candidates：" + faceIdentify.getCandidates().toString());


            }

            @Override
            public void onFail(Object reasonObj) {
                Csjlogger.info("face", "faceIdentify onFail:" + reasonObj.toString());
                setBtnEnable();
                CSJToast.showToast(FaceRecognitionAct.this, Static.CONTEXT.getString(R.string.identify_fail));
            }
        });
    }

    /**
     * 人脸识别后更新信息
     */
    private void updateMesg() {
        nameTv.setText(Static.CONTEXT.getString(R.string.name) + name);
        ageTv.setText(Static.CONTEXT.getString(R.string.age) + age);
        String sexTemp = "";
        if (0 < sex && sex <= 50) {
            sexTemp = getResources().getString(R.string.sex_girl);
        } else if (sex > 50 && sex <= 100) {
            sexTemp = getResources().getString(R.string.sex_boy);
        } else {
            sexTemp = getResources().getString(R.string.sex_unknown);
        }
        sexTv.setText(Static.CONTEXT.getString(R.string.sex) + sexTemp);
        appearanceTv.setText(Static.CONTEXT.getString(R.string.appearance) + appearance);
        smileTv.setText(Static.CONTEXT.getString(R.string.smile) + smile);
        if (null != CsjSpeechSynthesizer.getSynthesizer()) {
            CsjSpeechSynthesizer.getSynthesizer().startSpeaking("你好" + name, null);
        }
    }

    /**
     * 清除信息
     */
    private void clearMesg() {
        nameTv.setText(Static.CONTEXT.getString(R.string.name));
        ageTv.setText(Static.CONTEXT.getString(R.string.age));
        sexTv.setText(Static.CONTEXT.getString(R.string.sex));
        appearanceTv.setText(Static.CONTEXT.getString(R.string.appearance));
        smileTv.setText(Static.CONTEXT.getString(R.string.smile));
    }

    @Override
    protected void onPause() {
        try {
            CameraInterface.getInstance().doStopVideoRecord();
        } catch (IOException e) {
            Csjlogger.debug(e.getMessage());
            e.printStackTrace();
        }
        super.onPause();
    }

    /**
     * 人脸回调接口
     */
    @Override
    public void onFaceDetected(FaceDetector.Face[] faces, int faceCount, Bitmap oriBitmap) {
        synchronized (this) {
            Message m = mMainHandler.obtainMessage();
            m.what = EventUtil.UPDATE_FACE_RECT;
            m.arg1 = faceCount;
            m.obj = faces;
            m.sendToTarget();
            Csjlogger.debug(getClass().getSimpleName(), "bInHandelFaceCallBack is" + bInHandelFaceCallBack);

            if (bInHandelFaceCallBack) {
                return;
            }
            if (faceCount == 1) {
                if (Status != 0) {
                    bInHandelFaceCallBack = true;
                    Csjlogger.debug(getClass().getSimpleName(), "call back set bInHandelFaceCallBack true");
                } else {
                    return;
                }
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
                    mMainHandler.sendMessageDelayed(bmpMsg, 500);

                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    bInHandelFaceCallBack = false;
                }
            }
            oriBitmap.recycle();
        }
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

        if (null != surfaceView) {
            surfaceView.setVisibility(View.VISIBLE);
        }
        if (null != cropedImageView) {
            cropedImageView.setVisibility(View.INVISIBLE);
        }
    }


    private void stopFaceDetect() {
        if (null != faceView) {
            faceView.setVisibility(View.INVISIBLE);
        }
        if (null != surfaceView) {
            surfaceView.setVisibility(View.INVISIBLE);
        }
        if (null != cropedImageView) {
            cropedImageView.setVisibility(View.VISIBLE);

        }
    }

    private static class FaceHandler extends WeakReferenceHandler<FaceRecognitionAct> {
        FaceHandler(FaceRecognitionAct reference) {
            super(reference);
        }

        @Override
        protected void handleMessage(FaceRecognitionAct reference, Message msg) {
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
                    if (reference.Status != 0) {
                        CameraInterface.getInstance().setFaceDetection(false);
                        reference.stopFaceDetect();
                        Bitmap bitmap = (Bitmap) msg.obj;
                        //前置摄像头因为水平镜像的原因 需要左右调换图像
                        if (CameraInterface.getInstance().getCameraId() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            Matrix m = new Matrix();
                            m.setScale(-1, 1);
                            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                            reference.cropedImageView.setImageBitmap(bitmap);
                        } else {
                            reference.cropedImageView.setImageBitmap(bitmap);
                        }
                        reference.faceString = reference.getMapBase64((Bitmap) msg.obj);
                        reference.bitmap = (Bitmap) msg.obj;
                    } else {
                        Csjlogger.debug(getClass().getSimpleName(), "Status is " + reference.Status);
                    }
                    if (reference.Status == reference.REGISTERSTATUS) {
                        reference.createDialog();
                    } else if (reference.Status == reference.RECOGNITIONSTATUS) {
                        reference.detectionFace();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private String getMapBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
            int options = 100;
            while (baos.toByteArray().length / 1024 > 40) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
                baos.reset();//重置baos即清空baos
                bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
                options -= 5;//每次都减少5
            }
        } catch (Exception e) {
            Csjlogger.debug("获取不到图片");
        }

        String mapBase64 = android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.NO_WRAP);
        return mapBase64;
    }
}
