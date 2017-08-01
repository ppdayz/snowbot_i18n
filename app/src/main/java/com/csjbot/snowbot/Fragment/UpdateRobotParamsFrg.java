package com.csjbot.snowbot.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.android.core.entry.Static;
import com.android.core.util.SharedUtil;
import com.android.core.util.StrUtil;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.settings.ShowSNQRCodeActivity;
import com.csjbot.snowbot.bean.RegisterALiYunBean;
import com.csjbot.snowbot.bean.RobotParams;
import com.csjbot.snowbot.bean.aiui.entity.CsjSynthesizerListener;
import com.csjbot.snowbot.utils.OkHttp.DisposeDataListener;
import com.csjbot.snowbot.utils.OkHttp.HttpUtil;
import com.csjbot.snowbot.utils.OkHttp.OkHttpException;
import com.csjbot.snowbot.utils.RobotParamsUtil;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot.utils.SharedPreferencesSDUtil;
import com.csjbot.snowbot.utils.SpeechStatus;
import com.csjbot.snowbot.utils.UUIDGenerator;
import com.csjbot.snowbot.utils.UrlUtil;
import com.csjbot.snowbot_rogue.app.CsjSpeechSynthesizer;
import com.csjbot.snowbot_rogue.platform.SnowBotManager;
import com.csjbot.snowbot_rogue.utils.CSJToast;
import com.iflytek.cloud.SpeechError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import dou.utils.StringUtils;

/**
 * @Author: jl
 * @Date: 2016/12/15
 * @Desc:新增或者更新机器人参数fragment
 */

public class UpdateRobotParamsFrg extends BaseFrg {
    @BindView(R.id.tv_SN)
    TextView tvSN;//机器人唯一识别码
    @BindView(R.id.tv_up_computer)
    TextView tvUpComputer;//上位机
    @BindView(R.id.tv_up_palte)
    TextView tvUpPlate;//上身板
    @BindView(R.id.tv_down_plate)
    TextView tvDownPlate;//下身板
    @BindView(R.id.tv_navigation)
    TextView tvNavigation;//思岚导航
    @BindView(R.id.et_SN)
    EditText etSN;//机器人唯一识别码
    @BindView(R.id.et_up_computer)
    EditText etUpComputer;//上位机
    @BindView(R.id.et_up_palte)
    EditText etUpPlate;//上身板
    @BindView(R.id.et_down_plate)
    EditText etDownPlate;//下身板
    @BindView(R.id.et_navigation)
    EditText etNavigation;//思岚导航
    @BindView(R.id.our_ware_ok_btn)
    Button btnOK;//确认
    @BindView(R.id.our_ware_read_btn)
    Button btnRead;//读取
    @BindView(R.id.see_qr_code_btn)
    Button btnSeeQrCode;

    private boolean isAdd = false;//新增还是更新机器人参数  true新增  false更新

    private Handler mHandler = new Handler();

