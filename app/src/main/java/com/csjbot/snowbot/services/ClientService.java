package com.csjbot.snowbot.services;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.WindowManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.android.core.entry.Static;
import com.android.core.util.SharedUtil;
import com.android.core.util.StrUtil;
import com.csjbot.csjbase.base.CsjBaseService;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.VideoRecordActivity;
import com.csjbot.snowbot.activity.VidyoSampleActivity;
import com.csjbot.snowbot.bean.CommandBean;
import com.csjbot.snowbot.bean.CommandDataBean;
import com.csjbot.snowbot.bean.ContentBean;
import com.csjbot.snowbot.bean.CurrentState;
import com.csjbot.snowbot.bean.DataContent;
import com.csjbot.snowbot.bean.FromBean;
import com.csjbot.snowbot.bean.Home;
import com.csjbot.snowbot.bean.Location;
import com.csjbot.snowbot.bean.LoginContentBean;
import com.csjbot.snowbot.bean.OnLineContent;
import com.csjbot.snowbot.bean.Power;
import com.csjbot.snowbot.bean.Sensor;
import com.csjbot.snowbot.bean.ToBean;
import com.csjbot.snowbot.bean.UpdateDeviceDataBean;
import com.csjbot.snowbot_rogue.app.CsjSpeechSynthesizer;
import com.csjbot.snowbot.client.NettyClientNew;
import com.csjbot.snowbot.client.nettyHandler.ClientListener;
import com.csjbot.snowbot.client.serverHandler.MessageRecInterface;
import com.csjbot.snowbot.client.serverHandler.MessageServerHandler;
import com.csjbot.snowbot.utils.CommonTool;
import com.csjbot.snowbot.utils.Constants;
import com.csjbot.snowbot.utils.PowerStatus;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot.utils.TimeUtil;
import com.csjbot.snowbot_rogue.bean.MapSize;
import com.csjbot.snowbot_rogue.platform.SnowBotManager;
import com.csjbot.snowbot_rogue.servers.serials.SnowBotSerialServer;
import com.csjbot.snowbot_rogue.servers.slams.SnowBotMoveServer;
import com.csjbot.snowbot_rogue.servers.slams.events.RobotStatusUpdateEvent;
import com.csjbot.snowbot_rogue.utils.CSJToast;
import com.slamtec.slamware.action.MoveDirection;
import com.slamtec.slamware.robot.Pose;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ClientService extends CsjBaseService implements ClientListener, MessageRecInterface {
    @Override
    public boolean useEventBus() {
        return true;
    }

    NettyClientNew clientNew = NettyClientNew.getInstence();
    //        NettyServer server = NettyServer.getOurInstance();
    Handler mHeartHandler = new Handler();
    Handler powerHandler = new Handler();

    private int powerUpdateTime = 1000 * 3;//3秒更新一次sensor数据
    private int powerLowDialogTime = 1000 * 60 * 10;//10分钟提示一次低电量

    private CommandBean sendData = new CommandBean();
    private CommandDataBean<DataContent> sendCommonData = new CommandDataBean<>();
    private FromBean fromBean = new FromBean();
    private ToBean toBean = new ToBean();
    private SnowBotMoveServer snowBotMoveServer = SnowBotMoveServer.getInstance();
    private SnowBotSerialServer snowBotSerialServer = SnowBotSerialServer.getOurInstance();
    private List<Pose> poses = new ArrayList<>();
    private boolean isPartol = false;
    private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);
    private SnowBotManager snowBotManager;
    private TimeUtil timeUtil;
    private int dialogPowerLow = 1;
    private int speechPowerLow = 1;
    //电量
    private int percentage = 0;
    private boolean isCharging = false;

    @Override
    public void onCreate() {
        super.onCreate();
        timeUtil = TimeUtil.getInterface();
        snowBotManager = SnowBotManager.getInstance();

        clientNew.connect(Constants.SERVER_IPADDR, this);
        MessageServerHandler.setMessageRecInterface(this);
        fromBean.setType("robot");
        fromBean.setId(SharedUtil.getPreferStr(SharedKey.DEVICEUUID));
    }

    Runnable powerLower = new Runnable() {
        @Override
        public void run() {
            CsjSpeechSynthesizer.getSynthesizer().startSpeaking(getResources().getString(R.string.power_too_low), null);
            CSJToast.showToast(ClientService.this, getResources().getString(R.string.power_too_low));
            powerHandler.postDelayed(powerLower, powerLowDialogTime);
        }
    };

    private void showPowerLowDialog() {
        Csjlogger.debug("showPowerLowDialog ", "开始show");
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        Dialog normalDialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.power_low))
                .setMessage(getResources().getString(R.string.remind_again))
                .setPositiveButton(getResources().getString(R.string.ensure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PowerStatus.getIstance().setPowerLowWarn(true);
                        timeUtil.stop();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancle), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PowerStatus.getIstance().setPowerLowWarn(false);
                        timeUtil.stop();
                        dialog.dismiss();
                    }
                })
                .create();
        Csjlogger.debug("showPowerLowDialog ", "Dialog show create");
        normalDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        normalDialog.show();
        Csjlogger.debug("showPowerLowDialog ", "倒计时");
        timeUtil.getTime(10, new TimeUtil.TimeListener() {
            @Override
            public void getTime(int i) {
                if (i == 0) {
                    normalDialog.dismiss();
                }
            }
        });
    }

