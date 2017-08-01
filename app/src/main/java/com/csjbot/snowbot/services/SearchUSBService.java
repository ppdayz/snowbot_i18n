package com.csjbot.snowbot.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.core.util.SharedUtil;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.AdvertisementAct;
import com.csjbot.snowbot.utils.SharedKey;

import java.io.File;
import java.util.HashMap;

public class SearchUSBService extends Service {
    private Handler mHandler = new Handler();
    boolean isInRemove = false;
    private String usbPath = "";
    private View contentLayout;

    //定义浮动窗口布局
    private LinearLayout mFloatLayout;
    private WindowManager.LayoutParams wmParams;
    //创建浮动窗口设置布局参数的对象
    private WindowManager mWindowManager;
    private ImageView mFloatView;

    public SearchUSBService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createFloatView();
        startStorageListen();
    }

    private boolean checkUsbStorage() {
        return false;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastRec);
    }

    public void startStorageListen() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        filter.setPriority(1000);
        filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        filter.addAction(Intent.ACTION_MEDIA_BUTTON);
        filter.addAction(Intent.ACTION_MEDIA_CHECKING);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_NOFS);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        filter.addAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        filter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        filter.addAction(Intent.ACTION_MEDIA_SHARED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        registerReceiver(broadcastRec, filter);

    }


    /**
     * U盘插入流程：
     * <p>
     * MEDIA_CHECKING → MEDIA_MOUNTED → MEDIA_SCANNER_STARTED  →  MEDIA_SCANNER_FINISHED →  →  →  →  →  →
     * <p>
     * <p>
     * <p>
     * 移除U盘流程:
     * MEDIA_EJECT → MEDIA_SCANNER_STARTED   → MEDIA_SCANNER_FINISHED → MEDIA_UNMOUNTED →
     * MEDIA_REMOVED → MEDIA_SCANNER_STARTED → MEDIA_SCANNER_FINISHED
     */
    private final BroadcastReceiver broadcastRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            final String path = intent.getData().getPath();
            Csjlogger.debug("MediaAction " + action);
            Csjlogger.debug("path " + path);

            switch (action) {
                case Intent.ACTION_MEDIA_CHECKING:
                    break;
                case Intent.ACTION_MEDIA_MOUNTED:
                    usbPath = path;
                    SharedUtil.setPreferStr(SharedKey.USBPATH, usbPath);
                    mFloatView.setVisibility(View.VISIBLE);
                    break;
                case Intent.ACTION_MEDIA_SCANNER_STARTED:
                    break;
                case Intent.ACTION_MEDIA_SCANNER_FINISHED:
                    if (!isInRemove) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Csjlogger.debug(getMeidaInPath(new File(usbPath)).toString());
                            }
                        }).start();

                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                            }
                        }, 1000);
                    } else {
                        isInRemove = false;
                    }
                    break;
                case Intent.ACTION_MEDIA_SHARED:
                    break;
                case Intent.ACTION_MEDIA_UNMOUNTED:
                    break;
                case Intent.ACTION_MEDIA_REMOVED:
                case Intent.ACTION_MEDIA_EJECT:
                    SharedUtil.setPreferStr(SharedKey.USBPATH, "");
                    mFloatView.setVisibility(View.GONE);
                    isInRemove = true;
                    break;
                default:
                    break;
            }
        }
    };

    private void createFloatView() {
        wmParams = new WindowManager.LayoutParams();
        //获取WindowManagerImpl.CompatModeWrapper
        mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        //设置window type
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        //设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags =
//          LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//          LayoutParams.FLAG_NOT_TOUCHABLE
        ;

        //调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.TOP | Gravity.LEFT;

        // 以屏幕左上角为原点，设置x、y初始值
        wmParams.x = 0;
        wmParams.y = 0;

        //设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.float_usb_layout, null);
        //添加mFloatLayout
        mWindowManager.addView(mFloatLayout, wmParams);

        //浮动窗口按钮
        mFloatView = (ImageView) mFloatLayout.findViewById(R.id.floating_usb_btn);
        contentLayout = mFloatLayout.findViewById(R.id.contentLayout);

        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        //设置监听浮动窗口的触摸移动
        mFloatView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
                wmParams.x = (int) event.getRawX() - mFloatLayout.getMeasuredWidth() + mFloatView.getMeasuredWidth() / 2;
                //25为状态栏的高度
                wmParams.y = (int) event.getRawY() - mFloatView.getMeasuredHeight() / 2;
                mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                //刷新
                return false;
            }
        });

        mFloatView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchUSBService.this, AdvertisementAct.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        mFloatView.setVisibility(View.GONE);

        if (checkUsbStorage()) {
            mFloatView.setVisibility(View.VISIBLE);
        }
    }


    private String[] suffixs = {
            "wav", "jpg", "mp4", "3gp", "mp3"
    };

    public HashMap<String, String> getMeidaInPath(File file) {

        //从根目录开始扫描
        HashMap<String, String> fileList = new HashMap<>();
        getFileList(file, fileList);
        return fileList;
    }

    /**
     * @param path
     * @param fileList 注意的是并不是所有的文件夹都可以进行读取的，权限问题
     */
    private void getFileList(File path, HashMap<String, String> fileList) {
        //如果是文件夹的话
        if (path.isDirectory()) {
            //返回文件夹中有的数据
            File[] files = path.listFiles();
            //先判断下有没有权限，如果没有权限的话，就不执行了
            if (null == files)
                return;

            for (int i = 0; i < files.length; i++) {
                getFileList(files[i], fileList);
            }
        } else {//如果是文件的话直接加入
//            Csjlogger.debug(path.getAbsolutePath());
            //进行文件的处理
            String filePath = path.getAbsolutePath();
            //文件名
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            if (checkSuffix(fileName)) {
                //添加
                fileList.put(fileName, filePath);
            }

        }
    }

    private boolean isInList(String pf) {
        for (String str : suffixs) {
            if (str.equalsIgnoreCase(pf)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkSuffix(String fileName) {
        String[] token = fileName.split("\\.");
//        Logger.e(Arrays.toString(token));
        if (token.length > 1) {
            String pf = token[token.length - 1];
            if (isInList(pf)) {
                return true;
            }
//            Logger.e(pf);
        }

        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


}
