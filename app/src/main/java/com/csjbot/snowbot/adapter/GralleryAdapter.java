package com.csjbot.snowbot.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.PhotoActivity;
import com.csjbot.snowbot.activity.settings.VideoPlay;
import com.csjbot.snowbot.bean.GralleryItem;
import com.csjbot.snowbot.bean.LinePhoto;
import com.csjbot.snowbot.utils.CommonViewHolder;
import com.csjbot.snowbot_rogue.utils.Constant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.csjbot.snowbot.R.layout.item_grallery_list;

/**
 * Created by Administrator on 2016/8/26 0026.
 */
public class GralleryAdapter extends BaseAdapter {
    private Context context = null;
    private LayoutInflater mLayoutInflater = null;
    private int firstVisibleItem;
    private List<GralleryItem> gralleryItemList;
    private LinearLayout.LayoutParams layoutParams;
    private static final int COULNM_COUNT = 4;
    private GralleryAdapterDelClickInterface delClickInterface;
    private List<LinePhoto> allLinesData;
    private ImageView iv_img1;
    private ImageView iv_img2;
    private ImageView iv_img3;
    private ImageView iv_img4;
    private ImageView iv_video_hint1;
    private ImageView iv_video_hint2;
    private ImageView iv_video_hint3;
    private ImageView iv_video_hint4;

    public void setGralleryAdapterDelClickInterface(GralleryAdapterDelClickInterface inter) {
        delClickInterface = inter;
    }

    public GralleryAdapter(Context context, List<GralleryItem> items) {
        this.context = context;
        mLayoutInflater = LayoutInflater.from(context);
        this.gralleryItemList = items;

        allLinesData = new ArrayList<>();
        for (GralleryItem gralleryItem : gralleryItemList) {
            int row = gralleryItem.fileList.size() / COULNM_COUNT;
            if (gralleryItem.fileList.size() % COULNM_COUNT != 0) {
                row++;
            }
            for (int i = 0; i < row; i++) {
                LinePhoto linePhoto = new LinePhoto();
                linePhoto.setDateTime(gralleryItem.dateString);
                if (i == 0) {
                    linePhoto.setbDateFirst(true);
                }
                List<String> fileList = new ArrayList<>();
                for (int index = 0; (index < 4) && (index + i * 4) < gralleryItem.fileList.size(); index++) {
                    fileList.add(gralleryItem.fileList.get(index + 4 * i));
                }
                linePhoto.setFileList(fileList);
                allLinesData.add(linePhoto);
            }

        }

        layoutParams = new LinearLayout.LayoutParams(180, 180);
        layoutParams.topMargin = 8;
        layoutParams.leftMargin = 8;
        layoutParams.rightMargin = 8;
        layoutParams.bottomMargin = 8;
    }

    public void setGralleryItemList(List<GralleryItem> items) {
        this.gralleryItemList = items;
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return allLinesData.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public LinePhoto getItem(int position) {
        return allLinesData.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(item_grallery_list, null);
        }
        iv_img1 = (ImageView) convertView.findViewById(R.id.iv_img1);
        iv_img2 = (ImageView) convertView.findViewById(R.id.iv_img2);
        iv_img3 = (ImageView) convertView.findViewById(R.id.iv_img3);
        iv_img4 = (ImageView) convertView.findViewById(R.id.iv_img4);
        iv_video_hint1 = (ImageView) convertView.findViewById(R.id.iv_video_hint1);
        iv_video_hint2 = (ImageView) convertView.findViewById(R.id.iv_video_hint2);
        iv_video_hint3 = (ImageView) convertView.findViewById(R.id.iv_video_hint3);
        iv_video_hint4 = (ImageView) convertView.findViewById(R.id.iv_video_hint4);

        LinePhoto item = getItem(position);

        TextView showDate = CommonViewHolder.get(convertView, R.id.showDate);
//        LinearLayout videoContent = CommonViewHolder.get(convertView, videoContent);

        if (item.isbDateFirst()) {
            showDate.setText(item.getDateTime());
            showDate.setVisibility(View.VISIBLE);
            CommonViewHolder.get(convertView, R.id.gallery_ball).setVisibility(View.VISIBLE);
        } else {
            showDate.setVisibility(View.INVISIBLE);
            CommonViewHolder.get(convertView, R.id.gallery_ball).setVisibility(View.INVISIBLE);
        }


//        if (firstVisibleItem == position || position == 0) {
//            CommonViewHolder.get(convertView, R.id.gallery_ball).setVisibility(View.INVISIBLE);
//            CommonViewHolder.get(convertView, R.id.showDate).setVisibility(View.INVISIBLE);
//        }

        int itemCount = item.getFileList().size();
//        for (int i = 0; i < videoContent.getChildCount(); i++) {
//            videoContent.getChildAt(i).setVisibility(View.INVISIBLE);
        iv_img1.setVisibility(View.GONE);
        iv_img2.setVisibility(View.GONE);
        iv_img3.setVisibility(View.GONE);
        iv_img4.setVisibility(View.GONE);

        iv_video_hint1.setVisibility(View.GONE);
        iv_video_hint2.setVisibility(View.GONE);
        iv_video_hint3.setVisibility(View.GONE);
        iv_video_hint4.setVisibility(View.GONE);
        //        }

        for (int i = 0; i < itemCount; i++) {
            final String file = item.getFileList().get(i);
            String url = Constant.SDCARD_VIDEO_PATH + file + ".3gp";
            File mFile = new File(url);
//            ImageView iv = (ImageView) videoContent.getChildAt(i);
            ImageView iv = null;
            if (i == 0) {
                iv = iv_img1;
                if(mFile.exists()){
                    iv_video_hint1.setVisibility(View.VISIBLE);
                }else{
                    iv_video_hint1.setVisibility(View.GONE);
                }
            } else if (i == 1) {
                iv = iv_img2;
                if(mFile.exists()){
                    iv_video_hint2.setVisibility(View.VISIBLE);
                }else{
                    iv_video_hint2.setVisibility(View.GONE);
                }
            } else if (i == 2) {
                iv = iv_img3;
                if(mFile.exists()){
                    iv_video_hint3.setVisibility(View.VISIBLE);
                }else{
                    iv_video_hint3.setVisibility(View.GONE);
                }
            } else if (i == 3) {
                iv = iv_img4;
                if(mFile.exists()){
                    iv_video_hint4.setVisibility(View.VISIBLE);
                }else{
                    iv_video_hint4.setVisibility(View.GONE);
                }
            } else {
                iv = iv_img1;
                if(mFile.exists()){
                    iv_video_hint1.setVisibility(View.VISIBLE);
                }else{
                    iv_video_hint1.setVisibility(View.GONE);
                }
            }
            iv.setVisibility(View.VISIBLE);
            Glide.with(context).load(new File((Constant.SDCARD_VIDEO_PATH + file) + ".jpg")).into(iv);
//            iv.setImageBitmap(BitmapFactory.decodeFile((Constant.SDCARD_VIDEO_PATH + file) + ".jpg"));
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = Constant.SDCARD_VIDEO_PATH + file + ".3gp";
                    File fileVideo = new File(url);
                    if (fileVideo.exists()) {
                        Bundle bundle = new Bundle();
                        bundle.putString("url", url);
                        bundle.putBoolean("loop", false);
                        bundle.putString("title", file);
                        bundle.putInt(VideoPlay.VIDEO_TYPE, VideoPlay.RECODE_VIDEO);
                        Intent intent = new Intent(context, VideoPlay.class);
                        intent.putExtra("VIDEODATA", bundle);
                        context.startActivity(intent);

//                                Intent intent = new Intent(context, VideoPlayActivity.class);
//                                intent.putExtra("url", url);
//                                context.startActivity(intent);
                    } else {
                        Intent intent = new Intent(context, PhotoActivity.class);
                        intent.putExtra("url", (Constant.SDCARD_VIDEO_PATH + file) + ".jpg");
                        context.startActivity(intent);
                    }
                }
            });
            iv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (delClickInterface != null) {
                        delClickInterface.onDelClicked(Constant.SDCARD_VIDEO_PATH + file);
                    }
