package com.yuwell.mobilegp.database.entity;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Foreign;
import com.lidroid.xutils.db.annotation.Table;
import com.totoro.database.entity.EntityBase;

import java.util.Date;

/**
 * 血压测量
 * Created by Chen on 2015/4/28.
 */
@Table(name = "BPMEASUREMENT")
public class BPMeasurement extends EntityBase {

    public static final String PRESSURE_LEVEL_IDEAL = "0";
    public static final String PRESSURE_LEVEL_NORMAL = "1";
    public static final String PRESSURE_LEVEL_LOWER = "2";
    public static final String PRESSURE_LEVEL_SLIGHT = "3";
    public static final String PRESSURE_LEVEL_MODERATE = "4";
    public static final String PRESSURE_LEVEL_HEAVY = "5";

    // 舒张压
    @Column(column = "sbp")
    private int sbp;

    // 收缩压
    @Column(column = "dbp")
    private int dbp;

    // 脉率
    @Column(column = "pulseRate")
    private int pulseRate;

    // 心律不齐
    @Column(column = "arrhythima")
    private String arrhythima;

    // 测量时间
    @Column(column = "measureTime")
    private Date measureTime;

    // 血压等级
    @Column(column = "level")
    private String level;

    @Foreign(column = "personId", foreign = "id")
    private Person person;

    public BPMeasurement() {}

    public BPMeasurement(String id) {
        setId(id);
    }

    public int getSbp() {
        return sbp;
    }

    public void setSbp(int sbp) {
        this.sbp = sbp;
    }

    public int getDbp() {
        return dbp;
    }

    public void setDbp(int dbp) {
        this.dbp = dbp;
    }

    public int getPulseRate() {
        return pulseRate;
    }

    public void setPulseRate(int pulseRate) {
        this.pulseRate = pulseRate;
    }

    public String getArrhythima() {
        return arrhythima;
    }

    public void setArrhythima(String arrhythima) {
        this.arrhythima = arrhythima;
    }

    public Date getMeasureTime() {
        return measureTime;
    }

    public void setMeasureTime(Date measureTime) {
        this.measureTime = measureTime;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

}
