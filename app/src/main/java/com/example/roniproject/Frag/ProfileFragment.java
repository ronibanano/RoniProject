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

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private static final int RESULT_CODE = 1;

    private TextView tvFullName, tvCity;
    private ImageView imageProfile;
    private ListView personalBooksList;
    List<Book> booksList;
    BookAdapter bookAdapter;

    private Button btnAddBook;

    private FirebaseUser currentUser;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvFullName = view.findViewById(R.id.tv_full_name);
        tvCity = view.findViewById(R.id.tv_city);
        imageProfile = view.findViewById(R.id.imageProfile);
        personalBooksList = view.findViewById(R.id.personalBookList);
        btnAddBook = view.findViewById(R.id.btn_add_book);

        booksList = new ArrayList<>();
        bookAdapter = new BookAdapter(requireContext(), booksList);
        personalBooksList.setAdapter(bookAdapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            tvFullName.setText("No user logged in");
            tvCity.setText("");
            return;
        }

        String currentUserId = currentUser.getUid();

        loadUserBooks(currentUserId);
        loadUserInfo(currentUserId);

        btnAddBook.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddBook.class);
            intent.putExtra("result_code", RESULT_CODE);
            activityLauncher.launch(intent);
        });

        personalBooksList.setOnItemClickListener((parent, view1, position, id) -> {
            Book selectedBook = booksList.get(position);

            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("מחיקת ספר")
                    .setMessage("האם את בטוחה שברצונך למחוק את הספר \"" + selectedBook.getBookName() + "\"?")
                    .setPositiveButton("מחק", (dialog, which) -> {
                        deleteBookForUser(selectedBook, position);
                    })
                    .setNegativeButton("בטל", null)
                    .show();
        });
    }

    private void loadUserBooks(String userId) {
        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("Books");

        booksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                booksList.clear();
                for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                    Book book = bookSnapshot.getValue(Book.class);
                    if (book != null && bookSnapshot.child("owners").hasChild(userId)) {
                        book.setBookKID(bookSnapshot.getKey()); // חשוב: שומר את ה-bookID
                        booksList.add(book);
                    }
                }
                bookAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment", "Failed to load books", error.toException());
            }
        });
    }

    private void loadUserInfo(String userId) {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    tvFullName.setText(user.getFullName());
                    tvCity.setText(user.getCity());
                } else {
                    tvFullName.setText("User not found");
                    tvCity.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvFullName.setText("Failed to load user data");
                tvCity.setText("");
            }
        });
    }

    private void deleteBookForUser(Book book, int position) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        String bookId = book.getBookKID();

        DatabaseReference bookOwnersRef = FirebaseDatabase.getInstance()
                .getReference("Books")
                .child(bookId)
                .child("owners");

        bookOwnersRef.child(userId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "הספר נמחק", Toast.LENGTH_SHORT).show();
                booksList.remove(position);
                bookAdapter.notifyDataSetChanged();

                // לבדוק אם נשארו בעלים נוספים לספר
                bookOwnersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            // אין בעלים נוספים - מחיקת הספר כולו
                            FirebaseDatabase.getInstance().getReference("Books")
                                    .child(bookId).removeValue();

                            // מחיקת התמונה מ־Storage אם קיימת
                            String imageUrl = book.getBookCoverUrl(); // נניח שזה שדה URL של התמונה
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                com.google.firebase.storage.FirebaseStorage.getInstance()
                                        .getReferenceFromUrl(imageUrl)
                                        .delete()
                                        .addOnSuccessListener(aVoid -> Log.d("ProfileFragment", "התמונה נמחקה מה-Storage"))
                                        .addOnFailureListener(e -> Log.e("ProfileFragment", "שגיאה במחיקת תמונה", e));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("ProfileFragment", "שגיאה בבדיקת בעלים", error.toException());
                    }
                });
            } else {
                Toast.makeText(getContext(), "שגיאה במחיקת הספר", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Toast.makeText(requireContext(), "succses", Toast.LENGTH_SHORT).show();
                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        loadUserBooks(currentUserId);
                    }
                }
            }
    );
}
