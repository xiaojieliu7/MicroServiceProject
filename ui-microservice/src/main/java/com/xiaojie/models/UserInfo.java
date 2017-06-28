package com.xiaojie.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by hadoop on 17-6-25.
 */
@Entity
public class UserInfo {
    @Id
    @GeneratedValue
    private Long id;
    private String userid;
    private String password;
    private String age;
    private String gender;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
    public UserInfo() {
    }

    public UserInfo(String userid, String password, String age, String gender){
        this.userid=userid;
        this.password=password;
        this.age=age;
        this.gender=gender;
    }
}
