package com.csjbot.snowbot.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.core.util.SharedUtil;
import com.csjbot.csjbase.event.BusFactory;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.aiui.SpeechActivity;
import com.csjbot.snowbot.activity.face.ui.FaceRecoActivity;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.bean.Home;
import com.csjbot.snowbot.services.ClientService;
import com.csjbot.snowbot.services.EnglishSampleService;
import com.csjbot.snowbot.utils.BackUpMapTool;
import com.csjbot.snowbot.utils.PowerStatus;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot.utils.SpeechStatus;
import com.csjbot.snowbot.utils.UpdateApkManagerUtil;
import com.csjbot.snowbot_rogue.Events.TestDataEvent;
import com.csjbot.snowbot_rogue.app.CsjSpeechSynthesizer;
import com.csjbot.snowbot_rogue.bean.MapDataBean;
import com.csjbot.snowbot_rogue.platform.SnowBotManager;
import com.csjbot.snowbot_rogue.servers.slams.SnowBotMoveServer;
import com.csjbot.snowbot_rogue.servers.slams.events.ConnectedEvent;
import com.csjbot.snowbot_rogue.servers.slams.events.ConnectionLostEvent;
import com.csjbot.snowbot_rogue.servers.slams.events.RobotStatusUpdateEvent;
import com.csjbot.snowbot_rogue.utils.CSJToast;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.csjbot.snowbot_rogue.utils.WeakReferenceHandler;
import com.slamtec.slamware.geometry.Line;
import com.slamtec.slamware.robot.Map;
import com.slamtec.slamware.robot.Pose;

