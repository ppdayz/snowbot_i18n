package com.csjbot.snowbot.activity;

import android.content.Context;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.MyApplication;
import com.csjbot.snowbot.utils.OkHttp.LoadingDialog;
import com.vidyo.LmiDeviceManager.LmiDeviceManagerView;
import com.vidyo.LmiDeviceManager.LmiVideoCapturer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class VidyoSampleActivity extends FragmentActivity implements
        LmiDeviceManagerView.Callback {

    private static final String TAG = "VidyoSampleActivity";
    private boolean doRender = false;
    private LmiDeviceManagerView bcView; // new 2.2.2
    private boolean bcCamera_started = false;
    private static boolean loginStatus = false;
    private boolean cameraPaused = false;
    private boolean isGuest = true;
    private boolean cameraStarted = false;
    public static final int CALL_ENDED = 0;
    public static final int MSG_BOX = 1;
    public static final int CALL_RECEIVED = 2;
    public static final int CALL_STARTED = 3;
    public static final int SWITCH_CAMERA = 4;
    public static final int LOGIN_SUCCESSFUL = 5;
    final float degreePerRadian = (float) (180.0f / Math.PI);
    final int ORIENTATION_UP = 0;
    final int ORIENTATION_DOWN = 1;
    final int ORIENTATION_LEFT = 2;
    final int ORIENTATION_RIGHT = 3;
    private float[] mGData = new float[3];
    private float[] mMData = new float[3];
    private float[] mR = new float[16];
    private float[] mI = new float[16];
    private float[] mOrientation = new float[3];


    MyApplication app;
    Handler message_handler;
    StringBuffer message;
    private int currentOrientation;
    private SensorManager sensorManager;
    StringBuffer serverString;
    StringBuffer usernameString;
    StringBuffer passwordString;
    public static boolean isHttps = true;
    StringBuffer portalAddString;
    StringBuffer guestNameString;
    StringBuffer roomKeyString;
    int usedCamera = 1;

    private ImageView cameraView;
    private Button mutebutton;

    private String getAndroidSDcardMemDir() throws IOException {
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/VidyoMobile");
        dir.mkdirs();

        String sdDir = dir.toString() + "/";
        return sdDir;
    }

    private String getAndroidInternalMemDir() throws IOException {
        File fileDir = getFilesDir(); //crashing
        if (fileDir != null) {
            String filedir = fileDir.toString() + "/";
            Csjlogger.debug(TAG, "file directory = " + filedir);
            return filedir;
        } else {
            Csjlogger.error(TAG, "Something went wrong, filesDir is null");
        }
        return null;
    }

    private String writeCaCertificates() {
        try {
            InputStream caCertStream = getResources().openRawResource(R.raw.ca_certificates);
            File caCertDirectory;
            try {
                String pathDir = getAndroidInternalMemDir();
                caCertDirectory = new File(pathDir);
            } catch (Exception e) {
                caCertDirectory = getDir("marina", 0);
            }
            File cafile = new File(caCertDirectory, "ca-certificates.crt");
            FileOutputStream caCertFile = new FileOutputStream(cafile);
            byte buf[] = new byte[1024];
            int len;
            while ((len = caCertStream.read(buf)) != -1) {
                caCertFile.write(buf, 0, len);
            }
            caCertStream.close();
            caCertFile.close();

            return cafile.getPath();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Csjlogger.debug(TAG, "entering onCreate");
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); // disable title bar for dialog

        message_handler = new Handler() {
            public void handleMessage(Message msg) {
                Bundle b = msg.getData();
                switch (msg.what) {
                    case CALL_STARTED:
                        LoadingDialog.getInstance().dismissLoad();
                        app.StartConferenceMedia();
                        app.SetPreviewModeON(true);
                        app.SetCameraDevice(usedCamera);
                        app.DisableShareEvents();
                        startDevices();
                        double density = getResources().getDisplayMetrics().density;
                        app.SetPixelDensity(density);
                        //app.MuteCamera(true);
                        break;

                    case CALL_ENDED:
                        stopDevices();
                        loginStatus = false;
                        app.SignOff();
                        app.RenderRelease();
                        finish();
                        break;
                    case MSG_BOX:
                        message = new StringBuffer(b.getString("text"));
                        break;
                    case SWITCH_CAMERA:
                        String whichCamera = (String) (msg.obj);
                        boolean isFrontCam = whichCamera.equals("FrontCamera");
                        Csjlogger.debug(MyApplication.TAG, "Got camera switch = " + whichCamera);
                        break;
                    case LOGIN_SUCCESSFUL:
                        loginStatus = true;
                        Toast.makeText(VidyoSampleActivity.this, "登录成功", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

        app = (MyApplication) getApplication();
        app.setHandler(message_handler);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);// get the full screen size from android

        setContentView(R.layout.testconference);
        bcView = new LmiDeviceManagerView(this, this);
        View C = findViewById(R.id.glsurfaceview);
        ViewGroup parent = (ViewGroup) C.getParent();
        int index = parent.indexOfChild(C);
        parent.removeView(C);
        parent.addView(bcView, index);
//		bcView.setBackground(Static.CONTEXT.getResources().getDrawable(R.drawable.main_bg));

		/* Camera */
        usedCamera = 1;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        String caFileName = writeCaCertificates();
        app.initialize(caFileName, this);
        setupAudio(); // will set the audio to high volume level
        app.HideToolBar(false);
        app.SetEchoCancellation(true);

    }

    private void setupAudio() {
        int set_Volume = 65535;
        app.SetSpeakerVolume(set_Volume);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Csjlogger.debug(TAG, "onPause Begin");
        if (bcView != null)
            bcView.onPause();
        LmiVideoCapturer.onActivityPause();
        if (cameraStarted == true) {
            cameraPaused = true;
            cameraStarted = false;
        } else {
            cameraPaused = false;
        }
        app.DisableAllVideoStreams();
        Csjlogger.debug(TAG, "onPause End");
        app.EnableAllVideoStreams();

    }


    @Override
    protected void onStop() {
        super.onStop();
        stopDevices();
        app.uninitialize();
    }

    @Override
    public void onResume() {
        super.onResume();
        app.EnableAllVideoStreams();
        joinRoom();
        LoadingDialog.getInstance().showLoad(this, "连接视频中");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopDevices();
        app.uninitialize();
        finish();

    }

    void startDevices() {
        doRender = true;
    }

    void stopDevices() {
        doRender = false;

    }

    private void joinRoom() {
//        String guestLoginArray[] = {"rajkiran.sandboxga.vidyo.com", "guest1", "MgNZJsfNmEwSOcx7It8848Omzc"};
        String guestLoginArray[] = {"bj.vidyo.com", "guest", "Q3iCIWFEH4KYZZ7GVsPs7T87Qs"};
        portalAddString = new StringBuffer(guestLoginArray[0]);
        guestNameString = new StringBuffer(guestLoginArray[1]);
        roomKeyString = new StringBuffer(guestLoginArray[2]);
        String portal = "http://";
        portal += portalAddString.toString();
        Csjlogger.debug(TAG, "!!!!!!!!" + portal);
        if (null != app) {
            app.GuestRoomLink(guestNameString.toString(), portal, roomKeyString.toString());
        }
    }


    public void LmiDeviceManagerViewRender() {
        if (doRender)
            app.Render();
    }

    public void LmiDeviceManagerViewResize(int width, int height) {
        app.Resize(width, height);
    }

    public void LmiDeviceManagerViewRenderRelease() {
        app.RenderRelease();
    }

    public void LmiDeviceManagerViewTouchEvent(int id, int type, int x, int y) {
        app.TouchEvent(id, type, x, y);
    }

    public int LmiDeviceManagerCameraNewFrame(byte[] frame, String fourcc,
                                              int width, int height, int orientation, boolean mirrored) {
        return app.SendVideoFrame(frame, fourcc, width, height, orientation, mirrored);
    }

    public int LmiDeviceManagerMicNewFrame(byte[] frame, int numSamples,
                                           int sampleRate, int numChannels, int bitsPerSample) {
        return app.SendAudioFrame(frame, numSamples, sampleRate, numChannels,
                bitsPerSample);
    }

    public int LmiDeviceManagerSpeakerNewFrame(byte[] frame, int numSamples,
                                               int sampleRate, int numChannels, int bitsPerSample) {
        return app.GetAudioFrame(frame, numSamples, sampleRate, numChannels,
                bitsPerSample);
    }


}
