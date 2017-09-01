# 这个包是语音识别的主要实现

## 各个类的作用

**GoogleSpeechService.java**

主要的服务，功能有：

- 唤醒
- 语音识别
- 后处理--简单语音对话
- 简单运动控制
- 表情变换

**VoiceRecorder.java**

持续不断的录音，当检测到有语音（或者有任何声音），就通过回调通知上层

- 拾音
- 静音检测
- 拾音控制

**AudioSaverImpl.java** *（不重要）*

保存录音的实现


**IVoiceSaver.java**  *（不重要）*

保存录音的接口


----------

## 如何使用
```java
startService(new Intent(this, GoogleSpeechService.class));
```


## 工作流程
- **整个 *service* 需要初始化以下，都在onCreate()中初始化**

1. 获取 Google Speech API Access Token
2. 初始化安卓资源和TTS
3. 启动识别超时检测线程（国内由于要挂VPN，所以这里做了检测）
4. 初始化自定义语义资源


- **利用了EventBus来传递唤醒信号**

初始化EventBus，具体实现见基类`com.csjbot.csjbase.base.CsjBaseService`

```java
    @Override
    public boolean useEventBus() {
        return true;
    }
```

订阅了唤醒事件，其中`wakeup.getAngle()`为唤醒角度，
```java
    @Subscribe
    @SuppressWarnings("unused")
    public void wakeup(EventWakeup wakeup) {
        int angle = wakeup.getAngle();
        if (angle >= 0 && angle <= 360) {
            wakeup(angle);
        }
    }
```
接收来自串口的唤醒
`com.csjbot.snowbot.services.serial.EnglishVersionUart.checkBuffer`
```java
ibus.post(new EventWakeup(0, angle));
```

当唤醒了之后，进行唤醒处理：
1. 判断 `SpeechGrpc.SpeechStub mApi` 是否为空

	- 如果为空就返回并且报错
	
2. 小雪人转身
```java
snowBotManager.turnRound((short) angle);
```

3. 开始录音
```java
startVoiceRecorder();
```

4. 判断[`SpeechActivity`](https://github.com/ppdayz/snowbot_i18n/blob/master/app/src/main/java/com/csjbot/snowbot/activity/aiui/SpeechActivity.java)是否在最前台（栈顶）
	- 如果不在最前台（栈顶），就启动这个Activity
```java
	if (!Kits.Package.isTopActivity(this, "com.csjbot.snowbot.activity.aiui.SpeechActivity")) {
		Intent it = new Intent(this, SpeechActivity.class);
		it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(it);
	}
```
5. 停止并且重置正在进行的话语
```java
mVoiceRecorder.dismiss();
```

- **识别处理流程**
![](https://github.com/ppdayz/snowbot_i18n/blob/master/doc/images/Recognize.jpg)


- **处理识别到的音频**

识别到的音频会在Google服务器上被转换成文字，并且通过StreamObserver来进行获取
我们不需要关心如何处理音频，只需要处理Google返回的数据

```java
private final StreamObserver<StreamingRecognizeResponse> mResponseObserver
            = new StreamObserver<StreamingRecognizeResponse>() {

        @Override
        public void onNext(StreamingRecognizeResponse value) {
            // Receives a value from the stream.
        }

        @Override
        public void onError(Throwable t) {
            //  Receives a terminating error from the stream.
        }

        @Override
        public void onCompleted() {
            // Receives a notification of successful stream completion.
        }
    };
```
我们在 `onNext`中处理结果：
1. 处理 `StreamingRecognizeResponse`, 提取出识别的文字和是否是最终结果
2. 如果是最终结果，就在`SpeechActivity`中显示最终的结果，同时对结果做处理
	- 解析动作，如果匹配并执行就返回true，下面的就不执行
	- 如果没有匹配结果，就解析自定义语义，如果匹配就说出来；如果没有匹配就播放默认的语句
3. 如果不是，就在`SpeechActivity`中显示Toast
	**如果要加入自己的AI，在这里进行**

```java
		@Override
        public void onNext(StreamingRecognizeResponse response) {
            String text = null;
            boolean isFinal = false;
			// Parse Text
            if (response.getResultsCount() > 0) {
                final StreamingRecognitionResult result = response.getResults(0);
                isFinal = result.getIsFinal();
                if (result.getAlternativesCount() > 0) {
                    final SpeechRecognitionAlternative alternative = result.getAlternatives(0);
                    text = alternative.getTranscript();
                }
            }

            if (text != null) {
                Csjlogger.warn(text);
                lastRecognizingTime = System.currentTimeMillis();
                if (isFinal) {
					
					// Show Text in SpeechActivity 
                    postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_SPEAKTEXT_DATA, text));
                    postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_SPEAKTEXT_RC, 5));

					// 1. parse action, such as move,turn round 
                    if (!parseAction(text)) {
						// 2. parse Custom semantics
                        if (!parseSpeak(text)) {
                            postEvent(new AIUIEvent(EventsConstants.AIUIEvents.AIUI_ANSWERTEXT_DATA, "I can't understand,but I'm learning"));
                            mSpeechSynthesizer.startSpeaking("I can't understand,but I'm learning", speechSynthesizerListener);
                        }
                    }
                    lastRecognizingTime = Long.MAX_VALUE;
                } else {
					// if not final, Show toast in SpeechActivity
                    postEvent(new AIUIEvent(SpeechActivity.AIUI_SPEAKTEXT_DATA_NOT_FINAL, text));
                }
            }
        }
```

当onCompleted()被调用的时候，就说明一次识别已经完成

- ###在我们的提供的服务中，当机器人开始说话的时候，就会继续强制重置拾音并且开始识别，如果您有更加好的流程，欢迎提`issue`和`pull request`

