package com.csjbot.snowbot.bean.aiui.entity;

import android.content.Context;

import com.csjbot.csjbase.event.IBus;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.bean.aiui.AlertingTone.TonePlayer;
import com.csjbot.snowbot.bean.aiui.AlertingTone.VolumeManager;
import com.csjbot.snowbot_rogue.Events.AIUIEvent;
import com.csjbot.snowbot_rogue.Events.EventsConstants;
import com.csjbot.snowbot_rogue.utils.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 音乐语义结果。
 *
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年8月18日 上午10:44:36
 */
public class CMDResult extends SemanticResult {
    private final static String TAG = "MusicResult";

    private enum INSTYPE {
        // 不要问我为什么，讯飞写的，照抄过来的
        NEXT,
        PAST,
        REPLAY, // 继续播放
        REPEAT, // 重新播放
        PAUSE,
        LOOP,   // 列表循环
        CYCLE,  // 单曲循环
        VOLMAX,
        VOLMID,
        VOLMIN,
        VOLMINUS,
        VOLPLUS,
        INSTYPE_REPLAYANSWER
    }

    private static Map<String, INSTYPE> insTypeMap;

    static {
        insTypeMap = new HashMap<>();
        insTypeMap.put(Constant.Music.INSTYPE_NEXT, INSTYPE.NEXT);
        insTypeMap.put(Constant.Music.INSTYPE_PAST, INSTYPE.PAST);
        insTypeMap.put(Constant.Music.INSTYPE_REPLAY, INSTYPE.REPLAY);
        insTypeMap.put(Constant.Music.INSTYPE_REPEAT, INSTYPE.REPEAT);
        insTypeMap.put(Constant.Music.INSTYPE_PAUSE, INSTYPE.PAUSE);
        insTypeMap.put(Constant.Music.INSTYPE_LOOP, INSTYPE.LOOP);
        insTypeMap.put(Constant.Music.INSTYPE_CYCLE, INSTYPE.CYCLE);
        insTypeMap.put(Constant.Music.INSTYPE_VOLMAX, INSTYPE.VOLMAX);
        insTypeMap.put(Constant.Music.INSTYPE_VOLMID, INSTYPE.VOLMID);
        insTypeMap.put(Constant.Music.INSTYPE_VOLMIN, INSTYPE.VOLMIN);
        insTypeMap.put(Constant.Music.INSTYPE_VOLMINUS, INSTYPE.VOLMINUS);
        insTypeMap.put(Constant.Music.INSTYPE_VOLPLUS, INSTYPE.VOLPLUS);
        insTypeMap.put(Constant.Music.INSTYPE_REPLAYANSWER, INSTYPE.INSTYPE_REPLAYANSWER);
    }

    private VolumeManager mVolumeManager;

    private int currentVolume = -1;

    public boolean isSleepType() {
        return isSleepType;
    }

    private boolean isSleepType = false;

    // 用于标示是否音量增减
    private boolean isIncrease = false;

    public CMDResult(String service, JSONObject json) {
        super(service, json);
    }

    public String getInsType() {
        String operation = null;
        try {
            operation = json.getString(Constant.Music.KEY_OPERATION);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        if (Constant.Music.OPERATION_INS.equals(operation)) {
            // 控制语义
            try {
                JSONObject semantic = json.getJSONObject(Constant.Music.KEY_SEMANTIC);
                String insType = semantic.getJSONObject(Constant.Music.KEY_SLOTS).getString(Constant.Music.KEY_INSTYPE);

                return insType;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return "";
    }

    /**
     * 播放音量增减提示音
     */
    public void palyPromptTone() {
        // 连续播放三次提示音
        Csjlogger.debug(TAG, "playUseSoundPool");

        if (null != mVolumeManager) {
            currentVolume = mVolumeManager.getCurrentVolume();
//            mVolumeManager.setMaxVolume();
        }

        TonePlayer.playUseSoundPool(mContext, Constant.VOLUME_PATH, 3);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (null != mVolumeManager) {
                    if (isIncrease) {
                        mVolumeManager.setVolume(currentVolume + 2);
                    } else {
                        mVolumeManager.setVolume(currentVolume - 2);
                    }
                }

                TonePlayer.playUseSoundPool(mContext, Constant.VOLUME_PATH, 3);
                Csjlogger.debug(TAG, "playUseSoundPool");
            }
        }).start();


//        new Handler().postDelayed(new Runnable() {
//
//            @Override
//            public void run() {
//                if (null != mVolumeManager) {
//                    if (isIncrease) {
//                        mVolumeManager.setVolume(currentVolume + 2);
//                    } else {
//                        mVolumeManager.setVolume(currentVolume - 2);
//                    }
//                }
//
//                // 连续播放三次提示音
//                TonePlayer.playUseSoundPool(mContext, Constant.VOLUME_PATH, 3);
//                Csjlogger.debug(TAG, "playUseSoundPool");
//            }
//        }, 500);
    }

