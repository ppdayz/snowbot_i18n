package com.csjbot.snowbot.activity.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot_rogue.Events.AIUIEvent;
import com.csjbot.snowbot_rogue.Events.EventsConstants;
import com.csjbot.snowbot_rogue.utils.WifiUtil;
import com.iflytek.aiui.uartkit.UARTAgent;
import com.iflytek.aiui.uartkit.util.PacketBuilder;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AIUIWifiActivity extends CsjUIActivity implements WifiUtil.IWifiOpen {
    private UARTAgent mAgent = UARTAgent.getUARTAgent();
    private Handler mHandle = new Handler();
    private IntentFilter mFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    private ListView mListView;
    private ScanResultAdapter2 mAdapter = new ScanResultAdapter2();
    private String wifiState = "未连接";
    private TextView aiui_wifi_show;
    private WifiActionReceiver mWifiActionReceiver = new WifiActionReceiver();
    private WifiManager wm = null;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    new ScanResultTask().execute((Void) null);
                    break;
                case 1:
                    break;
                case 2:
                    break;
                default:
                    break;
            }
        }

    };

    public void gotoWifiPage(View view) {
        startActivity(new Intent(AIUIWifiActivity.this, SetWifiManualActivity.class));
        finish();
    }

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        wm = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        setupBack();

        wifiState = getString(R.string.wifi_state_disconnected);
        mListView = (ListView) findViewById(R.id.asb_listView);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ScanResult reesult = mAdapter.getItem(position);
                Intent intent = new Intent(AIUIWifiActivity.this, SetWifiActivityNew.class);
                intent.putExtra("scan_result", reesult);
                startActivity(intent);
                finish();
            }
        });

        wifiConnectedState();

        aiui_wifi_show = (TextView) findViewById(R.id.aiui_wifi_show);
        aiui_wifi_show.setText(wifiState);


        if (!WifiUtil.isWifiOpen(this)) {
            mListView.setVisibility(View.GONE);
            WifiUtil.openWifi(this, this);
        } else {
            mHandler.post(mCallBack);
        }
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_aiuiwifi;
    }

    static class ViewHolder {
        private ViewHolder() {
        }

        public static <T extends View> T getView(View mView, int id) {
            SparseArray<View> mViews = (SparseArray<View>) mView.getTag();

            if (mViews == null) {
                mViews = new SparseArray<>();
                mView.setTag(mViews);
            }

            View v = mViews.get(id);

            if (v == null) {
                v = mView.findViewById(id);
                mViews.put(id, v);
            }

            return (T) v;
        }
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public boolean onAIUIEvent(AIUIEvent event) {
        switch (event.getTag()) {
            case EventsConstants.AIUIEvents.AIUI_EVENT_WIFI_GET:
                wifiSetText((String) event.data);
                break;
        }
        return false;
    }

    private void wifiSetText(String wifiData) {
        try {
            JSONObject wifiType = new JSONObject(wifiData);
            boolean wifiContent = wifiType.getJSONObject("content").getBoolean("connected");
            if (wifiContent) {
                String ssid = wifiType.getJSONObject("content").getString("ssid");
                wifiState = ssid + getString(R.string.wifi_state_connected);
            } else {
                wifiState = getString(R.string.wifi_state_disconnected);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        mHandle.post(new Runnable() {
            @Override
            public void run() {
                aiui_wifi_show.setText(wifiState);
            }
        });
    }

    class ScanResultAdapter2 extends BaseAdapter {

        List<ScanResult> mScanResult = new ArrayList<>();
        Map<String, String> mWifiPwdMap = new HashMap<>();

        public void refreshList(List<ScanResult> mResult) {
            mScanResult.clear();
            mScanResult.addAll(mResult);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return this.mScanResult.size();
        }

        @Override
        public ScanResult getItem(int position) {
            return this.mScanResult.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(AIUIWifiActivity.this).inflate(R.layout.scan_result_item, null);
            }

            TextView mSsidTv = ViewHolder.getView(convertView, R.id.ssid_tv);
            TextView mEncryptTv = ViewHolder.getView(convertView, R.id.encrypt_tv);
            TextView mPwdTv = ViewHolder.getView(convertView, R.id.pwd_tv);

            if (getItem(position) != null) {

                ScanResult reesult = getItem(position);

                mSsidTv.setText(reesult.SSID);

                if (mWifiPwdMap.containsKey(reesult.SSID)) {
                    mPwdTv.setVisibility(View.VISIBLE);
                    mPwdTv.setText(mWifiPwdMap.get(reesult.SSID));
                } else {
                    mPwdTv.setVisibility(View.GONE);
                }
//
//                if (mInfo != null) {
//                    if (mInfo.getSSID() != null && (mInfo.getSSID().equals(reesult.SSID) || mInfo.getSSID().equals("\"" + reesult.SSID + "\""))) {
//                        mConnectTv.setVisibility(View.VISIBLE);
//                        int Ip = mInfo.getIpAddress();
//                        String strIp = "" + (Ip & 0xFF) + "." + ((Ip >> 8) & 0xFF) + "." + ((Ip >> 16) & 0xFF) + "." + ((Ip >> 24) & 0xFF);
//                        if (mInfo.getBSSID() != null && mInfo.getSSID() != null && strIp != null && !strIp.equals("0.0.0.0")) {
//                            mConnectTv.setText("已连接");
//                        } else {
//                            mConnectTv.setText("正在连接...");
//                        }
//                    } else {
//                        mConnectTv.setVisibility(View.GONE);
//                    }
//                } else {
//                    mConnectTv.setVisibility(View.GONE);
//                }

                mEncryptTv.setText(WifiUtil.getEncryptString(reesult.capabilities));

            }
            return convertView;
        }

    }

    class WifiActionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                goScanResult();
            }
        }
    }

    private void goScanResult() {
        mHandler.sendEmptyMessage(0);
    }

    class ScanResultTask extends AsyncTask<Void, Void, List<ScanResult>> {

        @Override
        protected List<ScanResult> doInBackground(Void... params) {
            return WifiUtil.getWifiScanResult(AIUIWifiActivity.this);
        }

        @Override
        protected void onPostExecute(List<ScanResult> result) {
            if (result != null) {
//                mButton.setVisibility(View.GONE);
                if (mListView.getVisibility() == View.GONE)
                    mListView.setVisibility(View.VISIBLE);
                mAdapter.refreshList(result);
            } else {
//                mButton.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);
            }
            super.onPostExecute(result);
        }
    }

    private Runnable mCallBack = new Runnable() {
        @Override
        public void run() {
            wm.startScan();
            mHandler.postDelayed(this, 500);
        }
    };


    @Override
    public void onWifiOpen(final int state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (state == WifiManager.WIFI_STATE_DISABLED) {
                    mListView.setVisibility(View.GONE);
                } else {
                    mHandler.post(mCallBack);
                }
            }
        });
    }


    public void wifiConnectedState() {
        mAgent.sendMessage(PacketBuilder.obtainWIFIStatusReqPacket());
    }

    @Override
    protected void onDestroy() {
//        lbm.unregisterReceiver(wifiConnected);
        super.onDestroy();
    }

    protected void onResume() {
        super.onResume();
        if (mWifiActionReceiver != null && mFilter != null)
            registerReceiver(mWifiActionReceiver, mFilter);
    }

    protected void onPause() {
        super.onPause();
        if (mWifiActionReceiver != null)
            unregisterReceiver(mWifiActionReceiver);
    }
}
