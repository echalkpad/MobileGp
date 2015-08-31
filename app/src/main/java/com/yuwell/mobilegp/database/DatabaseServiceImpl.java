package com.yuwell.mobilegp.database;

import com.lidroid.xutils.DbUtils;
import com.totoro.commons.classresolver.ClassFilter;
import com.totoro.database.DatabaseManager;
import com.totoro.database.annotation.Resource;
import com.totoro.database.dao.BaseDAO;
import com.yuwell.mobilegp.database.dao.BGMeasurementDAO;
import com.yuwell.mobilegp.database.dao.BPMeasurementDAO;
import com.yuwell.mobilegp.database.dao.PersonDAO;
import com.yuwell.mobilegp.database.entity.Person;

/**
 * Created by Chen on 2015/4/23.
 */
public class DatabaseServiceImpl extends DatabaseManager implements DatabaseService {

    @Resource(name = "BGMeasurementDAO")
    private BGMeasurementDAO bgMeasurementDAO;

    @Resource(name = "BPMeasurementDAO")
    private BPMeasurementDAO bpMeasurementDAO;

    @Resource(name = "PersonDAO")
    private PersonDAO personDAO;

    @Override
    public Person getPersonByIdNumber(String idNumber) {
        return null;
    }


    private DatabaseServiceImpl(String packageNames) {
        super(packageNames);
    }

    private static class DatabaseHolder {
        static DatabaseService instance = new DatabaseServiceImpl("com.yuwell.mobilegp.database.dao");
    }

    public static DatabaseService getInstance() {
        return DatabaseHolder.instance;
    }

    @Override
    public Class<? extends DatabaseManager> getSubClazz() {
        return DatabaseServiceImpl.class;
    }

    @Override
    public String getDbName() {
        return "mobilegp.db";
    }

    @Override
    public ClassFilter getDAOFilter() {
        return new ClassFilter() {
            @Override
            public boolean accept(Class clazz) {
                return BaseDAO.class.equals(clazz.getSuperclass());
            }
        };
    }

    @Override
    public int getDBVersion() {
        return 1;
    }

    @Override
    public DbUtils.DbUpgradeListener getUpgradeListener() {
        return null;
    }

    /*Setters*/

    public void setBGMeasurementDAO(BGMeasurementDAO bgMeasurementDAO) {
        this.bgMeasurementDAO = bgMeasurementDAO;
    }

    public void setBPMeasurementDAO(BPMeasurementDAO bpMeasurementDAO) {
        this.bpMeasurementDAO = bpMeasurementDAO;
    }

    public void setPersonDAO(PersonDAO personDAO) {
        this.personDAO = personDAO;
    }
}
