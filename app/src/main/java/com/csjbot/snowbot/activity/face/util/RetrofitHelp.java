package com.csjbot.snowbot.activity.face.util;

import com.csjbot.snowbot.activity.face.base.BaseApplication;

import java.io.File;
import java.io.IOException;

import dou.utils.DLog;
import dou.utils.DeviceUtil;
import mobile.ReadFace.FaceAnalyze;
import mobile.ReadFace.YMUtil;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by mac on 2017/4/9 下午4:03.
 */

public class RetrofitHelp {

    private APIService service;
    static volatile RetrofitHelp instance;
    private Retrofit retrofit;

    private RetrofitHelp(String baseUrl) {
        this.retrofit = (new Retrofit.Builder()).baseUrl(baseUrl).build();
        this.service = this.retrofit.create(APIService.class);
    }

    public static RetrofitHelp getInstance(String baseUrl) {
        if (instance == null) {
            synchronized (FaceAnalyze.class) {
                if (instance == null) {
                    instance = new RetrofitHelp(baseUrl);
                }
            }
        }
        return instance;
    }

    private String sync(Call<ResponseBody> call) {
        try {
            Response e = call.execute();
            return e.code() == 200 ? ((ResponseBody) e.body()).string() : "code = " + e.code() + "  " + e.message();
        } catch (IOException var3) {
            var3.printStackTrace();
            return null;
        }
    }

    private void async(Call<ResponseBody> call, final ApiListener listener) {
        if (listener == null) {
            YMUtil.i("listener not be null");
        } else {
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.code() == 200) {
                            listener.onCompleted(response.body().string());
                        } else {
                            listener.onCompleted("code = " + response.code() + "  " + response.message());
                        }
                    } catch (Exception var4) {
                        var4.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable e) {
                    listener.onError(e.getCause() + ":" + e.getMessage());
                }
            });
        }
    }

    public void postHeadToServer(File file_image, ApiListener listener) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file_image);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file_image.getAbsolutePath(), requestFile);
        String devicesId = DeviceUtil.getAndroidID(BaseApplication.getAppContext()) + "+" + DeviceUtil.getIMEI(BaseApplication.getAppContext());
        DLog.d("devicesId = " + devicesId);
        Call call = service.postImage(RequestBody.create(null, devicesId), body);
        this.async(call, listener);
    }

    interface APIService {
        @Multipart
        @POST("v1/face_images")
        Call<ResponseBody> postImage(@Part("dev_id") RequestBody var1, @Part okhttp3.MultipartBody.Part var2);
    }

    public interface ApiListener {
        void onError(String var1);

        void onCompleted(String var1);
    }

}
