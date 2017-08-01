package com.csjbot.snowbot.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.android.core.util.MD5Util;
import com.android.core.util.SharedUtil;
import com.android.core.util.StrUtil;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.Fragment.BaseFrg;
import com.csjbot.snowbot.Fragment.FifthFrg;
import com.csjbot.snowbot.Fragment.FirstFrg;
import com.csjbot.snowbot.Fragment.FourthFrg;
import com.csjbot.snowbot.Fragment.OutWareFrg;
import com.csjbot.snowbot.Fragment.SecFrg;
import com.csjbot.snowbot.Fragment.ThrFrg;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.bean.BFeed;
import com.csjbot.snowbot.bean.OutWare;
import com.csjbot.snowbot.utils.CommonTool;
import com.csjbot.snowbot.utils.IntentUtil;
import com.csjbot.snowbot.utils.OkHttp.DisposeDataListener;
import com.csjbot.snowbot.utils.OkHttp.HttpUtil;
import com.csjbot.snowbot.utils.OkHttp.OkHttpException;
import com.csjbot.snowbot.utils.RobotStatus;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot.utils.SharedPreferencesSDUtil;
import com.csjbot.snowbot.utils.SpeechStatus;
import com.csjbot.snowbot.utils.TimeUtil;
import com.csjbot.snowbot.utils.UUIDGenerator;
import com.csjbot.snowbot.utils.UrlUtil;
import com.csjbot.snowbot.views.DepthPageTransformer;
import com.csjbot.snowbot.views.FixedSpeedScroller;
import com.csjbot.snowbot.views.MyViewPager;
import com.csjbot.snowbot_rogue.utils.CSJToast;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ClipPagerTitleView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @Author: jl
 * @Date: 2016/12/15
 * @Desc:引导页面
 */

public class GuideAct extends CsjUIActivity {

    @BindView(R.id.viewPager)
    MyViewPager viewPager;
    @BindView(R.id.magic_indicator3)
    MagicIndicator magicIndicator;

    private FragmentPagerAdapter pagerAdapter;
    private List<BaseFrg> fragments = new ArrayList<BaseFrg>();
    private int currentItem = 0;
    private int MINVIEWPAGEITEM = 0;
    private int MAXVIEWPAGEITEM = 0;
    private OutWareFrg outWareFrag;
    private FirstFrg firstFrag;
    private SecFrg secFrag;
    private ThrFrg thrFrag;
    private FourthFrg fourthFrg;
    private FifthFrg fifthFrg;
    private int ROBOTSTATUS = 0;
    private FixedSpeedScroller mScroller;


