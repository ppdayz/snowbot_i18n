package com.csjbot.snowbot.bean;

import com.csjbot.snowbot.usermanager.UserManager;

/**
 * Created by Administrator on 2016/9/18 0018.
 */
public class PowerBean {

    /**
     * target : userID
     * datetime : sendtime
     * content : {"power":0,"recharge":true}
     */

    private SlotsBean slots;

    public PowerBean(int power, boolean isCharging) {
        slots = new SlotsBean();
        slots.content = slots.new ContentBean();
        slots.content.setPower(power);
        slots.content.setRecharge(isCharging);
    }

    public PowerBean() {
    }

    public SlotsBean getSlots() {
        return slots;
    }

    public void setSlots(SlotsBean slots) {
        this.slots = slots;
    }

    public static class SlotsBean {
        private String target = UserManager.getInstance().getCurrentUser();
        private String datetime = String.valueOf(System.currentTimeMillis());
        /**
         * power : 0
         * recharge : true
         */

        private ContentBean content;

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public String getDatetime() {
            return datetime;
        }

        public void setDatetime(String datetime) {
            this.datetime = datetime;
        }

        public ContentBean getContent() {
            return content;
        }

        public void setContent(ContentBean content) {
            this.content = content;
        }

        public class ContentBean {
            private int power;
            private boolean recharge;

            public int getPower() {
                return power;
            }

            public void setPower(int power) {
                this.power = power;
            }

            public boolean isRecharge() {
                return recharge;
            }

            public void setRecharge(boolean recharge) {
                this.recharge = recharge;
            }
        }
    }
}
