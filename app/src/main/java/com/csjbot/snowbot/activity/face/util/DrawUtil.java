package com.csjbot.snowbot.activity.face.util;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import com.android.core.entry.Static;
import com.csjbot.snowbot.activity.face.base.BaseApplication;
import com.csjbot.snowbot.activity.face.model.User;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dou.utils.DLog;
import dou.utils.DisplayUtil;
import dou.utils.StringUtils;
import mobile.ReadFace.YMFace;


/**
 * Created by mac on 16/8/15.
 */
public class DrawUtil {
    public interface FindRegisteredUserInterface {
        void RegisteredUserFind(User user);
    }

    private static Map<Integer, User> userMap = new HashMap<>();
    private static List<User> userList = new ArrayList<>();
    private static FindRegisteredUserInterface findRegisteredUserInterface;

    public static void setFindRegisteredUserInterface(FindRegisteredUserInterface userInterface) {
        findRegisteredUserInterface = userInterface;
    }

    public static User getUserById(String id) {
        DataSource dataSource = new DataSource(Static.CONTEXT);
        return dataSource.getUserByPersonId(id);
    }

    public static void clearDb() {
        DataSource dataSource = new DataSource(Static.CONTEXT);
        List<User> userList = dataSource.getAllUser();
        for (int i = 0; i < userList.size(); i++) {
            String imgPath = BaseApplication.getAppContext().getCacheDir()
                    + "/" + userList.get(i).getPersonId() + ".jpg";
            File imgFile = new File(imgPath);
            if (imgFile.exists()) {
                imgFile.delete();
            }
        }
        userMap.clear();
        dataSource.clearTable();
    }

    public static List<User> updateDataSource() {

        long time = System.currentTimeMillis();
        DataSource dataSource = new DataSource(Static.CONTEXT);
        userMap.clear();
        userList.clear();
        userList = dataSource.getAllUser();
        for (int i = 0; i < userList.size(); i++) {
            String imgPath = BaseApplication.getAppContext().getCacheDir()
                    + "/" + userList.get(i).getPersonId() + ".jpg";
            File imgFile = new File(imgPath);
            if (imgFile.exists()) {
                userList.get(i).setHead(imgPath);
            }
            userMap.put(Integer.valueOf(userList.get(i).getPersonId()), userList.get(i));
        }
        DLog.d(" update sql cost: " + (System.currentTimeMillis() - time));
        return userList;
    }

    public static String getNameFromPersonId(int personId) {
        if (personId > 0 && userMap.containsKey(personId)) {

            User user = userMap.get(personId);
            return user.getName();
        }
        return "";
    }

    public static void drawAnim(Object obj, View outputView, float scale_bit, int cameraId) {
        if (obj instanceof List) {
            List<YMFace> faces = (List<YMFace>) obj;
            drawAnim(faces, outputView, scale_bit, cameraId, null, false);
        } else {
            List<YMFace> faces = new ArrayList<>();
            faces.add((YMFace) obj);
            drawAnim(faces, outputView, scale_bit, cameraId, null, false);
        }
    }

    public static void drawAnim(YMFace face, View outputView, float scale_bit, int cameraId) {
        List<YMFace> faces = new ArrayList<>();
        faces.add(face);
        drawAnim(faces, outputView, scale_bit, cameraId, null, false);
    }

    // A83T
//    private static final int OFFSET_Y = 75;

    // 1080p
    private static final int OFFSET_Y = 160;

