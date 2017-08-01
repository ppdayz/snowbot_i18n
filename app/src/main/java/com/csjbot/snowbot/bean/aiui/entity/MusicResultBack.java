package com.csjbot.snowbot.bean.aiui.entity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.csjbot.csjbase.event.IBus;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.csjbot.snowbot.bean.aiui.AlertingTone.VolumeManager;

import org.json.JSONArray;
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
public class MusicResultBack extends SemanticResult {
    private final static String TAG = "MusicResult";

    enum INSTYPE {
        NEXT,
        PAST,
        REPLAY,
        REPEAT,
        PAUSE,
        VOLMAX,
        VOLMID,
        VOLMIN,
        VOLMINUS,
        VOLPLUS,
        INSTYPE_REPLAYANSWER
    }

    private static Map<String, INSTYPE> insTypeMap;

    static {
        insTypeMap = new HashMap<String, INSTYPE>();
        insTypeMap.put(Constant.Music.INSTYPE_NEXT, INSTYPE.NEXT);
        insTypeMap.put(Constant.Music.INSTYPE_PAST, INSTYPE.PAST);
        insTypeMap.put(Constant.Music.INSTYPE_REPLAY, INSTYPE.REPLAY);
        insTypeMap.put(Constant.Music.INSTYPE_REPEAT, INSTYPE.REPEAT);
        insTypeMap.put(Constant.Music.INSTYPE_PAUSE, INSTYPE.PAUSE);
        insTypeMap.put(Constant.Music.INSTYPE_VOLMAX, INSTYPE.VOLMAX);
        insTypeMap.put(Constant.Music.INSTYPE_VOLMID, INSTYPE.VOLMID);
        insTypeMap.put(Constant.Music.INSTYPE_VOLMIN, INSTYPE.VOLMIN);
        insTypeMap.put(Constant.Music.INSTYPE_VOLMINUS, INSTYPE.VOLMINUS);
        insTypeMap.put(Constant.Music.INSTYPE_VOLPLUS, INSTYPE.VOLPLUS);
        insTypeMap.put(Constant.Music.INSTYPE_REPLAYANSWER, INSTYPE.INSTYPE_REPLAYANSWER);
    }

    //    private Context mContext;
    private IBus mIbus;
    private VolumeManager mVolumeManager;
    private String url = "";
    private String answerText;

    private int currentVolume = -1;

    // 用于标示是否音量增减
    private boolean isIncrease = false;

    public MusicResultBack(String service, JSONObject json) {
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
            mVolumeManager.setMaxVolume();
        }

//        TonePlayer.playUseSoundPool(mContext, Constant.VOLUME_PATH, 3);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (null != mVolumeManager) {
                    if (isIncrease) {
                        mVolumeManager.setVolume(currentVolume + 2);
                    } else {
                        mVolumeManager.setVolume(currentVolume - 2);
                    }
                }

                // 连续播放三次提示音
//                TonePlayer.playUseSoundPool(mContext, Constant.VOLUME_PATH, 3);
                Csjlogger.debug(TAG, "playUseSoundPool");
            }
        }, 500);
    }

    public String getUrl() {
        return url;
    }

    public String getAnswerText() {
        return answerText;
    }

