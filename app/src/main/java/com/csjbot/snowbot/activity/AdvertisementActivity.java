package com.csjbot.snowbot.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import com.android.core.util.SharedUtil;
import com.android.core.util.StrUtil;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.services.AdvertisementService;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot_rogue.utils.CSJToast;
import com.jaygoo.widget.RangeSeekBar;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author: jl
 * @Time: 2017/1/9
 * @Desc:自定义广告
 */
public class AdvertisementActivity extends CsjUIActivity {

    @BindView(R.id.advertisement_et)
    EditText advertisementEt;
    @BindView(R.id.submit_btn)
    Button submitBtn;
    @BindView(R.id.RangeSeekBar)
    RangeSeekBar rangeSeekBar;

    private ArrayAdapter<String> adapter;
    private int repeatTime = 0;//-1为无限重复 0为未选择

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        setupBack(0);
        addTextWatcher(advertisementEt);
        repeatTime = SharedUtil.getPreferInt(SharedKey.REPEATTIME, 5);
        int process = 0;

        String advertisement = SharedUtil.getPreferStr(SharedKey.ADVERTISEMENT);
        advertisementEt.setText(advertisement);

        if (repeatTime == 5) {
            process = 0;
        } else if (repeatTime == 10) {
            process = 25;
        } else if (repeatTime == 60) {
            process = 50;
        } else if (repeatTime == 100) {
            process = 75;
        } else if (repeatTime == -1) {
            process = 100;
        }

        rangeSeekBar.setValue(process);

        rangeSeekBar.setOnRangeChangedListener((view, min, max, isFromUser) -> {
                    if (isFromUser) {
                        if (min == 0) {
                            repeatTime = 5;
                        } else if (min == 25) {
                            repeatTime = 10;
                        } else if (min == 50) {
                            repeatTime = 60;
                        } else if (min == 75) {
                            repeatTime = 100;
                        } else {
                            repeatTime = -1;
                        }
//                        Csjlogger.debug("min {}, max {}, repeatTime = {} ", min, max, repeatTime);
                    }
                }
        );
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_advertisement;
    }

    private void saveData() {
        String advertisement = advertisementEt.getText().toString().trim();
        if (StrUtil.isBlank(advertisement)) {
            CSJToast.showToast(this, "广告语不能为空");
            return;
        }
        if (repeatTime == 0) {
            CSJToast.showToast(this, "请选择播放次数");
            return;
        }
        SharedUtil.setPreferStr(SharedKey.ADVERTISEMENT, advertisement);
        SharedUtil.setPreferInt(SharedKey.REPEATTIME, repeatTime);
        Intent intent = new Intent(this, AdvertisementService.class);
        startService(intent);
        this.finish();
    }

    @NonNull
    private String StringFilter(String str) throws PatternSyntaxException {
        String regEx = "[/\\:*?<>|\"\n\t]"; //要过滤掉的字符
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    private void addTextWatcher(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                String editable = editText.getText().toString();
                String str = StringFilter(editable.toString());
                if (!editable.equals(str)) {
                    editText.setText(str);
                    editText.setSelection(str.length()); //光标置后
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    @OnClick(R.id.submit_btn)
    public void onClick() {
        saveData();
    }

}
