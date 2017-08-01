package com.csjbot.snowbot.bean;

import java.io.Serializable;

/**
 * @Author: jl
 * @Date: 2016/12/19
 * @Desc:
 */

public class OutWare implements Serializable {
    private boolean flag;
    private String message;

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
