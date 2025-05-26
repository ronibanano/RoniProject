package com.example.roniproject.Obj;

/**
 * Represents a preview of a chat session, typically displayed in a list of ongoing conversations.
 * <p>
 * This class is a Plain Old Java Object (POJO) used to store essential information
 * for displaying a chat preview, such as the other participant's user ID, their display name,
 * and their city. It is often used to populate UI elements like a {@link android.widget.ListView}
 * in a chat overview screen (e.g., {@link com.example.roniproject.Frag.ChatFragment}).
 * </p>
 * <p>
 * It provides constructors for creating {@code ChatPreview} instances and getter methods
 * for accessing its properties.
 * </p>
 *
 * @see com.example.roniproject.Frag.ChatFragment
 * @see com.example.roniproject.Obj.ChatPreviewAdapter
 * @see com.example.roniproject.Activities.ChatActivity
 */
public class ChatPreview {
    private String userId;   // ID of the other user in the chat
    private String userName; // Display name of the other user
    private String city;     // City of the other user

    /**
     * Default constructor.
     * <p>
     * Required for specific scenarios, such as when instances are created by certain
     * libraries or frameworks through reflection (e.g., Firebase Realtime Database if
     * this object were to be directly deserialized, though it's typically constructed
     * manually with data from different sources in the current app structure).
     * Initializes all fields to their default values (null).
     * </p>
     */
    public ChatPreview() {}

    /**
     * Constructs a new {@code ChatPreview} instance with specified details.
     *
     * @param userId   The unique identifier of the other user in the chat.
     * @param userName The display name of the other user.
     * @param city     The city where the other user is located.
     */
    public ChatPreview(String userId, String userName, String city) {
        this.userId = userId;
        this.userName = userName;
        this.city = city;
    }

    /**
     * Gets the user ID of the other participant in the chat.
     *
     * @return The other user's ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Gets the display name of the other participant in the chat.
     *
     * @return The other user's display name.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Gets the city of the other participant in the chat.
     *
     * @return The other user's city.
     */
    public String getCity() {
        return city;
    }
}