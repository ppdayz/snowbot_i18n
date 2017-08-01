package com.csjbot.snowbot.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;

/**
 * @Author: jl
 * @Date: 2016/12/15
 * @Desc:
 */

public abstract class BaseFrg extends Fragment {
    protected Context context;
    protected View mRootView;
    public OnClickLister onClickLister = null;


    protected String title;


    public abstract int getContentViewId();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(getContentViewId(), container, false);
        ButterKnife.bind(this, mRootView);
        this.context = getActivity();
        return mRootView;
    }

    public void setOnClickLister(OnClickLister onClickLister) {
        this.onClickLister = onClickLister;
    }


    public interface OnClickLister {
        void clickYes();

        void clickNo();

        void longClick(MotionEvent event);

        void clickSkip();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            onVisible();
        } else {
            onInvisible();
        }
    }

    protected void onVisible() {
    }

    protected void onInvisible() {
    }
}
