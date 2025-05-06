package com.example.roniproject.Obj;

public class ChatPreview {
    private String userId;
    private String userName;
    private String city;

    public ChatPreview() {}

    public ChatPreview(String userId, String userName, String city) {
        this.userId = userId;
        this.userName = userName;
        this.city = city;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getCity() {
        return city;
    }
}
