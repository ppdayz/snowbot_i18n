package com.csjbot.snowbot.activity.face.ui.icount;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.core.entry.Static;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.face.base.BaseActivity;
import com.csjbot.snowbot.activity.face.base.BaseApplication;
import com.csjbot.snowbot.activity.face.model.User;
import com.csjbot.snowbot.activity.face.util.DataSource;
import com.csjbot.snowbot.activity.face.util.DrawUtil;
import com.csjbot.snowbot.activity.face.util.GlideUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dou.helper.DouAdapter;
import dou.helper.DouViewHolder;
import dou.utils.DLog;
import dou.utils.DisplayUtil;
import mobile.ReadFace.YMFaceTrack;

import static com.csjbot.snowbot.R.id.page_cancle;


public class ManageFaceActivity extends BaseActivity implements View.OnClickListener {

    private TextView page_title, page_right;
    private ImageView insert_pic, insert_vid;
    private Button edit_person, delete_face;
    private View select_radio, edit_parent, edit_view;
    private ListView face_list_view;
    boolean selectAll = false;
    DouAdapter<User> douAdapter;
    private List<String> delete_list = new ArrayList<>();
    List<User> userList;
    private LinearLayout insert_pic_layout, insert_vid_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_face);

        page_title = (TextView) findViewById(R.id.page_title);
        edit_person = (Button) findViewById(R.id.edit_person);
        delete_face = (Button) findViewById(R.id.delete_face);
        select_radio = findViewById(R.id.select_radio);
        face_list_view = (ListView) findViewById(R.id.face_list_view);
        edit_parent = findViewById(R.id.edit_parent);
        page_title.setText(R.string.count_start_1);
        page_right = (TextView) findViewById(R.id.page_right);
        edit_view = findViewById(R.id.edit_view);

        page_right.setText(R.string.reset);
        page_right.setVisibility(View.GONE);

        insert_pic = (ImageView) findViewById(R.id.insert_pic);
        insert_vid = (ImageView) findViewById(R.id.insert_vid);

        insert_pic_layout = (LinearLayout) findViewById(R.id.insert_pic_layout);
        insert_vid_layout = (LinearLayout) findViewById(R.id.insert_vid_layout);


        View page_cancle = findViewById(R.id.page_cancle);
        page_cancle.setOnClickListener(this);

        edit_person.setOnClickListener(this);
        insert_pic_layout.setOnClickListener(this);
        insert_vid_layout.setOnClickListener(this);
        select_radio.setOnClickListener(this);
        delete_face.setOnClickListener(this);
    }

    public void initView() {

        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            insert_pic.getLayoutParams().width = 57 * sw / 620;
            insert_pic.getLayoutParams().height = 51 * sw / 620;
            insert_vid.getLayoutParams().width = 57 * sw / 620;
            insert_vid.getLayoutParams().height = 51 * sw / 620;
        }

        userList = DrawUtil.updateDataSource();
        enableView(userList.size() != 0);
        douAdapter = new DouAdapter<User>(ManageFaceActivity.this, userList, R.layout.user_item) {
            @Override
            public void convert(DouViewHolder douViewHolder, User user, int i) {
                View radio = douViewHolder.getView(R.id.select_radio);

                if (edit_person.getText().toString().equals(getString(R.string.manage_edit))) {
                    radio.setVisibility(View.GONE);
                } else {
                    radio.setVisibility(View.VISIBLE);
                }

                final String personId = user.getPersonId();
                if (delete_list.contains(personId)) {
                    radio.setBackgroundResource(R.drawable.manage_item_sele);
                } else {
                    radio.setBackgroundResource(R.drawable.manage_item_unse);
                }
                radio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (delete_list.contains(personId)) {
                            delete_list.remove(personId);
                            douAdapter.notifyDataSetChanged();
                        } else {
                            delete_list.add(personId);
                            douAdapter.notifyDataSetChanged();
                        }

                        if (delete_list.size() == userList.size() && !selectAll) {
                            selectAll = true;
                            select_radio.setBackgroundResource(R.drawable.manage_item_sele);
                        } else if (delete_list.size() != userList.size() && selectAll) {
                            selectAll = false;
                            select_radio.setBackgroundResource(R.drawable.manage_item_unse);
                        }
                    }
                });

                ImageView head = douViewHolder.getView(R.id.select_head);
                TextView desc = douViewHolder.getView(R.id.select_desc);
                if (user.getHead() != null) {
                    DLog.d("face is " + i);
                    GlideUtil.load(mContext, head, user.getHead());
                } else {
                    GlideUtil.load(R.drawable.transparent, head);
                }
                desc.setText("Face ID=" + user.getPersonId() + ", " + user.getName());
            }
        };

        face_list_view.setAdapter(douAdapter);

        face_list_view.setOnItemClickListener((parent, view, position, id) -> {
            if (!edit_person.getText().toString().equals(getString(R.string.manage_edit))) {
                final String personId = douAdapter.getItem(position).getPersonId();
                if (delete_list.contains(personId)) {
                    delete_list.remove(personId);
                    douAdapter.notifyDataSetChanged();
                } else {
                    delete_list.add(personId);
                    douAdapter.notifyDataSetChanged();
                }

                if (delete_list.size() == userList.size() && !selectAll) {
                    selectAll = true;
                    select_radio.setBackgroundResource(R.drawable.manage_item_sele);
                } else if (delete_list.size() != userList.size() && selectAll) {
                    selectAll = false;
                    select_radio.setBackgroundResource(R.drawable.manage_item_unse);
                }
            }
        });
    }

    public void topClick(View view) {
        switch (view.getId()) {
            case page_cancle:
                finish();
                break;
            case R.id.insert_pic_layout:
                startActivityForResult(new Intent(this, RegisterImageCameraActivity.class), 100);
                break;
            case R.id.insert_vid_layout:
                startActivityForResult(new Intent(this, RegisterVideoCameraActivity.class), 100);
                break;
            case R.id.page_right:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.delete_toast)
                        .setNegativeButton(R.string._sure, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                YMFaceTrack faceTrack = new YMFaceTrack();
                                faceTrack.initTrack(ManageFaceActivity.this, 0, 0);
                                faceTrack.resetAlbum();
                                DrawUtil.clearDb();
                                Toast.makeText(ManageFaceActivity.this, R.string.reset_db_success, Toast.LENGTH_SHORT).show();
                            }
                        }).setPositiveButton(R.string._not_sure, null);
                builder.create().show();
                break;
            case R.id.edit_person:
                if (edit_person.getText().toString().equals(getString(R.string.manage_edit))) {
                    edit_person.setText(R.string.manage_ending);
                    edit_parent.setVisibility(View.VISIBLE);
                    face_list_view.setPadding(0, 0, 0, DisplayUtil.dip2px(this, 50));
                } else {
                    edit_person.setText(R.string.manage_edit);
                    edit_parent.setVisibility(View.GONE);
                    face_list_view.setPadding(0, 0, 0, 0);
                    selectAll = false;
                    select_radio.setBackgroundResource(R.drawable.manage_item_unse);
                    delete_list.clear();
                }
                douAdapter.notifyDataSetChanged();
                break;
            case R.id.delete_face:

                if (delete_list.size() == 0) {
                    Toast.makeText(this, R.string.manage_toast_none_select, Toast.LENGTH_SHORT).show();
                } else {
                    new AlertDialog.Builder(this).setTitle(R.string.manage_sure_delete)
                            .setNegativeButton(R.string.manage_delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int position) {
                                    if (delete_list.size() == userList.size()) {
                                        //全部删除
                                        YMFaceTrack faceTrack = new YMFaceTrack();
                                        faceTrack.initTrack(ManageFaceActivity.this, 0, 0);
                                        faceTrack.resetAlbum();
                                        DrawUtil.clearDb();
                                    } else {
                                        //部分删除
                                        YMFaceTrack faceTrack = new YMFaceTrack();
                                        faceTrack.initTrack(ManageFaceActivity.this, 0, 0);

                                        for (int i = 0; i < delete_list.size(); i++) {
                                            String person_id = delete_list.get(i);
                                            DataSource dataSource = new DataSource(Static.CONTEXT);
                                            String imgPath = BaseApplication.getAppContext().getCacheDir()
                                                    + "/" + person_id + ".jpg";
                                            File imgFile = new File(imgPath);
                                            if (imgFile.exists()) {
                                                imgFile.delete();
                                            }
                                            dataSource.deleteById(person_id);
                                            faceTrack.deletePerson(Integer.parseInt(person_id));
                                        }
                                    }
                                    delete_list.clear();
                                    userList.clear();
                                    userList = DrawUtil.updateDataSource();
                                    enableView(userList.size() != 0);
                                    douAdapter.removeAllItems();
                                    douAdapter.addMoreItems(userList);
                                }
                            }).setPositiveButton(R.string._not_sure, null)
                            .create().show();

                }
                break;
            case R.id.select_radio:
                if (selectAll) {
                    selectAll = false;
                    select_radio.setBackgroundResource(R.drawable.manage_item_unse);
                    delete_list.clear();
                } else {
                    selectAll = true;
                    select_radio.setBackgroundResource(R.drawable.manage_item_sele);
                    delete_list.clear();
                    for (User user : userList) {
                        delete_list.add(user.getPersonId());
                    }
                }
                douAdapter.notifyDataSetChanged();
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        DLog.d(requestCode + ":" + resultCode);
        if (requestCode == 100 && resultCode == 101) {
            setResult(101, getIntent());
            finish();
        } else if (requestCode == 100) {
            //刷新一次
            long time = System.currentTimeMillis();
            delete_list.clear();
            userList.clear();
            DLog.d("clear end " + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();
            userList = DrawUtil.updateDataSource();
            DLog.d("update end " + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();
            enableView(userList.size() != 0);
            douAdapter.removeAllItems();
            douAdapter.addMoreItems(userList);
            DLog.d("list update end " + (System.currentTimeMillis() - time));
        }
    }

    private void enableView(boolean show) {
        if (show) {
            edit_view.setVisibility(View.VISIBLE);
        } else {
            edit_view.setVisibility(View.GONE);
            edit_parent.setVisibility(View.GONE);
            edit_person.setText(R.string.manage_edit);
            selectAll = false;
            select_radio.setBackgroundResource(R.drawable.manage_item_unse);
            delete_list.clear();
        }
    }

    @Override
    public void onClick(View v) {
        topClick(v);
    }
}
