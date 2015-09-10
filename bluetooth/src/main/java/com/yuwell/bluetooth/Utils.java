package com.yuwell.bluetooth;

import java.math.BigDecimal;

/**
 * Common utilities
 * Created by chenshuai on 2015/9/9.
 */
public class Utils {

    public static int decimalToHex(int n) {
        return Integer.parseInt(Integer.toHexString(n), 16);
    }

    public static String multiply(float v1, float v2) {
        BigDecimal b1 = new BigDecimal(Float.toString(v1));
        BigDecimal b2 = new BigDecimal(Float.toString(v2));
        return retainOneDecimal(b1.multiply(b2).toString());
    }

    private static String retainOneDecimal(String val) {
        if (val.endsWith(".")) {
            val = val.substring(0, val.length() - 1);
        }
        String[] strArr = val.split("\\.");
        if (strArr.length == 2 && strArr[1].length() > 1) {
            val = strArr[0] + "." + strArr[1].substring(0, 1);
        }
        return val;
    }
}
