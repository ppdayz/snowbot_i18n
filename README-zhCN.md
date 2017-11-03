|版本	|修改时间	|作者|	修改内容 |
|----|---|----|--- |
|v1.0 |    2017-01-31 |    浦耀宗 |   第一版 |
|v1.1    | 2017-02-11  |   浦耀宗  |   修正BUG |
|v1.2    | 2017-06-06    | 浦耀宗  |   完善内容 |
|v1.3    | 2017-08-13    | 浦耀宗  |   修正BUG，完善内容 |
|v1.4    | 2017-10-13    | 浦耀宗  |   完善文档内容 |
|v1.5    | 2017-11-02    | 浦耀宗  |   完善文档内容 |


目录
- [SDK能做什么](#SDK能做什么)
- [环境要求](#环境要求)
- [开放环境配置](#开放环境配置)
- [如何把SDK加入项目中](#如何把SDK加入项目中)
- [语音对话](#语音对话)
- [地图和行走](#地图和行走)
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


-	## 地图和行走

### 我们可以通过SDK来控制小雪人的前进、后退、左转、右转，获取地图、获取机器人当前姿态、到达特定的点、巡逻、回去充电
```SnowBotManager ```是一个单例，可以使用 ```SnowBotManager.getInstance()``` 来创建或者使用
```java
private SnowBotManager snowBotManager = SnowBotManager.getInstance();
```
或者
```
SnowBotManager.getInstance().getTracks();
```


1. 前进、后退、左转、右转
利用 ```SnowBotManager.moveBy``` 来控制小雪人的前后左右 
例子：
```java
SnowBotManager.getInstance().moveBy(MoveDirection.FORWARD); 
```
其中定义:
```java
public enum MoveDirection {
    FORWARD,
    BACKWARD,
    TURN_RIGHT,
    TURN_LEFT;

	private MoveDirection() {
}
```
调用以下函数来控制小雪人的转特定的角度：
````
SnowBotManager.turnRound 
```
例子：
```
SnowBotManager.getInstance().turnRound((short) 90); 
```
**我们定义：以机器人朝向为基准，正数逆时针转，负数顺时针转（正数向左转，负数向右转）**
也就是说```SnowBotManager.getInstance().turnRound((short) 90); ``` 是向左转90度
2. 获取地图
地图是以一个Bitmap的形式从```SnowBotManager```异步回调到应用之中，调用的时候需要**注意线程**
图片包括：原始的地图的点线图、虚拟墙、以及虚拟轨道
	-	注册回调
```
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
	-	开始获取地图
```
SnowBotManager.getInstance().getMap(this, this);
```
参数参照原型
```
com.csjbot.snowbot_rogue.platform.SnowBotManager#getMap(android.content.Context, 
								com.csjbot.snowbot_rogue.servers.slams.MoveServerMapListener)
```
	-	在UI中绘制
```
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
	                // 在UI中展示
	                break;
	            default:
                break;
        		}
    		}
		}
```
	-	停止获取地图
```
SnowBotManager.getInstance().stopGetMap();
```
3. 获取机器人当前姿态
通过方法
```
com.csjbot.snowbot_rogue.platform.SnowBotManager#getCurrentPose
```
返回的是```com.slamtec.slamware.robot.Pose```
Pose包含两个信息，一个是机器人的位置(Location)，一个是机器人的朝向(Rotation)
```
public class Pose {
		private Location location;
    	private Rotation rotation;
		... ...
}
```
```
    public Pose(float x, float y, float z, float yaw, float roll, float pitch) {
        this.location = new Location(x, y, z);
        this.rotation = new Rotation(yaw, roll, pitch);
    }
```
其中对我们有用的就只有**x, y, yaw**，yaw 就是机器人在地图中的位置，以机器人的初始位置为0，单位是弧度
4. 到达特定的点
通过以下方法来到达特定的点：
```
com.csjbot.snowbot_rogue.servers.slams.SnowBotMoveServer#moveTo(com.slamtec.slamware.robot.Location)
```
5. 巡逻
	-	开始巡逻
```
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
	-	结束巡逻
```
SnowBotManager.getInstance().stopPartol();
```
6. 回去充电
```
SnowBotManager.getInstance().goHome();
```