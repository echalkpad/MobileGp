package com.yuwell.mobilegp.database.dao;

import android.database.Cursor;
import android.text.TextUtils;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.totoro.commons.utils.DateUtil;
import com.totoro.database.dao.BaseDAO;
import com.yuwell.mobilegp.common.Const;
import com.yuwell.mobilegp.database.entity.BPMeasurement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Chen on 15-8-31.
 */
public class BPMeasurementDAO extends BaseDAO<BPMeasurement> {

    public BPMeasurementDAO(DbUtils db) {
        super(db);
    }

    public List<Date> getMeasureDate(Map<String, Object> condition) {
        String personId = (String) condition.get(BPMeasurement.COLUMN_PERSONID);
        Date start = (Date) condition.get(Const.START_DATE);
        Date end = (Date) condition.get(Const.END_DATE);
        String level = (String) condition.get(BPMeasurement.COLUMN_LEVEL);

        StringBuilder sql = new StringBuilder("SELECT measureTime FROM BPMEASUREMENT WHERE ")
                .append(BPMeasurement.COLUMN_DELETE_FLAG).append(" = '").append(BPMeasurement.NORMAL).append("'");

        if (!TextUtils.isEmpty(personId)) {
            sql.append(" AND personId = '").append(personId).append("'");
        }
        if (start != null) {
            sql.append(" AND measureTime >= ").append(DateUtil.formatStartDate(start).getTime());
        }
        if (end != null) {
            sql.append(" AND measureTime <= ").append(DateUtil.formatEndDate(end).getTime());
        }
        if (!TextUtils.isEmpty(level)) {
            sql.append(" AND level = '").append(level).append("'");
        }

        sql.append(" ORDER BY measureTime DESC");

        Set<Date> data = new TreeSet<Date>();
        try {
            Cursor cursor = db.execQuery(sql.toString());
            while (cursor.moveToNext()) {
                data.add(DateUtil.formatStartDate(new Date(cursor.getLong(0))));
            }
        } catch (DbException e) {
            e.printStackTrace();
        }

        List<Date> listDate = new ArrayList<Date>(data);
        Collections.sort(listDate, Collections.reverseOrder());

        return listDate;
    }

    public List<BPMeasurement> getList(Map<String, Object> condition) {
        String memberId = (String) condition.get(BPMeasurement.COLUMN_PERSONID);
        Date start = (Date) condition.get(Const.START_DATE);
        Date end = (Date) condition.get(Const.END_DATE);
        String level = (String) condition.get(BPMeasurement.COLUMN_LEVEL);
        Boolean desc = (Boolean) condition.get("desc");
        Integer top = (Integer) condition.get("top");

        Selector selector = getNormalSelector();

        if (!TextUtils.isEmpty(memberId)) {
            selector.and("personId", "=", memberId);
        }
        if (start != null) {
            selector.and("measureTime", ">=", DateUtil.formatStartDate(start).getTime());
        }
        if (end != null) {
            selector.and("measureTime", "<=", DateUtil.formatEndDate(end).getTime());
        }
        if (!TextUtils.isEmpty(level)) {
            selector.and("level", "=", level);
        }
        selector.orderBy("measureTime", desc == null || desc);
        if (top != null) {
            selector.limit(top);
        }

        return getList(selector, condition);
    }
}
