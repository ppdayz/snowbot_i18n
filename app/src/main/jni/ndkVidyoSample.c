#include <jni.h>
#include <stdio.h>
#include <string.h>
#include "VidyoClient.h"
#include "include/AndroidDebug.h"


jobject applicationJniObj = 0;
JavaVM* global_vm = 0;
static VidyoBool joinStatus = 0;
int x;
int y;
static VidyoBool allVideoDisabled = 0;

void SampleSwitchCamera(const char *name);
void SampleStartConference();
void SampleEndConference();
void SampleLoginSuccessful();

// Callback for out-events from VidyoClient
#define PRINT_EVENT(X) if(event==X) LOGI("GuiOnOutEvent recieved %s", #X);
void SampleGuiOnOutEvent(VidyoClientOutEvent event,
				   VidyoVoidPtr param,
				   VidyoUint paramSize,
				   VidyoVoidPtr data)
{

	LOGE("GuiOnOutEvent enter Event = %d\n",(int) event);
	if(event == VIDYO_CLIENT_OUT_EVENT_LICENSE)
	{
		VidyoClientOutEventLicense *eventLicense;
		eventLicense = (VidyoClientOutEventLicense *) param;

		VidyoUint error = eventLicense->error;
		VidyoUint vmConnectionPath = eventLicense->vmConnectionPath;
		VidyoBool OutOfLicenses = eventLicense->OutOfLicenses;

		LOGE("License Error: errorid=%d vmConnectionPath=%d OutOfLicense=%d\n", error, vmConnectionPath, OutOfLicenses);
	}
	else if(event == VIDYO_CLIENT_OUT_EVENT_SIGN_IN)
	{
		VidyoClientOutEventSignIn *eventSignIn;
		eventSignIn = (VidyoClientOutEventSignIn *) param;

		VidyoUint activeEid = eventSignIn->activeEid;
		VidyoBool signinSecured = eventSignIn->signinSecured;

		LOGE("activeEid=%d signinSecured=%d\n", activeEid, signinSecured);
		

		
		/*
		 * If the EID is not setup, it will return activeEid = 0
		 * in this case, we invoke the license request using below event
		 */
		if(!activeEid)
			(void)VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_LICENSE, NULL, 0);
	    //VidyoClientRequestCurrentUser user_id;
	    //VidyoUint ret = VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_GET_CURRENT_USER, &user_id, sizeof(user_id));
	    //LOGE("SG: logged in with %d. user_id.CurrentUserID: %s, user_id.CurrentUserDisplay: %s .", ret, user_id.currentUserID, user_id.currentUserDisplay);
	}
    else if(event == VIDYO_CLIENT_OUT_EVENT_SIGNED_IN)
	{
        // Send message to Client/application
        	LOGE("\SUCCESSn");
		SampleLoginSuccessful();  

    }

    else if(event == VIDYO_CLIENT_OUT_EVENT_REMOTE_SOURCE_ADDED)
    {
    	VidyoClientOutEventRemoteSourceChanged *remote;
        remote = (VidyoClientOutEventRemoteSourceChanged *) param;
        LOGE("Join Conference Event - received VIDYO_CLIENT_OUT_EVENT_CONFERENCE_ACTIVE\n" + remote->displayName);
    }

	else if(event == VIDYO_CLIENT_OUT_EVENT_CONFERENCE_ACTIVE)
	{
		LOGE("Join Conference Event - received VIDYO_CLIENT_OUT_EVENT_CONFERENCE_ACTIVE\n");
        SampleStartConference();
		joinStatus = 1;
		doResize(x,y);
	}
	else if(event == VIDYO_CLIENT_OUT_EVENT_CONFERENCE_ENDED)
	{
		LOGE("Left Conference Event\n");
		SampleEndConference();
		joinStatus = 0;
	}
	else if(event == VIDYO_CLIENT_OUT_EVENT_INCOMING_CALL)
	{
		LOGE("VIDYO_CLIENT_OUT_EVENT_INCOMING_CALL\n");
		VidyoBool ret = VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_ANSWER, NULL, 0);
		LOGE("SG: VIDYO_CLIENT_OUT_EVENT_INCOMING_CALL %d.", ret);
	}
    /*else if(event == VIDYO_CLIENT_OUT_EVENT_ADD_SHARE)
    {
        VidyoClientRequestWindowShares shareRequest;
        VidyoUint result;

        LOGI("VIDYO_CLIENT_OUT_EVENT_ADD_SHARE\n");
        memset(&shareRequest, 0, sizeof(shareRequest));
        shareRequest.requestType = LIST_SHARING_WINDOWS;
         VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_GET_WINDOW_SHARES,
                                              &shareRequest,
                                              sizeof(shareRequest));
        if (result != VIDYO_CLIENT_ERROR_OK)
        {
            LOGE("VIDYO_CLIENT_REQUEST_GET_WINDOW_SHARES failed");
        }
        else
        {
            LOGI("VIDYO_CLIENT_REQUEST_GET_WINDOW_SHARES success:%d, %d", shareRequest.shareList.numApp, shareRequest.shareList.currApp);

            shareRequest.shareList.newApp = shareRequest.shareList.currApp = 1;
            shareRequest.requestType = ADD_SHARING_WINDOW;
    
            result = VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_SET_WINDOW_SHARES,
                                              &shareRequest,
                                              sizeof(shareRequest));

            if (result != VIDYO_CLIENT_ERROR_OK)
            {
                LOGE("VIDYO_CLIENT_REQUEST_SET_WINDOW_SHARES failed\n");

            }
            else
            {
                LOGI("VIDYO_CLIENT_REQUEST_SET_WINDOW_SHARES success\n");
            }
        }
	}*/
	else if (event == VIDYO_CLIENT_OUT_EVENT_DEVICE_SELECTION_CHANGED)
	{
		VidyoClientOutEventDeviceSelectionChanged *eventOutDeviceSelectionChg = (VidyoClientOutEventDeviceSelectionChanged *)param;

		if (eventOutDeviceSelectionChg->changeType == VIDYO_CLIENT_USER_MESSAGE_DEVICE_SELECTION_CHANGED)
		{
			if (eventOutDeviceSelectionChg->deviceType == VIDYO_CLIENT_DEVICE_TYPE_VIDEO) 
			{
				SampleSwitchCamera((char *)eventOutDeviceSelectionChg->newDeviceName);
			}
		}

	}

}


