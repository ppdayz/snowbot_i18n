package com.csjbot.snowbot.activity;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.core.entry.Static;
import com.android.core.util.MD5Util;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.utils.BackUpMapTool;
import com.csjbot.snowbot.utils.OkHttp.LoadingDialog;
import com.csjbot.snowbot.utils.TimeUtil;
import com.csjbot.snowbot.views.BitMapAdapter;
import com.csjbot.snowbot.views.CusViewHodler;
import com.csjbot.snowbot.views.MyDecoration;
import com.csjbot.snowbot_rogue.bean.MapDataBean;
import com.csjbot.snowbot_rogue.servers.slams.SnowBotMoveServer;
import com.csjbot.snowbot_rogue.utils.CSJToast;
import com.csjbot.snowbot_rogue.utils.WeakReferenceHandler;

import net.steamcrafted.loadtoast.LoadToast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * @author: jl
 * @Time: 2017/1/13
 * @Desc:地图恢复页面
 */
public class RecoveryMapAct extends CsjUIActivity {
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.no_content)
    TextView noContent;

    private List<MapDataBean> mapDataBeanList = new ArrayList<>();
    private BitMapAdapter<MapDataBean> adapter;
    private RecoveryMapHandler mHandler = new RecoveryMapHandler(this);
    private LoadToast mLoadToast = null;
    private boolean canBack = true;


    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        mLoadToast = new LoadToast(this);
        mLoadToast.setText("地图恢复中");
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
        initAdapter();
        initRecylerView();
    }

    /**
     * 初始化地图列表数据
     */
    private void initData() {
        setupBack();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = mHandler.obtainMessage();
                message.what = 0;
                message.obj = BackUpMapTool.getMapDataBean();
                mHandler.sendMessage(message);
            }
        }).start();
        LoadingDialog.getInstance().showLoad(this, "努力加载本地数据中...");
    }

    private void initRecylerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new MyDecoration(this, MyDecoration.VERTICAL_LIST));
        recyclerView.setAdapter(adapter);

        // TODO 删除地图
//        adapter.setItemLongClickListener((postion, view) -> {
//            AlertDialog.Builder builder = new AlertDialog.Builder(RecoveryMapAct.this);
//            builder.setCancelable(false);
//            builder.setTitle("删除地图");
//            builder.setMessage("确定要删除地图吗？");
//            builder.setNegativeButton(RecoveryMapAct.this.getResources().getString(R.string.cancle),
//                    (dialog, which) -> dialog.dismiss());
//
//            builder.setPositiveButton(RecoveryMapAct.this.getResources().getString(R.string.delete),
//                    (dialog, which) -> adapter.removeItem(postion));
//
//            builder.create().show();
//        });
    }

    private void initAdapter() {
        adapter = new BitMapAdapter<MapDataBean>(mapDataBeanList, this) {
            ImageView iv;
            TextView tv;

            @Override
            public int getLayoutId() {
                return R.layout.layout_recovery_map_item;
            }


            @Override
            public void setViewModel(CusViewHodler viewHodler, MapDataBean data, int postion) {
                iv = (ImageView) viewHodler.getView().findViewById(R.id.map_iv);
                tv = (TextView) viewHodler.getView().findViewById(R.id.time_tv);
                tv.setText(data.getCreateTime());
                if (null != adapter.getBitmapDrawableFromMemoryCache(MD5Util.MD5(data.getData()))) {
                    iv.setImageBitmap(adapter.getBitmapDrawableFromMemoryCache(MD5Util.MD5(data.getData())));
                } else {
                    AsyncBitmapTask asyncBitmapTask = new AsyncBitmapTask(iv);
                    asyncBitmapTask.execute(data);
                }


            }
        };
        adapter.setItemClickListener((postion, view) -> {
            if (canBack) {
                canBack = false;
                BackUpMapTool.getInstance().recoveryMapData(RecoveryMapAct.this, mapDataBeanList.get(postion), new BackUpMapTool.RecoveryMapDataInterface() {
                    @Override
                    public void recoveryMapDataSucceed() {
                        TimeUtil timeUtil = TimeUtil.getInterface();
                        mLoadToast.show();
                        timeUtil.getTime(5, i -> {
                            mLoadToast.success();
                            canBack = true;
                            SnowBotMoveServer.getInstance().setIsInRecoveryMapData(false);
                            CSJToast.showToast(context, Static.CONTEXT.getResources().getString(R.string.restore_map_succeed));
                            finish();
                        });
                    }

                    @Override
                    public void recoveryMapDataFailed() {
                        canBack = true;
                    }
                });
            }
        });
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.recovery_map_layout;
    }

    private static class RecoveryMapHandler extends WeakReferenceHandler<RecoveryMapAct> {

        RecoveryMapHandler(RecoveryMapAct reference) {
            super(reference);
        }

        @Override
        protected void handleMessage(RecoveryMapAct reference, Message msg) {
            switch (msg.what) {
                case 0:
                    reference.mapDataBeanList = (List<MapDataBean>) msg.obj;
                    if (null != reference.mapDataBeanList && reference.mapDataBeanList.size() > 0) {
                        reference.noContent.setVisibility(View.GONE);
                        reference.recyclerView.setVisibility(View.VISIBLE);
                        reference.adapter.setData(reference.mapDataBeanList);
                    }
                    LoadingDialog.getInstance().dismissLoad();
                    if (null != reference.mapDataBeanList && reference.mapDataBeanList.size() > 0) {
                        reference.recyclerView.setVisibility(View.VISIBLE);
                        reference.noContent.setVisibility(View.GONE);
                    } else {
                        reference.recyclerView.setVisibility(View.GONE);
                        reference.noContent.setVisibility(View.VISIBLE);
                    }
                    break;
                default:
                    break;
            }
        }

    }

    @Override
    public void onBackPressed() {
        if (canBack) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLoadToast.success();
        LoadingDialog.getInstance().dismissLoad();
    }
}
