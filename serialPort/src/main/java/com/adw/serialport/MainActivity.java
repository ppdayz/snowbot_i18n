package com.adw.serialport;

import java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import com.adw.serialport.R;

import android.os.Bundle;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android_serialport_api.SerialPort;

public class MainActivity extends Activity {
	
	
	private static final String TAG ="SP_HJ";
	
	private static final boolean DEBUG = true;	
	
	
	private Button openb,closeb;
	private TextView  tv;
	
	
//	protected Application mApplication;
	protected SerialPort mSerialPort;
	protected OutputStream mOutputStream;
	private InputStream mInputStream;
	
	
	
	/*
	 * debug info
	 */
	public  void LOGD(String tag){
		if(DEBUG){Log.d(TAG, tag);}
	}
	
	/*
	 *  info
	 */
	public  void LOGI(String tag){
		if(DEBUG){Log.i(TAG, tag);}
	}	
	
	/*  wrong  info
	 * 
	 */
	public  void LOGW(String tag){
		if(DEBUG){Log.w(TAG, tag);}
	}	

	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		openb = (Button)findViewById(R.id.openbut);
		closeb = (Button)findViewById(R.id.closebut);
		
		tv =(TextView)findViewById(R.id.tv);
		
		
//		mApplication = (Application) this.getApplication();
	//   "/dev/ttyS3"   //"BAUDRATE", "9600"));
		try {
			mSerialPort = new SerialPort(new File("/dev/ttyS3"), 9600, 0);
		
		
		mOutputStream = mSerialPort.getOutputStream();
		mInputStream = mSerialPort.getInputStream();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		pr = new PrinterReceipt(mOutputStream);
		
		
		
		openb.setOnClickListener(l);
		closeb.setOnClickListener(l);
		LOGD("-----------onCreate----------------");
		
	}

	
	private PrinterReceipt  pr;
	
	/*
	 * button  OnClickListener   
	 */
	
	OnClickListener l =new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			if(v.getId() == R.id.openbut){
//				Intent serviceIntent = new Intent("SpPrintService");
//				startService(serviceIntent);
				String mun = "AA 01 0E 02 00 28 FA 55";
		
				pr.PrintCommand(mun);
				tv.setText("发送值："+mun);
				LOGD("-----------OnClickListener-----------openbut-----");
				
				
			}else if(v.getId() == R.id.closebut){
				String mun = "AA 01 0E 00 00 28 FA 55";
	
				pr.PrintCommand(mun);
				tv.setText("发送值："+mun);
				LOGD("-----------OnClickListener-----------closebut-----");
				
			}
			
			
		}
	};
	
	
	
	
	
	/*
	 * 
	 */
	
	
	
	
	
	
	
	
	
	
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
