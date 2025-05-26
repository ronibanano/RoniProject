package com.example.roniproject.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity to display search results for books.
 * <p>
 * This activity receives search criteria (search query and search type - by book name, author,
 * genre, or city) from the initiating Intent. It then queries the Firebase Realtime Database
 * for books matching the criteria.
 * </p>
 * <p>
 * The results are displayed in a ListView. Users can click on a book to see a list of its owners.
 * If the search was filtered by city, only owners in that city will be shown.
 * From the list of owners, users can select an owner to initiate a chat with them.
 * </p>
 * <p>
 * The activity also provides a "Filter" button to sort the displayed books alphabetically by name.
 * If no books match the search criteria, a "No results" message is displayed.
 * </p>
 * <p>
 * This class interacts with Firebase for:
 * <ul>
 *     <li>Fetching book data from the "Books" node.</li>
 *     <li>Fetching user data (full name, city) from the "Users" node to display owner information.</li>
 *     <li>Getting the current authenticated user ID.</li>
 * </ul>
 * </p>
 *
 * @see Book
 * @see BookAdapter
 * @see ChatActivity
 * @see FirebaseDatabase
 * @see FirebaseAuth
 */
public class SearchResultsActivity extends AppCompatActivity {

    private Button btnFilter;
    private ListView listBookResults;
    private TextView noResults;
    private BookAdapter bookAdapter;
    private List<Book> bookList = new ArrayList<>();

    /**
     * Called when the activity is first created.
     * <p>
     * Initializes UI components (ListView, Button, TextView), sets up the {@link BookAdapter},
     * retrieves search parameters (search query and type) from the incoming {@link Intent}.
     * It then fetches book data from Firebase Realtime Database based on these parameters
     * and populates the ListView. A click listener is set on the filter button to sort results,
     * and item click listeners on the ListView to show owner details.
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState(Bundle)}.
     *                           <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        btnFilter = findViewById(R.id.btnFilter);
        noResults = findViewById(R.id.tvNoResults);
        listBookResults = findViewById(R.id.listBookResults);
        bookAdapter = new BookAdapter(this, bookList);
        listBookResults.setAdapter(bookAdapter);

        Intent intent = getIntent();
        String searchUser = intent.getStringExtra("searchUser");
        String selectedOption = intent.getStringExtra("selectedOption");
        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("Books");

        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // מיין את הרשימה לפי שם הספר
                Collections.sort(bookList, new Comparator<Book>() {
                    @Override
                    public int compare(Book b1, Book b2) {
                        return b1.getBookName().compareToIgnoreCase(b2.getBookName());
                    }
                });

                // עדכן את ה-Adapter עם הסדר החדש
                bookAdapter.notifyDataSetChanged();
            }
        });

        if (selectedOption != null&&(selectedOption.equals("book name") || selectedOption.equals("author") || selectedOption.equals("genre"))) {

            booksRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    bookList.clear();

                    for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                        Book book = bookSnapshot.getValue(Book.class);
                        if (book != null && searchUser != null) {
                            String searchLower = searchUser.trim().toLowerCase();

                            switch (selectedOption) {
                                case "book name":
                                    if (book.getBookName() != null && book.getBookName().toLowerCase().contains(searchLower)) {
                                        bookList.add(book);
                                    }
                                    break;
                                case "author":
                                    if (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(searchLower)) {
                                        bookList.add(book);
                                    }
                                    break;
                                case "genre":
                                    if (book.getGenre() != null && book.getGenre().toLowerCase().contains(searchLower)) {
                                        bookList.add(book);
                                    }
                                    break;
                            }
                        }

                    }

                    bookAdapter.notifyDataSetChanged();

                    if (bookList.isEmpty()) {
                        btnFilter.setVisibility(View.GONE);
                        noResults.setVisibility(View.VISIBLE);
                    } else {
                        noResults.setVisibility(View.GONE);
                    }

                    listBookResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Book selectedBook = bookList.get(position);

                            showOwnersDialog(selectedBook);
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else{

            booksRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    bookList.clear();

                    for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                        Book book = bookSnapshot.getValue(Book.class);
                        if (book == null) continue;

                        Map<String, Boolean> cities = book.getCities();
                        if (cities != null && cities.containsKey(searchUser.trim())) {
                            bookList.add(book);
                        }
                    }

                    bookAdapter.notifyDataSetChanged();

                    if (bookList.isEmpty()) {
                        btnFilter.setVisibility(View.GONE);
                        noResults.setVisibility(View.VISIBLE);
                    } else {
                        noResults.setVisibility(View.GONE);
                    }

                    listBookResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Book selectedBook = bookList.get(position);

                            showOwnersByCityDialog(selectedBook,searchUser);
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // טיפול בשגיאה
                }
            });

        }

    }

    /**
     * Displays a dialog listing the owners of a specific book who are located in a given city.
     * <p>
     * Fetches the list of owners for the given {@link Book} from Firebase. Then, for each owner,
     * it retrieves their full name and city from the "Users" node in Firebase.
     * Only owners residing in the {@code cityFilter} and who are not the current user are displayed.
     * Selecting an owner from the dialog initiates a chat with them via {@link #openOrCreateChat(String, String)}.
     * </p>
     *
     * @param book       The book for which to display owners.
     * @param cityFilter The city to filter owners by. Only owners in this city will be shown.
     */
    private void showOwnersByCityDialog(Book book, String cityFilter) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String currentUserId = currentUser.getUid();