static JNIEnv *getJniEnv(jboolean *isAttached)
{
	int status;
	JNIEnv *env;
	*isAttached = 0;

	status = (*global_vm)->GetEnv(global_vm, (void **) &env, JNI_VERSION_1_4);
	if (status < 0) 
	{
		//LOGE("getJavaEnv: Failed to get Java VM");
		status = (*global_vm)->AttachCurrentThread(global_vm, &env, NULL);
		if(status < 0) 
		{
			LOGE("getJavaEnv: Failed to get Attach Java VM");
			return NULL;
		}
		//LOGE("getJavaEnv: Attaching to Java VM");
		*isAttached = 1;
	}
	

	
	return env;
}

static jmethodID getApplicationJniMethodId(JNIEnv *env, jobject obj, const char* methodName, const char* methodSignature)
{
	jmethodID mid;
	jclass appClass;

	appClass = (*env)->GetObjectClass(env, obj);
	if (!appClass) 
	{
		LOGE("getApplicationJniMethodId - getApplicationJniMethodId: Failed to get applicationJni obj class");
		return NULL;
	}
	
	mid = (*env)->GetMethodID(env, appClass, methodName, methodSignature);
	if (mid == NULL)
	{
		LOGE("getApplicationJniMethodId - getApplicationJniMethodId: Failed to get %s method", methodName);
		return NULL;
	}
	
	return mid;
}

void SampleStartConference()
{
    jboolean isAttached;
    JNIEnv *env;
    jmethodID mid;
    jstring js;
    LOGE("SampleStartConference Begin");
    env = getJniEnv(&isAttached);
    if (env == NULL)
        goto FAIL0;
    
    mid = getApplicationJniMethodId(env, applicationJniObj, "callStartedCallback", "()V");
    if (mid == NULL)
        goto FAIL1;
    
    (*env)->CallVoidMethod(env, applicationJniObj, mid);
	
    if (isAttached)
    {
        (*global_vm)->DetachCurrentThread(global_vm);
    }
    LOGE("SampleStartConference End");
    return;
FAIL1:
    if (isAttached)
    {
        (*global_vm)->DetachCurrentThread(global_vm);
    }
FAIL0:
    LOGE("SampleStartConference FAILED");
    return;
}