//                            dialogShow(Constant.SDCARD_VIDEO_PATH + file);
                    return true;
                }
            });
        }


//        int row, rowItemCount = COULNM_COUNT;
//
//        if (item.neetFlash) {
//            videoContent.setOrientation(LinearLayout.VERTICAL);
//
//            if (itemCount > COULNM_COUNT) {
//                row = itemCount / COULNM_COUNT + 1;
//            } else {
//                row = 1;
//            }
//
//            for (int i = 0; i < row; i++) {
//                LinearLayout hL = new LinearLayout(context);
//                hL.setOrientation(LinearLayout.HORIZONTAL);
//
//                // 说明是最后一行
//                if (i == row - 1) {
//                    rowItemCount = itemCount - i * COULNM_COUNT;
////                    Csjlogger.debug("到达最后一行  rowItemCount " + rowItemCount);
//                }
//
//                for (int j = 0; j < rowItemCount; j++) {
//                    final String file = item.fileList.get(i * COULNM_COUNT + j);
//                    ImageView iv = new ImageView(context);
//                    iv.setImageBitmap(BitmapFactory.decodeFile((Constant.SDCARD_VIDEO_PATH + file) + ".jpg"));
//                    iv.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            String url = Constant.SDCARD_VIDEO_PATH + file + ".3gp";
//                            File fileVideo = new File(url);
//                            if (fileVideo.exists()) {
//                                Bundle bundle = new Bundle();
//                                bundle.putString("url", url);
//                                bundle.putBoolean("loop",false);
//                                bundle.putString("title", file);
//                                Intent intent = new Intent(context, VideoPlay.class);
//                                intent.putExtra("VIDEODATA", bundle);
//                                context.startActivity(intent);
//
////                                Intent intent = new Intent(context, VideoPlayActivity.class);
////                                intent.putExtra("url", url);
////                                context.startActivity(intent);
//                            } else {
//                                Intent intent = new Intent(context, PhotoActivity.class);
//                                intent.putExtra("url", (Constant.SDCARD_VIDEO_PATH + file) + ".jpg");
//                                context.startActivity(intent);
//                            }
//                        }
//                    });
//                    iv.setOnLongClickListener(new View.OnLongClickListener() {
//                        @Override
//                        public boolean onLongClick(View v) {
//                            if (delClickInterface != null) {
//                                delClickInterface.onDelClicked(Constant.SDCARD_VIDEO_PATH + file);
//                            }
////                            dialogShow(Constant.SDCARD_VIDEO_PATH + file);
//                            return true;
//                        }
//                    });
//                    hL.addView(iv, layoutParams);
//                }
//
//                videoContent.addView(hL);
//            }
//
//            item.neetFlash = false;
////        }
//
//        showDate.setText(item.dateString);
//        getItem(position).isBallVisiable = true;

        return convertView;
    }


    public void setFirstVisibleItem(int firstVisibleItem) {
        this.firstVisibleItem = firstVisibleItem;
    }

}
