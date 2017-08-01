package com.csjbot.snowbot.bean;

/**
 * @author: jl
 * @Time: 2016/12/28
 * @Desc:
 */

public class Power {
    private  int power;
    private boolean recharge;

    public boolean isRecharge() {
        return recharge;
    }

    public void setRecharge(boolean recharge) {
        this.recharge = recharge;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }


}
