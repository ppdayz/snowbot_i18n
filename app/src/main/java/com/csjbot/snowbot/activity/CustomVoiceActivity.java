package com.csjbot.snowbot.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.core.entry.Static;
import com.android.core.util.SharedUtil;
import com.android.core.util.StrUtil;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot.views.BaseRecyViewAdpter;
import com.csjbot.snowbot.views.CusViewHodler;
import com.csjbot.snowbot.views.MyDecoration;
import com.csjbot.snowbot_rogue.utils.CSJToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author: jl
 * @Time: 2017/1/5
 * @Desc:
 */
public class CustomVoiceActivity extends CsjUIActivity {
    @BindView(R.id.question_et)
    EditText questionEt;
    @BindView(R.id.answer_et)
    EditText answerEt;
    @BindView(R.id.submit_btn)
    Button submitBtn;
    @BindView(R.id.no_content)
    TextView noContent;
    @BindView(R.id.answer_recylervew)
    RecyclerView answerRecylervew;
    @BindView(R.id.cancle_btn)
    Button cancleBtn;
    @BindView(R.id.del_btn)
    Button delBtn;


    private Map<String, String> customData = new HashMap<>();
    private List<String> questionData = new ArrayList<>();
    private List<String> answerData = new ArrayList<>();
    private BaseRecyViewAdpter<String> adapter;
    private boolean isModifyCustomVoice = false;
    private int currentPostion;


    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        initAdapter();
        initData();
        initRecylerView();
    }

    private void initRecylerView() {
        answerRecylervew.setLayoutManager(new LinearLayoutManager(this));
        answerRecylervew.setHasFixedSize(true);
        answerRecylervew.addItemDecoration(new MyDecoration(this, MyDecoration.VERTICAL_LIST));
        answerRecylervew.setAdapter(adapter);

    }

    private void initAdapter() {
        adapter = new BaseRecyViewAdpter<String>(questionData, this) {
            TextView questionName;

            @Override
            public int getLayoutId() {
                return R.layout.layout_wifi;
            }

            @Override
            public void setViewModel(CusViewHodler viewHodler, String data, int postion) {
                questionName = (TextView) viewHodler.getView().findViewById(R.id.wif_name_tv);
                questionName.setText(data);
            }
        };
        adapter.setItemClickListener(new BaseRecyViewAdpter.ItemClickListener() {
            @Override
            public void itemClick(int postion, View view) {
                isModifyCustomVoice = true;
                currentPostion = postion;
                delBtn.setVisibility(View.VISIBLE);
                cancleBtn.setVisibility(View.VISIBLE);
                questionEt.setText(questionData.get(postion));
                answerEt.setText(answerData.get(postion));
            }
        });
    }


    private void initData() {
        setupBack();
        if (null != SharedUtil.getMap(SharedKey.CUSTOMVOICE)) {
            customData = SharedUtil.getMap(SharedKey.CUSTOMVOICE);
            updateData();
        }
        addTextWatcher(questionEt);
        addTextWatcher(answerEt);
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.act_custom_voice;
    }


    @OnClick(R.id.submit_btn)
    public void onClick() {
        getCustomVoice();
    }


    private void getCustomVoice() {
        String question = questionEt.getText().toString();
        String answer = answerEt.getText().toString();
        if (checkData(question, answer)) {
            customData.put(question, answer);
            if (!isModifyCustomVoice) {
                CSJToast.showToast(this, "增加成功");
            } else {
                String questionTempData = questionEt.getText().toString().trim();
                if (!questionTempData.equals(questionData.get(currentPostion))) {
                    customData.remove(questionData.get(currentPostion));
                }
                CSJToast.showToast(this, "修改成功");
            }
            updateData();
            clearText();
        }
    }

    private boolean checkData(String question, String answer) {
        question = questionEt.getText().toString();
        answer = answerEt.getText().toString();
        if (StrUtil.isBlank(question)) {
            CSJToast.showToast(this, Static.CONTEXT.getString(R.string.question_empty));
            return false;
        }
        if (StrUtil.isBlank(answer)) {
            CSJToast.showToast(this, Static.CONTEXT.getString(R.string.answer_empty));
            return false;
        }
        return true;
    }


    private String StringFilter(String str) throws PatternSyntaxException {
        String regEx = "[/\\:*?<>|\"\n\t]"; //要过滤掉的字符
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    private void addTextWatcher(EditText editText) {

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                String editable = editText.getText().toString();
                String str = StringFilter(editable.toString());
                if (!editable.equals(str)) {
                    editText.setText(str);
                    editText.setSelection(str.length()); //光标置后
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }


    @OnClick({R.id.cancle_btn, R.id.del_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancle_btn:
                clearText();
                break;
            case R.id.del_btn:
                clearText();
                delDate(currentPostion);
                break;
        }
    }

    private void clearText() {
        isModifyCustomVoice = false;
        questionEt.setText("");
        answerEt.setText("");
        delBtn.setVisibility(View.GONE);
        cancleBtn.setVisibility(View.GONE);
    }

    private void delDate(int postion) {
        if (customData.containsKey(questionData.get(postion))) {
            customData.remove(questionData.get(postion));
            CSJToast.showToast(this, "删除成功");
            updateData();
        }
    }


    private void updateData() {
        SharedUtil.setMap(SharedKey.CUSTOMVOICE, customData);
        questionEt.setText("");
        answerEt.setText("");
        questionData.clear();
        answerData.clear();
        List<String> questDataTemp = new ArrayList<>(customData.keySet());
        List<String> answerDataTemp = new ArrayList<>(customData.values());
        questionData.addAll(questDataTemp);
        answerData.addAll(answerDataTemp);
        if (null != questionData && questionData.size() > 0) {
            answerRecylervew.setVisibility(View.VISIBLE);
            noContent.setVisibility(View.GONE);
            adapter.setData(questionData);
        } else {
            answerRecylervew.setVisibility(View.GONE);
            noContent.setVisibility(View.VISIBLE);
        }
    }

}
