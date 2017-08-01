package com.csjbot.snowbot.activity.face.util;

import java.util.ArrayList;
import java.util.List;

import dou.utils.DLog;
import mobile.ReadFace.YMFace;

/**
 * Created by mac on 16/7/4.
 */
public class RecoUtil {
    private static List<List<YMFace>> useList = new ArrayList<>();
    private static final int mxCount = 10;

    public static void addFaces(List<YMFace> faces) {
        if (useList.size() >= mxCount) useList.remove(0);
        if (faces == null || faces.size() == 0) {
            useList.add(new ArrayList<YMFace>());
        } else useList.add(faces);
    }


    private static List<Float> glassList = new ArrayList<>();

    public static boolean isGlass(float cur_score) {
        glassList.add(cur_score);
        if (glassList.size() > 10) glassList.remove(0);

        float sum = 0;
        for (float cur :
                glassList) {
            sum += cur;
        }

        float ava = sum / glassList.size();
        DLog.d("ava    =    " + ava);
        return ava >= 1;
    }

}
