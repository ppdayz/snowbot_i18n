package com.csjbot.snowbot.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.core.entry.Static;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.settings.SettingsAboutActivity;
import com.csjbot.snowbot.activity.settings.WifiConfigActivity;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.utils.CommonTool;
import com.csjbot.snowbot.utils.QRCodeUtil;
import com.csjbot.snowbot.utils.TimeUtil;
import com.csjbot.snowbot.utils.UUIDGenerator;
import com.csjbot.snowbot_rogue.platform.SnowBotManager;
import com.csjbot.snowbot_rogue.platform.SnowBotNetwokListen;
import com.google.zxing.WriterException;
import com.xw.repo.BubbleSeekBar;

import java.util.HashMap;

public class SettingsActivity extends CsjUIActivity implements View.OnClickListener {
    private ImageView ams_iv_qrcode;
    private BubbleSeekBar as_sbVolumn;
    private SeekBar as_sbBright;
    private AudioManager mAudioManager;
    private LinearLayout goToAboutPage, goToWifiSettings, goToAdvancedOptions, goRecoveryMap, goToCustom, goToAdvertisement, rebootSnowBot;
    private TextView snowbot_ipaddr;
    private Handler mHandler = new Handler();
    private String connected_wifi = "";
    private SnowBotManager botManager;
    private SettingsContentObserver mSettingsContentObserver;

    private void setupVolumnAndBrightness() {
        screenBrightness_check();
        as_sbBright.setProgress(getScreenBrightness());
        as_sbBright.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress <= 20) {
                }
                Intent intent = new Intent();
                intent.putExtra("value", progress);
                intent.setAction("com.smatek.action.changeBrightness");
                sendBroadcast(intent);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        registerVolumeChangeReceiver();


        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        as_sbVolumn.setProgress(current);
        as_sbVolumn.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {

            @Override
            public void onProgressChanged(int progress, float progressFloat) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            }

