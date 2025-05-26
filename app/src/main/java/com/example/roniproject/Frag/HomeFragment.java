package com.example.roniproject.Frag;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.roniproject.Activities.ChatActivity;
import com.example.roniproject.Obj.Book;
import com.example.roniproject.Obj.BookAdapter;
import com.example.roniproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment responsible for displaying a list of all available books in the application.
 * <p>
 * This fragment fetches book data from the Firebase Realtime Database and displays it in a
 * {@link ListView} using a custom {@link BookAdapter}. It provides a "Refresh" button
 * to manually reload the list of books.
 * </p>
 * <p>
 * When a user clicks on an item (a book) in the list, a dialog is displayed showing a list
 * of users who own that particular book (excluding the current user). From this dialog,
 * the current user can select another user to initiate a chat with them, which then navigates
 * to the {@link com.example.roniproject.Activities.ChatActivity}.
 * </p>
 * <p>
 * The fragment also handles the initialization of a background service,
 * {@link com.example.roniproject.ChatNotificationService}, if it hasn't been started already.
 * This service is responsible for listening to new chat messages and displaying notifications.
 * The fragment uses {@link SharedPreferences} to track whether the service has been started
 * to prevent multiple initializations.
 * </p>
 * <p>
 * Interactions with Firebase include:
 * <ul>
 *     <li>Fetching all books from the "Books" node.</li>
 *     <li>Fetching owner IDs for a selected book from its "owners" child node.</li>
 *     <li>Fetching user details (full name, city) from the "Users" node to display owner information.</li>
 *     <li>Getting the current authenticated user via {@link FirebaseAuth}.</li>
 * </ul>
 * </p>
 *
 * @see com.example.roniproject.Obj.Book
 * @see com.example.roniproject.Obj.BookAdapter
 * @see com.example.roniproject.Activities.ChatActivity
 * @see com.example.roniproject.ChatNotificationService
 * @see FirebaseDatabase
 * @see FirebaseAuth
 */
public class HomeFragment extends Fragment {

    private Button btnRefresh;
    private ListView allbooksList;
    private BookAdapter bookAdapter;
    private List<Book> booksList;

