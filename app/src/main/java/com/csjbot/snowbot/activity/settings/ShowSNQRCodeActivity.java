package com.csjbot.snowbot.activity.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.csjbot.snowbot.Fragment.SNQRFregment;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowSNQRCodeActivity extends CsjUIActivity {

    public class FragAdapter extends FragmentPagerAdapter {

        private List<Fragment> mFragments;

        public FragAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            mFragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment page = null;
            if (mFragments.size() > position) {
                page = mFragments.get(position);
                if (page != null) {
                    return page;
                }
            }
            return page;
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

    }

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        List<Fragment> fragments = new ArrayList<>();
        setupBack();

        HashMap<String, String> deviceInfos = (HashMap<String, String>) getIntent().getSerializableExtra("deviceInfos");

        for (Map.Entry<String, String> entry : deviceInfos.entrySet()) {
            SNQRFregment snqrFregment = new SNQRFregment();
            snqrFregment.setNameAndNumber(entry.getKey(), entry.getValue());
            fragments.add(snqrFregment);
        }

        FragAdapter adapter = new FragAdapter(getSupportFragmentManager(), fragments);

        //设定适配器
        ViewPager vp = (ViewPager) findViewById(R.id.qr_code_viewPager);
        vp.setAdapter(adapter);
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_show_snqrcode;
    }
}
