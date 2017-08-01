package com.csjbot.snowbot.services;

import android.app.IntentService;
import android.content.Intent;

import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot_rogue.servers.slams.events.ConfigPageEntey;
import com.csjbot.snowbot_rogue.servers.slams.events.ConnectedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author jwc
 *         用于检查是否连接以太网
 */
public class CheckEthernetService extends IntentService {

    public CheckEthernetService() {
        super("CheckEthernetService");
    }

    /**
     * 打开以太网命令
     */
    private static final String OPEN = "ifconfig eth0 up " + "\n"
            + "exit\n";
    /**
     * 关闭以太网命令
     */
    private static final String CLOSE = "ifconfig eth0 down " + "\n"
            + "exit\n";

    /**
     * 获取以太网IP地址命令
     */
    private static final String ETHERNET_IP = "ifconfig eth0";

    /**
     * 间隔时间(15秒)
     */
    private static final int TIME = 15000;


    /**
     * 是否连接
     */
    private boolean isConnected = false;

    private EventBus iBus = EventBus.getDefault();
    private String checkString = "192.168.99";

    @Override
    protected void onHandleIntent(Intent intent) {
        Csjlogger.debug("======================================" + checkString);
        while (!(getEthernetIP() != null && getEthernetIP().contains(checkString))) {
            if (isConnected) {
                break;
            }
            upDownEht0(CLOSE);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            upDownEht0(OPEN);

            try {
                Thread.sleep(TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate() {
        iBus.register(this);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (iBus != null) {
            iBus.unregister(this);
        }
        super.onDestroy();
    }


    private void upDownEht0(String cmd) {
        try {
            Process su;
            su = Runtime.getRuntime().exec("/system/bin/su");
            su.getOutputStream().write(cmd.getBytes());
            int status = su.waitFor();
            if ((status != 0)) {
                Csjlogger.error("status = {}", status);
            }
        } catch (Exception e) {
            Csjlogger.error(e);
        }
    }


    /**
     * 获取以太网ip地址
     *
     * @return ip地址
     */
    private String getEthernetIP() {
        InputStream is = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            Process process = Runtime.getRuntime().exec(ETHERNET_IP);
            is = process.getInputStream();
            inputStreamReader = new InputStreamReader(is);
            bufferedReader = new BufferedReader(inputStreamReader);
            String line = "";
            StringBuilder sb = new StringBuilder(line);
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            int status = process.waitFor();
            if (status != 0) {
                Csjlogger.error("status is {}" + status);
            }
            Csjlogger.debug("===EthernetIP====" + sb.toString());
            return sb.toString();
        } catch (Exception e) {
            Csjlogger.error(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    @Subscribe(threadMode = ThreadMode.POSTING)
    public void connectSlamSuccess(ConnectedEvent event) {
        isConnected = true;
    }


    @Subscribe(threadMode = ThreadMode.POSTING)
    public void configPageEntey(ConfigPageEntey event) {
        checkString = "192.168.11";
    }

}
