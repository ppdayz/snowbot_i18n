package com.csjbot.snowbot.activity.aiui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.aiui.base.AIUIActivity;
import com.csjbot.snowbot.bean.aiui.CookBean;
import com.csjbot.snowbot_rogue.app.CsjSpeechSynthesizer;
import com.csjbot.snowbot_rogue.utils.Constant;

import java.util.List;

import static com.csjbot.snowbot.R.id.imageList_news;


public class CookActivityBean extends AIUIActivity {
    private String data;
    private List<CookBean.ResultBean> cookList = null;
    private ListView listView;
    private Intent isMusicInt = new Intent(Constant.ACTION_IN_ACTIVITY);
    private Intent cookIntent;

    private BroadcastReceiver newsDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            data = intent.getStringExtra("data");
            CookBean cookBean = JSON.parseObject(data, CookBean.class);
            cookList = cookBean.getResult();
        }
    };

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        listView = (ListView) findViewById(R.id.listView_news);
        cookIntent = new Intent(this, CookActivity.class);

        data = getIntent().getStringExtra("data");
        CookBean cookBean = JSON.parseObject(data, CookBean.class);
        cookList = cookBean.getResult();

        listView.setAdapter(new MyAdapter(this, R.layout.list_view));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cookIntent.putExtra("imageUrl", cookList.get(position).getImgUrl());
                cookIntent.putExtra("title", cookList.get(position).getTitle());
                cookIntent.putExtra("accessory", cookList.get(position).getAccessory());
                cookIntent.putExtra("ingredient", cookList.get(position).getIngredient());
                cookIntent.putExtra("tag", cookList.get(position).getTag());
                cookIntent.putExtra("steps", cookList.get(position).getSteps());
                startActivity(cookIntent);
            }
        });

//        localBroadcastManager.registerReceiver(newsDataReceiver, new IntentFilter(Constant.ACTION_COOKBOOK_DATA));
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_news_bean;
    }

    @Override
    public void onBackPressed() {
        CsjSpeechSynthesizer.getSynthesizer().stopSpeaking();
        super.onBackPressed();
        this.finish();
    }

    @Override
    protected void onDestroy() {
//        localBroadcastManager.unregisterReceiver(newsDataReceiver);
        super.onDestroy();
    }

    private class MyAdapter extends ArrayAdapter {

        private int resource;

        public MyAdapter(Context context, int resource) {
            super(context, resource);
            this.resource = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHodler viewHder;
            if (convertView == null) {
                viewHder = new ViewHodler();
                convertView = LayoutInflater.from(CookActivityBean.this).inflate(resource, null);
                viewHder.imageList_news = (ImageView) convertView.findViewById(imageList_news);
                viewHder.titleList_news = (TextView) convertView.findViewById(R.id.titleList_news);
                convertView.setTag(viewHder);
            } else {
                viewHder = (ViewHodler) convertView.getTag();
            }

            if (!TextUtils.isEmpty(cookList.get(position).getImgUrl())) {
                Glide.with(CookActivityBean.this)
                        .load(cookList.get(position).getImgUrl())
                        .centerCrop()
                        .placeholder(R.drawable.loading)    // 缺省的占位图片，一般可以设置成一个加载中的进度GIF图
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(viewHder.imageList_news);
                viewHder.titleList_news.setText(cookList.get(position).getTitle());
            }

            return convertView;
        }

        @Override
        public int getCount() {
            // 假设加载的数据量很大
            return cookList != null ? cookList.size() : 0;
        }
    }

    class ViewHodler {
        ImageView imageList_news;
        TextView titleList_news;
    }

}
