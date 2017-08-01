package com.csjbot.snowbot.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.adapter.GralleryAdapter;
import com.csjbot.snowbot.adapter.GralleryAdapterDelClickInterface;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.bean.GralleryItem;
import com.csjbot.snowbot.utils.TimestampUtil;
import com.csjbot.snowbot_rogue.Events.AIUIEvent;
import com.csjbot.snowbot_rogue.Events.EventsConstants;
import com.csjbot.snowbot_rogue.app.CsjSpeechSynthesizer;
import com.csjbot.snowbot_rogue.utils.CSJToast;
import com.csjbot.snowbot_rogue.utils.Constant;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;

public class GalleryActivity extends CsjUIActivity implements GralleryAdapterDelClickInterface {
    private GralleryAdapter gralleryAdapter = null;
    private List<GralleryItem> gralleryItemList = new ArrayList<>();
    private int lastItem = -1;

    @BindView(R.id.grallery_listView)
    ListView grallery_listView;

    @BindView(R.id.showDate)
    TextView showDate;


    @Override


    public void afterViewCreated(Bundle savedInstanceState) {
        setupBack();
        initGralleryItems();
        gralleryAdapter = new GralleryAdapter(this, gralleryItemList);
        gralleryAdapter.setGralleryAdapterDelClickInterface(this);
        grallery_listView.setAdapter(gralleryAdapter);
        grallery_listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem >= gralleryAdapter.getCount()) {
                    return;
                }
                if (firstVisibleItem != lastItem) {
                    gralleryAdapter.setFirstVisibleItem(firstVisibleItem);
                    if (!showDate.getText().toString().equals(gralleryAdapter.getItem(firstVisibleItem).getDateTime())) {
                        showDate.setText(gralleryAdapter.getItem(firstVisibleItem).getDateTime());
                    }
                    gralleryAdapter.notifyDataSetChanged();
                    lastItem = firstVisibleItem;
                }
            }
        });
    }

    /**
     * 获取没有后缀的文件名字
     *
     * @param filename
     * @return
     */
    public String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }


    private GralleryItem getGralleryItemInList(String date) {
        for (GralleryItem item : gralleryItemList) {
            if (item.dateString.equals(date)) {
                return item;
            }
        }
        return null;
    }

    private void initGralleryItems() {
        File f = new File(Constant.SDCARD_VIDEO_PATH);
        File[] files = f.listFiles();// 列出所有文件
        if (files == null) {
            return;
        }

        long ts;
        String fileNameNoEx;
        for (File file : files) {
            if (file.getName().endsWith(".jpg")) {
                fileNameNoEx = getFileNameNoEx(file.getName());
                try {
                    ts = Long.valueOf(fileNameNoEx);
                } catch (NumberFormatException ex) {
                    continue;
                }

                String date = TimestampUtil.getTimestampString(new Timestamp(ts), TimestampUtil.DAY);
                GralleryItem item = getGralleryItemInList(date);

                if (item == null) {
                    item = new GralleryItem();
                    item.dateString = date;
                    item.fileList.add(fileNameNoEx);

                    gralleryItemList.add(item);
                    Csjlogger.debug("item == null , new and add ");
                } else {
                    item.fileList.add(fileNameNoEx);
                }
            }
        }
        //       List<GralleryItem> tempList = new ArrayList<>();
        //        tempList.addAll(CommonTool.getPhotoList(gralleryItemList));
        //        gralleryItemList.clear();
        //        gralleryItemList.addAll(tempList);
        Collections.sort(gralleryItemList);
    }

    @Override
    public void onDelClicked(String path) {
        dialogShow(path);
    }

    private void dialogShow(final String path) {
        new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.make_sure_delete))
                .setNegativeButton(getResources().getString(R.string.make_sure), (dialog, which) -> {
                    File videoFile = new File(path + ".3gp");
                    File imageFile = new File(path + ".jpg");
                    boolean ret = false;
                    if (videoFile.exists()) {
                        ret = videoFile.delete();
                    }
                    if (imageFile.exists()) {
                        ret = imageFile.delete();
                    }
                    if (ret) {
                        // FIXME  puyz add 很蠢得办法，要修改
                        gralleryItemList.clear();
                        initGralleryItems();
                        gralleryAdapter = new GralleryAdapter(GalleryActivity.this, gralleryItemList);
                        grallery_listView.setAdapter(gralleryAdapter);
                        gralleryAdapter.setGralleryAdapterDelClickInterface(GalleryActivity.this);
                    } else {
                        CSJToast.showToast(GalleryActivity.this, getResources().getString(R.string.delete_fail));
                    }
                })
                .setPositiveButton(getResources().getString(R.string.cancle), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create().show();
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_gallery;
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public boolean onAIUIEvent(AIUIEvent event) {
        switch (event.getTag()) {
            case EventsConstants.AIUIEvents.AIUI_EVENT_WAKEUP:
                finish();
                break;
            case EventsConstants.AIUIEvents.AIUI_EVENT_SUB_NLP:
                String question = (String) event.data;
                if (question.equals("已打开相册")) {
//                    CSJToast.showToast(context, "您已经在相册了");
                }
                CsjSpeechSynthesizer.getSynthesizer().stopSpeaking();
                    finish();
                    break;

            case EventsConstants.AIUIEvents.AIUI_EVENT_FORCE_SLEEP:
                CsjSpeechSynthesizer.getSynthesizer().stopSpeaking();
                finish();
                break;
        }
        return false;
    }
}
