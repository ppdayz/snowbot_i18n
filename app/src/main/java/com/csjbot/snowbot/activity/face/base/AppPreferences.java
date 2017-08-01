package com.csjbot.snowbot.activity.face.base;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Field;

/**
 * Created by mac on 16/8/16.
 */
public class AppPreferences {

    private AppPreferences(Context ctx) {
        readLocalProperties(ctx);
    }

    private static AppPreferences instance;

    public final static class Constant {
        public final static String COMMON_PREFS_NAME = "user_sp"; // 存储文件名称
    }

    public final static AppPreferences getInstance() {
        return instance == null ? instance = new AppPreferences(BaseApplication.getAppContext()) : instance;
    }

    public void saveToSp(String sp_name, String key, String value) {
        SharedPreferences.Editor editor = BaseApplication.getAppContext().getSharedPreferences(
                sp_name, Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getFromKey(String sp_name, String key) {
        SharedPreferences prefs = BaseApplication.getAppContext().getSharedPreferences(
                sp_name, Context.MODE_PRIVATE);
        return prefs.getString(key, "");
    }

    public void clearSp() {
        SharedPreferences.Editor editor = BaseApplication.getAppContext().getSharedPreferences(
                Constant.COMMON_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();


    }

    /**
     * 属性值本地数据持久化
     */
    public void saveInstance(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                Constant.COMMON_PREFS_NAME, Context.MODE_PRIVATE).edit();
        try {
            for (Field field : AppPreferences.class.getDeclaredFields()) {

                if (field.getType() == String.class) {
                    editor.putString(field.getName(),
                            String.valueOf(field.get(this)));
                } else if (field.getType() == Integer.TYPE) {
                    editor.putInt(field.getName(),
                            Integer.valueOf(field.get(this).toString()));
                } else if (field.getType() == Float.TYPE) {
                    editor.putFloat(field.getName(),
                            Float.valueOf(field.get(this).toString()));
                } else if (field.getType() == Long.TYPE) {
                    editor.putLong(field.getName(),
                            Long.valueOf(field.get(this).toString()));
                } else if (field.getType() == Boolean.TYPE) {
                    editor.putBoolean(field.getName(),
                            Boolean.valueOf(field.get(this).toString()));
                } else {
                    // NOTHING TO HERE. 仅仅保存，String, int, float, long, boolean
                    // 类型的数据
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        editor.apply();// 提交更新
    }

    public void readLocalProperties(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                Constant.COMMON_PREFS_NAME, Context.MODE_PRIVATE);
        try {
            for (Field field : AppPreferences.class.getDeclaredFields()) {
                if (field.getType() == String.class) {
                    field.set(this, prefs.getString(field.getName(), ""));
                } else if (field.getType() == Integer.TYPE) {
                    field.set(this, prefs.getInt(field.getName(), -1));
                } else if (field.getType() == Float.TYPE) {
                    field.set(this, prefs.getFloat(field.getName(), -1));
                } else if (field.getType() == Long.TYPE) {
                    field.set(this, prefs.getLong(field.getName(), -1));
                } else if (field.getType() == Boolean.TYPE) {
                    field.set(this, prefs.getBoolean(field.getName(), false));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