//    /**
//     * @param
//     * @Description: 获取电量和状态和电感器数据
//     * @author Administrator
//     * @time 2016/8/15 0015
//     */
//    public void getBatteryPercentageSensor() {
//        snowBotMoveServer.getBatteryPercentage();
//        snowBotMoveServer.getBatteryIsCharging();
//    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void messageRec(String msg) {
        recMessage(msg);
    }

    @Override
    public void recMessage(String msg) {
        JSONObject object = JSON.parseObject(msg);
        if (object != null) {
            object = object.getJSONObject("error");
            if (object != null) {
                int errorCode = object.getIntValue("code");
                if (errorCode == 20) {
                    clientNew.setisRegisted(false);
                }
            }
        }
        String command = "";
        String data = "";
        String service = "";
        try {
            CommandBean commandBean = JSON.parseObject(msg, CommandBean.class);
            String jsonObjectStr = JSON.toJSONString(commandBean.getData());
            CommandDataBean<ContentBean> commandDataBean = JSON.parseObject(jsonObjectStr, new TypeReference<CommandDataBean<ContentBean>>() {
            });
            service = commandDataBean.getService();
            ContentBean contentBean = JSON.parseObject(JSON.toJSONString(commandDataBean.getContent()), ContentBean.class);
            command = contentBean.getCmd();
            data = contentBean.getCmdDetail();
            SharedUtil.setPreferStr(SharedKey.USERID, contentBean.getFrom().getId());
            SharedUtil.setPreferStr(SharedKey.USERTYPE, contentBean.getFrom().getType());
            toBean.setType(SharedUtil.getPreferStr(SharedKey.USERTYPE));
            toBean.setId(SharedUtil.getPreferStr(SharedKey.USERID));
        } catch (Exception e) {

        }
        switch (service) {
            case "SessionStatus":
                CommandBean commandBean1 = JSON.parseObject(msg, CommandBean.class);
                String jsonObjectStr = JSON.toJSONString(commandBean1.getData());
                CommandDataBean<OnLineContent> commandDataBean1 = JSON.parseObject(jsonObjectStr, new TypeReference<CommandDataBean<OnLineContent>>() {
                });
                OnLineContent onLineContent = JSON.parseObject(JSON.toJSONString(commandDataBean1.getContent()), OnLineContent.class);
                if (onLineContent.getStatus().equals("online")) {
                    updateDeviceInfo();
                    try {
                        SharedUtil.setPreferStr(SharedKey.USERID, onLineContent.getContacts().get(0).getId());
                        SharedUtil.setPreferStr(SharedKey.USERTYPE, onLineContent.getContacts().get(0).getType());
                    } catch (Exception e) {

                    }
                }

                break;
            case "SessionClose":
                break;
        }
        if (command == null) {
            return;
        }
        switch (command) {
            case "Control": // 1
                try {
                    int dirction = Integer.parseInt(data);
                    if (dirction < 4) {
                        SnowBotMoveServer.getInstance().moveBy(MoveDirection.values()[dirction]);
                    }
                    fixedThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            getMapSize();
                        }
                    });
                    sendMapSize();
                } catch (NumberFormatException e) {

                }

                break;
            case "CancleAction":
                snowBotMoveServer.cancelAction();
                break;

            case "Goroom": {//1
                if (StrUtil.isNotBlank(data)) {
                    List<Home> homeData = SharedUtil.getListObj(SharedKey.HOMEDATAS, Home.class);
                    if (null != homeData && homeData.size() > 0) {
                        for (Home home : homeData) {
                            if (home.getHomename().equals(data)) {
                                snowBotMoveServer.moveTo(home.getmOffsetX(), home.getmOffsetY());
                                return;
                            }
                        }
                    }
                }

            }
            break;
            case "Voice":
                if (StrUtil.isNotBlank(data)) {
                    CsjSpeechSynthesizer.getSynthesizer().startSpeaking(data, null);
                }
                break;
            case "Aircontrol": //无
                int value = Integer.parseInt(data);
                if (value <= 8) {
                    Intent intent = new Intent(Constants.ClientActions.ACTION_AIR_CTRL);
                    intent.putExtra("air", value);
//                    lbm.sendBroadcast(intent);
                }
                break;
            case "Camera":
                Intent intent = new Intent(this, VideoRecordActivity.class);
                intent.putExtra("autoTakePhoto", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                break;
            case "Record":
                Intent intent1 = new Intent(this, VideoRecordActivity.class);
                intent1.putExtra("autoRecord", true);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent1);

                break;
            case "Requiremap":  // 1
                fixedThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        sendMapBase64Data();
                        getMapSize();
                    }
                });
                sendMapSize();

                break;
            // 获取姿态
            case "Requirepose": //1
                sendLocation();
                break;
            //  要求回去充电
            case "Recharge"://无
                SnowBotMoveServer.getInstance().goHome();
                break;
            // 巡逻
            case "Patrol": // 1
                SnowBotManager snowBot = SnowBotManager.getInstance();
                goParol(snowBot);
                break;
            // 机器人当前状态
            case "CurrentState": //假数据对接
                sendCurrentState();
                break;
            // 获取传感器数据
            case "Sensordata"://假数据对接
                sendSendordata();
                break;
            case "Power":  // 1
