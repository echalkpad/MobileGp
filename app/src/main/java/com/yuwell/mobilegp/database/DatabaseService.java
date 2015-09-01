package com.yuwell.mobilegp.database;

import com.yuwell.mobilegp.database.entity.BPMeasurement;
import com.yuwell.mobilegp.database.entity.Person;

import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2015/4/23.
 */
public interface DatabaseService {

    Person getPersonByIdNumber(String idNumber);

    boolean savePerson(Person person);

    List<BPMeasurement> getBpList(Map<String, Object> condition);
}
