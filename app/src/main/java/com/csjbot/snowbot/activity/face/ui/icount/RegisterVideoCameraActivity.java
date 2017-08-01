package com.csjbot.snowbot.activity.face.ui.icount;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.android.core.entry.Static;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.face.base.BaseApplication;
import com.csjbot.snowbot.activity.face.base.BaseCameraActivity;
import com.csjbot.snowbot.activity.face.model.User;
import com.csjbot.snowbot.activity.face.util.DataSource;
import com.csjbot.snowbot.activity.face.util.DrawUtil;

import java.io.File;
import java.util.List;

import dou.utils.BitmapUtil;
import dou.utils.StringUtils;
import mobile.ReadFace.YMFace;


/**
 * Created by mac on 16/7/13.
 */
public class RegisterVideoCameraActivity extends BaseCameraActivity {

    private RelativeLayout top_view;
    private Button start_video_insert;
    private TextView count_time;
    private View camera_layout, myLine;
    private VideoView video_view;

    boolean startRegister;
    boolean cacluFamiliar;
    boolean stop = false;
    long save_time = 0;
    boolean isSave = false;
    boolean isHideVideo = false;
    private Bitmap head;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
        setCamera_max_width(-1);
        initCamera();

        TextView page_title = (TextView) findViewById(R.id.page_title);
        page_title.setText(R.string.video_input);
        top_view = (RelativeLayout) findViewById(R.id.top_view);
        start_video_insert = (Button) findViewById(R.id.start_video_insert);
        count_time = (TextView) findViewById(R.id.count_time);
        video_view = (VideoView) findViewById(R.id.video_view);

