package com.csjbot.snowbot.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.core.entry.Static;
import com.android.core.util.SharedUtil;
import com.android.core.util.StrUtil;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.bean.CommandBean;
import com.csjbot.snowbot.bean.CommandDataBean;
import com.csjbot.snowbot.bean.DataContent;
import com.csjbot.snowbot.bean.FromBean;
import com.csjbot.snowbot.bean.Home;
import com.csjbot.snowbot.bean.ToBean;
import com.csjbot.snowbot.client.nettyHandler.ClientListener;
import com.csjbot.snowbot.utils.BackUpMapTool;
import com.csjbot.snowbot.utils.CommonTool;
import com.csjbot.snowbot.utils.DialogUtil;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot.utils.TimeUtil;
import com.csjbot.snowbot.views.BaseRecyViewAdpter;
import com.csjbot.snowbot.views.CusViewHodler;
import com.csjbot.snowbot.views.MyDecoration;
import com.csjbot.snowbot_rogue.app.CsjSpeechSynthesizer;
import com.csjbot.snowbot_rogue.bean.MapDataBean;
import com.csjbot.snowbot_rogue.platform.SnowBotManager;
import com.csjbot.snowbot_rogue.servers.slams.MoveServerMapListener;
import com.csjbot.snowbot_rogue.servers.slams.SnowBotMoveServer;
import com.csjbot.snowbot_rogue.utils.CSJToast;
import com.csjbot.snowbot_rogue.utils.WeakReferenceHandler;
import com.slamtec.slamware.action.MoveDirection;
import com.slamtec.slamware.geometry.Line;
import com.slamtec.slamware.robot.Location;
import com.slamtec.slamware.robot.Map;
import com.slamtec.slamware.robot.Pose;

import net.steamcrafted.loadtoast.LoadToast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MapActivity extends CsjUIActivity implements View.OnTouchListener, ClientListener, MoveServerMapListener {
    @BindView(R.id.totleBack)
    ImageView totleBack;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.mapImageView)
    ImageView mapImageView;
    @BindView(R.id.linear_layout)
    LinearLayout linearLayout;
    @BindView(R.id.add_home_layout)
    LinearLayout addHomeLayout;
    @BindView(R.id.add_wall_iv)
    ImageView addWallIv;
    @BindView(R.id.add_wall_tv)
    TextView addWallTv;
    @BindView(R.id.add_home_iv)
    ImageView addHomeIv;
    @BindView(R.id.add_home_tv)
    TextView addHomeTv;
    @BindView(R.id.del_wall_layout)
    LinearLayout delWallLayout;
    @BindView(R.id.modify_home_layout)
    LinearLayout modifyHomeLayout;
    @BindView(R.id.add_wall_layout)
    LinearLayout addWallLayout;
    @BindView(R.id.textView2)
    TextView textView2;
    @BindView(R.id.clear_map_layout)
    LinearLayout clearMapLayout;
    @BindView(R.id.del_home_sure_layout)
    LinearLayout delHomeSureLayout;
    @BindView(R.id.cancel)
    Button cancel;
    @BindView(R.id.sure)
    Button sure;

    private static final int MAP_UPDATE = 0x00;

    private boolean isAddHome = false;
    private boolean isAddingWall;
    private final int MAXHOME = 10;
    private float homeX, homeY;
    private float mOffsetX, mOffsetY;
    private int imageMapW, imageMapH, screenW, screenH, rightLayoutH, rightLayoutW;

    private BaseRecyViewAdpter<Home> adapter;
    private BaseRecyViewAdpter<Home> adapterEdit;
    private Bitmap bm;
    private Bitmap homeIcon, addWallSP;
    private CommandBean sendData = new CommandBean();
    private CommandDataBean<DataContent> sendCommonData = new CommandDataBean<>();
    private FromBean fromBean = new FromBean();
    private List<Home> chooseResults = new ArrayList<>();
    private List<Home> homeDatas = new ArrayList<>();
    private List<Pose> poses = new ArrayList<>();
    private LoadToast mLoadToast = null;
    private MapActivityHandler mHandler = new MapActivityHandler(this);
    private PointF startP, endP;
    private SnowBotManager snowBot = SnowBotManager.getInstance();
    private TimeUtil timeUtil = TimeUtil.getInterface();
    private ToBean toBean = new ToBean();

    private Runnable wakeupRunnable = () -> wakeupMapUpdate(false);
    private boolean isRecoveryMapData;
    private float bw = 0;
    private boolean isGoHome = false;

