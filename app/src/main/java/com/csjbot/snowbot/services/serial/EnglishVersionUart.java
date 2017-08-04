package com.csjbot.snowbot.services.serial;


import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.services.EventWakeup;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import android_serialport_api.SerialPort;

/**
 * Copyright (c) 2017, SuZhou CsjBot. All Rights Reserved.
 * www.csjbot.com
 * <p>
 * Created by 浦耀宗 at 2017/02/09 0009-14:06.
 * Email: puyz@csjbot.com
 */

public class EnglishVersionUart implements IConnector {
    private SerialPort mSerialPort = null;

    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    private DataReceive uartDataReceive;
    private boolean isRunning;
    private static final byte HEADER = (byte) 0xAA;
    private static final int BUFFER_READ_SIZE = 1;
    private static final int CONTENT_WITHOUT_DATA = 6;

    private EventBus ibus = EventBus.getDefault();

    /**
     * \r\n
     * \r\n
     * \r\n
     * WAKE UP!angle:
     * <p>
     * 三个\r\n 接 WAKE UP!angle:
     */
    private static final byte[] HEADERS = new byte[]{0x57, 0x41, 0x4B, 0x45, 0x20, 0x55, 0x50, 0x21, 0x61, 0x6E, 0x67, 0x6C, 0x065, 0x3A};

    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            Csjlogger.info("start 上身板接收线程");
            while (isRunning) {
                int size = 0;
                try {
                    byte[] buffer = new byte[BUFFER_READ_SIZE];

                    if (mInputStream == null) {
                        Csjlogger.error("mInputStream == null");
                        return;
                    }

                    size = mInputStream.read(buffer);
//                    Csjlogger.info("info");
                    if (size > 0) {
//                        onDataReceived(buffer, size);
//                        Csjlogger.info("EnglishVersionUart {}", NetDataTypeTransform.dumpHex(buffer));
                        strickBuffer(buffer, size);
                    }
                } catch (IOException e) {
                    Csjlogger.error(e);
                    return;
                }
            }
        }
    }

    // 256的缓存
    private byte[] mBuffer = new byte[256];
    private int offset = 0;

    private synchronized void strickBuffer(byte buf[], int size) {
        System.arraycopy(buf, 0, mBuffer, offset, size);
        offset += size;
        if (offset < mBuffer.length) {
            checkBuffer();
        } else {
            Arrays.fill(mBuffer, (byte) 0);
            offset = 0;
        }
    }

    private synchronized void checkBuffer() {
        if (mBuffer[0] == 0x57) {
            if (offset > HEADERS.length + 3) {
                byte[] tmp = Arrays.copyOf(mBuffer, HEADERS.length);
                if (Arrays.equals(tmp, HEADERS)) {
                    // 合法的头部
                    int anglelen = 3;
                    byte[] bytes = Arrays.copyOf(mBuffer, HEADERS.length + 3);
                    if (bytes[HEADERS.length + 1] == 0x0A) {
                        anglelen = 1;
                    } else if (bytes[HEADERS.length + 2] == 0x0A) {
                        anglelen = 2;
                    }
                    bytes = Arrays.copyOf(mBuffer, HEADERS.length + anglelen);

                    byte[] angleBytes = new byte[anglelen];
                    System.arraycopy(bytes, HEADERS.length, angleBytes, 0, anglelen);
                    int angle = Integer.valueOf(new String(angleBytes), 10);
                    Csjlogger.debug("angle = {}", angle);

                    ibus.post(new EventWakeup(0, angle));

                    Arrays.fill(mBuffer, (byte) 0);
                    offset = 0;
                } else {
                    // 需要舍弃
                    Arrays.fill(mBuffer, (byte) 0);
                    offset = 0;
                }
            }
        } else {
            Arrays.fill(mBuffer, (byte) 0);
            offset = 0;
        }
    }

    private void onDataReceived(byte[] buffer, int size) {
        if (uartDataReceive != null) {
            uartDataReceive.onReceive(Arrays.copyOf(buffer, size));
        }
    }

    @Override
    public int connect(String hostName, int port) {
        int ret = 0;
        try {
            mSerialPort = new SerialPort(new File(hostName), port, 0, "老五麦");
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
            isRunning = true;
            /* Create a receiving thread */
            mReadThread = new ReadThread();
            mReadThread.start();
//            test();
        } catch (SecurityException e) {
            Csjlogger.error("SecurityException", e);
            ret = Error.Uart.CONNECT_SECURITY_EXCEPTION;
        } catch (IOException e) {
            Csjlogger.error("IOException", e);
            ret = Error.Uart.CONNECT_IO_ERROR;
        }

        return ret;
    }

    @Override
    public int sendData(byte[] data) {
        int ret = Error.Uart.SEND_SUCCESS;
        if (mOutputStream != null) {
            try {
                mOutputStream.write(data);
//                Csjlogger.debug("向上身板发送数据 {} ", NetDataTypeTransform.dumpHex(data));
            } catch (IOException e) {
                Csjlogger.error("IOException", e);
                ret = Error.Uart.SEND_IO_ERROR;
            }
        } else {
            Csjlogger.error("mOutputStream == null");
            ret = Error.Uart.SEND_OUT_STREAM_NULL;
        }

        return ret;
    }

    @Override
    public void setDataReceive(DataReceive receive) {
        uartDataReceive = receive;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void destroy() {
        if (mSerialPort != null) {
            Csjlogger.info("destroy");
            mSerialPort = null;
            mReadThread.interrupt();
            isRunning = false;
        }
    }

    void test() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (i < 3) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    byte[] data = new byte[]{0x0A, 0x0D, 0x0A, 0x0D, 0x0A, 0x0D, 0x57, 0x41, 0x4B,
                            0x45, 0x20, 0x55, 0x50, 0x21, 0x61, 0x6E, 0x67, 0x6C, 0x65, 0x3A,
                            0x34, 0x0A, 0x0D, 0x0A, 0x0D, 0x0A, 0x0D, 0x0A, 0x23, 0x23, 0x23,
                            0x23, 0x23, 0x20, 0x49, 0x46, 0x4C, 0x59, 0x54, 0x45, 0x4B, 0x20, 0x58,
                            0x46, 0x4D, 0x31, 0x30, 0x35, 0x32, 0x31, 0x20, 0x23, 0x23, 0x23, 0x23,
                            0x23, 0x23, 0x23, 0x0D, 0x0A, 0x23, 0x23, 0x23, 0x23, 0x23, 0x20, 0x41,
                            0x50, 0x50, 0x5F, 0x56, 0x45, 0x52, 0x3A, 0x31, 0x2E, 0x30, 0x2E, 0x32,
                            0x2E, 0x31, 0x30, 0x30, 0x35, 0x20, 0x23, 0x23, 0x23, 0x23, 0x23, 0x0D,
                            0x0A, 0x23, 0x23, 0x23, 0x23, 0x23, 0x20, 0x4C, 0x49, 0x42, 0x5F, 0x56,
                            0x45, 0x52, 0x3A, 0x32, 0x2E, 0x30, 0x2E, 0x31, 0x2E, 0x31, 0x30, 0x30,
                            0x32, 0x20, 0x23, 0x23, 0x23, 0x23, 0x23, 0x0D, 0x0A, 0x23, 0x23, 0x23,
                            0x23, 0x23, 0x20, 0x58, 0x46, 0x4D, 0x31, 0x30, 0x35, 0x32, 0x31, 0x20,
                            0x43, 0x4D, 0x44, 0x20, 0x4C, 0x69, 0x73, 0x74, 0x3A, 0x20, 0x23, 0x23,
                            0x23, 0x23, 0x23, 0x0D, 0x0A, 0x43, 0x4D, 0x44, 0x3A, 0x56, 0x45, 0x52,
                            0x0A, 0x0D, 0x43, 0x4D, 0x44, 0x3A, 0x52, 0x45, 0x53, 0x45, 0x54, 0x0A,
                            0x0D, 0x43, 0x4D, 0x44, 0x3A, 0x42, 0x45, 0x41, 0x4D, 0x20, 0x20, 0x20,
                            0x75, 0x73, 0x61, 0x67, 0x65, 0x3A, 0x20, 0x42, 0x45, 0x41, 0x4D, 0x20,
                            0x6E, 0x75, 0x6D, 0x5B, 0x30, 0x7E, 0x33, 0x5D, 0x20, 0x0A, 0x0D, 0x0A,
                            0x0D, 0x45, 0x6E, 0x74, 0x65, 0x72, 0x20, 0x79, 0x6F, 0x75, 0x72, 0x20,
                            0x43, 0x4D, 0x44, 0x3A, 0x20};

                    for (byte b : data) {
                        strickBuffer(new byte[]{b}, 1);
                    }
                    i++;
                }
            }
        }).start();
    }
}
