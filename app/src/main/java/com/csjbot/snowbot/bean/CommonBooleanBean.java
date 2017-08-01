package com.csjbot.snowbot.bean;

/**
 * Created by Administrator on 2016/9/1 0001.
 */
public class CommonBooleanBean {

    /**
     * target : robotID
     * sendtime :
     * content : true
     */

    private SlotsBean slots;

    public SlotsBean getSlots() {
        return slots;
    }

    public void setSlots(SlotsBean slots) {
        this.slots = slots;
    }

    public static class SlotsBean {
        private String target;
        private String sendtime;
        private boolean content;

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public String getSendtime() {
            return sendtime;
        }

        public void setSendtime(String sendtime) {
            this.sendtime = sendtime;
        }

        public boolean isContent() {
            return content;
        }

        public void setContent(boolean content) {
            this.content = content;
        }
    }
}