void SampleLoginSuccessful()
{
    jboolean isAttached;
    JNIEnv *env;
    jmethodID mid;
    jstring js;
    LOGE("SampleLoginSuccessful Begin");
    env = getJniEnv(&isAttached);
    if (env == NULL)
        goto FAIL0;
    
    mid = getApplicationJniMethodId(env, applicationJniObj, "loginSuccessfulCallback", "()V");
    if (mid == NULL)
        goto FAIL1;
    
    (*env)->CallVoidMethod(env, applicationJniObj, mid);
	
    if (isAttached)
    {
        (*global_vm)->DetachCurrentThread(global_vm);
    }
    LOGE("SampleLoginSuccessful End");
    return;
FAIL1:
    if (isAttached)
    {
        (*global_vm)->DetachCurrentThread(global_vm);
    }
FAIL0:
    LOGE("SampleLoginSuccessful FAILED");
    return;
}


void SampleEndConference()
{
        jboolean isAttached;
        JNIEnv *env;
        jmethodID mid;
        jstring js;
        LOGE("SampleEndConference Begin");
        env = getJniEnv(&isAttached);
        if (env == NULL)
                goto FAIL0;

        mid = getApplicationJniMethodId(env, applicationJniObj, "callEndedCallback", "()V");
        if (mid == NULL)
                goto FAIL1;

        (*env)->CallVoidMethod(env, applicationJniObj, mid);
	
		if (isAttached)
		{
			(*global_vm)->DetachCurrentThread(global_vm);
		}
        LOGE("SampleEndConference End");
        return;
FAIL1:
		if (isAttached)
		{
			(*global_vm)->DetachCurrentThread(global_vm);
		}
FAIL0:
        LOGE("SampleEndConference FAILED");
        return;
}

void SampleSwitchCamera(const char *name)
{
        jboolean isAttached;
        JNIEnv *env;
        jmethodID mid;
        jstring js;
        LOGE("SampleSwitchCamera Begin");
        env = getJniEnv(&isAttached);
        if (env == NULL)
                goto FAIL0;

        mid = getApplicationJniMethodId(env, applicationJniObj, "cameraSwitchCallback", "(Ljava/lang/String;)V");
        if (mid == NULL)
                goto FAIL1;

        js = (*env)->NewStringUTF(env, name);
        (*env)->CallVoidMethod(env, applicationJniObj, mid, js);
	
		if (isAttached)
		{
			(*global_vm)->DetachCurrentThread(global_vm);
		}
        LOGE("SampleSwitchCamera End");
        return;
FAIL1:
		if (isAttached)
		{
			(*global_vm)->DetachCurrentThread(global_vm);
		}
FAIL0:
        LOGE("SampleSwitchCamera FAILED");
        return;
}

static jobject * SampleInitCacheClassReference(JNIEnv *env, const char *classPath) 
{
	jclass appClass = (*env)->FindClass(env, classPath);
	if (!appClass) 
	{
		LOGE("cacheClassReference: Failed to find class %s", classPath);
		return ((jobject*)0);
	}
	
	jmethodID mid = (*env)->GetMethodID(env, appClass, "<init>", "()V");
	if (!mid) 
	{
		LOGE("cacheClassReference: Failed to construct %s", classPath);
		return ((jobject*)0);
	}
	jobject obj = (*env)->NewObject(env, appClass, mid);
	if (!obj) 
	{
		LOGE("cacheClassReference: Failed to create object %s", classPath);
		return ((jobject*)0);
	}
	return (*env)->NewGlobalRef(env, obj);
}



