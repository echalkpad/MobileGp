package com.yuwell.mobilegp.ui;

import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.yuwell.bluetooth.core.BleService;
import com.yuwell.bluetooth.device.BPM;
import com.yuwell.bluetooth.device.Glucometer;
import com.yuwell.mobilegp.R;
import com.yuwell.mobilegp.common.GlobalContext;
import com.yuwell.mobilegp.common.event.EventMessage;
import com.yuwell.mobilegp.common.utils.DateUtil;
import com.yuwell.mobilegp.database.entity.Person;
import com.yuwell.mobilegp.ui.fragment.BgMeasure;
import com.yuwell.mobilegp.ui.fragment.BpMeasure;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Chen on 2015/8/31.
 */
public class Home extends AppCompatActivity {

    public static final String ID = "id";

    private int currentTab = 0;

    private AppSectionsPagerAdapter mAdapter;
    private ViewPager mViewPager;

    private TextView mTabOne;
    private TextView mTabTwo;
    private TextView mName;
    private TextView mGender;
    private TextView mBirthday;
    private TextView mIdNumber;
    private CircleImageView mImage;

    private BleService.LocalBinder mBluetoothLeService;

    private Person person;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        initViews();
        startBleService();
        showInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从后台重新切换回当前Activity
        if (mBluetoothLeService != null) {
            if (BluetoothProfile.STATE_DISCONNECTED == mBluetoothLeService.getConnectionState()) {
                mBluetoothLeService.scanBleDevice();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mBluetoothLeService != null) {
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
            case R.id.tab_1:
                mViewPager.setCurrentItem(0);
                break;
            case R.id.tab_2:
                mViewPager.setCurrentItem(1);
                break;
        }
    }

    public Person getPerson() {
        return person;
    }

    private void initViews() {
        mTabOne = (TextView) findViewById(R.id.tab_1);
        mTabOne.setSelected(true);
        mTabTwo = (TextView) findViewById(R.id.tab_2);

        mName = (TextView) findViewById(R.id.tv_name);
        mGender = (TextView) findViewById(R.id.tv_gender);
        mBirthday = (TextView) findViewById(R.id.tv_birthday);
        mIdNumber = (TextView) findViewById(R.id.tv_number);
        mImage = (CircleImageView) findViewById(R.id.img_user);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int i) {
                setTabCheck(i);
                if (mBluetoothLeService != null) {
                    if (currentTab == 0 && i == 1) {
//                        setConnectionUI(BluetoothProfile.STATE_DISCONNECTED);
                        mBluetoothLeService.setDevice(new Glucometer());
                    }
                    if (currentTab == 1 && i == 0) {
                        mBluetoothLeService.setDevice(new BPM());
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

    private void showInfo() {
        person = GlobalContext.getDatabase().getPersonByIdNumber(getIntent().getStringExtra(ID));
        if (person != null) {
            mName.setText(getString(R.string.name, person.getName()));
            mGender.setText(getString(R.string.gender, person.getGender()));
            mBirthday.setText(getString(R.string.birthday, DateUtil.formatCustomDate(person.getBirthday(), "yyyy年MM月dd日")));
            mIdNumber.setText(getString(R.string.id, person.getIdNumber()));

            if (!TextUtils.isEmpty(person.getImgPath())) {
                Bitmap bitmap = BitmapFactory.decodeFile(person.getImgPath());
                mImage.setImageBitmap(bitmap);
            }
        }
    }

    private void startBleService() {
        Intent startService = new Intent(this, BleService.class);
        bindService(startService, mServiceConnection, BIND_AUTO_CREATE);
    }

    private void setTabCheck(int i) {
        switch (i) {
            case 0:
                mTabOne.setSelected(true);
                mTabTwo.setSelected(false);
                break;
            case 1:
                mTabOne.setSelected(false);
                mTabTwo.setSelected(true);
                break;
        }
    }

    /**
     * Service连接回调函数
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = (BleService.LocalBinder) service;
            mBluetoothLeService.setDevice(new BPM());
            // Activity创建时判断当前蓝牙服务连接状态
            // 若断开则重新扫描，否则直接更新界面
            int state = mBluetoothLeService.getConnectionState();
            if (state == BluetoothProfile.STATE_DISCONNECTED) {
                mBluetoothLeService.scanBleDevice();
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


