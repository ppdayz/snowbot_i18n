package com.csjbot.snowbot.bean;

import com.csjbot.snowbot.utils.UUIDGenerator;

/**
 * Created by Administrator on 2016/9/5 0005.
 */
public class HeartBeatBean {

    public HeartBeatBean() {
        rc = 0;
        service = "pin";
        type = "robot";
        uid = UUIDGenerator.getInstance().getDeviceUUID();
    }

    /**
     * rc : 0
     * service : pin
     * type : user/robot
     * uid :
     */

    private int rc;
    private String service;
    private String type;
    private String uid;

    public int getRc() {
        return rc;
    }

    public void setRc(int rc) {
        this.rc = rc;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
