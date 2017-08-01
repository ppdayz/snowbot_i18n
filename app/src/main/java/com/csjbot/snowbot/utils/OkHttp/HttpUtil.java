package com.csjbot.snowbot.utils.OkHttp;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

import com.android.core.entry.Static;
import com.android.core.net.netstate.NetWorkUtil;
import com.android.core.util.AppToast;
import com.android.core.util.SharedUtil;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot_rogue.utils.CSJToast;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;


/**
 * @author: jl
 * @Time: 2016/10/31
 * @Desc:
 */

public class HttpUtil {
    private static final int TIME_OUT = 15;
    private static OkHttpClient mOkHttpClient;

    static {
        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
        okHttpBuilder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        okHttpBuilder.connectTimeout(TIME_OUT, TimeUnit.SECONDS);
        okHttpBuilder.readTimeout(TIME_OUT, TimeUnit.SECONDS);
        okHttpBuilder.writeTimeout(TIME_OUT, TimeUnit.SECONDS);
        okHttpBuilder.followRedirects(true);
        okHttpBuilder.cookieJar(new SimpleCookieJar());
//        okHttpBuilder.authenticator(new Authenticator() {
//            @Override
//            public Request authenticate(Route route, Response response) throws IOException {
//                String credential = Credentials.basic("admin", "123456");
//                return response.request().newBuilder().header("Authorization", credential).build();
//            }
//        });
        mOkHttpClient = okHttpBuilder.build();
    }


    public static void post(Context context, String url, Map<String, String> map, Class<?> clazz, DisposeDataListener mListener) {
//        if (!NetWorkUtil.isNetworkConnected(Static.CONTEXT)) {
//            AppToast.ShowToast(Static.CONTEXT.getString(R.string.tip_network_overtime));
//            return;
//        }
        Request request = CommonRequest.postRequest(url, map);
        DisposeDataHandle disposeDataHandle = new DisposeDataHandle(mListener, clazz);
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new CommonJsonCallback(disposeDataHandle));
    }

    public static void postStrJson(Context context, String url, String strJson, Class<?> clazz, DisposeDataListener mListener) {
        if (!SharedUtil.getPreferBool(SharedKey.NETWORKSTATUS, false)) {
            CSJToast.showToast(Static.CONTEXT, Static.CONTEXT.getString(R.string.tip_network_overtime), 2000);
            return;
        }
        Request request = CommonRequest.postStrJsonRequest(url, strJson);
        DisposeDataHandle disposeDataHandle = new DisposeDataHandle(mListener, clazz);
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new CommonJsonCallback(disposeDataHandle));
    }

    public static void postJson(Context context, String url, Map<String, String> map, Class<?> clazz, DisposeDataListener mListener) {
        if (!SharedUtil.getPreferBool(SharedKey.NETWORKSTATUS, false)) {
            CSJToast.showToast(Static.CONTEXT, Static.CONTEXT.getString(R.string.tip_network_overtime), 2000);
            return;
        }

        if (null != context) {
            LoadingDialog.getInstance().showLoad((FragmentActivity) context);
        }
        Request request = CommonRequest.postJsonRequest(url, map);
        DisposeDataHandle disposeDataHandle = new DisposeDataHandle(mListener, clazz);
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new CommonJsonCallback(disposeDataHandle));
    }


    public static void get(Context context, String url, Map<String, String> map, Class<?> clazz, DisposeDataListener mListener) {
        if (!NetWorkUtil.isNetworkConnected(Static.CONTEXT)) {
            CSJToast.showToast(context.getApplicationContext(), context.getApplicationContext().getString(R.string.tip_network_overtime));
            return;
        }
        Request request = CommonRequest.getRequest(url, map);
        DisposeDataHandle disposeDataHandle = new DisposeDataHandle(mListener, clazz);
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new CommonJsonCallback(disposeDataHandle));
    }

    /**
     * @author: jl
     * @Time: 2016/10/31:21:58
     * @Desc: 文件上传
     */

    public static void uploadFile(Context context, String url, Map<String, Object> map, Class<?> clazz, DisposeDataListener mListener) {
        if (!NetWorkUtil.isNetworkConnected(Static.CONTEXT)) {
            AppToast.ShowToast(Static.CONTEXT.getString(R.string.tip_network_overtime));
            return;
        }
        Request request = CommonRequest.upLoad(url, map);
        DisposeDataHandle disposeDataHandle = new DisposeDataHandle(mListener, clazz);
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new CommonJsonCallback(disposeDataHandle));

    }

    /**
     * @author: jl
     * @Time: 2016/11/1:9:45
     * @Desc: 文件下载
     */

    public static void downloadFile(Context context, String downloadUrl, String filePath, DisposeDataListener mListener) {
        if (!NetWorkUtil.isNetworkAvailable(Static.CONTEXT)) {
            AppToast.ShowToast(Static.CONTEXT.getString(R.string.tip_network_overtime));
            return;
        }
        DownloadFileHandle downloadFileHandle = new DownloadFileHandle(mListener, filePath);
        Request request = CommonRequest.downloadFile(downloadUrl);
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new DownloadFileCallBack(downloadFileHandle));
    }


}
