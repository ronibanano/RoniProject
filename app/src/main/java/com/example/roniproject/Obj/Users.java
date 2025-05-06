package com.example.roniproject.Obj;

public class Users {
    public Users() {
    }
    String email, fullName, city, phoneNumber;
    public Users(String email, String fullName, String city, String phoneNumber) {
        this.email = email;
        this.fullName = fullName;
        this.city = city;
        this.phoneNumber=phoneNumber;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
