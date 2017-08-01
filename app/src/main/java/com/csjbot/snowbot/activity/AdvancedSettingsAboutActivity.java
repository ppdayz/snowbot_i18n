package com.csjbot.snowbot.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.core.util.SharedUtil;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.utils.SharedKey;
import com.hanks.library.AnimateCheckBox;

import butterknife.BindView;

/**
 * @author: jl
 * @Time: 2017/1/5
 * @Desc:高级设置页面
 */
public class AdvancedSettingsAboutActivity extends CsjUIActivity {
    @BindView(R.id.totleBack)
    ImageView totleBack;
    @BindView(R.id.firstCheckbox)
    AnimateCheckBox firstCheckbox;
    @BindView(R.id.activity_settings_set_touch_action)
    RelativeLayout activitySettingsSetTouchAction;

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        setupBack();
        if (SharedUtil.getPreferBool(SharedKey.FACERECOGNITION, false)) {
            firstCheckbox.setChecked(true);
        } else {
            firstCheckbox.setChecked(false);
        }
        firstCheckbox.setOnCheckedChangeListener(new AnimateCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View buttonView, boolean isChecked) {
                SharedUtil.setPreferBool(SharedKey.FACERECOGNITION, isChecked);
            }
        });
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_advanced_settings_action;
    }

}
