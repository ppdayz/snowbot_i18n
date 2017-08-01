package com.csjbot.snowbot.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.csjbot.snowbot.R;

/**
 * Created by cxm on 2016/9/22.
 */
public class BatteryView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private Paint paintS, paintF, paintHead;
    private int bwidth;
    private int bheight;
    int varyInnerWidth;
    private int bColor, backgroundColor;
    private float bRadius;
    private RectF SrectF, FrectF, FrectReal;
    private Thread thread;
    private SurfaceHolder surfaceHolder;
    private Canvas canvas;
    private boolean isThreadRunning = true;
    private int distance;
    private int centerX, centerY;
    private float fulllength;
    private int batteryPercent = 0;
    private boolean isCharging = false;
    private int sleepTime = 100;

    public BatteryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public BatteryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BatteryView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
//        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        setZOrderMediaOverlay(true);
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);

        SrectF = new RectF();
        FrectF = new RectF();
        distance = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getContext().getResources().getDisplayMetrics());

        TypedArray attrArray = getContext().obtainStyledAttributes(attrs, R.styleable.BatteryLoadingView);
        if (attrArray != null) {

            bwidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getContext().getResources().getDisplayMetrics());
            bheight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getContext().getResources().getDisplayMetrics());

            bwidth = attrArray.getDimensionPixelSize(R.styleable.BatteryLoadingView_outBWidth, bwidth);
            bheight = attrArray.getDimensionPixelSize(R.styleable.BatteryLoadingView_outBHeight, bheight);
            bColor = attrArray.getColor(R.styleable.BatteryLoadingView_bColor, Color.GREEN);
            backgroundColor = attrArray.getColor(R.styleable.BatteryLoadingView_backgroundColor, Color.TRANSPARENT);
            bRadius = attrArray.getFloat(R.styleable.BatteryLoadingView_bRadius, 9f);
            attrArray.recycle();
        }
        initPaint();
    }

    private void initPaint() {
        paintS = new Paint();
        paintF = new Paint();
        paintHead = new Paint();

        paintS.setColor(bColor);
        paintF.setColor(bColor);
        paintHead.setColor(bColor);

        paintS.setStyle(Paint.Style.STROKE);
        paintS.setStrokeWidth(6);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        thread = new Thread(this);
//        thread.start();
        isThreadRunning = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        varyInnerWidth = 0;

        centerX = getMeasuredWidth() / 2;
        centerY = getMeasuredHeight() / 2;

        SrectF.left = centerX - bwidth / 2;
        SrectF.top = centerY - bheight / 2;
        SrectF.right = centerX + bwidth / 2 - distance * 2;
        SrectF.bottom = centerY + bheight / 2;

        FrectF.left = SrectF.left + 5;
        FrectF.top = SrectF.top + 5;
        FrectF.right = SrectF.left - 5;
        FrectF.bottom = SrectF.bottom - 5;

        fulllength = centerX + bwidth / 2 - distance * 2 - SrectF.left;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isThreadRunning = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        if (modeWidth == MeasureSpec.AT_MOST || modeWidth == MeasureSpec.UNSPECIFIED) {
            sizeWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bwidth, getContext().getResources().getDisplayMetrics());
            sizeWidth += getPaddingLeft() + getPaddingRight();
        }
        if (modeHeight == MeasureSpec.AT_MOST || modeHeight == MeasureSpec.UNSPECIFIED) {
            sizeHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bheight, getContext().getResources().getDisplayMetrics());
            sizeHeight += getPaddingBottom() + getPaddingTop();
        }

        setMeasuredDimension(sizeWidth, sizeHeight);

    }


    private void drawView(Canvas canvas) {
        canvas.drawRoundRect(SrectF, bRadius, bRadius, paintS);
        canvas.drawRect(centerX + bwidth / 2 - distance, centerY - bheight / 4, centerX + bwidth / 2,
                centerY + bheight / 4, paintHead);
        FrectF.right = FrectF.left + varyInnerWidth;

        canvas.drawRoundRect(FrectF, bRadius, bRadius, paintF);

        if (isCharging) {
            if (SrectF.left + varyInnerWidth < centerX + bwidth / 2 - distance * 2 - 10) {
                varyInnerWidth += 2;
            } else {
                varyInnerWidth = (int) (fulllength * batteryPercent / 100f);
            }
        }
    }

    public void setIsCharging(boolean charging) {
        isCharging = charging;
        if (isCharging) {
            sleepTime = 50;
            paintF.setColor(getResources().getColor(android.R.color.holo_green_light));
        } else {
            sleepTime = 200;
            setPercent(batteryPercent);
        }
    }

    public void setPercent(int percent) {
        batteryPercent = percent;
        if (batteryPercent > 100) {
            batteryPercent = 100;
        }

        if (batteryPercent < 0) {
            batteryPercent = 0;
        }

        if (batteryPercent >= 80 || isCharging) {
            paintF.setColor(getResources().getColor(android.R.color.holo_green_light));
        } else if (batteryPercent < 20) {
            paintF.setColor(getResources().getColor(android.R.color.holo_red_light));
        } else if (batteryPercent >= 20 && batteryPercent <= 40) {
            paintF.setColor(getResources().getColor(android.R.color.holo_orange_light));
        }

        if (!isCharging) {
            varyInnerWidth = (int) (fulllength * batteryPercent / 100f);
        }
    }


    @Override
    public void run() {
        while (isThreadRunning) {
            canvas = surfaceHolder.lockCanvas();
            if (canvas != null) {
                drawView(canvas);
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
            try {
                Thread.sleep(sleepTime); // 相当于帧频了，数值越小画面就越流畅
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
