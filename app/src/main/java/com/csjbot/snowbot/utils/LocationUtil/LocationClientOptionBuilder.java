package com.csjbot.snowbot.utils.LocationUtil;

import com.baidu.location.LocationClientOption;

/**
 * Created by xiasuhuei321 on 2017/6/20.
 * author:luo
 * e-mail:xiasuhuei321@163.com
 */

public class LocationClientOptionBuilder {
    private LocationClientOption option;

    private LocationClientOptionBuilder() {
        option = new LocationClientOption();
        // 设置返回的定位结果坐标系
        option.setCoorType("bd09ll");
        // 设置定位模式，本项目中只用wifi
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        // 发起定位请求间隔，0为只定位一次
        option.setScanSpan(0);
        // 设置是否需要地址信息
        option.setIsNeedAddress(true);
        // 设置是否用gps
        option.setOpenGps(false);
        option.setLocationNotify(true);
        option.setIgnoreKillProcess(false);
    }

    public static LocationClientOptionBuilder builder() {
        return new LocationClientOptionBuilder();
    }

    /**
     * 设置扫描间隔，0为仅定位一次
     * @param span 定位间隔时间
     * @return this
     */
    public LocationClientOptionBuilder setScanSpan(int span) {
        option.setScanSpan(span);
        return this;
    }

    /**
     * 获取LocationClientOption的实例
     * @return LocationClientOption的实例
     */
    public LocationClientOption build(){
        return option;
    }
}
