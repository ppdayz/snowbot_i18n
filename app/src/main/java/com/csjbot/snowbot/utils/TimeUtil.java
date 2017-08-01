package com.csjbot.snowbot.utils;

import android.os.Handler;

/**
 * @author: jl
 * @Time: 2017/2/24
 * @Desc:
 */
public class TimeUtil {
    private static TimeUtil timeUtil = new TimeUtil();

    private Handler handler = new Handler();
    private int time = 0;
    private TimeListener timeListener;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            time--;
            if (time == 0) {
                handler.removeCallbacks(runnable);
                if (null != timeListener) {
                    timeListener.getTime(time);
                }
            } else {
                handler.postDelayed(runnable, 1000);
            }

        }
    };


    private TimeUtil() {

    }

    public static TimeUtil getInterface() {
        return timeUtil;
    }

    public void getTime(int i, TimeListener e) {
        time = i;
        timeListener = e;
        handler.post(runnable);

    }

    public interface TimeListener {
        void getTime(int i);
    }

    public void stop() {
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }

    }

}
