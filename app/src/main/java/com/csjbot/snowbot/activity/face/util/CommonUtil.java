package com.csjbot.snowbot.activity.face.util;

import android.app.Activity;
import android.util.DisplayMetrics;

/**
 * 作者：EchoJ on 2017/3/3 17:26 <br>
 * 邮箱：echojiangyq@gmail.com <br>
 * 描述：
 */

public class CommonUtil {
    
public static int getWindowHeight(Activity mActivity) {
    //定义DisplayMetrics 对象    
    DisplayMetrics dm = new DisplayMetrics();
    //取得窗口属性    
    mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
    //窗口高度    
    return dm.heightPixels;
}
    
public static int getWindowWidth(Activity mActivity) {
    //定义DisplayMetrics 对象    
    DisplayMetrics dm = new DisplayMetrics();
    //取得窗口属性    
    mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
    //窗口高度    
    return dm.widthPixels;
}
}
