package com.csjbot.snowbot.utils.OkHttp;


import android.util.Base64;

import com.android.core.util.CheckUtil;
import com.android.core.util.StrUtil;
import com.csjbot.csjbase.log.Csjlogger;

import java.io.File;
import java.util.Map;

import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * @author: jl
 * @Time: 2016/10/30
 * @Desc:
 */

public class CommonRequest {
    private static final MediaType FILE_TYPE = MediaType.parse("application/octet-stream");
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * @author: jl
     * @Time: 2016/10/31:13:46
     * @Desc: get请求
     */

    public static Request getRequest(String url, Map<String, String> map) {
        StringBuilder urlBuilder = new StringBuilder(url).append("?");
        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        return new Request.Builder().url(urlBuilder.substring(0, urlBuilder.length() - 1)).get().build();
    }

    /**
     * @author: jl
     * @Time: 2016/10/31:13:47
     * @Desc: pos表单
     */
    public static Request postRequest(String url, Map<String, String> map) {
        FormBody.Builder builder = new FormBody.Builder();
        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        FormBody body = builder.build();

        return new Request.Builder().url(url).post(body).build();
    }

    /**
     * post Json数据
     */
    public static Request postStrJsonRequest(String url, String strJson) {
        RequestBody body = RequestBody.create(JSON, strJson);
//        String credential = Credentials.basic("admin", "123456");
        return new Request.Builder().url(url)./*header("Authorization", credential).*/post(body).build();
    }

    /**
     * @author: jl
     * @Time: 2016/10/31:13:47
     * @Desc: postJosn
     */
    public static Request postJsonRequest(String url, Map<String, String> map) {
        StringBuffer tempParams = new StringBuffer();
        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                tempParams.append("\"").append(entry.getKey()).append("\"").append(":");
                if (entry.getValue().substring(0, 1).equals("{")) {
                    tempParams.append(entry.getValue());
                } else {
                    tempParams.append("\"").append(entry.getValue()).append("\"");
                }
                tempParams.append(",");
            }

        }
        StringBuffer newStrBf = new StringBuffer(tempParams.substring(0, tempParams.toString().length() - 1));
        newStrBf = newStrBf.insert(newStrBf.toString().length(), "}").insert(0, "{");
        newStrBf = newStrBf.insert(0, "{\"data\":").insert(newStrBf.length(), "}");
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), newStrBf.toString());
        Csjlogger.debug("注册请求", url + "  " + newStrBf.toString());
        return new Request.Builder().url(url).post(requestBody).build();
    }

    /**
     * @author: jl
     * @Time: 2016/10/31:13:47
     * @Desc: 文件上传
     */
    public static Request upLoad(String url, Map<String, Object> map) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        if (CheckUtil.isNotNull(map)) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getValue() instanceof File) {
                    builder.addPart(MultipartBody.Part.createFormData(entry.getKey(), null, RequestBody.create(FILE_TYPE, (File) entry.getValue())));
                } else {
                    builder.addFormDataPart(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
        }
        return new Request.Builder().url(url).post(builder.build()).build();
    }

    /**
     * @author: jl
     * @Time: 2016/11/1:10:36
     * @Desc: 文件下载
     */
    public static Request downloadFile(String downloadUrl) {
        if (StrUtil.isBlank(downloadUrl)) {
            return null;
        }
        return new Request.Builder().url(downloadUrl).build();

    }


}
