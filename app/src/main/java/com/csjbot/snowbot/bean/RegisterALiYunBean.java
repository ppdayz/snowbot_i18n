package com.csjbot.snowbot.bean;

import java.util.List;

/**
 * @author: jl
 * @Time: 2017/1/13
 * @Desc:
 */

public class RegisterALiYunBean {
    /**
     * data : {"service":"RegisterDevice","server":"iot","content":{"uid":"","product":"xiaoxue","device":{"createtime":"","productKey":"","productSecret":"","deviceKey":"","deviceId":"","deviceSecret":"","topics":[{"grant":"","ruleId":123,"topic":""}]}}}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * service : RegisterDevice
         * server : iot
         * content : {"uid":"","product":"xiaoxue","device":{"createtime":"","productKey":"","productSecret":"","deviceKey":"","deviceId":"","deviceSecret":"","topics":[{"grant":"","ruleId":123,"topic":""}]}}
         */

        private String service;
        private String server;
        private ContentBean content;

        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }

        public String getServer() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }

        public ContentBean getContent() {
            return content;
        }

        public void setContent(ContentBean content) {
            this.content = content;
        }

        public static class ContentBean {
            /**
             * uid :
             * product : xiaoxue
             * device : {"createtime":"","productKey":"","productSecret":"","deviceKey":"","deviceId":"","deviceSecret":"","topics":[{"grant":"","ruleId":123,"topic":""}]}
             */

            private String uid;
            private String product;
            private DeviceBean device;

            public String getUid() {
                return uid;
            }

            public void setUid(String uid) {
                this.uid = uid;
            }

            public String getProduct() {
                return product;
            }

            public void setProduct(String product) {
                this.product = product;
            }

            public DeviceBean getDevice() {
                return device;
            }

            public void setDevice(DeviceBean device) {
                this.device = device;
            }

            public static class DeviceBean {
                /**
                 * createtime :
                 * productKey :
                 * productSecret :
                 * deviceKey :
                 * deviceId :
                 * deviceSecret :
                 * topics : [{"grant":"","ruleId":123,"topic":""}]
                 */

                private String createtime;
                private String productKey;
                private String productSecret;
                private String deviceKey;
                private String deviceId;
                private String deviceSecret;
                private List<TopicsBean> topics;

                public String getCreatetime() {
                    return createtime;
                }

                public void setCreatetime(String createtime) {
                    this.createtime = createtime;
                }

                public String getProductKey() {
                    return productKey;
                }

                public void setProductKey(String productKey) {
                    this.productKey = productKey;
                }

                public String getProductSecret() {
                    return productSecret;
                }

                public void setProductSecret(String productSecret) {
                    this.productSecret = productSecret;
                }

                public String getDeviceKey() {
                    return deviceKey;
                }

                public void setDeviceKey(String deviceKey) {
                    this.deviceKey = deviceKey;
                }

                public String getDeviceId() {
                    return deviceId;
                }

                public void setDeviceId(String deviceId) {
                    this.deviceId = deviceId;
                }

                public String getDeviceSecret() {
                    return deviceSecret;
                }

                public void setDeviceSecret(String deviceSecret) {
                    this.deviceSecret = deviceSecret;
                }

                public List<TopicsBean> getTopics() {
                    return topics;
                }

                public void setTopics(List<TopicsBean> topics) {
                    this.topics = topics;
                }

                public static class TopicsBean {
                    /**
                     * grant :
                     * ruleId : 123
                     * topic :
                     */

                    private String grant;
                    private int ruleId;
                    private String topic;

                    public String getGrant() {
                        return grant;
                    }

                    public void setGrant(String grant) {
                        this.grant = grant;
                    }

                    public int getRuleId() {
                        return ruleId;
                    }

                    public void setRuleId(int ruleId) {
                        this.ruleId = ruleId;
                    }

                    public String getTopic() {
                        return topic;
                    }

                    public void setTopic(String topic) {
                        this.topic = topic;
                    }
                }
            }
        }
    }
}
