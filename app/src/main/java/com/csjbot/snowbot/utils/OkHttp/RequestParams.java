package com.csjbot.snowbot.utils.OkHttp;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: jl
 * @Time: 2016/10/30
 * @Desc:
 */

public class RequestParams {
    public ConcurrentHashMap<String, String> urlParam = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, Object> fileParam = new ConcurrentHashMap<>();


}
