package com.csjbot.snowbot.bean.aiui.AlertingTone;

import android.content.Context;
import android.media.AudioManager;

/**
 * 音量设置管理
 *
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年7月23日 上午10:37:28
 */
public class VolumeManager {
    private static VolumeManager instance;

    private AudioManager mAudioManager;

    /**
     * 音量大小数组
     **/
    public static final int[] mLevelArray = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};

    public static final int MAX_VOLUME_LEVEL = mLevelArray.length - 1;
    public static final int MIN_VOLUME_LEVEL = 1;
    public static final int MID_VOLUME_LEVEL = 7;
    // 每次增加或减少的音量
    public static final int VOLUME_LEVEL = 2;

    public static synchronized VolumeManager getInstance(Context context) {
        if (null == instance) {
            instance = new VolumeManager(context);
            return instance;
        } else {
            return instance;
        }
    }

    private VolumeManager(Context context) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * 把音量设置到最大,暂时设置最大音量7级
     */
    public void setMaxVolume() {
        if (null != mAudioManager) {
            int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        }
//        setVolume(MAX_VOLUME_LEVEL);
    }

    /**
     * 把音量设置为最小
     */
    public void setMinVolume() {
        if (null != mAudioManager) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, MIN_VOLUME_LEVEL, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        }
//        setVolume(MIN_VOLUME_LEVEL);
    }

    /**
     * 音量增加
     */
    public void plusVolume() {
        int volume = VOLUME_LEVEL;
        int currentVolume = getCurrentVolume();
        if (-1 != currentVolume) {
            volume += currentVolume;
            if (volume >= MAX_VOLUME_LEVEL) {
                volume = MAX_VOLUME_LEVEL;
            }
            setVolume(volume);
        }
    }

    /**
     * 音量增加
     *
     * @param volume 要增加的大小
     */
    public void plusVolume(int volume) {
        int currentVolume = getCurrentVolume();
        if (-1 != currentVolume) {
            volume += currentVolume;
            if (volume >= MAX_VOLUME_LEVEL) {
                volume = MAX_VOLUME_LEVEL;
            }
            setVolume(volume);
        }
    }

    /**
     * 音量最小
     */
    public void minusVolume() {
        int volume = VOLUME_LEVEL;
        int currentVolume = getCurrentVolume();
        if (-1 != currentVolume) {
            volume = currentVolume - volume;
            if (volume <= MIN_VOLUME_LEVEL) {
                volume = MIN_VOLUME_LEVEL;
            }
            setVolume(volume);
        }
    }

    /**
     * 音量减小
     *
     * @param volume 要减小的音量
     */
    public void minusVolume(int volume) {
        int currentVolume = getCurrentVolume();
        if (-1 != currentVolume) {
            volume = currentVolume - volume;
            if (volume <= MIN_VOLUME_LEVEL) {
                volume = MIN_VOLUME_LEVEL;
            }
            setVolume(volume);
        }
    }

    /**
     * 音量设置为中间大小
     */
    public void setMidVolume() {
        if (null != mAudioManager) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, MID_VOLUME_LEVEL, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        }
//        setVolume(MID_VOLUME_LEVEL);
    }

    /**
     * 设置音量
     *
     * @param volume 音量大小
     */
    public void setVolume(int volume) {
        if (volume <= MIN_VOLUME_LEVEL) {
            volume = MIN_VOLUME_LEVEL;
        } else if (volume >= MAX_VOLUME_LEVEL) {
            volume = MAX_VOLUME_LEVEL;
        }

        if (null != mAudioManager) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        }
    }

    /**
     * 获得当前媒体播放音量
     */
    public int getCurrentVolume() {
        if (null != mAudioManager) {
            int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            return currentVolume;
        }
        return -1;
    }
}
