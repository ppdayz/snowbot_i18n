package com.csjbot.snowbot.activity.face.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceView;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import com.android.core.entry.Static;
import com.bumptech.glide.Glide;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.face.base.BaseApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import dou.utils.DLog;
import dou.utils.DisplayUtil;
import dou.utils.StringUtils;
import mobile.ReadFace.YMFace;

import static android.content.ContentValues.TAG;

/**
 * Created by mac on 16/6/23.
 */
public class TrackUtil {

    private static final int count = 20;
    private static List<Integer> emo_list = new ArrayList<>();//储存每张面部表情的集合

    public static void addFace(YMFace face) {
        if (face == null) return;
        emo_list.add(getMaxFromArr(face.getEmotions()));
        if (emo_list.size() > count) {
            emo_list.remove(0);
        }
    }

    private static int getMaxFromArr(float arr[]) {
        int position = 0;
        float max = 0;
        for (int j = 0; j < arr.length; j++) {
            if (max <= arr[j]) {
                max = arr[j];
                position = j;
            }
        }
        return position;
    }


    public static boolean isSmile() {//微笑拍照
        if (emo_list.size() <= 18) return false;
        return countPosition(emo_list) == 0;
    }


    private static int countPosition(List<Integer> emo_list) {

        Map<Integer, Integer> map = new HashMap();
        for (int i = 0; i < emo_list.size(); i++) {
            int position = emo_list.get(i);
            Integer count = map.get(position);
            map.put(position, (count == null) ? 1 : count + 1);
        }

        int max = 0;
        int position = 0;

        Iterator<Integer> iter = map.keySet().iterator();

        while (iter.hasNext()) {
            int key = iter.next();
            int value = map.get(key);
            if (max <= value) {
                position = key;
                max = value;
            }
        }
        return position;
    }

    public static void cleanFace() {
        emo_list.clear();
    }

    public static void startCountDownAnimation(CountDownAnimation countDownAnimation) {
        // Customizable animation
        // Use a set of animations
        Animation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f,
                0.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        Animation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
        countDownAnimation.setAnimation(animationSet);
        countDownAnimation.start();
    }

//    static int colorffList[] = {
//            0xffffd700,
//            0xff7cfc00,
//            0xff00ff00,
//            0xffffa500,
//            0xffff4500,
//            0xffff0000,
//            0xffffff00
//    };
//    static int colorList[] = {
//            0x44ffd700,
//            0x447cfc00,
//            0x4400ff00,
//            0x44ffa500,
//            0x44ff4500,
//            0x44ff0000,
//            0x44ffff00
//    };

    static int colorffList[] = {
            0xffffffff
    };
    static int colorList[] = {
            0x44ffffff
    };


    public static void drawAnim(List<YMFace> faces, SurfaceView outputView, float scale_bit, int cameraId) {
        drawAnim(faces, outputView, scale_bit, cameraId, null, false);
    }

    public static void drawAnim(YMFace face, SurfaceView outputView, float scale_bit, int cameraId) {
        List<YMFace> faces = new ArrayList<>();
        faces.add(face);
        drawAnim(faces, outputView, scale_bit, cameraId, null, false);
    }

    static String happystr = "";
    private static final int OFFSET_Y = 160;

    public static void drawAnim(List<YMFace> faces, SurfaceView outputView, float scale_bit, int cameraId, String fps, boolean showPoint) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Canvas canvas = outputView.getHolder().lockCanvas();

