package com.csjbot.snowbot.bean;

import java.util.List;

/**
 * @author: jl
 * @Time: 2017/1/11
 * @Desc:
 */

public class FaceRecognitionData   {

    /**
     * code : 0
     * data : {"face":[{"glass":true,"expression":18,"face_shape":{"right_eye":[{"x":162,"y":117},{"x":158,"y":120},{"x":153,"y":121},{"x":148,"y":121},{"x":143,"y":120},{"x":147,"y":116},{"x":152,"y":114},{"x":157,"y":115}],"nose":[{"x":128,"y":154},{"x":124,"y":122},{"x":120,"y":131},{"x":116,"y":140},{"x":112,"y":150},{"x":106,"y":161},{"x":118,"y":165},{"x":128,"y":167},{"x":137,"y":164},{"x":147,"y":158},{"x":140,"y":148},{"x":135,"y":139},{"x":130,"y":131}],"face_profile":[{"x":55,"y":122},{"x":55,"y":137},{"x":57,"y":152},{"x":59,"y":166},{"x":63,"y":180},{"x":69,"y":193},{"x":78,"y":204},{"x":90,"y":214},{"x":102,"y":221},{"x":116,"y":226},{"x":130,"y":226},{"x":143,"y":223},{"x":154,"y":215},{"x":164,"y":205},{"x":172,"y":195},{"x":179,"y":183},{"x":183,"y":170},{"x":185,"y":156},{"x":186,"y":142},{"x":186,"y":129},{"x":185,"y":116}],"mouth":[{"x":102,"y":189},{"x":110,"y":194},{"x":118,"y":198},{"x":128,"y":199},{"x":137,"y":197},{"x":145,"y":193},{"x":152,"y":186},{"x":144,"y":184},{"x":135,"y":182},{"x":128,"y":184},{"x":120,"y":183},{"x":111,"y":185},{"x":111,"y":190},{"x":119,"y":190},{"x":128,"y":191},{"x":136,"y":190},{"x":144,"y":188},{"x":144,"y":187},{"x":136,"y":188},{"x":128,"y":189},{"x":119,"y":188},{"x":111,"y":188}],"left_eye":[{"x":82,"y":121},{"x":87,"y":123},{"x":92,"y":124},{"x":97,"y":123},{"x":101,"y":121},{"x":97,"y":117},{"x":92,"y":117},{"x":87,"y":118}],"right_eyebrow":[{"x":175,"y":103},{"x":165,"y":104},{"x":155,"y":104},{"x":145,"y":106},{"x":134,"y":106},{"x":143,"y":98},{"x":154,"y":95},{"x":165,"y":95}],"left_eyebrow":[{"x":68,"y":110},{"x":79,"y":109},{"x":89,"y":108},{"x":100,"y":109},{"x":111,"y":108},{"x":101,"y":101},{"x":89,"y":99},{"x":77,"y":101}]},"gender":98,"beauty":71,"roll":2,"yaw":5,"x":53,"width":144,"face_id":"1872057541357220096","y":76,"pitch":5,"age":23,"height":144}],"image_height":240,"session_id":"","image_width":240}
     * message : OK
     */

