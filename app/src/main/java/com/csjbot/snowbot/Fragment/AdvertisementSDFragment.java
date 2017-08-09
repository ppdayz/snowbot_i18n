package com.csjbot.snowbot.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.settings.VideoPlay;
import com.csjbot.snowbot.adapter.SDAdverAdapter;
import com.csjbot.snowbot.bean.SDAdverBean;
import com.csjbot.snowbot_rogue.utils.CSJToast;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2017, SuZhou CsjBot. All Rights Reserved.
 * www.csjbot.com
 * Created by Luo on 2017/6/22.
 */

public class AdvertisementSDFragment extends Fragment implements SDAdverAdapter.ItemLongClickListener {

    private List<SDAdverBean> dataList = new ArrayList<>();
    private Map<String, Long> memoryMap = new HashMap<>();
    private Map<String, Double> ratioMap = new HashMap<>();

    private RecyclerView rv_ad_list;
    private SDAdverAdapter adverAdapter;

    private String[] suffixs = {
            "wav", "mp4", "3gp", "mp3", "avi",
//            "ppt", "pptx"
    };
    private String[] colors = {
            "#ea0000", "#9f35ff", "#2894ff", "#007979", "#00db00",
//            "#5b5b00", "#ff5809"
    };
    private TextView tv_hint;
    private LinearLayout ll_memory_container;
    private long totalSpace;
    private long usableSpace;
    private int usableWidth;
    private LinearLayout container;
    private boolean isFirst = true;

    // sdcard/csjbot/ad
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_sd_adver, null);

        rv_ad_list = (RecyclerView) view.findViewById(R.id.rv_ad_list);
        tv_hint = (TextView) view.findViewById(R.id.tv_hint);
        ll_memory_container = (LinearLayout) view.findViewById(R.id.rl_memory_container);

        adverAdapter = new SDAdverAdapter();
        adverAdapter.setItemLongClickListener(this);
        rv_ad_list.setLayoutManager(new GridLayoutManager(getActivity(), 6));
        rv_ad_list.setAdapter(adverAdapter);

        scanSDCardForAdver();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initSizeCountView(ll_memory_container);
            }
        }, 100);
        return view;
    }


    /**
     * 遍历目录，找到所有符合要求的文件
     */
    private void scanSDCardForAdver() {
        getSDTotalSize();
        File file = new File("/sdcard/csjbot/ad/");
        if (file.exists()) {
            if (file.isDirectory()) {
                dataList.clear();
                File[] fileArray = file.listFiles();
                for (File f : fileArray) {
                    String fileName = f.getName();
                    // 如果是以下几种格式之一
                    for (String s : suffixs) {
                        if (fileName.contains(s)) {
                            SDAdverBean sb = new SDAdverBean();
                            sb.setName(fileName);
                            sb.setPath(f.getAbsolutePath());
                            sb.setSize(f.length());
                            dataList.add(sb);
                            addMemoryCount(s, f.length());
                        }
                    }
                }
                if (dataList.size() == 0) {
                    noVideo();
                } else {
                    showVideo();
                    adverAdapter.setData(dataList);
                    adverAdapter.notifyDataSetChanged();
                    // 这里需要更新进度条
                    if(!isFirst) {
                        reloadView();
                    }
                }
            }
        } else {
            file.mkdir();
            noVideo();
        }

    }

    /**
     * 没有找到视频文件时的处理
     */
    private void noVideo() {
        if (rv_ad_list != null) {
            rv_ad_list.setVisibility(View.GONE);
            tv_hint.setVisibility(View.VISIBLE);
        }
    }

    private void showVideo() {
        if (rv_ad_list != null) {
            rv_ad_list.setVisibility(View.VISIBLE);
            tv_hint.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLongClick(View v, final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final AlertDialog alertDialog = builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SDAdverBean sdAdverBean = dataList.get(position);
                File f = new File(sdAdverBean.getPath());
                if (f.exists()) {
                    f.delete();
                    dataList.remove(position);
                    adverAdapter.notifyDataSetChanged();
                    if (dataList.size() == 0) {
                        noVideo();
                    }
                }
            }
        }).setMessage("确认删除改文件？")
                .create();

        alertDialog.show();
        Log.e("AdvertisementSDFragment", "longclick");
    }

    @Override
    public void onClick(View v, int position) {
        goPlay(new File(dataList.get(position).getPath()));
    }

    int count = 0;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            count++;
            if (count != 1) {
                scanSDCardForAdver();
            }
        }
    }

    private void getSDTotalSize() {
        File sdCard = Environment.getExternalStorageDirectory();
        // 获取文件目录对象剩余空间
        // 单位 byte
        //107375226880
        usableSpace = sdCard.getUsableSpace();
        totalSpace = sdCard.getTotalSpace();
//        memoryMap.put(TOTAL_MEMORY, totalSpace);
//        memoryMap.put(USABLE_MEMORY, usableSpace);
    }

    private void addMemoryCount(String formart, long size) {
        Long count = memoryMap.get(formart);
        long total = 0;
        if (count != null) {
            total = count;
        }
        total += size;
        memoryMap.put(formart, total);
    }

    /**
     * size初始单位为byte 即 B
     * 107375226880 B
     * / 1024 -> 104858620 KB
     * / 1024 -> 102400 MB
     * / 1024 -> 100 GB
     */
    private String long2String(long size) {
        StringBuilder sb = new StringBuilder();
        double s = (double) size;
        int count = 0;
        while (s > 1024 && count < 3) {
            s /= 1024.0;
//            Log.i("calc", "size -> " + s);
            count++;
        }
        DecimalFormat df = new DecimalFormat("#.00");
        String str = df.format(s);
        switch (count) {
            case 0:// byte
                sb.append(str).append(" B");
                break;
            case 1:// KB
                sb.append(str).append(" KB");
                break;
            case 2:// MB
                sb.append(str).append(" MB");
                break;
            case 3:// GB
                sb.append(str).append(" GB");
                break;
            default:
                sb.append("0 B");
                break;
        }

        return sb.toString();
    }

    /**
     * 初始化占比
     */
    private void initSizeCountView(ViewGroup parent) {
        isFirst = false;
        TextView tv_space = (TextView) parent.findViewById(R.id.tv_space);
        if (container == null) {
            container = (LinearLayout) parent.findViewById(R.id.ll_container);
        }

        int measuredWidth = container.getMeasuredWidth();
        usableWidth = (int) (usableSpace / (float) totalSpace * measuredWidth);

        Log.e("Ads", "container = " + container.getMeasuredWidth());
        Log.e("Ads", "usableWidth = " + usableWidth);

        tv_space.setText(getString(R.string.total_sd) + long2String(totalSpace));
        draw();
    }

    private void draw() {
        DecimalFormat df = new DecimalFormat("#.00");
        int freeLength = container.getMeasuredWidth();
        Map<Integer, Integer> lengthMap = new HashMap<>();

        for (int i = 0; i < suffixs.length; i++) {
            Long size = memoryMap.get(suffixs[i]);
            if (size == null) {
                continue;
            }
//            double ratio = (double) size / totalSpace;
//            String ratioF = df.format(ratio);
            int width = (int) ((float) size / totalSpace * freeLength);
            lengthMap.put(i, width);
            if (width < 30) {
                width = 30;
                freeLength -= width;
                if (freeLength < 30) {
                    for (Map.Entry<Integer, Integer> entry : lengthMap.entrySet()) {
                        int length = entry.getValue();
                        if (length > 200) {
                            length -= 20;
                            freeLength += 20;
                        }
                        lengthMap.put(entry.getKey(), length);
                    }
                    lengthMap.put(i, width);
                } else {
                    lengthMap.put(i, width);
                }
            } else {
                if (width > freeLength) {
                    double w = 0.85 * freeLength;
                    lengthMap.put(i, (int) w);
                } else {
                    lengthMap.put(i, width);
                }
            }
        }
        for (Map.Entry<Integer, Integer> entry : lengthMap.entrySet()) {
            Log.e("test", entry.getValue() + " : " + entry.getKey());
            addViewToContainer(entry.getValue(), entry.getKey());
        }

    }

    /**
     * 首先将长度小于最小长度的视图添加到View中。
     */
