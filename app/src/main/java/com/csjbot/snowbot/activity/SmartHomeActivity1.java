package com.csjbot.snowbot.activity;

import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.views.ColorArcProgressBar;
import com.csjbot.snowbot_rogue.servers.serials.SnowBotSerialServer;
import com.csjbot.snowbot_rogue.utils.WeakReferenceHandler;

import java.util.Calendar;

public class SmartHomeActivity1 extends CsjUIActivity implements View.OnClickListener {
    private ImageButton ib_head_return;
    private ColorArcProgressBar bar;
    private ImageView ib_fengsutiaojie;
    private ImageView ib_dingshiguanji;
    private ImageView ib_guanji;
    private RelativeLayout rl_define;
    private TextView tv_add_define;
    private int airIndex = 160;
    private TextView tempearture;
    private TextView humidity;
    private SnowBotSerialServer snowBotAction = SnowBotSerialServer.getOurInstance();
    private short shortNew;
    int speed = 0;
    int secs = 0;
    int animDuration = 600;
    private RotateAnimation rotateAnimation;

    private SmartHomeActivity1Handler mHandler = new SmartHomeActivity1Handler(this);

    private BroadcastReceiver airAcionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int air = intent.getIntExtra("air", 0);

            if (air == 0) {
                ib_fengsutiaojie.clearAnimation();
//                snowBotAction.turnOffAirCleaner();
            } else {
                speed = air;
//                snowBotAction.turnOnAirCleaner((byte) speed);
                animDuration = 600 - speed * 50;
                rotateAnimation.setDuration(animDuration);

                ib_fengsutiaojie.startAnimation(rotateAnimation);
//                snowBotAction.turnOnAirCleaner((byte) speed);

            }

        }
    };

    private BroadcastReceiver humidityAndtemperatureReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int humidity1 = intent.getIntExtra("humidity", 0);
            int temperature1 = intent.getIntExtra("temperature", 1);

            shortNew = intent.getShortExtra("pm25", (short) 0);
            tempearture.setText(String.valueOf(humidity1));
            humidity.setText(String.valueOf(temperature1));
            bar.setCurrentValues(shortNew);
        }
    };

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        setupBack(Color.WHITE);

//        mLocalBroadcastManager.registerReceiver(humidityAndtemperatureReceiver, new IntentFilter(Constant.ACTION_TEMPERATURE_HUMIDITYGET));
//        mLocalBroadcastManager.registerReceiver(airAcionReceiver, new IntentFilter(Constants.ClientActions.ACTION_AIR_CTRL));

        initView();
        initAnim();
        snowBotAction.requireAllSensor();
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        return R.layout.activity_smart_home1;
    }

    private class SmartHomeActivity1Handler extends WeakReferenceHandler<SmartHomeActivity1> {

        public SmartHomeActivity1Handler(SmartHomeActivity1 reference) {
            super(reference);
        }

        @Override
        protected void handleMessage(SmartHomeActivity1 reference, Message msg) {

        }
    }


//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        setContentView(R.layout.activity_smart_home1);
//
//        setupBack(Color.WHITE);
//
//        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
//        mLocalBroadcastManager.registerReceiver(humidityAndtemperatureReceiver, new IntentFilter(Constant.ACTION_TEMPERATURE_HUMIDITYGET));
//        mLocalBroadcastManager.registerReceiver(airAcionReceiver, new IntentFilter(Constants.ClientActions.ACTION_AIR_CTRL));
//
//        initView();
//        initAnim();
//        snowBotAction.requireAllSensor();
//    }

    private void initAnim() {
        rotateAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setRepeatCount(-1);
        rotateAnimation.setDuration(animDuration);
        rotateAnimation.setInterpolator(new LinearInterpolator());
    }

    private void initView() {
//        ib_head_return = ((ImageButton) findViewById(R.id.ib_head_return));
//        ib_head_return.setOnClickListener(this);
        bar = ((ColorArcProgressBar) findViewById(R.id.bar));
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bar.setCurrentValues(airIndex);
            }
        }, 500);

        ib_fengsutiaojie = ((ImageView) findViewById(R.id.ib_fengsutiaojie));
        ib_fengsutiaojie.setOnClickListener(this);
        ib_dingshiguanji = ((ImageView) findViewById(R.id.ib_dingshiguanji));
        ib_dingshiguanji.setOnClickListener(this);
        ib_guanji = ((ImageView) findViewById(R.id.ib_guanji));
        ib_guanji.setOnClickListener(this);
        rl_define = ((RelativeLayout) findViewById(R.id.rl_define));
        rl_define.setOnClickListener(this);
        tv_add_define = ((TextView) findViewById(R.id.tv_add_define));
        tempearture = ((TextView) findViewById(R.id.temperature));
        humidity = ((TextView) findViewById(R.id.humidity));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
//            case R.id.ib_head_return:
//                this.finish();
//                break;
            case R.id.ib_fengsutiaojie:
                speed += 2;
//                snowBotAction.turnOnAirCleaner((byte) speed);
                animDuration = 600 - speed * 50;
                rotateAnimation.setDuration(animDuration);

                if (speed == 8) {
                    speed = 0;
                }

                ib_fengsutiaojie.startAnimation(rotateAnimation);
                break;
            case R.id.ib_dingshiguanji:
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(System.currentTimeMillis());
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String s = "空气净化器将在" + hourOfDay + "个小时" + minute + "分钟之后自动关闭";
                        Toast.makeText(SmartHomeActivity1.this, s, Toast.LENGTH_LONG).show();
                        secs = hourOfDay * 3600 + minute * 60;
                    }
                }, hour, minute, true).show();
                break;

            case R.id.ib_guanji:
                speed = 0;
                ib_fengsutiaojie.clearAnimation();
//                snowBotAction.turnOffAirCleaner();
                break;
            case R.id.rl_define:
                Intent intent = new Intent(this, SmartHomeActivity2.class);
                startActivityForResult(intent, 2046);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 2207 && requestCode == 2046) {
            String s = data.getStringExtra("newStr");
            int setData = Integer.parseInt(data.getStringExtra("data"));
            tv_add_define.setText(s);
            if (shortNew > setData) {
//                snowBotAction.turnOnAirCleaner((byte) 2);
            }
        }
    }


    @Override
    protected void onDestroy() {
//        mLocalBroadcastManager.unregisterReceiver(humidityAndtemperatureReceiver);
//        mLocalBroadcastManager.unregisterReceiver(airAcionReceiver);
        super.onDestroy();
    }
}
