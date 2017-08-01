package com.csjbot.snowbot.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.services.FloatingWindowsService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class FamilyFunActivity extends CsjUIActivity {

    private LinearLayout content = null;
    private static final String PATH = Environment.getExternalStorageDirectory() + "/csjbot/app_info.json";
    private PackageManager pManager;
    private ArrayList<PakageMod> datas;
    private Intent serviceIntent;
    private String[] showList;

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return (R.layout.activity_family_fun);
    }

    public class PakageMod {
        public String pakageName;
        public String appName;
        public Drawable icon;
        public String activityName;

        public PakageMod() {
            super();
        }

        public PakageMod(String pakageName, String appName, Drawable icon) {
            super();
            this.pakageName = pakageName;
            this.appName = appName;
            this.icon = icon;
        }
    }


    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        setupBack();

        // 文件路径 /data/data/com.csjbot.snowbot/shared_prefs/app_list.xml
        /*
         * <pre>
                <?xml version='1.0' encoding='utf-8' standalone='yes' ?>
                <map>
                    <set name="app_list">
                        <string>啦啦啦啦啦</string>
                        <string>贝瓦早教</string>
                        <string>欢乐斗地主</string>
                        <string>贝瓦儿歌</string>
                    </set>
                </map>
         * </pre>
         */
        SharedPreferences tools = getSharedPreferences("app_list", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = tools.edit();

        // 先取app_list的Set
        Set<String> stringSet = tools.getStringSet("app_list", null);
        // 判断是否为null
        if (stringSet != null) {
            // Set 转list
            @SuppressWarnings({"Unchecked", "unchecked"})
            ArrayList list = new ArrayList(stringSet);
            // list 转 数组
            showList = (String[]) list.toArray(new String[list.size()]);
        } else {
            // new 默认数组
            showList = new String[]{"欢乐斗地主", "星宝乐园", "宝宝早教乐", "ABC", "小学生", "小伙伴", "拼音"};
            // 数组---》list---》Set
            @SuppressWarnings({"Unchecked", "unchecked"})
            Set<String> inSet = new HashSet(Arrays.asList(showList));
            // 保存List
            editor.putStringSet("app_list", inSet);
            editor.apply();
        }


        content = (LinearLayout) findViewById(R.id.family_app_content);
        datas = new ArrayList<>();

        getAllAlowedApps();
        setupConent();
    }

    private void getAllAlowedApps() {
        List<PackageInfo> appList = getAllApps();
        pManager = getPackageManager();

        for (int i = 0; i < appList.size(); i++) {
            PackageInfo pinfo = appList.get(i);
            PakageMod shareItem = new PakageMod();
            // 设置图片
            shareItem.icon = pManager.getApplicationIcon(pinfo.applicationInfo);
            // 设置应用程序名字
            shareItem.appName = pManager.getApplicationLabel(pinfo.applicationInfo).toString();
            // 设置应用程序的包名
            shareItem.pakageName = pinfo.applicationInfo.packageName;

            datas.add(shareItem);
        }
    }


    private List<PackageInfo> getAllApps() {

        List<PackageInfo> apps = new ArrayList<>();
        PackageManager pManager = getPackageManager();
        // 获取手机内所有应用
        List<PackageInfo> packlist = pManager.getInstalledPackages(0);
        for (int i = 0; i < packlist.size(); i++) {
            PackageInfo pak = packlist.get(i);
            // if()里的值如果<=0则为自己装的程序，否则为系统工程自带
            if ((pak.applicationInfo.flags & pak.applicationInfo.FLAG_SYSTEM) <= 0) {
                // 添加自己已经安装的应用程序
                apps.add(pak);
            }
//            apps.add(pak);
        }

        return apps;
    }


    private boolean isInAppList(String appName) {
        for (String show : showList) {
            if (appName.contains(show)) {
                return true;
            }
        }
        return false;
    }

    private void setupConent() {
        LayoutInflater layoutInflater = getLayoutInflater();

        for (final PakageMod app : datas) {
            if (isInAppList(app.appName)) {
                View rootView = layoutInflater.inflate(R.layout.item_app_show, null);

                ImageView icon = (ImageView) rootView.findViewById(R.id.appIcon);
                TextView name = (TextView) rootView.findViewById(R.id.appName);
                RelativeLayout root = (RelativeLayout) rootView.findViewById(R.id.appRoot);

                icon.setImageDrawable(app.icon);
                if (!app.appName.contains("贝瓦早教")) {
                    name.setText(app.appName);
                }

                root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = pManager.getLaunchIntentForPackage(app.pakageName);
                        startActivity(i);

                        if (serviceIntent == null) {
                            serviceIntent = new Intent(FamilyFunActivity.this, FloatingWindowsService.class);
                        }

                        startService(serviceIntent);
                    }
                });
                content.addView(rootView);
            }
        }
    }


    @Override
    protected void onResume() {
//        if (serviceIntent != null) {
//            stopService(serviceIntent);
//        }
        super.onResume();
    }
}