            @Override
            public void getProgressOnActionUp(int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(int progress, float progressFloat) {

            }
        });
    }

    public void getIpAddr() {
        try {
            botManager.getNetworkStatus(new SnowBotNetwokListen() {
                @Override
                public void getWifiStatus(HashMap<String, String> status) {
                    if (status == null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                snowbot_ipaddr.setText("获取失败");
                            }
                        });
                        return;
                    }

                    String ip = status.get("IP");
                    String ssid = status.get("SSID");
                    String mod = status.get("MODE");

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // TODO: 2017/4/20  把路由模式和disable模式分开
                            if (!mod.equalsIgnoreCase("STA")) {
                                snowbot_ipaddr.setText("网络模式错误");
                                return;
                            }

                            if (ip.equalsIgnoreCase("Unknown")) {
                                snowbot_ipaddr.setText("未连接");
                            } else {
                                snowbot_ipaddr.setText(ip);
                            }
                        }
                    });
                }

                @Override
                public void configWifiState(boolean success) {

                }
            });

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获得当前屏幕亮度值 0--255
     */

    private int getScreenBrightness() {
        int screenBrightness = 255;
        try {
            screenBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception localException) {

        }
        return screenBrightness;
    }

    private void setScreenBrightness(int paramInt) {
        //不让屏幕全暗
        if (paramInt <= 1) {
            paramInt = 1;
        }
        //设置当前activity的屏幕亮度
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        //0到1,调整亮度暗到全亮
        lp.screenBrightness = paramInt / 255f;
        this.getWindow().setAttributes(lp);
        //保存为系统亮度方法1
        android.provider.Settings.System.putInt(getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS, paramInt);

    }

    public void goToAboutPage(View view) {
        startActivity(new Intent(this, SettingsAboutActivity.class));
    }

    public void goRecoveryMap(View view) {
        startActivity(new Intent(this, RecoveryMapAct.class));
    }

    public void goToCustom(View view) {
        startActivity(new Intent(this, CustomVoiceActivity.class));
    }

    public void goToAdvancedOptions(View view) {
        startActivity(new Intent(this, AdvancedSettingsAboutActivity.class));
    }

    public void goToAdvertisement(View view) {
        startActivity(new Intent(this, AdvertisementActivity.class));
    }

    public void rebootSnowBot(View view) {
        showRebootDialog();
    }

    public void goToWifiSettings(View view) {
//        Intent intent = new Intent(this, AIUIWifiActivity.class);
//        intent.putExtra("connected_wifi", connected_wifi);
//        startActivity(intent);

        Intent intent = new Intent(this, WifiConfigActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        getIpAddr();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unregisterVolumeChangeReceiver();
        super.onDestroy();
    }

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        setupBack();
        if (botManager == null) {
            botManager = SnowBotManager.getInstance();
        }

        ams_iv_qrcode = (ImageView) findViewById(R.id.ams_iv_qrcode);
        as_sbVolumn = (BubbleSeekBar) findViewById(R.id.as_sbVolumn);
        as_sbBright = (SeekBar) findViewById(R.id.as_sbBright);
        snowbot_ipaddr = (TextView) findViewById(R.id.snowbot_ipaddr);
        setupVolumnAndBrightness();

        String url = UUIDGenerator.getInstance().getDeviceUUID();
        Bitmap qrCode = null;
        try {
            qrCode = QRCodeUtil.getQrCodeImage(350, 350, url);
            ams_iv_qrcode.setImageBitmap(qrCode);
        } catch (WriterException e) {
            Csjlogger.error(e);
        }

//        ams_iv_qrcode.setImageDrawable(Static.CONTEXT.getResources().getDrawable(R.drawable.qrcode));

        goToAboutPage = (LinearLayout) findViewById(R.id.goToAboutPage);
        goToWifiSettings = (LinearLayout) findViewById(R.id.goToWifiSettings);
        goToAdvancedOptions = (LinearLayout) findViewById(R.id.goToAdvancedOptions);
        goRecoveryMap = (LinearLayout) findViewById(R.id.goRecoveryMap);
        goToCustom = (LinearLayout) findViewById(R.id.goToCustom);
        goToAdvertisement = (LinearLayout) findViewById(R.id.goToAdvertisement);
        rebootSnowBot = (LinearLayout) findViewById(R.id.rebootSnowBot);

        goToAboutPage.setOnClickListener(this);
        goToWifiSettings.setOnClickListener(this);
        goToAdvancedOptions.setOnClickListener(this);
        goRecoveryMap.setOnClickListener(this);
        goToCustom.setOnClickListener(this);
        goToAdvertisement.setOnClickListener(this);
        rebootSnowBot.setOnClickListener(this);

        goToAboutPage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                longTouch(event);
                return false;
            }
        });

    }

    private int mLastMotionX, mLastMotionY;
    //是否移动了
    private boolean isMoved;
    //移动的阈值
    private static final int TOUCH_SLOP = 40;

    private TimeUtil timeUtil = TimeUtil.getInterface();

    /**
     * 长按"关于",显示管理员账号
     *
     * @param event
     */
    private void longTouch(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = x;
                mLastMotionY = y;
                isMoved = false;
                timeUtil.getTime(3, i -> {
                    if (i == 0) {
                        adminGet();
                    }
                });
                break;
            case MotionEvent.ACTION_MOVE:
                if (isMoved) break;
                if (Math.abs(mLastMotionX - x) > TOUCH_SLOP
                        || Math.abs(mLastMotionY - y) > TOUCH_SLOP) {
                    //移动超过阈值，则表示移动了
                    isMoved = true;
                    timeUtil.stop();
                }
                break;
            case MotionEvent.ACTION_UP:
                //释放了
                timeUtil.stop();
                break;
        }
    }

    /**
     * 获取管理员账号，密码
     */
    private void adminGet() {
//        if (CommonTool.isFastDoubleClick()) {
//            return;
//        }
//        Map<String, String> map = new HashMap<>();
//        map.put("uid", UUIDGenerator.getInstance().getDeviceUUID());
//        String adminGetUrl = (String) SharedPreferencesSDUtil.get(context, "urlUtil", SharedKey.GETADMIN, UrlUtil.GETADMIN);
//        HttpUtil.postJson(this, adminGetUrl, map, BFeed.class, new DisposeDataListener() {
//            @Override
//            public void onSuccess(Object responseObj) {
//                BFeed feed = (BFeed) responseObj;
//                if (feed.getData().isFlag()) {
//                    CsjSpeechSynthesizer2.getSynthesizer().startSpeaking(Static.CONTEXT.getResources().getString(R.string.snowbot_already_outware), new CsjSynthesizerListener() {
//                        @Override
//                        public void onSpeakBegin() {
//                            SpeechStatus.getIstance().setSpeakFinished(false);
//                        }
//
//                        @Override
//                        public void onCompleted(SpeechError speechError) {
//                            SpeechStatus.getIstance().setSpeakFinished(true);
//                            SharedUtil.setPreferInt(SharedKey.ROBOTREGISTERSTATUS, RobotStatus.ALREADYOUTWARE);
//                            while (!(SharedUtil.getPreferInt(SharedKey.ROBOTREGISTERSTATUS, 0) == RobotStatus.ALREADYOUTWARE)) {
//                                SharedUtil.setPreferInt(SharedKey.ROBOTREGISTERSTATUS, RobotStatus.ALREADYOUTWARE);
//                            }
//                            if (SharedUtil.getPreferInt(SharedKey.ROBOTREGISTERSTATUS, 0) == RobotStatus.ALREADYOUTWARE) {
//                                CommonTool.rebootDevice();
//                            }
//                        }
//                    });
//
//
//                } else {
//                    CSJToast.showToast(SettingsActivity.this, feed.getData().getMessage());
//                }
//            }
//
//            @Override
//            public void onFail(Object reasonObj) {
//
//            }
//        });
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_settings;
    }


    private void screenBrightness_check() {
        //先关闭系统的亮度自动调节
        try {
            if (android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE) == android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                android.provider.Settings.System.putInt(getContentResolver(),
                        android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
                        android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void showDownDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_hit));
        builder.setMessage(Static.CONTEXT.getString(R.string.shut_down_hint));
        builder.setPositiveButton(getString(R.string.make_sure), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CommonTool.shutDown();
            }
        });
        builder.setNegativeButton(getString(R.string.cancle), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showRebootDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_hit));
        builder.setMessage(Static.CONTEXT.getString(R.string.reboot_hint));
        builder.setPositiveButton(getString(R.string.make_sure), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CommonTool.rebootDevice();
            }
        });
        builder.setNegativeButton(getString(R.string.cancle), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goToAboutPage:
                goToAboutPage(v);
                break;
            case R.id.goToWifiSettings:
                goToWifiSettings(v);
                break;
            case R.id.goToAdvancedOptions:
                goToAdvancedOptions(v);
                break;
            case R.id.goRecoveryMap:
                goRecoveryMap(v);
                break;
            case R.id.goToCustom:
                goToCustom(v);
                break;
            case R.id.goToAdvertisement:
                goToAdvertisement(v);
                break;
            case R.id.rebootSnowBot:
                rebootSnowBot(v);
                break;
            default:
                break;
        }
    }

    private void registerVolumeChangeReceiver() {
        mSettingsContentObserver = new SettingsContentObserver(this, new Handler());
        getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mSettingsContentObserver);
    }

    private void unregisterVolumeChangeReceiver() {
        getApplicationContext().getContentResolver().unregisterContentObserver(mSettingsContentObserver);
    }

    public class SettingsContentObserver extends ContentObserver {
        Context context;

        public SettingsContentObserver(Context c, Handler handler) {
            super(handler);
            context = c;
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//            System.out.println("currVolume:" + currentVolume);
            as_sbVolumn.setProgress(currentVolume);
        }
    }
}
