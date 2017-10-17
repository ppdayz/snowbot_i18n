|版本	|修改时间	|作者|	修改内容 |
|----|---|----|--- |
|v1.0 |    2017-01-31 |    浦耀宗 |   第一版 |
|v1.1    | 2017-02-11  |   浦耀宗  |   修正BUG |
|v1.2    | 2017-06-06    | 浦耀宗  |   完善内容 |
|v1.3    | 2017-08-13    | 浦耀宗  |   修正BUG，完善内容 |
|v1.4    | 2017-10-13    | 浦耀宗  |   完善文档内容 |


目录
- [SDK能做什么](#SDK能做什么)
- [环境要求](#环境要求)
- [开放环境配置](#开放环境配置)
- [如何把SDK加入项目中](#如何把SDK加入项目中)
- [语音对话](#语音对话)
- 地图和行走
- 传感器
- 控制手臂
- 人脸识别
- 视频
- 注意事项
 


-	## SDK能做什么
我们的SDK是以aar（Android Archive [参考链接](https://developer.android.com/studio/projects/android-library.html#aar-contents)）形式集成进软件，提供了基础的语音对话、传感器、表情控制、地图和行走、控制手臂的接口


-	## 环境要求

工具/环境要求

|项目			|	要求|
|---|---
|Android Studio |	2.3.3 or above|
|JDK			|	1.7 or above|
|gradle			| 2.3.3 or above|


Android依赖要求

| Android			|版本|
|--|--
|compileSdkVersion	|25|
|minSdkVersion    	| 19|
|targetSdkVersion 	|23|
|SupportSdk			|23.3.0|


其他依赖的包，需要在app moudle的 gradle 中引用

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



-	## 开发环境配置

由于机器人的调试接口在屏幕内部，所以只能通过adb wifi的形式来进行，具体方法参照文档

[HowToSetUpAdbWifi](https://github.com/ppdayz/snowbot_i18n/blob/master/doc/HowToSetUpAdbWifi.md)


-	## 如何把SDK加入项目中

1.	Android Studio 新建项目
2.	把 aar 包(**snowbot-sdk-1.1.3.aar**)，拷贝到 "app\libs" 目录下
1.	在Module：app中加入如下
```java
	multiDexEnabled true

	repositories {
	    flatDir {
	        dirs 'libs'
	    }
	}
```
示例
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
3.	加入依赖
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
1.	现在就可以进行开发了



-	## 语音对话
	-	语音识别
 		参见文档	[英文语音识别 google-speech-api](https://github.com/ppdayz/snowbot_i18n/tree/master/app/src/main/java/com/csjbot/snowbot/services/google_speech)
	-	文字转语音
		我们内置了科大讯飞的TTS，详情请见类[CsjSpeechSynthesizer2.java](CsjSpeechSynthesizer2.java)
		你可以直接使用我们的类，也可以使用自己的TTS工具
		如果要使用我们内置的类，请参照以下步骤
		1. 初始化,创建实例
		```
		 mSpeechSynthesizer = CsjSpeechSynthesizer2.createSynthesizer(this.getApplicationContext(), resault -> {
            	if (resault == 0) {
                	// // FIXME: 2017/08/16 0016  TEST only
                	mSpeechSynthesizer.startSpeaking("Initialize voice successfully", null);
            	}
        });
		```
		2. 获取实例
		```java
		CsjSpeechSynthesizer2 speech = CsjSpeechSynthesizer2.getSynthesizer();
		speech.startSpeaking("something to say", synthesizerListener)
		```
		或者
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
		3. 自定义回调
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