JNIEXPORT void Java_com_csjbot_snowbot_app_MyApplication_Construct(JNIEnv* env, jobject javaThis,
                jstring caFilename, jstring logDir, jstring pathDir, jobject defaultActivity) {

	FUNCTION_ENTRY;
    
    
    VidyoClientAndroidRegisterDefaultVM(global_vm);
    VidyoClientAndroidRegisterDefaultApp(env, defaultActivity);
    
    const char *pathDirC = (*env)->GetStringUTFChars(env, pathDir, NULL);
    const char *logDirC = (*env)->GetStringUTFChars(env, logDir, NULL);
	const char *certificatesFileNameC = (*env)->GetStringUTFChars(env, caFilename, NULL);
	

 	//const char *logBaseFileName = "VidyoClientSample_";
 	//const char *installedDirPath = NULL;
 	//static const VidyoUint DEFAULT_LOG_SIZE = 1000000;
	//const char *logLevelsAndCategories = "fatal error warning debug@App info@AppEmcpClient debug@LmiApp debug@AppGui info@AppGui";
	VidyoRect videoRect = {(VidyoInt)(0), (VidyoInt)(0), (VidyoUint)(100), (VidyoUint)(100)};
    //VidyoUint logSize = DEFAULT_LOG_SIZE;

	applicationJniObj = SampleInitCacheClassReference(env, "com/csjbot/snowbot/app/MyApplication");
	// This will start logging to LogCat
    // Use mainly for debugging purposes
	VidyoClientConsoleLogConfigure(VIDYO_CLIENT_CONSOLE_LOG_CONFIGURATION_ALL);

	// Start the VidyoClient Library
    
    /* VidyoBool returnValue = VidyoClientStart(SampleGuiOnOutEvent,
     NULL,
     "/data/data/com.vidyo.vidyosample/cache/",
     logBaseFileName,
     "/data/data/com.vidyo.vidyosample/files/",
     logLevelsAndCategories,
     logSize,
     (VidyoWindowId)(0),
     &videoRect,
     NULL,
     &profileParam,NULL);
     if (returnValue)
*/
    VidyoClientLogParams logParam = {0};
    logParam.logLevelsAndCategories = "fatal error warning debug@AppVcsoapClient debug@App info@AppEmcpClient debug@LmiApp debug@AppGui info@AppGui";
    logParam.logSize = 5000000;
//    logParam.pathToLogDir = "/data/data/com.csjbot.snowbot.vidyosample/cache/";
    logParam.pathToLogDir = logDirC;
    logParam.logBaseFileName = "VidyoClientSample_";
//    logParam.pathToDumpDir = "/data/data/com.vidyo.vidyosample/files/";
    logParam.pathToDumpDir = logDirC;
    logParam.pathToConfigDir = pathDirC;
    
    

	LOGE("ApplicationJni_Construct: certifcateFileName=%s, configDir=%s, logDir=%s!\n", certificatesFileNameC, pathDirC, logDirC);


	VidyoBool returnValue = VidyoClientStart(SampleGuiOnOutEvent,
                                             NULL,
                                             &logParam,
											 (VidyoWindowId)(0),
											 &videoRect,
											 NULL,
											 NULL,
                                             NULL);
	if (returnValue)
	{
		LOGI("VidyoClientStart() was a SUCCESS\n");
	}
	else
	{
		//start failed
		LOGE("ApplicationJni_Construct VidyoClientStart() returned error!\n");
	}
    
        
    
	AppCertificateStoreInitialize(logDirC,certificatesFileNameC,NULL);

	FUNCTION_EXIT;
}

JNIEXPORT void Java_com_csjbot_snowbot_app_MyApplication_Login(JNIEnv* env, jobject javaThis,
		jstring vidyoportalName, jstring userName, jstring passwordName) {

	FUNCTION_ENTRY;
	LOGI("Java_com_csjbot_snowbot_app_MyApplication_Login() enter\n");

	const char *portalC = (*env)->GetStringUTFChars(env, vidyoportalName, NULL);
	const char *usernameC = (*env)->GetStringUTFChars(env, userName, NULL);
	const char *passwordC = (*env)->GetStringUTFChars(env, passwordName, NULL);

	LOGI("Starting Login Process\n");
	VidyoClientInEventLogIn event = {0};

	strlcpy(event.portalUri, portalC, sizeof(event.portalUri));
	strlcpy(event.userName, usernameC, sizeof(event.userName));
	strlcpy(event.userPass, passwordC, sizeof(event.userPass));

	LOGI("logging in with portalUri %s user %s ", event.portalUri, event.userName);
	VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_LOGIN, &event, sizeof(VidyoClientInEventLogIn));
 	FUNCTION_EXIT;
}

JNIEXPORT void Java_com_csjbot_snowbot_app_MyApplication_Record(JNIEnv* env, jobject javaThis) {

	FUNCTION_ENTRY;
	LOGI("Java_com_csjbot_snowbot_app_MyApplication_Login() enter\n");

	const char *portalC = (*env)->GetStringUTFChars(env, vidyoportalName, NULL);
	const char *usernameC = (*env)->GetStringUTFChars(env, userName, NULL);
	const char *passwordC = (*env)->GetStringUTFChars(env, passwordName, NULL);

	LOGI("Starting Login Process\n");
	VidyoClientInEventLogIn event = {0};

	strlcpy(event.portalUri, portalC, sizeof(event.portalUri));
	strlcpy(event.userName, usernameC, sizeof(event.userName));
	strlcpy(event.userPass, passwordC, sizeof(event.userPass));

	LOGI("logging in with portalUri %s user %s ", event.portalUri, event.userName);
	VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_LOGIN, &event, sizeof(VidyoClientInEventLogIn));
 	FUNCTION_EXIT;
}

