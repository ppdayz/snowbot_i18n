# This package is the main implementation of speech recognition

## The role of each class

**GoogleSpeechService.java**
The main service, features are:

- wake up
- Speech Recognition
- Postprocessing - Simple Voice Talk
- Simple motion control
- Expression transformation

**VoiceRecorder.java**
Continuously records audio and notifies the  VoiceRecorder.Callback} when voice (or any sound) is heard.

- pickup
- mute detection
- pickup control

**AudioSaverImpl.java** *（unimportant）*
The realization of Saving recording


**IVoiceSaver.java**  *（unimportant）*
The interface of Saving recording


----------

## How to use
```java
startService(new Intent(this, GoogleSpeechService.class));
```


## How dose it work
- **The whole *service* is needed to initialize the following methods，All are initialized in onCreate()**

1. fetch Google Speech API Access Token
2. Initialize Android resources and TTS
3. Start the recognition timeout detection thread（国内由于要挂VPN，所以这里做了检测）
4. Initialize custom semantic resources


- **Use EventBus to pass the wake-up Event**

Initialize EventBus, see the base class for concrete implementation`com.csjbot.csjbase.base.CsjBaseService`

```java
    @Override
    public boolean useEventBus() {
        return true;
    }
```

Subscribes to a wake-up event, where `wakeup.getAngle ()` is the wake-up angle，
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

Receive wake-up from the serial port：
`com.csjbot.snowbot.services.serial.EnglishVersionUart.checkBuffer`
```java
ibus.post(new EventWakeup(0, angle));
```

When you wake up the Robot, wake up:
1. Check whether the `SpeechGrpc.SpeechStub mApi` is NULL

	- If it is NULL ， return and report an error

2. The Robot Turns around
```java
snowBotManager.turnRound((short) angle);
```

3. Start recording
```java
startVoiceRecorder();
```

4. Check Whether [`SpeechActivity`](https://github.com/ppdayz/snowbot_i18n/blob/master/app/src/main/java/com/csjbot/snowbot/activity/aiui/SpeechActivity.java) is at the foreground (top of the stack)
	- If not in the foreground (top of the stack),  start the Activity
```java
	if (!Kits.Package.isTopActivity(this, "com.csjbot.snowbot.activity.aiui.SpeechActivity")) {
			Intent it = new Intent(this, SpeechActivity.class);
			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(it);
	}
```
5. Stop and reset the ongoing utterance
```java
mVoiceRecorder.dismiss();
```

- **Speech recognition flow**
![](https://github.com/ppdayz/snowbot_i18n/blob/master/doc/images/Recognize.jpg)







