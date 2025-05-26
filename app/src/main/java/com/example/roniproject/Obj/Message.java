package com.example.roniproject.Obj;

/**
 * Represents a chat message with its sender, receiver, text content, and timestamp.
 * <p>
 * This class is a Plain Old Java Object (POJO) used to model individual messages
 * within a chat conversation, primarily for interaction with Firebase Realtime Database.
 * Each message contains the ID of the sender, the ID of the receiver, the actual text
 * of the message, and a timestamp indicating when the message was sent.
 * </p>
 * <p>
 * It provides constructors for creating {@code Message} instances and getter/setter
 * methods for accessing and modifying its properties.
 * </p>
 *
 * @see com.example.roniproject.Activities.ChatActivity
 * @see com.example.roniproject.Obj.MessageAdapter
 * @see com.example.roniproject.ChatNotificationService
 */
public class Message {
    private String senderId;    // ID of the user who sent the message
    private String receiverId;  // ID of the user who received the message
    private String text;        // The content of the message
    private long timestamp;     // Time the message was sent (e.g., System.currentTimeMillis())

    /**
     * Default constructor.
     * <p>
     * Required for calls to {@link com.google.firebase.database.DataSnapshot#getValue(Class)}.
     * Initializes all fields to their default values (null for Strings, 0 for long).
     * </p>
     */
    public Message() {}

    /**
     * Constructs a new {@code Message} instance with specified details.
     *
     * @param senderId   The unique identifier of the sender.
     * @param receiverId The unique identifier of the receiver.
     * @param text       The textual content of the message.
     * @param timestamp  The time the message was sent, typically in milliseconds since epoch.
     */
    public Message(String senderId, String receiverId, String text, long timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.timestamp = timestamp;
    }

    /**
     * Gets the ID of the message sender.
     *
     * @return The sender's user ID.
     */
    public String getSenderId() { return senderId; }

    /**
     * Sets the ID of the message sender.
     *
     * @param senderId The new user ID for the sender.
     */
    public void setSenderId(String senderId) { this.senderId = senderId; }

    /**
     * Gets the ID of the message receiver.
     *
     * @return The receiver's user ID.
     */
    public String getReceiverId() { return receiverId; }

    /**
     * Sets the ID of the message receiver.
     *
     * @param receiverId The new user ID for the receiver.
     */
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    /**
     * Gets the text content of the message.
     *
     * @return The message text.
     */
    public String getText() { return text; }

    /**
     * Sets the text content of the message.
     *
     * @param text The new text for the message.
     */
    public void setText(String text) { this.text = text; }

    /**
     * Gets the timestamp of when the message was sent.
     *
     * @return The timestamp in milliseconds since the epoch.
     */
    public long getTimestamp() { return timestamp; }

    /**
     * Sets the timestamp of when the message was sent.
     *
     * @param timestamp The new timestamp for the message (milliseconds since epoch).
     */
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}