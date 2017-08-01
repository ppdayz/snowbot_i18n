package com.csjbot.snowbot.bean;

/**
 * @author: jl
 * @Time: 2016/12/28
 * @Desc:
 */

public class DataContent<T> {
    private FromBean from;
    private ToBean to;
    private String payloadType;
    private T payload;

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

    public String getPayloadType() {
        return payloadType;
    }

    public void setPayloadType(String payloadType) {
        this.payloadType = payloadType;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }


}
