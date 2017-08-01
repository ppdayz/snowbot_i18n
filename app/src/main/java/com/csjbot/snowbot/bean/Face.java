package com.csjbot.snowbot.bean;

import java.util.List;

/**
 * @author: jl
 * @Time: 2017/1/11
 * @Desc:
 */

public class Face {
    private boolean glass;

    private int expression;

    private List<FaceShape> face_shape;

    private int gender;

    private int beauty;

    private int roll;

    private int yaw;

    private int x;

    private int width;

    private String face_id;

    private int y;

    private int pitch;

    private int age;

    private int height;

    public void setGlass(boolean glass) {
        this.glass = glass;
    }

    public boolean getGlass() {
        return this.glass;
    }

    public void setExpression(int expression) {
        this.expression = expression;
    }

    public int getExpression() {
        return this.expression;
    }

    public void setFace_shape(List<FaceShape> face_shape) {
        this.face_shape = face_shape;
    }

    public List<FaceShape> getFace_shape() {
        return this.face_shape;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getGender() {
        return this.gender;
    }

    public void setBeauty(int beauty) {
        this.beauty = beauty;
    }

    public int getBeauty() {
        return this.beauty;
    }

    public void setRoll(int roll) {
        this.roll = roll;
    }

    public int getRoll() {
        return this.roll;
    }

    public void setYaw(int yaw) {
        this.yaw = yaw;
    }

    public int getYaw() {
        return this.yaw;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getX() {
        return this.x;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        return this.width;
    }

    public void setFace_id(String face_id) {
        this.face_id = face_id;
    }

    public String getFace_id() {
        return this.face_id;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getY() {
        return this.y;
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;
    }

    public int getPitch() {
        return this.pitch;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getAge() {
        return this.age;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return this.height;
    }

}
