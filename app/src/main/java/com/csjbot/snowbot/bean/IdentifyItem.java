package com.csjbot.snowbot.bean;

/**
 * @author: jl
 * @Time: 2017/1/11
 * @Desc:人脸检测候选者信息
 */
public class IdentifyItem {
    private String person_name;
    private float confidence;

    public String getPerson_name() {
        return person_name;
    }

    public void setPerson_name(String person_name) {
        this.person_name = person_name;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }


}
