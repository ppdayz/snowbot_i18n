package com.csjbot.snowbot.bean;

import android.net.wifi.ScanResult;

/**
 * @author: jl
 * @Time: 2017/1/4
 * @Desc:
 */

public class WifiDataBean {
    private ScanResult scanResult;
    private boolean connectStatus = false;

    public boolean isConnectStatus() {
        return connectStatus;
    }

    public void setConnectStatus(boolean connectStatus) {
        this.connectStatus = connectStatus;
    }

    public ScanResult getScanResult() {
        return scanResult;
    }

    public void setScanResult(ScanResult scanResult) {
        this.scanResult = scanResult;
    }


}
