/*
 * Copyright (c) 2012, Pandoranews Corporation, All Rights Reserved
 */
package com.csjbot.snowbot.views;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.csjbot.snowbot.R;


/**
 *@Author: jl
 *@Date: 2016/12/20
 *@Desc:pupwindow 基类
 *
 */
public abstract class BasePopupWindow extends PopupWindow {

    protected View anchor;
    protected View contentView;
    protected Context context;
    protected int aniType = -1;
    protected WindowManager windowManager;

    public BasePopupWindow(View anchor, View contentView, Context context, int aniType) {
        super();
        this.anchor = anchor;
        this.contentView = contentView;
        this.context = context;
        this.aniType = aniType;
        iniPopupWindow();
    }

    public BasePopupWindow(View contentView, Context context, int aniType) {
        super();
        this.contentView = contentView;
        this.context = context;
        this.aniType = aniType;
        iniPopupWindow();
    }

    public BasePopupWindow(View anchor, View contentView, Context context) {
        super();
        this.anchor = anchor;
        this.contentView = contentView;
        this.context = context;
        iniPopupWindow();
    }

    /**
     * @description 初始化PopupWindow
     * @author WangXu
     * @createDate 2014-11-13
     * @version 1.0
     */
    private void iniPopupWindow() {
        this.setContentView(contentView);
        this.setFocusable(true);
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                setFocusable(false);
            }
        });
        this.setOutsideTouchable(true);
        this.setBackgroundDrawable(new ColorDrawable());
        this.setWidth(LayoutParams.WRAP_CONTENT);
        this.setHeight(LayoutParams.WRAP_CONTENT);
        this.update();
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (aniType < 0) {

        } else {
            iniAnimation(aniType);
        }

    }



    /**
     * @description 设置PopupWindow弹出动画
     * @author WangXu
     * @createDate 2014-11-13
     * @version 1.0
     */
    private void iniAnimation(int aniType2) {
        switch (aniType2) {
            // aniType 为动画种类 共有四种，
            // 0 右上角缩放，1 左上角缩放，2 右下角缩放，3 右上角缩放
            case 0:
                this.setAnimationStyle(R.style.PopFadeRightUp);
                break;
            case 1:
                this.setAnimationStyle(R.style.PopFadeLeftUp);
                break;
            case 2:
                this.setAnimationStyle(R.style.PopFadeRightDown);
                break;
            case 3:
                this.setAnimationStyle(R.style.PopFadeRightUp);
                break;
            case 4:
                this.setAnimationStyle(R.style.PopFadeSuspendLeftUp);
                break;
            case 5:
                this.setAnimationStyle(R.style.PopFadeCenter);
            default:
                break;
        }

    }


    /**
     * @description 初始化里面的view
     * @author WangXu
     * @createDate 2014-11-13
     * @version 1.0
     */
    public abstract void initView();

    public void show(int i) {
        int[] location = new int[2];
        if (anchor != null) {
            anchor.getLocationOnScreen(location);
        }
        int xoffset;
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);


        contentView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        switch (i) {
            case 0:// 父控件正下方
                xoffset = anchor.getWidth() / 2 - contentView.getMeasuredWidth() / 2;
                showAsDropDown(anchor, xoffset, 0);
                break;
            case 1:// 父控件正上方
                xoffset = anchor.getWidth() / 2 - contentView.getMeasuredWidth() / 2;
                showAtLocation(anchor, Gravity.NO_GRAVITY, location[0] - anchor.getWidth(), location[1] - contentView.getMeasuredHeight());
                break;
            case 2:// 父控件左上对齐
                showAtLocation(anchor, Gravity.NO_GRAVITY, location[0], location[1] - contentView.getMeasuredHeight());
                break;
            case 3:// 父控件右上对齐
                showAtLocation(anchor, Gravity.NO_GRAVITY, location[0] - contentView.getMeasuredWidth() + anchor.getWidth(),
                        location[1] - contentView.getMeasuredHeight());
                break;
            case 4:
                showAsDropDown(anchor, 0, 0);// 父控件左下对齐
                break;
            case 5:
                xoffset = anchor.getWidth() - contentView.getMeasuredWidth();// 父控件右下对齐
                showAsDropDown(anchor, xoffset, 0);
                break;
            case 6:
                showAtLocation(anchor, Gravity.CENTER, 0, 0);// 在View中 居中显示
                break;
            case 7:
                showAtLocation(anchor, Gravity.CENTER_HORIZONTAL, 0, 70);// 在View中 顶部显示
                break;
            case 8:
                showAtLocation(anchor, Gravity.NO_GRAVITY, location[0], location[1]);
                break;
            case 9:
//                xoffset = anchor.getWidth() - contentView.getMeasuredWidth();// 父控件右下对齐
                showAsDropDown(anchor, 0, -20);
                break;
            case 10:
//                xoffset = anchor.getWidth() - contentView.getMeasuredWidth();// 父控件右下对齐
                showAsDropDown(anchor, -280, 15);
                break;
            case 11:
                showAtLocation(anchor, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                break;
            case 12:// 父控件正下方40dp
                xoffset = anchor.getWidth() / 2 - contentView.getMeasuredWidth() / 2;
                showAsDropDown(anchor, xoffset, 40);
                break;
            default:
                showAsDropDown(anchor, 0, 0);// 父控件左下对齐
                break;
        }

    }

}
