package com.csjbot.snowbot.bean.aiui;

import java.util.List;

/**
 * Created by Administrator on 2016/11/9 0009.
 */

public class WeatherBean {

    /**
     * wind : 西风3-4级
     * windLevel : 1
     * dateLong : 1478707200
     * lastUpdateTime : 2016-11-10 18:33:14
     * weatherType : 0
     * date : 2016-11-10
     * city : 苏州
     * exp : {"ct":{"expName":"穿衣指数","prompt":"建议着薄外套、开衫牛仔衫裤等服装。年老体弱者应适当添加衣物，宜着夹克衫、薄毛衣等。","level":"较舒适"}}
     * humidity : 78%
     * airQuality : 良
     * tempRange : 8℃
     * weather : 晴
     * temp : 10
     * pm25 : 38
     * airData : 55
     */

    private List<ResultBean> result;

    public List<ResultBean> getResult() {
        return result;
    }

    public void setResult(List<ResultBean> result) {
        this.result = result;
    }

    public static class ResultBean {
        private String wind;
        private int windLevel;
        private int dateLong;
        private String lastUpdateTime;
        private int weatherType;
        private String date;
        private String city;
        /**
         * ct : {"expName":"穿衣指数","prompt":"建议着薄外套、开衫牛仔衫裤等服装。年老体弱者应适当添加衣物，宜着夹克衫、薄毛衣等。","level":"较舒适"}
         */

        private ExpBean exp;
        private String humidity;
        private String airQuality;
        private String tempRange;
        private String weather;
        private int temp;
        private String pm25;
        private int airData;

        public String getWind() {
            return wind;
        }

        public void setWind(String wind) {
            this.wind = wind;
        }

        public int getWindLevel() {
            return windLevel;
        }

        public void setWindLevel(int windLevel) {
            this.windLevel = windLevel;
        }

        public int getDateLong() {
            return dateLong;
        }

        public void setDateLong(int dateLong) {
            this.dateLong = dateLong;
        }

        public String getLastUpdateTime() {
            return lastUpdateTime;
        }

        public void setLastUpdateTime(String lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
        }

        public int getWeatherType() {
            return weatherType;
        }

        public void setWeatherType(int weatherType) {
            this.weatherType = weatherType;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public ExpBean getExp() {
            return exp;
        }

        public void setExp(ExpBean exp) {
            this.exp = exp;
        }

        public String getHumidity() {
            return humidity;
        }

        public void setHumidity(String humidity) {
            this.humidity = humidity;
        }

        public String getAirQuality() {
            return airQuality;
        }

        public void setAirQuality(String airQuality) {
            this.airQuality = airQuality;
        }

        public String getTempRange() {
            return tempRange;
        }

        public void setTempRange(String tempRange) {
            this.tempRange = tempRange;
        }

        public String getWeather() {
            return weather;
        }

        public void setWeather(String weather) {
            this.weather = weather;
        }

        public int getTemp() {
            return temp;
        }

        public void setTemp(int temp) {
            this.temp = temp;
        }

        public String getPm25() {
            return pm25;
        }

        public void setPm25(String pm25) {
            this.pm25 = pm25;
        }

        public int getAirData() {
            return airData;
        }

        public void setAirData(int airData) {
            this.airData = airData;
        }

        public static class ExpBean {
            /**
             * expName : 穿衣指数
             * prompt : 建议着薄外套、开衫牛仔衫裤等服装。年老体弱者应适当添加衣物，宜着夹克衫、薄毛衣等。
             * level : 较舒适
             */

            private CtBean ct;

            public CtBean getCt() {
                return ct;
            }

            public void setCt(CtBean ct) {
                this.ct = ct;
            }

            public static class CtBean {
                private String expName;
                private String prompt;
                private String level;

                public String getExpName() {
                    return expName;
                }

                public void setExpName(String expName) {
                    this.expName = expName;
                }

                public String getPrompt() {
                    return prompt;
                }

                public void setPrompt(String prompt) {
                    this.prompt = prompt;
                }

                public String getLevel() {
                    return level;
                }

                public void setLevel(String level) {
                    this.level = level;
                }
            }
        }
    }


}
