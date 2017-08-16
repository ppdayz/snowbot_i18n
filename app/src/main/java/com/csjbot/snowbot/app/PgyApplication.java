package com.csjbot.snowbot.app;

import android.content.Intent;

import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.csjbot.snowbot.listener.MyLocationListener;
import com.csjbot.snowbot.services.FloatingWindowsService;
import com.csjbot.snowbot.services.SearchUSBService;
import com.csjbot.snowbot_rogue.services.BackgroundPlayService;
import com.csjbot.snowbot_rogue.services.RogueBGService;
import com.pgyersdk.crash.PgyCrashManager;
import com.usb2uartagent.Usb2UartagentManager;

/**
 * @author: jl
 * @Time: 2016/12/26
 * @Desc:基础application
 */

public class PgyApplication extends CsjApplication {
    public LocationClient mLocationClient = null;//百度定位
    public BDLocationListener myListener = new MyLocationListener();
    private Intent rogueBGServiceIntent, backgroundPlayService, aiuiService, searchUsbService, floatingWindowsService;

    @Override
    public void onCreate() {
        super.onCreate();
//        Usb2UartagentManager.getInstance().initUsb2UartagentManager(this);
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        initLocation();
        mLocationClient.start();
        PgyCrashManager.register(this);
        new Thread(() -> {
            rogueBGServiceIntent = new Intent(this, RogueBGService.class);
            backgroundPlayService = new Intent(this, BackgroundPlayService.class);
//            aiuiService = new Intent(this, AIUIService.class);
            searchUsbService = new Intent(this, SearchUSBService.class);
            floatingWindowsService = new Intent(this, FloatingWindowsService.class);
            startService(backgroundPlayService);
            startService(rogueBGServiceIntent);
            startService(searchUsbService);
//            startService(aiuiService);
            startService(floatingWindowsService);
        }).start();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        stopService(rogueBGServiceIntent);
        stopService(searchUsbService);
        Usb2UartagentManager.getInstance().releaseAll();
    }

    /**
     * 初始化定位信息
     */
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000 * 60 * 10;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(false);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }
}
