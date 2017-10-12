|版本	|修改时间	|作者|	修改内容 |
|----|---|----|--- |
|v1.0 |    2017-01-31 |    浦耀宗 |   第一版 |
|v1.1    | 2017-02-11  |   浦耀宗  |   修正BUG |
|v1.2    | 2017-06-06    | 浦耀宗  |   完善内容 |
|v1.3    | 2017-08-13    | 浦耀宗  |   修正BUG，完善内容 |


目录
- [SDK能做什么](#SDK能做什么)
- [环境要求](#环境要求)
- [开放环境配置](#开放环境配置)
- 如何把SDK加入项目中
- 语音对话
- 地图和行走
- 传感器
- 控制手臂
- 人脸识别
- 视频
- 注意事项
 


-	## SDK能做什么
我们的SDK是以aar（Android Archive [参考链接](https://developer.android.com/studio/projects/android-library.html#aar-contents)）形式集成进软件，提供了基础的语音对话、传感器、表情控制、地图和行走、控制手臂的接口


-	## 环境要求

```java
Android Studio 	2.3.3 or above
JDK				1.7 or above
gradle			 2.3.3 or above
```


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



-	## 开放环境配置

由于机器人的调试接口在屏幕内部，所以只能通过adb wifi的形式来进行，具体方法参照文档

[HowToSetUpAdbWifi]()




## 功能

小雪人SDK以aar形式集成功能包括以下：

1. 语音
    1.  语音识别 (Recognize) 
    2.  语义理解 (Understand)
    3.  语音合成 (Text To Speech)
    4.  [英文语音识别 google-speech-api](https://github.com/ppdayz/snowbot_i18n/tree/master/app/src/main/java/com/csjbot/snowbot/services/google_speech)
    
2. 传感器
    1.  触摸传感器
3. 面部表情控制
    1.  表情控制
    2.  表情资源 
4. 底盘控制
    1.  运动控制
    2.  地图控制
5. 上身控制
    1.  手臂控制
    2.  触摸传感器
