package com.csjbot.snowbot.bean;

/**
 * @author: jl
 * @Time: 2016/12/28
 * @Desc:
 */

public class CurrentState {
    private float pm25;
    private float tempreature;
    private float humidity;
    private float power;
    private boolean rechage;

    public float getPm25() {
        return pm25;
    }

    public void setPm25(float pm25) {
        this.pm25 = pm25;
    }

    public float getTempreature() {
        return tempreature;
    }

    public void setTempreature(float tempreature) {
        this.tempreature = tempreature;
    }

    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    public float getPower() {
        return power;
    }

    public void setPower(float power) {
        this.power = power;
    }

    public boolean isRechage() {
        return rechage;
    }

    public void setRechage(boolean rechage) {
        this.rechage = rechage;
    }


}
