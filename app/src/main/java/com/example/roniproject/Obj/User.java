package com.example.roniproject.Obj;

/**
 * The type User.
 */
public class User {

     private String userId;
     private String email;
     private String fullName;
     private String city;

    /**
     * Instantiates a new User.
     *
     * @param userId   the user id
     * @param email    the email
     * @param fullName the full name
     * @param city     the city
     */
    public User(String userId, String email, String fullName, String city) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.city = city;

    }

    /**
     * Instantiates a new User.
     */
    public User() {
    }

    /**
     * Gets user id.
     *
     * @return the user id
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets user id.
     *
     * @param userId the user id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets email.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets email.
     *
     * @param email the email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets full name.
     *
     * @return the full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets full name.
     *
     * @param fullName the full name
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Gets city.
     *
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets city.
     *
     * @param city the city
     */
    public void setCity(String city) {
        this.city = city;
    }

}
