package com.csjbot.snowbot.utils.OkHttp;

/**
 * @author: jl
 * @Time: 2016/10/30
 * @Desc:
 */

public class OkHttpException extends Exception {
    public static final int REANSON_UNKNOW = 0x01;
    public static final int REASON_NETWORK = 0x02;
    public static final int REASON_ALL_READY_REG = 0x03;
    public static final int REASON_RESP_BLANK = 0x04;
    public static final int REASON_PARSER_JSON_ERROR = 0x05;

    private int reason = REANSON_UNKNOW;

    public OkHttpException(int reason) {
        this.reason = reason;
    }

    public int getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "OkHttpException reason :" + reason;
    }
}
