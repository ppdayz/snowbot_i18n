package com.csjbot.snowbot.bean;

import com.csjbot.snowbot.usermanager.UserManager;

/**
 * Created by Administrator on 2016/9/13 0013.
 */
public class CurrentStateBean {

    /**
     * target : userID
     * datetime : sendtime
     * content : {"pm25":0,"temperature":0,"humidity":0,"power":0,"recharge":true}
     */

    public CurrentStateBean() {

    }


    public CurrentStateBean(int pm25, int temperature, int humidity, int power, boolean recharge) {
        slots = new SlotsBean();
        slots.setContent(new SlotsBean.ContentBean(pm25, temperature, humidity, power, recharge));
    }

    private SlotsBean slots;

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
         * pm25 : 0
         * temperature : 0
         * humidity : 0
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

        public static class ContentBean {

            public ContentBean(int pm25, int temperature, int humidity, int power, boolean recharge) {
                this.pm25 = pm25;
                this.temperature = temperature;
                this.humidity = humidity;
                this.power = power;
                this.recharge = recharge;
            }

            private int pm25;
            private int temperature;
            private int humidity;
            private int power;
            private boolean recharge;

            public int getPm25() {
                return pm25;
            }

            public void setPm25(int pm25) {
                this.pm25 = pm25;
            }

            public int getTemperature() {
                return temperature;
            }

            public void setTemperature(int temperature) {
                this.temperature = temperature;
            }

            public int getHumidity() {
                return humidity;
            }

            public void setHumidity(int humidity) {
                this.humidity = humidity;
            }

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