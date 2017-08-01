package com.csjbot.snowbot.activity.face.ui.icount;

import android.Manifest;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.face.base.BaseActivity;
import com.csjbot.snowbot.activity.face.view.CameraFragment;
import com.tbruyelle.rxpermissions.RxPermissions;

import rx.functions.Action1;


/**
 * Created by mac on 2017/1/17 下午2:14.
 */

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class TestCamera2Activity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.CAMERA)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {
                            setFragment();
                        } else {
                            Toast.makeText(mContext, "请同意软件的权限，才能继续使用", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void initView() {

    }

    protected void setFragment() {
        final Fragment fragment = CameraFragment.newInstance();

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }


}
