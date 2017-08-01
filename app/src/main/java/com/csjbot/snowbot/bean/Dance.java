package com.csjbot.snowbot.bean;

/**
 * @author: jl
 * @Time: 2017/2/9
 * @Desc:小雪人舞蹈
 */

public class Dance {
    private int leftCircle = 1;
    private int rtghtCircle = 1;
    private int leftHand = 1;
    private int rightHand = 1;

    public String getMusicPath() {
        return musicPath;
    }

    public void setMusicPath(String musicPath) {
        this.musicPath = musicPath;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    private String musicPath = "";
    private boolean isPlaying = false;

    public int getTotalHand() {
        return totalHand;
    }

    public void setTotalHand(int totalHand) {
        this.totalHand = totalHand;
    }

    private int totalHand = 1;

    public int getLeftCircle() {
        return leftCircle;
    }

    public void setLeftCircle(int leftCircle) {
        this.leftCircle = leftCircle;
    }

    public int getRtghtCircle() {
        return rtghtCircle;
    }

    public void setRtghtCircle(int rtghtCircle) {
        this.rtghtCircle = rtghtCircle;
    }

    public int getLeftHand() {
        return leftHand;
    }

    public void setLeftHand(int leftHand) {
        this.leftHand = leftHand;
    }

    public int getRightHand() {
        return rightHand;
    }

    public void setRightHand(int rightHand) {
        this.rightHand = rightHand;
    }


}
