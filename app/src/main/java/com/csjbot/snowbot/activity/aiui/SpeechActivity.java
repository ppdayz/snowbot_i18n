package com.csjbot.snowbot.activity.aiui;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.android.core.util.StrUtil;
import com.csjbot.csjbase.event.BusFactory;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.views.aiui.ChatMsgEntity;
import com.csjbot.snowbot.views.aiui.ChatMsgViewAdapter;
import com.csjbot.snowbot_rogue.Events.AIUIEvent;
import com.csjbot.snowbot_rogue.Events.EventsConstants;
import com.csjbot.snowbot_rogue.utils.CSJToast;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class SpeechActivity extends CsjUIActivity {
    private String question, answer;
    private int rc;
    private boolean speakTextRec = false, rcRec = false;

    private ListView mListView;
    private ChatMsgViewAdapter mAdapter;// 消息视图的Adapter
    private List<ChatMsgEntity> mDataArrays = new ArrayList<ChatMsgEntity>();// 消息对象数组
    public static final int AIUI_SPEAKTEXT_DATA_NOT_FINAL = 2103;


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
                question = (String) event.data;
//                if (question.equals("成语接龙")) {
//                    Intent intent = new Intent(this, WingedWordActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                    finish();
//                }
                speakTextRec = true;
                if (rcRec) {
                    speakTextRec = false;
                    rcRec = false;
                    mastersSend("rc=" + rc + " , " + question);
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
            case EventsConstants.AIUIEvents.AIUI_EVENT_WAKEUP:
                snowbotSend("在这呢，请问有什么可以帮你的吗");
                break;
            case EventsConstants.AIUIEvents.AIUI_EVENT_FORCE_SLEEP:
                this.finish();
                break;
            case AIUI_SPEAKTEXT_DATA_NOT_FINAL:
                CSJToast.showToast(this, (String) event.data);
                break;
            default:
                isCaptured = false;
                break;
        }
        return isCaptured;
    }


    public void backMethod(View view) {
        finish();
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
        entity.setMessage("小雪在这呢，请问有什么可以帮你的吗");
        entity.setMessage("Snow is Here, What Can I do for you?");
        entity.setMsgType(false);
        mDataArrays.add(entity);
        mAdapter = new ChatMsgViewAdapter(this, mDataArrays);
        mListView.setAdapter(mAdapter);
    }

    private void mastersSend(String content) {
        ChatMsgEntity entity = new ChatMsgEntity();
        entity.setName("主人");
        entity.setMessage(content);
        entity.setMsgType(true);

        mDataArrays.add(entity);
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
        return R.layout.list_speech;
    }
}
