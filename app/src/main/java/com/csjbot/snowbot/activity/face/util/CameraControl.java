package com.csjbot.snowbot.activity.face.util;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


@TargetApi(21)
public class CameraControl {
    //    public static final Object S_LOCK = new Object();
    public static final int REOPEN_CAMERA_DELAYED = 1000;

    private Activity mActivity;
    private TextureView mCameraTexture, mFaceTextureView;

    private HandlerThread mCameraThread, mBackgroundThread;
    private Handler mCameraHandler, mBackgroundHandler/*mCameraHandler, mMainHandler*/;

    private CameraCaptureSession mCameraCaptureSession;
    private String mCameraId;
    private Size mPreSize;
    private CameraDevice mCameraDevice;

    private CaptureRequest.Builder mRequestBuilder;
    private CaptureRequest mRequest;

    private ImageReader mImageReader;

//    private FaceTrackControl mFaceTrackControl;

    public CameraControl(Activity activity, TextureView cameraTexture, TextureView faceSurface) {
        mActivity = activity;
        mCameraTexture = cameraTexture;
        mFaceTextureView = faceSurface;

        mCameraThread = new HandlerThread("CameraControl-CameraHandler");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());

        mBackgroundThread = new HandlerThread("CameraControl-BackgroundThread");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());

        addListener(mCameraTexture);
    }

    public void release() {
        mCameraHandler.getLooper().quit();
        mCameraHandler = null;
        mBackgroundHandler.getLooper().quit();
        mBackgroundHandler = null;
        mActivity = null;
    }

    private void addListener(TextureView view) {
        view.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mCameraHandler.post(mReopenCameraRunnable);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                mCameraHandler.removeCallbacks(mReopenCameraRunnable);
                closeCamera();
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        // 收集摄像头支持的大过预览Surface的分辨率
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        // 如果找到多个预览尺寸，获取其中面积最小的
        if (bigEnough.size() > 0) {
            Size size = Collections.min(bigEnough, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
                }
            });
            Log.i("shlt", "size : " + size.getWidth() + " , " + size.getHeight());
            return size;
        } else {
            Log.i("shlt", "找不到合适的预览尺寸！！！");
            return choices[0];
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void openCamera() {
        CameraManager cameraManager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String id : cameraManager.getCameraIdList()) {
                mCameraId = id;
                Log.i("shlt", "CameraControl , openCamera , mCameraId : " + mCameraId);

                CameraCharacteristics css = cameraManager.getCameraCharacteristics(id);

                Rect rect = css.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                Log.i("shlt", "CameraControl , openCamera , 111 : " + rect.width() + " , " + rect.height());

                StreamConfigurationMap map = css.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.YUV_420_888)), new Comparator<Size>() {
                    @Override
                    public int compare(Size lhs, Size rhs) {
                        return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
                    }
                });
                Size previewSize = mPreSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), mCameraTexture.getWidth(), mCameraTexture.getHeight(), largest);
                Log.i("shlt", "CameraControl , openCamera , 222 : " + previewSize.getWidth() + " , " + previewSize.getHeight());
                break;
            }
        } catch (CameraAccessException e) {
            Log.i("shlt", "CameraControl , openCamera , catch");
            e.printStackTrace();
        }

        try {
            cameraManager.openCamera(mCameraId, mStateCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
//            synchronized (S_LOCK) {
            Log.i("shlt", "CameraControl , mStateCallback , onOpened");
            mCameraDevice = camera;
            createCameraPreviewSession();
//            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Log.i("shlt", "CameraControl , mStateCallback , onDisconnected");
            closeCamera();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.i("shlt", "CameraControl , mStateCallback , onError : " + error);
            closeCamera();
            if (error == ERROR_CAMERA_IN_USE) {
                mCameraHandler.postDelayed(mReopenCameraRunnable, REOPEN_CAMERA_DELAYED);
            }
        }
    };

    private Runnable mReopenCameraRunnable = new Runnable() {
        @Override
        public void run() {
            openCamera();
        }
    };

    private void createCameraPreviewSession() {
        try {
            if (mCameraCaptureSession != null) {
                mCameraCaptureSession.abortCaptures();
                mCameraCaptureSession = null;
            }
            mImageReader = ImageReader.newInstance(mCameraTexture.getWidth(), mCameraTexture.getHeight(), ImageFormat.YUV_420_888, 2);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);

            SurfaceTexture surfaceTexture = mCameraTexture.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mCameraTexture.getWidth(), mCameraTexture.getHeight());

            Surface surface = new Surface(surfaceTexture);
            mRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mRequestBuilder.addTarget(surface);
            mRequestBuilder.addTarget(mImageReader.getSurface());

            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    Log.i("shlt", "CameraControl , createCaptureSession , onConfigured");
                    try {
                        if (null == mCameraDevice)
                            return;

                        mCameraCaptureSession = session;

                        mRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        mRequest = mRequestBuilder.build();

//                        mFaceTrackControl = new FaceTrackControl(mActivity, mFaceTextureView, new FaceTrackControl.CallBack() {
//                            @Override
//                            public void drawAnim(List<YMFace> faces, float scaleBitX, float scaleBitY) {
////                                if (mActivity != null)
////                                    DrawUtil.drawAnim(mActivity, faces, null, mFaceTextureView, 1, 1, 0, null, false);
//                            }
//                        });

                        mCameraCaptureSession.setRepeatingRequest(mRequest, mCaptureCallback, mCameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Log.i("shlt", "CameraControl , createCaptureSession , onConfigureFailed");
//                    if (mFaceTrackControl != null)
//                        mFaceTrackControl.release();
//                    mFaceTrackControl = null;
                    closeCamera();
                }
            }, mCameraHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            byte[] bytes = acquireJpegBytesAndClose(reader);
            // test , 调用ym
//            if (mFaceTrackControl != null) {
//                mFaceTrackControl.onPreviewFrame(bytes);
//            }
        }
    };

    private byte[] acquireJpegBytesAndClose(ImageReader reader) {
        Image img = reader.acquireLatestImage();
        Image.Plane plane0 = img.getPlanes()[0];
        ByteBuffer buffer = plane0.getBuffer();
        byte[] imageBytes = new byte[buffer.remaining()];
        buffer.get(imageBytes);
        buffer.rewind();
        img.close();

        Image.Plane Y = img.getPlanes()[0];
        Image.Plane U = img.getPlanes()[1];
        Image.Plane V = img.getPlanes()[2];

        int Yb = Y.getBuffer().remaining();
        int Ub = U.getBuffer().remaining();
        int Vb = V.getBuffer().remaining();

        byte[] data = new byte[Yb + Ub + Vb];


        Y.getBuffer().get(data, 0, Yb);
        U.getBuffer().get(data, Yb, Ub);
        V.getBuffer().get(data, Yb + Ub, Vb);

        return imageBytes;
    }

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
//            Log.i("shlt", "CameraControl , mStateCallback , onCaptureCompleted");
//            process(result);

        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
//            Log.i("shlt", "CameraControl , mStateCallback , onCaptureProgressed");
//            process(partialResult);
        }
    };

    private void closeCamera() {
        Log.i("shlt", "CameraControl , closeCamera");
        mCameraHandler.post(new Runnable() {
            @Override
            public void run() {
//                synchronized (S_LOCK) {
                Log.i("shlt", "CameraControl , closeCamera in thread");
                mCameraCaptureSession = null;
                if (mCameraDevice != null) {
                    mCameraDevice.close();
                    mCameraDevice = null;
                }
//                }
            }
        });
    }
}
