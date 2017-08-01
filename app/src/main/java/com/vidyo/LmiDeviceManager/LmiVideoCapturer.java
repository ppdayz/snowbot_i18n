package com.vidyo.LmiDeviceManager;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.csjbot.csjbase.log.Csjlogger;

public class LmiVideoCapturer  {
	private static String TAG = "LmiVideoCapturer";
	Object lock=new Object();
	LmiVideoCapturerInternal capturerInternal;
	private boolean cameraStarted = false;
	private boolean internalConstructInProgress = false;
	private boolean cameraStartInProgress = false;
		//these timeout values are intentionally long
		//construction here can  potentially take up to a few seconds
	private final int internalConstructTimeout = 5000;
	private final int cameraStartTimeout = 5000;
	
	public LmiVideoCapturer(final Context context, final Activity activity, final String id)
	{
		Csjlogger.info(TAG, "Creating video capturer");
		capturerInternal = null; 
		
		if (Build.MANUFACTURER.toLowerCase().equalsIgnoreCase("amazon")) {
			String dev = Build.DEVICE;
			String model = Build.MODEL;
			Csjlogger.debug(TAG, "Device is =" + dev);
			Csjlogger.debug(TAG, "Model is =" + model);
			
			if (model.equalsIgnoreCase("Kindle Fire"))
				return;
		}
		
		internalConstructInProgress = true;
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				capturerInternal = new LmiVideoCapturerInternal(context, activity, id);													
				synchronized(lock) {
					Csjlogger.debug(TAG, "calling notify for lock");
					lock.notify();
					internalConstructInProgress = false;
				}
			}
		});

		if (internalConstructInProgress) {
			try{
				synchronized(lock){
					Csjlogger.debug(TAG, "calling wait for lock");
					lock.wait(internalConstructTimeout);
				}
		 	} catch (InterruptedException e) {
		 		Csjlogger.info(TAG, "Video capturer creation interrupted");
		 		e.printStackTrace();
		 	}
		} else {
			Csjlogger.debug(TAG, "creation already happened");
		}
		
		if (capturerInternal == null)
			Csjlogger.info(TAG, "Video capturer creation failed");
		else
			Csjlogger.info(TAG, "Video capturer created successfully");
		
	}
	
	private boolean verifyInternal() {
		if (internalConstructInProgress) {
			try{
			synchronized(lock) { 
					lock.wait(internalConstructTimeout);
				}
		 	} catch (InterruptedException e) {
		 		e.printStackTrace();
		 	}
		}
		
		if (capturerInternal == null) {
			Log.e(TAG, "Video capturer internal is null");
			return false;
		}
		
		return true;
	}

	public LmiVideoCapturerCapability[] getCapabilities() {
		if (!verifyInternal())
			return null;
		
		return capturerInternal.getCapabilities();
	}
	
public LmiVideoCapturerCapability[] getCapabilities(boolean anyFormat) {
		
		if (!verifyInternal())
			return null;
		
        return capturerInternal.getCapabilities(/*anyFormat*/);
	}

	public boolean start(final String format, final int width, final int height, final int frameRate) {

		Log.e(TAG, "start format: " + format + " width: " + width + " height: " + height + " framerate: " + frameRate);

		cameraStarted = false; 
		
		if (!verifyInternal())
			return false;
		
		cameraStartInProgress = true;
		
		capturerInternal.activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				cameraStarted = capturerInternal.start(format, width, height, frameRate);													
				synchronized(lock) {
			 		Csjlogger.info(TAG, "Camera start finished");

					lock.notify();
					cameraStartInProgress = false;
				}
			}
		});
				

		if (cameraStartInProgress) {
			try{
				synchronized(lock) { 
			 		Csjlogger.info(TAG, "waiting for camera start");
					lock.wait(cameraStartTimeout);
				}
		 	} catch (InterruptedException e) {
		 		Csjlogger.info(TAG, "Camera start interrupted");
		 		e.printStackTrace();
		 	}
		} else {
	 		Csjlogger.info(TAG, "NOT waiting for camera start");
		}
		
		if (!cameraStarted)
			Csjlogger.info(TAG, "Failed to start camera");
		
		return cameraStarted;
	}

	public void stop() { 
		Csjlogger.info(TAG, "Stop");

		if (!verifyInternal())
			return;
		
		while (cameraStartInProgress) {
			try{ 
				lock.wait(100);				
		 	} catch (InterruptedException e) {
		 		e.printStackTrace();
		 	}
		}	
		
		capturerInternal.activity.runOnUiThread(new Runnable()
											{
												@Override
												public void run(){
													capturerInternal.stop();
													cameraStarted = false;
												}
											}
								);
		
		while (capturerInternal.isActive()) {
			try{
				synchronized(this) {
					Csjlogger.debug(TAG, "Waiting for LmVideoCapturerInternal to stop");
					wait(500);
				}
		 	} catch (InterruptedException e) {
		 		e.printStackTrace();
		 	}
		}
		
		capturerInternal = null;
		
	}

	public int getOrientation() {
		if (!verifyInternal())
			return 0;
		
		return capturerInternal.getOrientation();
	}

	public boolean getMirrored() {
		Csjlogger.info(TAG, "getMirrored");
	
		if (!verifyInternal()) {
			Csjlogger.info(TAG, "getMirrored: verifyInternal failed, returning false");
			return false;
		}
		
		boolean retval = capturerInternal.getMirrored();
		Csjlogger.info(TAG, "getMirrored: returning " + retval);
		return retval;
	}

	public byte[] aquireFrame() {
		if (!verifyInternal())
			return null;
		
		return capturerInternal.aquireFrame();
	}

	public void releaseFrame(byte[] frame) {
		if (!verifyInternal())
			return;
		
		capturerInternal.releaseFrame(frame);
	}
	
	public int getFrameWidth(){
		if (!verifyInternal())
			return 0;
		
		return capturerInternal.getFrameWidth();
	}
	
	public int getFrameHeight(){
		if (!verifyInternal())
			return 0;
		
		return capturerInternal.getFrameHeight();
	}
	
	public static void onActivityPause() {
		Csjlogger.info(TAG, "onActivityPause");

		LmiVideoCapturerInternal.onActivityPause();
	}
	
	public static void onActivityResume() {
		Csjlogger.info(TAG, "onActivityResume");

		LmiVideoCapturerInternal.onActivityResume();
	}	
}
