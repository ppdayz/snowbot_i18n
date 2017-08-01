package com.csjbot.snowbot.bean.aiui;

/**
 * Created by Administrator on 2016/9/10 0010.
 */
public class ContentBean {


    /**
     * arg1 : 0
     * info : {"power":387327590400,"beam":2,"CMScore":84,"channel":2,"angle":210}
     * arg2 : 0
     * eventType : 4
     */

    private int arg1;
    private int arg2;
    private int eventType;
    private String info;
    private String result;

    public int getArg1() {
        return arg1;
    }

    public void setArg1(int arg1) {
        this.arg1 = arg1;
    }

    public int getArg2() {
        return arg2;
    }

    public void setArg2(int arg2) {
        this.arg2 = arg2;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
