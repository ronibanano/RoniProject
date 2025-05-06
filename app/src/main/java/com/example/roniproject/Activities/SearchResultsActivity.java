package com.example.roniproject.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.roniproject.Obj.Book;
import com.example.roniproject.Obj.BookAdapter;
import com.example.roniproject.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchResultsActivity extends AppCompatActivity {

    private ListView listBookResults;
    private TextView noResults;
    private BookAdapter bookAdapter;
    private List<Book> bookList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        noResults = findViewById(R.id.tvNoResults);
        listBookResults = findViewById(R.id.listBookResults);
        bookAdapter = new BookAdapter(this, bookList);
        listBookResults.setAdapter(bookAdapter);

        Intent intent = getIntent();
        String bookName = intent.getStringExtra("bookName");
        String author = intent.getStringExtra("author");
        String genre = intent.getStringExtra("genre");
        String location = intent.getStringExtra("location");

        // ניקוי הערכים מרווחים ואותיות גדולות
        if (bookName != null) bookName = bookName.trim().toLowerCase();
        if (author != null) author = author.trim().toLowerCase();
        if (genre != null) genre = genre.trim().toLowerCase();
        if (location != null) location = location.trim().toLowerCase();

        Log.d("SearchDebug", "location שהוזן: " + location);


        searchBooks(bookName, author, genre, location);

    }

    private void searchBooks(String bookName, String author, String genre, String location) {
       // DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("Books");
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // שליפת כל המשתמשים מראש ושמירת העיר שלהם במפה
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Map<String, String> userCities = new HashMap<>();

                // שמירת המידע על כל המשתמשים במפה (userID -> city)
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userID = userSnapshot.getKey();
                    Log.d("SearchDebug", "userID: " + userID);
                    if(userSnapshot.child("city").exists()){
                        String city = userSnapshot.child("city").getValue(String.class);
                        Log.d("SearchDebug", "userID: " + userID + ", city: [" + city + "]");
                    }else{
                        Log.w("SearchDebug", "userID: " + userID + " - city not found");
                    }
                    String city = userSnapshot.child("city").getValue(String.class);

                    // הוספת הלוג כאן לראות מה נישלח מה־Firebase
                    Log.d("SearchDebug", "userID: " + userID + ", city: [" + city + "]");

                    if (city != null) {
                        userCities.put(userID, city.trim().toLowerCase()); // שמירת העיר באותיות קטנות להשוואה נוחה
                    }
                }

                if(location != null){
                    // כעת יש לנו את כל המשתמשים בזיכרון, אפשר להתחיל לעבור על הספרים
                    fetchBooks(bookName, author, genre, location, userCities);
                }
                else {
                    noLocation(bookName, author, genre);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SearchDebug", "Firebase error: " + error.getMessage());
            }
        });


    }

    // פונקציה לשליפת ספרים לאחר שיש לנו את המידע על המשתמשים
    private void fetchBooks(String bookName, String author, String genre, String location, Map<String, String> userCities) {
        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("Books");

        // אוסף את כל ה-UIDs של משתמשים שגרים בעיר שהוזנה
        List<String> matchingUserIDs = new ArrayList<>();
        if (location != null && !location.trim().isEmpty()) {
            String normalizedLocation = location.trim().toLowerCase();
            for (Map.Entry<String, String> entry : userCities.entrySet()) {
                String city = entry.getValue();

                Log.d("SearchDebug", "עיר שהוזנה עבור משתמש: " + city);

                if (city != null && city.trim().equalsIgnoreCase(normalizedLocation)) {
                    matchingUserIDs.add(entry.getKey());
                }
            }
            Log.d("SearchDebug", "matchingUserIDs: " + matchingUserIDs);
        } else {
            // אם לא הוזנה עיר, נכלול את כל המשתמשים
            matchingUserIDs.addAll(userCities.keySet());
        }

        booksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                List<Book> tempBookList = new ArrayList<>();//ספרים שהערים תואמים לחיפוש

                for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                    Book book = bookSnapshot.getValue(Book.class);

                    if (book == null) continue;

                    Map<String, Boolean> ownersMap = book.getOwners();
                    if (ownersMap == null || ownersMap.isEmpty()) continue;

                    for (String ownerID : ownersMap.keySet()) {
                        if (matchingUserIDs.contains(ownerID)) {
                            Log.d("SearchDebug", "הוספתי ספר: " + book.getBookName());
                            // אם נמצא בעל תואם, הוסף את הספר לרשימת התוצאות
                            tempBookList.add(book);
                            break; // אין צורך לבדוק יותר, המשך לספר הבא
                        }
                    }

                }

                Log.d("SearchDebug", "ספרים מתאימים למיקום: " + tempBookList);

                haveLocation(tempBookList,bookName,author,genre);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        booksRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                bookList.clear();
