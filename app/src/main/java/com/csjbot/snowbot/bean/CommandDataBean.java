package com.csjbot.snowbot.bean;

import java.util.Date;

/**
 * @author: jl
 * @Time: 2016/12/28
 * @Desc:
 */

public class CommandDataBean<T> {
    private String service;
    private String server;
    private String timestamp;
    private T content;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }


}