        DatabaseReference ownersRef = FirebaseDatabase.getInstance().getReference("Books").child(book.getBookKID()).child("owners");

        ownersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ownersSnapshot) {
                List<String> userIds = new ArrayList<>();

                for (DataSnapshot owner : ownersSnapshot.getChildren()) {
                    String ownerId = owner.getKey();
                    if (!ownerId.equals(currentUserId)) {
                        userIds.add(ownerId);
                    }
                }

                if (userIds.isEmpty()) {
                    Toast.makeText(SearchResultsActivity.this, "אין בעלי ספר אחרים", Toast.LENGTH_SHORT).show();
                    return;
                }

                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot usersSnapshot) {
                        List<String> displayNames = new ArrayList<>();
                        List<String> filteredUserIds = new ArrayList<>();

                        for (String userId : userIds) {
                            DataSnapshot userSnap = usersSnapshot.child(userId);
                            String name = userSnap.child("fullName").getValue(String.class);
                            String city = userSnap.child("city").getValue(String.class);

                            if (city != null && city.equalsIgnoreCase(cityFilter)) {
                                filteredUserIds.add(userId);
                                if (name != null) {
                                    displayNames.add(name + " - " + city);
                                } else {
                                    displayNames.add(userId);
                                }
                            }
                        }

                        if (filteredUserIds.isEmpty()) {
                            Toast.makeText(SearchResultsActivity.this, "אין בעלי ספר בעיר זו", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(SearchResultsActivity.this);
                        builder.setTitle("בחר משתמש להתחלת שיחה");
                        builder.setItems(displayNames.toArray(new String[0]), (dialog, which) -> {
                            String selectedUserId = filteredUserIds.get(which);
                            openOrCreateChat(currentUserId, selectedUserId);
                        });
                        builder.show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SearchResultsActivity.this, "שגיאה בטעינת המשתמשים", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SearchResultsActivity.this, "שגיאה בגישה לבעלים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Displays a dialog listing all owners of a specific book, excluding the current user.
     * <p>
     * Fetches the list of owners for the given {@link Book} from Firebase. Then, for each owner,
     * it retrieves their full name and city from the "Users" node in Firebase.
     * Selecting an owner from the dialog initiates a chat with them via {@link #openOrCreateChat(String, String)}.
     * </p>
     *
     * @param book The book for which to display owners.
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
                    Toast.makeText(SearchResultsActivity.this, "אין בעלי ספר אחרים", Toast.LENGTH_SHORT).show();
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(SearchResultsActivity.this);
                        builder.setTitle("בחר משתמש להתחלת שיחה");
                        builder.setItems(displayNames.toArray(new String[0]), new DialogInterface.OnClickListener() {
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
                        Toast.makeText(SearchResultsActivity.this, "שגיאה בטעינת המשתמשים", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SearchResultsActivity.this, "שגיאה בגישה לבעלים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Creates a unique chat ID and starts the {@link ChatActivity} between the current user and another user.
     * <p>
     * The chat ID is generated by concatenating the two user IDs in a consistent order
     * (lexicographically smaller ID first) to ensure uniqueness for each pair of users.
     * The {@link ChatActivity} is then launched with the chat ID and the other user's ID.
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

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("otherUserId", otherUserId); // Pass the other user's ID to ChatActivity
        startActivity(intent);
    }
}