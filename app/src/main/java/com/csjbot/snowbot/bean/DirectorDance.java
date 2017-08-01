package com.csjbot.snowbot.bean;


import com.csjbot.snowbot.utils.OkHttp.DanceBuilder;

/**
 * @author: jl
 * @Time: 2017/2/9
 * @Desc:
 */

public class DirectorDance {
    private String music1 = "dance1.mp3";
    private String music2 = "dance2.mp3";
    private String music3 = "dance3.mp3";
    private DanceBuilder builder = new DanceBuilder();

    public Dance getDance1() {
        builder.assembleDance(1, 1, 1, 1, 1, music1);
        return builder.buildDance();
    }

    public Dance getDance2() {
        builder.assembleDance(1, 1, 2, 2, 2, music2);
        return builder.buildDance();
    }

    public Dance getDance3() {
        builder.assembleDance(1, 1, 3, 3, 3, music3);
        return builder.buildDance();
    }
}
