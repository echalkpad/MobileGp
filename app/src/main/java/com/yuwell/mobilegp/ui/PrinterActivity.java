package com.yuwell.mobilegp.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.yuwell.mobilegp.R;

import java.io.UnsupportedEncodingException;

/**
 * Created by Chen on 2015/8/11.
 */
public class PrinterActivity extends BTActivity {

    private static final String DEVICE_NAME = "BlueTooth Printer";

    private EditText mEditText;
    private Button mPrint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.printer_activity);

        mEditText = (EditText) findViewById(R.id.txt_content);
        mPrint = (Button) findViewById(R.id.btn_print);
        mPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mEditText.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    print(text);
                    mEditText.setText(null);
                }
            }
        });
    }

    @Override
    public String getDeviceName() {
        return DEVICE_NAME;
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
        mPrint.setEnabled(true);
    }

    @Override
    public void onDeviceDisconnected() {
        mPrint.setEnabled(false);
    }

    @Override
    public void onDeviceConnectionFailed() {
        if (!isFinishing()) {
            Toast.makeText(this, "连接失败", Toast.LENGTH_SHORT).show();
        }
    }
}
