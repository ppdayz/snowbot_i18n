package com.csjbot.snowbot.services;

import com.csjbot.csjbase.event.IBus;

/**
 * Copyright (c) 2017, SuZhou CsjBot. All Rights Reserved.
 * www.csjbot.com
 * <p>
 * Created by 浦耀宗 at 2017/07/22 0022-13:47.
 * Email: puyz@csjbot.com
 */

public class EventWakeup implements IBus.IEvent {
    private int mTag;
    private Object mObject;

    public EventWakeup(int tag, int angle) {
        mTag = tag;
        mObject = angle;
    }

    public int getTag() {
        return mTag;
    }

    public int getAngle() {
        return (int) mObject;
    }

    public Object getObject() {
        return mObject;
    }
}
