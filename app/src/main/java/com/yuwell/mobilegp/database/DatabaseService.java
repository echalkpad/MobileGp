package com.yuwell.mobilegp.database;

import com.yuwell.mobilegp.database.entity.BPMeasurement;
import com.yuwell.mobilegp.database.entity.Person;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2015/4/23.
 */
public interface DatabaseService {

    Person getPersonByIdNumber(String idNumber);

    boolean savePerson(Person person);

    List<Date> getBPHistoryDistinctDate(Map<String, Object> condition);

    Map<Date, List<BPMeasurement>> getBPListGroupByDate(List<Date> dateList, Map<String, Object> condition);

    boolean saveBP(BPMeasurement measurement);

}
