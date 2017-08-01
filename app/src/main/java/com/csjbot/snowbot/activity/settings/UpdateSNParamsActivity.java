package com.csjbot.snowbot.activity.settings;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.csjbot.snowbot.Fragment.OutWareFrg;
import com.csjbot.snowbot.Fragment.UpdateRobotParamsFrg;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;

/**
 * Created by 孙秀艳 on 2017/5/17.
 * 更新SN参数
 */

public class UpdateSNParamsActivity extends CsjUIActivity {
    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        setupBack(0);
        UpdateRobotParamsFrg fragment = new UpdateRobotParamsFrg();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isAdd", false);//true 新增机器人参数  false 修改机器人参数
        fragment.setArguments(bundle);
        FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_content, fragment);
        ft.commitAllowingStateLoss();
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.act_update_robot_params;
    }
}
