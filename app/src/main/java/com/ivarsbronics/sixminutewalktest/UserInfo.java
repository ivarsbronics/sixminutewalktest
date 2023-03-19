package com.ivarsbronics.sixminutewalktest;

public class UserInfo {
    public String birthDate, height, gender, weight;

    public UserInfo() {
    }

    public UserInfo (String txtBirthDate, String txtHeight, String txtGender, String txtWeight){
        this.birthDate = txtBirthDate;
        this.gender = txtGender;
        this.height = txtHeight;
        this.weight = txtWeight;
    }

    public String getAge() {
        return birthDate;
    }

    public String getHeight() {
        return height;
    }

    public String getGender() {
        return gender;
    }

    public String getWeight() {
        return weight;
    }

    public void setAge(String birthDate) {
        this.birthDate = birthDate;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }
}
