package com.yuwell.mobilegp.database.dao;

import com.lidroid.xutils.DbUtils;
import com.totoro.database.dao.BaseDAO;
import com.yuwell.mobilegp.database.entity.BGMeasurement;

/**
 * Created by Chen on 15-8-31.
 */
public class BGMeasurementDAO extends BaseDAO<BGMeasurement> {

    public BGMeasurementDAO(DbUtils db) {
        super(db);
    }
}
