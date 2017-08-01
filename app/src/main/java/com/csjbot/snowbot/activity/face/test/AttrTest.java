package com.csjbot.snowbot.activity.face.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import java.io.File;
import java.util.List;

import dou.utils.BitmapUtil;
import dou.utils.DLog;
import dou.utils.StringUtils;
import mobile.ReadFace.YMFace;
import mobile.ReadFace.YMFaceTrack;

/**
 * Created by mac on 16/8/2.
 */
public class AttrTest implements Test {


    public String test_path;
    public Context context;
    private YMFaceTrack faceTrack;
    private Paint paint;

    public AttrTest(Context context, String path) {
        this.test_path = path;
        this.context = context;
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(4);
        paint.setTextSize(20);

    }

    @Override
    public void initTest() {
        faceTrack = new YMFaceTrack();
        faceTrack.initTrack(context, YMFaceTrack.FACE_0, YMFaceTrack.RESIZE_WIDTH_640);
        File out = new File("/sdcard/img/output/");
        if (!out.exists()) out.mkdirs();
    }

    public void plistFile(File file) {
        File[] files = file.listFiles();

        for (int i = 0; i < files.length; i++) {
            File file1 = files[i];
            if (file1.isDirectory()) plistFile(file1);
            else {
                File curr = files[i];
                String currName = curr.getName();
                DLog.d("current name = " + currName + "  " + i + ":" + files.length);
                if (currName.contains(".jpg") || currName.contains(".png") || currName.contains(".jpeg")) {
                    Bitmap targetBitmap = BitmapUtil.decodeScaleImage(curr.getAbsolutePath(), 2000, 2000);
                    List<YMFace> ymFaces = faceTrack.detectMultiBitmap(targetBitmap);
                    if (ymFaces != null && ymFaces.size() != 0) {
                        DLog.d("detect img");
                        StringBuffer sb = new StringBuffer();
                        sb.append("age:" + faceTrack.getAge(0));
                        sb.append(" gender:" + faceTrack.getGender(0));
                        sb.append(" beauty:" + faceTrack.getFaceBeautyScore(0));

                        Bitmap current = Bitmap.createBitmap(targetBitmap.getWidth(), targetBitmap.getHeight(), Bitmap.Config.RGB_565);
                        Canvas canvas = new Canvas(current);
                        canvas.drawBitmap(targetBitmap, 0, 0, new Paint());
                        paint.setStyle(Paint.Style.FILL);
                        canvas.drawText(sb.toString(), 20, 60, paint);

                        float[] rect = ymFaces.get(0).getRect();
                        paint.setStyle(Paint.Style.STROKE);
                        RectF rectF = new RectF(rect[0], rect[1], rect[0] + rect[2], rect[1] + rect[3]);
                        canvas.drawRect(rectF, paint);

                        paint.setStrokeWidth(3);
                        float[] points = ymFaces.get(0).getLandmarks();
                        for (int j = 0; j < points.length / 2; j++) {
                            float x = points[j * 2];
                            float y = points[j * 2 + 1];
                            canvas.drawPoint(x, y, paint);
                        }

                        BitmapUtil.saveBitmap(current, "/sdcard/img/output/" + currName);
                    }

                }
            }
        }
    }

    @Override
    public void startTest() {
        if (!StringUtils.isEmpty(test_path)) {
            plistFile(new File(test_path));
        } else {
            DLog.d("test", "path is null");
        }

        DLog.d("*********end*********");
    }

    @Override
    public void finishTest() {
        faceTrack.onRelease();
    }
}