    private int code;
    private DataBean data;
    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class DataBean {
        /**
         * face : [{"glass":true,"expression":18,"face_shape":{"right_eye":[{"x":162,"y":117},{"x":158,"y":120},{"x":153,"y":121},{"x":148,"y":121},{"x":143,"y":120},{"x":147,"y":116},{"x":152,"y":114},{"x":157,"y":115}],"nose":[{"x":128,"y":154},{"x":124,"y":122},{"x":120,"y":131},{"x":116,"y":140},{"x":112,"y":150},{"x":106,"y":161},{"x":118,"y":165},{"x":128,"y":167},{"x":137,"y":164},{"x":147,"y":158},{"x":140,"y":148},{"x":135,"y":139},{"x":130,"y":131}],"face_profile":[{"x":55,"y":122},{"x":55,"y":137},{"x":57,"y":152},{"x":59,"y":166},{"x":63,"y":180},{"x":69,"y":193},{"x":78,"y":204},{"x":90,"y":214},{"x":102,"y":221},{"x":116,"y":226},{"x":130,"y":226},{"x":143,"y":223},{"x":154,"y":215},{"x":164,"y":205},{"x":172,"y":195},{"x":179,"y":183},{"x":183,"y":170},{"x":185,"y":156},{"x":186,"y":142},{"x":186,"y":129},{"x":185,"y":116}],"mouth":[{"x":102,"y":189},{"x":110,"y":194},{"x":118,"y":198},{"x":128,"y":199},{"x":137,"y":197},{"x":145,"y":193},{"x":152,"y":186},{"x":144,"y":184},{"x":135,"y":182},{"x":128,"y":184},{"x":120,"y":183},{"x":111,"y":185},{"x":111,"y":190},{"x":119,"y":190},{"x":128,"y":191},{"x":136,"y":190},{"x":144,"y":188},{"x":144,"y":187},{"x":136,"y":188},{"x":128,"y":189},{"x":119,"y":188},{"x":111,"y":188}],"left_eye":[{"x":82,"y":121},{"x":87,"y":123},{"x":92,"y":124},{"x":97,"y":123},{"x":101,"y":121},{"x":97,"y":117},{"x":92,"y":117},{"x":87,"y":118}],"right_eyebrow":[{"x":175,"y":103},{"x":165,"y":104},{"x":155,"y":104},{"x":145,"y":106},{"x":134,"y":106},{"x":143,"y":98},{"x":154,"y":95},{"x":165,"y":95}],"left_eyebrow":[{"x":68,"y":110},{"x":79,"y":109},{"x":89,"y":108},{"x":100,"y":109},{"x":111,"y":108},{"x":101,"y":101},{"x":89,"y":99},{"x":77,"y":101}]},"gender":98,"beauty":71,"roll":2,"yaw":5,"x":53,"width":144,"face_id":"1872057541357220096","y":76,"pitch":5,"age":23,"height":144}]
         * image_height : 240
         * session_id :
         * image_width : 240
         */

        private int image_height;
        private String session_id;
        private int image_width;
        private List<FaceBean> face;

        public int getImage_height() {
            return image_height;
        }

        public void setImage_height(int image_height) {
            this.image_height = image_height;
        }

        public String getSession_id() {
            return session_id;
        }

        public void setSession_id(String session_id) {
            this.session_id = session_id;
        }

        public int getImage_width() {
            return image_width;
        }

        public void setImage_width(int image_width) {
            this.image_width = image_width;
        }

        public List<FaceBean> getFace() {
            return face;
        }

        public void setFace(List<FaceBean> face) {
            this.face = face;
        }

        public static class FaceBean {
            /**
             * glass : true
             * expression : 18
             * face_shape : {"right_eye":[{"x":162,"y":117},{"x":158,"y":120},{"x":153,"y":121},{"x":148,"y":121},{"x":143,"y":120},{"x":147,"y":116},{"x":152,"y":114},{"x":157,"y":115}],"nose":[{"x":128,"y":154},{"x":124,"y":122},{"x":120,"y":131},{"x":116,"y":140},{"x":112,"y":150},{"x":106,"y":161},{"x":118,"y":165},{"x":128,"y":167},{"x":137,"y":164},{"x":147,"y":158},{"x":140,"y":148},{"x":135,"y":139},{"x":130,"y":131}],"face_profile":[{"x":55,"y":122},{"x":55,"y":137},{"x":57,"y":152},{"x":59,"y":166},{"x":63,"y":180},{"x":69,"y":193},{"x":78,"y":204},{"x":90,"y":214},{"x":102,"y":221},{"x":116,"y":226},{"x":130,"y":226},{"x":143,"y":223},{"x":154,"y":215},{"x":164,"y":205},{"x":172,"y":195},{"x":179,"y":183},{"x":183,"y":170},{"x":185,"y":156},{"x":186,"y":142},{"x":186,"y":129},{"x":185,"y":116}],"mouth":[{"x":102,"y":189},{"x":110,"y":194},{"x":118,"y":198},{"x":128,"y":199},{"x":137,"y":197},{"x":145,"y":193},{"x":152,"y":186},{"x":144,"y":184},{"x":135,"y":182},{"x":128,"y":184},{"x":120,"y":183},{"x":111,"y":185},{"x":111,"y":190},{"x":119,"y":190},{"x":128,"y":191},{"x":136,"y":190},{"x":144,"y":188},{"x":144,"y":187},{"x":136,"y":188},{"x":128,"y":189},{"x":119,"y":188},{"x":111,"y":188}],"left_eye":[{"x":82,"y":121},{"x":87,"y":123},{"x":92,"y":124},{"x":97,"y":123},{"x":101,"y":121},{"x":97,"y":117},{"x":92,"y":117},{"x":87,"y":118}],"right_eyebrow":[{"x":175,"y":103},{"x":165,"y":104},{"x":155,"y":104},{"x":145,"y":106},{"x":134,"y":106},{"x":143,"y":98},{"x":154,"y":95},{"x":165,"y":95}],"left_eyebrow":[{"x":68,"y":110},{"x":79,"y":109},{"x":89,"y":108},{"x":100,"y":109},{"x":111,"y":108},{"x":101,"y":101},{"x":89,"y":99},{"x":77,"y":101}]}
             * gender : 98
             * beauty : 71
             * roll : 2
             * yaw : 5
             * x : 53
             * width : 144
             * face_id : 1872057541357220096
             * y : 76
             * pitch : 5
             * age : 23
             * height : 144
             */

            private boolean glass;
            private int expression;
            private FaceShapeBean face_shape;
            private int gender;
            private int beauty;
            private int roll;
            private int yaw;
            private int x;
            private int width;
            private String face_id;
            private int y;
            private int pitch;
            private int age;
            private int height;

            public boolean isGlass() {
                return glass;
            }

            public void setGlass(boolean glass) {
                this.glass = glass;
            }

            public int getExpression() {
                return expression;
            }

            public void setExpression(int expression) {
                this.expression = expression;
            }

            public FaceShapeBean getFace_shape() {
                return face_shape;
            }

            public void setFace_shape(FaceShapeBean face_shape) {
                this.face_shape = face_shape;
            }

            public int getGender() {
                return gender;
            }

            public void setGender(int gender) {
                this.gender = gender;
            }

            public int getBeauty() {
                return beauty;
            }

            public void setBeauty(int beauty) {
                this.beauty = beauty;
            }

            public int getRoll() {
                return roll;
            }

            public void setRoll(int roll) {
                this.roll = roll;
            }

            public int getYaw() {
                return yaw;
            }

            public void setYaw(int yaw) {
                this.yaw = yaw;
            }

            public int getX() {
                return x;
            }

            public void setX(int x) {
                this.x = x;
            }

            public int getWidth() {
                return width;
            }

            public void setWidth(int width) {
                this.width = width;
            }

            public String getFace_id() {
                return face_id;
            }

            public void setFace_id(String face_id) {
                this.face_id = face_id;
            }

            public int getY() {
                return y;
            }

            public void setY(int y) {
                this.y = y;
            }

            public int getPitch() {
                return pitch;
            }

            public void setPitch(int pitch) {
                this.pitch = pitch;
            }

            public int getAge() {
                return age;
            }

            public void setAge(int age) {
                this.age = age;
            }

            public int getHeight() {
                return height;
            }

            public void setHeight(int height) {
                this.height = height;
            }

            public static class FaceShapeBean {
                private List<RightEyeBean> right_eye;
                private List<NoseBean> nose;
                private List<FaceProfileBean> face_profile;
                private List<MouthBean> mouth;
                private List<LeftEyeBean> left_eye;
                private List<RightEyebrowBean> right_eyebrow;
                private List<LeftEyebrowBean> left_eyebrow;

                public List<RightEyeBean> getRight_eye() {
                    return right_eye;
                }

                public void setRight_eye(List<RightEyeBean> right_eye) {
                    this.right_eye = right_eye;
                }

                public List<NoseBean> getNose() {
                    return nose;
                }

                public void setNose(List<NoseBean> nose) {
                    this.nose = nose;
                }

                public List<FaceProfileBean> getFace_profile() {
                    return face_profile;
                }

                public void setFace_profile(List<FaceProfileBean> face_profile) {
                    this.face_profile = face_profile;
                }

                public List<MouthBean> getMouth() {
                    return mouth;
                }

                public void setMouth(List<MouthBean> mouth) {
                    this.mouth = mouth;
                }

                public List<LeftEyeBean> getLeft_eye() {
                    return left_eye;
                }

                public void setLeft_eye(List<LeftEyeBean> left_eye) {
                    this.left_eye = left_eye;
                }

                public List<RightEyebrowBean> getRight_eyebrow() {
                    return right_eyebrow;
                }

                public void setRight_eyebrow(List<RightEyebrowBean> right_eyebrow) {
                    this.right_eyebrow = right_eyebrow;
                }

                public List<LeftEyebrowBean> getLeft_eyebrow() {
                    return left_eyebrow;
                }

                public void setLeft_eyebrow(List<LeftEyebrowBean> left_eyebrow) {
                    this.left_eyebrow = left_eyebrow;
                }

                public static class RightEyeBean {
                    /**
                     * x : 162
                     * y : 117
                     */

                    private int x;
                    private int y;

                    public int getX() {
                        return x;
                    }

                    public void setX(int x) {
                        this.x = x;
                    }

                    public int getY() {
                        return y;
                    }

                    public void setY(int y) {
                        this.y = y;
                    }
                }

                public static class NoseBean {
                    /**
                     * x : 128
                     * y : 154
                     */

                    private int x;
                    private int y;

                    public int getX() {
                        return x;
                    }

                    public void setX(int x) {
                        this.x = x;
                    }

                    public int getY() {
                        return y;
                    }

                    public void setY(int y) {
                        this.y = y;
                    }
                }

                public static class FaceProfileBean {
                    /**
                     * x : 55
                     * y : 122
                     */

                    private int x;
                    private int y;

                    public int getX() {
                        return x;
                    }

                    public void setX(int x) {
                        this.x = x;
                    }

                    public int getY() {
                        return y;
                    }

                    public void setY(int y) {
                        this.y = y;
                    }
                }

                public static class MouthBean {
                    /**
                     * x : 102
                     * y : 189
                     */

                    private int x;
                    private int y;

                    public int getX() {
                        return x;
                    }

                    public void setX(int x) {
                        this.x = x;
                    }

                    public int getY() {
                        return y;
                    }

                    public void setY(int y) {
                        this.y = y;
                    }
                }

                public static class LeftEyeBean {
                    /**
                     * x : 82
                     * y : 121
                     */

                    private int x;
                    private int y;

                    public int getX() {
                        return x;
                    }

                    public void setX(int x) {
                        this.x = x;
                    }

                    public int getY() {
                        return y;
                    }

                    public void setY(int y) {
                        this.y = y;
                    }
                }

                public static class RightEyebrowBean {
                    /**
                     * x : 175
                     * y : 103
                     */

                    private int x;
                    private int y;

                    public int getX() {
                        return x;
                    }

                    public void setX(int x) {
                        this.x = x;
                    }

                    public int getY() {
                        return y;
                    }

                    public void setY(int y) {
                        this.y = y;
                    }
                }

                public static class LeftEyebrowBean {
                    /**
                     * x : 68
                     * y : 110
                     */

                    private int x;
                    private int y;

                    public int getX() {
                        return x;
                    }

                    public void setX(int x) {
                        this.x = x;
                    }

                    public int getY() {
                        return y;
                    }

                    public void setY(int y) {
                        this.y = y;
                    }
                }
            }
        }
    }
}
