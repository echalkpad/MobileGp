package com.yuwell.mobilegp.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.totoro.commons.adapter.BaseViewHolder;
import com.totoro.commons.utils.DateUtil;
import com.totoro.commons.utils.ResourceUtil;
import com.yuwell.mobilegp.R;
import com.yuwell.mobilegp.bluetooth.BluetoothConstant;
import com.yuwell.mobilegp.common.Const;
import com.yuwell.mobilegp.common.GlobalContext;
import com.yuwell.mobilegp.common.event.EventListener;
import com.yuwell.mobilegp.common.event.EventMessage;
import com.yuwell.mobilegp.common.utils.CommonUtil;
import com.yuwell.mobilegp.database.DatabaseService;
import com.yuwell.mobilegp.database.entity.BPMeasurement;
import com.yuwell.mobilegp.database.entity.Person;
import com.yuwell.mobilegp.ui.Home;
import com.yuwell.mobilegp.ui.PrinterActivity;
import com.yuwell.mobilegp.ui.widget.DateRangePicker;
import com.yuwell.mobilegp.ui.widget.PopupListView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private ExpandableListView mListView;
    private HistoryAdapter mAdapter;

    private Date start;
    private Date end;
    private String level;

    private Person person;
    private DatabaseService db;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (Home) activity;
        person = this.activity.getPerson();

        start = end = new Date();
        db = GlobalContext.getDatabase();
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

        DateRangePicker picker = (DateRangePicker) mainView.findViewById(R.id.date_range_picker);
        picker.setDate(start, end);
        picker.setOnDateSetListener(new DateRangePicker.OnDateSetListener() {
            @Override
            public void onStartSet(Date date) {
                start = date;
                setListData();
            }

            @Override
            public void onEndSet(Date date) {
                end = date;
                setListData();
            }
        });

        PopupListView popupListView = (PopupListView) mainView.findViewById(R.id.pop_list);
        popupListView.setLevels(R.array.pressure_level);
        popupListView.setOnItemClickListener(new PopupListView.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                if (pos != 0) {
                    level = String.valueOf(pos - 1);
                } else {
                    level = "";
                }
                setListData();
            }
        });

        mListView = (ExpandableListView) mainView.findViewById(R.id.lv_bp);
        mAdapter = new HistoryAdapter(activity);
        mListView.setAdapter(mAdapter);
        setListData();

        return mainView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void setListData() {
        Map<String, Object> condition = new HashMap<>();
        condition.put(BPMeasurement.COLUMN_PERSONID, person.getId());
        condition.put(BPMeasurement.COLUMN_LEVEL, level);
        condition.put(Const.START_DATE, start);
        condition.put(Const.END_DATE, end);

        List<Date> dateList = db.getBPHistoryDistinctDate(condition);
        if (dateList.size() > 0) {
            Map<Date, List<BPMeasurement>> dateMap = db.getBPListGroupByDate(dateList, condition);
            mAdapter.setData(dateList, dateMap);
        } else {
            mAdapter.setData(dateList, new HashMap<Date, List<BPMeasurement>>());
            Toast.makeText(activity, R.string.no_data, Toast.LENGTH_SHORT).show();
        }
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

            BPMeasurement measurement = new BPMeasurement();
            measurement.setSbp(array[0]);
            measurement.setDbp(array[1]);
            measurement.setPulseRate(array[2]);
            measurement.setLevel(CommonUtil.getPressureLevel(array[0], array[1]));
            measurement.setPerson(person);
            measurement.setMeasureTime(new Date());
            if (db.saveBP(measurement)) {
                setListData();
            }
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

    public final class HistoryAdapter extends BaseExpandableListAdapter {

        private List<Date> dateList = new ArrayList<Date>();
        private Map<Date, List<BPMeasurement>> dataMap = new HashMap<Date, List<BPMeasurement>>();
        private LayoutInflater mInflater;

        public HistoryAdapter(Context context) {
            this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getGroupCount() {
            return dateList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return dataMap.get(dateList.get(groupPosition)).size();
        }

        @Override
        public String getGroup(int groupPosition) {
            return DateUtil.formatYMD(dateList.get(groupPosition));
        }

        @Override
        public BPMeasurement getChild(int groupPosition, int childPosition) {
            return dataMap.get(dateList.get(groupPosition)).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            DateViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_bp_history_group, null);
                holder = new DateViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (DateViewHolder) convertView.getTag();
            }

            if (!isExpanded) {
                mListView.expandGroup(groupPosition);
            }

            holder.mDate.setText(getGroup(groupPosition));

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            Measurement holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_bp_history_child, null);
                holder = new Measurement(convertView);
                convertView.setTag(holder);
            } else {
                holder = (Measurement) convertView.getTag();
            }

            BPMeasurement bpMeasurement = getChild(groupPosition, childPosition);
            holder.mMeasureTime.setText(DateUtil.formatCustomDate(bpMeasurement.getMeasureTime(), "HH:mm"));
            holder.mSbpDbp.setText(bpMeasurement.getSbp() + " / " + bpMeasurement.getDbp());
            holder.mPulseRate.setText(String.valueOf(bpMeasurement.getPulseRate()));
            holder.mLevel.setText(ResourceUtil.getStringId("bp_level_" + bpMeasurement.getLevel()));

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

        public void setData(List<Date> dateList, Map<Date, List<BPMeasurement>> dataMap) {
            this.dateList = dateList;
            this.dataMap = dataMap;
            notifyDataSetChanged();
        }
    }

    private static final class DateViewHolder extends BaseViewHolder {
        TextView mDate;

        public DateViewHolder(View view) {
            super(view);
            mDate = (TextView) view.findViewById(R.id.tv_measure_date);
        }
    }

    private static final class Measurement extends BaseViewHolder {
        TextView mSbpDbp;
        TextView mPulseRate;
        TextView mMeasureTime;
        TextView mLevel;

        public Measurement(View view) {
            super(view);
            mMeasureTime = (TextView) view.findViewById(R.id.tv_measure_time);
            mSbpDbp = (TextView) view.findViewById(R.id.tv_sbp_dbp);
            mPulseRate = (TextView) view.findViewById(R.id.tv_pulse_rate);
            mLevel = (TextView) view.findViewById(R.id.tv_level);
        }
    }


}
