package com.yuwell.mobilegp.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yuwell.mobilegp.R;
import com.yuwell.mobilegp.common.utils.DateUtil;
import com.yuwell.mobilegp.common.utils.DialogUtil;

import java.util.Date;

/**
 * Start-end date picker
 * Created by Chen on 15-9-1.
 */
public class DateRangePicker extends LinearLayout implements View.OnClickListener {

    private TextView mStartDate;
    private TextView mEndDate;

    private Date startDate;
    private Date endDate;

    private DialogUtil dialogUtil;

    private OnDateSetListener onDateSetListener;

    public DateRangePicker(Context context) {
        this(context, null);
    }

    public DateRangePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DateRangePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.date_range_picker, this);

        dialogUtil = new DialogUtil(context, new DialogUtil.OnDateSetListener() {
            @Override
            public void onDateSet(Date date, boolean isStart) {
                if (isStart) {
                    startDate = date;
                    mStartDate.setText(DateUtil.formatYMD(date));
                    if (onDateSetListener != null) {
                        onDateSetListener.onStartSet(date);
                    }
                } else {
                    endDate = date;
                    mEndDate.setText(DateUtil.formatYMD(date));
                    if (onDateSetListener != null) {
                        onDateSetListener.onEndSet(date);
                    }
                }
            }
        });

        mStartDate = (TextView) findViewById(R.id.tv_start_date);
        mStartDate.setOnClickListener(this);
        mEndDate = (TextView) findViewById(R.id.tv_end_date);
        mEndDate.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_start_date:
                dialogUtil.showDateDialog(true, startDate);
                break;
            case R.id.tv_end_date:
                dialogUtil.showDateDialog(false, endDate);
                break;
        }
    }

    public void setDate(Date start, Date end) {
        this.startDate = start;
        this.endDate = end;

        mStartDate.setText(DateUtil.formatYMD(start));
        mEndDate.setText(DateUtil.formatYMD(end));
    }

    public void setOnDateSetListener(OnDateSetListener onDateSetListener) {
        this.onDateSetListener = onDateSetListener;
    }

    public interface OnDateSetListener {
        void onStartSet(Date date);

        void onEndSet(Date date);
    }
}
