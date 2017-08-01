package com.csjbot.snowbot.bean.aiui;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.csjbot.csjbase.event.IBus;
import com.csjbot.snowbot.bean.aiui.entity.SemanticResult;


/**
 * 语义结果处理类。
 *
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年8月18日 上午10:47:03
 */
public class SemanticResultHandler {
    private static final String TAG = "SemanticResultHandler";

    private static SemanticResultHandler instance;

//	private static AIUIPlayerKitVer aiuiPlayer;

    private Context mContext;
    private IBus mIBus;

    private ResultHandler mResultHandler;

    private HandlerThread mHandlerThread;


//	public static SemanticResultHandler getInstance(Context context, AIUIPlayerKitVer player) {
//		aiuiPlayer = player;
//
//		if (null == instance) {
//			instance = new SemanticResultHandler(context);
//		}
//		return instance;
//	}
//
//	public static AIUIPlayerKitVer getAIUIPlayer() {
//		return aiuiPlayer;
//	}

    private SemanticResultHandler(IBus iBus, Context context) {
        mIBus = iBus;
        mContext = context;
        mHandlerThread = new HandlerThread("ResultHandleThread");
        mHandlerThread.start();
        mResultHandler = new ResultHandler(mHandlerThread.getLooper());
    }

    public void handleResult(SemanticResult result) {
        if (null != mResultHandler && null != result) {
            // 清空以前的消息，避免积压
            mResultHandler.removeMessages(MSG_SEMANTIC_RESULT);
            mResultHandler.obtainMessage(MSG_SEMANTIC_RESULT, result).sendToTarget();
        }
    }

    public void destroy() {
        if (null != mHandlerThread) {
            mHandlerThread.quit();
            mResultHandler = null;
        }
        instance = null;
    }

    private static final int MSG_SEMANTIC_RESULT = 1;

    class ResultHandler extends Handler {

        public ResultHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            SemanticResult result = (SemanticResult) msg.obj;
            result.handleResult(mIBus, mContext);
        }
    }

}
