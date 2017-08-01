package com.csjbot.snowbot.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.alibaba.fastjson.JSON;
import com.android.core.entry.Static;
import com.android.core.util.SharedUtil;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.bean.BFeed;
import com.csjbot.snowbot.bean.RegisterALiYunBean;
import com.csjbot.snowbot_rogue.app.CsjSpeechSynthesizer;
import com.csjbot.snowbot.bean.aiui.entity.CsjSynthesizerListener;
import com.csjbot.snowbot.utils.CommonTool;
import com.csjbot.snowbot.utils.OkHttp.DisposeDataListener;
import com.csjbot.snowbot.utils.OkHttp.HttpUtil;
import com.csjbot.snowbot.utils.OkHttp.OkHttpException;
import com.csjbot.snowbot.utils.RobotStatus;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot.utils.SharedPreferencesSDUtil;
import com.csjbot.snowbot.utils.SpeechStatus;
import com.csjbot.snowbot.utils.UUIDGenerator;
import com.csjbot.snowbot.utils.UrlUtil;
import com.csjbot.snowbot_rogue.utils.CSJToast;
import com.iflytek.cloud.SpeechError;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @Author: jl
 * @Date: 2016/12/15
 * @Desc:
 */

public class OutWareFrg extends BaseFrg {
    @BindView(R.id.our_ware_btn)
    Button ourWareBtn;

    @Override
    public int getContentViewId() {
        return R.layout.frag_outware_layout;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @OnClick({R.id.no_btn, R.id.our_ware_btn})
    public void onClick(View view) {
        if (null == onClickLister) {
            return;
        }
        switch (view.getId()) {
            case R.id.our_ware_btn:
                buttonAvailable(false);
                registerALiYUn();
                break;
            case R.id.no_btn:
                onClickLister.clickNo();
        }
    }

    /**
     * 后台注册
     */
    private void outWareHouse() {
        if (CommonTool.isFastDoubleClick()) {
            return;
        }
        Map<String, String> map = new HashMap<>();
        map.put("uid", UUIDGenerator.getInstance().getDeviceUUID());
        String outHouseUrl = (String) SharedPreferencesSDUtil.get(context, "urlUtil", SharedKey.OUTWAREHOUSE, UrlUtil.OUTWAREHOUSE);
        HttpUtil.postJson(getActivity(), outHouseUrl, map, BFeed.class, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                BFeed feed = (BFeed) responseObj;
                if (feed.getData().isFlag()) {
                    SharedUtil.setPreferInt(SharedKey.ROBOTREGISTERSTATUS, RobotStatus.ALREADYOUTWARE);
                    ourWareBtn.setText(getString(R.string.alreadly_out_ware));
                    CsjSpeechSynthesizer.getSynthesizer().startSpeaking(Static.CONTEXT.getResources().getString(R.string.snowbot_already_outware), new CsjSynthesizerListener() {
                        @Override
                        public void onSpeakBegin() {
                            SpeechStatus.getIstance().setSpeakFinished(false);
                            while (!(SharedUtil.getPreferInt(SharedKey.ROBOTREGISTERSTATUS, 0) == RobotStatus.ALREADYOUTWARE)) {
                                SharedUtil.setPreferInt(SharedKey.ROBOTREGISTERSTATUS, RobotStatus.ALREADYOUTWARE);
                            }
                        }

                        @Override
                        public void onCompleted(SpeechError speechError) {
                            SpeechStatus.getIstance().setSpeakFinished(true);
                            SharedUtil.setPreferInt(SharedKey.ROBOTREGISTERSTATUS, RobotStatus.ALREADYOUTWARE);
                            if (SharedUtil.getPreferInt(SharedKey.ROBOTREGISTERSTATUS, 0) == RobotStatus.ALREADYOUTWARE) {
                                CommonTool.rebootDevice();
                            }
                        }
                    });


                } else {
                    CSJToast.showToast(getActivity(), feed.getData().getMessage());
                }
            }

            @Override
            public void onFail(Object reasonObj) {
                buttonAvailable(true);
            }
        });
    }

    /**
     * 设置按钮是否可用，不可用变灰
     *
     * @param available
     */
    private void buttonAvailable(boolean available) {
        ourWareBtn.setEnabled(available);
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        buttonAvailable(false);
        CsjSpeechSynthesizer.getSynthesizer().startSpeaking(Static.CONTEXT.getResources().getString(R.string.snowbot_outware), new CsjSynthesizerListener() {
            @Override
            public void onSpeakBegin() {
                SpeechStatus.getIstance().setSpeakFinished(false);
            }

            @Override
            public void onCompleted(SpeechError speechError) {
                SpeechStatus.getIstance().setSpeakFinished(true);
                registerALiYUn();
            }
        });
    }

    @Override
    protected void onInvisible() {
        super.onInvisible();
        CsjSpeechSynthesizer.getSynthesizer().stopSpeaking();
    }

    /**
     * 注册阿里云
     */
    private void registerALiYUn() {
        Map<String, String> contentTemp = new HashMap<>();
        contentTemp.put("uid", UUIDGenerator.getInstance().getDeviceUUID());
        contentTemp.put("product", "xiaoxue");
        String contentStr = JSON.toJSONString(contentTemp);
        Map<String, String> map = new HashMap<>();
        map.put("service", "RegisterDevice");
        map.put("server", "iot");
        map.put("content", contentStr);
        String registerAliyunUrl = (String) SharedPreferencesSDUtil.get(context, "urlUtil", SharedKey.REGISTERALIYUN, UrlUtil.REGISTERALIYUN);
        HttpUtil.postJson(null, registerAliyunUrl, map, RegisterALiYunBean.class, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                RegisterALiYunBean registerALiYunBean = (RegisterALiYunBean) responseObj;
                if (null != registerALiYunBean) {
                    SharedUtil.setPreferStr(SharedKey.PRODUCTKEY, registerALiYunBean.getData().getContent().getDevice().getProductKey());
                    SharedUtil.setPreferStr(SharedKey.DEVICEKEY, registerALiYunBean.getData().getContent().getDevice().getDeviceKey());
                    outWareHouse();
                }
            }

            @Override
            public void onFail(Object reasonObj) {
                buttonAvailable(true);
                Csjlogger.debug("reasonObj " + reasonObj.toString());
                if (reasonObj instanceof OkHttpException) {
                    OkHttpException error = (OkHttpException) reasonObj;
                    Csjlogger.debug("getReason " + error.getReason());

                    if (error.getReason() == OkHttpException.REASON_ALL_READY_REG
                            || error.getReason() == OkHttpException.REASON_RESP_BLANK
                            || error.getReason() == OkHttpException.REASON_PARSER_JSON_ERROR
                            || error.getReason() == OkHttpException.REANSON_UNKNOW) {
//                        CsjSpeechSynthesizer.getSynthesizer().startSpeaking("注册阿里云失败,已经存在,请联系研发人员", null);
                        CSJToast.showToast(getActivity(), getResources().getString(R.string.aliyun_exist));
                    }
                }
            }
        });
    }
}
