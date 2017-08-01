package com.csjbot.snowbot.utils.OkHttp;

import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSON;
import com.android.core.entry.Static;
import com.android.core.util.CheckUtil;
import com.android.core.util.StrUtil;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot_rogue.utils.CSJToast;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * @author: jl
 * @Time: 2016/10/30
 * @Desc:网络回调监听
 */

public class CommonJsonCallback implements Callback {
    private static final String TAG = "TAG";
    private DisposeDataListener listener;
    private Class<?> mClass;
    private Handler mHanlder;

    public CommonJsonCallback(DisposeDataHandle handle) {
        this.listener = handle.mListener;
        this.mClass = handle.mclass;
        this.mHanlder = new Handler(Looper.getMainLooper());
    }


    @Override
    public void onFailure(Call call, final IOException e) {
        mHanlder.post(new Runnable() {
            @Override
            public void run() {
                LoadingDialog.getInstance().dismissLoad();
                CSJToast.showToast(Static.CONTEXT, Static.CONTEXT.getResources().getString(R.string.check_network_setting), 2000);
                listener.onFail(new OkHttpException(OkHttpException.REASON_NETWORK));
            }
        });
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        final String result = response.body().string();
        mHanlder.post(new Runnable() {
            @Override
            public void run() {
                Csjlogger.debug("注册返回 " + result);
                handleResponse(result);
            }
        });
    }


    private void handleResponse(String result) {
        LoadingDialog.getInstance().dismissLoad();
        if (StrUtil.isBlank(result)) {
            listener.onFail(new OkHttpException(OkHttpException.REASON_RESP_BLANK));
            return;
        }

        try {
            JSONObject jsonObj = new JSONObject(result);
            if (CheckUtil.isNull(mClass)) {
                listener.onSuccess(jsonObj);
            } else {
//                Object obj = new Gson().fromJson(result, mClass);
                Object obj = JSON.parseObject(result, mClass);
                if (CheckUtil.isNull(obj)) {
                    listener.onFail(new OkHttpException(OkHttpException.REASON_ALL_READY_REG));
                } else {
                    listener.onSuccess(obj);
                }
            }

        } catch (Exception e) {
            listener.onFail(new OkHttpException(OkHttpException.REASON_PARSER_JSON_ERROR));
        }
    }


}
