
| version | modified | author | amendment |
|----|---|----|--- |
|v1.0 |    2017-01-31 |    Leo Pu |   First edition |
|v1.1    | 2017-02-11  |   Leo Pu  |   Fix bugs |
|v1.2    | 2017-06-06    | Leo Pu  |   Improve the content |
|v1.3    | 2017-08-13    | Leo Pu  |   Fix bug &  improve content|
|v1.4    | 2017-10-13    | Leo Pu  |   Improve document content|


Catalog
-	[What Can SDK DO](# What Can SDK Do)
-	[Environmental Requirements](# Environmental Requirements)
-	[Development Environment Configuration](# Development Environment Configuration)
-	[How To Add SDK to the project](# How To Add SDK to the project)
-	[Speech & Dialogue](# Speech & Dialogue)
-	Map and walk
-	Sensors
-	Control arm
-	Face recognition
-	How To Use VIDEO
-	Notice

-	## What Can SDK Do
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

-	## Speech & Dialogue
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
