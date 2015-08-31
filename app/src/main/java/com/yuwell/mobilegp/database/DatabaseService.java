package com.yuwell.mobilegp.database;

import com.yuwell.mobilegp.database.entity.Person;

/**
 * Created by admin on 2015/4/23.
 */
public interface DatabaseService {

    Person getPersonByIdNumber(String idNumber);
}
