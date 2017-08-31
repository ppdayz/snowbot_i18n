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

- **Handles incoming audio**

The audio identified will be converted to text on the `Google server` and will be obtained via `StreamObserver`
We don't have to care about how to process audio, just processing the data returned by Google
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
We process the results in the `onNext` method:
1. processing `StreamingRecognizeResponse`, extracting the identified text and whether it is the final result
2. if it is the end result, show the final result in `SpeechActivity` and process the result at the same time
Parse action, if it matches and executes, returns true, and the following is not executed
If there is no matching result, parse the custom semantics; if matches are said; play the default statement if no match is found
3. if not, display Toast in `SpeechActivity`
**if you want to join your AI, do it here**

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
- ###In our Sample services, when the robot began to speak, will continue to force the reset pickup and began to identify, if you have more good procedures, welcome `issue` and `pull request`











