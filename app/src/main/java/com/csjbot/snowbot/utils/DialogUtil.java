package com.csjbot.snowbot.utils;

import android.content.DialogInterface;

import java.lang.reflect.Field;

/**
 * @author: jl
 * @Time: 2017/2/20
 * @Desc:
 */

public class DialogUtil {

    /**
     *
     * @param dialogInterface
     * @param close
     * 设置dialog点击按钮是否消失
     */
    public static void canCloseDialog(DialogInterface dialogInterface, boolean close) {
        try {
            Field field = dialogInterface.getClass().getSuperclass().getSuperclass().getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(dialogInterface, close);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
