package com.csjbot.snowbot.activity.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;

import butterknife.OnClick;

/**
 * @author jwc
 * Wifi配置选择页面
 */
public class WifiConfigActivity extends CsjUIActivity {

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        setupBack();
    }


    @OnClick({R.id.bt_aiui_config,R.id.bt_relay_config})
    public void onClick(View view){
        int id = view.getId();
        switch (id){
            case R.id.bt_aiui_config:// 语言Wifi信息配置
                jumpActivity(AIUIWifiActivity.class);
                break;
            case R.id.bt_relay_config:// Wifi中继信息配置
                jumpActivity(WifiConfigRelayActivity.class);
                break;
        }
    }

    /**
     * 跳转activity
     * @param cls
     */
    public void jumpActivity(Class<?> cls){
        Intent intent = new Intent(WifiConfigActivity.this,cls);
        startActivity(intent);
    }


    @Override
    public int getLayoutId() {
        return R.layout.activity_wifi_config;
    }

    @Override
    public void setListener() {

    }

}
