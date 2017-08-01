package com.csjbot.snowbot.utils;

/**
 * Created by Administrator on 2017/4/10 0010.
 */

public class PowerStatus {
    public boolean isPowerLowWarn() {
        return powerLowWarn;
    }

    public void setPowerLowWarn(boolean powerLowWarn) {
        this.powerLowWarn = powerLowWarn;
    }

    private boolean powerLowWarn = false;  //判断是否电量过低

    // 定义一个私有构造方法
    private PowerStatus() {

    }

    //定义一个静态私有变量(不初始化，不使用final关键字，使用volatile保证了多线程访问时instance变量的可见性，避免了instance初始化时其他变量属性还没赋值完时，被另外线程调用)
    private static volatile PowerStatus instance;

    //定义一个共有的静态方法，返回该类型实例
    public static PowerStatus getIstance() {
        // 对象实例化时与否判断（不使用同步代码块，instance不等于null时，直接返回对象，提高运行效率）
        if (instance == null) {
            //同步代码块（对象未初始化时，使用同步代码块，保证多线程访问时对象在第一次创建后，不再重复被创建）
            synchronized (SpeechStatus.class) {
                //未初始化，则初始instance变量
                if (instance == null) {
                    instance = new PowerStatus();
                }
            }
        }
        return instance;
    }

}
