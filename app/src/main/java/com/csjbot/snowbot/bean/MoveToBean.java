package com.csjbot.snowbot.bean;

/**
 * Created by Administrator on 2016/9/1 0001.
 */
public class MoveToBean {

    public MoveToBean() {
    }


    public MoveToBean(String target, String content) {
        slots = new SlotsBean();
        slots.setContent(content);
        slots.setDatetime(String.valueOf(System.currentTimeMillis()));
        slots.setTarget(target);
    }

    /**
     * target : robotUID
     * datetime : sendtime
     * content : room id
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
        private String datetime;
        private String content;

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

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
