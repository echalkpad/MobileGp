package com.yuwell.mobilegp.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
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
 * 患者血压测量
 * Created by Chen on 2015/4/28.
 */
public class BpMeasure extends Fragment implements EventListener {

    private Home activity;

    private TextView mSbp;
    private TextView mDbp;
    private TextView mPulseRate;

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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.bp_measure, container, false);
        mSbp = (TextView) mainView.findViewById(R.id.tv_sbp);
        mDbp = (TextView) mainView.findViewById(R.id.tv_dbp);
        mPulseRate = (TextView) mainView.findViewById(R.id.tv_pulse_rate);
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
    public void onEventMainThread(Message event) {
        if (event.what == EventMessage.BLE_DATA &&
                BluetoothConstant.DEVICE_TYPE_BLOOD_PRESSURE == event.arg1) {
            int[] array = (int[]) event.obj;
            mSbp.setText(String.valueOf(array[0]));
            mDbp.setText(String.valueOf(array[1]));
            mPulseRate.setText(String.valueOf(array[2]));
        }
        if (event.what == EventMessage.ON_PRINT && getUserVisibleHint()) {
            StringBuilder builder = new StringBuilder();
            builder.append("收缩压：" + mSbp.getText() + "\n");
            builder.append("舒张压：" + mDbp.getText() + "\n");
            builder.append("脉  率：" + mPulseRate.getText() + "\n");

            Intent intent = new Intent(activity, PrinterActivity.class);
            intent.putExtra("text", builder.toString());
            startActivity(intent);
        }
    }

}
