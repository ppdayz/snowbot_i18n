package com.csjbot.snowbot.utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.LauncherActivity;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.csjbot.snowbot.utils.UrlUtil.AUTOUPDATE;
import static dou.utils.HandleUtil.runOnUiThread;


/**
 * @项目名 SnowBot-SDK
 * @路径 name：android_serialport_api
 * @创建者 wql
 * @创建时间 2017/5/18 14:32
 * <p>
 * apk版本更新工具类
 * 需要添加 okhttp3   依赖
 * 服务器字段需要更改
 * <p>
 * 需创建一个ProgressBar  progress1的XML文件
 * <ProgressBar
 * android:id="@+id/progress2"
 * android:layout_width="match_parent"
 * android:layout_height="50dp"
 * style="?android:attr/progressBarStyleHorizontal"
 * android:max="100">
 * </ProgressBar>
 */


public class UpdateApkManagerUtil {
    private static final String TAG = "UpdateApkManagerUtil";
    // 应用程序Context

    private Context mContext;
    private LauncherActivity splashActivity; //入口是否是欢迎界面
    private boolean isFromWelcomeUI; //入口是否是欢迎界面
    // 提示消息
    private String updateMsg = "有最新的软件包，请下载！";
    // 下载安装包的网络路径
    private String apkUrl;
    private Dialog noticeDialog;// 提示有软件更新的对话框
    private Dialog downloadDialog;// 下载对话框
    private static final String savePath = "/sdcard/Download/";// 保存apk的文件夹
    private static final String saveFileName = savePath + "meihao.apk"; //保存的apk名称
    // 进度条与通知UI刷新的handler和msg常量
    private ProgressBar mProgress;
    private static final int DOWN_UPDATE = 1;
    private static final int DOWN_OVER = 2;
    private int progress;// 当前进度
    private Thread downLoadThread; // 下载线程
    private boolean interceptFlag = false;// 用户取消下载
    // 通知处理刷新界面的handler
    private Handler mHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE:
                    mProgress.setProgress(progress);
                    break;
                case DOWN_OVER:
                    installApk();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private String mVersion_name;


    public UpdateApkManagerUtil(Context context, boolean isFromWelcomeUI) {
        this.mContext = context;
        this.isFromWelcomeUI = isFromWelcomeUI;
        this.mContext = context;
        if (isFromWelcomeUI) {
            this.splashActivity = (LauncherActivity) context;
        }
    }

    // 显示更新程序对话框，供主程序调用nre
    public void checkUpdateInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    resolveJson();
                } catch (Exception e) {

                }

            }
        }).start();

    }

    private void resolveJson() throws Exception {

        // 01. 定义okhttp

        String url = AUTOUPDATE;//网址需要外部传入
        OkHttpClient okHttpClient_get = new OkHttpClient.Builder().connectTimeout(1, TimeUnit.SECONDS).build();
        // 02.请求体
        Request request = new Request.Builder().get()// get请求方式

                .url(url)
                .build();
        // 03.执行okhttp
        Response response;

        response = okHttpClient_get.newCall(request).execute();
        String json = response.body().string();


        JSONObject jsonObject = new JSONObject(json);
        // TODO: 2017/5/18 需要更改服务器字段
        JSONObject scoreObj = (JSONObject) jsonObject.get("result");
        JSONObject obj = (JSONObject) scoreObj.get("resule");
        int version_code = obj.getInt("version_code");//版本号
        apkUrl = obj.getString("url");//更新地址
        String channel = obj.getString("channel");//版本类型
        //版本名
        mVersion_name = obj.getString("version_name");
        String category = obj.getString("category");//机器人类型
        // 2.进行版本号比对
        int versionCode = getVersionCode(mContext);
        Log.e(TAG, "获取的数据是: 机器人类型:" + category + "  服务器版本号:"+version_code+"   当前版本"+versionCode+"  更新地址:" + apkUrl+ "  版本类型:" + channel + "  版本名:" + mVersion_name);
        if (version_code > versionCode) {
         runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 showNoticeDialog();
             }
         });
        }
    }

    private void showNoticeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                mContext);// Builder，可以通过此builder设置改变AleartDialog的默认的主题样式及属性相关信息
        builder.setTitle("软件版本更新");
        builder.setMessage(mVersion_name);
        builder.setPositiveButton("下载", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();// 当取消对话框后进行操作一定的代码？取消对话框
                showDownloadDialog();
            }
        });
        builder.setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        noticeDialog = builder.create();
        noticeDialog.show();
    }

    public void showDownloadDialog() {
        // TODO: 2017/5/18 xml需要从写
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("软件版本更新");
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.progress1, null);

        mProgress = (ProgressBar) v.findViewById(R.id.progress2);
        builder.setView(v);// 设置对话框的内容为一个View
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                interceptFlag = true;
                if (isFromWelcomeUI) {
                    //                    splashActivity.loadMainUI();
                }
            }
        });
        downloadDialog = builder.create();
        downloadDialog.show();
        downloadApk();
    }

    private void downloadApk() {
        downLoadThread = new Thread(mdownApkRunnable);
        downLoadThread.start();
    }

    //获取版本名
    public static String getVersionName(Context context, String packageName) {
        //包管理器
        PackageManager packageManager = context.getPackageManager();
        //参数一：要获取谁的信息就传谁的包名，参数二：标记,获取什么数据就给什么标记，给0，只获取最基本信息
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            String versionName = packageInfo.versionName;
            return versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "未知版本";
    }

    //获取版本号
    public static int getVersionCode(Context context) {
        //包管理器
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();

        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            Log.e(TAG, "包名: " + packageName);
            int versionCode = packageInfo.versionCode;
            return versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 1;
    }

    protected void installApk() {
        File apkfile = new File(saveFileName);
        if (!apkfile.exists()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                "application/vnd.android.package-archive");// File.toString()会返回路径信息
        mContext.startActivity(i);
    }

    private Runnable mdownApkRunnable = new Runnable() {
        @Override
        public void run() {
            URL url;
            try {
                url = new URL("http://"+apkUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                int length = conn.getContentLength();
                InputStream ins = conn.getInputStream();

                File file = new File(savePath);
                if (!file.exists()) {
                    file.mkdir();
                }
                String apkFile = saveFileName;
                File ApkFile = new File(apkFile);
                FileOutputStream outStream = new FileOutputStream(ApkFile);
                int count = 0;
                byte buf[] = new byte[1024];
                do {
                    int numread = ins.read(buf);
                    count += numread;
                    progress = (int) (((float) count / length) * 100);
                    // 下载进度
                    mHandler.sendEmptyMessage(DOWN_UPDATE);
                    if (numread <= 0) {
                        // 下载完成通知安装
                        mHandler.sendEmptyMessage(DOWN_OVER);
                        break;
                    }
                    outStream.write(buf, 0, numread);
                } while (!interceptFlag);// 点击取消停止下载
                outStream.close();
                ins.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}

