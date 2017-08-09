package com.csjbot.snowbot.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;

import com.csjbot.snowbot.Fragment.AdvertisementSDFragment;
import com.csjbot.snowbot.Fragment.AdvertisementUSBFragment;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author jwc
 */
public class NewAdvertisementActivity extends CsjUIActivity {

    @BindView(R.id.viewpager)
    ViewPager viewpager;

    @BindView(R.id.bt_sd)
    Button bt_sd;

    @BindView(R.id.bt_mount)
    Button bt_mount;

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {

        setupBack();

        initialize();


    }

    /**
     * 初始化
     */
    private void initialize(){
        viewpager.setAdapter(new AdvertisemenFragmentPagerAdapter(getSupportFragmentManager()));
        viewpager.addOnPageChangeListener(mOnPageChangeListener);
        bt_sd.setSelected(true);
    }

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            setBtSelected();
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    /**
     * 设置button选中状态
     */
    private void setBtSelected(){
        bt_sd.setSelected((!bt_sd.isSelected()));
        bt_mount.setSelected((!bt_mount.isSelected()));
    }

    @OnClick({R.id.bt_sd, R.id.bt_mount})
    public void onClick(View view){
        int id = view.getId();
        switch (id){
            case R.id.bt_sd:
                viewpager.setCurrentItem(0);
                break;
            case R.id.bt_mount:
                viewpager.setCurrentItem(1);
                break;
        }
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_new_advertisement;
    }

    static class AdvertisemenFragmentPagerAdapter extends FragmentPagerAdapter {

        Fragment[] mFragments = new Fragment[2];

        public AdvertisemenFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragments[0] = new AdvertisementSDFragment();
            mFragments[1] = AdvertisementUSBFragment.newInstance();
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments[position];
        }

        @Override
        public int getCount() {
            return mFragments.length;
        }
    }


}
