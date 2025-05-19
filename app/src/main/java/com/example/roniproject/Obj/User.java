package com.example.roniproject.Obj;

public class User {

     private String userId, email, fullName, city;

    public User(String userId, String email, String fullName, String city) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.city = city;

    }

    public User() {
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

}
