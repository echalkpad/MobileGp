package com.yuwell.mobilegp.database.dao;

import com.lidroid.xutils.DbUtils;
import com.totoro.database.dao.BaseDAO;
import com.yuwell.mobilegp.database.entity.Person;

/**
 * Created by Chen on 15-8-31.
 */
public class PersonDAO extends BaseDAO<Person> {

    public PersonDAO(DbUtils db) {
        super(db);
    }

}
