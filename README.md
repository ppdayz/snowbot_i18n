
| version | modified | author | amendment |
|----|---|----|--- |
|v1.0 |    2017-01-31 |    Leo Pu |   First edition |
|v1.1    | 2017-02-11  |   Leo Pu  |   Fix bugs |
|v1.2    | 2017-06-06    | Leo Pu  |   Improve the content |
|v1.3    | 2017-08-13    | Leo Pu  |   Fix bug &  improve content|
|v1.4    | 2017-10-13    | Leo Pu  |   Improve document content|


Catalog
-	[What Can We DO](#what-can-we-do)
-	[Environmental Requirements](#environmental-requirements)
-	[Development Environment Configuration](#development-environment-configuration)
-	[How To Add SDK to the project](#how-to-add-sdk-to-the-project)
-	[Speech and Dialogue](#speech-and-dialogue)
-	[Map and walk](#map-and-walk)
-	[Sensors](#sensors)
-	[Control Arm](#control-arm)
-	[Face recognition](#face-recognition)
-	[How To Use VIDEO](#how-to-use-video)
-	Notice

-	## What Can We Do
Our SDK is based on AAR (Android Archive [reference link](https://developer.android.com/studio/projects/android-library.html#aar-contents)) form of integration into the software, provides the basis for the speech dialogue, expression control, sensor, map and walking arm, control interface.

-	## Environmental Requirements
Tools / Environment requirements

|project 			|	requirements |
|---|---
|Android Studio |	2.3.3 or above|
|JDK			|	1.7 or above|
|gradle			| 2.3.3 or above|


Android Dependency Requirement

| Android			|Edition|
|--|--
|compileSdkVersion	|25|
|minSdkVersion    	|19|
|targetSdkVersion 	|23|
|SupportSdk			|23.3.0|


Other dependent packages need to be referenced in the app moudle in gradle
```java
dependencies {
	[... ... your dependencies]

	compile 'com.android.support:multidex:1.0.1'
	compile 'org.greenrobot:eventbus:3.0.0'	
	compile 'com.alibaba:fastjson:1.1.54.android'
	compile 'com.github.limedroid:ARecyclerView:v1.0.0'

	compile 'com.jakewharton:butterknife:8.4.0'
	apt 'com.jakewharton:butterknife-compiler:8.4.0'
}
```


-	## Development Environment Configuration
Since the debugging interface of the robot is inside the screen, it can only be carried out in the form of ADB WiFi, and the specific method refers to the document:
[HowToSetUpAdbWifi.md](https://github.com/ppdayz/snowbot_i18n/blob/master/doc/HowToSetUpAdbWifi.md)



-	## How To Add SDK to the project
1.	New project in the Android Studio
2.	Copy aar file (**snowbot-sdk-1.1.3.aar**) to the dir "app\libs"
3.	Add as follows in Module:app
```java
	multiDexEnabled true

	repositories {
	    flatDir {
	        dirs 'libs'
	    }
	}
```
For Example:
```java
android {
	compileSdkVersion xx
	buildToolsVersion xx
	defaultConfig {
		applicationId "xxx.xxx.xxx"
		minSdkVersion xx
		targetSdkVersion xx
		versionCode xx
		versionName xx
		
		multiDexEnabled true
	}
		
	buildTypes {
		... ...
	}
		
	repositories {
		flatDir {
			dirs 'libs'
		}
	}
}
```  
1.	 Add dependency
```java
dependencies {
			[... ... your dependencies]
		
			compile 'com.android.support:multidex:1.0.1'
			compile 'org.greenrobot:eventbus:3.0.0'	
			compile 'com.alibaba:fastjson:1.1.54.android'
			compile 'com.github.limedroid:ARecyclerView:v1.0.0'
		
			compile 'com.jakewharton:butterknife:8.4.0'
			apt 'com.jakewharton:butterknife-compiler:8.4.0'
}
```

-	## Speech and Dialogue
	-	Speech recognition
 		See document [English speech recognition](https://github.com/ppdayz/snowbot_i18n/tree/master/app/src/main/java/com/csjbot/snowbot/services/google_speech)
	-	Text to speech
		We built a iFLYTEK TTS class, see[CsjSpeechSynthesizer2.java](CsjSpeechSynthesizer2.java)
		You can use our classes directly, or you can use your own TTS tools
		If you want to use our built-in class, refer to the following steps:
		1. Initialize, create instances
		```
		 mSpeechSynthesizer = CsjSpeechSynthesizer2.createSynthesizer(this.getApplicationContext(), resault -> {
            	if (resault == 0) {
                	// // FIXME: 2017/08/16 0016  TEST only
                	mSpeechSynthesizer.startSpeaking("Initialize voice successfully", null);
            	}
        });
		```
		2.	Get instances
		```java
		CsjSpeechSynthesizer2 speech = CsjSpeechSynthesizer2.getSynthesizer();
		speech.startSpeaking("something to say", synthesizerListener)
		```
		**OR**
		```java
         CsjSpeechSynthesizer2.getSynthesizer().startSpeaking("something to say", new CsjSynthesizerListener() {
                    @Override
                    public void onSpeakBegin() {
                    }

                    @Override
                    public void onCompleted(SpeechError speechError) {
                    }
        });
		``` 
		3. Custom callback
		```java
		    private SynthesizerListener speechSynthesizerListener = new SynthesizerListener() {
			        @Override
			        public void onSpeakBegin() {
						// begin to speak
			        }
			
			        @Override
			        public void onBufferProgress(int i, int i1, int i2, String s) {
						// 
			        }
			
			        @Override
			        public void onSpeakPaused() {
						// Speak Paused
			        }
			
			        @Override
			        public void onSpeakResumed() {
						// Speak Resumed
			        }
			
			        @Override
			        public void onSpeakProgress(int percent, int beginPos, int endPos) {
						// Speak progress 
			        }
			
			        @Override
			        public void onCompleted(SpeechError speechError) {
						// Speak Completed
			        }
			
			        @Override
			        public void onEvent(int i, int i1, int i2, Bundle bundle) {
						// ERROR EVENT
			        }
			    };
		```
-	## Map and walk
### We can control the SnowBaby to [go forward, backward, left turn, right turn](#go-forward-backward-left-turn-and-right-turn), [get map](get-map), get the robot's current posture, arrive at a specific point, patrol, go back charging by SDK
```SnowBotManager ```is a singleton，we can use ```SnowBotManager.getInstance()``` create or get
```java
private SnowBotManager snowBotManager = SnowBotManager.getInstance();
```
OR
```
SnowBotManager.getInstance().getTracks();
```


1. go forward, backward, left turn and right turn
Use ```SnowBotManager.moveBy``` to control the SnowBaby to go forward, backward, left turn, right turn.
Example：
```java
SnowBotManager.getInstance().moveBy(MoveDirection.FORWARD); 
```
We define:
```java
public enum MoveDirection {
	FORWARD,
	BACKWARD,
	TURN_RIGHT,
	TURN_LEFT;

	private MoveDirection() {
	}
}
```
Call the following methods to control SnowBaby turn specific angle:
```java
SnowBotManager.turnRound 
```
Example：
```java
SnowBotManager.getInstance().turnRound((short) 90); 
```
**We define the robot's face as the base, the positive counter clockwise, 
the negative clockwise rotation (positive to left, negative to right)**
In other words```SnowBotManager.getInstance().turnRound((short) 90); ``` is "Left turn 90 degrees"
2. get map
The map is in the form of an Bitmap from the ```SnowBotManager``` to the application of asynchronous callback，we should pay attention to ***the thread*** when calling
The bitmap include: the original map (point-line graph), the virtual wall, and the virtual track.
Follow steps:
-	Registering Callbacks
```java
SnowBotManager.getInstance().setMoveServerMapListener(new MoveServerMapListener() {
        	@Override
        	public void getMap(Bitmap map) {
            	Message msg = mHandler.obtainMessage();
            	Bundle bundle = new Bundle();
            	bundle.putParcelable("bitmap", map);
           		msg.setData(bundle);
            	msg.what = MAP_UPDATE;
            	mHandler.sendMessage(msg);
        	}
    });
```
-	Start getting maps
```java
SnowBotManager.getInstance().getMap(this, this);
```
reference prototype
```java
com.csjbot.snowbot_rogue.platform.SnowBotManager#getMap(android.content.Context, 
								com.csjbot.snowbot_rogue.servers.slams.MoveServerMapListener)
```
-	Drawing in UI
```java
	private static class MapActivityHandler extends WeakReferenceHandler<MapActivity> {
		private Bitmap bm;
		// UI处理
		MapActivityHandler(MapActivity reference) {
			super(reference);
		}

		@Override
		protected void handleMessage(MapActivity reference, Message msg) {
			switch (msg.what) {
			case MAP_UPDATE:
				reference.bm = msg.getData().getParcelable("bitmap");
				// show in UI
				break;
			default:
				break;
			}
		}
	}
```
-	Stop getting maps
```java
SnowBotManager.getInstance().stopGetMap();
```
3. Get the current robot pose
We use this method:
```java
com.csjbot.snowbot_rogue.platform.SnowBotManager#getCurrentPose
```
returns ```com.slamtec.slamware.robot.Pose```
Pose contains two information, one is the robot's position (Location), and the other is the robot's orientation (Rotation)

```java
public class Pose {
		private Location location;
    	private Rotation rotation;
		... ...
}
```

```java
    public Pose(float x, float y, float z, float yaw, float roll, float pitch) {
        this.location = new Location(x, y, z);
        this.rotation = new Rotation(yaw, roll, pitch);
    }
```
Among them, only **x, y, yaw** are uesful, yaw is the orientation of the robot in the map(from axis), the initial position of the robot is 0, and the unit is radian

4. Reach specific points
Going to a specific point uses the following method
```java
com.csjbot.snowbot_rogue.servers.slams.SnowBotMoveServer#moveTo(com.slamtec.slamware.robot.Location)
```
5. patrol
-	Start patrolling
```java
	private void startPartol() {
	    	Pose[] poses = new Pose[]{
	            	new Pose(1f, 1f, 0f, 0f, 0f, 0f),
	            	new Pose(-1f, 1f, 0f, 0f, 0f, 0f),
	            	new Pose(1f, -1f, 0f, 0f, 0f, 0f),
	            	new Pose(-1f, -1f, 0f, 0f, 0f, 0f),
    		};

    		SnowBotManager.getInstance().partol(Arrays.asList(poses));
	}
```
-	Stop patrolling
```java
SnowBotManager.getInstance().stopPartol();
```
6. Go back charging
```java
SnowBotManager.getInstance().goHome();
```
-	## Sensors
we can get touch head event from Sensors.
Listen AIUIEvent and get case *EventsConstants.AIUIEvents.AIUI_EVENT_TOUCH_GET*
Sample：
1. Register the Eventbus
```java
EventBus ibus = EventBus.getDefault();
ibus.register(this);
```
2. Subscribe AIUIEvent
```java
@Subscribe(threadMode = ThreadMode.MAIN)
public boolean onAIUIEvent(AIUIEvent event) {

    switch (event.getTag()) {
        case EventsConstants.AIUIEvents.AIUI_EVENT_TOUCH_GET:
            touchGet();
            break;
        default:
            break;
    }
}
```
3. if you leave,  unregister the Eventbus
```java
if (ibus != null) {
    ibus.unregister(this);
}
``` 
-	## Control Arm
We use `SnowBotManager` to Control SnowBaby's Arm
Sample:
-	Waving hands three times
```java
SnowBotManager.getInstance().swingDoubleArm((byte)3);
```
-	Waving left hands three times 
```java
SnowBotManager.getInstance().swingLeftArm((byte)3);
``` 
-	Waving right hands three times 
```java
SnowBotManager.getInstance().swingRightArm((byte)3);
```
-	## Face recognition
**Coming soon**
-	## How To Use VIDEO
	Because we use a custom Android system, so we encapsulated the camera application, the user can directly call the ability inside the SDK to call the camera
you can call it follow steps:
1.	We have defined a SurfaceView to preview the Camera
```xml
  <com.csjbot.snowbot_rogue.camera.preview.CameraSurfaceView
        android:id="@+id/surfaceview"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_weight="1" />
```
2.	Start Camera and Preview
```java
   	@BindView(R.id.surfaceview)
	SurfaceView mSurfaceView;

	private Camera mCamera;
	private SurfaceHolder mSurfaceHolder;
	private MediaRecorder mediaRecorder;
	private boolean mIsRecording = false;

	private void startCamera() {
		mSurfaceHolder = mSurfaceView.getHolder();
	
		mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
		 	}
		
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				initpreview();
			}
		
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		
			}
			});
	}

	private void initpreview() {
		mCamera = CameraInterface.getInstance().getCameraDevice();
		if (mCamera != null) {
			//Start the video directly here
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					startmediaRecorder();
				}
			}, 5000);
		} else {
			// todo Handling errors
		}
	}
```
3.	Start Media Recorder
```java
	private void startmediaRecorder() {
		mCamera.unlock();

		if (mediaRecorder == null) {
			Csjlogger.debug("initVideoRecord");
			mIsRecording = true;

			CamcorderProfile mProfile = CamcorderProfile.get(CameraInterface.getInstance().getCameraId()
, CamcorderProfile.QUALITY_480P);

			//1st. Initial state
			mediaRecorder = new MediaRecorder();
			mediaRecorder.setCamera(mCamera);

			//2st. Initialized state

			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
			mediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

			//3st. config
			mediaRecorder.setOutputFormat(mProfile.fileFormat);
			mediaRecorder.setAudioEncoder(mProfile.audioCodec);
			mediaRecorder.setVideoEncoder(mProfile.videoCodec);
			mediaRecorder.setOutputFile(getName());
			mediaRecorder.setVideoSize(640, 480);
			mediaRecorder.setVideoFrameRate(mProfile.videoFrameRate);
			mediaRecorder.setVideoEncodingBitRate(mProfile.videoBitRate);
			mediaRecorder.setAudioEncodingBitRate(mProfile.audioBitRate);
			mediaRecorder.setAudioChannels(mProfile.audioChannels);
			mediaRecorder.setAudioSamplingRate(mProfile.audioSampleRate);
			
			mediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
		}

		try {
			mediaRecorder.prepare();
			mediaRecorder.start();
		} catch (Exception e) {
			// Handling error	
			e.printStackTrace();
			mCamera.lock();
		}
		
		if (mIsRecording) {
			// Handling UI
		}
	}

```
4.	Stop Media Recorder
```java
private void stopmediaRecorder() {
	if (mediaRecorder != null) {
		if (mIsRecording) {
			try {
				//The following three parameters must be added, 
				//otherwise they will burst，when mediarecorder.stop();
				//Error is ：RuntimeException:stop failed
				mediaRecorder.setOnErrorListener(null);
				mediaRecorder.setOnInfoListener(null);
				mediaRecorder.setPreviewDisplay(null);
				mediaRecorder.stop();
			} catch (IllegalStateException e) {
			} catch (RuntimeException e) {
			}
		
			//mCamera.lock();
			mediaRecorder.reset();
			mediaRecorder.release();
			mediaRecorder = null;
			mIsRecording = false;
	
			if (mCamera != null) {
				try {
					mCamera.reconnect();
				} catch (IOException e) {
					Toast.makeText(this, "reconect fail", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
		// Handling UI
		}
	}
}
```