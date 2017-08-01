package com.csjbot.snowbot.bean;

import com.csjbot.snowbot.usermanager.UserManager;

/**
 * Created by Administrator on 2016/9/18 0018.
 */
public class SensorBean {


    public SensorBean() {
    }

    public SensorBean(int pm25, int temperature, int humidity) {
        slots = new SlotsBean();
        slots.content = slots.new ContentBean(pm25, temperature, humidity);
    }

    /**
     * target : userID
     * datetime : sendtime
     * content : {"pm25":0,"temperature":0,"humidity":0}
     */

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
            private int pm25;
            private int temperature;
            private int humidity;

            public ContentBean(int pm25, int temperature, int humidity) {
                this.pm25 = pm25;
                this.temperature = temperature;
                this.humidity = humidity;
            }

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
        }
    }
}
