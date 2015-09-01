package com.yuwell.mobilegp.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yuwell.mobilegp.R;
import com.yuwell.mobilegp.bluetooth.BluetoothConstant;
import com.yuwell.mobilegp.common.event.EventListener;
import com.yuwell.mobilegp.common.event.EventMessage;
import com.yuwell.mobilegp.ui.Home;
import com.yuwell.mobilegp.ui.PrinterActivity;

import de.greenrobot.event.EventBus;

/**
 * 血糖测量
 * Created by Chen on 14-11-9.
 */
public class BgMeasure extends Fragment implements EventListener {

    private Home activity;

    private TextView mVal;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (Home) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus mEventBus = EventBus.getDefault();
        mEventBus.register(this);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.bg_measure, container, false);
        mVal = (TextView) mainView.findViewById(R.id.tv_bg_val);
        return mainView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 事件消息接收
     * @param event
     */
    @Override
    public void onEventMainThread(Message event) {
        if (event.what == EventMessage.BLE_DATA &&
                BluetoothConstant.DEVICE_TYPE_BLOOD_GLUCOSE == event.arg1) {
            mVal.setText((String) event.obj);
        }
        if (event.what == EventMessage.ON_PRINT && getUserVisibleHint()) {
            StringBuilder builder = new StringBuilder();
            builder.append("血糖：" + mVal.getText() + "\n");

            Intent intent = new Intent(activity, PrinterActivity.class);
            intent.putExtra("text", builder.toString());
            startActivity(intent);
        }
    }

}
