/**
{file:
{name: LmiVideoCapturerManager.java}
	{description: Video Capturer Manager interface. }
	{copyright:
		(c) 2006-2009 Vidyo, Inc.,
		433 Hackensack Avenue, 6th Floor,
		Hackensack, NJ  07601.
		All rights reserved.
		The information contained herein is proprietary to Vidyo, Inc.
		and shall not be reproduced, copied (in whole or in part), adapted,
		modified, disseminated, transmitted, transcribed, stored in a retrieval
		system, or translated into any language in any form by any means
		without the express written consent of Vidyo, Inc.}
}
 */
package com.vidyo.LmiDeviceManager;

import android.hardware.Camera;
import android.os.Build;
import android.util.Log;

import com.csjbot.csjbase.log.Csjlogger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class LmiVideoCapturerManager {
	private static String TAG = "LmiVideoCapturerManager";
	private LmiVideoCapturerDeviceInfo[] deviceInfoArray = {};
	private boolean alreadyEnumreated = false;
	
	public static final int BACK = 0;
	public static final int FRONT = 1;
	
	public LmiVideoCapturerManager() {
        Csjlogger.info(TAG, "LmiVideoCapturerManager()");
		enumerateDevices();
	}
	
	public void destroy() {
		deviceInfoArray = null;
	}

	public LmiVideoCapturerDeviceInfo[] getDevices() {
        Csjlogger.info(TAG, "getDevices()");
		return deviceInfoArray;
	}

	public int getNumberOfDevices() {
        Csjlogger.info(TAG, "getNumberOfDevices()");
		enumerateDevices();
		return deviceInfoArray.length;
	}

	private void enumerateDevices() {
        Csjlogger.info(TAG, "enumerateDevices()");

        if (alreadyEnumreated)
        	return;
        alreadyEnumreated = true;	

		if (Build.MANUFACTURER.toLowerCase().equalsIgnoreCase("amazon")) {
			String dev = Build.DEVICE;
			String model = Build.MODEL.toLowerCase();
			if (model.equalsIgnoreCase("kindle fire")) {
				deviceInfoArray = new LmiVideoCapturerDeviceInfo[0];
				return;
			}
			// Kindle Fire HDX devices seem to be returning incorrect orientation, based on being phone oriented
			if (model.equalsIgnoreCase("kfapwa") || model.equalsIgnoreCase("kfapwi") || model.equalsIgnoreCase("kfthwa") || model.equalsIgnoreCase("kfthwi") 
					|| model.equalsIgnoreCase("sd4930ur")) {
			} else {
				addOneGenericCameras(FRONT);
				return;
			}
		}

        try {
			if (getSprintTwinCamDevice() == true) {
				Csjlogger.debug(TAG, "Found front cameara using TwinCamDevice");
			} else if (getHtcFrontFacingCamera() == true) {
				Csjlogger.debug(TAG, "Found front cameara using HtcFrontFacingCamera");
			} else if (getFrontFacingCameraUsingAPI9() == true) {
				Csjlogger.debug(TAG, "Found front cameara using API Level 9");
			} else if (getMotorolaFrontFacingCamera() == true) {
				Csjlogger.debug(TAG, "Found front cameara using Motorola API");
			} else {
				Camera mCamera = Camera.open();
				if (setDualCameraSwitch(mCamera) == true) {
					Csjlogger.debug(TAG, "Found front cameara using DualCameraSwitch");
					addTwoGenericCameras();
				} else {
					Camera.Parameters parameters;
					/* camera-id 2 for front camera */
					parameters = mCamera.getParameters();
					parameters.set("camera-id", 2);
					mCamera.setParameters(parameters);

					parameters = mCamera.getParameters();
					/* rear camera has to be rotated */
					try {
						if (Integer.parseInt(parameters.get("camera-id")) != 2) {
							Csjlogger.debug(TAG, "Found rear camera");
							addOneGenericCameras(BACK);
						} else {
							/* Samsung Galaxy S */
							Csjlogger.debug(TAG, "Found front cameara using camera-id");
							addTwoGenericCameras();
						}
					} catch (Exception ex) {
						Csjlogger.error(TAG, "Camera ID not found, assuming only rear");
						addOneGenericCameras(BACK);
					}
				}
				Csjlogger.debug(TAG, "Before release");
				mCamera.release();
				Csjlogger.debug(TAG, "After release");
			}
		} catch (Exception ex) {
			Log.e(TAG, "Failed to find cameras " + ex.toString());
			return;
		}
		return;
	}

	private void addOneGenericCameras(int whichCamera) {
		deviceInfoArray = new LmiVideoCapturerDeviceInfo[1];
		if (whichCamera == FRONT)
			deviceInfoArray[0] = new LmiVideoCapturerDeviceInfo("Camera 0", Integer.toString(FRONT), "Cameara 0", "Front");
		else
			deviceInfoArray[0] = new LmiVideoCapturerDeviceInfo("Camera 0", Integer.toString(BACK), "Cameara 0", "Back");
	}
	
	private void addTwoGenericCameras() {
		deviceInfoArray = new LmiVideoCapturerDeviceInfo[2];
		deviceInfoArray[0] = new LmiVideoCapturerDeviceInfo("Camera 0", Integer.toString(BACK), "Cameara 0", "Back");
		deviceInfoArray[1] = new LmiVideoCapturerDeviceInfo("Camera 1", Integer.toString(FRONT), "Cameara 1", "Front");
	}
	
	/* HTC EVO 4G */ 
	private boolean getSprintTwinCamDevice() {
		try {
			Csjlogger.info(TAG, "getSprintTwinCamDevice()");
			Method method = Class.forName("com.sprint.hardware.twinCamDevice.FrontFacingCamera").getDeclaredMethod("getFrontFacingCamera", (Class[]) null);
			addTwoGenericCameras();
			Csjlogger.info(TAG, "getSprintTwinCamDevice() Used");
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	/* Samsung Epic 4G */ 
	private boolean getHtcFrontFacingCamera() {
		try {
			Csjlogger.info(TAG, "getHtcFrontFacingCamera()");
			Method method = Class.forName("android.hardware.HtcFrontFacingCamera").getDeclaredMethod("getCamera", (Class[]) null);
			addTwoGenericCameras();
			Csjlogger.info(TAG, "getHtcFrontFacingCamera() Used");
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	/* Motorola Atrix 4g */ 
	private boolean getMotorolaFrontFacingCamera() {
		try {
			Csjlogger.info(TAG, "getMotorolaFrontFacingCamera()");
			Method method = Class.forName("com.motorola.hardware.frontcamera.FrontCamera").getDeclaredMethod("getFrontCamera", (Class[]) null);
			addTwoGenericCameras();
			Csjlogger.info(TAG, "getMotorolaFrontFacingCamera() Used");
			return true;
		} catch (Exception ex) {
			return false;
		}
	}
	
	/* Gingerbread API Only get the last camera until there is full support for front/back */
	private boolean getFrontFacingCameraUsingAPI9() {
		try {
			Csjlogger.info(TAG, "getFrontFacingCameraUsingAPI9()");
			Method getNumberOfCameras = Class.forName("android.hardware.Camera").getDeclaredMethod("getNumberOfCameras", new Class[]{});
			int cameraIndex = ((Integer)getNumberOfCameras.invoke(null, new Object[]{})).intValue();
			Object cameraInfo = Class.forName("android.hardware.Camera$CameraInfo").newInstance();
			Field fieldFacing = cameraInfo.getClass().getField("facing");
			Method getCameraInfo = Class.forName("android.hardware.Camera").getMethod("getCameraInfo", Integer.TYPE, Class.forName("android.hardware.Camera$CameraInfo"));

			deviceInfoArray = new LmiVideoCapturerDeviceInfo[cameraIndex];

			for (int i = 0; i < cameraIndex; i++) {
				getCameraInfo.invoke( null, i, cameraInfo);
				if (fieldFacing.getInt(cameraInfo) == 1) {
					/* front camera */
					int index = (cameraIndex == 1) ? 1 : i;
					deviceInfoArray[i] = new LmiVideoCapturerDeviceInfo("Camera " + i, "" + index, "Cameara " + i, "Front");
				} else {
					deviceInfoArray[i] = new LmiVideoCapturerDeviceInfo("Camera " + i, "" + i, "Cameara " + i, "Back");
				}
			}
			Csjlogger.info(TAG, "getFrontFacingCameraUsingAPI9() Used");
			return true;
		} catch (Exception ex) {
			Log.e(TAG, "getFrontFacingCameraUsingAPI9 API error: " + ex.toString());
			return false;
		}
	}

	/* Dell Streak */
	private boolean setDualCameraSwitch(Camera mCamera) {
		try {
			Csjlogger.info(TAG, "setDualCameraSwitch()");
			Method method = Class.forName("android.hardware.Camera").getMethod("DualCameraSwitch", int.class);
			addTwoGenericCameras();
			Csjlogger.info(TAG, "setDualCameraSwitch() Used");
			return true;
		} catch (Exception ex) {
			return false;
		}
	}
}
