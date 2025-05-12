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

public class HomeFragment extends Fragment {

    private Button btnRefresh;
    private ListView allbooksList;
    private BookAdapter bookAdapter;
    private List<Book> booksList;



    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // התחלת שירות ChatNotificationService רק אם לא הופעל כבר
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

                prefs.edit().putBoolean("serviceStarted", true).apply(); // סימון שהשירות הופעל
            }
        }


        btnRefresh = view.findViewById(R.id.btn_refresh);
        allbooksList = view.findViewById(R.id.allbooksList);

        booksList = new ArrayList<>();
        bookAdapter = new BookAdapter(requireContext(), booksList);
        allbooksList.setAdapter(bookAdapter);

        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("Books");

        booksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                booksList.clear();
                for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                    Book book = bookSnapshot.getValue(Book.class);
                    if (book != null) {
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
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               // DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("Books");


                booksRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        booksList.clear();
                        for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                            Book book = bookSnapshot.getValue(Book.class);
                            if (book != null) {
                                book.setBookKID(bookSnapshot.getKey());
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

        // הוספת לחיצה על ספר
        allbooksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Book selectedBook = booksList.get(position);
                showOwnersDialog(selectedBook);
            }
        });
    }

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

    private void openOrCreateChat(String currentUserId, String otherUserId) {
        String chatId;
        if (currentUserId.compareTo(otherUserId) < 0) {
            chatId = currentUserId + "_" + otherUserId;
        } else {
            chatId = otherUserId + "_" + currentUserId;
        }

        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("otherUserId", otherUserId);
        startActivity(intent);
    }
}
