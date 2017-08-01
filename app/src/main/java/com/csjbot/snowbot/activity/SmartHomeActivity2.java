package com.csjbot.snowbot.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.views.LoopView.LoopView;
import com.csjbot.snowbot.views.LoopView.OnItemSelectedListener;

import java.util.ArrayList;

public class SmartHomeActivity2 extends CsjUIActivity implements View.OnClickListener {
    private RelativeLayout.LayoutParams layoutParams;
    private RelativeLayout rootview;
    private TextView tv_smartHome2;
    //    private ImageButton ib_head_return_2;
    private Button btn_yes;
    private String setData;

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        setContentView(R.layout.activity_smart_home2);
//        setupBack(Color.WHITE);
//        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//
//        initview();
//    }

    private void initview() {
        tv_smartHome2 = ((TextView) findViewById(R.id.tv_smartHome2));
//        ib_head_return_2 = ((ImageButton) findViewById(R.id.ib_head_return_2));
//        ib_head_return_2.setOnClickListener(this);
        btn_yes = ((Button) findViewById(R.id.btn_yes));
        btn_yes.setOnClickListener(this);
        rootview = ((RelativeLayout) findViewById(R.id.rootview));

        LoopView loopView = new LoopView(this);
        final ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i <= 200; i += 10) {
            list.add(i + "");
        }
        //设置是否循环播放
        //loopView.setNotLoop();
        //滚动监听
        loopView.setListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                tv_smartHome2.setText("空气指数超过" + list.get(index) + "，小雪为您自动开启净化器");
                setData = list.get(index);
            }
        });
        //设置原始数据
        loopView.setItems(list);
        //设置初始位置
        loopView.setInitPosition(16);
        tv_smartHome2.setText("空气指数超过" + list.get(16) + "，小雪为您自动开启净化器");
        setData = list.get(16);
        //设置字体大小
        loopView.setTextSize(30);
        rootview.addView(loopView, layoutParams);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
//            case R.id.ib_head_return_2:
//                this.finish();
//                break;
            case R.id.btn_yes:
                String newStr = tv_smartHome2.getText().toString();
                Intent intent = new Intent();
                intent.putExtra("newStr", newStr);
                intent.putExtra("data", setData);
                setResult(2207, intent);
                this.finish();
                break;

        }

    }

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        setupBack(Color.WHITE);
        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        initview();
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        return R.layout.activity_smart_home2;
    }
}