//    @Override
//    public boolean useEventBus() {
//        return true;
//    }

    private void init() {
        mapImageView.setOnTouchListener(this);
        ViewTreeObserver vto2 = mapImageView.getViewTreeObserver();
        vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mapImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                imageMapW = mapImageView.getWidth();
                imageMapH = mapImageView.getHeight();
            }
        });

        ViewTreeObserver vto3 = linearLayout.getViewTreeObserver();
        vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                linearLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                rightLayoutW = linearLayout.getWidth();
                rightLayoutH = linearLayout.getHeight();

                bw = (screenW - rightLayoutW - imageMapH) / 2;

            }
        });
        WindowManager wm = this.getWindowManager();
        screenW = wm.getDefaultDisplay().getWidth();
        screenH = wm.getDefaultDisplay().getHeight();

        snowBot.setAutoUpdateMap(false);
        mLoadToast = new LoadToast(this);
    }

    private void wakeupMapUpdate(boolean wakeup) {
        if (wakeup) {
            mHandler.removeCallbacks(wakeupRunnable);
            mHandler.postDelayed(wakeupRunnable, 60000);
        }

        snowBot.setWakeupMapUpdate(wakeup);
    }

    private void initData() {
        setupBack();

        homeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.home_icon);
        addWallSP = BitmapFactory.decodeResource(getResources(), R.mipmap.add_wall_start_point);
        if (null != SharedUtil.getListObj(SharedKey.HOMEDATAS, Home.class)) {
            homeDatas = SharedUtil.getListObj(SharedKey.HOMEDATAS, Home.class);
        }

        adapter = new BaseRecyViewAdpter<Home>(homeDatas, this) {
            TextView homeNameTv;

            @Override
            public int getLayoutId() {
                return R.layout.item_home_list;
            }

            @Override
            public void setViewModel(CusViewHodler viewHodler, Home data, int postion) {
                View view = viewHodler.getView();
                homeNameTv = (TextView) view.findViewById(R.id.home_name_tv);
                homeNameTv.setText(data.getHomename());

            }
        };

        adapterEdit = new BaseRecyViewAdpter<Home>(homeDatas, this) {
            TextView homeNameTv;
            CheckBox checkBox;

            @Override
            public int getLayoutId() {
                return R.layout.ed_item_home_list;
            }

            @Override
            public void setViewModel(CusViewHodler viewHodler, Home data, int postion) {
                View view = viewHodler.getView();
                homeNameTv = (TextView) view.findViewById(R.id.home_name_tv);
                homeNameTv.setText(data.getHomename());
                checkBox = (CheckBox) view.findViewById(R.id.cb);
                checkBox.setTag(postion);
                if (chooseResults != null) {
                    checkBox.setChecked((chooseResults.contains(homeDatas.get(postion))));
                } else {
                    checkBox.setChecked(false);
                }

            }
        };

        adapterEdit.setItemClickListener((postion, view) -> {
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.cb);
            if ((int) checkBox.getTag() == postion) {
                if (checkBox.isChecked()) {
                    checkBox.setChecked(false);
                    chooseResults.remove(homeDatas.get(postion));
                } else {
                    checkBox.setChecked(true);
                    chooseResults.add(homeDatas.get(postion));
                }
            }
        });

        adapter.setItemLongClickListener((postion, view) -> modifyHomeName(postion));

        adapter.setItemClickListener((postion, view) -> {
            CSJToast.showToast(MapActivity.this, getString(R.string.start_go_home) + homeDatas.get(postion).getHomename());
            goRoom(postion);
        });

        wakeupMapUpdate(true);
    }

    private void initRecylerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new MyDecoration(this, MyDecoration.VERTICAL_LIST));
        recyclerView.setAdapter(adapter);
    }

    public boolean onTouch(View v, MotionEvent event) {
        wakeupMapUpdate(true);
        if (isRecoveryMapData) {
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float bw = (screenW - rightLayoutW - imageMapH) / 2;
            if (null == bm) {
                return false;
            }
            //超出地图区域
            if (event.getX() < bw || event.getX() > bw + imageMapH) {
                CSJToast.showToast(this, getString(R.string.choose_right_home));
                return false;
            }
            homeX = event.getX();
            homeY = event.getY();

            float offsetX = ((bm.getWidth()) * 1f / imageMapH) * (event.getX() - bw),
                    offsetY = (bm.getHeight() * 1f / imageMapH) * event.getY();

            mOffsetX = offsetX;
            mOffsetY = offsetY;

            Csjlogger.warn("offsetX = {}, offsetY = {}", offsetX, offsetY);

            if (!isAddingWall && !isAddHome) {
                snowBot.moveTo(offsetX, offsetY);
            } else if (isAddingWall && !isAddHome) {
                // 如果开始点是空，就说明没有开始，设置开始点
                if (startP == null) {
                    startP = new PointF(mOffsetX, mOffsetY);
                } else if (endP == null) {  // 如果结束点为空，开始点不为空，就设置结束点，并且添加一个虚拟墙
                    endP = new PointF(mOffsetX, mOffsetY);
                    snowBot.addWall(startP, endP);
                    startP = null;
                    endP = null;
                } else { // 否则就清空
                    startP = null;
                    endP = null;
                }
            } else if (isAddHome && !isAddingWall && checkHome(homeX, homeY)) {
                addHomeDialog();
            }
        }

        return false;
    }

    @Override
    public void recMessage(final String msg) {
        mHandler.post(() -> CSJToast.showToast(MapActivity.this, msg, 2000));

        switch (msg) {
            case "stop":
                snowBot.cancelAction();
                break;
            case "up":
                snowBot.moveBy(MoveDirection.FORWARD);
                break;
            case "down":
                snowBot.moveBy(MoveDirection.BACKWARD);
                break;
            case "left":
                snowBot.moveBy(MoveDirection.TURN_LEFT);
                break;
            case "right":
                snowBot.moveBy(MoveDirection.TURN_RIGHT);
                break;
            default:
                break;
        }
    }

    @Override
    public void clientConnected() {
    }

    @Override
    public void clientDisConnected() {

    }

    @OnClick(R.id.clear_map_layout)
    public void onClearMap(View view) {
        if (isRecoveryMapData) {
            return;
        }
        wakeupMapUpdate(false);

        snowBot.fakeWakeup();

        if (CommonTool.isFastDoubleClick()) {
            return;
        }

        if (null != snowBot) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("清除地图")
                    .setMessage("如果清除地图，地图上的所有数据将被清除！")
                    .setNegativeButton("否", (dialog1, which) -> {
                        wakeupMapUpdate(true);
                        dialog1.dismiss();
                    })
                    .setPositiveButton("清除", (dialog12, which) -> {
                        snowBot.clearMap();
                        SharedUtil.setPreferInt(SharedKey.AIUISERVICESWITCH, 0);

                        clearHomeData();
                        mLoadToast.setText("正在清除地图");
                        mLoadToast.show();
                        SnowBotMoveServer.getInstance().setIsInRecoveryMapData(true);
                        isRecoveryMapData = true;

                        timeUtil.getTime(5, i -> {
                            mLoadToast.success();
                            SnowBotMoveServer.getInstance().setIsInRecoveryMapData(false);
                            isRecoveryMapData = false;
                            SharedUtil.setPreferInt(SharedKey.AIUISERVICESWITCH, 1);
                        });
                        wakeupMapUpdate(true);
                        dialog12.dismiss();
                    }).show();

        }
    }

    @OnClick({R.id.add_wall_layout, R.id.del_wall_layout, R.id.add_home_layout})
    public void onClick(View view) {
        wakeupMapUpdate(true);
        if (isRecoveryMapData) {
            return;
        }

        if (null == mapImageView.getDrawable()) {
            CSJToast.showToast(MapActivity.this, getString(R.string.map_empty), 1000);
            return;
        }

        switch (view.getId()) {
            case R.id.add_wall_layout:
                if (isAddingWall) {
                    setAddWall();
                } else {
                    setCancleAddWall();
                }

                if (isAddHome) {
                    setAddHome();
                }
                break;
            case R.id.del_wall_layout:
                if (isAddingWall) {
                    setAddWall();
                }
                if (isAddHome) {
                    setAddHome();
                }
                if (null != snowBot) {
                    snowBot.clearWalls();
                }
                break;
            case R.id.add_home_layout:
                if (isAddingWall) {
                    setAddWall();
                }

                if (null != homeDatas && homeDatas.size() >= MAXHOME) {
                    CSJToast.showToast(this, getString(R.string.max_home));
                    return;
                }

                addHomeAtCurrentPos();

//                if (!isAddHome) {
//                    setCancleAddHome();
//                } else {
//                    setAddHome();
//                }
                break;
        }
    }

    private void addHomeAtCurrentPos() {
//        moveTo(new Location((-offsetY + showMapH / 2) * 0.05f,
//        (-offsetX + showMapW / 2) * 0.05f, 0f));
//        ( - offsetX + imageMapH/2 ) * 0.05f = snowBot.getPose().getY()
//        ( - offsetX + imageMapH/2 ) = snowBot.getPose().getY()/0.05f
//        - offsetX =  snowBot.getPose().getY()/0.05f -  imageMapH/2
//        offsetX =  imageMapH/2 - snowBot.getPose().getY()/0.05f
        float offsetX = snowBot.getShowMapH() / 2 - snowBot.getCurrentPose().getY() / 0.05f;
        float offsetY = snowBot.getShowMapH() / 2 - snowBot.getCurrentPose().getX() / 0.05f;

        if (checkHome(offsetX, offsetY)) {
            addHomeDialog(offsetX, offsetY);
            Csjlogger.debug("offsetX = {}, offsetY = {}", offsetX, offsetY);
        }
    }

    public void setAddWall() {
        CSJToast.showToast(this, getString(R.string.cancle_add_wall), 1000);
        addWallTv.setText(getString(R.string.add_wall));
        addWallIv.setBackground(getResources().getDrawable(R.drawable.addwall));
        isAddingWall = false;
        startP = null;
        endP = null;
    }

    public void setCancleAddWall() {
        isAddingWall = true;
        CSJToast.showToast(this, getString(R.string.start_add_wall), 1000);
        addWallTv.setText(getString(R.string.cancle_add_wall));
        addWallIv.setBackground(getResources().getDrawable(R.mipmap.add_wall));
    }

    public void setAddHome() {
        CSJToast.showToast(this, getString(R.string.cancle_add_home), 1000);
        addHomeTv.setText(getString(R.string.add_home));
        isAddHome = false;
        startP = null;
        endP = null;
    }

    public void setCancleAddHome() {
        CSJToast.showToast(this, getString(R.string.start_add_home), 1000);
        addHomeTv.setText(getString(R.string.cancle_add_home));
        isAddHome = true;
        startP = null;
        endP = null;
    }

    private Bitmap superposition(Bitmap oriMap, Bitmap arrow, float offsetX, float offsetY) {
        // 另外创建一张图片
        // 创建一个新的和SRC长度宽度一样的位图
        Bitmap newb = Bitmap.createBitmap(oriMap.getWidth(), oriMap.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(newb);

        canvas.drawBitmap(oriMap, 0, 0, null);// 在 0，0坐标开始画入原图片src

        if (!isAddingWall && !isAddHome) {
            canvas.drawBitmap(arrow, offsetX - arrow.getWidth() / 2, offsetY - arrow.getHeight() / 2, null);
        }

        if (isAddHome) {
            canvas.drawBitmap(addWallSP, mOffsetX - addWallSP.getWidth() / 2, mOffsetY - addWallSP.getHeight() / 2, null);
        }

        if (startP != null) {
            canvas.drawBitmap(addWallSP, startP.x - addWallSP.getWidth() / 2, startP.y - addWallSP.getHeight() / 2, null);
        }

        if (endP != null) {
            canvas.drawBitmap(addWallSP, endP.x - addWallSP.getWidth() / 2, endP.y - addWallSP.getHeight() / 2, null);
        }

        if (null != homeDatas && homeDatas.size() > 0) {
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextSize(12);
            for (int i = 0; i < homeDatas.size(); i++) {
                canvas.drawBitmap(homeIcon, homeDatas.get(i).getmOffsetX() - homeIcon.getWidth() / 2, homeDatas.get(i).getmOffsetY() - homeIcon.getHeight() / 2, null);
                canvas.drawText(homeDatas.get(i).getHomename(), homeDatas.get(i).getmOffsetX() + homeIcon.getWidth(), homeDatas.get(i).getmOffsetY() + homeIcon.getHeight() / 2, paint);
            }
        }
        return newb;
    }

    /**
     * @param x
     * @param y
     * @return 检查点击的点是否在地图的范围内，房间数目是否超出10个
     */
    private boolean checkHome(float x, float y) {
        //如果点击区域不在范围内,则不添加房间
//        if (x < (screenW - rightLayoutW - imageMapH) / 2 || x > (screenW - rightLayoutW - imageMapH) / 2 + imageMapH) {
//            CSJToast.showToast(this, getString(R.string.choose_right_home));
//            return false;
//        }

        if (null != homeDatas && homeDatas.size() >= MAXHOME) {
            CSJToast.showToast(this, getString(R.string.max_home));
            return false;
        }
        return true;
    }

    /**
     * @Author: jl
     * @Date: 2016/12/20
     * @Desc:添加房间
     */
    private void addHome(float x, float y, String homeName) {
        Location location = new Location((-y + bm.getHeight() / 2) * 0.05f,
                (-x + bm.getWidth() / 2) * 0.05f, 0f);

        Csjlogger.debug("location is  {}", location.toString());

        Home home = new Home();
        home.setmOffsetX(x);
        home.setmOffsetY(y);
        home.setLocation(location);
        home.setHomename(homeName);
        homeDatas.add(home);
        SharedUtil.setListObj(SharedKey.HOMEDATAS, homeDatas);
        adapter.setData(homeDatas);
        sendHomeData();
    }

    private void addHomeDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.pup_window_addhome, null);
        EditText editText = (EditText) view.findViewById(R.id.room_name_ed);
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle(getString(R.string.input_rome_name));
        builder.setView(view);
        builder.setPositiveButton(getString(R.string.sure), (dialog, which) -> {
            DialogUtil.canCloseDialog(dialog, false);
            String roomName = editText.getText().toString();
            if (StrUtil.isBlank(roomName)) {
                return;
            }
            if (checkHomeName(roomName)) {
                addHome(homeX, homeY, roomName);
                DialogUtil.canCloseDialog(dialog, true);
            }
        });

        builder.setNegativeButton(getString(R.string.cancle), (dialog, which) -> DialogUtil.canCloseDialog(dialog, true));
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(dialog1 -> snowBot.getMap(this, this));
        dialog.setOnShowListener(dialog1 -> snowBot.stopGetMap());
        dialog.show();
    }

    private void addHomeDialog(final float offsetX, final float offsetY) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.pup_window_addhome, null);
        EditText editText = (EditText) view.findViewById(R.id.room_name_ed);
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle(getString(R.string.input_rome_name));
        builder.setView(view);
        builder.setPositiveButton(getString(R.string.sure), (dialog, which) -> {
            DialogUtil.canCloseDialog(dialog, false);
            String roomName = editText.getText().toString();
            if (StrUtil.isBlank(roomName)) {
                return;
            }
            if (checkHomeName(roomName)) {
                addHome(offsetX, offsetY, roomName);
                DialogUtil.canCloseDialog(dialog, true);
            }

        });
        builder.setNegativeButton(getString(R.string.cancle), (dialog, which) -> DialogUtil.canCloseDialog(dialog, true));
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(dialog1 -> snowBot.getMap(this, this));
        dialog.setOnShowListener(dialog1 -> snowBot.stopGetMap());
        dialog.show();
    }


    /**
     * 检查房间姓名
     */
    private boolean checkHomeName(String homeName) {
        for (Home home : homeDatas) {
            if (homeName.equals(home.getHomename())) {
                CSJToast.showToast(this, getString(R.string.home_name_duplicate), 1000);
                return false;
            }
        }
        return true;
    }

    /**
     * 修改房间名字
     *
     * @param postion
     */
    private void modifyHomeName(int postion) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.pup_window_addhome, null);
        EditText editText = (EditText) view.findViewById(R.id.room_name_ed);
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle(getString(R.string.input_rome_name));
        builder.setView(view);
        builder.setPositiveButton(getString(R.string.sure), (dialog, which) -> {
            String roomName = editText.getText().toString();
            if (StrUtil.isBlank(roomName)) {
                return;
            }
            homeDatas.get(postion).setHomename(roomName);
            adapter.setData(homeDatas);
            SharedUtil.setListObj(SharedKey.HOMEDATAS, homeDatas);
            sendHomeData();
            dialog.dismiss();
        });
        builder.setNegativeButton(getString(R.string.cancle), (dialog, which) -> {

        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onResume() {
        Csjlogger.debug("onResume");
        snowBot.setAutoUpdateMap(false);
        snowBot.getMap(this, this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        Csjlogger.debug("onPause");
        snowBot.setAutoUpdateMap(false);
        snowBot.stopGetMap();
        if (!isGoHome) {
            snowBot.stopPartol();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick(R.id.modify_home_layout)
    public void onClick() {
        if (isRecoveryMapData) {
            return;
        }
        wakeupMapUpdate(true);
        if (null != homeDatas && homeDatas.size() > 0) {
            delHomeSureLayout.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(adapterEdit);
        } else {
            CSJToast.showToast(this, getString(R.string.home_empty), 1000);
        }

    }

    /**
     * 保存地图
     */
    @OnClick(R.id.save_map_layout)
    public void saveMapData() {
        if (isRecoveryMapData) {
            return;
        }
        wakeupMapUpdate(true);

        if (CommonTool.isFastDoubleClick()) {
            return;
        }
        new Thread(() -> {
            saveMap();
            runOnUiThread(() ->
                    CSJToast.showToast(Static.CONTEXT, getResources().getString(R.string.map_save_success)));
        }).start();
    }

    /**
     * 恢复地图
     */
    @OnClick(R.id.recovery_map_layout)
    public void recoveryMap() {
        if (isRecoveryMapData) {
            return;
        }
        recoveryMapData();
    }

    /**
     * 删除房间的点击事件
     */
    @OnClick({R.id.cancel, R.id.sure})
    public void onClickBtn(View view) {
        wakeupMapUpdate(true);
        if (isRecoveryMapData) {
            return;
        }
        switch (view.getId()) {
            case R.id.cancel:
                chooseResults.clear();
                recyclerView.setAdapter(adapter);
                delHomeSureLayout.setVisibility(View.GONE);
                break;
            case R.id.sure:
                if (null != chooseResults && chooseResults.size() > 0) {
                    for (Home i : chooseResults) {
                        homeDatas.remove(i);
                    }
                }
                SharedUtil.setListObj(SharedKey.HOMEDATAS, homeDatas);
                for (Home i : homeDatas) {
                    Csjlogger.debug("getHomename" + i.getHomename());
                }
                sendHomeData();
                chooseResults.clear();
                adapter.setData(homeDatas);
                adapterEdit.setData(homeDatas);
                delHomeSureLayout.setVisibility(View.GONE);
                recyclerView.setAdapter(adapter);
                break;
        }
    }

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        ButterKnife.bind(this);
        init();
        initData();
        initRecylerView();
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_map_new;
    }


    @OnClick(R.id.go_home_layout)
    public void goHome() {
        isGoHome = true;
        snowBot.stopPartol();
        mHandler.postDelayed(() -> snowBot.goHome(), 2000);
        CsjSpeechSynthesizer.getSynthesizer().startSpeaking("小雪回去充电辣~", null);
        CSJToast.showToast(this, "回去充电");
    }

    /**
     * 巡逻
     */
    @OnClick(R.id.go_patrol_layout)
    public void goParol() {
        if (isRecoveryMapData) {
            return;
        }

        if (snowBot.isLowPowerDetected()) {
            CsjSpeechSynthesizer.getSynthesizer().startSpeaking(getString(R.string.low_power_warning), null);
            return;
        }

        wakeupMapUpdate(true);
        if (!snowBot.isPartol()) {
            List<Home> homeLists = SharedUtil.getListObj(SharedKey.HOMEDATAS, Home.class);

            if (null == homeLists) {
                CsjSpeechSynthesizer.getSynthesizer().startSpeaking(getResources().getString(R.string.set_patrol_point), null);
                CSJToast.showToast(this, getResources().getString(R.string.set_patrol_point), 1000);
                return;
            }

            if (homeLists.size() < 2) {
                CSJToast.showToast(this, getResources().getString(R.string.set_more_patrol_point), 1000);
                return;
            }

            poses.clear();

            for (Home home : homeLists) {
                Pose pose = new Pose();
                pose.setX(home.getLocation().getX());
                pose.setY(home.getLocation().getY());
//                pose.setZ(home.getRotation().getYaw());
                poses.add(pose);
            }
        }

        if (!snowBot.isPartol()) {
            CsjSpeechSynthesizer.getSynthesizer().startSpeaking(getResources().getString(R.string.start_patrol_speech), null);
            CSJToast.showToast(this, getResources().getString(R.string.start_patrol_speech), 1000);
            snowBot.partol(poses);
            snowBot.setAutoUpdateMap(false);
        } else {
            snowBot.stopPartol();
            CSJToast.showToast(this, getResources().getString(R.string.stop_patrol_speech), 1000);
            snowBot.setAutoUpdateMap(false);
        }
    }

    @Override
    public void getMap(Bitmap map) {
        Message msg = mHandler.obtainMessage();
        Bundle bundle = new Bundle();
//        Csjlogger.debug("get map");
        bundle.putParcelable("bitmap", map);
        msg.setData(bundle);
        msg.what = MAP_UPDATE;
        mHandler.sendMessage(msg);
    }

    private static class MapActivityHandler extends WeakReferenceHandler<MapActivity> {

        MapActivityHandler(MapActivity reference) {
            super(reference);
        }

        @Override
        protected void handleMessage(MapActivity reference, Message msg) {
            switch (msg.what) {
                case MAP_UPDATE:
                    reference.bm = msg.getData().getParcelable("bitmap");
                    reference.mapImageView.setImageBitmap(reference.superposition(reference.bm,
                            BitmapFactory.decodeResource(reference.getResources(), R.mipmap.go_point),
                            reference.mOffsetX, reference.mOffsetY));
                    break;
                default:
                    break;
            }
        }

    }

    /**
     * 清除房间数据
     */
    private void clearHomeData() {
        if (null != homeDatas && homeDatas.size() > 0) {
            homeDatas.clear();
            SharedUtil.setListObj(SharedKey.HOMEDATAS, homeDatas);
            chooseResults.clear();
            adapter.setData(homeDatas);
            adapterEdit.setData(homeDatas);
        }
    }

    /**
     * 发送房间数据
     */
    private void sendHomeData() {
    }

    /**
     * 去卧室
     */
    private void goRoom(int postion) {
        if (null != homeDatas && homeDatas.size() > 0) {
            Home home = homeDatas.get(postion);
            SnowBotMoveServer.getInstance().moveTo(home.getmOffsetX(), home.getmOffsetY());
//            CsjLog.d("臥室 " + home.getmOffsetX() + home.getmOffsetY());
        }
    }

    /**
     * saveMap
     * 保存map
     */
    private void saveMap() {
        BackUpMapTool.saveMap();
//        Map map = SnowBotMoveServer.getInstance().getBackUpMap();
//        if (null != map) {
//            MapDataBean mapDataBean = new MapDataBean();
//            mapDataBean.setDimension(map.getDimension());
////            mapDataBean.setCreateTime(System.currentTimeMillis());
//            mapDataBean.setOrigin(map.getOrigin());
//            mapDataBean.setResolution(map.getResolution());
//            mapDataBean.setTimestamp(map.getTimestamp());
//            mapDataBean.setData(Base64.encodeToString(map.getData(), Base64.NO_WRAP));
//            BackUpMapTool.saveMapToSD(mapDataBean);
//            BackUpMapTool.saveMapToLastDir(mapDataBean);
//        }
    }


    /**
     * 恢复地图
     */

    private void recoveryMapData() {
        wakeupMapUpdate(false);
        MapDataBean mapDataBean = BackUpMapTool.getLastFile();
        try {
            Map map = new Map(mapDataBean.getOrigin(), mapDataBean.getDimension(), mapDataBean.getResolution(), mapDataBean.getTimestamp(), Base64.decode(mapDataBean.getData(), Base64.NO_WRAP));
            List<Line> wallsList = new ArrayList<>();
            for (int i = 0; i < mapDataBean.getWallsData().size(); i++) {
                Line line = new Line(mapDataBean.getWallsData().get(i).getSegmentId(), mapDataBean.getWallsData().get(i).getLine_startPoint_x(), mapDataBean.getWallsData().get(i).getLine_startPoint_y(),
                        mapDataBean.getWallsData().get(i).getLine_endPoint_x(), mapDataBean.getWallsData().get(i).getLine_endPoint_y());
                wallsList.add(line);
            }

            if (null != mapDataBean) {
                // 先把各种都关了
                SnowBotMoveServer.getInstance().setIsInRecoveryMapData(true);
                isRecoveryMapData = true;
                SharedUtil.setPreferInt(SharedKey.AIUISERVICESWITCH, 0);

                View view = LayoutInflater.from(this).inflate(R.layout.dialog_recover_map, null);
                ImageView iv = (ImageView) view.findViewById(R.id.recovery_iv);
                iv.setImageBitmap(BackUpMapTool.getMapPic(mapDataBean));

                AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                builder.setCancelable(false);
                builder.setView(view);
                builder.setNegativeButton(getResources().getString(R.string.cancle), (dialog, which) -> {
                    wakeupMapUpdate(true);
                    SnowBotMoveServer.getInstance().setIsInRecoveryMapData(false);
                    SharedUtil.setPreferInt(SharedKey.AIUISERVICESWITCH, 1);
                    isRecoveryMapData = false;
                    dialog.dismiss();
                });
                builder.setPositiveButton("还原", (dialog, which) -> {
                    if (null != map) {
                        timeUtil.getTime(5, i -> {
                            mLoadToast.success();
                            SnowBotMoveServer.getInstance().setIsInRecoveryMapData(false);
                            SharedUtil.setPreferInt(SharedKey.AIUISERVICESWITCH, 1);
                            isRecoveryMapData = false;
                        });
                        SnowBotMoveServer.getInstance().recoveryMapData(map, wallsList);


                        mLoadToast.setText("正在恢复地图");
                        mLoadToast.show();
                        wakeupMapUpdate(true);
//                            CSJToast.showToast(MapActivity.this, getResources().getString(R.string.restore_map_succeed));
                    }
                    dialog.dismiss();
                });
                builder.create().show();
            }
        } catch (NullPointerException e) {
            wakeupMapUpdate(true);

            CsjSpeechSynthesizer.getSynthesizer().startSpeaking(getResources().getString(R.string.map_not_exist), null);
        }
    }

//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void robotStatusUpdated(RobotStatusUpdateEvent event) {
//        int powerPercent = event.getBatteryPercentage();
//        if (powerPercent < Constants.LOW_POWER_GO_HOME && isPartol) {
//            CsjSpeechSynthesizer2.getSynthesizer().startSpeaking(getString(R.string.low_power_waring), null);
//            snowBot.cancelAction();
//        }
//    }


}
