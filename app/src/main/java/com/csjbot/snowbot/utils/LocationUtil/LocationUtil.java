package com.csjbot.snowbot.utils.LocationUtil;

import android.content.Context;
import android.content.SharedPreferences;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.csjbot.snowbot.app.MyApplication;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiasuhuei321 on 2017/6/20.
 * author:luo
 * e-mail:xiasuhuei321@163.com
 */

public class LocationUtil {
    // 这个地址用来在用户设置地址的界面展示
    public static final String DEFAULT_CUSTOM_LOCATION_COMPLETE = "江苏省苏州市昆山市";
    public static final String DEFAULT_CUSTOM_LOCATION_CITY = "昆山市";
    private static String customLocationComplete = DEFAULT_CUSTOM_LOCATION_COMPLETE;
    // 这个地址是直接到县（县级市）
    private static String customLocationCity = DEFAULT_CUSTOM_LOCATION_CITY;


    // 下面三个元素分别代表省、市、县（县级市）的index，用于在SettingsSetTouchAction中
    // 展示默认选中的item
    public static int provinceIndex = 0;
    public static int cityIndex = 0;
    public static int areaIndex = 0;

    private static LocationClient client;
    private static BDLocationListener listener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            switch (bdLocation.getLocType()) {
                case BDLocation.TypeNetWorkLocation:
                case BDLocation.TypeOffLineLocation:
                    latitude = bdLocation.getLatitude();
                    longitude = bdLocation.getLongitude();
                    SharedPreferences sp = MyApplication.getAppContext()
                            .getSharedPreferences(LOCATION_CONF, Context.MODE_PRIVATE);
                    sp.edit().putLong(LATITUDE, (long) latitude)
                            .putLong(LONGITUDE, (long) longitude)
                            .apply();
                    break;
                case BDLocation.TypeServerError:
                case BDLocation.TypeNetWorkException:
                case BDLocation.TypeCriteriaException:
                    if (latitude == -1 || longitude == -1) {
                        latitude = getLongFromShared(LATITUDE);
                        longitude = getLongFromShared(LONGITUDE);
                    }
                    break;
            }

//            latitude = bdLocation.getLatitude();
//            longitude = bdLocation.getLongitude();
//            Log.e("===", "latitude" + bdLocation.getLatitude());
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    };
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String LOCATION_CONF = "location_conf";
    public static final String LOCATION_STR = "location_str";
    public static final String LOCATION_CITY_STR = "location_city_str";
    private static double latitude = -1;
    private static double longitude = -1;
    private static Map<String, Double> map;

    private static final double DEFAULT_LATITUDE = 31.33154000f;
    private static final double DEFAULT_LONGITUDE = 120.88511500f;

    public static void init(Context context, BDLocationListener listener) {
        // 检索的sdk
//        SDKInitializer.initialize(context);
        // 定位初始化
        client = new LocationClient(context.getApplicationContext());
        client.registerLocationListener(listener);
        client.setLocOption(LocationClientOptionBuilder.builder()
                .setScanSpan(0).build());

    }

    public static void init(Context context) {
        init(context, listener);
        // 从配置文件中获取用户定义的地址
        customLocationComplete = getStrFromShared(LOCATION_STR);
        customLocationCity = getStrFromShared(LOCATION_CITY_STR);
    }

    /**
     * 开始定位，默认只定位一次
     */
    public static void start() {
        client.start();
    }

    public static void start(BDLocationListener listener) {
        client.registerLocationListener(listener);
        client.start();
    }

    /**
     * 停止定位，默认的设置是不杀死定位服务
     */
    public static void stop() {
        client.stop();
    }

    /**
     * 获取缓存的经纬度
     *
     * @return 如果本地没有有效数据，
     */
    public static Map<String, Double> getLocation() {
        if (map != null && map.get(LATITUDE) != null &&
                map.get(LONGITUDE) != null) {
            return map;
        }

        map = new HashMap<>();

        // 如果经度或者纬度 = -1，就从本地中读取
        // 否则就用定位到的
        if (longitude == -1 || latitude == -1) {
            double latitude = getLongFromShared(LATITUDE);
            double longtitude = getLongFromShared(LONGITUDE);

            //如果本地读取都不是 -1， 就用本地的，否则就用默认的
            if (latitude != -1 && longtitude != -1) {
                map.put(LATITUDE, latitude);
                map.put(LONGITUDE, longtitude);
            } else {
                map.put(LATITUDE, DEFAULT_LATITUDE);
                map.put(LONGITUDE, DEFAULT_LONGITUDE);
            }
        } else {
            map.put(LATITUDE, latitude);
            map.put(LONGITUDE, longitude);
        }

        return map;
    }


    public static Double getLatitude() {
        return getLocation().get(LATITUDE);
    }

    public static Double getLongitude() {
        return getLocation().get(LONGITUDE);
    }

    private static long getLongFromShared(String key) {
        SharedPreferences sp = MyApplication.getAppContext()
                .getSharedPreferences(LOCATION_CONF, Context.MODE_PRIVATE);
        return sp.getLong(key, -1);
    }

    private static String getStrFromShared(String key) {
        SharedPreferences sp = MyApplication.getAppContext()
                .getSharedPreferences(LOCATION_CONF, Context.MODE_PRIVATE);
        if (key != null && key.equals(LOCATION_STR)) {
            return sp.getString(key, DEFAULT_CUSTOM_LOCATION_COMPLETE);
        } else if (key != null && key.equals(LOCATION_CITY_STR)) {
            return sp.getString(key, DEFAULT_CUSTOM_LOCATION_CITY);
        }
        return null;
    }

    private static void saveToShared(String key, String value) {
        SharedPreferences sp = MyApplication.getAppContext()
                .getSharedPreferences(LOCATION_CONF, Context.MODE_PRIVATE);
        sp.edit().putString(key, value).apply();
    }

    public static String getCustomLocationComplete() {
        return customLocationComplete;
    }

    public static void setCustomLocationComplete(String location) {
        customLocationComplete = location;
        saveToShared(LOCATION_STR, location);
    }

    public static void setCustomLocationCity(String customLocationCity) {
        LocationUtil.customLocationCity = customLocationCity;
        saveToShared(LOCATION_CITY_STR, customLocationCity);
    }

    public static String getCustomLocationCity() {
        return customLocationCity;
    }

}
