package com.csjbot.snowbot.bean;

/**
 * Created by Administrator on 2016/9/1 0001.
 */
public class CtrlBean {

    /**
     * content : forward
     * datetime : sendtime
     * target : robotUID
     */

    private SlotsBean slots;

    public SlotsBean getSlots() {
        return slots;
    }

    public void setSlots(SlotsBean slots) {
        this.slots = slots;
    }

    public static class SlotsBean {
        private String content;
        private String datetime;
        private String target;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getDatetime() {
            return datetime;
        }

        public void setDatetime(String datetime) {
            this.datetime = datetime;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }
    }
}
