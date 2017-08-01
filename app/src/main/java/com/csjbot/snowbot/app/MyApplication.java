package com.csjbot.snowbot.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.CallSuper;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.duersdk.DuerSDK;
import com.baidu.duersdk.DuerSDKFactory;
import com.baidu.duersdk.sdkconfig.SdkConfigInterface;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.activity.VidyoSampleActivity;
import com.csjbot.snowbot.utils.LocationUtil.LocationUtil;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import dou.utils.FileUtil;

/**
 * @author: jl
 * @Time: 2017/3/7:11:20
 * @Desc: 该类是移植的vidyo的，主要是一些nativie方法
 */

public class MyApplication extends PgyApplication {
    public static final String TAG = "VidyoSample";
    static Handler hdlr;
    long address;

    public MyApplication() {
        address = 0;
    }

    public MyApplication(Handler h) {
        hdlr = h;
        address = 0;
    }

    public void setHandler(Handler h) {
        hdlr = h;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initDuer();
//        LocationUtil.init(this, new BDLocationListener() {
//            @Override
//            public void onReceiveLocation(BDLocation bdLocation) {
//
//            }
//
//            @Override
//            public void onConnectHotSpotMessage(String s, int i) {
//
//            }
//        });
        LocationUtil.init(this);

        LocationUtil.start();
    }

    private String getAndroidInternalMemDir() throws IOException {
        File fileDir = getFilesDir(); //crashing
        if (fileDir != null) {
            String filedir = fileDir.toString() + "/";
            Csjlogger.debug(TAG, "file directory = " + filedir);
            return filedir;
        } else {
            Log.e(TAG, "Something went wrong, filesDir is null");
        }
        return null;
    }

    private String getAndroidCacheDir() throws IOException {
        File cacheDir = getCacheDir();
        if (cacheDir != null) {
            String filedir = cacheDir.toString() + "/";
            return filedir;
        }
        return null;
    }

    /**
     * This function is temporary until when we start using internal memory and cache
     */
    private String getAndroidSDcardMemDir() throws IOException {
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/VidyoMobile");
        dir.mkdirs();

        String sdDir = dir.toString() + "/";
        return sdDir;
    }

    public boolean initialize(String caFileName, Activity activity) {
        String pathDir;
        try {
            pathDir = getAndroidInternalMemDir();
//			pathDir = getAndroidSDcardMemDir();
        } catch (Exception e) {
            pathDir = "/data/data/com.csjbot.snowbot.app/app_marina/";
        }

        String logDir;
        try {
            logDir = getAndroidCacheDir();
//			logDir = getAndroidSDcardMemDir();
        } catch (Exception e) {
            logDir = "/data/data/com.csjbot.snowbot.app/app_marina/";
        }


        address = Construct(caFileName, logDir, pathDir, activity);
        if (address == 0)
            return false;
        else
            return true;
    }

    public void uninitialize() {
        Dispose();
    }

    public void cameraSwitchCallback(String name) {
        Message msg = Message.obtain();
        msg.obj = name;
        msg.setTarget(hdlr);
        msg.what = VidyoSampleActivity.SWITCH_CAMERA;
        msg.sendToTarget();
    }


    public void messageBox(String s, int msg_box) {
        Bundle b = new Bundle();
        b.putString("text", s);
        Message m = Message.obtain();
        m.what = VidyoSampleActivity.MSG_BOX;
        m.setData(b);
        m.setTarget(hdlr);
        m.sendToTarget();
    }

    public void callEndedCallback() {
        Csjlogger.debug(TAG, "Call ended received!");
        Message msg = Message.obtain();
        msg.setTarget(hdlr);
        msg.what = VidyoSampleActivity.CALL_ENDED;
        msg.sendToTarget();
    }


    public void callStartedCallback() {
        Csjlogger.debug(TAG, "Call started received!");
        Message msg = Message.obtain();
        msg.setTarget(hdlr);
        msg.what = VidyoSampleActivity.CALL_STARTED;
        msg.sendToTarget();
    }

    public void loginSuccessfulCallback() {
        Csjlogger.debug(TAG, "Login Successful received!");
        Message msg = Message.obtain();
        msg.setTarget(hdlr);
        msg.what = VidyoSampleActivity.LOGIN_SUCCESSFUL;
        msg.sendToTarget();
    }

	/*
     * Native methods that are implemented by the 'VidyoSample' native library,
	 */

    public native long Construct(String caFileName, String logDir, String pathDir, Activity activity);

    public native void Dispose();

    public native void AutoStartMicrophone(boolean autoStart);

    public native void AutoStartCamera(boolean autoStart);

    public native void AutoStartSpeaker(boolean autoStart);

    public native void Login(String vidyoportalName, String userName, String passwordName);

    public native void GuestSignIn(String pac, int guestID, String vmaddress, String loctag, String portal, String portalVersion, String guestName, String serverAddress);

    public native void GuestRoomLink(String name, String portal, String key);

    public native void Render();

    public native void RenderRelease();

    public native void SignOff();

    public native void HideToolBar(boolean disablebar);

    public native void SetCameraDevice(int camera);

    public native void SetPreviewModeON(boolean pip);

    public native void Resize(int width, int height);

    public native int SendAudioFrame(byte[] frame, int numSamples,
                                     int sampleRate, int numChannels, int bitsPerSample);

    public native int GetAudioFrame(byte[] frame, int numSamples,
                                    int sampleRate, int numChannels, int bitsPerSample);

    public native int SendVideoFrame(byte[] frame, String fourcc, int width,
                                     int height, int orientation, boolean mirrored);

    public native void TouchEvent(int id, int type, int x, int y);

    public native void SetOrientation(int orientation);

    public native void MuteCamera(boolean muteCamera);

    public native void SetPixelDensity(double density);

    public native void DirectCall(String who);

    public native void CancelCall();

    public native void DisableAllVideoStreams();

    public native String GetEID();

    public native void EnableAllVideoStreams();

    public native void StartConferenceMedia();

    public native void SetEchoCancellation(boolean aecenable);

    public native void SetSpeakerVolume(int volume);

    public native void IsShot(boolean isOpen);

    public native void DisableShareEvents();

    public native void Mute(boolean isOpen);

    // load the library - name matches jni/Android.mk
    static {
        System.loadLibrary("VidyoClientApp");
        System.loadLibrary("ndkVidyoSample");
    }


    private void initDuer() {
        DuerSDK duerSDK = DuerSDKFactory.getDuerSDK();

        //测试appid,appkey
        String appid = "dmD0DFAE6C3C8F9A89";
        String appkey = "4A1EB4F1C9160C02FD23D2C65C0A9BF5";

        //统计测试，sd卡配置文件动态设置appid,appkey
        File sdkConfigFile = new File(SdkConfigInterface.APP_CONFIGFILE);
        if (sdkConfigFile.isFile() && sdkConfigFile.exists()) {
            try {
                String content = FileUtil.getFileOutputString(SdkConfigInterface.APP_CONFIGFILE);
                JSONObject contentJson = new JSONObject(content);
                String fileAppId = contentJson.optString("appid");
                String fileAppKey = contentJson.optString("appkey");
                if (!TextUtils.isEmpty(fileAppId) && !TextUtils.isEmpty(fileAppKey)) {
                    appid = fileAppId;
                    appkey = fileAppKey;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //初始化sdk
        duerSDK.addSDKErrorLisener(new DuerSDK.SDKErrorLisner() {
            @Override
            public void onError(int i) {
                Csjlogger.warn("error code = {}", i);
            }
        });
        duerSDK.initSDK(this, appid, appkey);
    }
}
