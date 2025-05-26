package com.example.roniproject.Frag;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roniproject.Activities.AddBook;
import com.example.roniproject.Obj.Book;
import com.example.roniproject.Obj.BookAdapter;
import com.example.roniproject.Obj.User;
import com.example.roniproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment responsible for displaying the profile information of the currently logged-in user,
 * including their personal details and a list of books they own.
 * <p>
 * This fragment retrieves the user's details (full name and city) from the "Users" node
 * in Firebase Realtime Database. It also fetches a list of books associated with the current user
 * by checking the "owners" field under each book in the "Books" node.
 * </p>
 * <p>
 * The user's books are displayed in a {@link ListView}. Users can add new books via a button
 * that launches the {@link com.example.roniproject.Activities.AddBook} activity.
 * Tapping on a book in their list presents a dialog asking for confirmation to delete the book
 * from their ownership. If confirmed, the user is removed from the book's "owners" list in Firebase.
 * If the user was the last owner of the book, the entire book entry (including its cover image
 * in Firebase Storage, if applicable) is deleted.
 * </p>
 * <p>
 * Interactions with Firebase include:
 * <ul>
 *     <li>Fetching user data (full name, city) from the "Users" node based on the current user's ID.</li>
 *     <li>Fetching all books from the "Books" node and filtering them to show only those owned by the current user.</li>
 *     <li>Removing the current user from a book's "owners" list in the "Books" node.</li>
 *     <li>Deleting a book entirely from the "Books" node if no owners remain.</li>
 *     <li>Deleting a book's cover image from Firebase Storage if the book entry is deleted.</li>
 *     <li>Getting the current authenticated user via {@link FirebaseAuth}.</li>
 * </ul>
 * </p>
 * <p>
 * The fragment utilizes an {@link ActivityResultLauncher} to handle the result from
 * {@link com.example.roniproject.Activities.AddBook}, refreshing the user's book list upon successful addition.
 * </p>
 *
 * @see com.example.roniproject.Obj.User
 * @see com.example.roniproject.Obj.Book
 * @see com.example.roniproject.Obj.BookAdapter
 * @see com.example.roniproject.Activities.AddBook
 * @see FirebaseDatabase
 * @see FirebaseAuth
 * @see FirebaseStorage
 */
public class ProfileFragment extends Fragment {

    private static final int RESULT_CODE = 1; // Used with AddBook activity intent

    private TextView tvFullName, tvCity;
    private ListView personalBooksList;
    List<Book> booksList; // List of books owned by the user
    BookAdapter bookAdapter;

    private Button btnAddBook;

    private FirebaseUser currentUser;

    /**
     * Default constructor.
     * <p>
     * Required empty public constructor for Fragment instantiation.
     * </p>
     */
    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * <p>
     * This method inflates the layout for the fragment's UI.
     * </p>
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     *                           any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's
     *                           UI should be attached to. The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * <p>
     * Initializes UI components (TextViews for user info, ListView for books, Button for adding books).
     * Sets up the {@link BookAdapter} for the personal books list.
     * Fetches the current {@link FirebaseUser}. If a user is logged in, it proceeds to
     * load the user's personal books and their profile information using their UID.
     * An {@link ActivityResultLauncher} is registered to handle results from the AddBook activity.
     * Click listeners are set up for the "Add Book" button and for items in the personal books list
     * (to handle book deletion).
     * </p>
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvFullName = view.findViewById(R.id.tv_full_name);
        tvCity = view.findViewById(R.id.tv_city);
        personalBooksList = view.findViewById(R.id.personalBookList);
        btnAddBook = view.findViewById(R.id.btn_add_book);

        booksList = new ArrayList<>();
        // requireContext() is used to get a non-null Context, assuming the fragment is attached.
        bookAdapter = new BookAdapter(requireContext(), booksList);
        personalBooksList.setAdapter(bookAdapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            tvFullName.setText("No user logged in");
            tvCity.setText("");
            // Optionally disable btnAddBook and other interactive elements
            btnAddBook.setEnabled(false);
            return;
        }