    @Override
    public int getContentViewId() {
        return R.layout.frag_robot_params;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @OnClick({R.id.see_qr_code_btn, R.id.our_ware_ok_btn, R.id.our_ware_read_btn})
    public void onClick(View view) {
        if (null == onClickLister && isAdd) {
            return;
        }
        switch (view.getId()) {
            case R.id.see_qr_code_btn:
//                onClickLister.clickNo();//返回
                gotoSeeQrCodeActivity();
                break;
            case R.id.our_ware_ok_btn://确认
//                buttonAvailable(false);
//                addOrUpdateRobotParam();
                getActivity().finish();
                break;
            case R.id.our_ware_read_btn://读取
                readRobotParam();
                break;
            default:
                break;
        }
    }

    private void gotoSeeQrCodeActivity() {
        String UpPlate = etUpPlate.getText().toString();
        String DownPlate = etDownPlate.getText().toString();
        String UpComputer = etUpComputer.getText().toString();
        String Navigation = etNavigation.getText().toString();

        if (StringUtils.isEmpty(etUpPlate.getText().toString())) {
            return;
        }
        if (StringUtils.isEmpty(etDownPlate.getText().toString())) {
            return;
        }

        if (StringUtils.isEmpty(etUpComputer.getText().toString())) {
            return;
        }
        if (StringUtils.isEmpty(etNavigation.getText().toString())) {
            return;
        }

        HashMap<String, String> deviceInfos = new HashMap<>();


        deviceInfos.put("上位机二维码", UpComputer);
        deviceInfos.put("上身板二维码", UpPlate);
        deviceInfos.put("下身板二维码", DownPlate);
        deviceInfos.put("导航板二维码", Navigation);

        Intent intent = new Intent(getContext(), ShowSNQRCodeActivity.class);
        intent.putExtra("deviceInfos", deviceInfos);
        startActivity(intent);

    }

    /**
     * 读取机器人设备参数
     */
    private void readRobotParam() {
        getUpPlateSN();
        getDwonPlateSN();
        getSlamSN();
        getUpComputor();
    }


    // 上身板
    private void getUpPlateSN() {
        String upSn = SnowBotManager.getInstance().getUpBodySN();
        etUpPlate.setText(upSn);
    }

    // 下身板
    private void getDwonPlateSN() {
        String downSN = SnowBotManager.getInstance().getDownBodySN();
        etDownPlate.setText(downSN);
    }

    //上位机
    private void getUpComputor() {
        String upComputorId = UUIDGenerator.getInstance().getDeviceUUID();
        etUpComputer.setText(upComputorId);
    }

    //思岚导航SN
    private void getSlamSN() {
        String url = UrlUtil.SLAMPARAM;
        HttpUtil.get(getActivity(), url, null, String.class, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                String str = (String) responseObj;
                try {
                    JSONObject jsonObj = new JSONObject(str);
                    String slamSN = jsonObj.getString("S/N");
                    etNavigation.setText(slamSN);
                } catch (Exception e) {

                }
            }

            @Override
            public void onFail(Object reasonObj) {
                CSJToast.showToast(getActivity(), reasonObj.toString(), 2000);
            }
        });
    }

    /**
     * 新增或者更新机器人设备参数
     */
    private void addOrUpdateRobotParam() {
        if (StrUtil.isNotBlank(verifyData())) {//必填项没有填
            CSJToast.showToast(Static.CONTEXT, verifyData());
            buttonAvailable(true);
            return;
        } else {//缓存所有机器人参数
            cacheRobotParams();
        }
        String paramStr = JSON.toJSONString(getParamList()).trim();
        StringBuilder sb = new StringBuilder();
        sb.append("{\"sn\"").append(":\"").append(etSN.getText().toString()).append("\",\"params\"").append(":").append(paramStr).append("}");
        String robotUrl = (String) SharedPreferencesSDUtil.get(context, "urlUtil", SharedKey.ADDROBOTPARAM, UrlUtil.ADDROBOTPARAM);
        HttpUtil.postStrJson(getActivity(), robotUrl, sb.toString(), RobotParams.class, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                RobotParams feed = (RobotParams) responseObj;
                if ("200".equals(feed.getStatus())) {
                    if (isAdd) {//上位机修改，是否需要阿里云重新注册
                        registerALiYUn();
                    } else {
                        CSJToast.showToast(getActivity(), Static.CONTEXT.getResources().getString(R.string.update_params_success), 2000);
                        getActivity().finish();
                    }
                } else {
                    buttonAvailable(true);
                    CSJToast.showToast(getActivity(), feed.getMessage(), 2000);
                }
            }

            @Override
            public void onFail(Object reasonObj) {
                buttonAvailable(true);
            }
        });
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

    private void outWareHouse() {
        getActivity().finish();
//        SharedUtil.setPreferInt(SharedKey.ROBOTREGISTERSTATUS, RobotStatus.ALREADYOUTWARE);
//        btnOK.setText(getString(R.string.alreadly_out_ware));
//        CsjSpeechSynthesizer.getSynthesizer().startSpeaking(Static.CONTEXT.getResources().getString(R.string.snowbot_already_outware), new CsjSynthesizerListener() {
//            @Override
//            public void onSpeakBegin() {
//                SpeechStatus.getIstance().setSpeakFinished(false);
//                while (!(SharedUtil.getPreferInt(SharedKey.ROBOTREGISTERSTATUS, 0) == RobotStatus.ALREADYOUTWARE)) {
//                    SharedUtil.setPreferInt(SharedKey.ROBOTREGISTERSTATUS, RobotStatus.ALREADYOUTWARE);
//                }
//            }
//
//            @Override
//            public void onCompleted(SpeechError speechError) {
//                SpeechStatus.getIstance().setSpeakFinished(true);
//                SharedUtil.setPreferInt(SharedKey.ROBOTREGISTERSTATUS, RobotStatus.ALREADYOUTWARE);
//                if (SharedUtil.getPreferInt(SharedKey.ROBOTREGISTERSTATUS, 0) == RobotStatus.ALREADYOUTWARE) {
//                    CommonTool.rebootDevice();
//                }
//            }
//        });
    }

    /**
     * 验证数据完整性
     */
    private String verifyData() {
        String reminder = null;//提示信息
        if (StrUtil.isBlank(etSN.getText().toString())) {//sn
            reminder = Static.CONTEXT.getResources().getString(R.string.sn_not_null);
        }
        if (StrUtil.isBlank(etUpComputer.getText().toString())) {//上位机
            reminder = Static.CONTEXT.getResources().getString(R.string.up_computer_not_null);
        }
        if (StrUtil.isBlank(etUpPlate.getText().toString())) {//上身板
            reminder = Static.CONTEXT.getResources().getString(R.string.up_plate_not_null);
        }
        if (StrUtil.isBlank(etDownPlate.getText().toString())) {//下身板
            reminder = Static.CONTEXT.getResources().getString(R.string.down_plate_not_null);
        }
        if (StrUtil.isBlank(etNavigation.getText().toString())) {//导航核心板
            reminder = Static.CONTEXT.getResources().getString(R.string.navigation_not_null);
        }
        return reminder;
    }

    /**
     * 获取参数列表
     */
    private List<Map<String, String>> getParamList() {
        List<Map<String, String>> paramList = new ArrayList<>();
        Map<String, String> upPCMap = new HashMap<>();
        upPCMap.put("key", "up_computer");
        upPCMap.put("value", etUpComputer.getText().toString());
        upPCMap.put("memo", getString(R.string.robot_params_up_computer));
        paramList.add(upPCMap);
        Map<String, String> upPlateMap = new HashMap<>();
        upPlateMap.put("key", "up_plate");
        upPlateMap.put("value", etUpPlate.getText().toString());
        upPlateMap.put("memo", getString(R.string.robot_params_up_plate));
        paramList.add(upPlateMap);
        Map<String, String> downPlateMap = new HashMap<>();
        downPlateMap.put("key", "down_plate");
        downPlateMap.put("value", etDownPlate.getText().toString());
        downPlateMap.put("memo", getString(R.string.robot_params_down_plate));
        paramList.add(downPlateMap);
        Map<String, String> navigationMap = new HashMap<>();
        navigationMap.put("key", "navigation");
        navigationMap.put("value", etNavigation.getText().toString());
        navigationMap.put("memo", getString(R.string.robot_params_navigation));
        paramList.add(navigationMap);
        return paramList;
    }

    /**
     * 设置按钮是否可用，不可用变灰
     *
     * @param available
     */
    private void buttonAvailable(boolean available) {
        btnOK.setEnabled(available);
    }

    @Override
    public void onVisible() {
        super.onVisible();
        if (isAdd) {//新增语音提示准备出货
            CsjSpeechSynthesizer.getSynthesizer().startSpeaking(Static.CONTEXT.getResources().getString(R.string.snowbot_outware), new CsjSynthesizerListener() {
                @Override
                public void onSpeakBegin() {
                    SpeechStatus.getIstance().setSpeakFinished(false);
                }

                @Override
                public void onCompleted(SpeechError speechError) {
                    SpeechStatus.getIstance().setSpeakFinished(true);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                readRobotParam();
            }
        }, 1000);

        setParamsKey();
        Bundle bundle = getArguments();
        if (bundle != null) {
            isAdd = bundle.getBoolean("isAdd");
        } else {
            isAdd = true;
        }
//        if (isAdd) {//新增机器人参数
//            btnSeeQrCode.setVisibility(View.VISIBLE);
//        } else {
//            btnSeeQrCode.setVisibility(View.GONE);
//        }
        if (!isAdd) {//读取相关机器人参数信息
            setRobotParams();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
//        CsjSpeechSynthesizer.getSynthesizer().stopSpeaking();
    }

    @Override
    public void onInvisible() {
        super.onInvisible();
//        CsjSpeechSynthesizer.getSynthesizer().stopSpeaking();
    }

    /**
     * 缓存机器人参数到本地
     */
    private void cacheRobotParams() {
        RobotParamsUtil.setSn(etSN.getText().toString());
        RobotParamsUtil.setUpComputorID(etUpComputer.getText().toString());
        RobotParamsUtil.setUpPlateID(etUpPlate.getText().toString());
        RobotParamsUtil.setDownPlateID(etDownPlate.getText().toString());
        RobotParamsUtil.setNavigation(etNavigation.getText().toString());
    }

    /**
     * 设置机器人相关参数信息
     */
    private void setRobotParams() {
        etSN.setText(RobotParamsUtil.getSn());
        etUpComputer.setText(RobotParamsUtil.getUpComputorID());
        etUpPlate.setText(RobotParamsUtil.getUpPlateID());
        etDownPlate.setText(RobotParamsUtil.getDownPlateID());
        etNavigation.setText(RobotParamsUtil.getNavigation());
    }

    /**
     * 设置机器人参数名称
     */
    private void setParamsKey() {
        tvSN.setText(convertString("SN"));
        tvDownPlate.setText(convertString(getString(R.string.robot_params_down_plate)));
        tvNavigation.setText(convertString(getString(R.string.robot_params_navigation)));
        tvUpComputer.setText(convertString(getString(R.string.robot_params_up_computer)));
        tvUpPlate.setText(convertString(getString(R.string.robot_params_up_plate)));
    }

    private Spanned convertString(String strKey) {
        return Html.fromHtml(strKey + "<font color=red>*</font>" + " :");
    }
}
