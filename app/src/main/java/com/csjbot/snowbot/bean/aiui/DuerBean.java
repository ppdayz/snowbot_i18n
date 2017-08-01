package com.csjbot.snowbot.bean.aiui;

import java.util.List;

/**
 * Copyright (c) 2017, SuZhou CsjBot. All Rights Reserved.
 * www.csjbot.com
 * <p>
 * Created by 浦耀宗 at 2017/06/10 0010-14:03.
 * Email: puyz@csjbot.com
 */

public class DuerBean {


    /**
     * result : {"nlu":{"domain":"universal_search","intent":"kg","slots":{}},"bot_id":"aries_general","bot_meta":{"version":"1.0.0","type":"其他","description":"desc"},"views":[{"type":"image","list":[]}],"speech":{"type":"Text","content":"世界上面积最大的国家是俄罗斯，面积为约1709.82万平方千米。"}}
     * id : 1497073940_112g4cbmh
     * logid : 14970739398436
     * user_id : DFBE9EC67F32457F29E558B57AA5FB61|0
     * time : 1497073940
     * cuid : DFBE9EC67F32457F29E558B57AA5FB61|0
     * se_query : 最大的国家
     * msg : ok
     * timeuse : 524
     * client_msg_id : 402f48a0-1c96-4bfe-bb54-eba370b5440e
     * status : 0
     */

    private ResultBean result;
    private String id;
    private String logid;
    private String user_id;
    private int time;
    private String cuid;
    private String se_query;
    private String msg;
    private int timeuse;
    private String client_msg_id;
    private int status;

    public ResultBean getResult() {
        return result;
    }

    public void setResult(ResultBean result) {
        this.result = result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLogid() {
        return logid;
    }

    public void setLogid(String logid) {
        this.logid = logid;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getCuid() {
        return cuid;
    }

    public void setCuid(String cuid) {
        this.cuid = cuid;
    }

    public String getSe_query() {
        return se_query;
    }

    public void setSe_query(String se_query) {
        this.se_query = se_query;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getTimeuse() {
        return timeuse;
    }

    public void setTimeuse(int timeuse) {
        this.timeuse = timeuse;
    }

    public String getClient_msg_id() {
        return client_msg_id;
    }

    public void setClient_msg_id(String client_msg_id) {
        this.client_msg_id = client_msg_id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public static class ResultBean {
        /**
         * nlu : {"domain":"universal_search","intent":"kg","slots":{}}
         * bot_id : aries_general
         * bot_meta : {"version":"1.0.0","type":"其他","description":"desc"}
         * views : [{"type":"image","list":[]}]
         * speech : {"type":"Text","content":"世界上面积最大的国家是俄罗斯，面积为约1709.82万平方千米。"}
         */

        private NluBean nlu;
        private String bot_id;
        private BotMetaBean bot_meta;
        private SpeechBean speech;
        private List<ViewsBean> views;

        public NluBean getNlu() {
            return nlu;
        }

        public void setNlu(NluBean nlu) {
            this.nlu = nlu;
        }

        public String getBot_id() {
            return bot_id;
        }

        public void setBot_id(String bot_id) {
            this.bot_id = bot_id;
        }

        public BotMetaBean getBot_meta() {
            return bot_meta;
        }

        public void setBot_meta(BotMetaBean bot_meta) {
            this.bot_meta = bot_meta;
        }

        public SpeechBean getSpeech() {
            return speech;
        }

        public void setSpeech(SpeechBean speech) {
            this.speech = speech;
        }

        public List<ViewsBean> getViews() {
            return views;
        }

        public void setViews(List<ViewsBean> views) {
            this.views = views;
        }

        public static class NluBean {
            /**
             * domain : universal_search
             * intent : kg
             * slots : {}
             */

            private String domain;
            private String intent;
            private SlotsBean slots;

            public String getDomain() {
                return domain;
            }

            public void setDomain(String domain) {
                this.domain = domain;
            }

            public String getIntent() {
                return intent;
            }

            public void setIntent(String intent) {
                this.intent = intent;
            }

            public SlotsBean getSlots() {
                return slots;
            }

            public void setSlots(SlotsBean slots) {
                this.slots = slots;
            }

            public static class SlotsBean {
            }
        }

        public static class BotMetaBean {
            /**
             * version : 1.0.0
             * type : 其他
             * description : desc
             */

            private String version;
            private String type;
            private String description;

            public String getVersion() {
                return version;
            }

            public void setVersion(String version) {
                this.version = version;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getDescription() {
                return description;
            }

            public void setDescription(String description) {
                this.description = description;
            }
        }

        public static class SpeechBean {
            /**
             * type : Text
             * content : 世界上面积最大的国家是俄罗斯，面积为约1709.82万平方千米。
             */

            private String type;
            private String content;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }
        }

        public static class ViewsBean {
            /**
             * type : image
             * list : []
             */

            private String type;
            private List<?> list;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public List<?> getList() {
                return list;
            }

            public void setList(List<?> list) {
                this.list = list;
            }
        }
    }
}
