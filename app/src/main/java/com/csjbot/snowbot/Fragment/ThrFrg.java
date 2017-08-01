package com.csjbot.snowbot.Fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;

import com.android.core.entry.Static;
import com.android.core.util.MD5Util;
import com.android.core.util.SharedUtil;
import com.android.core.util.StrUtil;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.bean.BFeed;
import com.csjbot.snowbot_rogue.app.CsjSpeechSynthesizer;
import com.csjbot.snowbot.utils.OkHttp.DisposeDataListener;
import com.csjbot.snowbot.utils.OkHttp.HttpUtil;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot.utils.SharedPreferencesSDUtil;
import com.csjbot.snowbot.utils.UrlUtil;
import com.csjbot.snowbot_rogue.utils.CSJToast;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @Author: jl
 * @Date: 2016/12/15
 * @Desc:
 */

public class ThrFrg extends BaseFrg {

    @BindView(R.id.master_num_ed)
    EditText masterNumEd;
    @BindView(R.id.verify_num_ed)
    EditText verifyNumEd;
    @BindView(R.id.set_pad_ed)
    EditText setPadEd;
    @BindView(R.id.set_nickname_ed)
    EditText setNicknameEd;
    @BindView(R.id.verify_num_btn)
    Button verifyNumBtn;
    @BindView(R.id.yes_skip)
    Button yesSkip;


    private String masterNum;
    private String verifyNum;
    private String setPsd;
    private String masterNickName;
    private ValueAnimator valueAnimator;

    @Override
    public int getContentViewId() {
        return R.layout.frag_thr_layout;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, super.onCreateView(inflater, container, savedInstanceState));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @OnClick({R.id.no_btn, R.id.yes_btn, R.id.yes_skip})
    public void onClick(View view) {
        if (null == onClickLister) {
            return;
        }
        switch (view.getId()) {
            case R.id.no_btn:
                onClickLister.clickNo();
                break;
            case R.id.yes_btn:
                getData();
                break;
            case R.id.yes_skip:
                onClickLister.clickSkip();
                break;
        }
    }


    public void getData() {
        masterNum = masterNumEd.getText().toString();
        verifyNum = verifyNumEd.getText().toString();
        setPsd = setPadEd.getText().toString();
        masterNickName = setNicknameEd.getText().toString();
        if (StrUtil.isBlank(masterNum)) {
            CSJToast.showToast(getActivity(), getString(R.string.master_num_empaty));
            return;
        }

        if (StrUtil.isBlank(verifyNum)) {
            CSJToast.showToast(getActivity(), getString(R.string.verify_num_empaty));
            return;
        }

        if (StrUtil.isBlank(setPsd)) {
            CSJToast.showToast(getActivity(), getString(R.string.psd_num_empaty));
            return;
        } else if (setPsd.length() < 4) {
            CSJToast.showToast(getActivity(), getString(R.string.psd_num_style));
            return;
        }

        if (StrUtil.isBlank(masterNickName)) {
            CSJToast.showToast(getActivity(), getString(R.string.nickname_empaty));
            return;
        }
        String tempPsd = MD5Util.MD5(setPsd);
        if (StrUtil.isNotBlank(tempPsd)) {
            String firstTemp = tempPsd.substring(0, 2);
            String lastTemp = tempPsd.substring(2, tempPsd.length());
            setPsd = lastTemp + firstTemp;
        }

        SharedUtil.setPreferStr(SharedKey.MASTERNUM, masterNum);
        SharedUtil.setPreferStr(SharedKey.VERIFYNUM, verifyNum);
        SharedUtil.setPreferStr(SharedKey.PASSWORD, setPsd);
        SharedUtil.setPreferStr(SharedKey.MASTERNICKNAME, masterNickName);

        onClickLister.clickYes();
    }


    @OnClick(R.id.verify_num_btn)
    public void onClick() {
        if (checkPhoneNum()) {
            initAnimator();
            Map<String, String> map = new HashMap<>();
            map.put("mobile", SharedUtil.getPreferStr(SharedKey.MASTERNUM));
            String getAuthCodeUrl = (String) SharedPreferencesSDUtil.get(context, "urlUtil", SharedKey.GETAUTHCODE, UrlUtil.GETAUTHCODE);
            HttpUtil.postJson(null, getAuthCodeUrl, map, BFeed.class, new DisposeDataListener() {
                @Override
                public void onSuccess(Object responseObj) {
                    BFeed bFeed = (BFeed) responseObj;
                    if (!bFeed.getData().isFlag()) {
                        CSJToast.showToast(getActivity(), getString(R.string.check_phone_num));
                    }
                }

                @Override
                public void onFail(Object reasonObj) {
                    CSJToast.showToast(getActivity(), getString(R.string.check_phone_num_fail));
                }
            });
        }

    }

    private boolean checkPhoneNum() {
        masterNum = masterNumEd.getText().toString();
        if (StrUtil.isBlank(masterNum)) {
            CSJToast.showToast(getActivity(), getString(R.string.master_num_empaty));
            return false;
        }
        SharedUtil.setPreferStr(SharedKey.MASTERNUM, masterNum);
        return true;
    }


    public void initAnimator() {
        int time = 60;
        valueAnimator = ValueAnimator.ofInt(time, 0).setDuration(time * 1000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int i = (int) animation.getAnimatedValue();
                verifyNumBtn.setText(i + "S");
            }
        });
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                verifyNumBtn.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                verifyNumBtn.setText(getString(R.string.get_berify_num));
                verifyNumBtn.setEnabled(true);
                valueAnimator.removeAllUpdateListeners();
                valueAnimator.removeAllListeners();
            }
        });
        valueAnimator.start();
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        CsjSpeechSynthesizer.getSynthesizer().startSpeaking(Static.CONTEXT.getResources().getString(R.string.snowbot_registration), null);
    }

    @Override
    protected void onInvisible() {
        super.onInvisible();
        CsjSpeechSynthesizer.getSynthesizer().stopSpeaking();
        if (null != verifyNumBtn) {
            verifyNumBtn.setText(Static.CONTEXT.getResources().getString(R.string.get_berify_num));
            verifyNumBtn.setEnabled(true);
        }
        if (null != valueAnimator) {
            valueAnimator.cancel();
        }
    }
}
