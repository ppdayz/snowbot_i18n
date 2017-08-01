package com.csjbot.snowbot.bean.aiui;


import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.utils.LocationUtil.LocationUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Administrator on 2016/11/11 0011.
 */

public class TouchWeather {
    private String fengxiang;
    private String fengli;
    private String highTemperature;
    private String weatherType;
    private String lowTemperature;
    private String date;
    private String ganmao;

    public interface WeatherGetListener {
        void weatherGet(String weather);

        void onError();
    }

    public String getAnwserText() {
        return anwserText;
    }


    private String anwserText;

    public void getWeatherData(WeatherGetListener listener) {
        OkHttpClient httpClient = new OkHttpClient();

//        Request.Builder requestBuilder = new Request.Builder().url("http://192.168.11.1/admin/wifi_select.html");
        Request.Builder requestBuilder = new Request.Builder().url("http://wthrcdn.etouch.cn/weather_mini?city=" + LocationUtil.getCustomLocationCity());
        requestBuilder.method("GET", null);
        Request request = requestBuilder.build();
        Call mcall = httpClient.newCall(request);
        mcall.enqueue(new okhttp3.Callback() {
            /**
             * Called when the request could not be executed due to cancellation, a connectivity problem or
             * timeout. Because networks can fail during an exchange, it is possible that the remote server
             * accepted the request before the failure.
             *
             * @param call
             * @param e
             */
            @Override
            public void onFailure(Call call, IOException e) {
                if (listener != null) {
                    listener.onError();
                }
                Csjlogger.debug(e.toString());
            }

            /**
             * Called when the HTTP response was successfully returned by the remote server. The callback may
             * proceed to read the response body with {@link Response#body}. The response is still live until
             * its response body is {@linkplain ResponseBody closed}. The recipient of the callback may
             * consume the response body on another thread.
             * <p>
             * <p>Note that transport-layer success (receiving a HTTP response code, headers and body) does
             * not necessarily indicate application-layer success: {@code response} may still indicate an
             * unhappy HTTP response code like 404 or 500.
             *
             * @param call
             * @param response
             */
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (null != response.cacheResponse()) {
                    String str = response.cacheResponse().toString();
//                    Csjlogger.debug("cache---" + str);
                } else {
                    String body = response.body().string();
                    Csjlogger.info("onResponse is {} ", body);
                    try {
                        JSONObject jsonObject = new JSONObject(body);
                        JSONObject data = jsonObject.getJSONObject("data");
                        ganmao = data.getString("ganmao");
                        JSONArray forecast = data.getJSONArray("forecast");
                        JSONObject weatherData = (JSONObject) forecast.get(0);
                        fengxiang = weatherData.getString("fengxiang");
                        fengli = weatherData.getString("fengli");
                        highTemperature = weatherData.getString("high");
                        weatherType = weatherData.getString("type");
                        lowTemperature = weatherData.getString("low");
                        date = weatherData.getString("date");
                        anwserText = "今天是" + date + ",天气" + weatherType + "，" + fengxiang + fengli + "，最" + highTemperature +
                                "，最" + lowTemperature + "," + ganmao;
                        if (listener != null) {
                            listener.weatherGet(anwserText);
                        }
                        Csjlogger.debug(anwserText);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}
