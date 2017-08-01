package com.csjbot.snowbot.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot.services.FloatingWindowsService;

import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/8/25 0025.
 */
public abstract class CsjBaseActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //隐藏状态栏
        //定义全屏参数
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //获得当前窗体对象
        Window window = this.getWindow();
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);
    }

    protected final void setupBack() {
        setupBack(0);
    }

    protected final void setupBack(int color) {
        ImageView back = (ImageView) findViewById(R.id.totleBack);
        if (back != null) {

            if (color == Color.WHITE) {
                back.setImageResource(R.mipmap.back_white);
            }

            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    public void showFloatingWindow() {
        Intent intent = new Intent(this, FloatingWindowsService.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
