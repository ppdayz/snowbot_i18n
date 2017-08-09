package com.csjbot.snowbot.Fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.settings.VideoPlay;
import com.csjbot.snowbot.views.dialog.MyWaitingDialog;
import com.csjbot.snowbot_rogue.utils.CSJToast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * @author jwc
 * U盘文件的显示
 */
public class AdvertisementUSBFragment extends Fragment {

    // 搜索成功
    public static final int SEARCH_SUC = 0;
    // 复制成功
    public static final int COPY_SUC = 10;

    // 要扫描的U盘路径
    public static final String SCANNER_PATH = "/mnt/usb_storage";
    // 要复制到SD卡的路径
    public static final String COPY_TO_PATH = "/mnt/sdcard/csjbot/ad/";

    protected ArrayList<File> fileList = new ArrayList<>();

    protected USBAdapter mUSBAdapter = null;

    protected Dialog mDialog = null;

    protected MyWaitingDialog mWaitingDialog = null;

    protected RecyclerView mRecyclerView = null;

    protected TextView tv_msg = null;

    protected MyHandler mHandler = new MyHandler(this);

    protected File selectedFile = null;

    protected String[] suffixs = {
            "wav", "mp4", "3gp", "mp3", "avi"
    };

    /**
     * 静态内部类Handler(避免内存泄露)
     */
    static class MyHandler extends Handler{

        WeakReference<AdvertisementUSBFragment> mContextWeakReference = null;

        public MyHandler(AdvertisementUSBFragment usbFragment){
            super();
            mContextWeakReference = new WeakReference<>(usbFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            AdvertisementUSBFragment usbFragment = mContextWeakReference.get();
            if(usbFragment != null){
                switch (msg.what){
                    case SEARCH_SUC:// 搜索到视频文件消息
                        // 发送给recyclerview进行局部刷新
                        usbFragment.addFile((File) msg.obj);

                        break;
                    case COPY_SUC:// 复制成功消息
                        usbFragment.copySuc();
                        break;
                }
            }
        }
    }

    /**
     * 添加一个文件
     * @param file
     */
    public void addFile(File file){
        fileList.add(file);
        mUSBAdapter.notifyItemChanged((fileList.size()-1));
        if(tv_msg.isShown() || tv_msg.getVisibility() == View.VISIBLE){
            tv_msg.setVisibility(View.GONE);
        }
    }

    /**
     * 复制成功
     */
    public void copySuc(){
        dismissWaitDialog();
        CSJToast.showToast(getActivity(),getString(R.string.advertisement_copy_suc));
    }


    /**
     * 构建一个实例对象
     * @return
     */
    public static AdvertisementUSBFragment newInstance() {
        AdvertisementUSBFragment fragment = new AdvertisementUSBFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_advertisement_usb, container, false);
        initialize(view);

        return view;
    }

