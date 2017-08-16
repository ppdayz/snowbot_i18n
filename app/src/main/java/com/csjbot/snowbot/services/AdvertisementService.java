package com.csjbot.snowbot.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.android.core.util.SharedUtil;
import com.android.core.util.StrUtil;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot_rogue.app.CsjSpeechSynthesizer;
import com.csjbot.snowbot.bean.aiui.entity.CsjSynthesizerListener;
import com.csjbot.snowbot.pupwindow.PlayAdvertiseFloatWind;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot.utils.SpeechStatus;
import com.iflytek.cloud.SpeechError;

/**
 * @author: jl
 * @Time: 2017/1/10
 * @Desc:广告播放
 */

public class AdvertisementService extends Service {
    private Handler handler = new Handler();
    private String advertisement = "";
    private int repeatTime = 0;
    private CsjSpeechSynthesizer csjSpeechSynthesizer = CsjSpeechSynthesizer.getSynthesizer();
    private PlayAdvertiseFloatWind playAdvertiseFloatWind;
    private int surplusTimes = 0;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            startSpeak(surplusTimes, advertisement);
        }
    };

    @Override
    public void onCreate() {
        playAdvertiseFloatWind = new PlayAdvertiseFloatWind(this);
        playAdvertiseFloatWind.setClickListener(new PlayAdvertiseFloatWind.ClickListener() {
            @Override
            public void clickPlay(boolean isplaying) {
                if (isplaying) {
                    stopSpeak();
                    Csjlogger.debug("stopSpeak");
                } else {
                    startSpeak(surplusTimes, advertisement);
                    Csjlogger.debug("startSpeak {}, {} ",surplusTimes, advertisement);

                }
            }

            @Override
            public void clickStop() {
                Csjlogger.debug("clickStop");

                stopSpeak();
                stopSelf();
            }
        });
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getData();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void getData() {
        advertisement = SharedUtil.getPreferStr(SharedKey.ADVERTISEMENT);
        repeatTime = SharedUtil.getPreferInt(SharedKey.REPEATTIME, 0);
        if (StrUtil.isBlank(advertisement) || repeatTime == 0) {
            return;
        }
        startSpeak(repeatTime, advertisement);

    }

    private void startSpeak(int time, String advertisement) {
        if (time == 0 || null == csjSpeechSynthesizer) {
            playAdvertiseFloatWind.remove();
            stopSelf();
            return;
        }

        csjSpeechSynthesizer.startSpeaking(advertisement, new CsjSynthesizerListener() {
            @Override
            public void onSpeakBegin() {
                SpeechStatus.getIstance().setSpeakFinished(false);
            }

            @Override
            public void onCompleted(SpeechError speechError) {
                SpeechStatus.getIstance().setSpeakFinished(true);
                surplusTimes = time - 1;
                handler.postDelayed(runnable, 3000);
            }
        });
    }

    private void stopSpeak() {
        if (null == csjSpeechSynthesizer) {
            return;
        }
        handler.removeCallbacks(runnable);
        csjSpeechSynthesizer.stopSpeaking();
    }


}
