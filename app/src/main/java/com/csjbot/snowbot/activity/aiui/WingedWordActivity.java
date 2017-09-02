package com.csjbot.snowbot.activity.aiui;

import android.os.Bundle;
import android.widget.ListView;

import com.android.core.util.StrUtil;
import com.csjbot.csjbase.event.BusFactory;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.services.CsjSpeechSynthesizer2;
import com.csjbot.snowbot.views.aiui.ChatMsgEntity;
import com.csjbot.snowbot.views.aiui.ChatMsgViewAdapter;
import com.csjbot.snowbot_rogue.Events.AIUIEvent;
import com.csjbot.snowbot_rogue.Events.EventsConstants;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WingedWordActivity extends CsjUIActivity {
    private String question, answer;
    private int rc;
    private boolean speakTextRec = false, rcRec = false;
    private String mITPK_url;
    private String mITPK_cy;
    private ListView mListView;
    private ChatMsgViewAdapter mAdapter;// 消息视图的Adapter
    private List<ChatMsgEntity> mDataArrays = new ArrayList<ChatMsgEntity>();// 消息对象数组


    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        ibus = BusFactory.getBus();
        ibus.register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public boolean onAIUIEvent(AIUIEvent event) {
        //        Csjlogger.debug("AIUIEvent " + event.getTag());
        boolean isCaptured = true;
        switch (event.getTag()) {
            case EventsConstants.AIUIEvents.AIUI_SPEAKTEXT_DATA:
                Csjlogger.error("question=================" + question);
                speakTextRec = true;
                if (rcRec) {
                    speakTextRec = false;
                    rcRec = false;
                    mastersSend("rc=" + rc + " , " + question);
                }
                question = (String) event.data;
                if (question.equals("再见")) {
                    finish();
                }
                break;
            case EventsConstants.AIUIEvents.AIUI_SPEAKTEXT_RC:
                rc = (int) event.data;
                rcRec = true;
                if (speakTextRec) {
                    speakTextRec = false;
                    rcRec = false;
                    mastersSend("rc=" + rc + " , " + question);
                }
                break;
            case EventsConstants.AIUIEvents.AIUI_ANSWERTEXT_DATA:
                answer = (String) event.data;
                if (StrUtil.isNotBlank(answer)) {
                    snowbotSend(answer);
                }
                break;

            case EventsConstants.AIUIEvents.AIUI_EVENT_FORCE_SLEEP:
                finish();
                break;
            default:
                isCaptured = false;
                break;
        }
        return isCaptured;
    }

    @Override
    protected void onDestroy() {
        if (ibus != null) {
            ibus.unregister(this);
        }
        super.onDestroy();
    }

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        mListView = (ListView) findViewById(R.id.speechListview);
        initData();// 初始化数据
        setupBack();

        mListView.setSelection(mAdapter.getCount() - 1);
    }

    public void initData() {
        ChatMsgEntity entity = new ChatMsgEntity();
        entity.setName("机器人");
        entity.setMessage("来呀互相伤害啦~");
        entity.setMsgType(false);
        mDataArrays.add(entity);
        mAdapter = new ChatMsgViewAdapter(this, mDataArrays);
        mListView.setAdapter(mAdapter);
        CsjSpeechSynthesizer2.getSynthesizer().startSpeaking(entity.getMessage(), null);
    }

    private void mastersSend(String content) {
        ChatMsgEntity entity = new ChatMsgEntity();
        entity.setName("主人");
        entity.setMessage(content);
        entity.setMsgType(true);
        //成语接龙
        mITPK_cy = question;
        String ITPK_KEY = "&api_key=b96ff49c1c84b64cd2c5c516ee5461ae&api_secret=zylwjeg5ynhx";
        mITPK_url = "http://i.itpk.cn/api.php?question=@cy" + mITPK_cy + ITPK_KEY;
        getAsynHttp();
        mDataArrays.add(entity);
        Csjlogger.error("tag", "mastersSend:===================== " + mDataArrays);
        mAdapter.notifyDataSetChanged();// 通知ListView，数据已发生改变
        mListView.setSelection(mListView.getCount() - 1);// 发送一条消息时，ListView显示选择最后一项
    }

    private void snowbotSend(String content) {
        ChatMsgEntity entity = new ChatMsgEntity();
        entity.setName("机器人");
        entity.setMessage(content);
        entity.setMsgType(false);

        mDataArrays.add(entity);
        mAdapter.notifyDataSetChanged();// 通知ListView，数据已发生改变
        mListView.setSelection(mListView.getCount() - 1);// 发送一条消息时，ListView显示选择最后一项
    }


    @Override
    public void setListener() {
    }

    @Override
    public int getLayoutId() {
        return R.layout.list_idiom_speech;
    }

    private void getAsynHttp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 01. 定义okhttp
                OkHttpClient okHttpClient_get = new OkHttpClient();
                // 02.请求体
                Request request = new Request.Builder()
                        .get()//get请求方式
                        .url(mITPK_url)//网址
                        .build();

                // 03.执行okhttp

                try {
                    Response response = okHttpClient_get.newCall(request).execute();
                    String itpk = response.body().string();
                    //                    String itpk = str.substring(1);//请求下来的第一位有符号需要切割掉
                    Csjlogger.error("itpk========" + itpk);
                    Csjlogger.error("ITPK============" + mITPK_cy);
                    CsjSpeechSynthesizer2.getSynthesizer().startSpeaking(itpk, null);
                    postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_ANSWERTEXT_DATA, itpk));
                } catch (IOException e) {
                    e.printStackTrace();
                    Csjlogger.debug("IOException" + e.getMessage());
                }
            }
        }).start();
    }
}
