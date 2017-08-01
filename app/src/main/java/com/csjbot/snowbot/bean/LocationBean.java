package com.csjbot.snowbot.bean;

import com.csjbot.snowbot.usermanager.UserManager;

/**
 * Created by Administrator on 2016/9/2 0002.
 */
public class LocationBean {

    public LocationBean(float x, float y, float yaw) {
        slots = new SlotsBean();
        slots.content = new SlotsBean.ContentBean();
        slots.content.setX(x);
        slots.content.setY(y);
        slots.content.setYaw(yaw);
    }

    /**
     * target : userID
     * datetime : sendtime
     * content : {"x":0,"y":0,"yaw":0}
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
         * x : 0
         * y : 0
         * yaw : 0
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
            private float x;
            private float y;
            private float yaw;

            public float getX() {
                return x;
            }

            public void setX(float x) {
                this.x = x;
            }

            public float getY() {
                return y;
            }

            public void setY(float y) {
                this.y = y;
            }

            public float getYaw() {
                return yaw;
            }

            public void setYaw(float yaw) {
                this.yaw = yaw;
            }
        }
    }
}
