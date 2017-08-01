package com.csjbot.snowbot.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.android.core.entry.Static;
import com.android.core.util.SharedUtil;
import com.android.core.util.StrUtil;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot_rogue.app.CsjSpeechSynthesizer;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot_rogue.utils.CSJToast;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @Author: jl
 * @Date: 2016/12/15
 * @Desc:
 */

public class FourthFrg extends BaseFrg {
    private String robotNickName;

    @BindView(R.id.set_robot_nickname)
    EditText setRobotNickname;


    @Override
    public int getContentViewId() {
        return R.layout.frag_fourth_layout;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

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
                robotNickName = setRobotNickname.getText().toString();
                if (StrUtil.isNotBlank(robotNickName)) {
                    SharedUtil.setPreferStr(SharedKey.ROBOTNICKNAME, robotNickName);
                    onClickLister.clickYes();
                } else {
                    CSJToast.showToast(getActivity(), getString(R.string.robot_nickname_empty));
                }
                break;
        }
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        CsjSpeechSynthesizer.getSynthesizer().startSpeaking(Static.CONTEXT.getResources().getString(R.string.snowbot_name), null);
    }

    @Override
    protected void onInvisible() {
        super.onInvisible();
        CsjSpeechSynthesizer.getSynthesizer().stopSpeaking();
    }
}
