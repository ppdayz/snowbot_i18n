package com.csjbot.snowbot.activity.face.ui.icount;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.core.entry.Static;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.face.base.BaseApplication;
import com.csjbot.snowbot.activity.face.base.BaseCameraActivity;
import com.csjbot.snowbot.activity.face.model.User;
import com.csjbot.snowbot.activity.face.util.DataSource;
import com.csjbot.snowbot.activity.face.util.DrawUtil;
import com.csjbot.snowbot.activity.face.util.TrackUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import dou.utils.BitmapUtil;
import dou.utils.DLog;
import dou.utils.StringUtils;
import mobile.ReadFace.YMFace;
import mobile.ReadFace.net.NetFaceTrack;


/**
 * Created by mac on 16/8/11.
 */
public class RegisterImageCameraActivity extends BaseCameraActivity implements View.OnClickListener {

    boolean isAdd = false;
    boolean isKnowing = false;
    int addCount = 0;
    int personId = -111;

    private RelativeLayout top_view;
    private TextView tips, next;
    private View show_image, camera_layout;
    private Button add_face;
    private View page_cancle;

    private boolean saveImage = false;
    private String age, gender, score;
    private boolean mRegisterFace = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unlock_insert_activity);
        setCamera_max_width(-1);
        initCamera();
        showFps(false);
    }

    public void initView() {
        TextView title = (TextView) findViewById(R.id.page_title);
        tips = (TextView) findViewById(R.id.tips);
        next = (TextView) findViewById(R.id.next);

        show_image = findViewById(R.id.show_image);
        add_face = (Button) findViewById(R.id.add_face);
        add_face.setVisibility(View.GONE);
        show_image.setBackgroundResource(R.drawable.nomal_1);


        add_face.getLayoutParams().width = getDoomW(450);
        add_face.getLayoutParams().height = getDoomW(170);

        title.setText(R.string.photograph_input);
        next.setVisibility(View.GONE);

        Button register = (Button) findViewById(R.id.register);
        top_view = (RelativeLayout) findViewById(R.id.top_view);
        camera_layout = findViewById(R.id.camera_layout);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add_face.setVisibility(View.VISIBLE);
                top_view.setVisibility(View.GONE);
                camera_layout.setVisibility(View.VISIBLE);
                add_face.setVisibility(View.VISIBLE);
                mRegisterFace = true;
            }
        });

        page_cancle = findViewById(R.id.page_cancle);
        page_cancle.setOnClickListener(this);
        add_face.setOnClickListener(this);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add_face.setVisibility(View.VISIBLE);
                addCount = 4;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mRegisterFace) {
            add_face.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void drawAnim(List<YMFace> faces, SurfaceView draw_view, float scale_bit, int cameraId, String fps) {
        TrackUtil.drawAnim(faces, draw_view, scale_bit, cameraId, fps, false);
    }

    @Override
    protected List<YMFace> analyse(final byte[] bytes, int iw, int ih) {
        final List<YMFace> faces = faceTrack.trackMulti(bytes, iw, ih);
//        final List<YMFace> faces = faceTrack.faceDetect(bytes, iw, ih);
//        YMFace face = faceTrack.faceDetect(bytes, iw, ih);
//        if (face != null)
//            faces.add(face);

        final byte[] data = bytes;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (faces != null && faces.size() > 0) {
                    initModel2(faces, data);
                } else {
                    tipSetText(getResources().getString(R.string.cant_found_you));
                    setUnEnable();
                }
            }
        });

        return faces;
    }

    void tipSetText(final String string) {
        if (!tips.getText().toString().equals(string))
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tips.setText(string);
                }
            });
    }

    boolean isTrue = false;
    static NetFaceTrack netFaceTrack = NetFaceTrack.getInstance("http://121.42.141.249:8011/", "5042a421fb4f1324ba7948370559f03e", "98b898104646a8c88345fadbbeeef8b360e69c58");

    private void initModel2(List<YMFace> faces, final byte[] bytes) {
        //rule 1 人脸框在指定框内
//        if (!isCenter(faces.get(0).getRect())) {
//            setUnEnable();
//            return;
//        }
        //rule 2 第一张图片是正脸的
        final float[] rect = faces.get(0).getRect();
        int limx = (int) (rect[0] + rect[2] / 2);
        int limy = (int) (rect[1] + rect[3] / 2);
        boolean isTouchable = TrackUtil.isTouchable(limx, limy);
        if (isAdd && !isTouchable) {
            Toast.makeText(RegisterImageCameraActivity.this, "面部移动速度过快，请慢点", Toast.LENGTH_SHORT).show();
            isAdd = false;
        }
        switch (addCount) {
            case 0:
                if (!isTrue) isTrue = isAdd1(faces);
                if (isTrue) {
                    tipSetText(getString(R.string.tipe_add1));
                    setEnable();
                    if (isAdd) {
                        isTrue = false;

                        //TODO net register async
                        //save image to file
//                        String register_name = "/sdcard/test.jpg";
//                        final File file = new File(register_name);
//                        saveImage(file, bytes);
//                        netFaceTrack.faceDetaction(file, "", new ApiListener() {
//                            @Override
//                            public void onError(String s) {
//                                DLog.d(s);
//                            }
//
//                            @Override
//                            public void onCompleted(String s) {
//                                DLog.d(s);
//                                String face_id = "";
//                                try {
//                                    face_id = new JSONObject(s).getJSONArray("faces")
//                                            .getJSONObject(0).getString("face_id");
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                                //检测到人脸时，开始创建people
//                                netFaceTrack.peopleCreate(face_id, "my_name", new ApiListener() {
//                                    @Override
//                                    public void onError(String s) {
//                                        DLog.d(s);
//                                    }
//
//                                    @Override
//                                    public void onCompleted(String s) {
//                                        DLog.d(s);
//                                        //创建people完成，返回personId为此人唯一标识，可以选择调用
//                                        // netFaceTrack.peopleAddFace()多添加几张人脸
//
//                                        //然后将此personId加入group
//                                        String personId = null;
//                                        try {
//                                            personId = new JSONObject(s).getString("person_id");
//                                        } catch (JSONException e) {
//                                            e.printStackTrace();
//                                        }
//                                        netFaceTrack.groupsCreate(personId, "my_group_name", new ApiListener() {
//                                            @Override
//                                            public void onError(String s) {
//                                                DLog.d(s);
//                                            }
//
//                                            @Override
//                                            public void onCompleted(String s) {
//                                                DLog.d(s);
//                                                //记住此groupId，，识别时需要使用
//                                                //此时可以进行识别了，到此测试结果为
//                                                //imageId为"0090b7aceaef036c165c6e8838710b02"
//                                                // faceId为"559759b7785d3307603eb037f0ea512b"{"status":"ok","image_id":"0090b7aceaef036c165c6e8838710b02","width":640,"height":480,"faces":[{"face_id":"559759b7785d3307603eb037f0ea512b","rect":
//                                                // personId为"d17a9141f6cb1d76e6cfcbf19bf2248e" {"status":"ok","person_id":"d17a9141f6cb1d76e6cfcbf19bf2248e","name":"my_name","face_count":1}
//                                                // groupId为"6d10d1cc338711798d1786daca9b4529" {"status":"ok","group_id":"6d10d1cc338711798d1786daca9b4529","name":"my_group_name","person_count":1}
//
//                                            }
//                                        });
//                                    }
//                                });
//                            }
//                        });
                        //TODO net idenfy async groupId为"6d10d1cc338711798d1786daca9b4529"
//                        String register_name = "/sdcard/test.jpg";
//                        final File file = new File(register_name);
//                        saveImage(file, bytes);
//                        netFaceTrack.faceDetaction(file, "", new ApiListener() {
//                            @Override
//                            public void onError(String s) {
//                                DLog.d(s);
//                            }
//
//                            @Override
//                            public void onCompleted(String s) {
//                                DLog.d(s);
//                                String face_id = "";
//                                try {
//                                    face_id = new JSONObject(s).getJSONArray("faces")
//                                            .getJSONObject(0).getString("face_id");
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                                netFaceTrack.faceIdentification(face_id, "6d10d1cc338711798d1786daca9b4529", new ApiListener() {
//                                    @Override
//                                    public void onError(String s) {
//                                        DLog.d(s);
//                                    }
//
//                                    @Override
//                                    public void onCompleted(String s) {
//                                        DLog.d(s);
//                                        //查看识别结果
//                                        //{"status":"ok","face_id":"9a6abd46f2a7b0b7adacaeea0d37af27",
//                                        // "group_id":"6d10d1cc338711798d1786daca9b4529",
//                                        // "candidates":[{"person_id":"d17a9141f6cb1d76e6cfcbf19bf2248e",
//                                        // "name":"my_name","confidence":0.9623942212977742}]}
//                                    }
//                                });
//                            }
//                        });
                        //同步调用，请在子线程中执行,略,如下识别
                        //TODO net register sync

                        //同步调用，请在子线程中执行
                        //TODO net idenfy sync groupId为"6d10d1cc338711798d1786daca9b4529"
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                String register_name = "/sdcard/test.jpg";
//                                final File file = new File(register_name);
//                                saveImage(file, bytes);
//                                String detect_result = netFaceTrack.faceDetaction(file, "", null);
//                                DLog.d(detect_result);
//                                String face_id = "";
//                                try {
//                                    face_id = new JSONObject(detect_result).getJSONArray("faces")
//                                            .getJSONObject(0).getString("face_id");
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                                String identify_result = netFaceTrack.faceIdentification(face_id, "6d10d1cc338711798d1786daca9b4529", null);
//                                DLog.d(identify_result);
//                                //查看识别结果
////                                 //{"status":"ok","face_id":"9a6abd46f2a7b0b7adacaeea0d37af27",
////                                 // "group_id":"6d10d1cc338711798d1786daca9b4529",
////                                 // "candidates":[{"person_id":"d17a9141f6cb1d76e6cfcbf19bf2248e",
////                                 // "name":"my_name","confidence":0.9623942212977742}]}
//                            }
//                        }).start();

                        //TODO local register
                        personId = faceTrack.identifyPerson(0);
                        if (personId == -111) {
                            addFace1(bytes, rect);
                        } else {
                            User user = DrawUtil.getUserById(personId + "");
                            String name = personId + "";
                            if (user != null) name = user.getName();

                            final AlertDialog.Builder builder = new AlertDialog.Builder(RegisterImageCameraActivity.this);
                            builder.setTitle(R.string.dalog_notice).setCancelable(false);
                            builder.setMessage(String.format(getString(R.string.dialog_msg), name))
                                    .setPositiveButton(R.string.ignore, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            personId = -111;
                                            addCount = 0;
                                        }
                                    })
                                    .setNegativeButton(R.string.update, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            faceTrack.deletePerson(personId);
                                            DataSource dataSource = new DataSource(Static.CONTEXT);
                                            String imgPath = Static.CONTEXT.getCacheDir()
                                                    + "/" + personId + ".jpg";
                                            File imgFile = new File(imgPath);
                                            if (imgFile.exists()) {
                                                imgFile.delete();
                                            }
                                            dataSource.deleteById(personId + "");
//                                            dataSource.insert(user);
//                                            BitmapUtil.saveBitmap(head, mContext.getCacheDir() + "/" + personId + ".jpg");
                                            addFace1(bytes, rect);
                                        }
                                    })
                                    .setNeutralButton(R.string.i_not_him, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            addFace1(bytes, rect);
                                        }
                                    });
                            builder.create().show();
                        }
                    }
                } else {
                    tipSetText(getString(R.string.please_face_to));
                    setUnEnable();
                }

                break;
            case 1:
                if (!isTrue) isTrue = isAdd2(faces);
                if (isTrue) {
                    tipSetText(getString(R.string.tips_add2));
                    setEnable();
                    if (isAdd) {
                        isTrue = false;
                        addCount++;
                        int i = faceTrack.updatePerson(personId, 0);
                        DLog.d("update 1 ：" + i);
                        saveImageFromCamera(personId, 1, bytes);
                        show_image.setBackgroundResource(R.drawable.nomal_3);

                    }
                } else {
                    tipSetText(getString(R.string.please_left_20));
                    setUnEnable();
                }

                break;
            case 2:
                if (!isTrue) isTrue = isAdd3(faces);
                if (isTrue) {
                    tipSetText(getString(R.string.tips_add3));
                    setEnable();
                    if (isAdd) {
                        isTrue = false;
                        addCount++;
                        int i = faceTrack.updatePerson(personId, 0);
                        DLog.d("update 2 ：" + i);
                        saveImageFromCamera(personId, 2, bytes);
                        show_image.setBackgroundResource(R.drawable.nomal_4);
                    }
                } else {
                    tipSetText(getString(R.string.please_up_20));
                    setUnEnable();
                }
                break;
            case 3:
                if (!isTrue) isTrue = isAdd4(faces);
                if (isTrue) {
                    tipSetText(getString(R.string.tipe_add4));
                    setEnable();
                    if (isAdd) {
                        isTrue = false;
                        addCount++;
                        int i = faceTrack.updatePerson(personId, 0);
                        DLog.d("update 3 ：" + i);
                        saveImageFromCamera(personId, 3, bytes);
                    }
                } else {
                    tipSetText(getString(R.string.please_down_20));
                    setUnEnable();
                }
                break;
            case 4:
                //TODO 结束
                DLog.d("end add person");
                doEnd();
                addCount++;
                break;
        }

        isAdd = false;
    }

    private boolean isAdd4(List<YMFace> faces) {//加低头数据

        YMFace face = faces.get(0);
        float facialOri[] = face.getHeadpose();
        float y = facialOri[1];

        if (y > -10) {
            return true;
        }
        return false;
    }

    private boolean isAdd3(List<YMFace> faces) {//加抬头数据

        YMFace face = faces.get(0);
        float facialOri[] = face.getHeadpose();
        float y = facialOri[1];
        if (y <= -10) {
            return true;
        }
        return false;
    }

    private boolean isAdd2(List<YMFace> faces) {//加侧脸数据

        YMFace face = faces.get(0);
        float facialOri[] = face.getHeadpose();
        float z = facialOri[2];
        if (Math.abs(z) >= 15) {
            return true;
        }
        return false;
    }

    private boolean isAdd1(List<YMFace> faces) {//加正脸数据

        YMFace face = faces.get(0);
        float facialOri[] = face.getHeadpose();

        float x = facialOri[0];
        float y = facialOri[1];
        float z = facialOri[2];

        if (Math.abs(x) <= 15 && Math.abs(y) <= 15 && Math.abs(z) <= 15) {
            return true;
        }
        return false;
    }

    @SuppressLint("StringFormatMatches")
    public void topClick(View view) {
        switch (view.getId()) {
            case R.id.page_cancle:
                onBackPressed();
                break;
            case R.id.add_face:
                if (!isAdd) isAdd = true;
                if (isKnowing) {
                    isAdd = false;
                    Toast.makeText(RegisterImageCameraActivity.this,
                            String.format(getString(R.string.know_yet), personId), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (addCount >= 4 || personId == -111) {
            stopCamera();
            finish();
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dalog_notice).setMessage(String.format(getString(R.string.dialog_msg1), addCount))
                    .setNegativeButton(R.string._sure_out, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            faceTrack.deletePerson(personId);
                            stopCamera();
                            finish();
                        }
                    }).setPositiveButton(R.string._keep_pre, null);
            builder.create().show();
        }
        DrawUtil.updateDataSource();
    }

    public void saveImageFromCamera(int personId, int count, byte[] yuvBytes) {
        if (!saveImage) return;
        File tmpFile = new File("/sdcard/img/fr/" + personId);
        if (!tmpFile.exists()) tmpFile.mkdirs();
        tmpFile = new File("/sdcard/img/fr/" + personId + "/img_" + count + ".jpg");
        saveImage(tmpFile, yuvBytes);
    }

    private void saveImage(File file, byte[] yuvBytes) {

        FileOutputStream fos = null;
        try {
            YuvImage image = new YuvImage(yuvBytes, ImageFormat.NV21, iw, ih, null);
            fos = new FileOutputStream(file);
            image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 90, fos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert fos != null;
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    boolean button_enable = false;

    void setEnable() {

        if (!button_enable) {
            add_face.setEnabled(true);
            add_face.setBackgroundResource(R.drawable.add_face_able);
            button_enable = !button_enable;
        }
    }

    void setUnEnable() {
        if (button_enable) {
            add_face.setEnabled(false);
            add_face.setBackgroundResource(R.drawable.add_face_unable);
            button_enable = !button_enable;
        }
    }

    void addFace1(byte[] bytes, float[] rect) {
        next.setVisibility(View.VISIBLE);
        personId = faceTrack.addPerson(0);//添加人脸
        int gender_score = faceTrack.getGender(0);
        gender = " ";
        if (gender_score >= 0)
            gender = faceTrack.getGender(0) == 0 ? "F" : "M";
//        score = faceTrack.getFaceBeautyScore(0) + "";
        score = " ";
        age = String.valueOf(TrackUtil.computingAge(faceTrack.getAge(0)));
        DLog.d("add Face 1 " + personId + " age :" + age + " gender: " + gender);
        saveImageFromCamera(personId, 0, bytes);
        if (personId > 0) {
            addCount++;//添加人脸成功

            show_image.setBackgroundResource(R.drawable.nomal_2);

            Bitmap image = BitmapUtil.getBitmapFromYuvByte(bytes, iw, ih);

            //TODO 此处在保存人脸小图
            if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                Matrix matrix = new Matrix();
                matrix.postRotate(270);
                head = Bitmap.createBitmap(image, iw - (int) rect[1] - (int) rect[3], (int) rect[0],
                        (int) rect[3], (int) rect[2], matrix, true);
            } else if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {

                if (BaseApplication.reverse_180) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(180);
                    head = Bitmap.createBitmap(image, iw - (int) rect[0] - (int) rect[2], ih - (int) rect[1] - (int) rect[3],
                            (int) rect[2], (int) rect[3], matrix, true);
                } else {
                    head = Bitmap.createBitmap(image, (int) rect[0], (int) rect[1],
                            (int) rect[2], (int) rect[3], null, true);
                }
            }
        } else {
            DLog.d("添加人脸失败！");
            Toast.makeText(mContext, "添加人脸失败！请重新添加", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap head = null;

    void doEnd() {

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
                        } else {
                            doEnd();
                            return;
                        }
//                        if (saveImage) {
//                            //修改文件夹名
//                            File tmpFile = new File("/sdcard/img/fr/" + personId);
//                            tmpFile.renameTo(new File("/sdcard/img/fr/" + name));
//                        }

                        User user = new User("" + personId, name, age, gender);
                        user.setScore(score);
                        DataSource dataSource = new DataSource(mContext);
                        dataSource.insert(user);
                        BitmapUtil.saveBitmap(head, mContext.getCacheDir() + "/" + personId + ".jpg");

                        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setCancelable(false);
                        builder.setMessage(R.string.image_sure_next)
                                .setNegativeButton(R.string._yes,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
//                                                addCount = 0;
//                                                personId = -111;
//                                                show_image.setBackgroundResource(R.drawable.nomal_1);
//                                                next.setVisibility(View.GONE);


                                                setResult(101, getIntent());
                                                onBackPressed();
                                            }
                                        })
                                .setPositiveButton(R.string._no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        DLog.d("back start");
                                        setResult(102, getIntent());
                                        onBackPressed();
                                    }
                                });
                        builder.create().show();

                    }
                });
        builder.create().show();
    }


    @Override
    public void onClick(View v) {
        topClick(v);
    }


}