    @Override
    public void handleResult(IBus iBus, Context context) {

        try {
            mIbus = iBus;
            mContext = context;
            if (mVolumeManager == null) {
                mVolumeManager = VolumeManager.getInstance(mContext);
            }

            // 音乐控制语义包括播放、暂停、上一首、下一首等。这里只处理了休眠的控制命令其他的可以参考此处的处理
            String operation = json.getString(Constant.Music.KEY_OPERATION);
            if (Constant.Music.OPERATION_INS.equals(operation)) {
                // 控制语义
                JSONObject semantic = json.getJSONObject(Constant.Music.KEY_SEMANTIC);
                String insType = semantic.getJSONObject(Constant.Music.KEY_SLOTS).getString(Constant.Music.KEY_INSTYPE);

                // 休眠语义
                // TODO 检查并且修改
                if (Constant.Music.INSTYPE_SLEEP.equals(insType)) {
                    postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_EVENT_FORCE_SLEEP));
                    isSleepType = true;
                    return;
                }
                INSTYPE type = insTypeMap.get(insType);
                if (null != type) {
                    switch (type) {
                        case VOLMAX: {
                            if (null != mVolumeManager) {
                                mVolumeManager.setMaxVolume();
                            }
                            Constant.ISVOLCHANGE = true;
                        }
                        break;

                        case VOLMIN: {
                            if (null != mVolumeManager) {
                                mVolumeManager.setMinVolume();
                            }
                            Constant.ISVOLCHANGE = true;
                        }
                        break;

                        case VOLMID: {
                            if (null != mVolumeManager) {
                                mVolumeManager.setMidVolume();
                            }
                            Constant.ISVOLCHANGE = true;
                        }
                        break;

                        case VOLPLUS: {
                            isIncrease = true;
                            palyPromptTone();
                            Constant.ISVOLCHANGE = true;
                        }
                        break;

                        case VOLMINUS: {
                            isIncrease = false;
                            palyPromptTone();
                            Constant.ISVOLCHANGE = true;
                        }
                        break;

                        case NEXT:
                            Constant.ISVOLCHANGE = false;
                            postEvent(new AIUIEvent(EventsConstants.AIUIEvents.MUSICX_MUSIC_CRTL, Constant.Music.INSTYPE_NEXT));
                            break;
                        case PAST:
                            Constant.ISVOLCHANGE = false;
                            postEvent(new AIUIEvent(EventsConstants.AIUIEvents.MUSICX_MUSIC_CRTL, Constant.Music.INSTYPE_PAST));
                            break;
                        case PAUSE:
                            Constant.ISVOLCHANGE = false;
                            postEvent(new AIUIEvent(EventsConstants.AIUIEvents.MUSICX_MUSIC_CRTL, Constant.Music.INSTYPE_PAUSE));
                            break;
                        case LOOP:
                            Constant.ISVOLCHANGE = false;
                            postEvent(new AIUIEvent(EventsConstants.AIUIEvents.MUSICX_MUSIC_CRTL, Constant.Music.INSTYPE_LOOP));
                            break;
                        case CYCLE:
                            Constant.ISVOLCHANGE = false;
                            postEvent(new AIUIEvent(EventsConstants.AIUIEvents.MUSICX_MUSIC_CRTL, Constant.Music.INSTYPE_CYCLE));
                            break;
                        case REPLAY:
                            Constant.ISVOLCHANGE = false;
                            postEvent(new AIUIEvent(EventsConstants.AIUIEvents.MUSICX_MUSIC_CRTL, Constant.Music.INSTYPE_REPLAY));

                            break;
                        case REPEAT:
                            Constant.ISVOLCHANGE = false;
                            postEvent(new AIUIEvent(EventsConstants.AIUIEvents.MUSICX_MUSIC_CRTL, Constant.Music.INSTYPE_REPEAT));
                            break;
                        default:
                            break;
                    }
                }
                return;
            }
        } catch (JSONException e1) {
            Csjlogger.error(e1.getMessage());
//            AIUIPlayerKitVer player = SemanticResultHandler.getAIUIPlayer();
//            if (null != player) {
//                PlayItem item = new PlayItem(ContentType.TEXT, "音乐结果解析出错", null);
//                List<PlayItem> itemList = new ArrayList<PlayItem>();
//                itemList.add(item);
//
//                player.playItems(itemList, null);
//            }
        }
    }

    @Override
    protected void doAfterTTS() {

    }

}
