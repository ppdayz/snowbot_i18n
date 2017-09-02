package com.csjbot.snowbot.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.core.entry.Static;
import com.android.core.util.SharedUtil;
import com.android.core.util.StrUtil;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.bean.WifiDataBean;
import com.csjbot.snowbot.bean.aiui.entity.CsjSynthesizerListener;
import com.csjbot.snowbot.services.CsjSpeechSynthesizer2;
import com.csjbot.snowbot.utils.OkHttp.LoadingDialog;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot.utils.SpeechStatus;
import com.csjbot.snowbot.utils.TimeUtil;
import com.csjbot.snowbot.utils.WifiTools;
import com.csjbot.snowbot.views.BaseRecyViewAdpter;
import com.csjbot.snowbot.views.CusViewHodler;
import com.csjbot.snowbot.views.MyDecoration;
import com.csjbot.snowbot_rogue.platform.SnowBotManager;
import com.csjbot.snowbot_rogue.platform.SnowBotNetwokListen;
import com.csjbot.snowbot_rogue.utils.CSJToast;
import com.csjbot.snowbot_rogue.utils.CSJWifiUtils;
import com.csjbot.snowbot_rogue.utils.WeakReferenceHandler;
import com.iflytek.cloud.SpeechError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * @Author: jl
 * @Date: 2016/12/15
 * @Desc:
 */

public class SecFrg extends BaseFrg {
    @BindView(R.id.recyclerView_wifi)
    RecyclerView recyclerViewWifi;
    @BindView(R.id.no_btn)
    Button noBtn;

    private BaseRecyViewAdpter<WifiDataBean> adapter;
    private List<WifiDataBean> mWifiList = new ArrayList<>();
    protected WifiTools wifiTools;
    private boolean noBtnIsGone = false;
    private CSJWifiUtils.CSJScanResult mScanResult = new CSJWifiUtils.CSJScanResult();
    private Handler handler = new Handler();
    private boolean isConnected;
    private TimeUtil timeUtil = TimeUtil.getInterface();

    private SecFrgHandler mHandler = new SecFrgHandler(this);
    private static final int CONNECT_FAILD = 0;

    static class SecFrgHandler extends WeakReferenceHandler<SecFrg> {
        public SecFrgHandler(SecFrg reference) {
            super(reference);
        }

