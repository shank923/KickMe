package com.example.user.kickme.User_Acitivity;


/**
 * Created by poocoder on 11/16/17 and edited by smthls00 on 22/12/17.
 */


public class User {
    private String nameS, surnameS, emailS;
    private Double latitude, longitude;


    public User(String nameS, String surnameS, String emailS, Double latitude, Double longitude){
        this.nameS = nameS;
        this.surnameS = surnameS;
        this.emailS = emailS;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getNameS() {
        return nameS;
    }

    public String getSurnameS() {
        return surnameS;
    }

    public String getEmailS() {
        return emailS;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

}
