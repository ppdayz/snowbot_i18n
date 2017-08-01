package com.csjbot.snowbot.activity.aiui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.webkit.WebView;

import com.csjbot.snowbot_rogue.utils.Constant;


public class WebActivity extends Activity {
    private WebView webview;
    private String str;
    private LocalBroadcastManager localBroadcastManager;
    private boolean autoBack = false;
    private Handler mHandler = new Handler();

    private BroadcastReceiver wakeupReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 200);

        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onBackPressed();
                }
            }, 1000);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        //实例化WebView对象
        webview = new WebView(this);
        webview.getSettings().setJavaScriptEnabled(true);
        str = getIntent().getStringExtra("url");
        autoBack = getIntent().getBooleanExtra("autoBack", false);

        if (autoBack) {
            localBroadcastManager.registerReceiver(receiver, new IntentFilter(Constant.ACTION_SPEACH_END));
        }
        localBroadcastManager.registerReceiver(wakeupReceiver, new IntentFilter(Constant.ACTION_WAKEUP));

        //设置Web视图
        setContentView(webview);

        //加载需要显示的网页
        webview.loadUrl(str);
    }

    @Override
    public void onBackPressed() {
        localBroadcastManager.unregisterReceiver(receiver);
        localBroadcastManager.unregisterReceiver(wakeupReceiver);
        super.onBackPressed();
    }
}

