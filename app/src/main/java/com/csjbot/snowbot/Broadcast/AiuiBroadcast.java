package com.csjbot.snowbot.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.android.core.util.SharedUtil;
import com.android.core.util.StrUtil;
import com.csjbot.csjbase.event.IBus;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.bean.aiui.entity.CsjSynthesizerListener;
import com.csjbot.snowbot.services.CsjSpeechSynthesizer2;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot.utils.SpeechStatus;
import com.csjbot.snowbot_rogue.Events.AIUIEvent;
import com.csjbot.snowbot_rogue.Events.EventsConstants;
import com.csjbot.snowbot_rogue.Events.ExpressionEvent;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.iflytek.cloud.SpeechError;

/**
 * @author: jl
 * @Time: 2017/2/6
 * @Desc:处理其他应用发过来的aiui广播
 */

public class AiuiBroadcast extends BroadcastReceiver {
    public static final String SEND_ACTION = "com.csjbot.snowbot.Broadcast.sendContent";
    public static final String RECEIVE_ACTION = "com.csjbot.snowbot.Broadcast";
    protected IBus ibus = null;

    public AiuiBroadcast(IBus ibus) {
        this.ibus = ibus;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(AiuiBroadcast.RECEIVE_ACTION)) {
            if (intent.getExtras() != null) {
                Bundle bundle = intent.getExtras();

                int aiuiSwitch = bundle.getInt(SharedKey.AIUISERVICESWITCH, 1);
                int aiuiType = bundle.getInt(SharedKey.AIUISERVICETYPE,0);
                SharedUtil.setPreferInt(SharedKey.AIUISERVICESWITCH, aiuiSwitch);
                SharedUtil.setPreferInt(SharedKey.AIUISERVICETYPE, aiuiType);

                String ttsContent = bundle.getString(SharedKey.TTSCONTENT);
                if (StrUtil.isNotBlank(ttsContent)) {
                    postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_ANSWERTEXT_DATA, ttsContent));
                    CsjSpeechSynthesizer2.getSynthesizer().startSpeaking(ttsContent, new CsjSynthesizerListener() {
                        @Override
                        public void onSpeakBegin() {
                            SpeechStatus.getIstance().setSpeakFinished(false);
                            postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_SPEAK));
                        }

                        @Override
                        public void onCompleted(SpeechError speechError) {
                            SpeechStatus.getIstance().setSpeakFinished(true);
                            postEvent(new ExpressionEvent(Constant.Expression.EXPRESSION_NORMAL));
                        }
                    });
                }
            }

        }
    }

    public void postEvent(IBus.IEvent event) {
        if (ibus != null) {
            Csjlogger.debug("postEvent  " + event.getTag());
            ibus.post(event);
        }
    }

}
