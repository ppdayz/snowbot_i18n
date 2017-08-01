package com.csjbot.snowbot.views.dialog;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * @项目名 SnowBot-SDK
 * @路径 name：com.csjbot.snowbot.views.dialog
 * @创建者 jwc
 * @创建时间 2017/6/14 15:35
 */

public class MyWaitingDialog extends ProgressDialog{

    public MyWaitingDialog(Context context) {
        super(context);
        init();
    }

    public MyWaitingDialog(Context context, int theme) {
        super(context, theme);
        init();
    }

    /**
     * 初始化
     */
    private void init(){
        setIndeterminate( true );
        setCancelable( false );
    }

    /**
     * 显示dialog
     * @param title
     * @param message
     */
    public void showDialog(String title,String message){
        if(title != null) {
            this.setTitle(title);
        }
        if(message != null){
            this.setMessage(message);
        }
        this.show();
    }
}