    /**
     * 初始化
     */
    private void initialize(View view){
        tv_msg = (TextView) view.findViewById(R.id.tv_msg);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 6);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setAdapter(mUSBAdapter = new USBAdapter());
        mUSBAdapter.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public void onItemLongClick(File file) {
                selectedFile = file;
                showDialog();
            }
        });
        mUSBAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(File file) {
                goPlay(file);
            }
        });
        // 开始搜索文件
        new SearThread(this).start();
    }

    /**
     * 获取文件大小
     * @param file
     * @return
     */
    private int getFileSize(File file){
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
     * @param file
     */
    private void goPlay(File file){
        if(getFileSize(file) <= 0){
            CSJToast.showToast(getActivity(),"文件损坏,无法播放!");
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

    /**
     * 显示加载对话框
     */
    public void showWaitDialog(){
        if(mWaitingDialog == null) {
            mWaitingDialog = new MyWaitingDialog(getActivity());
        }
        mWaitingDialog.showDialog(getString(R.string.prompt),getString(R.string.advertisement_is_copy));
    }

    /**
     * 关闭加载对话框
     */
    public void dismissWaitDialog(){
        if(mWaitingDialog != null && mWaitingDialog.isShowing()) {
            mWaitingDialog.dismiss();
            mWaitingDialog = null;
        }
    }

    /**
     * 调用搜索视频文件方法
     */
    static class SearThread extends Thread {

        WeakReference<AdvertisementUSBFragment> mWeakReference;

        public SearThread(AdvertisementUSBFragment usbFragment) {
            super();
            mWeakReference = new WeakReference<>(usbFragment);
        }

        @Override
        public void run() {
            super.run();
            if(mWeakReference.get() != null) {
                mWeakReference.get().getAllFiles(new File(AdvertisementUSBFragment.SCANNER_PATH));
            }
        }
    }

    /**
     * 调用拷贝视频文件方法
     */
    static class CopyThread extends Thread {

        WeakReference<AdvertisementUSBFragment> mWeakReference;

        public CopyThread(AdvertisementUSBFragment usbFragment) {
            super();
            mWeakReference = new WeakReference<>(usbFragment);
        }

        @Override
        public void run() {
            super.run();
            AdvertisementUSBFragment usbFragment = mWeakReference.get();
            if(mWeakReference.get()  != null){
                usbFragment.copyToSD(usbFragment.selectedFile);
            }
        }
    }


    /**
     * item点击事件回调
     */
    interface OnItemClickListener{
        void onItemClick(File file);
    }

    /**
     * item长按事件回调
     */
    interface OnItemLongClickListener{
        void onItemLongClick(File file);
    }


    class USBAdapter extends RecyclerView.Adapter<USBAdapter.MyViewHolder> {

        OnItemClickListener mOnItemClickListener;
        OnItemLongClickListener mOnItemLongClickListener;

        public void setOnItemClickListener(OnItemClickListener l){
            this.mOnItemClickListener = l;
        }
        public void setOnItemLongClickListener(OnItemLongClickListener l){
            this.mOnItemLongClickListener = l;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.fragment_advertisement_usb_item, null));
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            holder.tv_name.setText(fileList.get(position).getName());
            // 设置子项点击的时候触发自己写的监听
            if(mOnItemClickListener != null) {
                holder.usb_item_ll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnItemClickListener.onItemClick(fileList.get(position));
                    }
                });
            }
            // 设置子项长按的时候触发自己写的监听
            if(mOnItemLongClickListener != null){
                holder.usb_item_ll.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mOnItemLongClickListener.onItemLongClick(fileList.get(position));
                        return false;
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return fileList.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            LinearLayout usb_item_ll;

            TextView tv_name;

            public MyViewHolder(View view) {
                super(view);
                tv_name = (TextView) view.findViewById(R.id.tv_name);
                usb_item_ll = (LinearLayout) view.findViewById(R.id.usb_item_ll);
            }
        }

    }

    /**
     * 如果U盘存在并可用,遍历U盘里的目录获取所有的视频
     *
     * @param root 遍历的根目录
     */
    public void getAllFiles(File root) {
        File[] files = root.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.contains(suffixs[0])
                        || fileName.contains(suffixs[1])
                        || fileName.contains(suffixs[2])
                        || fileName.contains(suffixs[3])
                        || fileName.contains(suffixs[4])
                        ) {
                    // 查询到视频文件发送消息
                    mHandler.obtainMessage(SEARCH_SUC, file).sendToTarget();
                } else {
                    if (file.isDirectory()) {
                        getAllFiles(file);
                    }
                }
            }
        }
    }

    /**
     * 复制文件到指定目录
     * @param file 被复制文件
     */
    private void copyToSD(File file){
        File toFile = new File(COPY_TO_PATH+"/"+file.getName());
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            inputStream = new FileInputStream(file);
            fileOutputStream = new FileOutputStream(toFile);
            byte[] bt = new byte[1024];
            int len = -1;
            while((len = inputStream.read(bt))!=-1){
                fileOutputStream.write(bt,0,len);
            }
            fileOutputStream.flush();
            // 发送拷贝成功消息
            mHandler.obtainMessage(COPY_SUC).sendToTarget();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if(inputStream != null){
                    inputStream.close();
                }
                if(fileOutputStream != null){
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 显示dialog
     */
    private void showDialog() {

        if(mDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.advertisement_copy_sd_msg);

            builder.setTitle(R.string.prompt);

            builder.setPositiveButton(R.string.advertisement_ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mDialog.dismiss();
                    showWaitDialog();
                    // 开始复制
                    new CopyThread(AdvertisementUSBFragment.this).start();
                }
            });

            builder.setNegativeButton(R.string.advertisement_cancel, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface diag, int which) {
                    mDialog.dismiss();
                }
            });

            mDialog = builder.create();
        }
        mDialog.show();
    }

}
