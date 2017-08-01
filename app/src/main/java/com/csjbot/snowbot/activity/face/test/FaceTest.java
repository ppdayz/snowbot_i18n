package com.csjbot.snowbot.activity.face.test;

import android.content.Context;
import android.graphics.Bitmap;

import com.csjbot.snowbot.activity.face.model.User;
import com.csjbot.snowbot.activity.face.util.DataSource;

import java.io.File;
import java.util.List;

import dou.utils.BitmapUtil;
import dou.utils.DLog;
import mobile.ReadFace.YMFace;
import mobile.ReadFace.YMFaceTrack;

/**
 * Created by mac on 16/7/28.
 */
public class FaceTest implements Test {


    @Override
    public void initTest() {
        faceTrack = new YMFaceTrack();
        faceTrack.initTrack(context, YMFaceTrack.FACE_0, YMFaceTrack.RESIZE_WIDTH_1920);
        faceTrack.setRecognitionConfidence(75);
        faceTrack.resetAlbum();
    }

    public String test_path = "/sdcard/img/fr";
    public Context context;
    private YMFaceTrack faceTrack;

    public FaceTest(Context context) {
        this.context = context;
    }

    @Override
    public void startTest() {

        File test_file = new File(test_path);
        if (!test_file.exists()) {
            DLog.d("model path is missing");
            return;
        }
        DLog.d("start face test");
        File[] fileNames = test_file.listFiles();
        for (int i = 0; i < fileNames.length; i++) {
            //数个文件夹
            File dic = fileNames[i];
            if (dic.isDirectory()) {
                File[] images = dic.listFiles();
                boolean isAdd = false;
                int personId = -1;
                String name = "";
                for (int j = 0; j < images.length; j++) {
                    File image = images[j];
                    name = image.getName();
                    if (image.getName().contains(".jpg") || image.getName().contains(".png") || image.getName().contains(".jpeg")) {
                        if (j == 0 || !isAdd) {
                            personId = addPerson(image);
                            if (personId > 0) {
                                User user = new User("" + personId, dic.getName(), "", "");
                                user.setScore(" ");
                                DataSource dataSource = new DataSource(context);
                                dataSource.insert(user);
                                isAdd = true;
                            }
                        } else {
                            updatePerson(image, personId);
                        }
                    }
                }

                DLog.d(name + " personId: " + personId +
                        " faceCountbyId: " + faceTrack.getFaceCountByPersonId(personId) +
                        "  all_size : " + faceTrack.getAlbumSize());
            }
        }

        //test 图片相似度
        Bitmap bitmap = BitmapUtil.decodeScaleImage("/sdcard/img/img_zhuyuan.jpg", 1920, 1920);
        List<YMFace> ymFaces = faceTrack.detectMultiBitmap(bitmap);
        for (int i = 0; i < ymFaces.size(); i++) {
            long time = System.currentTimeMillis();
            int identifyPerson = faceTrack.identifyPerson(i);
            int confidence = faceTrack.getRecognitionConfidence();
            DLog.d("identify end " + identifyPerson + " time :" + (System.currentTimeMillis() - time) + " con = " + confidence);
        }
    }

    private void updatePerson(File image, int personId) {

        if (detectStart(image)) {
            int updateResult = faceTrack.updatePerson(personId, 0);
            DLog.d(image.getName() + " update : " + updateResult + " con ：" + faceTrack.getRecognitionConfidence());
        }
    }

    private int addPerson(File image) {
        if (detectStart(image)) {
            int personId = faceTrack.addPerson(0);
            DLog.d(image.getName() + " add : " + personId + " con ：" + faceTrack.getRecognitionConfidence());
            return personId;
        }
        return -1;
    }

    private boolean detectStart(File image) {
        Bitmap bitmap = BitmapUtil.getSmallBitmap(image.getAbsolutePath(), 1920, 1920);
        List<YMFace> ymFaces = faceTrack.detectMultiBitmap(bitmap);
//        int score = netFaceTrack.getFaceBeautyScore(0);
//        DLog.d("face score = "+ score);
        if (ymFaces != null && ymFaces.size() != 0) {
            return true;
        }
        return false;
    }

    @Override
    public void finishTest() {

    }
}
