package com.yuwell.mobilegp.common.utils;

import com.yuwell.mobilegp.database.entity.BGMeasurement;
import com.yuwell.mobilegp.database.entity.BPMeasurement;

/**
 * Created by Chen on 15-8-31.
 */
public class CommonUtil {

    /**
     * 根据测量点计算血糖等级
     * @param glucoseVal
     * @param measurePoint
     * @return
     */
    public static int getGlucoseLevel(float glucoseVal, int measurePoint) {
        int level;

        if (measurePoint % 2 == 0) {
            //餐前
            if (glucoseVal < 4.2f) {
                level = BGMeasurement.LEVEL_LOW;
            } else if (glucoseVal >= 4.2f && glucoseVal < 6.1f) {
                level = BGMeasurement.LEVEL_NORMAL;
            } else if (glucoseVal >= 6.1f && glucoseVal < 7f){
                level = BGMeasurement.LEVEL_HIGH;
            } else {
                level = BGMeasurement.LEVEL_EXTREME_HIGH;
            }
        } else {
            //餐后
            if (glucoseVal < 4.4f) {
                level = BGMeasurement.LEVEL_LOW;
            } else if (glucoseVal >= 4.4f && glucoseVal < 7.8f) {
                level = BGMeasurement.LEVEL_NORMAL;
            } else if (glucoseVal >= 7.8f && glucoseVal < 11.1f){
                level = BGMeasurement.LEVEL_HIGH;
            } else {
                level = BGMeasurement.LEVEL_EXTREME_HIGH;
            }
        }

        return level;
    }

    public static String getPressureLevel(int sbp, int dbp) {
        String sbplevel;
        String dbplevel;

        if (sbp >= 180) {
            sbplevel = BPMeasurement.PRESSURE_LEVEL_HEAVY;
        } else if (sbp <= 179 && sbp >= 160) {
            sbplevel = BPMeasurement.PRESSURE_LEVEL_MODERATE;
        } else if (sbp <=159 && sbp >= 140) {
            sbplevel = BPMeasurement.PRESSURE_LEVEL_SLIGHT;
        } else if (sbp <= 139 && sbp >= 130) {
            sbplevel = BPMeasurement.PRESSURE_LEVEL_LOWER;
        } else if (sbp <= 129 && sbp >= 120) {
            sbplevel = BPMeasurement.PRESSURE_LEVEL_NORMAL;
        } else {
            sbplevel = BPMeasurement.PRESSURE_LEVEL_IDEAL;
        }

        if (dbp >= 110) {
            dbplevel = BPMeasurement.PRESSURE_LEVEL_HEAVY;
        } else if (dbp <= 109 && dbp >= 100) {
            dbplevel = BPMeasurement.PRESSURE_LEVEL_MODERATE;
        } else if (dbp <= 99 && dbp >= 90) {
            dbplevel = BPMeasurement.PRESSURE_LEVEL_SLIGHT;
        } else if (dbp <= 89 && dbp >= 85) {
            dbplevel = BPMeasurement.PRESSURE_LEVEL_LOWER;
        } else if (dbp <= 84 && dbp >= 80) {
            dbplevel = BPMeasurement.PRESSURE_LEVEL_NORMAL;
        } else {
            dbplevel = BPMeasurement.PRESSURE_LEVEL_IDEAL;
        }

        return Integer.valueOf(sbplevel) > Integer.valueOf(dbplevel) ? sbplevel : dbplevel;
    }
}
