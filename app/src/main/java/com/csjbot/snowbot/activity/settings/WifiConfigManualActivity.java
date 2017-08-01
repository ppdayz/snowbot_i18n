package com.csjbot.snowbot.activity.settings;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.utils.UrlUtil;
import com.csjbot.snowbot.views.dialog.MyWaitingDialog;
import com.csjbot.snowbot_rogue.utils.CSJToast;

import butterknife.BindView;

/**
 * @author jwc
 *         wifi手动配置页面
 */
public class WifiConfigManualActivity extends CsjUIActivity {


    @BindView(R.id.webview)
    WebView webview;

    private MyWaitingDialog mWaitingDialog = null;

    private Handler mHandler = null;

    /**
     * 超时时间
     */
    private int timeOut = 2000 * 50;


    @Override
    public void setListener() {

    }

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {

        setupBack();

        showDialog();

        initWebView();
    }

    /**
     * 初始化webview
     */
    public void initWebView() {
        mHandler = new Handler();

        WebSettings settings = webview.getSettings();
        settings.setSupportZoom(false);          //支持缩放
        settings.setBuiltInZoomControls(true);  //启用内置缩放装置
        settings.setJavaScriptEnabled(true);    //启用JS脚本

        /**
         * 放大
         */
        webview.setInitialScale(180);

        // 加载url地址
        webview.loadUrl(UrlUtil.ROUTER_IPADDR + "/cgi-bin/luci?username=root&&password=admin&getcontent=0");
        webview.setWebViewClient(mWebViewClient);
        mHandler.postDelayed(mTimeOutRunnable, timeOut);

    }

    private Runnable mTimeOutRunnable = new Runnable() {
        @Override
        public void run() {
            if (mWaitingDialog.isShowing()) {
                connectFail();
            }
        }
    };

    /**
     * 网络问题
     */
    private void connectFail() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mTimeOutRunnable);
        }
        dismissDialog();
        CSJToast.showToast(context, getString(R.string.check_network));
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        //当点击链接时,希望覆盖而不是打开新窗口
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);  //加载新的url
            return true;    //返回true,代表事件已处理,事件流到此终止
        }


        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            hiddenDiv();
            dismissDialog();
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            CSJToast.showToast(context, getString(R.string.plese_check_network));
            dismissDialog();
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    };

    /**
     * 使用js代码隐藏Div布局
     */
    public void hiddenDiv() {
        // 使用js隐藏div方法
        webview.loadUrl("javascript:function hidden(){document.getElementsByClassName('nav')[0].style.display='none';" +
                "document.getElementsByClassName('apps common')[0].style.display='none';" +
                "document.getElementsByClassName('status')[0].style.display='none';" +
                "document.getElementsByClassName('setup')[0].style.width='800px';" +
                "}");
        // 调用隐藏div的function
        webview.loadUrl("javascript:hidden();");
        // 显示wifi修改页面
        webview.loadUrl("javascript:changePanel('wifinfo_24')");


        if (!webview.isShown()) {
            webview.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_wifi_config_manual;
    }

    /**
     * 显示加载对话框
     */
    public void showDialog() {
        if (mWaitingDialog == null) {
            mWaitingDialog = new MyWaitingDialog(this);
        }
        mWaitingDialog.showDialog(getString(R.string.prompt), getString(R.string.loading_page));
    }

    /**
     * 关闭加载对话框
     */
    public void dismissDialog() {
        if (mWaitingDialog != null && mWaitingDialog.isShowing()) {
            mWaitingDialog.dismiss();
            mWaitingDialog = null;
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
            mHandler = null;
            mTimeOutRunnable = null;
        }
        if (webview != null) {
            webview.removeAllViews();
            webview.destroy();
            webview = null;
        }
    }
}
