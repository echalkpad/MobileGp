package com.yuwell.mobilegp.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivsign.android.IDCReader.IDCReaderSDK;
import com.yuwell.mobilegp.R;
import com.yuwell.mobilegp.bluetooth.OnDataRead;
import com.yuwell.mobilegp.common.utils.FileManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chen on 2015/8/11.
 */
public class IDCardActivity extends BTActivity implements OnDataRead {

    private static final String TAG = IDCardActivity.class.getSimpleName();

    private static final byte[] CMD_FIND = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x20, 0x01, 0x22};
    private static final byte[] CMD_SELECT = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x20, 0x02, 0x21};
    private static final byte[] CMD_READ = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x30, 0x01, 0x32};

    private List<Byte> byteArray = new ArrayList<>();

    private Button mRead;
    private TextView mName;
    private TextView mGender;
    private TextView mNation;
    private TextView mBirthday;
    private TextView mAddress;
    private TextView mNumber;
    private TextView mIssuingAuthority;
    private TextView mValidPeriod;
    private ImageView image;

    private int readFlag = -99;
    private String[] decodeInfo = new String[10];

    private int state = 0;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.id_card_activity);

        mRead = (Button) findViewById(R.id.btn_read);
        mRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write(CMD_FIND);
                state = 0;
            }
        });

        mName = (TextView) findViewById(R.id.tv_name);
        mGender = (TextView) findViewById(R.id.tv_gender);
        mNation = (TextView) findViewById(R.id.tv_nation);
        mBirthday = (TextView) findViewById(R.id.tv_birthday);
        mAddress = (TextView) findViewById(R.id.tv_address);
        mNumber = (TextView) findViewById(R.id.tv_number);
        mIssuingAuthority = (TextView) findViewById(R.id.tv_licence_issuing_authority);
        mValidPeriod = (TextView) findViewById(R.id.tv_valid_period);
        image = (ImageView) findViewById(R.id.imageView1);

        if (getService() != null) {
            getService().setOnDataRead(this);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                FileManager.copyAssets(IDCardActivity.this);
            }
        }).start();
    }

    @Override
    public String getDeviceName() {
        return "CVR-100B";
    }

    @Override
    public void onDeviceConnected() {
        mRead.setEnabled(true);
    }

    @Override
    public void onDeviceDisconnected() {
        mRead.setEnabled(false);
    }

    @Override
    public void onDeviceConnectionFailed() {
        if (!isFinishing()) {
            Toast.makeText(this, "连接失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRead(int dataSize, byte[] data) {
        switch (state) {
            case 0:
                if (data[9] == -97) {
                    state++;
                    write(CMD_SELECT);
                } else {
                    readFlag = -3;//寻卡失败
                }
                break;
            case 1:
                if (data[9] == -112) {
                    state++;
                    write(CMD_READ);
                    byteArray = new ArrayList<>();
                } else {
                    readFlag = -4;//选卡失败
                }
                break;
            case 2:
                if (byteArray.size() < 1294) {
                    for (int i = 0; i < dataSize; i++) {
                        byteArray.add(data[i]);
                    }

                    if (byteArray.size() == 1295) {
                        Log.d(TAG, "OK");
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startDecoding();
                            }
                        }, 500);
                    }
                }
                break;
        }
    }

    private void startDecoding() {
        if (byteArray.get(9) == -112) {
            byte[] dataBuf = new byte[256];

            for (int i = 0; i < 256; i++) {
                dataBuf[i] = byteArray.get(14 + i);
            }
            String TmpStr = null;
            try {
                TmpStr = new String(dataBuf, "UTF16-LE");
                TmpStr = new String(TmpStr.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {

            }

            decodeInfo[0] = TmpStr.substring(0, 15);
            decodeInfo[1] = TmpStr.substring(15, 16);
            decodeInfo[2] = TmpStr.substring(16, 18);
            decodeInfo[3] = TmpStr.substring(18, 26);
            decodeInfo[4] = TmpStr.substring(26, 61);
            decodeInfo[5] = TmpStr.substring(61, 79);
            decodeInfo[6] = TmpStr.substring(79, 94);
            decodeInfo[7] = TmpStr.substring(94, 102);
            decodeInfo[8] = TmpStr.substring(102, 110);
            decodeInfo[9] = TmpStr.substring(110, 128);
            if (decodeInfo[1].equals("1"))
                decodeInfo[1] = "男";
            else
                decodeInfo[1] = "女";
            try {
                int code = Integer.parseInt(decodeInfo[2].toString());
                decodeInfo[2] = decodeNation(code);
            } catch (Exception e) {
                decodeInfo[2] = "";
            }

            //照片解码
            try {
                int ret = IDCReaderSDK.Init();
                if (ret == 0) {
                    byte[] datawlt = new byte[1384];
                    byte[] byLicData = {(byte) 0x05, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x5B, (byte) 0x03, (byte) 0x33, (byte) 0x01, (byte) 0x5A, (byte) 0xB3, (byte) 0x1E, (byte) 0x00};
                    for (int i = 0; i < 1295; i++) {
                        datawlt[i] = byteArray.get(i);
                    }
                    int t = IDCReaderSDK.unpack(datawlt, byLicData);
                    if (t == 1) {
                        readFlag = 1;//读卡成功
                    } else {
                        readFlag = 6;//照片解码异常
                    }
                } else {
                    readFlag = 6;//照片解码异常
                }
            } catch (Exception e) {
                readFlag = 6;//照片解码异常
            }
        } else {
            readFlag = -5;//读卡失败！
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showInfo();
            }
        });
    }

    private void showInfo() {
        try {
            if (readFlag > 0) {
                mName.setText(decodeInfo[0].trim());
                mGender.setText(decodeInfo[1].trim());
                mNation.setText(decodeInfo[2].trim());
                mBirthday.setText(decodeInfo[3].trim());
                mAddress.setText(decodeInfo[4].trim());
                mNumber.setText(decodeInfo[5].trim());
                mIssuingAuthority.setText(decodeInfo[6].trim());
                mValidPeriod.setText(decodeInfo[7] + "-" + decodeInfo[8]);

                if (readFlag == 1) {
                    FileInputStream fis = new FileInputStream(Environment.getExternalStorageDirectory() + "/wltlib/zp.bmp");
                    Bitmap bmp = BitmapFactory.decodeStream(fis);
                    fis.close();
                    image.setImageBitmap(bmp);
                } else {
                    mName.append("照片解码失败，请检查路径" + Environment.getExternalStorageDirectory() + "/wltlib/");
                    image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.face));
                }
            } else {
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.face));
                if (readFlag == -2) {
                    mName.setText("蓝牙连接异常");
                }
                if (readFlag == -3) {
                    mName.setText("无卡或卡片已读过");
                }
                if (readFlag == -4) {
                    mName.setText("无卡或卡片已读过");
                }
                if (readFlag == -5) {
                    mName.setText("读卡失败");
                }
                if (readFlag == -99) {
                    mName.setText("操作异常");
                }
            }
        } catch (IOException e) {
            mName.setText("读取数据异常！");
            image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.face));
        }
    }

    private String decodeNation(int code) {
        String nation;
        switch (code) {
            case 1:
                nation = "汉";
                break;
            case 2:
                nation = "蒙古";
                break;
            case 3:
                nation = "回";
                break;
            case 4:
                nation = "藏";
                break;
            case 5:
                nation = "维吾尔";
                break;
            case 6:
                nation = "苗";
                break;
            case 7:
                nation = "彝";
                break;
            case 8:
                nation = "壮";
                break;
            case 9:
                nation = "布依";
                break;
            case 10:
                nation = "朝鲜";
                break;
            case 11:
                nation = "满";
                break;
            case 12:
                nation = "侗";
                break;
            case 13:
                nation = "瑶";
                break;
            case 14:
                nation = "白";
                break;
            case 15:
                nation = "土家";
                break;
            case 16:
                nation = "哈尼";
                break;
            case 17:
                nation = "哈萨克";
                break;
            case 18:
                nation = "傣";
                break;
            case 19:
                nation = "黎";
                break;
            case 20:
                nation = "傈僳";
                break;
            case 21:
                nation = "佤";
                break;
            case 22:
                nation = "畲";
                break;
            case 23:
                nation = "高山";
                break;
            case 24:
                nation = "拉祜";
                break;
            case 25:
                nation = "水";
                break;
            case 26:
                nation = "东乡";
                break;
            case 27:
                nation = "纳西";
                break;
            case 28:
                nation = "景颇";
                break;
            case 29:
                nation = "柯尔克孜";
                break;
            case 30:
                nation = "土";
                break;
            case 31:
                nation = "达斡尔";
                break;
            case 32:
                nation = "仫佬";
                break;
            case 33:
                nation = "羌";
                break;
            case 34:
                nation = "布朗";
                break;
            case 35:
                nation = "撒拉";
                break;
            case 36:
                nation = "毛南";
                break;
            case 37:
                nation = "仡佬";
                break;
            case 38:
                nation = "锡伯";
                break;
            case 39:
                nation = "阿昌";
                break;
            case 40:
                nation = "普米";
                break;
            case 41:
                nation = "塔吉克";
                break;
            case 42:
                nation = "怒";
                break;
            case 43:
                nation = "乌孜别克";
                break;
            case 44:
                nation = "俄罗斯";
                break;
            case 45:
                nation = "鄂温克";
                break;
            case 46:
                nation = "德昂";
                break;
            case 47:
                nation = "保安";
                break;
            case 48:
                nation = "裕固";
                break;
            case 49:
                nation = "京";
                break;
            case 50:
                nation = "塔塔尔";
                break;
            case 51:
                nation = "独龙";
                break;
            case 52:
                nation = "鄂伦春";
                break;
            case 53:
                nation = "赫哲";
                break;
            case 54:
                nation = "门巴";
                break;
            case 55:
                nation = "珞巴";
                break;
            case 56:
                nation = "基诺";
                break;
            case 97:
                nation = "其他";
                break;
            case 98:
                nation = "外国血统中国籍人士";
                break;
            default:
                nation = "";
                break;
        }
        return nation;
    }
}
