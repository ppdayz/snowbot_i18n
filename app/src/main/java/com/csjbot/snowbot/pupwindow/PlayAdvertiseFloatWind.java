package com.csjbot.snowbot.pupwindow;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.csjbot.snowbot.R;

/**
 * @author: jl
 * @Time: 2017/1/10
 * @Desc:
 */

public class PlayAdvertiseFloatWind extends LinearLayout {
    /**
     * 记录小悬浮窗的宽度
     */
    public static int viewWidth;

    /**
     * 记录小悬浮窗的高度
     */
    public static int viewHeight;

    /**
     * 用于更新小悬浮窗的位置
     */
    private WindowManager windowManager;

    private WindowManager.LayoutParams wmParams;
    /**
     * 小悬浮窗的参数
     */
    /**
     * 记录当前手指位置在屏幕上的横坐标值
     */
    private float xInScreen;

    /**
     * 记录当前手指位置在屏幕上的纵坐标值
     */
    private float yInScreen;

    /**
     * 记录手指按下时在屏幕上的横坐标的值
     */
    private float xDownInScreen;

    /**
     * 记录手指按下时在屏幕上的纵坐标的值
     */
    private float yDownInScreen;

    /**
     * 记录手指按下时在小悬浮窗的View上的横坐标的值
     */
    private float xInView;
    /**
     * 记录手指按下时在小悬浮窗的View上的纵坐标的值
     */
    private float yInView;
    private ImageView playIv, stopIv;
    private ClickListener clickListener;
    private boolean isplaying = true;

    public PlayAdvertiseFloatWind(Context context) {
        super(context);
        wmParams = new WindowManager.LayoutParams();
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        //设置window type
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        //设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags =
//          LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//          LayoutParams.FLAG_NOT_TOUCHABLE
        ;

        //调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.TOP | Gravity.LEFT;

        // 以屏幕左上角为原点，设置x、y初始值
        wmParams.x = 0;
        wmParams.y = 0;
        //设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        LinearLayout view = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.advertisement_float, this);
        windowManager.addView(view, wmParams);
        viewWidth = view.getLayoutParams().width;
        viewHeight = view.getLayoutParams().height;
        playIv = (ImageView) view.findViewById(R.id.play_iv);
        stopIv = (ImageView) view.findViewById(R.id.stop_iv);
        playIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != clickListener) {
                    clickListener.clickPlay(isplaying);
                    if (isplaying) {
                        isplaying = false;
                        playIv.setImageResource(R.drawable.pause);
                    } else {
                        isplaying = true;
                        playIv.setImageResource(R.drawable.play);
                    }

                }

            }
        });
        stopIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != clickListener) {
                    clickListener.clickStop();
                }
                windowManager.removeViewImmediate(PlayAdvertiseFloatWind.this);
            }
        });
    }


    public void remove(){
        windowManager.removeViewImmediate(PlayAdvertiseFloatWind.this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xInView = event.getX();
                yInView = event.getY();
                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY();
                xInScreen = event.getRawX();
                yInScreen = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                xInScreen = event.getRawX();
                yInScreen = event.getRawY();
                updateViewPosition();
                break;

        }
        return super.onTouchEvent(event);
    }




    /**
     * 更新小悬浮窗在屏幕中的位置。
     */
    private void updateViewPosition() {
        wmParams.x = (int) (xInScreen - xInView);
        wmParams.y = (int) (yInScreen - yInView);
        windowManager.updateViewLayout(this, wmParams);
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void clickPlay(boolean isplaying);

        void clickStop();
    }

}
