package com.csjbot.snowbot.bean;

/**
 * Created by Administrator on 2016/9/5 0005.
 */
public class CommonIntBean {

    public CommonIntBean() {
    }

    public CommonIntBean(String target, int content) {
        slots = new SlotsBean();
        slots.setContent(content);
        slots.setTarget(target);
    }

    /**
     * target : robotID
     * sendtime :
     * content : string
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
        private String sendtime = String.valueOf(System.currentTimeMillis());
        private int content;

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

        public int getContent() {
            return content;
        }

        public void setContent(int content) {
            this.content = content;
        }
    }
}
