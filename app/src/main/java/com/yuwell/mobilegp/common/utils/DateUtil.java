package com.yuwell.mobilegp.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Chen on 2015/9/1.
 */
public class DateUtil extends com.totoro.commons.utils.DateUtil {

    public static Date parseCustomString(String date, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            return sdf.parse(date);
        } catch (ParseException var2) {
            var2.printStackTrace();
            return null;
        }
    }
}