    /**
     * 初始化指示器
     */
    private void initMagicIndicator() {
        MagicIndicator magicIndicator = (MagicIndicator) findViewById(R.id.magic_indicator3);
        magicIndicator.setBackgroundResource(R.drawable.round_indicator_bg);
        CommonNavigator commonNavigator = new CommonNavigator(this);
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return fragments == null ? 0 : fragments.size();
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                ClipPagerTitleView clipPagerTitleView = new ClipPagerTitleView(context);
                clipPagerTitleView.setText(fragments.get(index).getTitle());
                clipPagerTitleView.setTextColor(Color.parseColor("#D9B8A2"));
                clipPagerTitleView.setClipColor(Color.WHITE);
                return clipPagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator indicator = new LinePagerIndicator(context);
                float navigatorHeight = context.getResources().getDimension(R.dimen.dm_50dp);
                float borderWidth = UIUtil.dip2px(context, 1);
                float lineHeight = navigatorHeight - 2 * borderWidth;
                indicator.setLineHeight(lineHeight);
                indicator.setRoundRadius(lineHeight / 2);
                indicator.setYOffset(borderWidth);
                indicator.setColors(Color.parseColor("#D9B8A2"));
                return indicator;
            }
        });
        magicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(magicIndicator, viewPager);
    }


    /**
     * 初始化fragments viewpager数据
     */
    private void initData() {
        ROBOTSTATUS = SharedUtil.getPreferInt(SharedKey.ROBOTREGISTERSTATUS, 0);
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                return false;
            }
        });
        viewPager.setScanScroll(false);

        firstFrag = new FirstFrg();
        secFrag = new SecFrg();
        thrFrag = new ThrFrg();
        fourthFrg = new FourthFrg();
        fifthFrg = new FifthFrg();
        outWareFrag = new OutWareFrg();
        if (fragments.size() > 0) {
            fragments.clear();
        }
        switch (ROBOTSTATUS) {
            case RobotStatus.NONOUTWARE:
                secFrag.setNoBtnGone();
                secFrag.setTitle(getString(R.string.one_step));
                outWareFrag.setTitle(getString(R.string.two_step));
                fragments.add(secFrag);
                fragments.add(outWareFrag);
                break;
            default:
                firstFrag.setTitle(getString(R.string.one_step));
                secFrag.setTitle(getString(R.string.two_step));
                thrFrag.setTitle(getString(R.string.thr_step));
                fourthFrg.setTitle(getString(R.string.four_step));
                fifthFrg.setTitle(getString(R.string.five_step));
                fragments.add(firstFrag);
                fragments.add(secFrag);
                fragments.add(thrFrag);
                fragments.add(fourthFrg);
                fragments.add(fifthFrg);

        }
        initMagicIndicator();
        MAXVIEWPAGEITEM = fragments.size() - 1;
        for (BaseFrg frag : fragments) {
            fragmentSetOnClickListener(frag);
        }
    }


    private void init() {
        pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }
        };
        viewPager.setPageTransformer(true, new DepthPageTransformer());
        viewPager.setAdapter(pagerAdapter);
        try {
            Field mField = ViewPager.class.getDeclaredField("mScroller");
            mField.setAccessible(true);
            mScroller = new FixedSpeedScroller(viewPager.getContext(), new AccelerateInterpolator());
            mField.set(viewPager, mScroller);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setViewPageCurrentItem(currentItem);
    }

    private int mLastMotionX, mLastMotionY;
    //是否移动了
    private boolean isMoved;
    //移动的阈值
    private static final int TOUCH_SLOP = 40;

    private TimeUtil timeUtil = TimeUtil.getInterface();

    /**
     * @Desc:设置fragment的监听事件
     */
    private void fragmentSetOnClickListener(BaseFrg fragment) {
        fragment.setOnClickLister(new BaseFrg.OnClickLister() {
            @Override
            public void clickYes() {
                if (CommonTool.isFastDoubleClick()) {
                    return;
                }
                int registerResult = SharedUtil.getPreferInt(SharedKey.ROBOTREGISTERSTATUS, 0);
                //主人注册和验证code
                if (fragments.get(viewPager.getCurrentItem()) instanceof ThrFrg && (registerResult != RobotStatus.ALREADLYREGISTER)) {
                    uploadCusInfo();
                    return;
                }


                //上传小雪人昵称
                if (fragments.get(viewPager.getCurrentItem()) instanceof FourthFrg && registerResult != RobotStatus.ALREADCOMPLETE) {
                    uploadRebotName();
                    return;
                }


                if (viewPager.getCurrentItem() == MAXVIEWPAGEITEM) {
//                    SharedUtil.setPreferInt(SharedKey.ROBOTREGISTERSTATUS, RobotStatus.ALREADCOMPLETE);
                    IntentUtil.startActivity(GuideAct.this, LauncherActivity.class);
                    GuideAct.this.finish();
                }
                setViewPageCurrentItem(currentItem + 1);
            }

            @Override
            public void clickNo() {
                setViewPageCurrentItem(currentItem - 1);
            }

            @Override
            public void longClick(MotionEvent event) {//长按事件，长按7秒弹出跳过界面
                int x = (int) event.getX();
                int y = (int) event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mLastMotionX = x;
                        mLastMotionY = y;
                        isMoved = false;
                        timeUtil.getTime(5, i -> {
                            if (i == 0) {
                                showTypeDialog();
                            }
                        });
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (isMoved) break;
                        if (Math.abs(mLastMotionX - x) > TOUCH_SLOP
                                || Math.abs(mLastMotionY - y) > TOUCH_SLOP) {
                            //移动超过阈值，则表示移动了
                            isMoved = true;
                            timeUtil.stop();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        //释放了
                        timeUtil.stop();
                        break;
                }
            }

            @Override
            public void clickSkip() {
                SharedUtil.setPreferInt(SharedKey.ROBOTREGISTERSTATUS, RobotStatus.ALREADLYREGISTER);
                SharedUtil.setPreferBool(SharedKey.SKIP_REGISTRATION, true);
                IntentUtil.startActivity(GuideAct.this, LauncherActivity.class);
                GuideAct.this.finish();
            }
        });
    }

    private void showTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        View view = View.inflate(this, R.layout.dialog_skin_reg, null);
        EditText tv_user_accounts = (EditText) view.findViewById(R.id.user_accounts);
        EditText tv_user_password = (EditText) view.findViewById(R.id.user_password);
        Button button_ensure = (Button) view.findViewById(R.id.ensureSkip);
        Button button_cancle = (Button) view.findViewById(R.id.cancelSkip);
        button_ensure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user_accounts = tv_user_accounts.getText().toString();
                String user_password = tv_user_password.getText().toString();
                if (StrUtil.isNotBlank(user_accounts) && user_accounts.equalsIgnoreCase("csjbot123")
                        && StrUtil.isNotBlank(user_password) && user_password.equalsIgnoreCase("123456")) {
                    initMainAct();
                    dialog.dismiss();
                } else {
                    CSJToast.showToast(GuideAct.this, getResources().getString(R.string.enter_current_info));
                }
            }
        });
        button_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setView(view);
        dialog.show();
    }

    /**
     * 跳到主页面
     */
    private void initMainAct() {
        new Handler().postDelayed(() -> {
            IntentUtil.startActivity(GuideAct.this, LauncherActivity.class);
            GuideAct.this.finish();
        }, 100);
    }

    private void setViewPageCurrentItem(int i) {
        if (i > MAXVIEWPAGEITEM || i < MINVIEWPAGEITEM) {
            return;
        }
        viewPager.setCurrentItem(i);
        currentItem = i;
    }

    /**
     * @Author: jl
     * @Date: 2016/12/16
     * @Desc:上传主人和验证码相关信息,验证信息
     */
    public void uploadCusInfo() {
        String psd = SharedUtil.getPreferStr(SharedKey.PASSWORD);
        String finalPsd = "";
        if (StrUtil.isNotBlank(psd)) {
            String tempPsd = MD5Util.MD5(psd);

        }
        Map<String, String> map = new HashMap();
        map.put("mobile", SharedUtil.getPreferStr(SharedKey.MASTERNUM));
        map.put("auth_code", SharedUtil.getPreferStr(SharedKey.VERIFYNUM));
        map.put("password", SharedUtil.getPreferStr(SharedKey.PASSWORD));
        map.put("nickname", SharedUtil.getPreferStr(SharedKey.MASTERNICKNAME));
        map.put("uid", SharedUtil.getPreferStr(SharedKey.DEVICEUUID));
        Csjlogger.info("注册信息 " + map.toString());
        String masterRegisterUrl = (String) SharedPreferencesSDUtil.get(context, "urlUtil", SharedKey.MASTERREGISTER, UrlUtil.MASTERREGISTER);
        HttpUtil.postJson(this, masterRegisterUrl, map, BFeed.class, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                BFeed feed = (BFeed) responseObj;
                OutWare outWare = feed.getData();
                if (outWare.isFlag()) {
                    SharedUtil.setPreferInt(SharedKey.ROBOTREGISTERSTATUS, RobotStatus.ALREADLYREGISTER);
                    setViewPageCurrentItem(currentItem + 1);
                } else {
                    CSJToast.showToast(GuideAct.this, outWare.getMessage());
                }
            }

            @Override
            public void onFail(Object reasonObj) {
                if (reasonObj instanceof OkHttpException) {
                    OkHttpException error = (OkHttpException) reasonObj;

                    if (error.getReason() == OkHttpException.REASON_ALL_READY_REG
                            || error.getReason() == OkHttpException.REASON_RESP_BLANK
                            || error.getReason() == OkHttpException.REASON_PARSER_JSON_ERROR
                            || error.getReason() == OkHttpException.REANSON_UNKNOW) {
                        CSJToast.showToast(GuideAct.this, getResources().getString(R.string.snowbot_registration_exist));
                    }
                }

            }
        });
    }

    /**
     * @Author: jl
     * @Date: 2016/12/19
     * @Desc:上传小雪人昵称
     */

    public void uploadRebotName() {
        Map<String, String> map = new HashMap();
        map.put("uid", SharedUtil.getPreferStr(SharedKey.DEVICEUUID));
        map.put("name", SharedUtil.getPreferStr(SharedKey.ROBOTNICKNAME));
        String updateRobotUrl = (String) SharedPreferencesSDUtil.get(context, "urlUtil", SharedKey.UPDATEROBOT, UrlUtil.UPDATEROBOT);
        HttpUtil.postJson(null, updateRobotUrl, map, BFeed.class, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                BFeed feed = (BFeed) responseObj;
                OutWare outWare = feed.getData();
                if (outWare.isFlag()) {
                    setViewPageCurrentItem(currentItem + 1);

                } else {
                    CSJToast.showToast(GuideAct.this, outWare.getMessage());
                }
            }

            @Override
            public void onFail(Object reasonObj) {
                CSJToast.showToast(GuideAct.this, getResources().getString(R.string.snowbot_name_fail));
            }
        });
    }


    /**
     * @Author: jl
     * @Date: 2016/12/19
     * @Desc:
     */

    private void isWareHouse() {
        if (CommonTool.isFastDoubleClick()) {
            return;
        }
        Map<String, String> map = new HashMap<>();
        map.put("uid", UUIDGenerator.getInstance().getDeviceUUID());
        String outHouseUrl = (String) SharedPreferencesSDUtil.get(context, "urlUtil", SharedKey.OUTWAREHOUSE, UrlUtil.OUTWAREHOUSE);
        HttpUtil.postJson(null, outHouseUrl, map, BFeed.class, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                BFeed feed = (BFeed) responseObj;
            }

            @Override
            public void onFail(Object reasonObj) {

            }
        });


    }

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        //butterKinfe框架注解
        SpeechStatus.getIstance().setAiuiResponse(false);
        ButterKnife.bind(this);
        initData();
        init();
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.act_guide_act;
    }

}
