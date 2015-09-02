package com.yuwell.mobilegp.database;

import com.lidroid.xutils.DbUtils;
import com.totoro.commons.classresolver.ClassFilter;
import com.totoro.database.DatabaseManager;
import com.totoro.database.annotation.Resource;
import com.totoro.database.dao.BaseDAO;
import com.yuwell.mobilegp.common.Const;
import com.yuwell.mobilegp.database.dao.BGMeasurementDAO;
import com.yuwell.mobilegp.database.dao.BPMeasurementDAO;
import com.yuwell.mobilegp.database.dao.PersonDAO;
import com.yuwell.mobilegp.database.entity.BGMeasurement;
import com.yuwell.mobilegp.database.entity.BPMeasurement;
import com.yuwell.mobilegp.database.entity.Person;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Map<String, Object> condition = new HashMap<>();
        condition.put("idNumber", idNumber);
        return personDAO.getEntity(condition);
    }

    @Override
    public boolean savePerson(Person person) {
        return personDAO.saveOrUpdate(person);
    }

    @Override
    public List<Date> getBPHistoryDistinctDate(Map<String, Object> condition) {
        return bpMeasurementDAO.getMeasureDate(condition);
    }

    @Override
    public Map<Date, List<BPMeasurement>> getBPListGroupByDate(List<Date> dateList, Map<String, Object> condition) {
        Map<Date, List<BPMeasurement>> map = new HashMap<Date, List<BPMeasurement>>();
        for (Date date : dateList) {
            condition.put(Const.START_DATE, date);
            condition.put(Const.END_DATE, date);
            map.put(date, bpMeasurementDAO.getList(condition));
        }
        return map;
    }

    @Override
    public boolean saveBP(BPMeasurement measurement) {
        return bpMeasurementDAO.saveOrUpdate(measurement);
    }

    @Override
    public List<Date> getBGHistoryDistinctDate(Map<String, Object> condition) {
        return bgMeasurementDAO.getMeasureDate(condition);
    }

    @Override
    public Map<Date, List<BGMeasurement>> getBGListGroupByDate(List<Date> dateList, Map<String, Object> condition) {
        Map<Date, List<BGMeasurement>> map = new HashMap<>();
        for (Date date : dateList) {
            condition.put(Const.START_DATE, date);
            condition.put(Const.END_DATE, date);
            map.put(date, bgMeasurementDAO.getList(condition));
        }
        return map;
    }

    @Override
    public boolean saveBG(BGMeasurement measurement) {
        return bgMeasurementDAO.saveOrUpdate(measurement);
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