//                List<Book> tempBookList = new ArrayList<>();
//
//                for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
//                    Book book = bookSnapshot.getValue(Book.class);
//                    Map<String, Boolean> ownersMap = book.getOwners();
//
//                    if (ownersMap != null && !ownersMap.isEmpty()) {
//                        for (String ownerID : ownersMap.keySet()) {
//                            String userCity = userCities.get(ownerID); // קבלת העיר ממפת המשתמשים
//
//                            // בדיקת התאמה לקריטריונים
//                            if ((bookName.isEmpty() || book.getBookName().toLowerCase().contains(bookName.toLowerCase())) &&
//                                    (author.isEmpty() || book.getAuthor().toLowerCase().contains(author.toLowerCase())) &&
//                                    (genre.isEmpty() || book.getGenre().toLowerCase().contains(genre.toLowerCase())) &&
//                                    (location.isEmpty() || (userCity != null && userCity.trim().equalsIgnoreCase(location.trim())))) {
//
////                                Log.d("SearchResults", "User entered city: " + location.toLowerCase());
////                                Log.d("SearchResults", "City from database: " + userCity.toLowerCase());
//
////                                if (userCity != null && userCity.trim().equalsIgnoreCase(location.trim())) {
////                                    Log.d("SearchResults", "City match found!");
////                                } else {
////                                    Log.d("SearchResults", "No match for city.");
////                                }
//
//                                if (!tempBookList.contains(book)) { // הימנעות מכפילויות
//                                    tempBookList.add(book);
//                                }
//                            }
//                        }
//                    }
//                }
//
//                // עדכון הרשימה והצגת הנתונים
//                bookList.clear();
//                bookList.addAll(tempBookList);
//                bookAdapter.notifyDataSetChanged();
//
//                if (tempBookList.isEmpty()) {
//                    noResults.setVisibility(View.VISIBLE);
//                } else {
//                    noResults.setVisibility(View.GONE);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) { }
//        });
    }

    private void noLocation(String bookName, String author, String genre) {
        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("Books");

        booksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookList.clear();
                List<Book> tempBookList = new ArrayList<>();

                for (DataSnapshot bookSnapshot : snapshot.getChildren()){
                    Book book = bookSnapshot.getValue(Book.class);

                    if ((bookName.isEmpty() || book.getBookName().toLowerCase().contains(bookName.toLowerCase())) &&
                            (author.isEmpty() || book.getAuthor().toLowerCase().contains(author.toLowerCase())) &&
                            (genre.isEmpty() || book.getGenre().toLowerCase().contains(genre.toLowerCase()))){

                        if (!tempBookList.contains(book)) { // הימנעות מכפילויות
                            tempBookList.add(book);
                        }
                    }
                }

                bookList.clear();
                bookList.addAll(tempBookList);
                bookAdapter.notifyDataSetChanged();

                if (tempBookList.isEmpty()) {
                    noResults.setVisibility(View.VISIBLE);
                } else {
                    noResults.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void haveLocation(List<Book> tempBookList, String bookName, String author, String genre) {
        List<Book> filteredBooks = new ArrayList<>();

        for (Book book : tempBookList) {
            Log.d("SearchDebug", "בודק ספר: " + book.getBookName());
            boolean matches =
                    (bookName == null || bookName.isEmpty() || book.getBookName().toLowerCase().contains(bookName.toLowerCase())) &&
                            (author == null || author.isEmpty() || book.getAuthor().toLowerCase().contains(author.toLowerCase())) &&
                            (genre == null || genre.isEmpty() || book.getGenre().toLowerCase().contains(genre.toLowerCase()));

            if (matches) {
                Log.d("SearchDebug", "הספר עבר את הפילטרים!");
                filteredBooks.add(book);
            }
        }

        Log.d("SearchDebug", "ספרים שעברו את הפילטר: " + filteredBooks);


        bookList.clear();
        bookList.addAll(filteredBooks);
        bookAdapter.notifyDataSetChanged();

        if (filteredBooks.isEmpty()) {
            noResults.setVisibility(View.VISIBLE);
        } else {
            noResults.setVisibility(View.GONE);
        }

    }

}
