package com.csjbot.snowbot.bean.aiui.entity;

/**
 * Created by Administrator on 2017/1/16 0016.
 */

public class WeatherDate {

    /**
     * subfocus : 天气状态
     * queryType : 内容
     * operation : QUERY
     * datetime : {"type":"DT_BASIC","date":"2017-01-17","dateOrig":"明天"}
     * location : {"type":"LOC_BASIC","cityAddr":"苏州","city":"苏州市"}
     */

    private SlotsBean slots;

    public SlotsBean getSlots() {
        return slots;
    }

    public void setSlots(SlotsBean slots) {
        this.slots = slots;
    }

    public static class SlotsBean {
        private String subfocus;
        private String queryType;
        private String operation;
        /**
         * type : DT_BASIC
         * date : 2017-01-17
         * dateOrig : 明天
         */

        private DatetimeBean datetime;
        /**
         * type : LOC_BASIC
         * cityAddr : 苏州
         * city : 苏州市
         */

        private LocationBean location;

        public String getSubfocus() {
            return subfocus;
        }

        public void setSubfocus(String subfocus) {
            this.subfocus = subfocus;
        }

        public String getQueryType() {
            return queryType;
        }

        public void setQueryType(String queryType) {
            this.queryType = queryType;
        }

        public String getOperation() {
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public DatetimeBean getDatetime() {
            return datetime;
        }

        public void setDatetime(DatetimeBean datetime) {
            this.datetime = datetime;
        }

        public LocationBean getLocation() {
            return location;
        }

        public void setLocation(LocationBean location) {
            this.location = location;
        }

        public static class DatetimeBean {
            private String type;
            private String date;
            private String dateOrig;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getDate() {
                return date;
            }

            public void setDate(String date) {
                this.date = date;
            }

            public String getDateOrig() {
                return dateOrig;
            }

            public void setDateOrig(String dateOrig) {
                this.dateOrig = dateOrig;
            }
        }

        public static class LocationBean {
            private String type;
            private String cityAddr;
            private String city;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getCityAddr() {
                return cityAddr;
            }

            public void setCityAddr(String cityAddr) {
                this.cityAddr = cityAddr;
            }

            public String getCity() {
                return city;
            }

            public void setCity(String city) {
                this.city = city;
            }
        }
    }
}
