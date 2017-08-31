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
持续不断的录音，但检测到有语音（或者有任何声音），就通过回调通知上层

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

并且订阅了唤醒事件，其中`wakeup.getAngle()`为唤醒角度，
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

当唤醒了之后，就行唤醒处理：
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


