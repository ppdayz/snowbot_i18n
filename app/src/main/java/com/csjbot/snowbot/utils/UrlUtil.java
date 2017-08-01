package com.csjbot.snowbot.utils;

import com.csjbot.snowbot.utils.OkHttp.RootUrl;
import com.csjbot.snowbot_rogue.utils.Constant;

/**
 * @Author: jl
 * @Date: 2016/12/19
 * @Desc:
 */

public final class UrlUtil {
    public static String OUTWAREHOUSE = RootUrl.API_PATH + "robot/outWarehouse"; //机器人出库
    public static String MASTERREGISTER = RootUrl.API_PATH + "robot/masterRegister"; //主人注册
    public static String UPDATEROBOT = RootUrl.API_PATH + "robot/updateRobot"; //小雪昵称
    public static String GETAUTHCODE = RootUrl.API_PATH + "robot/getAuthCode"; //获得验证码
    public static String REGISTERFACE = RootUrl.API_PATH + "face_recognition/addPerson"; //人脸注册
    public static String DETECTFACE = RootUrl.API_PATH + "face_recognition/FaceDetect"; //人脸识别
    public static String FACEIDENTIFY = RootUrl.API_PATH + "face_recognition/FaceIdentify"; //人脸检索
    public static String REGISTERALIYUN = RootUrl.API_PATH1 + "iot/device/reg"; //aliyun注册
    public static String ADDROBOTPARAM = RootUrl.API_PATH2 + "csj/api/cms/addOrUpdateRobotParam"; //新增机器人参数
    //    public static String ADDROBOTPARAM = RootUrl.API_PATH2 + "csj/api/cms/addRobotParam"; //新增机器人参数
    public static String UPDATEROBOTPARAM = RootUrl.API_PATH2 + "csj/api/cms/updateRobotParam"; //更新机器人参数
    public static String SLAMPARAM = "http://" + Constant.SLAM_DEFAULT_IP + "/service/system/admin/sn"; //更新机器人参数

    public static String GETADMIN = RootUrl.API_PATH + "api/sms/getAdmin"; //获得管理员账号

    //    public static String AUTOUPDATE = "http://192.168.1.168:8080/update.txt";//版本更新
    public static String AUTOUPDATE = RootUrl.API_PATH + "api/tms/versionRobot?category=snow&channel=standard";//版本更新


    public static final String ROUTER_IPADDR = "http://192.168.99.1";

}
