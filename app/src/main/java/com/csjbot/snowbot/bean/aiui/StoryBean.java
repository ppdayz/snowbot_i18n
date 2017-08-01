package com.csjbot.snowbot.bean.aiui;

import java.util.List;

/**
 * Created by Administrator on 2016/10/18 0018.
 */

public class StoryBean {

    /**
     * result : [{"playUrl":"http://leting.voicecloud.cn/Service/download.vc?appid=4fa74777&uid=1601119&rid=11919","id":4780,"series":"未知","author":"佚名","category":"成语","status":"1","name":"狼子野心"},{"playUrl":"http://leting.voicecloud.cn/Service/download.vc?appid=4fa74777&uid=1601119&rid=35583","id":4381,"series":"未知","author":"李维明","category":"民间","status":"1","name":"农历一月二十日天穿日的由來"},{"playUrl":"http://leting.voicecloud.cn/Service/download.vc?appid=4fa74777&uid=1601119&rid=34859","id":4305,"series":"未知","author":"李维明","category":"童话","status":"1","name":"几张飘落的红叶"},{"playUrl":"http://leting.voicecloud.cn/Service/download.vc?appid=4fa74777&uid=1601119&rid=31510","id":3501,"series":"未知","author":"王玲","category":"童话","status":"1","name":"鼹鼠过圣诞节"},{"playUrl":"http://leting.voicecloud.cn/Service/download.vc?appid=4fa74777&uid=1601119&rid=30881","id":3203,"series":"未知","author":"王玲","category":"童话","status":"1","name":"被施魔法的公主"}]
     * title :
     * searchlevel :
     */

    private String title;
    private String searchlevel;
    /**
     * playUrl : http://leting.voicecloud.cn/Service/download.vc?appid=4fa74777&uid=1601119&rid=11919
     * id : 4780
     * series : 未知
     * author : 佚名
     * category : 成语
     * status : 1
     * name : 狼子野心
     */

    private List<ResultBean> result;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSearchlevel() {
        return searchlevel;
    }

    public void setSearchlevel(String searchlevel) {
        this.searchlevel = searchlevel;
    }

    public List<ResultBean> getResult() {
        return result;
    }

    public void setResult(List<ResultBean> result) {
        this.result = result;
    }

    public static class ResultBean {
        private String playUrl;
        private int id;
        private String series;
        private String author;
        private String category;
        private String status;
        private String name;

        public String getPlayUrl() {
            return playUrl;
        }

        public void setPlayUrl(String playUrl) {
            this.playUrl = playUrl;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getSeries() {
            return series;
        }

        public void setSeries(String series) {
            this.series = series;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