    public static void drawAnim(List<YMFace> faces, View outputView, float scale_bit,
                                int cameraId, String fps, boolean showPoint) {
        float ty = 0;
        Paint paint = new Paint();
        Canvas canvas = ((SurfaceView) outputView).getHolder().lockCanvas();
        if (canvas != null) {
            try {
                int viewH = outputView.getHeight();
                int viewW = outputView.getWidth();
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                if (faces == null || faces.size() == 0) return;
                for (int i = 0; i < faces.size(); i++) {

                    paint.setColor(0x44ffffff);
                    int size = DisplayUtil.dip2px(Static.CONTEXT, 3);

                    paint.setStrokeWidth(size);
                    paint.setStyle(Paint.Style.STROKE);

                    YMFace ymFace = faces.get(i);

                    float[] rect = ymFace.getRect();
                    float x1 = viewW - rect[0] * scale_bit - rect[2] * scale_bit;
                    if (cameraId == (BaseApplication.yu ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK))
                        x1 = rect[0] * scale_bit;

                    // FIXME puyz 修改，这里因为改了dou.helper.CameraHelper.setOptimalPreviewSize() 里面的方法，
                    // 强行全屏了，所以需要把绘制 人脸框的 Y 的坐标向上提50px，下面的Y2 也是一样
                      float y1 = (float) (rect[1] * (scale_bit-0.8));
//                    float y1 = (float) (rect[1] * (scale_bit - 0.8) - 360 * (rect[1] * 2 / 1440));
                    ty = y1;
//                    Log.e("FaceReo", "y1: " + y1);
                    float rect_width = rect[2] * scale_bit;
                    //draw rect
                    RectF rectf = new RectF(x1, y1, x1 + rect_width, y1 + rect_width);
                    canvas.drawRect(rectf, paint);
//                    DLog.d(rect[0] + " : " + rect[1] + " : " + rect[2] + ": " + rect[3]);
                    //draw grid
                    int line = 10;
                    int per_line = (int) (rect_width / (line + 1));
                    int smailSize = DisplayUtil.dip2px(Static.CONTEXT, 1.5f);
                    paint.setStrokeWidth(smailSize);
//                    for (int j = 1; j < line + 1; j++) {
//                        canvas.drawLine(x1 + per_line * j, y1, x1 + per_line * j, y1 + rect_width, paint);
//                        canvas.drawLine(x1, y1 + per_line * j, x1 + rect_width, y1 + per_line * j, paint);
//                    }

                    paint.setStrokeWidth(size);
                    paint.setColor(Color.RED);
//                    注意前置后置摄像头问题
                    float x2 = viewW - rect[0] * scale_bit - rect[2] * scale_bit;
                    if (cameraId == (BaseApplication.yu ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK))
                        x2 = rect[0] * scale_bit;

                    float y2 = (float) (rect[1] * (scale_bit-0.8));
                    // FIXME: 2017/5/5 puyz
//                    float y2 = rect[1] * scale_bit - 360 * (1 - rect[1] * 2 / 1440);
//                    float y2 = (float) (rect[1] * (scale_bit - 0.8) - 360 * (rect[1] * 2 / 1440));

                    float length = rect[3] * scale_bit / 5;
                    float width = rect[3] * scale_bit;
                    float heng = size / 2;
                    canvas.drawLine(x2 - heng, y2, x2 + length, y2, paint);
                    canvas.drawLine(x2, y2 - heng, x2, y2 + length, paint);

                    x2 = x2 + width;
                    canvas.drawLine(x2 + heng, y2, x2 - length, y2, paint);
                    canvas.drawLine(x2, y2 - heng, x2, y2 + length, paint);

                    y2 = y2 + width;
                    canvas.drawLine(x2 + heng, y2, x2 - length, y2, paint);
                    canvas.drawLine(x2, y2 + heng, x2, y2 - length, paint);

                    x2 = x2 - width;
                    canvas.drawLine(x2 - heng, y2, x2 + length, y2, paint);
                    canvas.drawLine(x2, y2 + heng, x2, y2 - length, paint);


                    int personId = ymFace.getPersonId();
                    StringBuilder sb = new StringBuilder();
//                    sb.append("trackId : " + ymFace.getTrackId() + " ");

                    if (personId > 0 && userMap.containsKey(personId)) {

                        User user = userMap.get(personId);
                        String name = user.getName();
                        String gender = user.getGender();
                        String age = user.getAge();
                        String score = user.getScore();

                        if (findRegisteredUserInterface != null) {
                            findRegisteredUserInterface.RegisteredUserFind(user);
                        }

                        if (StringUtils.isEmpty(name)) {
                            sb.append("id=").append(personId).append("   ");
                        } else {
                            if (isChinese(name)) {
                                if (name.length() > 4) {
                                    name = name.substring(0, 4) + "…";
                                }
                            } else {
                                if (name.length() > 10) {
                                    name = name.substring(0, 10) + "…";
                                }
                            }
                            sb.append(name).append("  ");
                        }

                        if (!StringUtils.isEmpty(gender))
                            sb.append(gender).append("/");
                        if (!StringUtils.isEmpty(age))
                            sb.append(age);
//                        sb.append(" con = " + ymFace.getConfidence());
                    } else if (ymFace.getAge() > 0) {
                        int gender = ymFace.getGender();
                        sb.append(gender >= 0 ? gender == 1 ? " M/" : " F/" : gender + "/ ");
                        sb.append(ymFace.getAge());
//                        sb.append(" /" + ymFace.getBeautyScore());
                    }

                    paint.setColor(Color.RED);
                    paint.setStrokeWidth(0);
                    paint.setStyle(Paint.Style.FILL);
                    int fontSize = DisplayUtil.dip2px(Static.CONTEXT, 20);
                    paint.setTextSize(fontSize);
                    canvas.drawText(sb.toString(), x1, y1 - 30, paint);
                }

                if (!StringUtils.isEmpty(fps)) {
                    paint.setColor(Color.RED);
                    paint.setStrokeWidth(0);
                    paint.setAntiAlias(true);
                    paint.setStyle(Paint.Style.FILL);

                    int sizet = DisplayUtil.sp2px(BaseApplication.getAppContext(), BaseApplication.yu ? 28 : 17);
                    paint.setTextSize(sizet);

                    canvas.drawText(fps, 20, viewH * 3 / 17, paint);
                }

                /**
                 * 测试代码
                 */
//                paint.setColor(Color.RED);
//                paint.setStrokeWidth(0);
//                paint.setAntiAlias(true);
//                paint.setStyle(Paint.Style.FILL);

//                int sizet = DisplayUtil.sp2px(BaseApplication.getAppContext(), BaseApplication.yu ? 28 : 17);
//                paint.setTextSize(sizet);
//
//                canvas.drawText("y1 = "+ty, 1000, viewH * 3 / 17, paint);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                ((SurfaceView) outputView).getHolder().unlockCanvasAndPost(canvas);
            }
        }
    }


    /*判断是否有中文*/
    private static final boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    public static final boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }


}