//    private void setLength(ViewGroup parent) {
//        DecimalFormat df = new DecimalFormat("#.00");
//        int freeLength = container.getMeasuredWidth();
//        for (int i = 0; i < suffixs.length; i++) {
//            Long size = memoryMap.get(suffixs[i]);
//            if (size == null) {
//                continue;
//            }
//            double ratio = (double) size / totalSpace;
//            String ratioF = df.format(ratio);
//            int width = (int) (size / totalSpace * freeLength);
//            if (width < 50) {
//                width = 50;
//                freeLength -= width;
//                addViewToContainer(width, i);
//                continue;
//            }
//
//            ratioMap.put(suffixs[i], Double.parseDouble(ratioF));
//        }
//        calcWidth(ratioMap, freeLength);
//        addViewToContainer((int) (usableSpace / (float) totalSpace * freeLength), -1);
//    }

//    private void calcWidth(Map<String, Double> ratioMap, int freeLength) {
//        int count = 0;
//        for (int i = 0; i < suffixs.length; i++) {
//            if (ratioMap.get(suffixs[i]) == null) {
//                continue;
//            }
//            int width = (int) (ratioMap.get(suffixs[i]) * freeLength);
//            if (width < 50) {
//                width = 50;
//                freeLength -= width;
//                addViewToContainer(width, i);
//                ratioMap.remove(suffixs[i]);
//                calcWidth(ratioMap, freeLength);
//            } else {
//                count++;
//                if (count == ratioMap.size()) {
//                    freeLength -= (int) (ratioMap.get(suffixs[i]) * freeLength);
//                    addViewToContainer((int) (ratioMap.get(suffixs[i]) * freeLength), i);
//                }
//            }
//        }
//
//    }
    private void addViewToContainer(int width, int index) {
        TextView textView = new TextView(getActivity());
        textView.setTextColor(Color.parseColor("#ffffff"));
        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(width, container.getMeasuredHeight());
        if (index != -1) {
            if (index == 0) {
                params.setMargins(10, 0, 5, 5);
            } else {
                textView.setBackgroundColor(Color.parseColor(colors[index]));
            }
        } else {
            textView.setBackgroundColor(Color.parseColor("#006030"));
        }
        textView.setLayoutParams(params);
        container.addView(textView);

    }

    private void reloadView(){
        if(container != null)
            container.removeAllViews();
        initSizeCountView(ll_memory_container);
    }


    /**
     * 获取文件大小
     *
     * @param file
     * @return
     */
    private int getFileSize(File file) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            return fileInputStream.available();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 跳转播放页面
     *
     * @param file
     */
    private void goPlay(File file) {
        if (getFileSize(file) <= 0) {
            CSJToast.showToast(getActivity(), "文件损坏,无法播放!");
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString("url", file.getAbsolutePath());
        bundle.putBoolean("loop", true);
        bundle.putString("title", file.getName());
        Intent intent = new Intent(getActivity(), VideoPlay.class);
        intent.putExtra("VIDEODATA", bundle);
        startActivity(intent);
    }

}