package com.csjbot.snowbot.bean.aiui.entity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.android.core.entry.Static;
import com.android.core.util.StrUtil;
import com.csjbot.csjbase.event.IBus;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.GalleryActivity;
import com.csjbot.snowbot.activity.VideoRecordActivity;
import com.csjbot.snowbot.utils.SpeechStatus;
import com.csjbot.snowbot_rogue.Events.AIUIEvent;
import com.csjbot.snowbot_rogue.Events.EventsConstants;
import com.csjbot.snowbot_rogue.Events.ExpressionEvent;
import com.csjbot.snowbot_rogue.app.CsjSpeechSynthesizer;
import com.csjbot.snowbot_rogue.platform.SnowBotManager;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.iflytek.cloud.SpeechError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * 信息类语义结果，包括天气、火车和航班。
 *
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年8月18日 上午10:43:17
 */
public class OpenQAResult extends SemanticResult {
    private boolean cameraAction, photosAction;
    public static final String ACTION_STOP = "action_stop"; //停下
    public static final String ACTION_TAKE_PICTURE = "action_take_picture";//拍照
    public static final String ACTION_OPEN_PICTURES = "action_open_pictures";//查看相册

    private SnowBotManager snowBot = SnowBotManager.getInstance();

    public OpenQAResult(String service, JSONObject json) {
        super(service, json);
    }

    @Override
    public void handleResult(IBus iBus, Context context) {
        try {
            mContext = context;
            mIbus = iBus;
            cameraAction = false;
            photosAction = false;
            String robotAction = json.getJSONObject("answer").getString("text");
            if (StrUtil.isNotBlank(robotAction)) {
                handleQAResult(robotAction);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doAfterTTS() {

    }

    private void handleQAResult(String robotAction) {
        if (robotAction.equalsIgnoreCase(ACTION_STOP)) {
            snowBot.stopPartol();
        } else if (robotAction.equalsIgnoreCase(ACTION_TAKE_PICTURE)) {
            if (!activityIsOnTop(mContext, "com.csjbot.snowbot.activity.VideoRecordActivity")) {
                cameraAction = true;
                speechText(Static.CONTEXT.getResources().getString(R.string.open_camera));
            } else {
                cameraAction = true;
                speechText(Static.CONTEXT.getResources().getString(R.string.open_camera_finish));
            }
        } else if (robotAction.equalsIgnoreCase(ACTION_OPEN_PICTURES)) {
            if (!activityIsOnTop(mContext, "com.csjbot.snowbot.activity.GalleryActivity")) {
                photosAction = true;
                speechText(Static.CONTEXT.getResources().getString(R.string.open_photos));
            } else {
                speechText(Static.CONTEXT.getResources().getString(R.string.open_photos_finish));
            }
        } else {
            speechText(robotAction);
        }
    }

    /**
     * 判断当前activity
     *
     * @param context
     * @param activityName
     * @return
     */
    private boolean activityIsOnTop(Context context, String activityName) {
        if (context == null || TextUtils.isEmpty(activityName)) {
            return false;
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (activityName.equals(cpn.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 说完话后，判断应该做什么事情
     *
     * @param text
     */
    private void speechText(String text) {
        postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_ANSWERTEXT_DATA, text));
        Csjlogger.debug("text" + text);
        CsjSpeechSynthesizer.getSynthesizer().startSpeaking(text, new CsjSynthesizerListener() {
            @Override
            public void onSpeakBegin() {
                postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_SPEAK));
                SpeechStatus.getIstance().setSpeakFinished(false);
            }

            @Override
            public void onCompleted(SpeechError speechError) {
                postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_NORMAL));
                SpeechStatus.getIstance().setSpeakFinished(true);
                if (cameraAction) {
                    cameraAction(mContext);
                } else if (photosAction) {
                    photosAction(mContext);
                }
            }
        });
    }

    /**
     * 拍照
     *
     * @param context
     */
    private void cameraAction(Context context) {
        Csjlogger.info("cameraAction");
        Intent i = new Intent(context, VideoRecordActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("autoTakePhoto", true);
        context.startActivity(i);
    }

    /**
     * 打开相册
     *
     * @param context
     */
    private void photosAction(Context context) {
        Csjlogger.info("photosAction");
        Intent i = new Intent(context, GalleryActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}
