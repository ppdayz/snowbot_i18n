package com.csjbot.snowbot.services.serial;

/**
 * Copyright (c) 2016, SuZhou CsjBot. All Rights Reserved.
 * www.csjbot.com
 * <p>
 * Created by 浦耀宗 at 2016/11/17 0017-12:34.
 * Email: puyz@csjbot.com
 */

public interface DataReceive {
    void onReceive(byte[] data);
}
