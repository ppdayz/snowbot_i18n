package com.csjbot.snowbot.usermanager;

/**
 * Created by Administrator on 2016/9/13 0013.
 */
public class UserManager {
    private static UserManager ourInstance = new UserManager();

    public static UserManager getInstance() {
        return ourInstance;
    }

    private UserManager() {
    }

    public String getCurrentUser() {
        return "18112606392";
    }
}