        String currentUserId = currentUser.getUid();

        loadUserBooks(currentUserId);
        loadUserInfo(currentUserId);

        btnAddBook.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddBook.class);
            // Pass any necessary data to AddBook, e.g., if it needs the user ID directly.
            // intent.putExtra("userId", currentUserId); // Example
            intent.putExtra("result_code", RESULT_CODE); // For identifying the request if needed
            activityLauncher.launch(intent);
        });

        personalBooksList.setOnItemClickListener((parent, view1, position, id) -> {
            Book selectedBook = booksList.get(position);

            // Show confirmation dialog before deleting
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("מחיקת ספר") // "Delete Book"
                    .setMessage("האם את בטוחה שברצונך למחוק את הספר \"" + selectedBook.getBookName() + "\"?") // "Are you sure you want to delete the book...?"
                    .setPositiveButton("מחק", (dialog, which) -> { // "Delete"
                        deleteBookForUser(selectedBook, position);
                    })
                    .setNegativeButton("בטל", null) // "Cancel"
                    .show();
        });
    }

    /**
     * Loads the list of books owned by the specified user from Firebase Realtime Database.
     * <p>
     * It iterates through all books in the "Books" node and checks if the provided {@code userId}
     * exists as a child under the "owners" node of each book. If the user is an owner,
     * the book's Firebase key (bookKID) is set on the {@link Book} object, and the book is added
     * to the {@code booksList}. Finally, the {@code bookAdapter} is notified to refresh the ListView.
     * </p>
     *
     * @param userId The UID of the user whose books are to be loaded.
     */
    private void loadUserBooks(String userId) {
        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("Books");

        // Use addListenerForSingleValueEvent if data is needed only once per load.
        // If real-time updates to the owned books list are needed (e.g., another device adds a book),
        // consider addValueEventListener and managing its lifecycle.
        booksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                booksList.clear();
                for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                    Book book = bookSnapshot.getValue(Book.class);
                    // Check if the book object is valid and if it has an "owners" child
                    // containing the current user's ID.
                    if (book != null && bookSnapshot.child("owners").hasChild(userId)) {
                        book.setBookKID(bookSnapshot.getKey()); // Crucial: Store the book's Firebase key
                        booksList.add(book);
                    }
                }
                bookAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment", "Failed to load user books", error.toException());
                Toast.makeText(getContext(), "Failed to load your books.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Loads the profile information (full name and city) of the specified user from Firebase.
     * <p>
     * It attaches a {@link ValueEventListener} to the "Users/{userId}" path in Firebase.
     * When data is retrieved, it updates the {@code tvFullName} and {@code tvCity} TextViews.
     * If the user data is not found or an error occurs, appropriate messages are displayed.
     * </p>
     *
     * @param userId The UID of the user whose information is to be loaded.
     */
    private void loadUserInfo(String userId) {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // This listener will update UI in real-time if user info changes in Firebase.
        // If one-time load is sufficient, use addListenerForSingleValueEvent.
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    tvFullName.setText(user.getFullName());
                    tvCity.setText(user.getCity());
                } else {
                    tvFullName.setText("User data not found");
                    tvCity.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment", "Failed to load user info", error.toException());
                tvFullName.setText("Failed to load user data");
                tvCity.setText("");
            }
        });
    }

    /**
     * Deletes a book from the current user's ownership.
     * <p>
     * Removes the current user's ID from the "owners" list of the specified {@link Book} in Firebase.
     * After successful removal, it checks if any other owners remain for the book.
     * If no other owners exist, the entire book entry (from the "Books" node) and its associated
     * cover image (from Firebase Storage, if a URL exists) are deleted.
     * The local {@code booksList} and {@code bookAdapter} are updated accordingly.
     * </p>
     *
     * @param book     The {@link Book} object to be deleted from the user's ownership.
     *                 It is assumed that {@code book.getBookKID()} returns a valid Firebase key.
     * @param position The position of the book in the {@code booksList}, used for local removal.
     */
    private void deleteBookForUser(Book book, int position) {
        // currentUser should already be checked in onViewCreated, but a local check is good practice.
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in. Cannot delete book.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (book.getBookKID() == null || book.getBookKID().isEmpty()) {
            Toast.makeText(getContext(), "Error: Book ID missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String bookId = book.getBookKID();

        DatabaseReference bookOwnersRef = FirebaseDatabase.getInstance()
                .getReference("Books")
                .child(bookId)
                .child("owners");

        // Remove the user from the book's owners list
        bookOwnersRef.child(userId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "הספר נמחק", Toast.LENGTH_SHORT).show(); // "The book has been deleted"
                // Remove from local list and update adapter BEFORE checking if book needs full deletion
                // to give immediate UI feedback for the ownership removal.
                if (position >= 0 && position < booksList.size()) {
                    booksList.remove(position);
                    bookAdapter.notifyDataSetChanged();
                } else {
                    // If position is invalid, reload books to ensure consistency
                    loadUserBooks(userId);
                }


                // Check if there are any other owners left for this book
                bookOwnersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists() || !snapshot.hasChildren()) {
                            // No owners left, delete the entire book node from "Books"
                            FirebaseDatabase.getInstance().getReference("Books")
                                    .child(bookId).removeValue()
                                    .addOnSuccessListener(aVoid -> Log.d("ProfileFragment", "Book node deleted successfully."))
                                    .addOnFailureListener(e -> Log.e("ProfileFragment", "Failed to delete book node.", e));

                            // Delete the book's image from Firebase Storage if it exists
                            String imageUrl = book.getBookCoverUrl();
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                // Basic validation for Firebase Storage URL
                                if (imageUrl.startsWith("gs://") || imageUrl.startsWith("https://firebasestorage.googleapis.com/")) {
                                    com.google.firebase.storage.FirebaseStorage.getInstance()
                                            .getReferenceFromUrl(imageUrl)
                                            .delete()
                                            .addOnSuccessListener(aVoid -> Log.d("ProfileFragment", "Image deleted from Storage"))
                                            .addOnFailureListener(e -> Log.e("ProfileFragment", "Error deleting image from Storage", e));
                                } else {
                                    Log.w("ProfileFragment", "Invalid image URL format, cannot delete from Storage: " + imageUrl);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("ProfileFragment", "Error checking book owners after deletion", error.toException());
                    }
                });
            } else {
                Toast.makeText(getContext(), "שגיאה במחיקת הספר", Toast.LENGTH_SHORT).show(); // "Error deleting the book"
                Log.e("ProfileFragment", "Failed to remove user from book owners", task.getException());
            }
        });
    }


    /**
     * ActivityResultLauncher for handling results from activities started for a result.
     * <p>
     * This launcher is specifically registered to listen for results from the
     * {@link com.example.roniproject.Activities.AddBook} activity. If the result code is
     * {@link Activity#RESULT_OK}, it indicates a book was successfully added (or an operation
     * completed successfully). It then reloads the current user's book list by calling
     * {@link #loadUserBooks(String)} to reflect the changes in the UI.
     * </p>
     */
    private ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                /**
                 * Called when an activity you launched exits, giving you the requestCode you started it with,
                 * the resultCode it returned, and any additional data from it.
                 *
                 * @param result The result returned from the activity.
                 */
                @Override
                public void onActivityResult(ActivityResult result) {
                    // Check if the result is from the AddBook activity and was successful
                    // The 'RESULT_CODE' check is not strictly necessary if this launcher
                    // is only used for AddBook, but it's good practice if AddBook sets a specific code.
                    // Here, we mainly care about Activity.RESULT_OK.
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Assuming AddBook activity returns RESULT_OK upon successful book addition
                        Toast.makeText(requireContext(), "Book list updated.", Toast.LENGTH_SHORT).show();
                        if (currentUser != null) {
                            loadUserBooks(currentUser.getUid()); // Refresh the book list
                        }
                    } else {
                        // Handle other results if necessary, e.g., RESULT_CANCELED
                        // Log.d("ProfileFragment", "AddBook activity returned with code: " + result.getResultCode());
                    }
                }
            }
    );
}