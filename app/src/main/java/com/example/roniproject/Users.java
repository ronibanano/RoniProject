package com.example.roniproject;

public class Users {
    public Users() {
    }
    String email, fullName, city;
    public Users(String email, String fullName, String city) {
        this.email = email;
        this.fullName = fullName;
        this.city = city;
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
