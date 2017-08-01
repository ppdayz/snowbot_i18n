package com.csjbot.snowbot.activity.settings;

import android.os.Bundle;

import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot_rogue.Events.EventsConstants;
import com.csjbot.snowbot_rogue.Events.ExpressionEvent;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.csjbot.snowbot_rogue.utils.SharePreferenceTools;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SettingsChangeFaceActivity extends CsjUIActivity {
    private ColorPicker picker = null;
    private SharePreferenceTools sharePreferenceTools;
    private int mColor = 0;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        setupBack();

        sharePreferenceTools = new SharePreferenceTools(SettingsChangeFaceActivity.this);
        mColor = sharePreferenceTools.getInt(Constant.SharePreference.EXPRESSION_FACE_COLOR);

        picker = (ColorPicker) findViewById(R.id.picker);

        OpacityBar opacityBar = (OpacityBar) findViewById(R.id.opacitybar);
        SaturationBar saturationBar = (SaturationBar) findViewById(R.id.saturationbar);
        ValueBar valueBar = (ValueBar) findViewById(R.id.valuebar);

        picker.addOpacityBar(opacityBar);
        picker.addSaturationBar(saturationBar);
        picker.addValueBar(valueBar);


        picker.setOldCenterColor(mColor);
        picker.setNewCenterColor(mColor);
        picker.setColor(mColor);

        picker.setOnColorSelectedListener(new ColorPicker.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
//                Intent intent = new Intent(Constant.Expression.ACTION_CHANGE_FACE_COLOR);
//                intent.putExtra("color", color);
//                lbm.sendBroadcast(intent);
                postEvent(new ExpressionEvent(EventsConstants.ExpressionEvent.ACTION_CHANGE_FACE_COLOR, color));
            }
        });

        picker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
//                Intent intent = new Intent(Constant.Expression.ACTION_CHANGE_FACE_COLOR);
//                intent.putExtra("color", color);
//                lbm.sendBroadcast(intent);
                postEvent(new ExpressionEvent(EventsConstants.ExpressionEvent.ACTION_CHANGE_FACE_COLOR, color));
                Csjlogger.debug("color = " + Integer.toHexString(color).toUpperCase());
                sharePreferenceTools.putInt(Constant.SharePreference.EXPRESSION_FACE_COLOR, color);
            }
        });
        valueBar.setOnValueChangedListener(new ValueBar.OnValueChangedListener() {

            @Override
            public void onValueChanged(int value) {

            }
        });

        saturationBar.setOnSaturationChangedListener(new SaturationBar.OnSaturationChangedListener() {
            @Override
            public void onSaturationChanged(int saturation) {

            }
        });
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_settings_change_face;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAIUIEvent(int i) {
    }
}
