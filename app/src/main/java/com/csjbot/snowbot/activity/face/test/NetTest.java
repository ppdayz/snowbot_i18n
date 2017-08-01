package com.csjbot.snowbot.activity.face.test;

import java.io.File;

import dou.utils.DLog;
import mobile.ReadFace.net.ApiListener;
import mobile.ReadFace.net.NetFaceTrack;

/**
 * Created by mac on 2017/1/6 下午4:23.
 */

public class NetTest {

    /**
     * api_id:f6f96cec55e5a9823c6115ddcad6ff80
     * api_secret:cc4c90231dd9e6b970fdd8d385e7a782b2bd9f59
     * face_id:1ab80cbe585bc0a80bd4773d432ce56c
     * group_id:38522d53b96bb9949a67ff8399c78f2a
     */

    static ApiListener listener = new ApiListener() {
        @Override
        public void onError(String s) {
            DLog.d(s);
        }

        @Override
        public void onCompleted(String s) {
            DLog.d(s);
        }
    };
    static NetFaceTrack netFaceTrack = NetFaceTrack.getInstance("http://121.42.141.249:8011/","5042a421fb4f1324ba7948370559f03e","98b898104646a8c88345fadbbeeef8b360e69c58");
    public static void testDetection(String file) {
        netFaceTrack.faceDetaction(new File(file), "", listener);
    }

    public static void testfaceVerificationFace(String face_id, String face_id2) {
        netFaceTrack.faceVerificationFace(face_id, face_id2, listener);
    }

    public static void testfaceVerificationPerson(String face_id, String person_id) {
        netFaceTrack.faceVerificationPerson(face_id, person_id, listener);
    }


    public static void testCreatePeople(String face_id, String name) {
        netFaceTrack.peopleCreate(face_id, name, listener);
    }

    public static void testPeopleAddFace(String person_id, String face_id) {
        netFaceTrack.peopleAddFace(person_id, face_id, listener);
    }

    public static void testPeopleEmpty(String person_id) {
        netFaceTrack.peopleEmpty(person_id, listener);
    }

    public static void testPeopleDelete(String person_id) {
        netFaceTrack.peopleDelete(person_id, listener);
    }

    public static void testPeopleRemoveFace(String person_id, String face_id) {
        netFaceTrack.peopleRemoveFace(person_id, face_id, listener);
    }


    public static void testfaceIdentification(String face_id, String group_id) {
        netFaceTrack.faceIdentification(face_id, group_id, listener);
    }


    public static void testGroupCreate(String person_id, String name) {
        netFaceTrack.groupsCreate(person_id, name, listener);
    }

    public static void testGroupAddPerson(String group_id, String person_id) {
        netFaceTrack.groupsAddPerson(group_id, person_id, listener);
    }

    public static void testGroupRemovePerson(String group_id, String person_id) {
        netFaceTrack.groupsRemovePerson(group_id, person_id, listener);
    }

    public static void testgroupsEmpty(String group_id, String person_id) {
        netFaceTrack.groupsEmpty(group_id, listener);
    }

    public static void testgroupsDelete(String group_id, String person_id) {
        netFaceTrack.groupsDelete(group_id, listener);
    }


}
