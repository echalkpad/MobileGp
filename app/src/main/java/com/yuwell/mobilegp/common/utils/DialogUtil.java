package com.yuwell.mobilegp.common.utils;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * 对话框通用方法
 * Created by Chen on 2014/11/13.
 */
public class DialogUtil {

    private static final Calendar mCalendar = Calendar.getInstance();

    private OnDateSetListener mOnDateSetListener;
    private View.OnClickListener mOnClickListener;

    private Context mContext;
    private LayoutInflater mLayoutInflater;

    public DialogUtil(Context mContext) {
        this(mContext, null);
    }

    public DialogUtil(Context mContext, OnDateSetListener mOnDateSetListener) {
        this.mContext = mContext;
        this.mOnDateSetListener = mOnDateSetListener;
        this.mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * 显示日期选择对话框
     * @param isStart 是否为开始日期对话框
     */
    public void showDateDialog(final boolean isStart, final Date initDate) {
        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, monthOfYear);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                if (mOnDateSetListener != null) {
                    mOnDateSetListener.onDateSet(mCalendar.getTime(), isStart);
                }
            }
        };
        mCalendar.setTime(initDate);

        new DatePickerDialog(mContext, listener,
                mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void setOnDateSetListener(OnDateSetListener mOnDateSetListener) {
        this.mOnDateSetListener = mOnDateSetListener;
    }

    public void setOnOkClickListener(View.OnClickListener mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
    }

    public interface OnDateSetListener {
        public void onDateSet(Date date, boolean isStart);
    }
}
