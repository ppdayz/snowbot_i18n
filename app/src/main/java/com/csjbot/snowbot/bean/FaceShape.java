package com.csjbot.snowbot.bean;

import java.util.List;

/**
 * @author: jl
 * @Time: 2017/1/11
 * @Desc:
 */

public class FaceShape {
    private List<FaceProfile> face_profile;
    private List<FaceProfile> left_eye;
    private List<FaceProfile> right_eye;
    private List<FaceProfile> left_eyebrow;
    private List<FaceProfile> right_eyebrow;
    private List<FaceProfile> mouth;
    private List<FaceProfile> nose;


    public List<FaceProfile> getNose() {
        return nose;
    }

    public void setNose(List<FaceProfile> nose) {
        this.nose = nose;
    }

    public List<FaceProfile> getFace_profile() {
        return face_profile;
    }

    public void setFace_profile(List<FaceProfile> face_profile) {
        this.face_profile = face_profile;
    }

    public List<FaceProfile> getLeft_eye() {
        return left_eye;
    }

    public void setLeft_eye(List<FaceProfile> left_eye) {
        this.left_eye = left_eye;
    }

    public List<FaceProfile> getRight_eye() {
        return right_eye;
    }

    public void setRight_eye(List<FaceProfile> right_eye) {
        this.right_eye = right_eye;
    }

    public List<FaceProfile> getLeft_eyebrow() {
        return left_eyebrow;
    }

    public void setLeft_eyebrow(List<FaceProfile> left_eyebrow) {
        this.left_eyebrow = left_eyebrow;
    }

    public List<FaceProfile> getRight_eyebrow() {
        return right_eyebrow;
    }

    public void setRight_eyebrow(List<FaceProfile> right_eyebrow) {
        this.right_eyebrow = right_eyebrow;
    }

    public List<FaceProfile> getMouth() {
        return mouth;
    }

    public void setMouth(List<FaceProfile> mouth) {
        this.mouth = mouth;
    }


}
