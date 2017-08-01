package com.csjbot.snowbot.bean.aiui.entity;

import android.content.Context;

import com.csjbot.csjbase.event.IBus;
import com.csjbot.snowbot_rogue.Events.ExpressionEvent;
import com.csjbot.snowbot.bean.aiui.SimilarityUtil;
import com.csjbot.snowbot_rogue.platform.SnowBotManager;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.slamtec.slamware.action.MoveDirection;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

/**
 * Created by Administrator on 2016/11/28 0028.
 */

public class RobotActionResult extends SemanticResult {
    private String robotAction;
    private String direction;
    private String expressMode;
    private SnowBotManager snowBot = SnowBotManager.getInstance();
    private String angle;
    private SimilarityUtil similarityUtil = new SimilarityUtil();

    public RobotActionResult(String service, JSONObject json) {
        super(service, json);
    }

    @Override
    public void handleResult(IBus iBus, Context context) {
        try {
            mContext = context;
            mIbus = iBus;
            robotAction = json.getString("operation");
            if (robotAction.equals("TRUN")) {          //转身
                direction = json.getJSONObject("semantic").getJSONObject("slots").getString("direction");
                switch (direction) {
                    case "front":
                        if (json.getJSONObject("semantic").getJSONObject("slots").has("angle")) {
                            angle = json.getJSONObject("semantic").getJSONObject("slots").getString("angle");
                            if (similarityUtil.isNumber(angle)) {
                                snowBot.turnRound(Short.parseShort(angle));
                            } else {
                                snowBot.turnRound((short) similarityUtil.numberParser(angle));
                            }
                        } else {
                            snowBot.turnRound(new Random().nextBoolean() ? (short) 180 : (short) -180);
                        }
                        break;
                    case "left":
                        if (json.getJSONObject("semantic").getJSONObject("slots").has("angle")) {
                            angle = json.getJSONObject("semantic").getJSONObject("slots").getString("angle");
                            if (similarityUtil.isNumber(angle)) {
                                snowBot.turnRound(Short.parseShort(angle));
                            } else {
                                snowBot.turnRound((short) similarityUtil.numberParser(angle));
                            }
                        } else {
                            snowBot.turnRound((short) 90);
                        }
                        break;
                    case "right":
                        if (json.getJSONObject("semantic").getJSONObject("slots").has("angle")) {
                            angle = json.getJSONObject("semantic").getJSONObject("slots").getString("angle");
                            if (similarityUtil.isNumber(angle)) {
                                int angleRight = -Integer.parseInt(angle);
                                snowBot.turnRound((short) angleRight);
                            } else {
                                snowBot.turnRound((short) -similarityUtil.numberParser(angle));
                            }
                        } else {
                            snowBot.turnRound((short) -90);
                        }
                        break;
                    case "back":
                        if (json.getJSONObject("semantic").getJSONObject("slots").has("angle")) {
                            angle = json.getJSONObject("semantic").getJSONObject("slots").getString("angle");
                            if (similarityUtil.isNumber(angle)) {
                                snowBot.turnRound(Short.parseShort(angle));
                            } else {
                                snowBot.turnRound((short) similarityUtil.numberParser(angle));
                            }
                        } else {
                            snowBot.turnRound(new Random().nextBoolean() ? (short) 180 : (short) -180);
                        }
                        break;
                    default:
                        break;
                }
            } else if (robotAction.equals("WALK")) {       //行走
                direction = json.getJSONObject("semantic").getJSONObject("slots").getString("direction");
                switch (direction) {
                    case "front":
                        if (json.getJSONObject("semantic").getJSONObject("slots").has("steps")) {
                            int steps = json.getJSONObject("semantic").getJSONObject("slots").getInt("steps");
                            goForwardAndBack(MoveDirection.FORWARD, steps);
                        } else {
                            goForwardAndBack(MoveDirection.FORWARD, 3);
                        }
                        break;
                    case "left":
                        break;
                    case "right":
                        break;
                    case "back":
                        if (json.getJSONObject("semantic").getJSONObject("slots").has("steps")) {
                            int steps = json.getJSONObject("semantic").getJSONObject("slots").getInt("steps");
                            goForwardAndBack(MoveDirection.BACKWARD, steps);
                        } else {
                            goForwardAndBack(MoveDirection.BACKWARD, 3);
                        }
                        break;
                    default:
                        break;
                }

            } else if (robotAction.equals("EXPRESS")) {
                expressMode = json.getJSONObject("semantic").getJSONObject("slots").getString("mode");
                switch (expressMode) {
                    case "cute":
                        postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_SHY));
                        break;
                    case "cry":
                        postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_SORROW));
                        break;
                    case "smile":
                        postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_HAPPY));
                        break;
                    case "wink":
                        postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_NORMAL));
                        break;
                    default:
                        break;
                }
            } else if (robotAction.equals("NEAR")) {
                goForwardAndBack(MoveDirection.FORWARD, 3);
            } else if (robotAction.equals("WAVE")) {
                //挥手
                snowBot.swingDoubleArm((byte) 0x04);
            } else if (robotAction.equals("HANDSUP")) {
                direction = json.getJSONObject("semantic").getJSONObject("slots").getString("direction");
                switch (direction) {
                    case "left":
                        snowBot.swingLeftArm((byte) 0x04);
                        break;
                    case "right":
                        snowBot.swingRightArm((byte) 0x04);
                        break;
                    case "double":
                        snowBot.swingDoubleArm((byte) 0x04);
                    default:
                        break;
                }
            } else if (robotAction.equals("DANCE")) {
                //跳舞
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doAfterTTS() {

    }

    private void goForwardAndBack(final MoveDirection direction, final int steps) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (i < 2 * steps) {
                    snowBot.moveBy(direction);
                    i++;
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

}
