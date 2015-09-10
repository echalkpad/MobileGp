package com.yuwell.mobilegp.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.totoro.commons.adapter.BaseViewHolder;
import com.totoro.commons.utils.ResourceUtil;
import com.yuwell.bluetooth.constants.BleMessage;
import com.yuwell.mobilegp.R;
import com.yuwell.mobilegp.common.Const;
import com.yuwell.mobilegp.common.GlobalContext;
import com.yuwell.mobilegp.common.event.EventListener;
import com.yuwell.mobilegp.common.event.EventMessage;
import com.yuwell.mobilegp.common.utils.CommonUtil;
import com.yuwell.mobilegp.common.utils.DateUtil;
import com.yuwell.mobilegp.database.DatabaseService;
import com.yuwell.mobilegp.database.entity.BGMeasurement;
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
 * 血糖测量
 * Created by Chen on 14-11-9.
 */
public class BgMeasure extends Fragment implements EventListener {

    private Home activity;

    private TextView mVal;
    private TextView mMeasureTime;
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
        db = GlobalContext.getDatabase();
        start = end = new Date();
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
        mMeasureTime = (TextView) mainView.findViewById(R.id.tv_measure_time);

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
        popupListView.setLevels(R.array.bg_level);
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

        mListView = (ExpandableListView) mainView.findViewById(R.id.lv_bg);
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
        condition.put(BGMeasurement.COLUMN_PERSONID, person.getId());
        condition.put(BGMeasurement.COLUMN_LEVEL, level);
        condition.put(Const.START_DATE, start);
        condition.put(Const.END_DATE, end);

        List<Date> dateList = db.getBGHistoryDistinctDate(condition);
        if (dateList.size() > 0) {
            Map<Date, List<BGMeasurement>> dateMap = db.getBGListGroupByDate(dateList, condition);
            mAdapter.setData(dateList, dateMap);
        } else {
            mAdapter.setData(dateList, new HashMap<Date, List<BGMeasurement>>());
        }
    }

    /**
     * 事件消息接收
     * @param event
     */
    @Override
    public void onEventMainThread(Message event) {
        if (event.what == BleMessage.BG_DATA) {
            String val  = (String) event.obj;
            Date time = new Date();

            mVal.setText(val);
            mMeasureTime.setText(DateUtil.formatHMS(time));

            BGMeasurement measurement = new BGMeasurement();
            measurement.setValue(Float.valueOf((String) event.obj));
            measurement.setMeasureTime(time);
            measurement.setPerson(person);
            measurement.setLevel(CommonUtil.getGlucoseLevel(measurement.getValue()));
            if (db.saveBG(measurement)) {
                setListData();
            }
        }
        if (event.what == EventMessage.ON_PRINT && getUserVisibleHint()) {
            StringBuilder builder = new StringBuilder();
            builder.append("血糖：" + mVal.getText() + "\n");

            Intent intent = new Intent(activity, PrinterActivity.class);
            intent.putExtra("text", builder.toString());
            startActivity(intent);
        }
    }

    public final class HistoryAdapter extends BaseExpandableListAdapter {

        private List<Date> dateList = new ArrayList<>();
        private Map<Date, List<BGMeasurement>> dataMap = new HashMap<>();
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
        public BGMeasurement getChild(int groupPosition, int childPosition) {
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
                convertView = mInflater.inflate(R.layout.item_bg_history_child, null);
                holder = new Measurement(convertView);
                convertView.setTag(holder);
            } else {
                holder = (Measurement) convertView.getTag();
            }

            BGMeasurement bgMeasurement = getChild(groupPosition, childPosition);
            holder.mMeasureTime.setText(DateUtil.formatCustomDate(bgMeasurement.getMeasureTime(), "HH:mm"));
            holder.mVal.setText(String.valueOf(bgMeasurement.getValue()));
            holder.mLevel.setText(ResourceUtil.getStringId("bg_level_" + bgMeasurement.getLevel()));

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

        public void setData(List<Date> dateList, Map<Date, List<BGMeasurement>> dataMap) {
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
        TextView mVal;
        TextView mMeasureTime;
        TextView mLevel;

        public Measurement(View view) {
            super(view);
            mMeasureTime = (TextView) view.findViewById(R.id.tv_measure_time);
            mVal = (TextView) view.findViewById(R.id.tv_bg_val);
            mLevel = (TextView) view.findViewById(R.id.tv_level);
        }
    }

}
