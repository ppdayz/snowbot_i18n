package com.csjbot.snowbot.bean;

import com.csjbot.snowbot.usermanager.UserManager;

/**
 * Created by Administrator on 2016/9/2 0002.
 */
public class CommonStringBean {


    public CommonStringBean() {
    }

    public CommonStringBean(String content) {
        slots = new SlotsBean();
        slots.setContent(content);
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
        private String target = UserManager.getInstance().getCurrentUser();
        private String sendtime = String.valueOf(System.currentTimeMillis());
        private String content;

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

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
