package com.csjbot.snowbot.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.csjbot.snowbot.R;

import java.io.OutputStream;


public class FloatingWindowsService extends Service {

    //定义浮动窗口布局
    private LinearLayout mFloatLayout;
    private WindowManager.LayoutParams wmParams;
    //创建浮动窗口设置布局参数的对象
    private WindowManager mWindowManager;
    private ImageView mFloatView, float_back, float_vol_up, float_vol_down;
    private View contentLayout;
    private Handler mHandler = new Handler();
    private AudioManager audio;


    @Override
    public void onCreate() {
        super.onCreate();
        audio = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
        createFloatView();
    }

    public void Restart() {
        Intent mIntent = new Intent();
        ComponentName comp = new ComponentName(getApplication().getPackageName(), "com.csjbot.snowbot.activity.LauncherActivity");
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mIntent.setComponent(comp);
        mIntent.setAction("android.intent.action.VIEW");
        startActivity(mIntent);

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private AlphaAnimation mHideAnimation = null;

    private AlphaAnimation mShowAnimation = null;

    /**
     * View渐隐动画效果
     */
    private void setHideAnimation(final View view, int duration) {
        if (null == view || duration < 0) {
            return;
        }

        if (null != mHideAnimation) {
            mHideAnimation.cancel();
        }

        mHideAnimation = new AlphaAnimation(1.0f, 0.0f);
        mHideAnimation.setDuration(duration);
        mHideAnimation.setFillAfter(true);

        view.startAnimation(mHideAnimation);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.INVISIBLE);
            }
        }, duration);
    }

    /**
     * View渐现动画效果
     */
    private void setShowAnimation(final View view, int duration) {
        if (null == view || duration < 0) {
            return;
        }

        if (null != mShowAnimation) {
            mShowAnimation.cancel();
        }


        mShowAnimation = new AlphaAnimation(0.0f, 1.0f);
        mShowAnimation.setDuration(duration);
        mShowAnimation.setFillAfter(true);

        view.startAnimation(mShowAnimation);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.VISIBLE);
            }
        }, duration);
    }

    private void createFloatView() {
        wmParams = new WindowManager.LayoutParams();
        //获取WindowManagerImpl.CompatModeWrapper
        mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        //设置window type
        wmParams.type = LayoutParams.TYPE_PHONE;
        //设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags =
//          LayoutParams.FLAG_NOT_TOUCH_MODAL |
                LayoutParams.FLAG_NOT_FOCUSABLE
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
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.float_window_layout, null);
        //添加mFloatLayout
        mWindowManager.addView(mFloatLayout, wmParams);

        //浮动窗口按钮
        mFloatView = (ImageView) mFloatLayout.findViewById(R.id.floating_btn);
        contentLayout = mFloatLayout.findViewById(R.id.contentLayout);
        float_back = (ImageView) mFloatLayout.findViewById(R.id.float_back);
        float_vol_up = (ImageView) mFloatLayout.findViewById(R.id.float_vol_up);
        float_vol_down = (ImageView) mFloatLayout.findViewById(R.id.float_vol_down);

        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        GestureDetector gestureDetector = new GestureDetector(this, new OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (contentLayout.getVisibility() == View.VISIBLE) {
                    setHideAnimation(contentLayout, 200);
                } else {
                    setShowAnimation(contentLayout, 200);
                }
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                wmParams.x = (int) e2.getRawX() - mFloatLayout.getMeasuredWidth() + mFloatView.getMeasuredWidth() / 2;
                //25为状态栏的高度
                wmParams.y = (int) e2.getRawY() - mFloatView.getMeasuredHeight() / 2;
                mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
        //设置监听浮动窗口的触摸移动
        mFloatView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
                return  gestureDetector.onTouchEvent(event);
            }
        });

//        mFloatView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (contentLayout.getVisibility() == View.VISIBLE) {
//                    setHideAnimation(contentLayout, 200);
//                } else {
//                    setShowAnimation(contentLayout, 200);
//                }
//            }
//        });

        float_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RootShellCmd cmd = new RootShellCmd();
                cmd.simulateKey(KeyEvent.KEYCODE_BACK);
            }
        });

        float_vol_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);

            }
        });

        float_vol_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
//                Restart();
            }
        });
    }

    /**
     * 用root权限执行Linux下的Shell指令
     *
     * @author jzj
     * @since 2014-09-09
     */
    public class RootShellCmd {
        private OutputStream os;

        /**
         * 执行shell指令
         *
         * @param cmd 指令
         */
        public final void exec(String cmd) {
            try {
                if (os == null) {
                    os = Runtime.getRuntime().exec("su").getOutputStream();
                }
                os.write(cmd.getBytes());
                os.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 后台模拟全局按键
         *
         * @param keyCode 键值
         */
        public final void simulateKey(int keyCode) {
            exec("input keyevent " + keyCode + "\n");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatLayout != null) {
            mWindowManager.removeView(mFloatLayout);
        }
    }


    public  void addFloatView(){
        if (mFloatLayout != null) {
            mWindowManager.removeView(mFloatLayout);
        }
    }


}
