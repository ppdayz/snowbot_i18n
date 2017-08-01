package com.csjbot.snowbot.activity.face.ui.icount;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.face.base.BaseActivity;
import com.csjbot.snowbot.activity.face.base.BaseApplication;
import com.csjbot.snowbot.activity.face.test.AttrTest;
import com.csjbot.snowbot.activity.face.ui.FaceRecoActivity;
import com.csjbot.snowbot.activity.face.ui.SettingActivity;
import com.tbruyelle.rxpermissions.RxPermissions;

import dou.utils.AppUtil;
import dou.utils.DLog;
import dou.utils.DisplayUtil;
import mobile.ReadFace.FaceAnalyze;
import rx.functions.Action1;


/**
 * Created by mac on 16/6/27.
 */


public class CountStartActivity extends BaseActivity implements View.OnClickListener {

    private final static int LINE = 3;
    TextView version;
    private LinearLayout select_center;
    private View _logo;

    private int texts[] = {R.string.count_start_2, R.string.count_start_1, R.string.count_start_3, R.string.count_start_4};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);
    }

    @Override
    protected void onResume() {
        super.onResume();
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
                        , Manifest.permission.READ_PHONE_STATE)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {
                            initView();
                        } else {
                            Toast.makeText(CountStartActivity.this, "请同意软件的权限，才能继续使用", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void initView() {

        version = (TextView) findViewById(R.id.version);
        String sdkVersion = FaceAnalyze.nativeSDKVersion();
        String appVersion = AppUtil.getVersion(this);
        sdkVersion = sdkVersion.replace("-", "\n");
        version.setText(sdkVersion + "\n" + "app-v : " + appVersion);
        _logo = findViewById(R.id._logo);
        _logo.getLayoutParams().width = getDoom(290);
        _logo.getLayoutParams().height = getDoom(58);
        if (BaseApplication.useLogo) _logo.setVisibility(View.VISIBLE);


        if (select_center != null && select_center.getChildCount() != 0)
            select_center.removeAllViews();
        select_center = (LinearLayout) findViewById(R.id.select_center);
        for (int i = 0; i < LINE; i++) {
            RelativeLayout clickView = new RelativeLayout(this);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(getDoom(600), getDoom(160));
            clickView.setBackgroundResource(R.drawable.start_button_selector);
            clickView.setLayoutParams(params);

            TextView textView = new TextView(this);
            params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            textView.setTextSize(DisplayUtil.px2sp(BaseApplication.getAppContext(), 54));
            textView.setTextColor(0xffffffff);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            clickView.addView(textView, params);
            textView.setSingleLine();
            textView.setText(texts[i]);
            View view = new View(this);
            params = new RelativeLayout.LayoutParams(60, 60);
            select_center.addView(clickView);
            select_center.addView(view, params);
            clickView.setTag(i);
            clickView.setOnClickListener(this);
        }
    }


    @Override
    public void onClick(View view) {
        switch ((int) view.getTag()) {
            case 1:
                startActivityForResult(new Intent(CountStartActivity.this, ManageFaceActivity.class), 100);
                break;
            case 0:
                startActivity(new Intent(CountStartActivity.this, FaceRecoActivity.class));
                break;
            case 2:
                startActivity(new Intent(CountStartActivity.this, MoreActivity.class));
                break;
            case 3:
                AttrTest test = new AttrTest(this, "/sdcard/img/video2");
                test.initTest();
                test.startTest();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        DLog.d(requestCode + ":" + resultCode);
        if (requestCode == 100 && resultCode == 101) {
            startActivity(new Intent(this, FaceRecoActivity.class));
        }
    }

    public void setting(View view) {
        startActivity(new Intent(this, SettingActivity.class));
    }
}