JNIEXPORT void Java_com_csjbot_snowbot_app_MyApplication_GuestRoomLink(JNIEnv* env, jobject javaThis,
																			  jstring guestName, jstring portal, jstring room) {
	VidyoClientInEventRoomLink event = {0};

	const char *roomC = (*env)->GetStringUTFChars(env, room, NULL);
	const char *guestNameC = (*env)->GetStringUTFChars(env, guestName, NULL);
	const char *portalC = (*env)->GetStringUTFChars(env, portal, NULL);

	event.clientType = VIDYO_CLIENT_CLIENTTYPE_A;
	strlcpy(event.portalUri, portalC, sizeof(event.portalUri));
	strlcpy(event.displayName,guestNameC, sizeof(event.displayName));
	strlcpy(event.roomKey,roomC, sizeof(event.roomKey));

	LOGI("logging in with portalUri %s user %s ", event.portalUri, event.displayName);
	VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_ROOM_LINK, &event, sizeof(VidyoClientInEventRoomLink));
}

JNIEXPORT void Java_com_csjbot_snowbot_app_MyApplication_GuestSignIn(JNIEnv* env, jobject javaThis,
		jstring pak, jint guestID, jstring vmaddress, jstring loctag, jstring portal, jstring portalVersion, jstring guestName, jstring serverAddress) {

	FUNCTION_ENTRY;
	LOGI("Java_com_vidyo_vidyosample_VidyoSampleApplication_GuestSignIn() enter\n");

	const char *portalC = (*env)->GetStringUTFChars(env, portal, NULL);
	const char *pakC = (*env)->GetStringUTFChars(env, pak, NULL);

	const char *vmaddressC = (*env)->GetStringUTFChars(env, vmaddress, NULL);
	const char *loctagC = (*env)->GetStringUTFChars(env, loctag, NULL);
	const char *portalVersionC = (*env)->GetStringUTFChars(env, portalVersion, NULL);
	const char *guestNameC = (*env)->GetStringUTFChars(env, guestName, NULL);
	const char *serverAddressC = (*env)->GetStringUTFChars(env, serverAddress, NULL);

	LOGI("Sending SignIn Event\n");
	VidyoClientInEventSignIn event = {0};

	event.guestLogin = VIDYO_TRUE;
	event.guestId = guestID;
	event.emcpSecured = VIDYO_TRUE;
	event.proxyType =  VIDYO_CLIENT_VIDYO_PROXY;
	event.numberProxies = 1;
	strlcpy(event.vidyoProxyAddress[0],"uk1-me-vr1.emea.vidyo.com",sizeof(event.vidyoProxyAddress[0]));
	strlcpy(event.vidyoProxyPort[0],"443",sizeof(event.vidyoProxyPort[0]));
	strlcpy(event.portalAddress, portalC, sizeof(event.portalAddress));
	strlcpy(event.portalVersion, portalVersionC, sizeof(event.portalVersion));
	strlcpy(event.locationTag, loctagC, sizeof(event.locationTag));
	strlcpy(event.vmIdentity, vmaddressC, sizeof(event.vmIdentity));
	strlcpy(event.portalAccessKey, pakC, sizeof(event.portalAccessKey));
	strlcpy(event.userName, guestNameC, sizeof(event.userName));
	strlcpy(event.serverAddress, serverAddressC, sizeof(event.serverAddress));
	
	LOGI("logging in with portalUri %s user %s ", event.portalAddress, event.userName);
	VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_SIGN_IN, &event, sizeof(VidyoClientInEventSignIn));
 	FUNCTION_EXIT;
}

JNIEXPORT void JNICALL Java_com_csjbot_snowbot_app_MyApplication_Dispose(JNIEnv *env, jobject jObj2)
{
	FUNCTION_ENTRY;
	if (VidyoClientStop())
		LOGI("VidyoClientStop() SUCCESS!!\n");
        
	else
		LOGE("VidyoClientStop() FAILURE!!\n");

	FUNCTION_EXIT;
}


