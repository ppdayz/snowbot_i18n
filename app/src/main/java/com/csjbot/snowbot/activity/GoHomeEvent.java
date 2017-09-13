package com.csjbot.snowbot.activity;

import com.csjbot.csjbase.event.IBus;

/**
 * Copyright (c) 2017, SuZhou CsjBot. All Rights Reserved.
 * www.csjbot.com
 * <p>
 * Created by 浦耀宗 at 2017/07/07 0007-13:06.
 * Email: puyz@csjbot.com
 */

public class GoHomeEvent implements IBus.IEvent {
    public static final int GOHOME_SUCCESS = 0x00;
    public static final int GOHOME_FAILED_BUT_CHARGING = 0x01;
    public static final int GOHOME_NOT_FIND_POSE = 0x02;
    public static final int GOHOME_TRY_OUT_OF_TIMES = 0x03;
    public static final int EXIT_CHARGING_PAGE = 0x04;

    private int tag;
    private String desc;


    public GoHomeEvent(int tag, String desc) {
        this.tag = tag;
        this.desc = desc;
    }

    public String getDesc() {
        return this.desc;
    }

    @Override
    public int getTag() {
        return tag;
    }
}
