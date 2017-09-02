package com.csjbot.snowbot.activity.aiui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.aiui.base.AIUIActivity;
import com.csjbot.snowbot.services.CsjSpeechSynthesizer2;
import com.csjbot.snowbot.utils.SpeechStatus;
import com.csjbot.snowbot_rogue.Events.AIUIEvent;
import com.csjbot.snowbot_rogue.Events.EventsConstants;
import com.csjbot.snowbot_rogue.Events.ExpressionEvent;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SynthesizerListener;

public class PoetryActivity extends AIUIActivity {
    private TextView poetry_question;
    private TextView poetry_answer;
    private ImageView poetry_image;
    private FrameLayout poetry_image_layout;
    private FrameLayout poetry_text_layout;
    private String questionText, answerText;

    private ScrollView scroll_news;

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        questionText = getIntent().getStringExtra("speakText");
        answerText = getIntent().getStringExtra("answerText");
        init();

        setAnswerText(answerText);
    }

    private void setAnswerText(String answerText) {
        if (answerText.contains("[k3]") && answerText.contains("[k0]")) {
            answerText = answerText.replace("[k3]", "");
            answerText = answerText.replace("[k0]", "");
        }

        //获取view的高度
        final ViewTreeObserver vto = poetry_answer.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mLayout = poetry_answer.getLayout();
                poetry_answer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        if (answerText.equals("0")) {
            poetrySpeak("小雪没有搜到相关答案呢，小雪会继续学习的");
            answerText = "小雪没有搜到相关答案呢，小雪会继续学习的";
        } else {
            poetrySpeak(answerText);
        }

        layoutSetting(answerText);
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);//must store the new intent unless getIntent() will return the old one

        questionText = intent.getStringExtra("speakText");
        answerText = intent.getStringExtra("answerText");
        setAnswerText(answerText);
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_poetry;
    }

    private void init() {
        poetry_text_layout = (FrameLayout) findViewById(R.id.poetry_text_layout);
        poetry_image_layout = (FrameLayout) findViewById(R.id.poetry_image_layout);
        poetry_image = (ImageView) findViewById(R.id.poetry_image);
        poetry_answer = (TextView) findViewById(R.id.poetry_answer);
        poetry_question = (TextView) findViewById(R.id.poetry_question);
        scroll_news = (ScrollView) findViewById(R.id.sroll_news);
    }

    private void layoutSetting(String answerText) {
        if (questionText.contains("春晓")) {
            poetry_text_layout.setVisibility(View.GONE);
            poetry_image_layout.setVisibility(View.VISIBLE);
            poetry_image_layout.setBackgroundResource(R.drawable.chunxiao);
        } else if (questionText.contains("静夜思")) {
            poetry_text_layout.setVisibility(View.GONE);
            poetry_image_layout.setVisibility(View.VISIBLE);
            poetry_image.setBackgroundResource(R.drawable.jingyesi);
        } else if (questionText.contains("春夜喜雨")) {
            poetry_text_layout.setVisibility(View.GONE);
            poetry_image_layout.setVisibility(View.VISIBLE);
            poetry_image.setBackgroundResource(R.drawable.chunyexiyu);
        } else if (questionText.contains("咏鹅")) {
            poetry_text_layout.setVisibility(View.GONE);
            poetry_image_layout.setVisibility(View.VISIBLE);
            poetry_image.setBackgroundResource(R.drawable.yonge);
        } else if (questionText.contains("游子吟")) {
            poetry_text_layout.setVisibility(View.GONE);
            poetry_image_layout.setVisibility(View.VISIBLE);
            poetry_image.setBackgroundResource(R.drawable.youziyin);
        } else {
            poetry_image_layout.setVisibility(View.GONE);
            poetry_text_layout.setVisibility(View.VISIBLE);
            poetry_text_layout.setBackgroundResource(R.drawable.back_porety);
            poetry_question.setText(questionText);
            poetry_answer.setText(answerText);
            poetry_answer.setMovementMethod(ScrollingMovementMethod.getInstance());
        }
    }

    private void poetrySpeak(String text) {
        postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_ANSWERTEXT_DATA, text));
        CsjSpeechSynthesizer2.getSynthesizer().startSpeaking(text, new SynthesizerListener() {
            @Override
            public void onSpeakBegin() {
                SpeechStatus.getIstance().setSpeakFinished(false);
                postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_SPEAK));
            }

            @Override
            public void onBufferProgress(int i, int i1, int i2, String s) {

            }

            @Override
            public void onSpeakPaused() {

            }

            @Override
            public void onSpeakResumed() {

            }

            @Override
            public void onSpeakProgress(int percent, int beginPos, int endPos) {
                //自动滚屏
                autoScroll(beginPos);
            }

            @Override
            public void onCompleted(SpeechError speechError) {
                SpeechStatus.getIstance().setSpeakFinished(true);
                postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_NORMAL));
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        });
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

    // 自动滚屏相关代码
    Layout mLayout;
    int lastLine = 0;

    private void autoScroll(int beginPos) {
        int line = getLine(beginPos);
        //如果行数发生变化
        if (line != lastLine) {
            //保持7行的高度
            if (line >= 7) {
                scroll_news.smoothScrollTo(0, poetry_answer.getTop() + mLayout.getLineTop(line - 6));
            }
            lastLine = line;
        }
    }

    private int getLine(int staPos) {
        int lineNumber = 0;
        if (mLayout != null) {
            int line = mLayout.getLineCount();
            for (int i = 0; i < line - 1; i++) {
                if (staPos <= mLayout.getLineStart(i)) {
                    lineNumber = i;
                    break;
                }
            }
        }
        return lineNumber;
    }
}
