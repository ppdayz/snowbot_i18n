package com.csjbot.snowbot.activity.aiui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.aiui.base.AIUIActivity;
import com.csjbot.snowbot.bean.aiui.WeatherBean;
import com.csjbot.snowbot.bean.aiui.entity.WeatherDate;
import com.csjbot.snowbot.services.CsjSpeechSynthesizer2;
import com.csjbot.snowbot_rogue.Events.AIUIEvent;
import com.csjbot.snowbot_rogue.Events.EventsConstants;
import com.csjbot.snowbot_rogue.Events.ExpressionEvent;
import com.csjbot.snowbot_rogue.utils.Constant;

import java.util.List;

public class WeatherAcitvity extends AIUIActivity {
    private ImageView weather_image;
    private TextView weather_state;
    private TextView temperature_text;
    private TextView wind_text;
    private TextView humidity_text;
    private TextView airQuality_text;
    private TextView expName_text;
    private TextView city_text;
    private TextView date_text;

    private String weatherState;
    private String temperature;
    private String wind;
    private String humidity;
    private String airQuality;
    private String expName;
    private String city;
    private String date;
    private String level;
    private String data;
    private List<WeatherBean.ResultBean> weatherList = null;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };

    Handler mHandler = new Handler();

    @Override
    public boolean onAIUIEvent(AIUIEvent event) {
        if (super.onAIUIEvent(event)) {
            return true;
        }
        switch (event.getTag()) {
            case EventsConstants.AIUIEvents.AIUI_EVENT_VOICE_START:
                mHandler.removeCallbacks(runnable);
                break;
            case EventsConstants.AIUIEvents.AIUI_EVENT_VOICE_END:
                mHandler.postDelayed(runnable, 3000);
                break;
            default:
                Csjlogger.debug("event unCaptured   -》" + event.getTag());
                break;
        }

        return false;
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        data = intent.getStringExtra("data");
        try {
            weatherDataSet(data, intent.getStringExtra("semantic"));
        } catch (Exception e) {
            CsjSpeechSynthesizer2.getSynthesizer().startSpeaking("没有查找到天气，请换点别的吧", null);
            Csjlogger.error(e);
            this.finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        LinearLayout v = (LinearLayout) findViewById(R.id.line1);//找到你要设透明背景的layout 的id
        LinearLayout v2 = (LinearLayout) findViewById(R.id.line3);
        v.getBackground().setAlpha(100);//0~255透明度值
        v2.getBackground().setAlpha(100);

        data = getIntent().getStringExtra("data");
        try {
            weatherDataSet(data, getIntent().getStringExtra("semantic"));
        } catch (Exception e) {
            CsjSpeechSynthesizer2.getSynthesizer().startSpeaking("没有查找到天气，请换点别的吧", null);
            Csjlogger.error(e);
            this.finish();
        }
    }

    private void weatherDataSet(String weatherData, String weadate) {
        WeatherBean weatherResult = JSON.parseObject(weatherData, WeatherBean.class);
        weatherList = weatherResult.getResult();
        WeatherDate weatherDateInfo = JSON.parseObject(weadate, WeatherDate.class);
        for (int i = 0; i < weatherList.size(); i++) {
            String weatherDate = weatherDateInfo.getSlots().getDatetime().getDate();
            if (weatherDate.equalsIgnoreCase("CURRENT_DAY")) {
                weatherState = weatherList.get(0).getWeather();
                temperature = weatherList.get(0).getTempRange();
                wind = weatherList.get(0).getWind();
                city = weatherList.get(0).getCity();
                date = weatherList.get(0).getDate();
                humidity = weatherList.get(0).getHumidity();
                airQuality = weatherList.get(0).getAirQuality();
                if (weatherList.get(0).getExp().getCt().getPrompt() != null) {
                    expName = weatherList.get(0).getExp().getCt().getPrompt();
                } else {
                    expName = "未知";
                }
                if (weatherList.get(0).getExp().getCt().getLevel() != null) {
                    level = weatherList.get(0).getExp().getCt().getLevel();
                } else {
                    level = "未知";
                }
                init();
            } else if (weatherDate.equalsIgnoreCase(weatherList.get(i).getDate())) {
                weatherState = weatherList.get(i).getWeather();
                temperature = weatherList.get(i).getTempRange();
                wind = weatherList.get(i).getWind();
                city = weatherList.get(i).getCity();
                date = weatherList.get(i).getDate();
                humidity = weatherList.get(0).getHumidity();
                airQuality = weatherList.get(0).getAirQuality();
                if (weatherList.get(0).getExp().getCt().getPrompt() != null) {
                    expName = weatherList.get(0).getExp().getCt().getPrompt();
                } else {
                    expName = "未知";
                }
                if (weatherList.get(0).getExp().getCt().getLevel() != null) {
                    level = weatherList.get(0).getExp().getCt().getLevel();
                } else {
                    level = "未知";
                }
                init();
            }
        }
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_weather;
    }

    private void init() {
        weather_image = (ImageView) findViewById(R.id.weather_image);
        weatherImage();
        weather_state = (TextView) findViewById(R.id.weather_state);
        weather_state.setText(weatherState);
        temperature_text = (TextView) findViewById(R.id.temperature_text);
        temperature_text.setText(temperature + " ( " + level + " ) ");
        wind_text = (TextView) findViewById(R.id.wind_text);
        wind_text.setText(wind);
        humidity_text = (TextView) findViewById(R.id.humidity_text);
        humidity_text.setText(humidity);
        airQuality_text = (TextView) findViewById(R.id.airQuality_text);
        airQuality_text.setText(airQuality);
        expName_text = (TextView) findViewById(R.id.expName_text);
        expName_text.setText(expName);
        city_text = (TextView) findViewById(R.id.city_text);
        city_text.setText(city);
        date_text = (TextView) findViewById(R.id.date_text);
        date_text.setText(date);
    }

    private void weatherImage() {
        if (weatherState.contains("雨") && weatherState.contains("雪")) {
            weather_image.setImageResource(R.drawable.sleet);
        } else if (weatherState.contains("雪")) {
            weather_image.setImageResource(R.drawable.snow);
        } else if (weatherState.contains("雨") && weatherState.contains("雷")) {
            weather_image.setImageResource(R.drawable.thunderstorm);
        } else if (weatherState.contains("雨")) {
            weather_image.setImageResource(R.drawable.heavyrain);
        } else if (weatherState.contains("阴")) {
            weather_image.setImageResource(R.drawable.overcast);
        } else if (weatherState.contains("晴")) {
            weather_image.setImageResource(R.drawable.sunny);
        } else {
            weather_image.setImageResource(R.drawable.cloudy);
        }
    }

    public void closeSpeech(View view) {
        CsjSpeechSynthesizer2.getSynthesizer().stopSpeaking();
        postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_NORMAL));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

