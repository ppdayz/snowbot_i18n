package com.csjbot.snowbot.bean.aiui;

/**
 * Created by Administrator on 2016/10/8 0008.
 */

public class TopResultBean {
    /**
     * sid : cid6f19dc1e@ch00ba0b53d29b010019
     * intent : some String
     */

    private String sid;
    private String intent = "";

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }
}
