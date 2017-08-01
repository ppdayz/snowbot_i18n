package com.csjbot.snowbot.utils;

/**
 * @Author: jl
 * @Date: 2016/12/19
 * @Desc:
 */

public final class RobotStatus {
    public static final int NONOUTWARE = 0; //未出库
    public static final int ALREADYOUTWARE = 1;//已出库,机器人注册
    public static final int NONREGISTER = 2;//未注册
    public static final int ALREADLYREGISTER = 3;//已注册
    public static final int ALREADCOMPLETE = 4;//已完成引导页
}
