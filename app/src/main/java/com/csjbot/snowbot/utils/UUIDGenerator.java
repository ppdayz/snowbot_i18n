package com.csjbot.snowbot.utils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.csjbot.snowbot.app.CsjApplication;

import dou.utils.CpuUtil;

/**
 * Created by Administrator on 2016/6/12 0012.
 */
public class UUIDGenerator {
    private String snowBotUUID = "";

    private static UUIDGenerator instance = new UUIDGenerator();

    private UUIDGenerator() {
        //实例化SharedPreferences对象（第一步）
        SharedPreferences mySharedPreferences = CsjApplication.getAppContext().getSharedPreferences("UUID",
                Activity.MODE_PRIVATE);
        //实例化SharedPreferences.Editor对象（第二步）
        snowBotUUID = mySharedPreferences.getString("uuid", "");

        // 如果为空，就生成一个
        if (snowBotUUID.isEmpty()) {
            snowBotUUID = generatorUUID();
            SharedPreferences.Editor editor = mySharedPreferences.edit();
            editor.putString("uuid", snowBotUUID);
            editor.commit();
        }
    }

    private String generatorUUID() {
        Context ctx = CsjApplication.getAppContext();
        String m_szWLANMAC = CpuUtil.getMacAddress();

        TelephonyManager TelephonyMgr = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        String szImei = TelephonyMgr.getDeviceId();

        BluetoothAdapter m_BluetoothAdapter = null; // Local Bluetooth adapter
        m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String m_szBTMAC = m_BluetoothAdapter.getAddress();

        String m_szAndroidID = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);

        String m_szLongID = szImei + m_szAndroidID + m_szWLANMAC + m_szBTMAC;

        return MD5.stringToMD5(m_szLongID);
    }

    public static UUIDGenerator getInstance() {
        return instance;
    }

    public String getDeviceUUID() {
//        return "94C9FEEF39C8B648";
        return snowBotUUID;
    }
}
