package com.csjbot.snowbot.activity.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.utils.UUIDGenerator;
import com.csjbot.snowbot.utils.UrlUtil;
import com.csjbot.snowbot.views.dialog.MyWaitingDialog;
import com.csjbot.snowbot_rogue.app.CsjSpeechSynthesizer;
import com.csjbot.snowbot_rogue.platform.SnowBotManager;
import com.csjbot.snowbot_rogue.platform.SnowBotNetwokListen;
import com.csjbot.snowbot_rogue.servers.slams.events.ConfigPageEntey;
import com.csjbot.snowbot_rogue.utils.CSJToast;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.OnClick;

public class WifiConfigAutomaticActivity extends CsjUIActivity {

    @BindView(R.id.webview)
    WebView webview;

    private MyWaitingDialog mWaitingDialog = null;

    private Handler mHandler = null;

    private String path = UrlUtil.ROUTER_IPADDR + "/cgi-bin/luci?username=root&&password=admin&getcontent=0";

    /**
     * 无线网络的ssid
     */
    private String ssid = "";
    /**
     * 无线网络的密码
     */
    private String pwd = "";

    /**
     * 从网页中拿到的index值
     */
    private int index = -1;

    /**
     * 间隔2秒
     */
    private int intervalTime = 2000;

    /**
     * 超时时间
     */
    private int timeOut = 2000 * 50;

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        initialize();
        setupBack();
        EventBus.getDefault().post(new ConfigPageEntey());
    }

    /**
     * 初始化
     */
    private void initialize() {
        mWaitingDialog = new MyWaitingDialog(this);
        mHandler = new Handler();

        // 截取uuid的后五位
        String uuid = UUIDGenerator.getInstance().getDeviceUUID();
        ssid = "SnowBot_" + uuid.substring((uuid.length() - 5));
        pwd = "12345678";

        WebSettings settings = webview.getSettings();
        settings.setSupportZoom(true);          //支持缩放
        settings.setBuiltInZoomControls(true);  //启用内置缩放装置
        settings.setJavaScriptEnabled(true);    //启用JS脚本

        // 设置js调用android方法
        webview.addJavascriptInterface(new JavaScriptInterface(), "JavaScriptInterface");
    }

    @OnClick({R.id.bt_automatic, R.id.bt_manual, R.id.btn_setSlam})
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.bt_automatic:// 自动设置wifi信息
                // 显示加载框
                showDialog();
                mHandler.postDelayed(mTimeOutRunnable, timeOut);
                setWifiInfo();
                break;
            case R.id.bt_manual:// 手动设置wifi信息
                //jumpActivity(WifiConfigManualActivity.class);
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri url = Uri.parse(path);
                intent.setData(url);
                startActivity(intent);
                break;
            case R.id.btn_setSlam:
                showDialog(getString(R.string.config_slam_info));
                SnowBotManager.getInstance().configNetWorkNew(new SnowBotNetwokListen() {
                    @Override
                    public void getWifiStatus(HashMap<String, String> status) {
                    }

                    @Override
                    public void configWifiState(boolean wifiCMDSendSuccess) {
                        if (wifiCMDSendSuccess) {
                            webview.post(() -> CSJToast.showToast(context, "底盘配置成功，请重新启动机器人！"));
                            CsjSpeechSynthesizer.getSynthesizer().startSpeaking("底盘配置成功，请重新启动机器人", null);
                            dismissDialog();
                        } else {
                            webview.post(() -> {
                                dismissDialog();
                                CSJToast.showToast(context, "底盘配置错误,请手动配置");
                            });
                        }
                    }
                });
                break;
            default:
                break;
        }
    }

    /**
     * activity跳转
     *
     * @param cls
     */
    public void jumpActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }

    /**
     * 自动配置wifi信息
     */
    public void setWifiInfo() {
        // 加载webview
        webview.loadUrl(path);
        webview.setWebViewClient(new WebViewClient() {
            //当点击链接时,希望覆盖而不是打开新窗口
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);  //加载新的url
                return true;    //返回true,代表事件已处理,事件流到此终止
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // 通过js调用修改wifi信息函数
                webview.loadUrl("javascript:changePanel('wiresetup','w_status=open&w_ssid=" + ssid + "&w_type=psk%2Bpsk2&w_pwd=" + pwd + "&w_code_t=psk%2Bpsk2&w_code=" + pwd + "&sid_hide=0&wifi_channel=auto&wifi_type=0')");
                mHandler.postDelayed(mCheckSucRunnable, intervalTime);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                connectFail();
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        });
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_wifi_config_automatic;
    }

    /**
     * 显示加载对话框
     */
    public void showDialog(final String content) {
        if (mWaitingDialog == null) {
            mWaitingDialog = new MyWaitingDialog(this);
        }
        mWaitingDialog.showDialog(getString(R.string.prompt), content);
    }

    /**
     * 显示加载对话框
     */
    public void showDialog() {
        if (mWaitingDialog == null) {
            mWaitingDialog = new MyWaitingDialog(this);
        }
        mWaitingDialog.showDialog(getString(R.string.prompt), getString(R.string.config_wifi_info));
    }

    /**
     * 关闭加载对话框
     */
    public void dismissDialog() {
        if (mWaitingDialog != null && mWaitingDialog.isShowing()) {
            mWaitingDialog.dismiss();
        }
    }

    /**
     * 是否配置成功检查
     */
    private Runnable mCheckSucRunnable = new Runnable() {
        @Override
        public void run() {
            // 如果index>0 则存在success,配置成功
            if (index > 0) {
                configSuccess();
            } else {
                mHandler.postDelayed(mCheckSucRunnable, intervalTime);
            }
            getIndex();
        }
    };

    /**
     * 超时检查
     */
    private Runnable mTimeOutRunnable = new Runnable() {
        @Override
        public void run() {
            if (mWaitingDialog.isShowing()) {
                connectFail();
            }
        }
    };

    /**
     * wifi配置成功
     */
    private void configSuccess() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mCheckSucRunnable);
            mHandler.removeCallbacks(mTimeOutRunnable);
        }
        dismissDialog();
        CSJToast.showToast(context, getString(R.string.config_success) + "," + getString(R.string.plese_restart));
    }


    /**
     * 网络问题
     */
    private void connectFail() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mCheckSucRunnable);
            mHandler.removeCallbacks(mTimeOutRunnable);
        }
        dismissDialog();
        CSJToast.showToast(context, getString(R.string.check_network));
    }


    /**
     * js调用本地方法代码(将网页中ssid标签的值传回)
     */
    private void getIndex() {
        // 返回网页内容包含success字符的下标值
        webview.loadUrl("javascript:function getIndex(){JavaScriptInterface.setIndex(document.getElementsByClassName('setup_c')[0].innerText.indexOf('success'))};");
        webview.loadUrl("javascript:getIndex();");


    }


    /**
     * @author jwc
     *         给js提供调用的类
     */
    public class JavaScriptInterface {

        /**
         * 给js提供的调用方法
         *
         * @param i 下标
         */
        @JavascriptInterface
        public void setIndex(int i) {
            index = i;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recycle();
    }

    /**
     * 回收资源
     */
    private void recycle() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mTimeOutRunnable);
            mHandler.removeCallbacks(mCheckSucRunnable);
            mHandler = null;
            mTimeOutRunnable = null;
            mCheckSucRunnable = null;
        }
        if (webview != null) {
            webview.removeAllViews();
            webview.destroy();
            webview = null;
        }
    }
}
