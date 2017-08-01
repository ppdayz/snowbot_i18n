package com.csjbot.snowbot.utils;

import com.android.core.entry.Static;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.bean.GralleryItem;
import com.csjbot.snowbot_rogue.utils.CSJToast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @Author: jl
 * @Date: 2016/12/16
 * @Desc:
 */

public class CommonTool {
    private static long lastClickTime = 0;//上次点击的时间

    private static int spaceTime = 1000;//时间间隔

    public static boolean isFastDoubleClick() {

        return isFastDoubleClick(spaceTime);
    }


    public static boolean isFastDoubleClick(long duringTime) {
        long currentTime = System.currentTimeMillis();//当前系统时间

        boolean isAllowClick;//是否允许点击

        if (currentTime - lastClickTime > duringTime) {

            isAllowClick = false;

        } else {
            CSJToast.showToast(Static.CONTEXT, Static.CONTEXT.getString(R.string.fast_click));
            isAllowClick = true;

        }

        lastClickTime = currentTime;

        return isAllowClick;
    }


    public static byte[] charToByte(char c) {
        byte[] b = new byte[2];
        b[0] = (byte) ((c & 0xFF00) >> 8);
        b[1] = (byte) (c & 0xFF);
        return b;
    }

    public static String getCurrentTime() {
        long currentTime = System.currentTimeMillis();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date date = new Date(currentTime);
        return formatter.format(date);
    }

    /**
     * "yyyy年MM月dd日 HH:mm:ss
     *
     * @return
     */
    public static String getCurrentTimeFormat1() {
        long currentTime = System.currentTimeMillis();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        Date date = new Date(currentTime);
        return formatter.format(date);
    }

    /**
     * "yyyy年MM月dd日 HH:mm:ss
     *
     * @return
     */
    public static String getCurrentTimeFormat1(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        Date date = new Date(time);
        return formatter.format(date);
    }

    public static String getSavaName() {
        long currentTime = System.currentTimeMillis();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date date = new Date(currentTime);
        return formatter.format(date);
    }

    /**
     * yyyy年MM月dd日 HH:mm:ss 转化为 yyyyMMddHH:mm:ss
     *
     * @return
     */

    public static long getSavaTime(String filename) {
        String[] sourceStrArray = filename.split("_");
        String temp = sourceStrArray[0] + sourceStrArray[1] + sourceStrArray[2] + sourceStrArray[3] + sourceStrArray[4] + sourceStrArray[5];
        return Long.parseLong(temp);

    }

    /**
     * 关机
     */
    public static void shutDown() {
        try {
            Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot -p"});
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * 重启
     */
    public static void rebootDevice() {
        try {
            Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot"});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获得相册的数据并排序
     *
     * @param mDatas
     * @return
     */
    public static List<GralleryItem> getPhotoList(List<GralleryItem> mDatas) {
        if (mDatas != null && mDatas.size() > 0) {
            for (int i = 0; i < mDatas.size() - 1; i++) {
                for (int j = 0; j < mDatas.size() - 1 - i; j++) {
                    if (StringFormat(mDatas.get(j).dateString) < StringFormat(mDatas.get(j + 1).dateString)) {
                        GralleryItem jItem = mDatas.get(j);
                        GralleryItem j1Item = mDatas.get(j + 1);
                        mDatas.set(j, j1Item);
                        mDatas.set(j + 1, jItem);
                    }


                }
            }
        }
        return mDatas;
    }

    /**
     * 2017-2-1 格式转换为201721
     *
     * @param tempStr
     * @return
     */
    public static Long StringFormat(String tempStr) {
        String[] sourceStrArray = tempStr.split("-");
        String temp = sourceStrArray[0] + sourceStrArray[1] + sourceStrArray[2];
        return Long.parseLong(temp);

    }

    public static boolean Ping() {
        if (true) {
            return true;
        }
        String result = null;
        try {
            String ip = "www.baidu.com";// 除非百度挂了，否则用这个应该没问题~
            Process p = Runtime.getRuntime().exec("ping -c 1 -w 100 " + ip);// ping1次
            // 读取ping的内容，可不加。
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            Csjlogger.info("TTT", "result content : " + stringBuffer.toString());
            // PING的状态
            int status = p.waitFor();
            if (status == 0) {
                result = "successful~";
                return true;
            } else {
                result = "failed~ cannot reach the IP address";
            }
        } catch (IOException e) {
            result = "failed~ IOException";
        } catch (InterruptedException e) {
            result = "failed~ InterruptedException";
        } finally {
            Csjlogger.info("TTT", "result = " + result);
        }
        return false;
    }


}