        @Override
        protected void handleMessage(SecFrg reference, Message msg) {
            switch (msg.what) {
                case SecFrg.CONNECT_FAILD:
                    CSJToast.showToast(reference.getActivity(), Static.CONTEXT.getResources().getString(R.string.check_chassis_connect));
                    CsjSpeechSynthesizer2.getSynthesizer().startSpeaking(Static.CONTEXT.getResources().getString(R.string.check_chassis_connect), null);
                    if (LoadingDialog.getInstance().getStatus()) {
                        LoadingDialog.getInstance().dismiss();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public int getContentViewId() {
        return R.layout.frag_sec_layout;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void setNoBtnGone() {
        noBtnIsGone = true;
    }

    private void initData() {
        SharedUtil.setPreferStr(SharedKey.CONNECTEDSSID, "");
        wifiTools = new WifiTools(getActivity());
        wifiTools.openWifi(getActivity());
        adapter.setData(mWifiList);
//        startGetResult();
    }


    private void initRecyView() {
        adapter = new BaseRecyViewAdpter<WifiDataBean>(mWifiList, getActivity()) {
            TextView wifiNameTv;
            TextView connectStatus;

            @Override
            public int getLayoutId() {
                return R.layout.layout_wifi;
            }

            @Override
            public void setViewModel(CusViewHodler viewHodler, WifiDataBean data, int postion) {
                wifiNameTv = (TextView) viewHodler.getView().findViewById(R.id.wif_name_tv);
                connectStatus = (TextView) viewHodler.getView().findViewById(R.id.wif_commect_status_tv);
                wifiNameTv.setText(data.getScanResult().SSID);
                connectStatus.setText(data.isConnectStatus() ? "已连接" : "");
            }
        };
        adapter.setItemClickListener(new BaseRecyViewAdpter.ItemClickListener() {
            @Override
            public void itemClick(int postion, View view) {
                ScanResult scanResult = mWifiList.get(postion).getScanResult();
                mScanResult.ssid = scanResult.SSID;
                mScanResult.bssid = scanResult.BSSID;
                String capabilities = scanResult.capabilities;
                if (capabilities.contains("WPA") || capabilities.contains("wpa")) {
                    mScanResult.secret = "WPA";
                } else if (capabilities.contains("WEP") || capabilities.contains("wep")) {
                } else {
                    mScanResult.secret = "NONE";
                }
                String ssid = mWifiList.get(postion).getScanResult().SSID;
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle(mScanResult.ssid);
                alert.setMessage(getResources().getString(R.string.wifi_password));
                final EditText et_password = new EditText(getActivity());
                alert.setView(et_password);
                alert.setPositiveButton(getResources().getString(R.string.connect_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pw = et_password.getText().toString();
                        if (StrUtil.isBlank(pw)) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.psd_num_empaty), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        LoadingDialog.getInstance().showLoad(getActivity(), getResources().getString(R.string.try_to_connect));
                        mScanResult.passwd = pw;
                        isConnected = false;
                        CSJWifiUtils.connectWifi(mScanResult, snowBotNetwokListen);
                    }
                });
                alert.setNegativeButton(getResources().getString(R.string.cancle), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alert.setCancelable(false);
                alert.create();
                alert.show();

            }
        });
        recyclerViewWifi.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewWifi.setHasFixedSize(true);
        recyclerViewWifi.addItemDecoration(new MyDecoration(getActivity(), MyDecoration.VERTICAL_LIST));
        recyclerViewWifi.setAdapter(adapter);
        if (noBtnIsGone) {
            noBtn.setVisibility(View.GONE);
        }

    }


    @OnClick({R.id.no_btn, R.id.yes_btn})
    public void onClick(View view) {
        if (null == onClickLister) {
            return;
        }
        switch (view.getId()) {
            case R.id.no_btn:
                onClickLister.clickNo();
                break;
            case R.id.yes_btn:
                onClickLister.clickYes();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initRecyView();
        initData();
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        handler.post(mCallBack);
        if (null != CsjSpeechSynthesizer2.getSynthesizer()) {
            CsjSpeechSynthesizer2.getSynthesizer().startSpeaking(Static.CONTEXT.getResources().getString(R.string.snowbot_restart), null);
        }
    }

    @Override
    protected void onInvisible() {
        super.onInvisible();
        handler.removeCallbacks(mCallBack);
        CsjSpeechSynthesizer2.getSynthesizer().stopSpeaking();
    }


    private SnowBotNetwokListen snowBotNetwokListen = new SnowBotNetwokListen() {
        boolean firstCheck = false;

        @Override
        public void getWifiStatus(HashMap<String, String> status) {
            Csjlogger.debug("getWifiStatus", status.toString());
            String statusStr = status.toString();
            if (statusStr.contains(mScanResult.ssid) && !statusStr.contains("Unknown")) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        timeUtil.stop();
                        CSJToast.showToast(getActivity(), "连接 " + mScanResult.ssid + "成功");
                        CsjSpeechSynthesizer2.getSynthesizer().startSpeaking("连接WiFi成功", new CsjSynthesizerListener() {
                            @Override
                            public void onSpeakBegin() {
                                SpeechStatus.getIstance().setSpeakFinished(false);
                            }

                            @Override
                            public void onCompleted(SpeechError speechError) {
                                SpeechStatus.getIstance().setSpeakFinished(true);
                                onClickLister.clickYes();
                            }
                        });
                    }
                });
                SharedUtil.setPreferStr(SharedKey.CONNECTEDSSID, mScanResult.ssid);
                isConnected = true;
                LoadingDialog.getInstance().dismiss();
            } else {
                SharedUtil.setPreferStr(SharedKey.CONNECTEDSSID, "");
            }

        }

        @Override
        public void configWifiState(boolean success) {
            if (!success) {
                connectWifiFail();
            } else {
                startGetResult();
                timeUtil.getTime(30, new TimeUtil.TimeListener() {
                    @Override
                    public void getTime(int i) {
                        if (i == 0 && LoadingDialog.getInstance().getStatus()) {
                            LoadingDialog.getInstance().dismissLoad();
                            CsjSpeechSynthesizer2.getSynthesizer().startSpeaking("连接超时,请检查网络", null);
                            CSJToast.showToast(Static.CONTEXT, Static.CONTEXT.getResources().getString(R.string.connect_out_time));
                        }
                    }
                });
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


    private Runnable mCallBack = new Runnable() {
        @Override
        public void run() {
            wifiTools.startScan(getActivity());
            List<WifiDataBean> tempList = wifiTools.getWifiList();

            if (null != tempList && tempList.size() > 0) {
                if (mWifiList.contains(tempList)) {
                    String ssid = SharedUtil.getPreferStr(SharedKey.CONNECTEDSSID);
                    if (StrUtil.isNotBlank(ssid)) {
                        for (int i = 0; i < mWifiList.size(); i++) {
                            if (mWifiList.get(i).getScanResult().SSID.equals(ssid)) {
                                mWifiList.get(i).setConnectStatus(true);
                                adapter.setData(mWifiList);
                                return;
                            }
                        }
                    }
                    adapter.setData(mWifiList);
                } else {
                    mWifiList.clear();
                    mWifiList.addAll(tempList);
                    String ssid = SharedUtil.getPreferStr(SharedKey.CONNECTEDSSID);
                    if (StrUtil.isNotBlank(ssid)) {
                        for (int i = 0; i < mWifiList.size(); i++) {
                            if (mWifiList.get(i).getScanResult().SSID.equals(ssid)) {
                                mWifiList.get(i).setConnectStatus(true);
                                adapter.setData(mWifiList);
                                return;
                            }
                        }
                    }

                }
            }
            adapter.setData(mWifiList);
            handler.postDelayed(mCallBack, 1000);
        }
    };

}
