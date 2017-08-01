package com.csjbot.snowbot.activity.settings;

import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.core.entry.Static;
import com.android.core.util.SharedUtil;
import com.android.core.util.StrUtil;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot_rogue.app.CsjSpeechSynthesizer;
import com.csjbot.snowbot.utils.TimeUtil;
import com.csjbot.snowbot_rogue.Events.AIUIEvent;
import com.csjbot.snowbot_rogue.Events.EventsConstants;
import com.csjbot.snowbot_rogue.platform.SnowBotNetwokListen;
import com.csjbot.snowbot_rogue.utils.CSJToast;
import com.csjbot.snowbot_rogue.utils.CSJWifiUtils;
import com.csjbot.snowbot_rogue.utils.SharePreferenceTools;
import com.csjbot.snowbot_rogue.utils.WeakReferenceHandler;
import com.iflytek.aiui.uartkit.UARTAgent;
import com.iflytek.aiui.uartkit.util.PacketBuilder;

import net.steamcrafted.loadtoast.LoadToast;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import butterknife.BindView;

public class SetWifiActivityNew extends CsjUIActivity {
    private static final int CONNECT_FAILD = 0;

    private CSJWifiUtils.CSJScanResult scanResult = new CSJWifiUtils.CSJScanResult();
    private EditText edittext_wifi_passwd;
    private LoadToast loadToast = null;
    private ScanResult mResult;
    private SetWifiActivityHandler mHandler = new SetWifiActivityHandler(this);
    private SharePreferenceTools wifiSave;
    private TimeUtil timeUtil = TimeUtil.getInterface();
    private UARTAgent agent = UARTAgent.getUARTAgent();
    private boolean isConnected;
    boolean isSuccess = false;

    @BindView(R.id.set_wifi_btn)
    Button setWifiButton;
    private boolean exit = false;

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        TextView showText = (TextView) findViewById(R.id.showText);
        setWifiButton.setEnabled(true);

        setupBack();
        mResult = getIntent().getParcelableExtra("scan_result");
        scanResult.ssid = mResult.SSID;
        scanResult.bssid = mResult.BSSID;
        showText.setText(getResources().getString(R.string.please_enter) + mResult.SSID + getResources().getString(R.string.enter_password));

        String capabilities = mResult.capabilities;
        if (capabilities.contains("WPA") || capabilities.contains("wpa")) {
            scanResult.secret = "WPA";
        } else if (capabilities.contains("WEP") || capabilities.contains("wep")) {
        } else {
            scanResult.secret = "NONE";
        }

        edittext_wifi_passwd = (EditText) findViewById(R.id.edittext_wifi_passwd);
        if (StrUtil.isNotBlank(SharedUtil.getPreferStr(scanResult.ssid))) {
            edittext_wifi_passwd.setText(SharedUtil.getPreferStr(scanResult.ssid));
        }
        loadToast = new LoadToast(this);
        loadToast.setText(getResources().getString(R.string.configuration));
        wifiSave = new SharePreferenceTools(this);
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_set_wifi;
    }

    static class SetWifiActivityHandler extends WeakReferenceHandler<SetWifiActivityNew> {
        public SetWifiActivityHandler(SetWifiActivityNew reference) {
            super(reference);
        }

        @Override
        protected void handleMessage(SetWifiActivityNew reference, Message msg) {
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
            if (statusStr.contains(scanResult.ssid) && !statusStr.contains("Unknown")) {
                mHandler.post(new Runnable() {
                    /**
                     * Starts executing the active part of the class' code. This method is
                     * called when a thread is started that has been created with a class which
                     * implements {@code Runnable}.
                     */
                    @Override
                    public void run() {
                        // TODO 说出连接成功
                        CSJToast.showToast(SetWifiActivityNew.this, "连接 " + scanResult.ssid + "成功");
                        CsjSpeechSynthesizer.getSynthesizer().startSpeaking("连接WiFi成功", null);
                        timeUtil.stop();
                        loadToast.success();
                        SharedUtil.setPreferStr(scanResult.ssid, scanResult.passwd);
                    }
                });

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 1000);

                isConnected = true;
            }
        }

        @Override
        public void configWifiState(boolean wifiCMDSendSuccess) {
            Csjlogger.debug("ConfigWifiState success = " + wifiCMDSendSuccess);
            if (!wifiCMDSendSuccess) {
                Csjlogger.debug("配置失败");
                mHandler.sendEmptyMessage(CONNECT_FAILD);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // setWifiButton set disable
                        setWifiButton.setEnabled(true);
                    }
                });
            } else {
                // 开始出现动画
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // setWifiButton set disable
                        loadToast.show();
                    }
                });

                // 开始30秒倒计时
                timeUtil.getTime(60, i -> {
                    if (i == 0) {
                        loadToast.error();
                        setWifiButton.setEnabled(true);
                        CsjSpeechSynthesizer.getSynthesizer().startSpeaking("连接超时,请检查网络", null);
                        CSJToast.showToast(SetWifiActivityNew.this, getResources().getString(R.string.connect_out_time));
                    }
                });

                // 开启一个检查是否连接成功的线程
                startGetResult();
            }
        }
    };


    private void startGetResult() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                while (!isSuccess && !exit) {
                    agent.sendMessage(PacketBuilder.obtainWIFIStatusReqPacket());
                    Csjlogger.debug("get aiui wifi state");

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void setWifi(View view) {
        setWifiButton.setEnabled(false);
        if (edittext_wifi_passwd.getText().toString().isEmpty()
                && !scanResult.secret.equalsIgnoreCase("NONE")) {
            CSJToast.showToast(this, getResources().getString(R.string.psd_num_empaty));
            setWifiButton.setEnabled(true);
            return;
        }

        scanResult.passwd = edittext_wifi_passwd.getText().toString();

        CSJWifiUtils.connectWifiNew(scanResult, snowBotNetwokListen);

        wifiSave.putString("ssid", scanResult.ssid);
        wifiSave.putString("password", scanResult.passwd);
        wifiSave.putString("wifiType", scanResult.secret);
    }

    @Override
    protected void onDestroy() {
        isConnected = true;
        exit = true;
        super.onDestroy();
    }

    private void wifiConnectSuccess() {
        CSJToast.showToast(SetWifiActivityNew.this, "连接 " + scanResult.ssid + "成功");
        CsjSpeechSynthesizer.getSynthesizer().startSpeaking("连接WiFi成功", null);
        timeUtil.stop();
        loadToast.success();
        SharedUtil.setPreferStr(scanResult.ssid, scanResult.passwd);
        mHandler.post(this::finish);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public boolean onAIUIEvent(AIUIEvent event) {
        switch (event.getTag()) {
            case EventsConstants.AIUIEvents.AIUI_EVENT_WIFI_GET:
//                wifiSetText((String) event.data);
                Csjlogger.debug((String) event.data);

                try {
                    JSONObject wifiType = new JSONObject((String) event.data);
                    boolean wifiContent = wifiType.getJSONObject("content").getBoolean("connected");
                    if (wifiContent) {
                        String ssid = wifiType.getJSONObject("content").getString("ssid");
                        if (ssid.substring(1, ssid.length() - 1).equalsIgnoreCase(scanResult.ssid)) {
                            isSuccess = true;
                            wifiConnectSuccess();
                        }
                    }

                } catch (JSONException e) {
                    Csjlogger.error(e);
                }

                break;
        }
        return false;
    }
}
