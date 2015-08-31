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

	public static final int LEVEL_LOW = 1;
	public static final int LEVEL_NORMAL = 2;
	public static final int LEVEL_HIGH = 3;
	public static final int LEVEL_EXTREME_HIGH = 4;

	@Column(column = "value")
	private float value;
	
	@Column(column = "measureTime")
	private Date measureTime;
	
	@Foreign(column = "personId", foreign = "id")
	private Person person;

	@Column(column = "level")
	private int level;

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

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
}
