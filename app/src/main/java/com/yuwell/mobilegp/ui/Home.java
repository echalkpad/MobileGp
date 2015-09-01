package com.yuwell.mobilegp.ui;

import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.yuwell.mobilegp.R;
import com.yuwell.mobilegp.bluetooth.BluetoothConstant;
import com.yuwell.mobilegp.bluetooth.BluetoothLeService;
import com.yuwell.mobilegp.common.event.EventMessage;
import com.yuwell.mobilegp.ui.fragment.BgMeasure;
import com.yuwell.mobilegp.ui.fragment.BpMeasure;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Chen on 2015/8/31.
 */
public class Home extends AppCompatActivity {

    private int currentTab = 0;

    private AppSectionsPagerAdapter mAdapter;
    private ViewPager mViewPager;

    private BluetoothLeService mBluetoothLeService;

    private boolean printerConnected;

    private List<String> toPrintList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        initViews();
        startBleService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从后台重新切换回当前Activity
        if (mBluetoothLeService != null) {
            mBluetoothLeService.connect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mBluetoothLeService != null) {
            mBluetoothLeService.disconnectSecondaryDevice();
            unbindService(mServiceConnection);
            mBluetoothLeService = null;
        }
    }

    /**
     * Tab页点击事件响应
     * @param v
     */
    public void onTabClick(View v) {
        switch (v.getId()) {
            case R.id.btn_bp:
                mViewPager.setCurrentItem(0);
                break;
            case R.id.btn_bg:
                mViewPager.setCurrentItem(1);
                break;
        }
    }

    private void initViews() {
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int i) {
                if (mBluetoothLeService != null) {
                    if (currentTab == 0 && i == 1) {
//                        setConnectionUI(BluetoothProfile.STATE_DISCONNECTED);
                        mBluetoothLeService.resetDeviceType(BluetoothConstant.DEVICE_TYPE_BLOOD_GLUCOSE);
                    }
                    if (currentTab == 1 && i == 0) {
                        mBluetoothLeService.resetDeviceType(BluetoothConstant.DEVICE_TYPE_BLOOD_PRESSURE);
                    }
                }
                currentTab = i;
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        findViewById(R.id.btn_print).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                if (mBluetoothLeService != null) {
//                    mBluetoothLeService.disconnectSecondaryDevice();
//                }

                Message message = new Message();
                message.what = EventMessage.ON_PRINT;
                EventBus.getDefault().post(message);
            }
        });
    }

    private void startBleService() {
        Intent startService = new Intent(this, BluetoothLeService.class);
        startService.putExtra(BluetoothLeService.INTENT_TYPE, BluetoothConstant.DEVICE_TYPE_BLOOD_PRESSURE);
        bindService(startService, mServiceConnection, BIND_AUTO_CREATE);
    }

    /**
     * Service连接回调函数
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            // Activity创建时判断当前蓝牙服务连接状态
            // 若断开则重新扫描，否则直接更新界面
            int state = mBluetoothLeService.getConnectionState();
            if (state == BluetoothProfile.STATE_DISCONNECTED) {
                mBluetoothLeService.scanDevice();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    /**
     * PagerView适配器
     */
    private final class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments;

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments = new ArrayList<>();
            fragments.add(new BpMeasure());
            fragments.add(new BgMeasure());
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public Fragment getItem(int i) {
            return fragments.get(i);
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }
}


