package com.csjbot.snowbot.activity.aiui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.aiui.base.AIUIActivity;
import com.csjbot.snowbot.bean.aiui.NewsBean;
import com.csjbot.snowbot.services.CsjSpeechSynthesizer2;
import com.csjbot.snowbot_rogue.Events.AIUIEvent;
import com.csjbot.snowbot_rogue.Events.EventsConstants;
import com.csjbot.snowbot_rogue.Events.ExpressionEvent;
import com.csjbot.snowbot_rogue.utils.Constant;

import java.util.List;


public class NewsActivityBean extends AIUIActivity {
    private static final String TAG = "NewsActivityBean";
    private String data;
    private List<NewsBean.ResultBean> newsList = null;
    private ListView listView;
    private Intent newsIntent;
    private Handler mHandler = new Handler();
    private boolean clickEvent = false;
    private int num = 0;

    @Override
    public boolean onAIUIEvent(AIUIEvent event) {
        if (super.onAIUIEvent(event)) {
            return true;
        }
        switch (event.getTag()) {
            case EventsConstants.AIUIEvents.MUSICX_NEWS_END:
                newsNext();
                break;
            default:
                Csjlogger.debug("event unCaptured   -》" + event.getTag());
                break;
        }
        return false;
    }

    private void newsNext() {
        num++;
        if (num > newsList.size()) {
            num = 0;
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                newsIntent.putExtra("imageUrl", newsList.get(num).getImgUrl());
                newsIntent.putExtra("url", newsList.get(num).getUrl());
                newsIntent.putExtra("title", newsList.get(num).getTitle());
                newsIntent.putExtra("clickEvent", clickEvent);
                startActivity(newsIntent);
            }
        }, 3000);
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);//must store the new intent unless getIntent() will return the old one
        newsDoData(intent.getStringExtra("data"));
    }

    private void newsDoData(String data) {
        NewsBean newsBean = JSON.parseObject(data, NewsBean.class);
        newsList = newsBean.getResult();

        listView.setAdapter(new MyAdapter(NewsActivityBean.this, R.layout.list_view));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                clickEvent = true;
                newsIntent.putExtra("imageUrl", newsList.get(position).getImgUrl());
                newsIntent.putExtra("url", newsList.get(position).getUrl());
                newsIntent.putExtra("title", newsList.get(position).getTitle());
                newsIntent.putExtra("clickEvent", clickEvent);
                startActivity(newsIntent);
            }
        });

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!clickEvent) {
                    newsIntent.putExtra("imageUrl", newsList.get(0).getImgUrl());
                    newsIntent.putExtra("url", newsList.get(0).getUrl());
                    newsIntent.putExtra("title", newsList.get(0).getTitle());
                    newsIntent.putExtra("clickEvent", clickEvent);
                    startActivity(newsIntent);
                }
            }
        }, 5000);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        listView = (ListView) findViewById(R.id.listView_news);
        newsIntent = new Intent(this, NewsActivity.class);

        data = getIntent().getStringExtra("data");
        NewsBean newsBean = JSON.parseObject(data, NewsBean.class);
        newsList = newsBean.getResult();
        // TODO: 2017/5/23 0023  此循环是拉下来的数据有重叠,判断过后留一条
        for (int i = 0; i < newsList.size(); i++) {
            if (newsList.get(i) == (newsList.get(i))) {
                newsList.remove(i);
            }
        }
        listView.setAdapter(new MyAdapter(this, R.layout.list_view));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                clickEvent = true;
                newsIntent.putExtra("imageUrl", newsList.get(position).getImgUrl());
                newsIntent.putExtra("url", newsList.get(position).getUrl());
                newsIntent.putExtra("title", newsList.get(position).getTitle());
                newsIntent.putExtra("clickEvent", clickEvent);
                startActivity(newsIntent);
            }
        });




        try {
            newsIntent.putExtra("imageUrl", newsList.get(0).getImgUrl());
            newsIntent.putExtra("url", newsList.get(0).getUrl());
            newsIntent.putExtra("title", newsList.get(0).getTitle());
            newsIntent.putExtra("clickEvent", clickEvent);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!clickEvent) {
                        startActivity(newsIntent);
                    }
                }
            }, 5000);
        }catch (IndexOutOfBoundsException e){
                CsjSpeechSynthesizer2.getSynthesizer().startSpeaking("没有获取到新闻，请换点别的吧", null);
                this.finish();
        }

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
        CsjSpeechSynthesizer2.getSynthesizer().stopSpeaking();
        postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_NORMAL));
        finish();
    }

    @Override
    protected void onDestroy() {
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
                convertView = LayoutInflater.from(NewsActivityBean.this).inflate(resource, null);
                viewHder.imageList_news = (ImageView) convertView.findViewById(R.id.imageList_news);
                viewHder.titleList_news = (TextView) convertView.findViewById(R.id.titleList_news);
                convertView.setTag(viewHder);
            } else {
                viewHder = (ViewHodler) convertView.getTag();
            }

            if (!TextUtils.isEmpty(newsList.get(position).getImgUrl())) {
                Glide.with(NewsActivityBean.this)
                        .load(newsList.get(position).getImgUrl())
                        .centerCrop()
                        .placeholder(R.drawable.loading)    // 缺省的占位图片，一般可以设置成一个加载中的进度GIF图
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(viewHder.imageList_news);
                viewHder.titleList_news.setText(newsList.get(position).getTitle());
            }
            return convertView;
        }

        @Override
        public int getCount() {
            // 假设加载的数据量很大
            return newsList != null ? newsList.size() : 0;
        }
    }

    class ViewHodler {
        ImageView imageList_news;
        TextView titleList_news;
    }

}