    /**
     * Default constructor.
     * <p>
     * Required empty public constructor for Fragment instantiation.
     * </p>
     */
    public HomeFragment() {
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
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * <p>
     * This method initializes UI components (ListView, Button), sets up the {@link BookAdapter},
     * and fetches the initial list of books from Firebase Realtime Database.
     * It also checks if the {@link com.example.roniproject.ChatNotificationService} needs to be started
     * and initiates it if necessary, using {@link SharedPreferences} to ensure it's started only once.
     * Listeners are set for the refresh button and for item clicks on the book list.
     * </p>
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Start ChatNotificationService if not already started
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            SharedPreferences prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            boolean serviceStarted = prefs.getBoolean("serviceStarted", false);

            if (!serviceStarted) {
                Intent serviceIntent = new Intent(requireContext(), com.example.roniproject.ChatNotificationService.class);
                serviceIntent.putExtra("userId", currentUser.getUid());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    requireContext().startForegroundService(serviceIntent);
                } else {
                    requireContext().startService(serviceIntent);
                }
                prefs.edit().putBoolean("serviceStarted", true).apply();
            }
        }

        btnRefresh = view.findViewById(R.id.btn_refresh);
        allbooksList = view.findViewById(R.id.allbooksList);

        booksList = new ArrayList<>();
        // requireContext() is used to get a non-null Context, assuming the fragment is attached.
        bookAdapter = new BookAdapter(requireContext(), booksList);
        allbooksList.setAdapter(bookAdapter);

        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("Books");

        // Initial load of books and subsequent updates
        booksRef.addValueEventListener(new ValueEventListener() {
            /**
             * This method will be called with a snapshot of the data at this location.
             * It will also be called each time that data changes.
             * <p>
             * Clears the current {@code booksList} and repopulates it with {@link Book} objects
             * retrieved from the Firebase data snapshot. Notifies the {@code bookAdapter}
             * of the data change to update the UI.
             * </p>
             * @param snapshot The current data at the "Books" location.
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                booksList.clear();
                for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                    Book book = bookSnapshot.getValue(Book.class);
                    if (book != null) {
                        // Ensure bookKID is set, as it might not be part of the Book object by default
                        // if not explicitly saved. However, standard Firebase deserialization
                        // usually doesn't include the key in the object itself unless handled.
                        // Here, it seems the key is set later or within showOwnersDialog.
                        // For consistency in the list, if the key is vital for other operations
                        // initiated directly from this list (not just the dialog), it should be set here.
                        // The refresh listener explicitly sets it.
                        booksList.add(book);
                    }
                }
                bookAdapter.notifyDataSetChanged();
            }

            /**
             * This method will be triggered in the event that this listener either failed at the server,
             * or is removed as a result of the security and Firebase Database rules.
             * <p>
             * Displays a Toast message indicating failure to load books.
             * </p>
             * @param error A description of the error that occurred.
             */
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load books", Toast.LENGTH_SHORT).show();
            }
        });

        // Listener for the refresh button
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when the refresh button has been clicked.
             * <p>
             * Re-fetches the list of books from Firebase by adding a new ValueEventListener
             * to the "Books" reference. This explicitly fetches the data again.
             * Importantly, this listener also sets the {@code bookKID} for each book object,
             * which is the key of the book in the Firebase database.
             * </p>
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                // The booksRef is already defined above, can be reused.
                booksRef.addValueEventListener(new ValueEventListener() { // Or use get() for a one-time fetch
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        booksList.clear();
                        for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                            Book book = bookSnapshot.getValue(Book.class);
                            if (book != null) {
                                book.setBookKID(bookSnapshot.getKey()); // Set the book's key
                                booksList.add(book);
                            }
                        }
                        bookAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(requireContext(), "Failed to load books", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Listener for clicks on individual books in the list
        allbooksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * Callback method to be invoked when an item in this AdapterView has been clicked.
             * <p>
             * Retrieves the selected {@link Book} object from the list and calls
             * to display a dialog with its owners.
             * </p>
             * @param parent The AdapterView where the click happened.
             * @param view The view within the AdapterView that was clicked.
             * @param position The position of the view in the adapter.
             * @param id The row id of the item that was clicked.
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Book selectedBook = booksList.get(position);
                // Ensure the bookKID is set before showing the dialog if it wasn't set earlier.
                // The refresh button's listener sets it. If the initial load doesn't,
                // there might be an issue here if selectedBook.getBookKID() is null.
                // It's crucial that selectedBook.getBookKID() returns a valid key.
                if (selectedBook.getBookKID() == null || selectedBook.getBookKID().isEmpty()) {
                    // Attempt to find the key. This is a fallback and ideally the key should
                    // be consistently set when the book object is created/retrieved.
                    // This scenario is less likely if the refresh listener is the primary way
                    // detailed book objects (with keys) are populated.
                    // A better approach is to ensure bookKID is always populated.
                    // For now, proceeding with what's available.
                    Toast.makeText(requireContext(), "Book key missing. Please refresh.", Toast.LENGTH_SHORT).show();
                    return;
                }
                showOwnersDialog(selectedBook);
            }
        });
    }

    /**
     * Displays an AlertDialog listing the users who own the specified book, excluding the current user.
     * <p>
     * Fetches the owner IDs from the "Books/{bookKID}/owners" path in Firebase. Then, for each
     * owner ID (that is not the current user), it retrieves the corresponding user's full name
     * and city from the "Users" node. These details are displayed in the dialog.
     * If a user is selected from the dialog, {@link #openOrCreateChat(String, String)} is called
     * to start a chat with that user.
     * </p>
     *
     * @param book The {@link Book} object for which to display owners. It's assumed that
     *             {@code book.getBookKID()} returns a valid Firebase key for the book.
     */
    private void showOwnersDialog(Book book) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String currentUserId = currentUser.getUid();

        DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference("Books").child(book.getBookKID()).child("owners");

        bookRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ownersSnapshot) {
                List<String> userIds = new ArrayList<>();
                List<String> displayNames = new ArrayList<>();

                for (DataSnapshot owner : ownersSnapshot.getChildren()) {
                    String ownerId = owner.getKey();
                    if (!ownerId.equals(currentUserId)) {
                        userIds.add(ownerId);
                    }
                }

                if (userIds.isEmpty()) {
                    Toast.makeText(requireContext(), "אין בעלי ספר אחרים", Toast.LENGTH_SHORT).show();
                    return;
                }

                // עכשיו נמשוך את השמות והערים מה־Users
                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot usersSnapshot) {
                        displayNames.clear();
                        for (String userId : userIds) {
                            DataSnapshot userSnap = usersSnapshot.child(userId);
                            String name = userSnap.child("fullName").getValue(String.class);
                            String city = userSnap.child("city").getValue(String.class);
                            if (name != null && city != null) {
                                displayNames.add(name + " - " + city);
                            } else {
                                displayNames.add(userId); // fallback
                            }
                        }

                        // הצגת דיאלוג עם הרשימה
                        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                        builder.setTitle("בחר משתמש להתחלת שיחה");
                        builder.setItems(displayNames.toArray(new String[0]), new DialogInterface.OnClickListener() {
                            /**
                             * This method will be invoked when a button in the dialog is clicked.
                             * @param dialog The dialog that received the click.
                             * @param which The button that was clicked (the index of the item).
                             */
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String selectedUserId = userIds.get(which);
                                openOrCreateChat(currentUserId, selectedUserId);
                            }
                        });
                        builder.show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(requireContext(), "שגיאה בטעינת המשתמשים", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "שגיאה בגישה לבעלים", Toast.LENGTH_SHORT).show();
            }
        });
    }



    /**
     * Creates a unique chat ID and starts the {@link com.example.roniproject.Activities.ChatActivity}
     * between the current user and another user.
     * <p>
     * The chat ID is generated by concatenating the two user IDs in a consistent lexicographical order
     * (smaller ID first, then underscore, then larger ID) to ensure uniqueness for each pair of users.
     * The {@link com.example.roniproject.Activities.ChatActivity} is then launched with the generated
     * chat ID and the other user's ID passed as extras in the Intent.
     * </p>
     *
     * @param currentUserId The ID of the currently logged-in user.
     * @param otherUserId   The ID of the user with whom to start or open a chat.
     */
    private void openOrCreateChat(String currentUserId, String otherUserId) {
        String chatId;
        // Ensure consistent chatId by ordering user IDs lexicographically
        if (currentUserId.compareTo(otherUserId) < 0) {
            chatId = currentUserId + "_" + otherUserId;
        } else {
            chatId = otherUserId + "_" + currentUserId;
        }

        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("otherUserId", otherUserId); // Pass the other user's ID to ChatActivity
        startActivity(intent);
    }
}
