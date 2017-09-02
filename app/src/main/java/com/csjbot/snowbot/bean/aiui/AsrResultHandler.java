package com.csjbot.snowbot.bean.aiui;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.android.core.entry.Static;
import com.csjbot.csjbase.event.IBus;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.GalleryActivity;
import com.csjbot.snowbot.activity.VideoRecordActivity;
import com.csjbot.snowbot.bean.aiui.AlertingTone.VolumeManager;
import com.csjbot.snowbot.bean.aiui.entity.CsjSynthesizerListener;
import com.csjbot.snowbot.services.CsjSpeechSynthesizer2;
import com.csjbot.snowbot_rogue.Events.AIUIEvent;
import com.csjbot.snowbot_rogue.Events.EventsConstants;
import com.csjbot.snowbot_rogue.Events.ExpressionEvent;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.iflytek.cloud.SpeechError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * 语法结果处理类。
 *
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年9月27日 下午7:05:36
 */
public class AsrResultHandler {
    private static final int MSG_ASR_RESULT = 1;

    /*离线音乐控制语义id*/
    public final static int MUSIC_LAST = 1001;
    public final static int MUSIC_NEXT = 1002;
    public final static int MUSIC_PAUSE = 1003;
    public final static int MUSIC_PLAY = 1004;
    public final static int VOLUNE_UP = 1005;
    public final static int VOLUME_DOWN = 1006;
    public final static int MUSIC_REPLAY = 1007;

    /*离线休眠语义id*/
    public final static int ID_SLEEP = 2001;

    /*拍照id*/
    public final static int OPEN_CAMERA = 4001;

    /*相册控制id*/
    public final static int OPEN_PHOTOS = 4002;

    /*带我去/我想去/去*/
    public final static int WALK_ACTION = 5001;

    private Context mContext;
    protected IBus mIbus = null;

    private String homeName;

    private HandlerThread mAsrHandlerThread;

    private ResultHandler mResultHandler;

    private VolumeManager mVolumeManager;

    public AsrResultHandler(Context context, IBus iBus) {
        mContext = context;
        mIbus = iBus;
        mAsrHandlerThread = new HandlerThread("AsrResultThread");
        mAsrHandlerThread.start();
        mResultHandler = new ResultHandler(mAsrHandlerThread.getLooper());

        if (mVolumeManager == null) {
            mVolumeManager = VolumeManager.getInstance(mContext);
        }
    }

    public void handleResult(JSONObject AsrResult) {
        if (AsrResult.length() == 0) return;

        int commandId = parseAsrResult(AsrResult);
        mResultHandler.removeMessages(MSG_ASR_RESULT);
        Message.obtain(mResultHandler, MSG_ASR_RESULT, commandId, 0).sendToTarget();
    }

    /**
     * 解析离线语法识别结果并返回id。
     *
     * @param resultJson
     * @return id
     */
    private int parseAsrResult(JSONObject resultJson) {
        int id = 0;

        if (null != resultJson) {
            try {
                if (resultJson.getInt("rc") == 4) {
                    id = -1;
                } else {
                    JSONObject ws = (JSONObject) resultJson.getJSONArray("ws").get(0);
                    JSONObject cw = (JSONObject) ws.getJSONArray("cw").get(0);

                    id = Integer.valueOf(cw.getString("id"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                id = 0;
            }
        }

        return id;
    }

    public void postEvent(IBus.IEvent event) {
        if (mIbus != null) {
            Csjlogger.debug("postEvent  " + event.getTag());
            mIbus.post(event);
        }
    }

    public void destroy() {
        mAsrHandlerThread.quit();
        mResultHandler = null;
        if (mIbus != null) {
            mIbus.unregister(this);
        }
    }

    class ResultHandler extends Handler {
        private boolean cameraAction, photosAction;

        public ResultHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            cameraAction = false;
            photosAction = false;
            boolean needInterrupt = false;
            if (msg.what == MSG_ASR_RESULT) {
                switch (msg.arg1) {
                    case MUSIC_LAST:
//					PlayController.getInstance(mContext).onMusicCommand("", InsType.PAST);;
                        break;
                    case OPEN_CAMERA:
                        needInterrupt = true;
                        if (!activityIsOnTop(mContext, "com.csjbot.snowbot.activity.VideoRecordActivity")) {
                            cameraAction = true;
                            speechText(Static.CONTEXT.getResources().getString(R.string.open_camera));
                        } else {
                            cameraAction(mContext);
                        }
                        break;
                    case OPEN_PHOTOS:
                        needInterrupt = true;
                        if (!activityIsOnTop(mContext, "com.csjbot.snowbot.activity.VideoRecordActivity")) {
                            photosAction = true;
                            speechText(Static.CONTEXT.getResources().getString(R.string.open_photos));
                        } else {
                            speechText(Static.CONTEXT.getResources().getString(R.string.open_photos_finish));
                        }
                        break;
                    case WALK_ACTION:
//                        goWhere(msg.);
                        break;
                    default:
                        break;
                }
                if (needInterrupt) {
                    postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_EVENT_OTHER_CMD));
                }
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
            CsjSpeechSynthesizer2.getSynthesizer().startSpeaking(text, new CsjSynthesizerListener() {
                @Override
                public void onSpeakBegin() {
                    postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_SPEAK));
                }

                @Override
                public void onCompleted(SpeechError speechError) {
                    postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_NORMAL));
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
            Intent i = new Intent(context, GalleryActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}
