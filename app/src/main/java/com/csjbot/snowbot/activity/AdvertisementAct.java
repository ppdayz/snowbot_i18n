package com.csjbot.snowbot.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.android.core.util.SharedUtil;
import com.android.core.util.StrUtil;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.settings.VideoPlay;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot.views.BaseRecyViewAdpter;
import com.csjbot.snowbot.views.CusViewHodler;
import com.csjbot.snowbot.views.MyDecoration;

import net.steamcrafted.loadtoast.LoadToast;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

/**
 * @author: jl
 * @Time: 2017/1/5
 * @Desc:广告页面
 */


public class AdvertisementAct extends CsjUIActivity {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private BaseRecyViewAdpter<String> adapter;
    private List<String> fileName = new ArrayList<>();
    private List<String> filePath = new ArrayList<>();
    private LoadToast mloadTost;

    private EventBus ibus = null;

    private String usbPath;
    private String[] suffixs = {
            "wav", "mp4", "3gp", "mp3", "avi", "ppt", "pptx"
    };


    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        initData();
        initAdapter();
        initRecyclerView();
    }

    /**
     * 初始化广告的数据
     */
    private void initData() {
        setupBack();
        usbPath = SharedUtil.getPreferStr(SharedKey.USBPATH);
//        if (StrUtil.isBlank(usbPath) && checkSD()) {
//            CsjSpeechSynthesizer.getSynthesizer().startSpeaking("请重新插拔优盘", null);
//        }
        if (StrUtil.isNotBlank(usbPath)) {
            mloadTost = new LoadToast(AdvertisementAct.this);
            mloadTost.setText("搜索中，请稍后");
            mloadTost.show();
            GetFileTask getFileTask = new GetFileTask();
            getFileTask.execute(usbPath);
        }
    }

    private boolean checkSD() {
        return Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new MyDecoration(this, MyDecoration.VERTICAL_LIST));
        recyclerView.setAdapter(adapter);
    }

    private void initAdapter() {
        adapter = new BaseRecyViewAdpter<String>(fileName, this) {
            TextView fileNameTv;

            @Override
            public int getLayoutId() {
                return R.layout.layout_wifi;
            }

            @Override
            public void setViewModel(CusViewHodler viewHodler, String data, int postion) {
                fileNameTv = (TextView) viewHodler.getView().findViewById(R.id.wif_name_tv);
                fileNameTv.setText(data);
            }
        };
        adapter.setItemClickListener(new BaseRecyViewAdpter.ItemClickListener() {
            @Override
            public void itemClick(int postion, View view) {
                Bundle bundle = new Bundle();
                bundle.putString("url", filePath.get(postion));
                bundle.putBoolean("loop", true);
                bundle.putString("title", fileName.get(postion));
                bundle.putInt(VideoPlay.VIDEO_TYPE, VideoPlay.USER_VIDEO);
                Intent intent = new Intent(AdvertisementAct.this, VideoPlay.class);
                intent.putExtra("VIDEODATA", bundle);
                startActivity(intent);
            }
        });
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.act_advertisement;
    }


    public HashMap<String, String> getMeidaInPath(File file) {

        //从根目录开始扫描
        HashMap<String, String> fileList = new HashMap<>();
        getFileList(file, fileList);
        return fileList;
    }


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

    private boolean checkSuffix(String fileName) {
        String[] token = fileName.split("\\.");
        if (token.length > 1) {
            String pf = token[token.length - 1];
            if (isInList(pf)) {
                return true;
            }
        }

        return false;
    }

    private boolean isInList(String pf) {
        for (String str : suffixs) {
            if (str.equalsIgnoreCase(pf)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 获取文件的asyncTask
     */
    private class GetFileTask extends AsyncTask<String, Object, Map> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Map doInBackground(String... params) {
            if (StrUtil.isNotBlank(params[0])) {
                File file = new File(params[0]);
                return getMeidaInPath(file);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Map map) {
            super.onPostExecute(map);
            mloadTost.success();
            if (null != map && map.size() > 0) {
                fileName = new ArrayList<>(map.keySet());
                filePath = new ArrayList<>(map.values());
                adapter.setData(fileName);
            }
        }
    }


}
