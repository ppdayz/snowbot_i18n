package com.csjbot.snowbot.activity.aiui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.aiui.base.AIUIActivity;
import com.csjbot.snowbot.bean.aiui.CookBean;
import com.csjbot.snowbot.bean.aiui.entity.CsjSynthesizerListener;
import com.csjbot.snowbot.services.CsjSpeechSynthesizer2;
import com.csjbot.snowbot.utils.SpeechStatus;
import com.csjbot.snowbot_rogue.Events.AIUIEvent;
import com.csjbot.snowbot_rogue.Events.EventsConstants;
import com.csjbot.snowbot_rogue.Events.ExpressionEvent;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.iflytek.cloud.SpeechError;

import java.util.List;


public class CookActivity extends AIUIActivity {
    private Handler mHandler = new Handler();

    private String data;
    private List<CookBean.ResultBean> cookList = null;
    private String imageUrl, title, accessory, ingredient, steps, tag;

    private ImageView image_cook;
    private TextView title_cook;
    private TextView text_accessory;
    private TextView text_ingredient;
    private TextView text_steps;
    private TextView text_tag;

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);//must store the new intent unless getIntent() will return the old one
        cookBookDoData(intent.getStringExtra("data"));
    }

    private void cookBookDoData(String data) {
        CookBean cookBean = JSON.parseObject(data, CookBean.class);
        cookList = cookBean.getResult();

        imageUrl = cookList.get(0).getImgUrl();
        title = cookList.get(0).getTitle();
        accessory = cookList.get(0).getAccessory();
        ingredient = cookList.get(0).getIngredient();
        steps = cookList.get(0).getSteps();
        tag = cookList.get(0).getTag();

        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        data = getIntent().getStringExtra("data");
        cookBookDoData(data);
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_cook;
    }

    private void init() {
        image_cook = (ImageView) findViewById(R.id.image_cook);
        title_cook = (TextView) findViewById(R.id.title_cook);
        title_cook.setText(title);
        text_accessory = (TextView) findViewById(R.id.text_accessory);
        text_accessory.setText(accessory);
        text_ingredient = (TextView) findViewById(R.id.text_ingredient);
        text_ingredient.setText(ingredient);
        text_steps = (TextView) findViewById(R.id.text_steps);
        text_steps.setText(steps);
        text_tag = (TextView) findViewById(R.id.title_cookTag);
        text_tag.setText(tag);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(imageUrl)) {
                    Glide.with(CookActivity.this)
                            .load(imageUrl)
                            .centerCrop()
                            .placeholder(R.drawable.loading)    // 缺省的占位图片，一般可以设置成一个加载中的进度GIF图
                            .crossFade()
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .into(image_cook);
                }
            }
        });
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                cookBookRead(steps);
            }
        }, 1500);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void cookBookRead(String cookBookText) {
        postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_ANSWERTEXT_DATA, cookBookText));
        CsjSpeechSynthesizer2.getSynthesizer().startSpeaking(cookBookText, new CsjSynthesizerListener() {
            @Override
            public void onSpeakBegin() {
                SpeechStatus.getIstance().setSpeakFinished(false);
                postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_SPEAK));
            }

            @Override
            public void onCompleted(SpeechError speechError) {
                SpeechStatus.getIstance().setSpeakFinished(true);
                postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_NORMAL));
            }
        });
    }
}
