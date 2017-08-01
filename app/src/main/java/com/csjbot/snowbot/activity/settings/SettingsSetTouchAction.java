package com.csjbot.snowbot.activity.settings;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.android.core.util.SharedUtil;
import com.baidu.robot.thirdparty.google.Gson;
import com.bigkoo.pickerview.OptionsPickerView;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.bean.AreaBean;
import com.csjbot.snowbot.utils.GetJsonDataUtil;
import com.csjbot.snowbot.utils.LocationUtil.LocationUtil;
import com.csjbot.snowbot.utils.OkHttp.LoadingDialog;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot_rogue.utils.SharePreferenceTools;
import com.hanks.library.AnimateCheckBox;

import org.json.JSONArray;

import java.util.ArrayList;


public class SettingsSetTouchAction extends CsjUIActivity {
    private AnimateCheckBox firstCheckbox, secondCheckbox;
    private SharePreferenceTools sharePreferenceTools;

    private ArrayList<AreaBean> options1Items = new ArrayList<>();
    private ArrayList<ArrayList<String>> options2Items = new ArrayList<>();
    private ArrayList<ArrayList<ArrayList<String>>> options3Items = new ArrayList<>();
    private TextView tv_pick_area;

    private boolean hasData = false;
    private LoadingDialog dialog;


    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        setupBack();
        secondCheckbox = (AnimateCheckBox) findViewById(R.id.secondCheckbox);
        if (SharedUtil.getPreferBool(SharedKey.WEATHERSWITCH, false)) {
            secondCheckbox.setChecked(SharedUtil.getPreferBool(SharedKey.WEATHERSWITCH, false));
        }
        sharePreferenceTools = new SharePreferenceTools(this);
        secondCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> SharedUtil.setPreferBool(SharedKey.WEATHERSWITCH, isChecked));

        tv_pick_area = (TextView) findViewById(R.id.tv_pick_area);
        tv_pick_area.setText(LocationUtil.getCustomLocationComplete()+"（目前仅支持中国境内）");
        tv_pick_area.setOnClickListener(v -> {
            if (!hasData) {
                dialog = LoadingDialog.getInstance();
                dialog.show(getSupportFragmentManager(), "数据加载中");
                new Thread(() -> {
                    /**
                     * 注意：assets 目录下的Json文件仅供参考，实际使用可自行替换文件
                     * 关键逻辑在于循环体
                     *
                     * */
                    String JsonData = new GetJsonDataUtil().getJson(SettingsSetTouchAction.this, "province.json");//获取assets目录下的json文件数据

                    ArrayList<AreaBean> jsonBean = parseData(JsonData);//用Gson 转成实体

                    /**
                     * 添加省份数据
                     *
                     * 注意：如果是添加的JavaBean实体，则实体类需要实现 IPickerViewData 接口，
                     * PickerView会通过getPickerViewText方法获取字符串显示出来。
                     */
                    options1Items = jsonBean;

                    for (int i = 0; i < jsonBean.size(); i++) {//遍历省份
                        ArrayList<String> CityList = new ArrayList<>();//该省的城市列表（第二级）
                        ArrayList<ArrayList<String>> Province_AreaList = new ArrayList<>();//该省的所有地区列表（第三级）

                        for (int c = 0; c < jsonBean.get(i).getCityList().size(); c++) {//遍历该省份的所有城市
                            String CityName = jsonBean.get(i).getCityList().get(c).getName();
                            if (!CityName.equals("其他")) {
                                CityList.add(CityName);//添加城市
                            }

                            ArrayList<String> City_AreaList = new ArrayList<>();//该城市的所有地区列表

                            //如果无地区数据，建议添加空字符串，防止数据为null 导致三个选项长度不匹配造成崩溃
                            if (jsonBean.get(i).getCityList().get(c).getArea() == null
                                    || jsonBean.get(i).getCityList().get(c).getArea().size() == 0) {
                                City_AreaList.add("");
                            } else {

                                for (int d = 0; d < jsonBean.get(i).getCityList().get(c).getArea().size(); d++) {//该城市对应地区所有数据
                                    String AreaName = jsonBean.get(i).getCityList().get(c).getArea().get(d);
                                    if (!AreaName.equals("其他")) {
                                        City_AreaList.add(AreaName);//添加该城市所有地区数据
                                    }
                                }
                            }
                            Province_AreaList.add(City_AreaList);//添加该省所有地区数据
                        }

                        /**
                         * 添加城市数据
                         */
                        options2Items.add(CityList);

                        /**
                         * 添加地区数据
                         */
                        options3Items.add(Province_AreaList);
                    }
                    handler.sendEmptyMessage(1);
                    hasData = true;
                }).start();
            } else {
                handler.sendEmptyMessage(1);
            }
        });

    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_settings_set_touch_action;
    }

    public ArrayList<AreaBean> parseData(String result) {//Gson 解析
        ArrayList<AreaBean> detail = new ArrayList<>();
        try {
            JSONArray data = new JSONArray(result);
            Gson gson = new Gson();
            for (int i = 0; i < data.length(); i++) {
                AreaBean entity = gson.fromJson(data.optJSONObject(i).toString(), AreaBean.class);
                detail.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return detail;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (dialog != null) {
                dialog.dismiss();
            }
            OptionsPickerView pvOptions = new OptionsPickerView.Builder(SettingsSetTouchAction.this, (options1, options2, options3, v) -> {
                //返回的分别是三个级别的选中位置
                String tx = options1Items.get(options1).getPickerViewText() +
                        options2Items.get(options1).get(options2) +
                        options3Items.get(options1).get(options2).get(options3);
                tv_pick_area.setText(tx+"（目前仅支持中国境内）");
                LocationUtil.setCustomLocationComplete(tx);
                LocationUtil.setCustomLocationCity(options3Items.get(options1).get(options2).get(options3));

            })

                    .setTitleText("城市选择")
                    .setDividerColor(Color.BLACK)
                    .setTextColorCenter(Color.BLACK) //设置选中项文字颜色
                    .setContentTextSize(35)
                    .setOutSideCancelable(false)// default is true
                    .build();
            pvOptions.setPicker(options1Items, options2Items, options3Items);//三级选择器
            pvOptions.show();
//            Toast.makeText(SettingsSetTouchAction.this, "show", Toast.LENGTH_SHORT).show();
        }
    };

}