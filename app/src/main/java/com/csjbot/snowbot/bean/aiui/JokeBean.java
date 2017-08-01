package com.csjbot.snowbot.bean.aiui;

import java.util.List;

/**
 * Created by Administrator on 2017/1/4 0004.
 */

public class JokeBean {

    /**
     * result : [{"id":26,"author":"笑话大王","title":"搭卖","category":"娱乐","subCategory":"内涵段子","tag":"娱乐;幽默笑话;搞笑;笑话大王;笑话锦集第一季","albumUrl":"http://www.ximalaya.com/1000558/album/2677710","album":"笑话锦集","mp4Url":"http://audio.xmcdn.com/group8/M01/4D/F4/wKgDYFWyMvjRIMRgAAIAsvmwmaE558.m4a","mp3Url":"http://audio.xmcdn.com/group3/M06/D8/77/wKgDsVLjQ0mSCw-PAAUvTReyB1c465.mp3"},{"id":93,"author":"笑话大王","title":"自讨没趣","category":"娱乐","subCategory":"内涵段子","tag":"冷笑话;微博;笑话大王","albumUrl":"http://www.ximalaya.com/1000558/album/2677704","album":"微博冷笑话","mp4Url":"http://audio.xmcdn.com/group15/M05/4F/4F/wKgDaFWy7vGxtqv1AAJObiE2Rco924.m4a","mp3Url":"http://audio.xmcdn.com/group3/M05/D3/1E/wKgDsVLfwK_C30buAAX8hA7yNmQ162.mp3"},{"id":97,"author":"笑话大王","title":"老外买鸡","category":"娱乐","subCategory":"内涵段子","tag":"冷笑话;微博;笑话大王","albumUrl":"http://www.ximalaya.com/1000558/album/2677704","album":"微博冷笑话","mp4Url":"http://audio.xmcdn.com/group16/M00/43/86/wKgDalWlBJSxj41UAAFmk_pJbnY189.m4a","mp3Url":"http://audio.xmcdn.com/group3/M06/D2/99/wKgDslLfsp3Cm5k5AAOZ59SqIlY934.mp3"},{"id":50,"author":"笑话大王","title":"抄袭者","category":"娱乐","subCategory":"内涵段子","tag":"冷笑话;微博;笑话大王","albumUrl":"http://www.ximalaya.com/1000558/album/2677704","album":"微博冷笑话","mp4Url":"http://audio.xmcdn.com/group9/M00/48/F5/wKgDZlWs2_XR5A3EAAD-gCJQ3Oo050.m4a","mp3Url":"http://audio.xmcdn.com/group3/M05/D2/2C/wKgDsVLfiWSyXsDTAAKHdD6jU6k437.mp3"},{"id":70,"author":"笑话大王","title":"主任演讲","category":"娱乐","subCategory":"内涵段子","tag":"冷笑话;微博;笑话大王","albumUrl":"http://www.ximalaya.com/1000558/album/2677704","album":"微博冷笑话","mp4Url":"http://audio.xmcdn.com/group11/M00/22/45/wKgDbVV9pkDj8_MLAAG7SyrJ0RA872.m4a","mp3Url":"http://audio.xmcdn.com/group3/M05/CD/85/wKgDsVLd05biqi1rAAR4maGXscQ198.mp3"}]
     * title :
     */

    private String title;
    /**
     * id : 26
     * author : 笑话大王
     * title : 搭卖
     * category : 娱乐
     * subCategory : 内涵段子
     * tag : 娱乐;幽默笑话;搞笑;笑话大王;笑话锦集第一季
     * albumUrl : http://www.ximalaya.com/1000558/album/2677710
     * album : 笑话锦集
     * mp4Url : http://audio.xmcdn.com/group8/M01/4D/F4/wKgDYFWyMvjRIMRgAAIAsvmwmaE558.m4a
     * mp3Url : http://audio.xmcdn.com/group3/M06/D8/77/wKgDsVLjQ0mSCw-PAAUvTReyB1c465.mp3
     */

    private List<ResultBean> result;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ResultBean> getResult() {
        return result;
    }

    public void setResult(List<ResultBean> result) {
        this.result = result;
    }

    public static class ResultBean {
        private int id;
        private String author;
        private String title;
        private String category;
        private String subCategory;
        private String tag;
        private String albumUrl;
        private String album;
        private String mp4Url;
        private String mp3Url;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getSubCategory() {
            return subCategory;
        }

        public void setSubCategory(String subCategory) {
            this.subCategory = subCategory;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getAlbumUrl() {
            return albumUrl;
        }

        public void setAlbumUrl(String albumUrl) {
            this.albumUrl = albumUrl;
        }

        public String getAlbum() {
            return album;
        }

        public void setAlbum(String album) {
            this.album = album;
        }

        public String getMp4Url() {
            return mp4Url;
        }

        public void setMp4Url(String mp4Url) {
            this.mp4Url = mp4Url;
        }

        public String getMp3Url() {
            return mp3Url;
        }

        public void setMp3Url(String mp3Url) {
            this.mp3Url = mp3Url;
        }
    }
}
