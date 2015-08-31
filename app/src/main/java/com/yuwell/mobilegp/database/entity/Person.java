package com.yuwell.mobilegp.database.entity;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Table;
import com.totoro.database.entity.EntityBase;

/**
 * Created by Chen on 15-8-31.
 */
@Table(name = "PERSON")
public class Person extends EntityBase {

    @Column(column = "name")
    private String name;

    @Column(column = "gender")
    private String gender;

    @Column(column = "birthday")
    private String birthday;

    @Column(column = "idNumber")
    private String idNumber;

    @Column(column = "imgPath")
    private String imgPath;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }
}
