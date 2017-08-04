package com.csjbot.snowbot.services.serial;


import com.csjbot.csjbase.log.Csjlogger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Copyright (c) 2017, SuZhou CsjBot. All Rights Reserved.
 * www.csjbot.com
 * <p>
 * Created by 浦耀宗 at 2017/07/21 0021-16:44.
 * Email: puyz@csjbot.com
 */

public class Old5MicSerialManager implements DataReceive {
    private static final Old5MicSerialManager ourInstance = new Old5MicSerialManager();

    public static Old5MicSerialManager getInstance() {
        return ourInstance;
    }

    private ExecutorService mMainExecutor;
    private EnglishVersionUart rk3288;

    private Old5MicSerialManager() {
        rk3288 = new EnglishVersionUart();
        mMainExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread ret = new Thread(r, "Old5MicSerialExecutor");
                ret.setDaemon(true);
                return ret;
            }
        });

        mMainExecutor.submit(new Runnable() {
            @Override
            public void run() {
                int ret = rk3288.connect("/dev/ttyS4", 115200);
                if (ret == 0) {
                    rk3288.setDataReceive(Old5MicSerialManager.this);
                }
                Csjlogger.warn("ret = {}", ret);
            }

        });
    }

    public void reset() {
        rk3288.sendData(new byte[]{0x52, 0x45, 0x53, 0x45, 0x54, 0x0D});
    }

    private void sendData(byte[] data) {
        rk3288.sendData(data);
    }

    @Override
    public void onReceive(byte[] data) {

    }
}
