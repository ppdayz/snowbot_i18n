package com.csjbot.snowbot.utils.OkHttp;

/**
 * @author: jl
 * @Time: 2016/10/30
 * @Desc:
 */

public class DisposeDataHandle {
    public DisposeDataListener mListener = null;
    public Class<?> mclass = null;

    public DisposeDataHandle(DisposeDataListener mListener, Class<?> clazz) {
        this.mListener = mListener;
        this.mclass = clazz;
    }


}
