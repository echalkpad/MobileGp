package com.yuwell.mobilegp.ui;

import android.os.Bundle;
import android.widget.TextView;

import com.yuwell.mobilegp.R;
import com.yuwell.mobilegp.bluetooth.OnDataRead;

/**
 * Created by Chen on 2015/9/11.
 */
public class ECGActivity extends BTActivity implements OnDataRead {

    private static final byte[] ACK_HELLO = {85, 01, 01, 42 | -128, 10};
    private static final byte[] ACK_TOTAL_LENGTH = {85, 2, 0, 0, 10};

    private TextView mState;

    private int n;
    private int timeN = 0;
    private byte packageN = 0;
    private int packageLen = 30;
    private byte temp1;
    private byte byteLenH = 0;
    private byte byteLenL = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ecg_activity);
        mState = (TextView) findViewById(R.id.tv_state);

        if (getService() != null) {
            getService().setOnDataRead(this);
            getService().setBufferSize(1);
        }
    }

    @Override
    public String getDeviceName() {
        return "ECG:HC-201B";
    }

    @Override
    public boolean doDiscoveryOnCreate() {
        return true;
    }

    @Override
    public void onNothingDiscovered() {
        mState.setText("未发现设备");
    }

    @Override
    public void onDeviceConnected() {
        mState.setText("已连接");
    }

    @Override
    public void onDeviceDisconnected() {
        mState.setText("连接断开");
    }

    @Override
    public void onDeviceConnectionFailed() {
        mState.setText("连接失败");
    }

    @Override
    public void onRead(int len, byte[] data) {
        n++;
        printHexString1(data[0]);
        if (timeN == 3 && n == 9) {
            byteLenL = data[0];
        }
        if (timeN == 3 && n == 10) {
            byteLenH = data[0];
        }

        if (timeN == 3 && n == 2) {
            temp1 = data[0];
        }
        if (timeN == 3 && n == 11) {
            //长度
            System.out.println((Integer.parseInt(printHexString2(byteLenH) + printHexString2(byteLenL), 16) + 10) + "-----------------------");
            packageLen = (Integer.parseInt(printHexString2(byteLenH) + printHexString2(byteLenL), 16) + 10);
        }

        if (len > 0) {
            if (n == 4 && data[0] == 85 && timeN == 0) {
                for (byte b : ACK_HELLO) {
                    getService().write(b);
                }
                n = 0;
                timeN = 1;
            }
            if (data[0] == 2 && timeN == 1) {
                for (byte b : ACK_TOTAL_LENGTH) {
                    getService().write(b);
                }
                timeN = 2;
            }
            if ((data[0] == 3 || data[0] == 4) && timeN == 2) {
                System.out.println("第1个数据包");
                n = 2;
                timeN = 3;
                temp1 = data[0];
                System.out.println("Data-Regular报文的第二个字节-->" + temp1);
                packageN = 0;
            }
            if (n == packageLen && timeN == 3) {
                packageN++;
                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>第" + packageN + "个数据包");
                // ACK-Data包
                byte[] b1 = {85, temp1, packageN, 0, 10};
                for (byte b : b1) {
                    getService().write(b);
                }
                n = 0;
                if (packageN == 30) {
                    timeN = 0;
                    packageN = 0;
                }
            }
        }
    }

    // 将指定byte数组以16进制的形式打印到控制台
    public String printHexString2(byte b) {
        String hex = Integer.toHexString(b & 0xFF);
        if (hex.length() == 1) {
            hex = '0' + hex;
        }
        return hex.toUpperCase();
    }

    public void printHexString1(byte b) {
        String hex = Integer.toHexString(b & 0xFF);
        if (hex.length() == 1) {
            hex = '0' + hex;
        }
        System.out.println(hex.toUpperCase() + "--->下行的参数---" + n);
    }
}
