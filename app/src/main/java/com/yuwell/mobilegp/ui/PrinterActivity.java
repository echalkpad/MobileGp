package com.yuwell.mobilegp.ui;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.yuwell.mobilegp.R;

import java.io.UnsupportedEncodingException;

/**
 * Created by Chen on 2015/8/11.
 */
public class PrinterActivity extends BTActivity {

    private static final String DEVICE_NAME = "BlueTooth Printer";

//    private EditText mEditText;
//    private Button mPrint;

    private TextView mState;
    private String printText;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_print);
        mState = (TextView) findViewById(R.id.tv_state);
        printText = getIntent().getStringExtra("text");
    }

    @Override
    public String getDeviceName() {
        return DEVICE_NAME;
    }

    @Override
    public boolean doDiscoveryOnCreate() {
        return true;
    }

    public synchronized void print(String message) {
        if (message.length() > 0) {
            byte[] send;
            try {
                send = message.getBytes("GBK");
            } catch (UnsupportedEncodingException var5) {
                send = message.getBytes();
            }

            write(send);
            byte[] tail = new byte[]{(byte)10, (byte)13, (byte)0};
            write(tail);
        }
    }

    @Override
    public void onDeviceConnected() {
        mState.setText(R.string.printing);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                print(printText);

                if (getService() != null) {
                    getService().stop();
                }
                finish();
            }
        }, 600);
    }

    @Override
    public void onDeviceDisconnected() {
        mState.setText(R.string.not_connected);
    }

    @Override
    public void onDeviceConnectionFailed() {
        if (!isFinishing()) {
            mState.setText(R.string.connect_failed);
        }
    }
}
