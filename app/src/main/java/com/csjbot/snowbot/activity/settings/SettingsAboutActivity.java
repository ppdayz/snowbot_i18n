package com.csjbot.snowbot.activity.settings;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.core.entry.Static;
import com.android.core.util.SharedUtil;
import com.android.core.util.StrUtil;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.LauncherActivity;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.utils.CommonViewHolder;
import com.csjbot.snowbot.utils.DialogUtil;
import com.csjbot.snowbot.utils.FileUtil;
import com.csjbot.snowbot.utils.SharedKey;
import com.csjbot.snowbot.utils.UUIDGenerator;
import com.csjbot.snowbot_rogue.platform.SnowBotManager;
import com.csjbot.snowbot_rogue.utils.CSJToast;
import com.csjbot.snowbot_rogue.utils.Constant;
import com.pgyersdk.javabean.AppBean;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;

import net.steamcrafted.loadtoast.LoadToast;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import dou.utils.CpuUtil;

/**
 * Created by Administrator on 2016/9/8 0008.
 */
public class SettingsAboutActivity extends CsjUIActivity {

    /**
     * 点击次数
     */
    int mClickCount;
    private String[] items;
    private int mI;

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        setupBack();

        initAboutList();


        mLoadTost = new LoadToast(this);

        adapter = new AboutListViewAdapter();
        ListView aboutListView = (ListView) findViewById(R.id.asb_listView);
        aboutListView.setAdapter(adapter);
        aboutListView.setOnItemClickListener((parent, view, position, id) -> {
            String trmpStr = aboutBeanArrayList.get(position).getTitle();
            if (trmpStr.equals(Static.CONTEXT.getString(R.string.check_for_updates))) {
                mLoadTost.setText("检查中...");
                mLoadTost.show();
                initUpdate();
//                    checkVersionUpdate();
                //TODO 连续点击系统版本，可跳出清除数据
            } else if (trmpStr.equals(Static.CONTEXT.getString(R.string.system_version))) {
                onDisplayClearData();
            } else if (trmpStr.equals(Static.CONTEXT.getString(R.string.clear_data))) {
                clearDataDialog();
            } else if (trmpStr.equals(Static.CONTEXT.getString(R.string.custom_file_copy))) {
                copyFile();
            } else if (trmpStr.equals(Static.CONTEXT.getString(R.string.language))) {
                //选择语言
                showLanguagedialog();
            } else if (trmpStr.equals(Static.CONTEXT.getString(R.string.software_version))) {
                mClickCount++;
                if (mClickCount == 6) {
                    // 进入wifi自动配置管理页面
//                    Intent intent = new Intent(this, MaterialLockActivity.class);
//                    intent.putExtra("nextActivityName", WifiConfigAutomaticActivity.class.getName());
//                    startActivity(intent);
                    startActivity(new Intent(SettingsAboutActivity.this, WifiConfigAutomaticActivity.class));
                    mClickCount = 0;
                }
            } else {
                AboutBean bean = adapter.getItem(position);
                Class<?> cls = bean.nextActivity;
                if (cls != null) {
                    startActivity(new Intent(SettingsAboutActivity.this, cls));
                }
            }
        });
    }

    private void initUpdate() {
        PgyUpdateManager.register(this, new UpdateManagerListener() {
                    @Override
                    public void onNoUpdateAvailable() {
                        mLoadTost.setText("已是最新版本");
                        mLoadTost.show();
                        mLoadTost.success();
                    }

                    @Override
                    public void onUpdateAvailable(String s) {
                        mLoadTost.success();
                        // 将新版本信息封装到AppBean中
                        final AppBean appBean = getAppBeanFromString(s);
                        new AlertDialog.Builder(SettingsAboutActivity.this)
                                .setTitle(getString(R.string.update_title))
                                .setMessage(StrUtil.isBlank(appBean.getReleaseNote()) ? getString(R.string.update_message) : appBean.getReleaseNote())
                                .setCancelable(false)
                                .setNegativeButton(
                                        getString(R.string.update_sure),
                                        (dialog, which) -> startDownloadTask(
                                                SettingsAboutActivity.this,
                                                appBean.getDownloadURL())).show();
                    }
                }
        );
    }


    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_settings_about;
    }

    private class AboutBean {
        public String title;
        public String content;
        Class nextActivity;

        AboutBean(String title, String content, Class nextActivity) {
            this.title = title;
            this.content = content;
            this.nextActivity = nextActivity;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Class getNextActivity() {
            return nextActivity;
        }

        public void setNextActivity(Class nextActivity) {
            this.nextActivity = nextActivity;
        }
    }

    private ArrayList<AboutBean> aboutBeanArrayList = new ArrayList<>();
    private AboutListViewAdapter adapter;
    private AboutBean clearData = new AboutBean(Static.CONTEXT.getResources().getString(R.string.clear_data), "", null);
    private LoadToast mLoadTost = null;
    private Handler mHandler = new Handler();

    private long[] mHints = new long[5];

    public void onDisplayClearData() {
        System.arraycopy(mHints, 1, mHints, 0, mHints.length - 1);//把从第二位至最后一位之间的数字复制到第一位至倒数第一位
        mHints[mHints.length - 1] = SystemClock.uptimeMillis();//从开机到现在的时间毫秒数
        if (SystemClock.uptimeMillis() - mHints[0] <= 1000) {//连续点击之间间隔小于一秒，有效
            if (!aboutBeanArrayList.contains(clearData)) {
                aboutBeanArrayList.add(clearData);
                aboutBeanArrayList.add(new AboutBean("硬件测试", "", HardwareTest.class));

                adapter.notifyDataSetChanged();
            }
        }
    }

    // 得到本机Mac地址
    public void getLocalMac() {
        // 获取wifi管理器
        // TODO 判断wifi是否打开
        for (AboutBean aboutBean : aboutBeanArrayList) {
            if (aboutBean.getTitle().equalsIgnoreCase(getResources().getString(R.string.mac_address))) {
                aboutBean.setContent(CpuUtil.getMacAddress());
                break;
            }
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }


    private void initAboutList() {
        aboutBeanArrayList.add(new AboutBean(getResources().getString(R.string.snowbot_name_text), null == SharedUtil.getPreferStr(SharedKey.ROBOTNICKNAME) ? "小雪" : SharedUtil.getPreferStr(SharedKey.ROBOTNICKNAME), null));
        PackageManager pm = this.getPackageManager();

        try {
            PackageInfo pi = pm.getPackageInfo(this.getPackageName(), 0);
            aboutBeanArrayList.add(new AboutBean(getResources().getString(R.string.software_version), pi.versionName, null));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        aboutBeanArrayList.add(new AboutBean(getResources().getString(R.string.system_version), "1.2.0-980", null));
        aboutBeanArrayList.add(new AboutBean(getResources().getString(R.string.hardware_version), SnowBotManager.getInstance().getUpBodySN(), null));
        aboutBeanArrayList.add(new AboutBean(getResources().getString(R.string.serial_number), UUIDGenerator.getInstance().getDeviceUUID(), null));
//        aboutBeanArrayList.add(new AboutBean(getResources().getString(R.string.snow_face_color), "", SettingsChangeFaceActivity.class));
        aboutBeanArrayList.add(new AboutBean(getResources().getString(R.string.mac_address), "未获取", null));
//        aboutBeanArrayList.add(new AboutBean(getResources().getString(R.string.audio_module), "", AIUIWifiActivity.class));
        aboutBeanArrayList.add(new AboutBean(getResources().getString(R.string.learn_the_ropes), "", SettingsSetTouchAction.class));
//        aboutBeanArrayList.add(new AboutBean("网络测试", "", null));
//        aboutBeanArrayList.add(new AboutBean("音频测试", "", null));
//        aboutBeanArrayList.add(new AboutBean("硬件测试", "", SettingsHWTestActivity.class));
        aboutBeanArrayList.add(new AboutBean(getResources().getString(R.string.custom_file_copy), "", null));
        aboutBeanArrayList.add(new AboutBean(getResources().getString(R.string.check_for_updates), "", null));
        aboutBeanArrayList.add(new AboutBean(getResources().getString(R.string.modify_sn_params), "", UpdateSNParamsActivity.class));
        aboutBeanArrayList.add(new AboutBean(getResources().getString(R.string.language), "", null));
        getLocalMac();
    }


    private void showLanguagedialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(Static.CONTEXT.getString(R.string.language));
        items = new String[]{getString(R.string.simple_cn), "English"};
        mI = SharedUtil.getPreferInt(this, SharedKey.LANGUAGE, 0);
        builder.setSingleChoiceItems(items, mI, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedUtil.setPreferInt(SettingsAboutActivity.this, SharedKey.LANGUAGE, which);
                settingLanguage(which);
                refresh();
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void refresh() {
        Intent intent = new Intent();
        intent.setClass(SettingsAboutActivity.this, LauncherActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void settingLanguage(int which) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        Configuration config = context.getResources().getConfiguration();
        switch (which) {
            case 0:
                config.locale = Locale.CHINA;
                break;
            case 1:
                config.locale = Locale.UK;
                break;
        }
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }


    private class AboutListViewAdapter extends BaseAdapter {
        private LayoutInflater layoutInflater = LayoutInflater.from(SettingsAboutActivity.this);

        /**
         * How many items are in the data set represented by this Adapter.
         *
         * @return Count of items.
         */
        @Override
        public int getCount() {
            return aboutBeanArrayList.size();
        }

        /**
         * Get the data item associated with the specified position in the data set.
         *
         * @param position Position of the item whose data we want within the adapter's
         *                 data set.
         * @return The data at the specified position.
         */
        @Override
        public AboutBean getItem(int position) {
            return aboutBeanArrayList.get(position);
        }

        /**
         * Get the row id associated with the specified position in the list.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        @Override
        public long getItemId(int position) {
            return 0;
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
                convertView = layoutInflater.inflate(R.layout.item_settings_about, parent, false);
            }

            AboutBean aboutBean = getItem(position);
            TextView isa_tvTitle = CommonViewHolder.get(convertView, R.id.isa_tvTitle);
            TextView isa_tvContent = CommonViewHolder.get(convertView, R.id.isa_tvContent);
            ImageView isa_ivRight = CommonViewHolder.get(convertView, R.id.isa_ivRight);


            isa_tvTitle.setText(aboutBean.title);
            isa_tvContent.setText(aboutBean.content);
            isa_ivRight.setVisibility(aboutBean.nextActivity == null ? View.GONE : View.VISIBLE);
//            convertView.setClickable(aboutBean.nextActivity == null ? false : true);

            return convertView;
        }
    }

    public static final String FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/csjbot/data/";

    private void clearDataDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.pup_window_addhome, null);
        EditText editText = (EditText) view.findViewById(R.id.room_name_ed);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setTitle(getString(R.string.dialog_hit));
        builder.setMessage(Static.CONTEXT.getString(R.string.clear_data_hint));
        builder.setPositiveButton(getString(R.string.make_sure), (dialog, which) -> {
            DialogUtil.canCloseDialog(dialog, false);
            String roomName = editText.getText().toString();
            if (StrUtil.isNotBlank(roomName) && roomName.equals("csjbot123")) {
//                    DataCleanManager.cleanApplicationData(SettingsAboutActivity.this);
//                    CommonTool.rebootDevice();
                if (FileUtil.delFile(new File(FILE_PATH))) {
                    CSJToast.showToast(SettingsAboutActivity.this, Static.CONTEXT.getString(R.string.delete_succeeded));
                } else {
                    CSJToast.showToast(SettingsAboutActivity.this, Static.CONTEXT.getString(R.string.delete_failed));
                }
                DialogUtil.canCloseDialog(dialog, true);
            } else {
                CSJToast.showToast(SettingsAboutActivity.this, Static.CONTEXT.getString(R.string.psd_error));
            }
        });
        builder.setNegativeButton(getString(R.string.cancle), (dialog, which) -> DialogUtil.canCloseDialog(dialog, true));
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void copyFile() {
        if (StrUtil.isNotBlank(SharedUtil.getPreferStr(SharedKey.USBPATH)) &&
                FileUtil.copy(SharedUtil.getPreferStr(SharedKey.USBPATH) + "/csjbot/", Constant.CUSTOM_FILEPATH, Constant.CUSTOM_FILENAME) == 0) {
            CSJToast.showToast(SettingsAboutActivity.this, Static.CONTEXT.getString(R.string.copy_success));
            mHandler.postDelayed(() -> FileUtil.readText(Constant.CUSTOM_FILEPATH, Constant.CUSTOM_FILEPATH + Constant.CUSTOM_FILENAME), 1000);
        } else if (StrUtil.isBlank(SharedUtil.getPreferStr(SharedKey.USBPATH))) {
            CSJToast.showToast(SettingsAboutActivity.this, Static.CONTEXT.getString(R.string.insert_U));
        } else if (FileUtil.copy(SharedUtil.getPreferStr(SharedKey.USBPATH) + "/csjbot/", Constant.CUSTOM_FILEPATH, Constant.CUSTOM_FILENAME) == -1) {
            CSJToast.showToast(SettingsAboutActivity.this, Static.CONTEXT.getString(R.string.check_file_correct));
        } else {
            CSJToast.showToast(SettingsAboutActivity.this, Static.CONTEXT.getString(R.string.copy_failed));
        }
    }
}
