package com.csjbot.snowbot.utils.OkHttp;

/**
 * Created by Administrator on 2017/3/23 0023.
 */

import com.csjbot.snowbot.bean.Dance;
import com.csjbot.snowbot.utils.Builder;

/**
 * @author: jl
 * @Time: 2017/2/9
 * @Desc:
 */

public class DanceBuilder extends Builder {
    private Dance dance = new Dance();

    @Override
    public void setPart() {

    }

    public void assembleDance(int leftCircle, int rtghtCircle, int leftHand, int rightHand, int totalHand, String musicName) {
        setLeftCircle(leftCircle);
        setRtghtCircle(rtghtCircle);
        setLeftHand(leftHand);
        setRightHand(rightHand);
        setTotalHand(totalHand);
        setMusicPath(musicName);
    }

    public void setMusicPath(String musicName) {
        dance.setMusicPath(musicName);
    }


    public void setLeftCircle(int leftCircle) {
        dance.setLeftCircle(leftCircle);
    }


    public void setRtghtCircle(int rtghtCircle) {
        dance.setRtghtCircle(rtghtCircle);
    }


    public void setLeftHand(int leftHand) {
        dance.setLeftHand(leftHand);

    }


    public void setRightHand(int rightHand) {
        dance.setRightHand(rightHand);

    }


    public void setTotalHand(int totalHand) {
        dance.setTotalHand(totalHand);

    }


    @Override
    public Dance buildDance() {
        return dance;
    }
}
