package com.csjbot.snowbot.utils.OkHttp;

import android.os.Handler;
import android.os.Looper;

import com.csjbot.csjbase.log.Csjlogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author: jl
 * @Time: 2016/11/1
 * @Desc:
 */

public class DownloadFileCallBack implements Callback {
    private DisposeDataListener listener;
    private Handler mHanlder;
    private String filePath;

    public DownloadFileCallBack(DownloadFileHandle downloadFileHandle) {
        this.listener = downloadFileHandle.mListener;
        this.filePath = downloadFileHandle.mFilePath;
        this.mHanlder = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onFailure(Call call, IOException e) {

    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        String TAG = "TAG";
        File file = new File(filePath);
        InputStream inputStream = null;
        byte[] buf = new byte[2048];
        int len = 0;
        FileOutputStream fileOutputStream = null;
        try {
            long total = response.body().contentLength();
            long current = 0;
            inputStream = response.body().byteStream();
            fileOutputStream = new FileOutputStream(file);
            while ((len = inputStream.read(buf)) != -1) {
                current += len;
                fileOutputStream.write(buf, 0, len);
            }
            fileOutputStream.close();
            listener.onSuccess(file);
        } catch (IOException e) {
            Csjlogger.error(TAG, e.toString());
            // FIXME :这里需要和其他地方一样，或者用的时候做好判断
            listener.onFail(e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                Csjlogger.error(TAG, e.toString());
            }
        }

    }
}
