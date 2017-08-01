package com.csjbot.snowbot.bean;

/**
 * Created by Administrator on 2016/9/1 0001.
 */
public class PacketBean {

    public PacketBean() {
    }

    public PacketBean(int rc, String service, String uid, Object semantic) {
        this.rc = rc;
        this.service = service;
        this.type = "robot";
        this.uid = uid;
        this.semantic = semantic;
    }

    /**
     * semantic : {"slots":{"datetime":"xxxxxx"}}
     * rc : 0
     * service : login
     * type : user/robot
     * uid :
     */

    private int rc;
    private String service;
    private String type;
    private String uid;
    private Object semantic;


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

    public Object getSemantic() {
        return semantic;
    }

    public void setSemantic(Object semantic) {
        this.semantic = semantic;
    }
}
