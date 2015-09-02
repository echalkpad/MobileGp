package com.yuwell.mobilegp.database.entity;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Foreign;
import com.lidroid.xutils.db.annotation.Table;
import com.totoro.database.entity.EntityBase;

import java.util.Date;

/**
 * 血糖测量
 */
@Table(name = "BGMEASUREMENT")
public class BGMeasurement extends EntityBase {

	public static final String COLUMN_PERSONID = "personId";
	public static final String COLUMN_LEVEL = "level";

	public static final String LEVEL_LOW = "0";
	public static final String LEVEL_NORMAL = "1";
	public static final String LEVEL_HIGH = "2";
	public static final String LEVEL_EXTREME_HIGH = "3";

	@Column(column = "value")
	private float value;
	
	@Column(column = "measureTime")
	private Date measureTime;
	
	@Foreign(column = "personId", foreign = "id")
	private Person person;

	@Column(column = "level")
	private String level;

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}

	public Date getMeasureTime() {
		return measureTime;
	}

	public void setMeasureTime(Date measureTime) {
		this.measureTime = measureTime;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}
}
