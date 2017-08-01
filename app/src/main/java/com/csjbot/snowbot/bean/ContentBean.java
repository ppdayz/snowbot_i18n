package com.csjbot.snowbot.bean;

/**
 * @author: jl
 * @Time: 2016/12/28
 * @Desc:
 */

public class ContentBean {
    private FromBean from;
    private ToBean to;
    private String cmd;
    private String cmdDetail;

    public FromBean getFrom() {
        return from;
    }

    public void setFrom(FromBean from) {
        this.from = from;
    }

    public ToBean getTo() {
        return to;
    }

    public void setTo(ToBean to) {
        this.to = to;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getCmdDetail() {
        return cmdDetail;
    }

    public void setCmdDetail(String cmdDetail) {
        this.cmdDetail = cmdDetail;
    }


}
