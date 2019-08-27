package com.stupidtree.hita.online;

import java.util.Objects;

import cn.bmob.v3.BmobObject;

public class Teacher extends BmobObject {
    String name;
    String gender;
    String title;
    String school;
    String phone;
    String email;
    String teacherCode;
    String address;


    String detail;
    String photoLink;

    public Teacher(String teacherCode,String name, String gender, String title, String school, String phone, String email, String detail) {
        this.teacherCode = teacherCode;
        this.name = name;
        this.gender = gender;
        this.title = title;
        this.school = school;
        this.phone = phone;
        this.email = email;
        this.detail = detail;
    }
    public void setPhotoLink(String photoLink) {
        this.photoLink = photoLink;
    }

    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public String getTitle() {
        return title;
    }

    public String getSchool() {
        return school;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getDetail() {
        return detail;
    }

    public String getPhotoLink() {
        return photoLink;
    }

    public String getTeacherCode() {
        return teacherCode;
    }

    public void setTeacherCode(String teacherCode) {
        this.teacherCode = teacherCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Teacher teacher = (Teacher) o;
        return Objects.equals(name, teacher.name) &&
                Objects.equals(teacherCode, teacher.teacherCode);
    }

    @Override
    public String toString() {
        return "Teacher{" +
                "name='" + name + '\'' +
                ", teacherCode='" + teacherCode + '\'' +
                ", photoLink='" + photoLink + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, teacherCode);
    }
}
