# Google Speech 的使用 #
目录

1. 概览
2. 准备工作
3. 如何自己添加功能

## 让我们开始 ##
1. 概览 —— 我们这个例子实现了什么

我们的功能主要在这几个类中：

    com.csjbot.snowbot.services.google_speech.GoogleSpeechService    
	com.csjbot.snowbot.services.google_speech.VoiceRecorder
	com.csjbot.snowbot.services.serial.Old5MicSerialManager

其中

	类 VoiceRecorder 的作用是 连续不断地录音，并且当任何语音（声音）被收集到的时候，调用回调方法（VoiceRecorder.Callback）
 	Continuously records audio and notifies the Callback （VoiceRecorder.Callback）when voice (or any sound) is heard

	类 GoogleSpeechService 的作用是

	


1. 准备工作

	- 第一步，需要成为Google开发者，登录Google 官方网站 [https://cloud.google.com/](https://cloud.google.com/)
	- 第二步，使用Google Speech 服务 ， 网址是 [https://cloud.google.com/speech/](https://cloud.google.com/speech/)
	- 第三步，使用Google控制台，到 Project Page 选择或者创建一个项目，网址是 [https://console.cloud.google.com/](https://console.cloud.google.com/)
	- 第四步，Enable billing for your project ， 参见 [https://support.google.com/cloud/answer/6293499#enable-billing](https://support.google.com/cloud/answer/6293499#enable-billing)
	- 第五步，Enable the Cloud Speech API