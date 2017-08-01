package com.csjbot.snowbot.utils;

import com.csjbot.snowbot.bean.Dance;
import com.csjbot.snowbot_rogue.platform.SnowBotManager;
import com.csjbot.snowbot_rogue.servers.slams.SnowBotMoveServer;
import com.slamtec.slamware.action.MoveDirection;

/**
 * Created by Administrator on 2017/3/23 0023.
 */

public class DanceUtil {
    public static void LeftCircle(Dance dance) {
        SnowBotMoveServer.getInstance().turnRound(MoveDirection.TURN_LEFT, dance.getLeftCircle());
    }

    public static void RightCircle(Dance dance) {
        SnowBotMoveServer.getInstance().turnRound(MoveDirection.TURN_RIGHT, dance.getRtghtCircle());
    }

    public static void LeftHand(Dance dance) {
        SnowBotManager.getInstance().swingLeftArm((byte) dance.getLeftHand());
    }

    public static void RightHand(Dance dance) {
        SnowBotManager.getInstance().swingRightArm((byte) dance.getRightHand());
    }

    public static void LRHand(Dance dance) {
        SnowBotManager.getInstance().swingDoubleArm((byte) dance.getTotalHand());
    }

    public static void StopDance() {
        SnowBotMoveServer.getInstance().cancelAction();
    }
}
