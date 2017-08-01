package com.csjbot.snowbot.utils.OkHttp;

/**
 * @author: jl
 * @Time: 2016/10/30
 * @Desc:
 */

public interface DisposeDataListener {
     void onSuccess(Object responseObj);
     void onFail(Object reasonObj);

}