//    Intent musicInt = new Intent(Constant.ACTION_MUSIC_CONTROL);

    @Override
    public void handleResult(IBus iBus, Context context) {
        try {
            String operation = json.getString(Constant.Music.KEY_OPERATION);
            mIbus = iBus;

            JSONObject used_state = json.getJSONObject(Constant.Music.KEY_STATE);
            String mismatch = null;

            if (null != used_state) {
                mismatch = used_state.optString(Constant.Music.KEY_MISMATCH);
            }

            // mismatch不为空播放answer中的提示语，否则不存在此字段的时候播放音乐。、
            if (!TextUtils.isEmpty(mismatch)) {
                super.handleResult(iBus, context);
                return;
            }

            // 获得歌曲信息
            JSONArray results = json.getJSONObject("data").getJSONArray("result");
            JSONObject song = (JSONObject) results.get(0);

            if (null != song) {
                //歌曲播放url
                String audiopath = song.optString("audiopath");
                Csjlogger.debug("audiopath " + audiopath);
                url = audiopath;
                JSONObject answer = json.optJSONObject("answer");
                if (null != answer) {
                    // 合成的音乐信息
                    String text = answer.optString("text");
                    answerText = text;
                    Csjlogger.debug("answerText " + answerText);
                    Csjlogger.debug(TAG, audiopath);

                    if (!TextUtils.isEmpty(audiopath)) {
                        // 播放合成音和音乐播放
//                        List<PlayItem> itemList = new ArrayList<PlayItem>();
//                        itemList.add(new PlayItem(ContentType.TEXT, text, new Runnable() {
//
//                            @Override
//                            public void run() {
//                                try {
//                                    Thread.sleep(1000);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }));
//                        itemList.add(new PlayItem(ContentType.MUSIC, audiopath, null));
//
//                        AIUIPlayerKitVer player = SemanticResultHandler.getAIUIPlayer();
//                        if (null != player) {
//                            player.playItems(itemList, null);
//                        }
                    }
                }
            }
            // 音乐控制语义包括播放、暂停、上一首、下一首等。这里只处理了休眠的控制命令其他的可以参考此处的处理
            if (Constant.Music.OPERATION_INS.equals(operation)) {
                // 控制语义
                JSONObject semantic = json.getJSONObject(Constant.Music.KEY_SEMANTIC);
                String insType = semantic.getJSONObject(Constant.Music.KEY_SLOTS).getString(Constant.Music.KEY_INSTYPE);

                // 休眠语义
                // TODO 检查并且修改
                if (Constant.Music.INSTYPE_SLEEP.equals(insType)) {
                    Intent intent = new Intent(Constant.ACTION_SLEEP);
//                    if (null != context) {
//                        context.sendBroadcast(intent);
//                    }
                    return;
                }
                INSTYPE type = insTypeMap.get(insType);
                Csjlogger.debug("type " + type.toString());
                if (null != type) {
                    switch (type) {
                        case VOLMAX: {
                            if (null != mVolumeManager) {
                                mVolumeManager.setMaxVolume();
                            }
                        }
                        break;

                        case VOLMIN: {
                            if (null != mVolumeManager) {
                                mVolumeManager.setMinVolume();
                            }
                        }
                        break;

                        case VOLMID: {
                            if (null != mVolumeManager) {
                                mVolumeManager.setMidVolume();
                            }
                        }
                        break;

                        case VOLPLUS: {
                            isIncrease = true;
                            palyPromptTone();
                        }
                        break;

                        case VOLMINUS: {
                            isIncrease = false;
                            palyPromptTone();
                        }
                        break;
                        case NEXT:
//                            if (null != context) {
//                                musicInt.putExtra("action", Constant.Music.INSTYPE_NEXT);
//                                musicInt.putExtra("url", getUrl());
//                                Csjlogger.debug("getUrl" + getUrl());
//                                context.sendBroadcast(musicInt);
//                            }
                            break;
                        case PAST:
//                            if (null != context) {
//                                musicInt.putExtra("action", Constant.Music.INSTYPE_PAST);
//                                musicInt.putExtra("url", getUrl());
//                                context.sendBroadcast(musicInt);
//                            }
                            break;
                        case PAUSE:
//                            if (null != context) {
//                                musicInt.putExtra("action", Constant.Music.INSTYPE_PAUSE);
//                                context.sendBroadcast(musicInt);
//                            }
                            break;
                        case REPLAY:
//                            if (null != context) {
//                                musicInt.putExtra("action", Constant.Music.INSTYPE_REPLAY);
//                                context.sendBroadcast(musicInt);
//                            }
                            break;
                        case REPEAT:
//                            if (null != context) {
//                                musicInt.putExtra("action", Constant.Music.INSTYPE_REPEAT);
//                                context.sendBroadcast(musicInt);
//                            }
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