JNIEXPORT jint JNICALL JNI_OnLoad( JavaVM *vm, void *pvt )
{
	FUNCTION_ENTRY;
	LOGI("JNI_OnLoad called\n");
	global_vm = vm;
	FUNCTION_EXIT;
	return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL JNI_OnUnload( JavaVM *vm, void *pvt )
{
	FUNCTION_ENTRY
	LOGE("JNI_OnUnload called\n");
	FUNCTION_EXIT
}

JNIEXPORT void JNICALL Java_com_csjbot_snowbot_app_MyApplication_Render(JNIEnv *env, jobject jObj2)
{
//	FUNCTION_ENTRY;
	doRender();
//	FUNCTION_EXIT;
}


JNIEXPORT void Java_com_csjbot_snowbot_app_MyApplication_RenderRelease(JNIEnv *env, jobject jObj2)
{
	FUNCTION_ENTRY;
	doSceneReset();
	FUNCTION_EXIT;
}

 void JNICALL Java_com_csjbot_snowbot_app_MyApplication_Resize(JNIEnv *env, jobject jobj, jint width, jint height)
{
	FUNCTION_ENTRY;
	LOGI("JNI Resize width=%d height=%d\n", width, height);
	x = width;
	y = height;
	doResize( (VidyoUint)width, (VidyoUint)height);
	FUNCTION_EXIT;
}


JNIEXPORT void JNICALL Java_com_csjbot_snowbot_app_MyApplication_TouchEvent(JNIEnv *env, jobject jobj, jint id, jint type, jint x, jint y)
{
	//FUNCTION_ENTRY;
	doTouchEvent((VidyoInt)id, (VidyoInt)type, (VidyoInt)x, (VidyoInt)y);
	//FUNCTION_EXIT;
}


JNIEXPORT void JNICALL Java_com_csjbot_snowbot_app_MyApplication_SetOrientation(JNIEnv *env, jobject jobj,  jint orientation)
{
FUNCTION_ENTRY;

        VidyoClientOrientation newOrientation = VIDYO_CLIENT_ORIENTATION_UP;

        //translate LMI orienation to client orientation
        switch(orientation) {
                case 0: newOrientation = VIDYO_CLIENT_ORIENTATION_UP;
                                LOGI("VIDYO_CLIENT_ORIENTATION_UP");
                                break;
                case 1: newOrientation = VIDYO_CLIENT_ORIENTATION_DOWN;
                        LOGI("VIDYO_CLIENT_ORIENTATION_DOWN");
                        break;
                case 2: newOrientation = VIDYO_CLIENT_ORIENTATION_LEFT;
                        LOGI("VIDYO_CLIENT_ORIENTATION_LEFT");
                        break;
                case 3: newOrientation = VIDYO_CLIENT_ORIENTATION_RIGHT;
                        LOGI("VIDYO_CLIENT_ORIENTATION_RIGHT");
                        break;
        }

        doClientSetOrientation(newOrientation);

FUNCTION_EXIT;
return;
}

JNIEXPORT void JNICALL Java_com_csjbot_snowbot_app_MyApplication_SetPixelDensity(JNIEnv *env, jobject jobj, jdouble density)
{
    FUNCTION_ENTRY;
	doSetPixelDensity(density);

    FUNCTION_EXIT;
}

JNIEXPORT void JNICALL Java_com_csjbot_snowbot_app_MyApplication_SetCameraDevice(JNIEnv *env, jobject jobj, jint camera)
{
        //FUNCTION_ENTRY
	VidyoClientRequestConfiguration requestConfig;
	VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_GET_CONFIGURATION, &requestConfig, sizeof(VidyoClientRequestConfiguration));

	/*
	 * Value of 0 is (currently) used to signify the front camera
	 */
	if (camera == 0)
	{
		requestConfig.currentCamera = 0;
	}
	/*
	 * Value of 1 is (currently) used to signify the back camera
	 */
	else if (camera == 1)
	{
		requestConfig.currentCamera = 1;
	}
	VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_SET_CONFIGURATION, &requestConfig, sizeof(VidyoClientRequestConfiguration));

        //FUNCTION_EXIT
}


JNIEXPORT void JNICALL Java_com_csjbot_snowbot_app_MyApplication_SetPreviewModeON(JNIEnv *env, jobject jobj, jboolean pip)
{
	VidyoClientInEventPreview event;
	if (pip)
		event.previewMode = VIDYO_CLIENT_PREVIEW_MODE_DOCK;
	else
		event.previewMode = VIDYO_CLIENT_PREVIEW_MODE_NONE;
	VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_PREVIEW, &event, sizeof(VidyoClientInEventPreview));
}



void _init()
{
	FUNCTION_ENTRY;
	LOGE("_init called\n");
	FUNCTION_EXIT;
}

void _fini()
{
	FUNCTION_ENTRY;
	LOGE("_fini called\n");
	FUNCTION_EXIT;
}

