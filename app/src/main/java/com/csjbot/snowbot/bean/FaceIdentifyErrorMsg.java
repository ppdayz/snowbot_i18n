package com.csjbot.snowbot.bean;

import com.csjbot.snowbot.R;

/**
 * Copyright (c) 2017, SuZhou CsjBot. All Rights Reserved.
 * www.csjbot.com
 * <p>
 * Created by 浦耀宗 at 2017/02/09 0009-14:06.
 * Email: puyz@csjbot.com
 */

public enum FaceIdentifyErrorMsg {
    ErrorCode1101(-1101, R.string.facecode1101),
    ErrorCode1102(-1102, R.string.facecode1102),
    ErrorCode1103(-1103, R.string.facecode1103),
    ErrorCode1104(-1104, R.string.facecode1104),
    ErrorCode1105(-1105, R.string.facecode1105),
    ErrorCode1106(-1106, R.string.facecode1106),
    ErrorCode1107(-1107, R.string.facecode1107),
    ErrorCode1108(-1108, R.string.facecode1108),
    ErrorCode1109(-1109, R.string.facecode1109),
    ErrorCode1200(-1200, R.string.facecode1200),
    ErrorCode1300(-1300, R.string.facecode1300),
    ErrorCode1301(-1301, R.string.facecode1301),
    ErrorCode1302(-1302, R.string.facecode1302),
    ErrorCode1303(-1303, R.string.facecode1303),
    ErrorCode1305(-1305, R.string.facecode1305),
    ErrorCode1304(-1304, R.string.facecode1304),
    ErrorCode1306(-1306, R.string.facecode1306),
    ErrorCode1307(-1307, R.string.facecode1307),
    ErrorCode1308(-1308, R.string.facecode1308),
    ErrorCode1309(-1309, R.string.facecode1309),
    ErrorCode1310(-1310, R.string.facecode1310),
    ErrorCode1311(-1311, R.string.facecode1311),
    ErrorCode1312(-1312, R.string.facecode1312),
    ErrorCode1400(-1400, R.string.facecode1400),
    ErrorCode1403(-1403,R.string.facecode1403),
    ErrorCode2001(-2001,R.string.facecode2001),
    ErrorCode2017(-2017,R.string.facecode2017);

    private FaceIdentifyErrorMsg(int errodCode, int errodMsg) {
        this.errodCode = errodCode;
        this.errodMsg = errodMsg;
    }

    int errodCode;
    int errodMsg;

    public static int getErrorString(int errodCode) {
        for (FaceIdentifyErrorMsg errorMsg : FaceIdentifyErrorMsg.values()) {
            if (errodCode == errorMsg.errodCode) {
                return errorMsg.errodMsg;
            }
        }
        return R.string.facecodeother;
    }
}
