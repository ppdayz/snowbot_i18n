package com.csjbot.snowbot.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.core.entry.Static;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot_rogue.app.CsjSpeechSynthesizer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

/**
 * @Author: jl
 * @Date: 2016/12/15
 * @Desc:
 */

public class FirstFrg extends BaseFrg {


    @BindView(R.id.skip_registration)
    TextView skipRegistration;

    @Override
    public int getContentViewId() {
        return R.layout.frag_first_layout;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, super.onCreateView(inflater, container, savedInstanceState));
        return super.onCreateView(inflater, container, savedInstanceState);
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
    protected void onVisible() {
        super.onVisible();
        CsjSpeechSynthesizer.getSynthesizer().startSpeaking(Static.CONTEXT.getResources().getString(R.string.snowbot_home), null);
    }

    @Override
    protected void onInvisible() {
        super.onInvisible();
//        CsjSpeechSynthesizer.getSynthesizer().stopSpeaking();
    }

    @OnTouch(R.id.skip_registration)
    public boolean onTouchEvent(MotionEvent event) {
        if (null == onClickLister) {
            return false;
        }
        onClickLister.longClick(event);
        return true;
    }

}
