package com.csjbot.snowbot.utils.OkHttp;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.android.core.util.StrUtil;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;


/**
 * @author: jl
 * @Time: 2017/1/11
 * @Desc:
 */

public class LoadingDialog extends DialogFragment {
    public static LoadingDialog loadingDialog;
    private TextView loading_tv;
    private String content;
    private FragmentActivity activity;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.loading_layout, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT));
        loading_tv = (TextView) view.findViewById(R.id.loading_tv);
        if (StrUtil.isBlank(content)) {
            content = "加载中...";
        }
        loading_tv.setText(content);
        return view;
    }


    public static LoadingDialog getInstance() {
        if (null == loadingDialog) {
            loadingDialog = new LoadingDialog();
        }
        return loadingDialog;
    }

    public void showLoad(FragmentActivity activity) {
        this.showLoad(activity, "");
    }

    public void showLoad(FragmentActivity activity, String content) {
        this.content = content;
        this.activity = activity;
        Csjlogger.debug("loadingDialog " + loadingDialog.isVisible());
        if (loadingDialog != null && null != this.activity && !loadingDialog.isVisible()) {
//            loadingDialog.setCancelable(false);
            FragmentManager fm = activity.getSupportFragmentManager();
            loadingDialog.show(fm, "tag");
            Csjlogger.debug("loadingDialog " + loadingDialog.isVisible());
        }
    }


    public void dismissLoad() {
        if (loadingDialog != null && null != activity) {
            loadingDialog.dismiss();
        }
    }

    public boolean getStatus() {
        if (loadingDialog != null && null != activity) {
            return loadingDialog.isVisible();
        }
        return false;
    }



}