import net.steamcrafted.loadtoast.LoadToast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LauncherActivity extends CsjUIActivity {
    // 低电量Dialog显示间隔时间
//    private static final long DIALOG_SHOW_INTVER = 10 * 1000;
    private static final long DIALOG_SHOW_INTVER = 10 * 60 * 1000;

    //        public static final long POWER_CHARGING_FULL_TIME = 10 * 1000;
    public static final long POWER_CHARGING_FULL_TIME = 30 * 60 * 1000;

    private static final int LOWPOWER_TO_GOHOME_CD = 60;

    private Intent clientService;
    private ImageView main_ivSettings, mian_vidyo;
    private SnowBotManager snowBot = SnowBotManager.getInstance();
    private ImageView powerIv;
    private ImageView chargingIv;
    private ImageView disconnectIv;
    private TextView powerTv;
    //判断是否正在巡逻
    private LauncherActivityHandler mHandler = new LauncherActivityHandler(this);
    private List<Pose> poses = new ArrayList<>();
    private boolean isCharging = false;
    private int powerPercent = 0;
    private LoadToast mLoadToast = null;
    private AlertDialog lowpowerDialog, lowpower30Dialog, chargeFullDialog;
    private long lowpowerDialogTime = 0;
    private long lowpower30DialogTime = 0;

    private int lowPowerToGoHomeCD = LOWPOWER_TO_GOHOME_CD;
    private boolean notShowUntilNextCharing;
    private long firstTimeReach100 = 0;

    public void partol(View view) {
        if(!snowBot.isSlamConnected()){
            CSJToast.showToast(this, "请等待底盘连接");
            return;
        }
        if (!snowBot.isPartol()) {
            List<Home> homeLists;
            homeLists = SharedUtil.getListObj(SharedKey.HOMEDATAS, Home.class);
            if (null == homeLists) {
                CsjSpeechSynthesizer.getSynthesizer().startSpeaking(getResources().getString(R.string.set_patrol_point), null);
                CSJToast.showToast(this, getResources().getString(R.string.set_patrol_point), 1000);
                return;
            }

            if (homeLists.size() < 2) {
                CSJToast.showToast(this, getResources().getString(R.string.set_more_patrol_point), 1000);
                return;
            }

            if (snowBot.isSlamConnected() && snowBot.isLowPowerDetected()) {
                CsjSpeechSynthesizer.getSynthesizer().startSpeaking(
                        String.format(Locale.getDefault(),
                                getResources().getString(R.string.snowbot_low_power_cant_partol),
                                "小雪"), null);
                return;
            }

            poses.clear();

            for (Home home : homeLists) {
                Pose pose = new Pose();
                pose.setX(home.getLocation().getX());
                pose.setY(home.getLocation().getY());
                poses.add(pose);
            }
        }

        if (!snowBot.isPartol()) {
            CsjSpeechSynthesizer.getSynthesizer().startSpeaking(getResources().getString(R.string.start_patrol_speech), null);
            CSJToast.showToast(this, getResources().getString(R.string.start_patrol_speech), 1000);
            snowBot.partol(poses);
            mLoadToast.setText("正在巡逻");
            mLoadToast.show();
        } else {
            snowBot.stopPartol();
            mLoadToast.success();
        }

    }

    /**
     * 这个类特殊处理，在onstart的时候注册，在ondestroy里面注销
     *
     * @return false
     */
    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {

        // 开启以太网检查服务
//        startService(new Intent(this, CheckEthernetService.class));
        startService(new Intent(this, EnglishSampleService.class));

        UpdateApkManagerUtil mUpdateApkManagerUtil = new UpdateApkManagerUtil(LauncherActivity.this, false);
        mUpdateApkManagerUtil.checkUpdateInfo();

        if (!PowerStatus.getIstance().isPowerLowWarn()) {
            SpeechStatus.getIstance().setAiuiResponse(true);
        }
        SpeechStatus.getIstance().setAiuiResponse(true);
        clientService = new Intent(this, ClientService.class);

        main_ivSettings = (ImageView) findViewById(R.id.main_ivSettings);
        mian_vidyo = (ImageView) findViewById(R.id.main_ivVidyo);
        powerIv = (ImageView) findViewById(R.id.power);
        chargingIv = (ImageView) findViewById(R.id.charging);
        disconnectIv = (ImageView) findViewById(R.id.disconnect);

//        powerHandler.postDelayed(runnable, 1000);
        main_ivSettings.setOnLongClickListener(v -> {
            Intent mIntent = new Intent();
            ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.Settings");
            mIntent.setComponent(comp);

            mIntent.setAction("android.intent.action.VIEW");
            startActivity(mIntent);
            return false;
        });

        mian_vidyo.setOnLongClickListener(v -> {
            Intent it = new Intent(LauncherActivity.this, SpeechActivity.class);
            it.putExtra("Action", Constant.ACTION_WAKEUP);
            startActivity(it);
            return false;
        });

        mLoadToast = new LoadToast(this);
        createLowPowerWarningDialog();
        createLowPower30WarningDialog();
        createChargeFullDialog();

        startService(clientService);
        registerReceiver(powerTestReceiver, new IntentFilter("com.csjbot.snowbot.powertest"));

//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                EventBus.getDefault().post(new RobotStatusUpdateEvent(25, false, 22));
//                mHandler.postDelayed(this, 6000);
//            }
//        }, 6000);
    }


    private Runnable goHomeCountDownRunnable = new Runnable() {
        @Override
        public void run() {
            if (lowPowerToGoHomeCD > 0) {
                mLoadToast.setText(String.valueOf(lowPowerToGoHomeCD));
                lowPowerToGoHomeCD--;
                mHandler.postDelayed(this, 1000);
            } else {
                lowpower30DialogTime = System.currentTimeMillis();
                lowPowerToGoHomeCD = LOWPOWER_TO_GOHOME_CD;
                mLoadToast.setText("正在巡逻");
                mLoadToast.success();
                mHandler.removeCallbacks(goHomeCountDownRunnable);
                lowpower30Dialog.dismiss();
                // TODO gohome
                snowBot.goHome();
            }
        }
    };

    private void createChargeFullDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.low_power_30_warning_dialog, null);

        chargeFullDialog = builder.setView(view)
                .setPositiveButton("确定", (dialogInterface, i) -> {
                }).create();

        chargeFullDialog.setOnDismissListener((dialogInterface) -> firstTimeReach100 = 0);

        chargeFullDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
        chargeFullDialog.setCanceledOnTouchOutside(false);//点击屏幕不消失
        TextView tv = (TextView) view.findViewById(R.id.low_power_waring_text);
        tv.setText("小雪已经充满电，可以为主人服务啦");
    }

    private void createLowPower30WarningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.low_power_30_warning_dialog, null);

        lowpower30Dialog = builder.setView(view)
                .setPositiveButton("取消", (dialogInterface, i) -> {

                }).create();

        lowpower30Dialog.setOnDismissListener((dialogInterface) -> {
            lowpower30DialogTime = System.currentTimeMillis();
            lowPowerToGoHomeCD = LOWPOWER_TO_GOHOME_CD;
            mLoadToast.setText("正在巡逻");
            mLoadToast.success();
            mHandler.removeCallbacks(goHomeCountDownRunnable);
            SpeechStatus.getIstance().setAiuiResponse(true);
        });

        lowpower30Dialog.setOnShowListener(dialog -> {
            SpeechStatus.getIstance().setAiuiResponse(false);
            mLoadToast.show();
            mHandler.post(goHomeCountDownRunnable);
        });

        lowpower30Dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
        lowpower30Dialog.setCanceledOnTouchOutside(false);//点击屏幕不消失
        TextView tv = (TextView) view.findViewById(R.id.low_power_waring_text);
        tv.setText(String.format(Locale.getDefault(),
                getString(R.string.low_power_30_warning_stop_partol_to_gohome), LOWPOWER_TO_GOHOME_CD));
    }

    public void createLowPowerWarningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.low_power_waring_dialog, null);

        lowpowerDialog = builder.setView(view)
                .setPositiveButton("确定", (dialogInterface, i) -> {
                    CheckBox cb = (CheckBox) view.findViewById(R.id.low_power_waring_checkBox);
                    if (cb.isChecked()) {
                        notShowUntilNextCharing = true;
                    }

                }).create();

        lowpowerDialog.setOnDismissListener(dialog -> {
            SpeechStatus.getIstance().setAiuiResponse(true);
            lowpowerDialogTime = System.currentTimeMillis();
            Csjlogger.info("lowpowerDialog dismis, lowpowerDialogTime = {}", lowpowerDialogTime);
        });


        lowpowerDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
        lowpowerDialog.setCanceledOnTouchOutside(false);//点击屏幕不消失
        TextView tv = (TextView) view.findViewById(R.id.low_power_waring_text);
        tv.setText(getString(R.string.low_power_warning));
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_launcher;
    }

    private static class LauncherActivityHandler extends WeakReferenceHandler<LauncherActivity> {

        public LauncherActivityHandler(LauncherActivity reference) {
            super(reference);
        }

        @Override
        protected void handleMessage(LauncherActivity reference, Message msg) {

        }
    }

    public void startMap(View view) {
//        startActivityForResult(new Intent(this, MapActivity.class), 10086);
        if (snowBot.isSlamConnected()) {
            startActivity(new Intent(this, MapActivity.class));
        } else {
            CSJToast.showToast(this, "请等待底盘连接");
        }
    }

    public void startVideo(View view) {
        startActivity(new Intent(this, VidyoSampleActivity.class));
    }

    public void goToDance(View view) {
        startActivity(new Intent(this, DanceAct.class));
    }

    public void startVideoCall(View view) {
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = getPackageManager().queryIntentActivities(resolveIntent, 0);
        for (ResolveInfo ri : apps) {
            if (ri != null) {
                String packageName1 = ri.activityInfo.packageName;
                String className = ri.activityInfo.name;
//                Csjlogger.debug("packageName1 " + packageName1 + " , className " + className);
                if (className.contains("mobileqq")) {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    ComponentName cn = new ComponentName(packageName1, className);
                    intent.setComponent(cn);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        }
    }

    public void startFaceRecognition(View view) {
        startActivity(new Intent(this, FaceRecoActivity.class));
    }

    public void startVideoRecord(View view) {
//        Intent intent = new Intent(this, MaterialLockActivity.class);
//        intent.putExtra("nextActivityName", LauncherActivity.class.getName());
//        startActivity(intent);
        startActivity(new Intent(this, VideoRecordActivity.class));
    }

    public void goToStudyPage(View view) {
        startActivity(new Intent(this, FamilyFunActivity.class));
    }

    public void goToSmartHome(View view) {
        startActivity(new Intent(this, SmartHomeActivity1.class));
    }

    public void startGallery(View view) {
        startActivity(new Intent(this, GalleryActivity.class));
    }

    public void goToSettings(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void startAdvertisement(View view) {
        startActivity(new Intent(this, AdvertisementAct.class));
    }

    @Override
    public void onBackPressed() {
//        Csjlogger.info("onBackPressed");
    }

    @Override
    protected void onStart() {
        ibus = BusFactory.getBus();
        ibus.register(this);

        super.onStart();
    }

    @Override
    protected void onDestroy() {
        stopService(clientService);
        snowBot.close();

        if (ibus != null) {
            ibus.unregister(this);
        }

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        SharedUtil.setPreferInt(SharedKey.AIUISERVICESWITCH, 1);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        snowBot.stopPartol();
        mLoadToast.success();
    }

    private void recoveryMapData() {
        MapDataBean mapDataBean = BackUpMapTool.getLastFile();
        if (null == mapDataBean) {
            return;
        }
        Map map = new Map(mapDataBean.getOrigin(), mapDataBean.getDimension(), mapDataBean.getResolution(), mapDataBean.getTimestamp(), Base64.decode(mapDataBean.getData(), Base64.NO_WRAP));

        List<Line> wallsList = new ArrayList<>();
        for (int i = 0; i < mapDataBean.getWallsData().size(); i++) {
            Line line = new Line(mapDataBean.getWallsData().get(i).getSegmentId(), mapDataBean.getWallsData().get(i).getLine_startPoint_x(), mapDataBean.getWallsData().get(i).getLine_startPoint_y(),
                    mapDataBean.getWallsData().get(i).getLine_endPoint_x(), mapDataBean.getWallsData().get(i).getLine_endPoint_y());
            wallsList.add(line);
        }

        if (null != mapDataBean) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LauncherActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_recover_map, null);
            builder.setView(view);
            ImageView iv = (ImageView) view.findViewById(R.id.recovery_iv);
            iv.setImageBitmap(BackUpMapTool.getMapPic(mapDataBean));
            builder.setNegativeButton(getResources().getString(R.string.cancle), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.setPositiveButton(getResources().getString(R.string.restore), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (null != map) {
                        SnowBotMoveServer.getInstance().recoveryMapData(map, wallsList);
                        CSJToast.showToast(LauncherActivity.this, getResources().getString(R.string.restore_map_succeed));
                    }
                    dialog.dismiss();
                }
            });

            builder.setCancelable(false);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private AlphaAnimation alphaAnimation1 = new AlphaAnimation(0.01f, 1.0f);
    private boolean isChargingAniming = false;

    public void startChargingAnim() {
        isChargingAniming = true;
        alphaAnimation1.setDuration(2000);
        alphaAnimation1.setRepeatCount(Animation.INFINITE);
        alphaAnimation1.setRepeatMode(Animation.REVERSE);
        chargingIv.setAnimation(alphaAnimation1);
        alphaAnimation1.start();
    }

    public void stopChargingAnim() {
        isChargingAniming = false;
        alphaAnimation1.cancel();
    }

    //  power show
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void robotStatusUpdated(RobotStatusUpdateEvent event) {
//        Csjlogger.debug("Robot is isCharging {} , BatteryPercentage is {}, LocalizationQuality is {}",
//                event.isCharging(), event.getBatteryPercentage(), event.getLocalizationQuality());
        isCharging = event.isCharging();
        powerPercent = event.getBatteryPercentage();

        if (powerPercent > 100) {
            powerPercent = 100;
        }

        if (isCharging) {
            powerIv.setVisibility(View.INVISIBLE);
            disconnectIv.setVisibility(View.INVISIBLE);
            chargingIv.setVisibility(View.VISIBLE);

            mHandler.post(() -> {
                if (isChargingAniming) {
                    startChargingAnim();
                }
            });

            if (lowpowerDialog.isShowing()) {
                lowpowerDialog.dismiss();
            }

            if (lowpower30Dialog.isShowing()) {
                lowpower30Dialog.dismiss();
            }

            notShowUntilNextCharing = false;
            lowpowerDialogTime = 0;
            lowpower30DialogTime = 0;

            if (powerPercent == 100) {
                if (firstTimeReach100 == 0) {
                    firstTimeReach100 = System.currentTimeMillis();
                }

                if (System.currentTimeMillis() - firstTimeReach100 > POWER_CHARGING_FULL_TIME
                        && !chargeFullDialog.isShowing()) {
                    chargeFullDialog.show();
                }
            }
        } else {
            firstTimeReach100 = 0;
            if (chargeFullDialog.isShowing()) {
                chargeFullDialog.dismiss();
            }

            if (isChargingAniming) {
                stopChargingAnim();
            }

            powerIv.setVisibility(View.VISIBLE);
            disconnectIv.setVisibility(View.INVISIBLE);
            chargingIv.setVisibility(View.INVISIBLE);

            if (powerPercent < 30 || snowBot.isLowPowerDetected()) {
                powerIv.setImageResource(R.drawable.nopow);
            } else if ((powerPercent >= 30 && powerPercent < 40)) {
                powerIv.setImageResource(R.drawable.lowpow);
            } else if (powerPercent >= 40 && powerPercent < 80) {
                powerIv.setImageResource(R.drawable.midpow);
            } else {
                powerIv.setImageResource(R.drawable.highpow);
            }

            if (snowBot.isLowPowerDetected()) {
                if (lowpowerDialog.isShowing()) {
                    lowpowerDialog.dismiss();
                }

                String tts = String.format(Locale.getDefault(),
                        getString(R.string.low_power_30_warning), LOWPOWER_TO_GOHOME_CD);
                if (snowBot.isPartol()) {
                    snowBot.stopPartol();
                    mLoadToast.error();
                    tts = String.format(Locale.getDefault(),
                            getString(R.string.low_power_30_warning_stop_partol_to_gohome), LOWPOWER_TO_GOHOME_CD);
                }

                if ((System.currentTimeMillis() - lowpower30DialogTime) > DIALOG_SHOW_INTVER &&
                        !lowpower30Dialog.isShowing()) {
                    lowpower30Dialog.show();
                    SpeechStatus.getIstance().setAiuiResponse(false);
                    CsjSpeechSynthesizer.getSynthesizer().startSpeaking(tts, null);
                }

                return;
            }

            if (powerPercent < Constant.LOW_POWER_TO_WARNING) {
                String tts = getString(R.string.low_power_warning);
//                if (snowBot.isLowPowerDetected()) {
//                    if (snowBot.isPartol()) {
//                        snowBot.stopPartol();
//                        mLoadToast.error();
//                        tts = String.format(Locale.getDefault(),
//                                getString(R.string.low_power_30_warning_stop_partol));
//                    }
//                }

                if ((System.currentTimeMillis() - lowpowerDialogTime) > DIALOG_SHOW_INTVER &&
                        !lowpowerDialog.isShowing() &&
                        !notShowUntilNextCharing) {//此时提示框未显示
                    lowpowerDialog.show();
                    SpeechStatus.getIstance().setAiuiResponse(false);
                    Csjlogger.info("lowpowerDialog show , System.currentTimeMillis() is {}", System.currentTimeMillis());
                    CsjSpeechSynthesizer.getSynthesizer().startSpeaking(tts, null);
                }
            }
        }
    }

    BroadcastReceiver powerTestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            EventBus.getDefault().post(new RobotStatusUpdateEvent(intent.getIntExtra("powerPercent", 50),
                    intent.getBooleanExtra("isCharging", false), 75));

            if (intent.hasExtra("testPower")) {
                EventBus.getDefault().post(new TestDataEvent(0, intent.getBooleanExtra("testPower", false)));
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void connectSlamFailed(ConnectionLostEvent event) {
        powerIv.setVisibility(View.INVISIBLE);
        disconnectIv.setVisibility(View.VISIBLE);
        chargingIv.setVisibility(View.INVISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void connectSlamSuccess(ConnectedEvent event) {
        if (isChargingAniming) {
            stopChargingAnim();
        }

        powerIv.setVisibility(View.VISIBLE);
        disconnectIv.setVisibility(View.INVISIBLE);
        chargingIv.setVisibility(View.INVISIBLE);
    }
}
