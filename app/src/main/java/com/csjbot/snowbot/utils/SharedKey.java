package com.csjbot.snowbot.utils;

/**
 * @Author: jl
 * @Date: 2016/12/16
 * @Desc:
 */

public class SharedKey {
    /**
     * 注册信息
     */
    public static final String DEVICEUUID = "DEVICEUUID";//DeviceUUID
    public static final String ROBOTREGISTERSTATUS = "ROBOTREGISTERSTATUS";//robot 出库 注册完成状态
    public static final String MASTERNUM = "MASTERNUM";//主人手机号码
    public static final String PASSWORD = "PASSWORD";//密码
    public static final String VERIFYNUM = "VERIFYNUM";//验证码
    public static final String MASTERNICKNAME = "MASTERNICKNAME";//主人昵称
    public static final String ROBOTNICKNAME = "ROBOTNICKNAME";//小雪人昵称
    public static final String USERID = "USERID";//客户端的id
    public static final String USERTYPE = "USERTYPE";//小客户端的类型
    public static final String PRODUCTKEY = "PRODUCTKEY";//productKey  阿里云注册返回值
    public static final String DEVICEKEY = "DEVICEKEY";//deviceKey 阿里云注册返回值

    public static final String SKIP_REGISTRATION = "SKIP_REGISTRATION";//跳过注册
    public static final String LANGUAGE ="LANGUAGE" ;

    /**
     * 与后台、阿里云的连接地址
     */
    public static String OUTWAREHOUSE = "OUTWAREHOUSE"; //机器人出库
    public static String MASTERREGISTER = "MASTERREGISTER"; //主人注册
    public static String UPDATEROBOT = "UPDATEROBOT"; //小雪初始化
    public static String GETAUTHCODE = "GETAUTHCODE"; //获得验证码
    public static String REGISTERFACE = "REGISTERFACE"; //人脸注册
    public static String DETECTFACE = "DETECTFACE"; //人脸识别
    public static String FACEIDENTIFY = "FACEIDENTIFY"; //人脸检索
    public static String REGISTERALIYUN = "REGISTERALIYUN"; //aliyun注册
    public static String ADDROBOTPARAM = "ADDROBOTPARAM"; //新增机器人参数
    public static String UPDATEROBOTPARAM = "UPDATEROBOTPARAM"; //更新机器人参数

    public static String GETADMIN = "GETADMIN"; //获得管理员账号

    public static final String PERCENT = "PERCENT";//电量
    public static final String ISCHARGING = "ISCHARGING";//电量
    public static final String PM25 = "PM25";//传感数据
    public static final String HUMIDITY = "HUMIDITY";//传感数据
    public static final String TEMPERATURE = "TEMPERATURE";//传感数据
    public static final String CUSTOMVOICE = "CUSTOMVOICE";//自定义语音答案
    public static final String USBPATH = "USBPATH";//U盘路径
    public static final String CONNECTEDSSID = "CONNECTEDSSID";//已连接的SSID
    public static final String ADVERTISEMENT = "ADVERTISEMENT";//自定义广告语
    public static final String REPEATTIME = "REPEATTIME";//广告语重复次数
    public static final String FACERECOGNITION = "FACERECOGNITION";//人脸识别是否开启
    public static final String LATITUDE = "LATITUDE";//机器人纬度
    public static final String LONTITUDE = "LONTITUDE";//机器人经度
    public static final String AIUISERVICESWITCH = "AIUISERVICESWITCH";//aiui switch
    public static final String AIUICONTENT = "AIUICONTENT";//aiui返回结果
    public static final String TTSSWITCH = "TTSSWITCH";//TTS switch
    public static final String TTSCONTENT = "TTSCONTENT";//TTS content
    public static final String AIUIDATA = "AIUIDATA";
    public static final String WEATHERSWITCH = "WEATHERSWITCH";//触摸头部播报天气预报是否开启
    public static final String NETWORKSTATUS = "NETWORKSTATUS";//网络状态，false 无网络  true网络正常
    public static final String AIUISERVICETYPE = "AIUISERVICETYPE";//aiui具体service
    public static final String URL = "URL";//媒体文件url
    public static final String TITLE = "TITLE";//媒体title
    public static final String LOOP = "LOOP";//是否循环播放
    public static final String HOMEDATAS = "HOMEDATAS";//添加房间名称

    /*机器人基本参数*/
    public static final String SN = "SN";//机器人唯一识别码
    public static final String UPCOMPUTER = "UPCOMPUTER";//上位机识别码
    public static final String UPPLATE = "UPPLATE";//上身板识别码
    public static final String DOWNPLATE = "DOWNPLATE";//下身板识别码
    public static final String NAVIGATION = "NAVIGATION";//导航识别码
    public static final String RADAR = "RADAR";//雷达识别码
    public static final String AIUI = "AIUI";//aiui识别码
    public static final String DLP = "DLP";//dlp识别码
    public static final String WALKDRIVENLEFT = "WALKDRIVENLEFT";//行动电机左识别码
    public static final String WALKDRIVENRIGHT = "WALKDRIVENRIGHT";//行动电机右识别码

}