        if (canvas == null) return;
        try {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            int viewW = outputView.getLayoutParams().width;
            int viewH = outputView.getLayoutParams().height;
            if (faces == null || faces.size() == 0) return;
            for (int i = 0; i < faces.size(); i++) {
                int size = DisplayUtil.dip2px(BaseApplication.getAppContext(), 3);
                paint.setStrokeWidth(size);
                paint.setStyle(Paint.Style.STROKE);
                YMFace ymFace = faces.get(i);

                float[] rect = ymFace.getRect();
                float x1 = viewW - rect[0] * scale_bit - rect[2] * scale_bit;
                if (cameraId == (BaseApplication.yu ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK))
                    x1 = rect[0] * scale_bit;
//                    float y1 =(rect[1] * scale_bit - 360 * (1 - rect[1] * 2 / 1440));
                // TODO: 2017/5/24 0024 wql修改
                float y1 = (float) (rect[1] * (scale_bit-0.8) - 360 * ( rect[1] * 2 / 1440));
//                float y1 = rect[1] * scale_bit;
                Log.e(TAG, "y2: ------------"+y1);
//                Csjlogger.info("rect[1] {} ,  {}", rect[1], 360 * (1 - rect[1] * 2 / 1440));

                float rect_width = rect[2] * scale_bit;
                paint.setColor(colorList[ymFace.getTrackId() % colorList.length]);
                //draw rect
                RectF rectf = new RectF(x1, y1, x1 + rect_width, y1 + rect_width);
                canvas.drawRect(rectf, paint);

                //draw grid

                int line = 10;
                int per_line = (int) (rect_width / (line + 1));
                int smailSize = DisplayUtil.dip2px(Static.CONTEXT, 1.5f);
                paint.setStrokeWidth(smailSize);
                for (int j = 1; j < line + 1; j++) {
                    canvas.drawLine(x1 + per_line * j, y1, x1 + per_line * j, y1 + rect_width, paint);
                    canvas.drawLine(x1, y1 + per_line * j, x1 + rect_width, y1 + per_line * j, paint);
                }


                paint.setColor(colorffList[ymFace.getTrackId() % colorffList.length]);
                //注意前置后置摄像头问题
                float length = rect[3] * scale_bit / 5;
                float width = rect[3] * scale_bit;
                float heng = size / 2;
                canvas.drawLine(x1 - heng, y1, x1 + length, y1, paint);
                canvas.drawLine(x1, y1 - heng, x1, y1 + length, paint);

                x1 = x1 + width;
                canvas.drawLine(x1 + heng, y1, x1 - length, y1, paint);
                canvas.drawLine(x1, y1 - heng, x1, y1 + length, paint);

                y1 = y1 + width;
                canvas.drawLine(x1 + heng, y1, x1 - length, y1, paint);
                canvas.drawLine(x1, y1 + heng, x1, y1 - length, paint);

                x1 = x1 - width;
                canvas.drawLine(x1 - heng, y1, x1 + length, y1, paint);
                canvas.drawLine(x1, y1 + heng, x1, y1 - length, paint);
                if (showPoint) {
                    paint.setColor(Color.rgb(57, 138, 243));
                    size = DisplayUtil.dip2px(BaseApplication.getAppContext(), 2.5f);
                    paint.setStrokeWidth(size);
                    float[] points = ymFace.getLandmarks();
                    for (int j = 0; j < points.length / 2; j++) {
                        float x = viewW - points[j * 2] * scale_bit;
                        if (cameraId == (BaseApplication.yu ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK))
                            x = points[j * 2] * scale_bit;
                        float y = points[j * 2 + 1] * scale_bit;
                        canvas.drawPoint(x, y, paint);
                    }
                }
//                float[] headposes = ymFace.getHeadpose();
//                DLog.d(headposes[0]+" : "+headposes[1]+" : "+headposes[2]);

                if (ymFace.getAge() > 0) {
                    x1 = viewW - rect[0] * scale_bit - rect[2] * scale_bit;
                    if (cameraId == (BaseApplication.yu ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK))
                        x1 = rect[0] * scale_bit;
                   // y1 = rect[1] * scale_bit;
//
                    y1 = (float) (rect[1] * (scale_bit-0.8) - 360 * ( rect[1] * 2 / 1440));
                    StringBuffer sb = new StringBuffer();

//                    sb.append("personId :  "+ymFace.getPersonId() + " ");
                    sb.append(ymFace.getGender() == 1 ? "M" : "");
                    sb.append(ymFace.getGender() == 0 ? "F" : "");
                    sb.append("/");
                    sb.append(ymFace.getAge());
//                    sb.append(" /" + (int) rect[2]);
//                    sb.append(ymFace.getGlassScore()>0.5 ? "眼镜 " : "");
//                    int happy = (int) (ymFace.getEmotions()[0] * 100);
//                    if (happy >= 70) happystr = "开心";
//                    if (happy <= 20) happystr = "";
//
//                    sb.append(happystr);

//                    sb.append(ymFace.getGender() + "");
//                    if (ymFace.getBeautyScore() != 0)
//                        sb.append(ymFace.getBeautyScore());
                    paint.setColor(colorffList[ymFace.getTrackId() % colorffList.length]);
                    paint.setStrokeWidth(0);
                    paint.setStyle(Paint.Style.FILL);
                    int fontSize = DisplayUtil.dip2px(BaseApplication.getAppContext(), 20);
                    paint.setTextSize(fontSize);
                    Bitmap bitmap;
                    if (StringUtils.isEmpty(happystr)) {
                        bitmap = BitmapFactory.decodeResource(Static.CONTEXT.getResources(), R.mipmap.show_cry);
                    } else {
                        bitmap = BitmapFactory.decodeResource(Static.CONTEXT.getResources(), R.mipmap.show_smile);
                    }
                    Rect rect_text = new Rect();
                    paint.getTextBounds(sb.toString(), 0, sb.toString().length(), rect_text);
                    canvas.drawText(sb.toString()/*+" id:"+ymFace.getTrackId()*/, x1, y1 - (bitmap.getHeight() / 2 - rect_text.height() / 2), paint);

//                    canvas.drawBitmap(bitmap, x1 + rect_text.width() + 5, y1 - bitmap.getHeight(), null);
                }
            }

            if (!StringUtils.isEmpty(fps)) {
                paint.setColor(Color.RED);
                paint.setStrokeWidth(0);
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.FILL);

                int sizet = DisplayUtil.sp2px(BaseApplication.getAppContext(), BaseApplication.yu ? 28 : 17);
                paint.setTextSize(sizet);
                canvas.drawText(fps, 20, viewH * 3 / 17, paint);
                DLog.d(fps);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            outputView.getHolder().unlockCanvasAndPost(canvas);
        }
    }


    public static int computingAge(int age) {
        int ran = new Random().nextInt(3) + 3;
        age = (age / ran) * ran;
        return age;
    }

    public static void displayView(Context context, ImageView v, String path) {
        Glide.with(context).load(new File(path)).into(v);
    }

    private static List<Float> limitArr = new ArrayList<>();

    private static int preX, preY;

    public static boolean isTouchable(int limiX, int limiY) {

        int mutix = preX - limiX;
        int mutiy = preY - limiY;
        limitArr.add((float) Math.sqrt(mutix * mutix + mutiy * mutiy));

        preX = limiX;
        preY = limiY;
        if (limitArr.size() > 5) {
            List<Float> temp = new ArrayList<>();
            temp.addAll(limitArr);
            Collections.sort(temp);
            int limit = (int) Math.abs(temp.get(temp.size() - 2) + temp.get(temp.size() - 1));
            temp.clear();
            limitArr.remove(0);
            if (limit < 40) return true;
        }
        return false;
    }

}
