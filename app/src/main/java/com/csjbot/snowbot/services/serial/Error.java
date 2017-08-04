package com.csjbot.snowbot.services.serial;

/**
 * Copyright (c) 2016, SuZhou CsjBot. All Rights Reserved. <br>
 * www.csjbot.com<br>
 * <p>
 * Created by 浦耀宗 at 2016/11/07 0007-19:19.<br>
 * Email: puyz@csjbot.com<br>
 */
public class Error {

    public class Uart {
        public static final int CONNECT_SUCCESS = 0;
        public static final int CONNECT_SECURITY_EXCEPTION = 1;
        public static final int CONNECT_IO_ERROR = 3;


        public static final int SEND_SUCCESS = 0;
        public static final int SEND_OUT_STREAM_NULL = 1;
        public static final int SEND_IO_ERROR = 2;
    }
}
