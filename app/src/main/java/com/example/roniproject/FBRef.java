package com.example.roniproject;

import com.google.firebase.Firebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Provides static references to Firebase services and database paths.
 * <p>
 * This utility class centralizes access to Firebase instances, making it easier
 * to manage and use Firebase services like {@link FirebaseStorage} and
 * {@link FirebaseDatabase} throughout the application. It initializes static
 * references to:
 * <ul>
 *     <li>The default {@link FirebaseStorage} instance.</li>
 *     <li>The root {@link StorageReference} of the Firebase Storage.</li>
 *     <li>A specific {@link StorageReference} to a "Books" path within Firebase Storage,
 *         likely used for storing book-related files like cover images.</li>
 *     <li>A {@link DatabaseReference} to a "Messages" node in Firebase Realtime Database,
 *         used for chat functionalities.</li>
 *     <li>A {@link DatabaseReference} to a "Users" node in Firebase Realtime Database,
 *         used for storing user profile information.</li>
 * </ul>
 * By using static references, other parts of the application can easily access these
 * Firebase resources without needing to instantiate {@code FBRef} or re-initialize
 * Firebase references repeatedly.
 * </p>
 * <p>
 * Example Usage:
 * <pre>{@code
 * StorageReference booksStorageRef = FBRef.refBooks;
 * DatabaseReference usersDbRef = FBRef.refUsers;
 * }</pre>
 * </p>
 *
 * @see com.google.firebase.storage.FirebaseStorage
 * @see com.google.firebase.storage.StorageReference
 * @see com.google.firebase.database.FirebaseDatabase
 * @see com.google.firebase.database.DatabaseReference
 */
public class FBRef {
    /**
     * Static instance of {@link FirebaseStorage}, initialized to the default app instance.
     * Used for accessing Firebase Cloud Storage services.
     */
    public static FirebaseStorage FBST = FirebaseStorage.getInstance();

    /**
     * Static {@link StorageReference} pointing to the root of the Firebase Cloud Storage bucket.
     */
    public static StorageReference refST = FBST.getReference();

    /**
     * Static {@link StorageReference} pointing to the "Books" directory within Firebase Cloud Storage.
     * Likely used for storing files related to books, such as cover images.
     */
    public static StorageReference refBooks = refST.child("Books");

    /**
     * Static {@link DatabaseReference} pointing to the "Messages" node in Firebase Realtime Database.
     * Used for storing and retrieving chat messages.
     */
    public static DatabaseReference refMessages = FirebaseDatabase.getInstance().getReference().child("Messages");

    /**
     * Static {@link DatabaseReference} pointing to the "Users" node in Firebase Realtime Database.
     * Used for storing and retrieving user profile information.
     */
    public static DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference().child("Users");

}
