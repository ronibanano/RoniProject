package com.example.roniproject.Obj;

/**
 * Represents a User object with their essential details.
 * <p>
 * This class is a Plain Old Java Object (POJO) used to model user data,
 * primarily for interaction with Firebase Realtime Database. It includes fields
 * for the user's unique ID (typically from Firebase Authentication), email address,
 * full name, and city.
 * </p>
 * <p>
 * It provides constructors for creating {@code User} instances and getter/setter
 * methods for accessing and modifying its properties.
 * </p>
 *
 * @see com.example.roniproject.Activities.LoginActivity
 * @see com.example.roniproject.Activities.RegisertActivity
 * @see com.example.roniproject.Frag.ProfileFragment
 */
public class User {

    private String userId;
    private String email;
    private String fullName;
    private String city;

    /**
     * Instantiates a new {@code User} with specified details.
     *
     * @param userId   The unique identifier for the user (e.g., Firebase UID).
     * @param email    The email address of the user.
     * @param fullName The full name of the user.
     * @param city     The city where the user resides.
     */
    public User(String userId, String email, String fullName, String city) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.city = city;
    }

    /**
     * Default constructor.
     * <p>
     * Required for calls to {@link com.google.firebase.database.DataSnapshot#getValue(Class)}.
     * Initializes all fields to their default values (null).
     * </p>
     */
    public User() {
    }

    /**
     * Gets the unique ID of the user.
     *
     * @return The user's ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the unique ID of the user.
     *
     * @param userId The new ID for the user.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the email address of the user.
     *
     * @return The user's email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the user.
     *
     * @param email The new email address for the user.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the full name of the user.
     *
     * @return The user's full name.
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the full name of the user.
     *
     * @param fullName The new full name for the user.
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Gets the city where the user resides.
     *
     * @return The user's city.
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the city where the user resides.
     *
     * @param city The new city for the user.
     */
    public void setCity(String city) {
        this.city = city;
    }
}
