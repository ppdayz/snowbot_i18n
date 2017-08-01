package com.csjbot.snowbot.bean;

/**
 * Created by Administrator on 2016/9/1 0001.
 */
public class LoginBean {

    public LoginBean() {
        slots = new SlotsBean();
    }

    /**
     * datetime : xxxxxx
     */

    private SlotsBean slots;

    public SlotsBean getSlots() {
        return slots;
    }

    public void setSlots(SlotsBean slots) {
        this.slots = slots;
    }

    public static class SlotsBean {
        private String datetime = String.valueOf(System.currentTimeMillis());
        private String Authorizationcode = "123456789ABCDEF";

        public String getDatetime() {
            return datetime;
        }

        public void setDatetime() {
            this.datetime = String.valueOf(System.currentTimeMillis());
        }


        public String getAuthorizationcode() {
            return Authorizationcode;
        }

        public void setAuthorizationcode(String authorizationcode) {
            Authorizationcode = authorizationcode;
        }
    }
}
