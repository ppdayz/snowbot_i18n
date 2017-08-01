package com.csjbot.snowbot.activity.settings;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.core.entry.Static;
import com.android.core.util.SharedUtil;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot_rogue.app.CsjSpeechSynthesizer;
import com.csjbot.snowbot.utils.TimeUtil;
import com.csjbot.snowbot_rogue.platform.SnowBotManager;
import com.csjbot.snowbot_rogue.platform.SnowBotNetwokListen;
import com.csjbot.snowbot_rogue.utils.CSJToast;
import com.csjbot.snowbot_rogue.utils.CSJWifiUtils;
import com.csjbot.snowbot_rogue.utils.SharePreferenceTools;
import com.csjbot.snowbot_rogue.utils.WeakReferenceHandler;

import net.steamcrafted.loadtoast.LoadToast;

import java.util.HashMap;

import butterknife.BindView;

public class SetWifiManualActivity extends CsjUIActivity {
    private static final int CONNECT_FAILD = 0;

    private EditText edittext_wifi_passwd;
    private EditText edittext_wifi_ssid;


    private LoadToast loadToast = null;
    private SetWifiManualActivityHandler mHandler = new SetWifiManualActivityHandler(this);
    private SharePreferenceTools wifiSave;
    private TimeUtil timeUtil = TimeUtil.getInterface();
    private boolean isConnected;
    private String ssid, passwd;

    @BindView(R.id.set_wifi_btn)
    Button setWifiButton;

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        TextView showText = (TextView) findViewById(R.id.showText);
        setWifiButton.setEnabled(true);

        setupBack();

        edittext_wifi_passwd = (EditText) findViewById(R.id.edittext_wifi_passwd);
        edittext_wifi_ssid = (EditText) findViewById(R.id.edittext_wifi_ssid);
        loadToast = new LoadToast(this);
        loadToast.setText(getResources().getString(R.string.configuration));
        wifiSave = new SharePreferenceTools(this);
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_set_wifi_manual;
    }

    static class SetWifiManualActivityHandler extends WeakReferenceHandler<SetWifiManualActivity> {

        public SetWifiManualActivityHandler(SetWifiManualActivity reference) {
            super(reference);
        }

        @Override
        protected void handleMessage(SetWifiManualActivity reference, Message msg) {
            switch (msg.what) {
                case CONNECT_FAILD:
                    reference.setWifiButton.setEnabled(true);
                    CSJToast.showToast(reference, Static.CONTEXT.getResources().getString(R.string.check_chassis_connect));
                    CsjSpeechSynthesizer.getSynthesizer().startSpeaking(Static.CONTEXT.getResources().getString(R.string.check_chassis_connect), null);
                    break;
                default:
                    break;
            }

        }
    }


    private SnowBotNetwokListen snowBotNetwokListen = new SnowBotNetwokListen() {
        @Override
        public void getWifiStatus(HashMap<String, String> status) {
            String statusStr = status.toString();
            if (statusStr.contains(ssid) && !statusStr.contains("Unknown")) {
                mHandler.post(new Runnable() {
                    /**
                     * Starts executing the active part of the class' code. This method is
                     * called when a thread is started that has been created with a class which
                     * implements {@code Runnable}.
                     */
                    @Override
                    public void run() {
                        // TODO 说出连接成功
                        CSJToast.showToast(SetWifiManualActivity.this, "连接 " + ssid + "成功");
                        CsjSpeechSynthesizer.getSynthesizer().startSpeaking("连接WiFi成功", null);
                        timeUtil.stop();
                        loadToast.success();
                        SharedUtil.setPreferStr(ssid, passwd);
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 1000);

                    }
                });
                isConnected = true;
            }
        }

        @Override
        public void configWifiState(boolean wifiCMDSendSuccess) {
            Csjlogger.debug("ConfigWifiState success = " + wifiCMDSendSuccess);
            if (!wifiCMDSendSuccess) {
                Csjlogger.debug("配置失败");
                connectWifiFail();
            } else {


                // 开始出现动画
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // setWifiButton set disable
                        setWifiButton.setEnabled(false);
                        loadToast.show();
                    }
                });

                // 开始30秒倒计时
                timeUtil.getTime(30, i -> {
                    if (i == 0) {
                        loadToast.error();
                        setWifiButton.setEnabled(true);
                        CsjSpeechSynthesizer.getSynthesizer().startSpeaking("连接超时,请检查网络", null);
                        CSJToast.showToast(SetWifiManualActivity.this, getResources().getString(R.string.connect_out_time));
                    }
                });

                // 开启一个检查是否连接成功的线程
                startGetResult();
            }
        }
    };

    private void connectWifiFail() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                mHandler.sendEmptyMessage(CONNECT_FAILD);

            }
        }).start();
    }

    private void startGetResult() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while (!isConnected) {
                    try {
                        SnowBotManager.getInstance().getNetworkStatus(snowBotNetwokListen);
                    } catch (NullPointerException e) {

                    }

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void setWifi(View view) {
        if (edittext_wifi_passwd.getText().toString().isEmpty()) {
            CSJToast.showToast(this, getResources().getString(R.string.psd_num_empaty));
            return;
        }


        passwd = edittext_wifi_passwd.getText().toString();
        ssid = edittext_wifi_ssid.getText().toString();

        CSJWifiUtils.connectWifi(ssid, passwd, snowBotNetwokListen);

        wifiSave.putString("ssid", ssid);
        wifiSave.putString("password", passwd);
        wifiSave.putString("wifiType", "WPA");
    }

    @Override
    protected void onDestroy() {
        isConnected = true;

        super.onDestroy();
    }
}
