package com.csjbot.snowbot.bean;

import com.slamtec.slamware.robot.Location;
import com.slamtec.slamware.robot.Rotation;

import java.io.Serializable;

/**
 * @Author: jl
 * @Date: 2016/12/20
 * @Desc:
 */

public class Home implements Serializable {
    private int index;
    private float mOffsetX;
    private float mOffsetY;
    private String homename;
    private Location location;
    private Rotation rotation;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getHomename() {
        return homename;
    }

    public void setHomename(String homename) {
        this.homename = homename;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }

    public float getmOffsetX() {
        return mOffsetX;
    }

    public void setmOffsetX(float mOffsetX) {
        this.mOffsetX = mOffsetX;
    }

    public float getmOffsetY() {
        return mOffsetY;
    }

    public void setmOffsetY(float mOffsetY) {
        this.mOffsetY = mOffsetY;
    }


}