JNIEXPORT void JNICALL Java_com_csjbot_snowbot_app_MyApplication_DisableAllVideoStreams(JNIEnv *env, jobject jobj)
{
    if (!allVideoDisabled)
    {
        //this would have the effect of stopping all video streams but self preview
        
        VidyoClientRequestSetBackground reqBackground = {0};
        reqBackground.willBackground = VIDYO_TRUE;
        (void)VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_SET_BACKGROUND,
                                     &reqBackground, sizeof(reqBackground));
        
        allVideoDisabled = VIDYO_TRUE;
    }
    
    
		
}

JNIEXPORT void JNICALL Java_com_csjbot_snowbot_app_MyApplication_EnableAllVideoStreams(JNIEnv *env, jobject jobj)
{
	
	{
		if (allVideoDisabled)
		{
            VidyoClientRequestSetBackground reqBackground = {0};
			reqBackground.willBackground = VIDYO_FALSE;
            (void)VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_SET_BACKGROUND,
                                         &reqBackground, sizeof(reqBackground));
            
			//this would have the effect of enabling all video streams 
			allVideoDisabled = VIDYO_FALSE;
//			rearrangeSceneLayout();
		}	
	}

}

JNIEXPORT void JNICALL Java_com_csjbot_snowbot_app_MyApplication_MuteCamera(JNIEnv *env, jobject jobj, jboolean MuteCamera)
{
	 FUNCTION_ENTRY;
	/*VidyoClientRequestConfiguration requestConfiguration;
	VidyoUint ret = VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_GET_CONFIGURATION, &requestConfiguration,
	                                                                             sizeof(requestConfiguration));
	  if (ret != VIDYO_CLIENT_ERROR_OK) {
	          LOGE("VIDYO_CLIENT_REQUEST_GET_CONFIGURATION returned error!");
	          return;
	  }
	requestConfiguration.enableHideCameraOnJoin = 1 ;
	
	VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_SET_CONFIGURATION, &requestConfiguration, sizeof(VidyoClientRequestConfiguration));
	LOGI("set enableHideCameraOnJoin=1");*/
    
	
	VidyoClientInEventMute event;
	event.willMute = MuteCamera;
	VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_MUTE_VIDEO, &event, sizeof(VidyoClientInEventMute));
	
	FUNCTION_EXIT;
}

JNIEXPORT jstring JNICALL Java_com_csjbot_snowbot_app_MyApplication_GetEID(JNIEnv *env, jobject jobj)
{
        FUNCTION_ENTRY
		jstring result;
        VidyoClientRequestGetEid  reqEID;
        (void)VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_GET_EID,
                                     &reqEID, sizeof(reqEID));
									 
		LOGI("Got EID %s",reqEID.EID);
		result = (*env)->NewStringUTF(env,reqEID.EID); 
		return result;
		FUNCTION_ENTRY
}

JNIEXPORT void JNICALL Java_com_csjbot_snowbot_app_MyApplication_DirectCall(JNIEnv *env, jobject jobj, jstring who)
{
	const char *member = (*env)->GetStringUTFChars(env, who, NULL);

	
	
	
	VidyoClientInEventPortalService service = {0};
	VidyoClientPortalServiceDirectCall event = {0};

	strlcpy(event.entityID, member, sizeof(event.entityID));
	event.entityType = VIDYO_CLIENT_ENTITY_TYPE_MEMBER;
	event.typeRequest = 0;
	memcpy(&(service.requests.directCall),&event,sizeof(event));
	LOGI("Making direct call %s",member);
	VidyoBool ret = VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_PORTAL_SERVICE, &event,sizeof(VidyoClientInEventPortalService));
	if (!ret)
		LOGW("Java_com_csjbot_snowbot_app_MyApplication_DirectCall() failed!\n");
}

