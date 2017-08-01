package com.csjbot.snowbot.bean;

/**
 * @author: jl
 * @Time: 2017/1/13
 * @Desc:
 */

public class UpdateDeviceDataBean {

    /**
     * productKey :
     * deviceKey :
     * status :
     * geo : {"x":12.11,"y":-43.901}
     */

    private String productKey;
    private String deviceKey;
    private String status;
    private GeoBean geo;

    public String getProductKey() {
        return productKey;
    }

    public void setProductKey(String productKey) {
        this.productKey = productKey;
    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(String deviceKey) {
        this.deviceKey = deviceKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public GeoBean getGeo() {
        return geo;
    }

    public void setGeo(GeoBean geo) {
        this.geo = geo;
    }

    public static class GeoBean {
        /**
         * x : 12.11
         * y : -43.901
         */

        private double x;
        private double y;

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }
    }
}
