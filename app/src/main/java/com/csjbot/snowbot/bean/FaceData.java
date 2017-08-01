package com.csjbot.snowbot.bean;

import java.util.List;

/**
 * @author: jl
 * @Time: 2017/1/11
 * @Desc:
 */

public class FaceData {
    private List<Face> face;

    private int image_height;

    private String session_id;

    private int image_width;

    public void setFace(List<Face> face) {
        this.face = face;
    }

    public List<Face> getFace() {
        return this.face;
    }

    public void setImage_height(int image_height) {
        this.image_height = image_height;
    }

    public int getImage_height() {
        return this.image_height;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public String getSession_id() {
        return this.session_id;
    }

    public void setImage_width(int image_width) {
        this.image_width = image_width;
    }

    public int getImage_width() {
        return this.image_width;
    }
}
