package com.csjbot.snowbot.bean;

import java.util.List;

/**
 * @author: jl
 * @Time: 2017/1/11
 * @Desc:人脸检测实体类
 */

public class FaceIdentify {
    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public List<IdentifyItem> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<IdentifyItem> candidates) {
        this.candidates = candidates;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String session_id;
    private List<IdentifyItem> candidates;
    private int code;
    private String message;
}
