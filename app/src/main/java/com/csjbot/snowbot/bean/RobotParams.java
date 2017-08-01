package com.csjbot.snowbot.bean;

import java.io.Serializable;

/**
 * @Author: jl
 * @Date: 2016/12/19
 * @Desc:
 */

public class RobotParams implements Serializable {
    private String status;
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