//                getBatteryPercentageSensor();
                sendPower();
                break;
            case "RequestRome":  // 请求命令
                sendHomeData();
                break;
            case "VideoCall":
                Intent intent2 = new Intent(this, VidyoSampleActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent2);
                break;
            default:
                break;
        }
    }


    private Runnable HeartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    getMapSize();

                }
            });
            sendMapSize();
            mHeartHandler.postDelayed(HeartBeatRunnable, 10000);
        }
    };


    /**
     * 字符串的压缩
     *
     * @return 返回压缩后的字符串
     * @throws IOException
     */
    public static byte[] compress(byte[] ori) throws IOException {
        // 创建一个新的 byte 数组输出流
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // 使用默认缓冲区大小创建新的输出流
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        // 将 b.length 个字节写入此输出流
        gzip.write(ori);
        gzip.close();
        // 使用指定的 charsetName，通过解码字节将缓冲区内容转换为字符串
        return out.toByteArray();
    }


    /**
     * 字符串的解压
     *
     * @return 返回解压缩后的字符串
     * @throws IOException
     */
    public static byte[] unCompress(byte[] ori) throws IOException {
        // 创建一个新的 byte 数组输出流
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // 创建一个 ByteArrayInputStream，使用 buf 作为其缓冲区数组
        ByteArrayInputStream in = new ByteArrayInputStream(ori);
        // 使用默认缓冲区大小创建新的输入流
        GZIPInputStream gzip = new GZIPInputStream(in);
        byte[] buffer = new byte[256];
        int n = 0;
        while ((n = gzip.read(buffer)) >= 0) {// 将未压缩数据读入字节数组
            // 将指定 byte 数组中从偏移量 off 开始的 len 个字节写入此 byte数组输出流
            out.write(buffer, 0, n);
        }
        // 使用指定的 charsetName，通过解码字节将缓冲区内容转换为字符串
        return out.toByteArray();
    }


    @Override
    public void clientConnected() {
//        Csjlogger.debug("Netty Client connected");
//        onLine();
        mHeartHandler.post(HeartBeatRunnable);

    }

    @Override
    public void clientDisConnected() {
//        Csjlogger.debug("Netty Client disconnected");
    }

    @Override
    public void onDestroy() {
        mHeartHandler.removeCallbacks(HeartBeatRunnable);
        offfLine();
        NettyClientNew.getInstence().closeChannle();
        Csjlogger.debug("onDestroy");
        super.onDestroy();
    }

    /**
     * 发送地图数据
     *
     * @param
     */
    private void sendMapData(String mapData) {
        DataContent<String> dataContent = new DataContent<>();
        dataContent.setFrom(fromBean);
        dataContent.setTo(toBean);
        dataContent.setPayload(mapData);
        dataContent.setPayloadType("Map");
        sendCommonData.setContent(dataContent);
        sendCommonData.setServer("iot");
        sendCommonData.setService("SendData");
        sendCommonData.setTimestamp(CommonTool.getCurrentTime());
        sendData.setData(sendCommonData);
        String dataStr = JSON.toJSONString(sendData);
        clientNew.sendMessage(dataStr);
    }

    /**
     * 发送电量
     *
     * @param
     */
    private void sendPower() {
        DataContent<Power> dataContent = new DataContent<>();
        Power power = new Power();
        power.setPower(percentage);
        power.setRecharge(isCharging);
        dataContent.setFrom(fromBean);
        dataContent.setTo(toBean);
        dataContent.setPayload(power);
        dataContent.setPayloadType("Power");
        sendCommonData.setContent(dataContent);
        sendCommonData.setServer("iot");
        sendCommonData.setService("SendData");
        sendCommonData.setTimestamp(CommonTool.getCurrentTime());
        sendData.setData(sendCommonData);
        String dataStr = JSON.toJSONString(sendData);
//        client.sendMsg(dataStr);
        clientNew.sendMessage(dataStr);
    }

    /**
     * 发送Location
     *
     * @param
     */
    private void sendLocation() {
        Pose pose = snowBotMoveServer.getPose();
        com.slamtec.slamware.robot.Location robotLocation = pose.getLocation();
        DataContent<Location> dataContent = new DataContent<>();
        Location location = new Location();
        location.setX(robotLocation.getX());
        location.setY(robotLocation.getY());
        location.setY(robotLocation.getZ());
        dataContent.setFrom(fromBean);
        dataContent.setTo(toBean);
        dataContent.setPayload(location);
        dataContent.setPayloadType("Location");
        sendCommonData.setContent(dataContent);
        sendCommonData.setServer("iot");
        sendCommonData.setService("SendData");
        sendCommonData.setTimestamp(CommonTool.getCurrentTime());
        sendData.setData(sendCommonData);
        String dataStr = JSON.toJSONString(sendData);
        clientNew.sendMessage(dataStr);
    }

    /**
     * 发送Sendordata
     *
     * @param
     */
    private void sendSendordata() {
        DataContent<Sensor> dataContent = new DataContent<>();
        Sensor sensor = new Sensor();
        sensor.setHumidity(SharedUtil.getPreferInt(SharedKey.HUMIDITY, 0));
        sensor.setPm25(SharedUtil.getPreferInt(SharedKey.PM25, 0));
        sensor.setTempreature(SharedUtil.getPreferInt(SharedKey.TEMPERATURE, 0));
        dataContent.setFrom(fromBean);
        dataContent.setTo(toBean);
        dataContent.setPayload(sensor);
        dataContent.setPayloadType("Sendordata");
        sendCommonData.setContent(dataContent);
        sendCommonData.setServer("iot");
        sendCommonData.setService("SendData");
        sendCommonData.setTimestamp(CommonTool.getCurrentTime());
        sendData.setData(sendCommonData);
        String dataStr = JSON.toJSONString(sendData);
        clientNew.sendMessage(dataStr);
    }

    /**
     * 发送CurrentState
     *
     * @param
     */
    private void sendCurrentState() {
        DataContent<CurrentState> dataContent = new DataContent<>();
        CurrentState currentState = new CurrentState();
        currentState.setTempreature(SharedUtil.getPreferInt(SharedKey.TEMPERATURE, 0));
        currentState.setHumidity(SharedUtil.getPreferInt(SharedKey.HUMIDITY, 0));
        currentState.setPm25(SharedUtil.getPreferInt(SharedKey.PM25, 0));
        currentState.setPower(percentage);
        currentState.setRechage(SharedUtil.getPreferBool(SharedKey.ISCHARGING, false));
        dataContent.setFrom(fromBean);
        dataContent.setTo(toBean);
        dataContent.setPayload(currentState);
        dataContent.setPayloadType("CurrentState");
        sendCommonData.setContent(dataContent);
        sendCommonData.setServer("iot");
        sendCommonData.setService("SendData ");
        sendCommonData.setTimestamp(CommonTool.getCurrentTime());
        sendData.setData(sendCommonData);
        String dataStr = JSON.toJSONString(sendData);
        clientNew.sendMessage(dataStr);
    }

    private void onLine() {
        CommandBean commandBean = new CommandBean();
        CommandDataBean<LoginContentBean> commandDataBean = new CommandDataBean<>();
        LoginContentBean loginContentBean = new LoginContentBean();
        commandDataBean.setServer("iot");
        commandDataBean.setService("ClientOnline");
        commandDataBean.setTimestamp(CommonTool.getCurrentTime());
        loginContentBean.setType("robot");
        loginContentBean.setId(SharedUtil.getPreferStr(SharedKey.DEVICEUUID));
        commandDataBean.setContent(loginContentBean);
        commandBean.setData(commandDataBean);
        String str = JSON.toJSONString(commandBean);
        clientNew.sendMessage(str);
    }

    private void offfLine() {
        Csjlogger.debug("Netty Client offline");
        CommandBean commandBean = new CommandBean();
        CommandDataBean<LoginContentBean> commandDataBean = new CommandDataBean<>();
        LoginContentBean loginContentBean = new LoginContentBean();
        commandDataBean.setServer("iot");
        commandDataBean.setService("ClientOffline");
        commandDataBean.setTimestamp(CommonTool.getCurrentTime());
        loginContentBean.setType("robot");
        loginContentBean.setId(SharedUtil.getPreferStr(SharedKey.DEVICEUUID));
        commandDataBean.setContent(loginContentBean);
        commandBean.setData(commandDataBean);
        String str = JSON.toJSONString(commandBean);
        clientNew.sendMessage(str);
    }


    private void sendMapBase64Data() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Bitmap bitmap = snowBotMoveServer.getBitMap(this);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
            int options = 100;
            while (baos.toByteArray().length / 1024 > 20) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
                baos.reset();//重置baos即清空baos
                bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
                options -= 5;//每次都减少5
            }
        } catch (Exception e) {
            Csjlogger.debug("获取不到图片");
        }
        String mapBase64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
        sendMapData(mapBase64);
    }

    /**
     * 发送地图尺寸数据，用于绘制机器人箭头
     */
    private void getMapSize() {
        try {
            snowBotMoveServer.getMapSize();
        } catch (Exception e) {
            Csjlogger.debug("无法获取机器人图标尺寸数据");
        }
    }

    private void sendMapSize() {
        fromBean.setType("robot");
        fromBean.setId(SharedUtil.getPreferStr(SharedKey.DEVICEUUID));
        toBean.setType(SharedUtil.getPreferStr(SharedKey.USERTYPE));
        toBean.setId(SharedUtil.getPreferStr(SharedKey.USERID));
        DataContent<MapSize> dataContent = new DataContent<>();
        MapSize mapSize = new MapSize();
        mapSize = SharedUtil.getObj("MAPSIZE", MapSize.class);
        if (null != mapSize) {
            dataContent.setFrom(fromBean);
            dataContent.setTo(toBean);
            dataContent.setPayloadType("MapSize");
            dataContent.setPayload(mapSize);
            sendCommonData.setContent(dataContent);
            sendCommonData.setServer("iot");
            sendCommonData.setService("SendData");
            sendCommonData.setTimestamp(CommonTool.getCurrentTime());
            sendData.setData(sendCommonData);
            String homeDataStr = JSON.toJSONString(sendData);
            if (homeDataStr != null) {
                clientNew.sendMessage(homeDataStr);
            }
        }
    }


    private void sendHomeData() {
        List<Home> homeData = new ArrayList<>();
        if (clientNew != null) {
            homeData = SharedUtil.getListObj(SharedKey.HOMEDATAS, Home.class);
        }
        fromBean.setType("robot");
        fromBean.setId(SharedUtil.getPreferStr(SharedKey.DEVICEUUID));
        toBean.setType(SharedUtil.getPreferStr(SharedKey.USERTYPE));
        toBean.setId(SharedUtil.getPreferStr(SharedKey.USERID));
        DataContent<List<Home>> dataContent = new DataContent<>();
        dataContent.setFrom(fromBean);
        dataContent.setTo(toBean);
        dataContent.setPayload(homeData);
        dataContent.setPayloadType("Home");
        sendCommonData.setContent(dataContent);
        sendCommonData.setServer("iot");
        sendCommonData.setService("SendData");
        sendCommonData.setTimestamp(CommonTool.getCurrentTime());
        sendData.setData(sendCommonData);
        String homeDataStr = JSON.toJSONString(sendData);
        clientNew.sendMessage(homeDataStr);
    }


    public void goParol(SnowBotManager snowBot) {
        if (!isPartol) {
            List<Home> homeLists = new ArrayList<>();
            homeLists = SharedUtil.getListObj(SharedKey.HOMEDATAS, Home.class);
            if (null != homeLists && homeLists.size() == 0) {
                CsjSpeechSynthesizer.getSynthesizer().startSpeaking("巡逻点未设置，请设置巡逻点", null);
                powerHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        CSJToast.showToast(Static.CONTEXT, "设置巡逻点", 1000);
                    }
                });

                return;
            }

            if (null != homeLists && homeLists.size() < 2) {
                powerHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        CSJToast.showToast(Static.CONTEXT, "设置的点少于两个，请设置更多的点", 1000);
                    }
                });
                return;
            }

            for (Home home : homeLists) {
                Pose pose = new Pose();
                pose.setX(home.getLocation().getX());
                pose.setY(home.getLocation().getY());
//                pose.setZ(home.getRotation().getYaw());
                poses.add(pose);
            }
        }

        if (!isPartol) {
            CsjSpeechSynthesizer.getSynthesizer().startSpeaking("小雪开始巡逻辣", null);
            powerHandler.post(new Runnable() {
                @Override
                public void run() {
                    CSJToast.showToast(Static.CONTEXT, "开始巡逻", 1000);
                }
            });
            snowBot.partol(poses);
            isPartol = true;
        } else {
            snowBot.stopPartol();
            isPartol = false;
        }
    }

    /**
     * 上传机器人所在地信息
     */
    private void updateDeviceInfo() {
        CommandBean commandBean = new CommandBean();
        CommandDataBean<UpdateDeviceDataBean> commandDataBean = new CommandDataBean<>();
        UpdateDeviceDataBean updateDeviceDataBean = new UpdateDeviceDataBean();
        UpdateDeviceDataBean.GeoBean geoBean = new UpdateDeviceDataBean.GeoBean();
        geoBean.setX(SharedUtil.getPreferFloat(SharedKey.LONTITUDE));
        geoBean.setY(SharedUtil.getPreferFloat(SharedKey.LATITUDE));
        updateDeviceDataBean.setGeo(geoBean);
        updateDeviceDataBean.setDeviceKey(SharedUtil.getPreferStr(SharedKey.DEVICEKEY));
        updateDeviceDataBean.setProductKey(SharedUtil.getPreferStr(SharedKey.PRODUCTKEY));
        commandDataBean.setContent(updateDeviceDataBean);
        commandDataBean.setServer("iot");
        commandDataBean.setService("DeviceStatus");
        commandDataBean.setTimestamp(CommonTool.getCurrentTime());
        commandBean.setData(commandDataBean);
        String tempStr = JSON.toJSONString(commandBean);
        clientNew.sendMessage(tempStr);
    }

    private boolean needShowDialog = true;
    private static boolean isAutoTakePhoto;

    public static void setIsAutoTakePhoto(boolean isAutoTakePhoto) {
        ClientService.isAutoTakePhoto = isAutoTakePhoto;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void robotStatusUpdated(RobotStatusUpdateEvent event) {
//        Csjlogger.debug("Robot is isCharging {} , BatteryPercentage is {}, LocalizationQuality is {}",
//                event.isCharging(), event.getBatteryPercentage(), event.getLocalizationQuality());

        isCharging = event.isCharging();
        percentage = event.getBatteryPercentage();

//        if (percentage > 0 && !isCharging) {
//            if (percentage <= Constants.LOW_POWER_TO_WARNING && percentage > 5 && dialogPowerLow == 1
//                    && needShowDialog && !isAutoTakePhoto) {  //当电量大于5小于30时，跳转dialog，语音唤醒不可用；确定后，可用
////                    showPowerLowDialog();
//
//                dialogPowerLow++;
//                speechPowerLow = 1;
//                PowerStatus.getIstance().setPowerLowWarn(true);
//                SpeechStatus.getIstance().setAiuiResponse(false);
//                Intent intent = new Intent(ClientService.this, DialogAct.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//                powerHandler.removeCallbacks(powerLower);
////                needShowDialog = false;
//            } else if (percentage <= 5 && speechPowerLow == 1) {  //当电量小于5时，语音十分钟提示一次，且语音不可用
//                speechPowerLow++;
//                dialogPowerLow = 1;
//                PowerStatus.getIstance().setPowerLowWarn(true);
//                snowBotManager.stopPartol();
//                powerHandler.post(powerLower);
//            } else if (percentage > Constants.LOW_POWER_TO_WARNING) {
//                dialogPowerLow = 1;
//                speechPowerLow = 1;
//                PowerStatus.getIstance().setPowerLowWarn(false);
//                powerHandler.removeCallbacks(powerLower);
//            }
//
//        } else if (isCharging) { //充电状态下，可用
//            PowerStatus.getIstance().setPowerLowWarn(false);
//        }
    }
}