        myLine = findViewById(R.id.myLine);
        camera_layout = findViewById(R.id.camera_layout);
    }

    public void initView() {


        showVideo();
        Button register = (Button) findViewById(R.id.register);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                video_view.stopPlayback();
                isHideVideo = true;
                top_view.setVisibility(View.GONE);
                start_video_insert.setVisibility(View.VISIBLE);
                camera_layout.setVisibility(View.VISIBLE);
            }
        });

        start_video_insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRegister();


                ValueAnimator animator = ValueAnimator.ofInt(sw, 0);
                animator.setTarget(myLine);
                animator.setDuration(3000).start();
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int value = (int) animation.getAnimatedValue();
                        myLine.getLayoutParams().width = value;
                        myLine.requestLayout();

                        if (value == 0)
                            stop = true;
                    }
                });
            }
        });

        View page_cancle = findViewById(R.id.page_cancle);
        page_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }


    private void startRegister() {
        startRegister = true;
        isSave = false;
        start_video_insert.setClickable(false);
        start_video_insert.setBackgroundResource(R.mipmap.button_bgunse);
    }


    @Override
    protected void drawAnim(List<YMFace> faces, SurfaceView draw_view, float scale_bit, int cameraId, String fps) {
//        TrackUtil.drawAnim(faces, draw_view, scale_bit, cameraId, fps, false);
    }

    String gender;
    String age;

    @Override
    protected List<YMFace> analyse(byte[] bytes, int iw, int ih) {
        //注册使用

        if (isHideVideo) {
            isHideVideo = false;
            video_view.setVisibility(View.INVISIBLE);
        }

        if (startRegister) {

            if (save_time == 0 && !isSave) {
                save_time = System.currentTimeMillis();
            }
            faceTrack.registerFromVideo(bytes, iw, ih);

            if (System.currentTimeMillis() - save_time >= 1 && !isSave) {
                List<YMFace> faces = faceTrack.faceDetect(bytes, iw, ih);
                if (faces != null && faces.size() > 0 && !isSave) {
                    YMFace face = faces.get(0);
                    int gender_score = faceTrack.getGender(0);
                    age = faceTrack.getAge(0) + "";
                    gender = " ";
                    if (gender_score >= 0) {
                        gender = faceTrack.getGender(0) == 0 ? "F" : "M";
                        age = faceTrack.getAge(0) + "";
                        isSave = true;
                        float[] rect = face.getRect();
                        Bitmap image = BitmapUtil.getBitmapFromYuvByte(bytes, iw, ih);

                        //TODO 此处在保存人脸小图
                        Matrix matrix = new Matrix();
                        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {

                            matrix.postRotate(270);
                            head = Bitmap.createBitmap(image, iw - (int) rect[1] - (int) rect[3], (int) rect[0],
                                    (int) rect[3], (int) rect[2], matrix, true);
                        } else if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {

                            if (BaseApplication.reverse_180) {
                                matrix.postRotate(180);
                                head = Bitmap.createBitmap(image, iw - (int) rect[0] - (int) rect[2], ih - (int) rect[1] - (int) rect[3],
                                        (int) rect[2], (int) rect[3], matrix, true);
                            } else {
                                head = Bitmap.createBitmap(image, (int) rect[0], (int) rect[1],
                                        (int) rect[2], (int) rect[3], null, true);
                            }
                        }
                    }
                }
            }
            if (stop) {//personId
                stop = false;
                startRegister = false;

                final int personId = faceTrack.registerFromVideoEnd();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (personId > 0) {
                            //判断自己的数据库中是否存在此人，，存在则不重复添加
                            cacluSimlar(personId);
                        } else {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setCancelable(false);
                            builder.setMessage(R.string.video_re_failure)
                                    .setNegativeButton(R.string._yes,
                                            (dialogInterface, i) -> {
                                                start_video_insert.setClickable(true);
                                                start_video_insert.setBackgroundResource(R.mipmap.button_bg);
                                                top_view.setVisibility(View.VISIBLE);
                                                showVideo();
                                                start_video_insert.setVisibility(View.GONE);
                                            })
                                    .setPositiveButton(R.string._no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            finish();
                                        }
                                    });
                            builder.create().show();
                        }
                    }
                });
            }
        }
        return null;
    }

    void doEnd(final int personId) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(false);
        final EditText et = new EditText(mContext);
        et.setGravity(Gravity.CENTER);
        et.setHint(R.string.insert_nickname);
        et.setHintTextColor(0xffc6c6c6);
        builder.setTitle(R.string.dalog_notice)
                .setMessage(String.format(getString(R.string.dialog_msg2), personId))
                .setView(et)
                .setPositiveButton(R.string._sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String name = et.getText().toString();
                        if (!StringUtils.isEmpty(name.trim())) {
                            User user = new User("" + personId, name, age, gender);
                            DataSource dataSource = new DataSource(mContext);
                            dataSource.insert(user);
                            BitmapUtil.saveBitmap(head, mContext.getCacheDir() + "/" + personId + ".jpg");
                        } else {
                            doEnd(personId);
                            return;
                        }
                        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setCancelable(false);
                        builder.setMessage(R.string.image_sure_next)
                                .setNegativeButton(R.string._yes,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                setResult(101, getIntent());
                                                onBackPressed();
                                            }
                                        })
                                .setPositiveButton(R.string._no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        setResult(102, getIntent());
                                        onBackPressed();
                                    }
                                });
                        builder.create().show();
                    }
                });
        builder.create().show();
    }

    public void topClick(View view) {
        onBackPressed();
    }


    @Override
    public void onBackPressed() {
        stopCamera();
        super.onBackPressed();
    }

    private void showVideo() {
        video_view.setVisibility(View.VISIBLE);
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video_show);
        video_view.setVideoURI(uri);
        video_view.start();
        video_view.requestFocus();
        video_view.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                video_view.start();
            }
        });
    }


    private void cacluSimlar(final int personId) {

        User user = DrawUtil.getUserById(personId + "");
        if (user == null) {
            doEnd(personId);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(RegisterVideoCameraActivity.this);
            builder.setTitle(R.string.dalog_notice).setCancelable(false);
            //String.format(getString(R.string.dialog_msg), user.getName())
            builder.setMessage(String.format(getString(R.string.dialog_msg), user.getName()))
                    .setPositiveButton(R.string._not_sure, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            setResult(102, getIntent());
                            onBackPressed();
                        }
                    })
                    .setNegativeButton(R.string.manage_update, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            faceTrack.deletePerson(personId);
                            DataSource dataSource = new DataSource(Static.CONTEXT);
                            String imgPath = BaseApplication.getAppContext().getCacheDir()
                                    + "/" + personId + ".jpg";
                            File imgFile = new File(imgPath);
                            if (imgFile.exists()) {
                                imgFile.delete();
                            }
                            dataSource.deleteById(personId + "");
                            dataSource.insert(user);
                            BitmapUtil.saveBitmap(head, mContext.getCacheDir() + "/" + personId + ".jpg");
                            setResult(102, getIntent());
                            onBackPressed();
                        }
                    });

            builder.create().show();
        }
    }
}
