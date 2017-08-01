package com.csjbot.snowbot.activity.face.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.support.v4.util.SimpleArrayMap;

import com.android.core.entry.Static;
import com.csjbot.snowbot.activity.face.util.DrawUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import dou.utils.BitmapUtil;
import dou.utils.DLog;
import dou.utils.DisplayUtil;
import mobile.ReadFace.YMFace;
import mobile.ReadFace.YMFaceTrack;

/**
 * Created by icount on 2016/11/11.
 *
 */

public class ReadFromMp4 implements Test {

    public String test_path;
    public Context context;
    private YMFaceTrack faceTrack;
    private Paint paint;

    public ReadFromMp4(Context context, String path) {
        this.test_path = path;
        this.context = context;
        paint = new Paint();
        paint.setColor(Color.rgb(57, 138, 243));
        paint.setStrokeWidth(4);
        paint.setTextSize(10);
        initTest();
        startTest();
    }

    @Override
    public void initTest() {
        faceTrack = new YMFaceTrack();
        faceTrack.initTrack(context, YMFaceTrack.FACE_0, YMFaceTrack.RESIZE_WIDTH_1920);
        faceTrack.setRecognitionConfidence(75);
        faceTrack.resetAlbum();

    }

    @Override
    public void startTest() {

        trackingMap = new SimpleArrayMap<>();
        DrawUtil.updateDataSource();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = test_path;
                File file = new File(test_path);
                if (file.exists()) {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(path);
                    int time = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                    DLog.d("Time:" + time);
                    int count_time = 1;
                    int count_frame = 0;
                    while (count_time < time) {
                        count_time += 200;
                        count_frame++;
                        Bitmap bitmap = retriever.getFrameAtTime(count_time * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

                        String pathPic = Environment.getExternalStorageDirectory() + "/img/out/" + count_frame + ".jpg";

                        FileOutputStream fos;
                        try {
                            fos = new FileOutputStream(pathPic);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                            DLog.d("out" + count_frame + "：" + count_time);
                            fos.close();

                            Bitmap getBitmap = BitmapUtil.getSmallBitmap(pathPic, 1000, 1000);
//                            getBitmap = drawInfo(getBitmap);
                            new File(pathPic).delete();
                            BitmapUtil.saveBitmap(getBitmap, pathPic);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    DLog.d("文件不存在");
                }

            }
        }).start();
    }

    @Override
    public void finishTest() {

    }

    private SimpleArrayMap<Integer, YMFace> trackingMap;

    private Bitmap drawInfo(Bitmap bitmap) {
        final Bitmap current = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final List<YMFace> faces = faceTrack.trackMulti(current);

        if (faces != null && faces.size() > 0) {
            for (int i = 0; i < faces.size(); i++) {
                final YMFace ymFace = faces.get(i);
                final int trackId = ymFace.getTrackId();
                if (!trackingMap.containsKey(trackId)) {
                    DLog.d("identify start");
                    int identifyPerson = faceTrack.identifyPerson(i);
                    int confidence = faceTrack.getRecognitionConfidence();
                    DLog.d("identify end " + identifyPerson + "  confidence = " + confidence);
                    if (identifyPerson > 0) {
                        int size = trackingMap.size();
                        for (int j = 0; j < size; j++) {
                            YMFace face = trackingMap.get(trackingMap.keyAt(j));
                            if (face.getPersonId() == identifyPerson)
                                trackingMap.removeAt(j);
                        }
                        ymFace.setIdentifiedPerson(identifyPerson, confidence);
                        trackingMap.put(trackId, ymFace);
                    }
                }
            }

            for (int i = 0; i < faces.size(); i++) {
                final YMFace ymFace = faces.get(i);
                final int trackId = ymFace.getTrackId();
                if (trackingMap.containsKey(trackId)) {
                    YMFace face = trackingMap.get(trackId);
                    ymFace.setIdentifiedPerson(face.getPersonId(), face.getConfidence());
                }
            }

            //draw bitmap

            Canvas canvas = new Canvas(current);
            canvas.drawBitmap(bitmap, 0, 0, new Paint());
            for (int i = 0; i < faces.size(); i++) {
                final YMFace ymFace = faces.get(i);
                float[] rect = ymFace.getRect();
                //draw rect
                paint.setColor(0x44ffffff);
                int size = DisplayUtil.dip2px(Static.CONTEXT, 3);

                paint.setStrokeWidth(size);
                paint.setStyle(Paint.Style.STROKE);
                int x1 = (int) rect[0];
                int y1 = (int) rect[1];
                RectF rectf = new RectF(rect[0], rect[1], rect[2], rect[3]);
                canvas.drawRect(rectf, paint);

                int personId = ymFace.getPersonId();
                int confidence = ymFace.getConfidence();
                StringBuilder sb = new StringBuilder();
                if (personId > 0) {
                    paint.setColor(Color.WHITE);
                    paint.setStrokeWidth(0);
                    paint.setStyle(Paint.Style.FILL);
                    int fontSize = DisplayUtil.dip2px(Static.CONTEXT, 20);
                    paint.setTextSize(fontSize);

                    sb.append(DrawUtil.getNameFromPersonId(personId));

                }
                canvas.drawText(sb.toString() + "  " + confidence, x1, y1 - 30, paint);
            }
        }

        return current;
    }
}
