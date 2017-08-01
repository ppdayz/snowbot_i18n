package com.csjbot.snowbot.utils.OkHttp;

/**
 * @author: jl
 * @Time: 2016/11/1
 * @Desc:
 */

public class DownloadFileHandle {
    public DisposeDataListener mListener = null;
    public String mFilePath = "";

    public DownloadFileHandle(DisposeDataListener disposeDataListener, String filePath) {
        this.mListener = disposeDataListener;
        this.mFilePath = filePath;
    }
}
