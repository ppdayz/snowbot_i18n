package com.csjbot.snowbot.activity.aiui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.aiui.base.AIUIActivity;
import com.csjbot.snowbot.bean.aiui.Compute;
import com.csjbot.snowbot_rogue.app.CsjSpeechSynthesizer;
import com.csjbot.snowbot.bean.aiui.entity.CsjSynthesizerListener;
import com.csjbot.snowbot.utils.SpeechStatus;
import com.csjbot.snowbot_rogue.Events.AIUIEvent;
import com.csjbot.snowbot_rogue.Events.EventsConstants;
import com.csjbot.snowbot_rogue.Events.ExpressionEvent;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.iflytek.cloud.SpeechError;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CountActivity extends AIUIActivity {

    /*全局表达式*/
    static String exp = "";

    /*设置可以连接加减运算字符的状态为true*/
    static boolean canAddPNOperateChar = true;

    /*设置可以连接乘除运算字符的状态为false*/
    static boolean canAddMDOperateChar = false;

    /*设置可以连接数字字符的状态为true*/
    static boolean canAddDigitChar = true;

    /*设置可以连接点字符的状态为false*/
    static boolean canAddDotChar = false;

    /*设置可以连接左括号字符的状态为true*/
    static boolean canAddLeftChar = true;

    /*设置可以连接右括号字符的状态为false*/
    static boolean canAddRightChar = false;

    /*设置可以计算表达式为的状态为false*/
    static boolean canCalculate = false;

    /*是否允许添加点号*/
    static boolean allowAddDotChar = true;

    static int countLeftChar = 0;

    /*显示表达式*/
    EditText express_input;

    /*显示结果 */
    EditText express_output;

    /*清空按钮*/
    Button btn_clear;

    /*返回按钮*/
    Button btn_back;

    /*+符号按钮*/
    Button btn_add;

    /*-符号按钮*/
    Button btn_minus;

    /*x符号按钮*/
    Button btn_times;

    /*div符号按钮*/
    Button btn_div;

    /*计算结果按钮*/
    Button btn_equal;

    /*点符号按钮 */
    Button btn_dot;

    /*左括号*/
    Button btn_leftp;

    /*右括号*/
    Button btn_rightp;

    int btn_num_id[] = {R.id.btn_num_0, R.id.btn_num_1, R.id.btn_num_2,
            R.id.btn_num_3, R.id.btn_num_4, R.id.btn_num_5, R.id.btn_num_6,
            R.id.btn_num_7, R.id.btn_num_8, R.id.btn_num_9};
    /*0-9数字按钮*/
    Button btn_num[] = new Button[10];

    private String questionText, answerText;

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {

        /**初始化计算器*/
        runCalculator();
        questionText = getIntent().getStringExtra("speakText");
        answerText = getIntent().getStringExtra("answerText");
        hanGoDig();
        express_input.setText(questionText);
        express_output.setText(answerText);
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);//must store the new intent unless getIntent() will return the old one

        runCalculator();
        questionText = intent.getStringExtra("speakText");
        answerText = intent.getStringExtra("answerText");
        hanGoDig();
        express_input.setText(questionText);
        express_output.setText(answerText);
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_count;
    }


    public void runCalculator() {
        express_input = (EditText) findViewById(R.id.express_input);
        express_output = (EditText) findViewById(R.id.express_output);

        btn_clear = (Button) findViewById(R.id.btn_clear);
        btn_clear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                resetBoolean();
            }

        });
        /**重置显示结果*/
        resetBoolean();

        btn_back = (Button) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                backExpression();
            }
        });

        for (int i = 0; i < btn_num_id.length; i++) {
            btn_num[i] = (Button) findViewById(btn_num_id[i]);
            btn_num[i].setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    numBtnClickEvent(arg0.getId());
                }
            });
        }

        btn_add = (Button) findViewById(R.id.btn_sign_add);
        btn_add.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (canAddPNOperateChar) {
                    exp = exp + '+';
                    express_input.setText(exp);
                    canAddDigitChar = true;
                    canAddLeftChar = true;
                    allowAddDotChar = true;
                    canAddDotChar = false;
                    canAddPNOperateChar = false;
                    canAddMDOperateChar = false;
                    canAddRightChar = false;
                    canCalculate = false;
                }
            }
        });

        btn_minus = (Button) findViewById(R.id.btn_sign_minus);
        btn_minus.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (canAddPNOperateChar) {
                    exp = exp + '-';
                    express_input.setText(exp);
                    canAddDigitChar = true;
                    canAddLeftChar = true;
                    allowAddDotChar = true;
                    canAddDotChar = false;
                    canAddPNOperateChar = false;
                    canAddMDOperateChar = false;
                    canAddRightChar = false;
                    canCalculate = false;
                }
            }
        });

        btn_times = (Button) findViewById(R.id.btn_sign_times);
        btn_times.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (canAddMDOperateChar) {
                    exp = exp + '*';
                    express_input.setText(exp);
                    canAddDigitChar = true;
                    canAddLeftChar = true;
                    allowAddDotChar = true;
                    canAddDotChar = false;
                    canAddPNOperateChar = false;
                    canAddMDOperateChar = false;
                    canAddRightChar = false;
                    canCalculate = false;
                }
            }
        });

        btn_div = (Button) findViewById(R.id.btn_sign_div);
        btn_div.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (canAddMDOperateChar) {
                    exp = exp + '/';
                    express_input.setText(exp);
                    canAddDigitChar = true;
                    canAddLeftChar = true;
                    allowAddDotChar = true;
                    canAddDotChar = false;
                    canAddPNOperateChar = false;
                    canAddMDOperateChar = false;
                    canAddRightChar = false;
                    canCalculate = false;
                }
            }
        });

        btn_leftp = (Button) findViewById(R.id.btn_leftp);
        btn_leftp.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (canAddLeftChar) {
                    exp = exp + '(';
                    express_input.setText(exp);
                    canAddPNOperateChar = true;
                    allowAddDotChar = true;
                    canAddMDOperateChar = false;
                    canAddDotChar = false;
                    canCalculate = false;
                    countLeftChar++;
                }
            }
        });

        btn_rightp = (Button) findViewById(R.id.btn_rightp);
        btn_rightp.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (canAddRightChar) {
                    exp = exp + ')';
                    express_input.setText(exp);
                    canAddPNOperateChar = true;
                    canAddMDOperateChar = true;
                    canAddDigitChar = false;
                    canAddDotChar = false;
                    allowAddDotChar = false;
                    countLeftChar--;
                    if (countLeftChar >= 1) {
                        canAddRightChar = true;
                        canCalculate = false;
                    } else {
                        canAddRightChar = false;
                        canCalculate = true;
                    }
                }
            }
        });

        btn_dot = (Button) findViewById(R.id.btn_num_dot);
        btn_dot.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (canAddDotChar && allowAddDotChar) {
                    exp = exp + '.';
                    express_input.setText(exp);
                    canAddDigitChar = true;
                    canAddDotChar = false;
                    allowAddDotChar = false;
                    canAddPNOperateChar = false;
                    canAddMDOperateChar = false;
                    canAddLeftChar = false;
                    canAddRightChar = false;
                    canCalculate = false;
                }
            }
        });

        btn_equal = (Button) findViewById(R.id.btn_num_equal);
        btn_equal.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                calculateExpress();
            }

        });
    }

    public boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    public void resetBoolean() {
        exp = "";
        canAddPNOperateChar = true;
        canAddMDOperateChar = false;
        canAddDigitChar = true;
        canAddDotChar = false;
        canAddLeftChar = true;
        canAddRightChar = false;
        canCalculate = false;
        allowAddDotChar = true;
        countLeftChar = 0;
        express_input.setText("0.");
        express_output.setText("");
    }

    public void backExpression() {
        if (exp.length() != 0) {
            char temp = exp.charAt(exp.length() - 1);
            exp = exp.substring(0, exp.length() - 1);
            express_input.setText(exp);

            if (exp.length() == 0) {
                resetBoolean();
            } else if (temp >= '0' && temp <= '9') {
                int sign = 0;
                int len = exp.length() - 1;
                while (len >= 0 && (isDigit(exp.charAt(len)) || exp.charAt(len) == '.')) {
                    if (exp.charAt(len) == '.') {
                        sign = 1;
                    }
                    len--;
                }
                if (sign == 1) { // 如果删除表达式的最后一个字符后的表达式的最后一个字符是数字
                    if (isDigit(exp.charAt(exp.length() - 1))) {
                        canAddDigitChar = true;
                        canAddPNOperateChar = true;
                        canAddMDOperateChar = true;
                        canAddDotChar = false;
                        allowAddDotChar = false;
                        canAddLeftChar = false;
                        if (countLeftChar != 0) {
                            canAddRightChar = true;
                            canCalculate = false;
                        } else {
                            canAddRightChar = false;
                            canCalculate = true;
                        }
                    } else// 如果表达式的最后个字符是点
                    {
                        canAddDigitChar = true;
                        canAddPNOperateChar = false;
                        canAddMDOperateChar = false;
                        canAddDotChar = false;
                        allowAddDotChar = false;
                        canAddLeftChar = false;
                        canAddRightChar = false;
                        canCalculate = false;
                    }
                } else// 表达式的最后数字字符串内没有点字符
                {
                    if (isDigit(exp.charAt(exp.length() - 1))) {
                        canAddDigitChar = true;
                        canAddPNOperateChar = true;
                        canAddMDOperateChar = true;
                        canAddDotChar = true;
                        allowAddDotChar = true;
                        canAddLeftChar = false;
                        if (countLeftChar != 0) {
                            canAddRightChar = true;
                            canCalculate = false;
                        } else {
                            canAddRightChar = false;
                            canCalculate = true;
                        }
                    } else { // 表达式的最后一个字符是运算符(+,-,*,/)
                        if (exp.charAt(exp.length() - 1) != '(') {
                            canAddDigitChar = true;
                            canAddLeftChar = true;
                            allowAddDotChar = true;
                            canAddPNOperateChar = false;
                            canAddMDOperateChar = false;
                            canAddDotChar = false;
                            canAddRightChar = false;
                            canCalculate = false;
                        }
                        // 表达式的最后一个字符是'('
                        else {
                            canAddDigitChar = true;
                            canAddLeftChar = true;
                            canAddPNOperateChar = true;
                            allowAddDotChar = true;
                            canAddMDOperateChar = false;
                            canAddDotChar = false;
                            canAddRightChar = false;
                            canCalculate = false;
                        }
                    }
                }
            } else if (temp == '.') {
                canAddDigitChar = true;
                canAddPNOperateChar = true;
                canAddMDOperateChar = true;
                canAddDotChar = true;
                allowAddDotChar = true;
                canAddLeftChar = false;
                if (countLeftChar != 0) {
                    canAddRightChar = true;
                    canCalculate = false;
                } else {
                    canAddRightChar = false;
                    canCalculate = true;
                }

                if (exp.length() >= 2 && exp.charAt(exp.length() - 1) == '0') {
                    char tempChar = exp.charAt(exp.length() - 2);
                    if (!isDigit(tempChar)) {
                        canAddDotChar = true;
                        allowAddDotChar = true;
                        canAddPNOperateChar = true;
                        canAddMDOperateChar = true;
                        canAddDigitChar = false;
                        if (countLeftChar >= 1) {
                            canCalculate = false;
                        } else {
                            canCalculate = true;
                        }
                    }
                } else if (exp.length() == 1 && exp.charAt(0) == '0') {
                    canAddDotChar = true;
                    allowAddDotChar = true;
                    canAddPNOperateChar = true;
                    canAddMDOperateChar = true;
                    canAddDigitChar = false;
                    if (countLeftChar >= 1) {
                        canCalculate = false;
                    } else {
                        canCalculate = true;
                    }
                }
            } else if (temp == '+' || temp == '-' || temp == '*' || temp == '/') { // 表达式的最后一个字符只可能是'(',')'或数字
                if (temp == '+' || temp == '-') {
                    if (exp.charAt(exp.length() - 1) == '(') {
                        canAddPNOperateChar = true;
                        allowAddDotChar = true;
                        canAddMDOperateChar = false;
                        canAddDotChar = false;
                        canCalculate = false;
                    } else if (exp.charAt(exp.length() - 1) == ')') {
                        canAddPNOperateChar = true;
                        canAddMDOperateChar = true;
                        canAddDigitChar = false;
                        canAddDotChar = false;
                        allowAddDotChar = false;
                        if (countLeftChar >= 1) {
                            canCalculate = false;
                        } else {
                            canCalculate = true;
                        }
                    } else {
                        canAddPNOperateChar = true;
                        canAddMDOperateChar = true;
                        canAddDigitChar = true;
                        canAddLeftChar = false;
                        if (countLeftChar != 0) {
                            canAddRightChar = true;
                            canCalculate = false;
                        } else {
                            canAddRightChar = false;
                            canCalculate = true;
                        }
                        int len = exp.length() - 1;
                        int sign = 0;
                        while (len >= 0
                                && (isDigit(exp.charAt(len)) || exp.charAt(len) == '.')) {
                            if (exp.charAt(len) == '.') {
                                sign = 1;
                            }
                            len--;
                        }
                        // 如果表达式的最后数字字符串内有点字符
                        if (sign == 1) {
                            canAddDotChar = false;
                            allowAddDotChar = false;
                        } else {
                            canAddDotChar = true;
                            allowAddDotChar = true;
                        }
                        if ((exp.length() == 1 && exp.charAt(0) == '0')
                                || (exp.length() >= 2
                                && exp.charAt(exp.length() - 1) == '0'
                                && !isDigit(exp
                                .charAt(exp.length() - 2)) && exp
                                .charAt(exp.length() - 2) != '.')) {
                            canAddDotChar = true;
                            allowAddDotChar = true;
                            canAddPNOperateChar = true;
                            canAddMDOperateChar = true;
                            canAddDigitChar = false;
                            if (countLeftChar >= 1) {
                                canCalculate = false;
                            } else {
                                canCalculate = true;
                            }
                        }
                    }
                } else// 表达式的最后一个字符只可能是')'或数字
                {
                    if (exp.charAt(exp.length() - 1) == ')') {
                        canAddPNOperateChar = true;
                        canAddMDOperateChar = true;
                        canAddDigitChar = false;
                        canAddDotChar = false;
                        allowAddDotChar = false;
                        if (countLeftChar >= 1) {
                            canCalculate = false;
                        } else {
                            canCalculate = true;
                        }
                    } else {
                        canAddPNOperateChar = true;
                        canAddMDOperateChar = true;
                        canAddDigitChar = true;
                        canAddLeftChar = false;
                        if (countLeftChar != 0) {
                            canAddRightChar = true;
                            canCalculate = false;
                        } else {
                            canAddRightChar = false;
                            canCalculate = true;
                        }
                        int len = exp.length() - 1;
                        int sign = 0;
                        while (len >= 0
                                && (isDigit(exp.charAt(len)) || exp.charAt(len) == '.')) {
                            if (exp.charAt(len) == '.') {
                                sign = 1;
                            }
                            len--;
                        }
                        // 如果表达式的最后数字字符串内有点字符
                        if (sign == 1) {
                            canAddDotChar = false;
                            allowAddDotChar = false;
                        } else {
                            canAddDotChar = true;
                            allowAddDotChar = true;
                        }
                        if ((exp.length() == 1 && exp.charAt(0) == '0')
                                || (exp.length() >= 2
                                && exp.charAt(exp.length() - 1) == '0'
                                && !isDigit(exp
                                .charAt(exp.length() - 2)) && exp
                                .charAt(exp.length() - 2) != '.')) {
                            canAddDotChar = true;
                            allowAddDotChar = true;
                            canAddPNOperateChar = true;
                            canAddMDOperateChar = true;
                            canAddDigitChar = false;
                            if (countLeftChar >= 1) {
                                canCalculate = false;
                            } else {
                                canCalculate = true;
                            }
                        }
                    }
                }
            }
            // 表达式的最后字符只可能是运算符(+,-,*,/);
            else if (temp == '(') {
                countLeftChar--;
                canAddDigitChar = true;
                allowAddDotChar = true;
                canAddPNOperateChar = false;
                canAddMDOperateChar = false;
                canAddDotChar = false;
                canCalculate = false;
            }
            // 表达式的最后字符只可能是数字字符
            else // temp==')'
            { // result.setText("删除了右括号!");
                countLeftChar++;
                canAddDigitChar = true;
                canAddPNOperateChar = true;
                canAddMDOperateChar = true;
                canAddLeftChar = false;
                if (countLeftChar >= 1) {
                    canAddRightChar = true;
                    canCalculate = false;
                } else {
                    canAddRightChar = false;
                    canCalculate = true;
                }

                int len = exp.length() - 1;
                int sign = 0;
                while (len >= 0
                        && (isDigit(exp.charAt(len)) || exp.charAt(len) == '.')) {
                    if (exp.charAt(len) == '.') {
                        sign = 1;
                    }
                    len--;
                }
                // 如果表达式的最后数字字符串内有点字符
                if (sign == 1) {
                    canAddDotChar = false;
                    allowAddDotChar = false;
                } else {
                    canAddDotChar = true;
                    allowAddDotChar = true;
                }
            }
        } else {
            exp = "";
            express_input.setText("0.");
        }
    }

    public void numBtnClickEvent(int viewId) {
        char ch = '\0';
        // 找出是哪个数字按钮的点击事件
        for (int i = 0; i < btn_num_id.length; i++) {
            if (viewId == btn_num_id[i]) {
                ch = (char) ((int) '0' + i);
            }
        }
        if (ch >= '0' && ch <= '9') {
            if (canAddDigitChar) {
                exp = exp + ch;
                express_input.setText(exp);
                canAddDigitChar = true;
                canAddPNOperateChar = true;
                canAddMDOperateChar = true;
                canAddLeftChar = false;
                canAddDotChar = true;
                if (countLeftChar == 0) {
                    canCalculate = true;
                    canAddRightChar = false;
                } else {
                    canCalculate = false;
                    canAddRightChar = true;
                }
            }
            if (ch == '0') {
                char lastChar = exp.charAt(exp.length() - 1);
                if (lastChar == '0') {
                    if (exp.length() == 1) {
                        canAddDotChar = true;
                        allowAddDotChar = true;
                        canAddDigitChar = false;
                        canAddPNOperateChar = true;
                        canAddMDOperateChar = true;
                        canAddLeftChar = false;
                        canAddRightChar = false;
                        canCalculate = true;
                    } else if (exp.length() >= 2) {
                        char temp = exp.charAt(exp.length() - 2);
                        if (temp == '+' || temp == '-' || temp == '*'
                                || temp == '/') {
                            canAddDotChar = true;
                            allowAddDotChar = true;
                            canAddDigitChar = false;
                            canAddPNOperateChar = true;
                            canAddMDOperateChar = true;
                            canAddLeftChar = false;
                            canAddRightChar = false;
                            if (countLeftChar >= 1) {
                                canCalculate = false;
                            } else {
                                canCalculate = true;
                            }
                        }
                    }
                }
            }
        }
    }

    public void calculateExpress() {
        if (canCalculate) {
            Compute cp = new Compute();
            express_input.setText(exp + "=");
            String resultStr = cp.compute(exp + "=");
            if (resultStr.startsWith("Error")) {
                express_output.setText(resultStr);
            } else {
                express_output.setText(getResources().getString(R.string.the_result) + resultStr);
            }
            exp = "";
            canAddPNOperateChar = true;
            allowAddDotChar = true;
            canAddDigitChar = true;
            canAddLeftChar = true;
            canAddMDOperateChar = false;
            canAddDotChar = false;
            canAddRightChar = false;
            canCalculate = false;
            countLeftChar = 0;
        } else {
            if (countLeftChar >= 1) {
                express_output.setText(getResources().getString(R.string.count_error_missing) + countLeftChar + getResources().getString(R.string.count_missing_right_parenthesis));
            } else {
                if (exp.length() != 0) {
                    express_output.setText(getResources().getString(R.string.count_error));
                }
            }
        }
    }

    private void voice(String text) {
        postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_ANSWERTEXT_DATA, text));
        CsjSpeechSynthesizer.getSynthesizer().startSpeaking(text, new CsjSynthesizerListener() {
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

    /**
     * 汉字转数字运算符
     */
    private void hanGoDig() {
        if (questionText.contains("乘以")) {
            questionText = questionText.replace("乘以", "×");
        }
        if (questionText.contains("乘")) {
            questionText = questionText.replace("乘", "×");
        }
        if (questionText.contains("除以")) {
            questionText = questionText.replace("除以", "÷");
        }
        if (questionText.contains("除")) {
            questionText = questionText.replace("除", "÷");
        }
        if (questionText.contains("加")) {
            questionText = questionText.replace("加", "+");
        }
        if (questionText.contains("加上")) {
            questionText = questionText.replace("加上", "+");
        }
        if (questionText.contains("减")) {
            questionText = questionText.replace("减", "-");
        }
        if (questionText.contains("减去")) {
            questionText = questionText.replace("减去", "-");
        }
        if (questionText.contains("点")) {
            questionText = questionText.replace("点", ".");
        }
        if (questionText.contains("等于")) {
            questionText = questionText.replace("等于", "=");
            int loc = questionText.indexOf("=");//首先获取字符的位置
            questionText = questionText.substring(0, loc + 1);
        }

        isNumeric(questionText);
    }

    /**
     * 判断是否为正确的计算公式
     *
     * @param str
     */
    public void isNumeric(String str) {
        if (str.contains("×")) {
            str = str.replace("×", "");
        }
        if (str.contains("÷")) {
            str = str.replace("÷", "");
        }
        if (str.contains("+")) {
            str = str.replace("+", "");
        }
        if (str.contains("-")) {
            str = str.replace("-", "");
        }
        if (str.contains(".")) {
            str = str.replace(".", "");
        }
        if (str.contains("=")) {
            str = str.replace("=", "");
        }

        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            answerText = getResources().getString(R.string.count_answer);
        }
        voice(answerText);
    }
}