JNIEXPORT void JNICALL Java_com_csjbot_snowbot_app_MyApplication_CancelCall(JNIEnv* env, jobject jobj, jboolean disablebar)
{
	LOGI("Java_com_csjbot_snowbot_app_MyApplication_CancelCall() enter\n");

	VidyoBool ret = VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_CANCEL, NULL,0);
	if (!ret)
		LOGW("Java_com_csjbot_snowbot_app_MyApplication_CancelCall() failed!\n");
}
JNIEXPORT void JNICALL Java_com_csjbot_snowbot_app_MyApplication_SignOff(JNIEnv* env, jobject jobj)
{
	VidyoBool ret = VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_SIGNOFF, NULL,0);

}
JNIEXPORT void JNICALL Java_com_csjbot_snowbot_app_MyApplication_StartConferenceMedia(JNIEnv *env, jobject jobj)
{
    doStartConferenceMedia();
}
JNIEXPORT void JNICALL Java_com_csjbot_snowbot_app_MyApplication_HideToolBar(JNIEnv* env, jobject jobj, jboolean disablebar)
{
LOGI("Java_com_csjbot_snowbot_app_MyApplication_HideToolBar() enter\n");
    VidyoClientInEventEnable event;
    event.willEnable = VIDYO_TRUE;
    VidyoBool ret = VidyoClientSendEvent(VIDYO_CLIENT_IN_EVENT_ENABLE_BUTTON_BAR, &event,sizeof(VidyoClientInEventEnable));
    if (!ret)
        LOGW("Java_com_csjbot_snowbot_app_MyApplication_HideToolBar() failed!\n");
}

// this function will enable echo cancellation
JNIEXPORT void JNICALL Java_com_csjbot_snowbot_app_MyApplication_SetEchoCancellation(JNIEnv *env, jobject jobj, jboolean aecenable)
{
	// get persistent configuration values
	  VidyoClientRequestConfiguration requestConfiguration;

	  VidyoUint ret = VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_GET_CONFIGURATION, &requestConfiguration,
	                                                                             sizeof(requestConfiguration));
	  if (ret != VIDYO_CLIENT_ERROR_OK) {
	          LOGE("VIDYO_CLIENT_REQUEST_GET_CONFIGURATION returned error!");
	          return;
	  }

	  // modify persistent configuration values, based on current values of on-screen controls
	  if (aecenable) {
	          requestConfiguration.enableEchoCancellation = 1;
	  } else {
	          requestConfiguration.enableEchoCancellation = 0;
	  }

	  // set persistent configuration values
	  ret = VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_SET_CONFIGURATION, &requestConfiguration,
	                                                           sizeof(requestConfiguration));
	  if (ret != VIDYO_CLIENT_ERROR_OK) {
	          LOGE("VIDYO_CLIENT_REQUEST_SET_CONFIGURATION returned error!");
	  }
	}
JNIEXPORT void JNICALL Java_com_csjbot_snowbot_app_MyApplication_SetSpeakerVolume(JNIEnv *env, jobject jobj, jint volume)

{
	//FUNCTION ENTRY
	VidyoClientRequestVolume volumeRequest;
	volumeRequest.volume = volume;
	VidyoClientSendRequest(VIDYO_CLIENT_REQUEST_SET_VOLUME_AUDIO_OUT, &volumeRequest,
		                                                           sizeof(volumeRequest));
	//FUNCTION EXIT
	return;
}

JNIEXPORT void JNICALL Java_com_csjbot_snowbot_app_MyApplication_DisableShareEvents(JNIEnv *env, jobject javaThisj)
{
FUNCTION_ENTRY
VidyoClientSendEvent (VIDYO_CLIENT_IN_EVENT_DISABLE_SHARE_EVENTS, 0, 0);
LOGI("Disable Shares Called - Vimal");
FUNCTION_EXIT;
}
JNIEXPORT void Java_com_csjbot_snowbot_app_MyApplication_Mute(JNIEnv *env, jobject javaThisj,jboolean isOpen)
 {
 FUNCTION_ENTRY;
 VidyoClientInEventMute muteEvent = {0};
 muteEvent.willMute = isOpen ? VIDYO_TRUE :  VIDYO_FALSE;

 VidyoClientSendEvent (VIDYO_CLIENT_IN_EVENT_MUTE_AUDIO_IN, &muteEvent, sizeof(VidyoClientInEventMute));
 LOGI("Disable Shares Called - Vimal");
 FUNCTION_EXIT;
 }
 JNIEXPORT void Java_com_csjbot_snowbot_app_MyApplication_IsShot(JNIEnv *env, jobject javaThisj,jboolean isOpen)
 {
 FUNCTION_ENTRY;
 VidyoClientInEventMute muteEvent = {0};
 muteEvent.willMute = isOpen ? VIDYO_TRUE :  VIDYO_FALSE;

 VidyoClientSendEvent (VIDYO_CLIENT_IN_EVENT_MUTE_VIDEO, &muteEvent, sizeof(VidyoClientInEventMute));
 LOGI("Disable Shares Called - Vimal");
 FUNCTION_EXIT;
 }

