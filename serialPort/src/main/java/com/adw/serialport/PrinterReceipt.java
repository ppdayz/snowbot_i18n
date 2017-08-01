package com.adw.serialport;

import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 
 * ��ӡƾ��
 * 
 * @author Administrator
 * 
 */
/**
 * @author Administrator
 * 
 */
public class PrinterReceipt {
	private OutputStream mOutputStream;
	public static boolean isStop = false;
	public static boolean isReturn = false; // ����ӡ��ʱ�Ƿ񷵻�
	byte[] mBuffer; // Ҫ���͵�����
	private boolean SerialPortisSucceed = true; // �����Ƿ�����
	public PrinterReceipt(OutputStream OPS) {
		super();
		this.mOutputStream = OPS;
	}

	public boolean Getissucceed() {
		return SerialPortisSucceed;
	}
	
	
	
	
	
	
	private static final String TAG ="SP_HJ";
	
	private static final boolean DEBUG = true;	
	/*
	 * debug info
	 */
	public  void LOGD(String tag){
		if(DEBUG){Log.d(TAG, tag);}
	}
	
	

	/*
	 * TimerTask mTimerTask = new TimerTask() {
	 * 
	 * @Override public void run() { if (!isReturn) { isReturn = true; Message
	 * msg = handler.obtainMessage(); msg.obj = null; msg.what =
	 * HandlerMessageIDs.REQUEST_GET_PRINTER_STATUS; msg.obj = "0";
	 * handler.sendMessage(msg); } } };
	 */

	public boolean PrintCommand(String str) {
		
		String mun = str;
//		Log.d("Serial_hj","---------PrintCommand-------------mun=="+mun);
		Log.d("Serial_hj","---------PrintCommand-------------mun==");
//		
		 byte[] content = getByteInfo(mun, mun);
		
		if (SerialPortisSucceed) {

			if (content != null && content.length != 0) {

				try {
					
					mOutputStream.write(content);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		return SerialPortisSucceed;
	}

	/**
	 * ����ӡ��
	 */
	public void CheckPrinter() {
		LOGD("----------------CheckPrinter----------mBuffer=="+mBuffer);
		if (SerialPortisSucceed) {
			mBuffer = new byte[3];
			int iIndex = 0;
			mBuffer[iIndex++] = 0x10;
			mBuffer[iIndex++] = 0x04;
			mBuffer[iIndex++] = 0x04;
			try {
				mOutputStream.write(mBuffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * ת��byte����
	 * 
	 * @param dataStr
	 * @return
	 */
	public byte[] getByteInfo(String dataStr, String num) {
		String[] s = dataStr.split(" ");
		int len = s.length;
		String[] n = num.split(" ");
		int len2 = n.length;
		int all_num = len+len2;
		byte[] bytes = new byte[all_num + 6];
		for (int i = 0; i < len; i++) {
			bytes[i] = (byte) (Integer.valueOf(s[i], 16) & 0xff);
		}
		/*
		 * bytes[len++] = 0x0a; bytes[len++] = 0x0a; bytes[len++] = 0x0a;
		 * bytes[len++] = 0x0a;
		 */
		for (int j = len; j < all_num; j++) {
			bytes[j] = (byte) (Integer.valueOf(n[j-len], 16) & 0xff);
		}
	
	//ȫ��	
		bytes[all_num++] = 0x0A;
		bytes[all_num++] = 0x0A;
		bytes[all_num++] = 0x0A;
		bytes[all_num++] = 0x0A;
		bytes[all_num++] = 0x1B;
		bytes[all_num++] = 0x69;
	//����
	/*	bytes[all_num++] = 0x0A;
		bytes[all_num++] = 0x0A;
		bytes[all_num++] = 0x0A;
		bytes[all_num++] = 0x0A;
		bytes[all_num++] = 0x1B;
		bytes[all_num++] = 0x6D;*/
		return bytes;
	}

	public byte[] getByteInfo() {

		int contentlen = 32;
		byte[] bytes = new byte[32 + 7];
		for (int i = 0; i < contentlen; i++) {
			bytes[i] = 0X31;
		}
		bytes[contentlen++] = 0x0a;
		bytes[contentlen++] = 0x0a;
		bytes[contentlen++] = 0x0a;
		bytes[contentlen++] = 0x0a;
		bytes[contentlen++] = 0x0a;
		bytes[contentlen++] = 0x1B;
		bytes[contentlen++] = 0x69;

		return bytes;

	}

	protected String onDataReceived(final byte[] buffer, final int size) {

		String hex = "";
		hex = Integer.toHexString(buffer[0] & 0xFF);
		System.out.println("----------hex---------------" + hex);
		return hex;
	}
}
