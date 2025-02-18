package com.example.roniproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {

//    private static final int PICK_IMAGE_REQUEST = 1;
//    private static final int CAPTURE_IMAGE_REQUEST = 2;
//    private static final int DELETE_IMAGE_REQUEST = 3;

    private TextView tvFullName, tvCity;
    private ImageView imageProfile;
    private ListView personalBookList;
    private ArrayList<String> bookList = new ArrayList<>();

    private Button btnAddBook;
    DatabaseReference mRef;

    //adb = new AlertDialog.Builder(this);



    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvFullName = view.findViewById(R.id.tv_full_name);
        tvCity = view.findViewById(R.id.tv_city);
        imageProfile = view.findViewById(R.id.imageProfile);
        personalBookList = view.findViewById(R.id.personalBookList);
        btnAddBook=view.findViewById(R.id.btn_add_book);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, bookList);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            tvFullName.setText("No user logged in");
            tvCity.setText("");
            return;
        }

        String userId = currentUser.getUid();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users").child(userId);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user = snapshot.getValue(Users.class);
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




        /*
        btnAddBook.setOnClickListener(new View.OnClickListener(
                ) {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddBook.class);
                startActivity(intent);
            }
        });

         */



//        imageProfile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // הצגת אפשרויות לבחירת תמונה, צילום או מחיקה
//                showImagePickerOptions();
//            }
//        });

//        tvFullName.setOnClickListener(v -> {
//            // הצגת EditText למצב עריכה
//            tvFullName.setVisibility(View.GONE);
//            etFullName.setText(tvFullName.getText());
//            etFullName.setVisibility(View.VISIBLE);
//            btnSave.setVisibility(View.VISIBLE);
//        });
//
//        tvCity.setOnClickListener(v -> {
//            // הצגת EditText למצב עריכה
//            tvCity.setVisibility(View.GONE);
//            etCity.setText(tvCity.getText());
//            etCity.setVisibility(View.VISIBLE);
//            btnSave.setVisibility(View.VISIBLE);
//        });
//
//        btnSave.setOnClickListener(v -> {
//            // שמירת נתונים ל-Firebase
//            String newFullName = etFullName.getText().toString().trim();
//            String newCity = etCity.getText().toString().trim();
//
//            if (!newFullName.isEmpty() && !newCity.isEmpty()) {
//                FirebaseUser currentUser1 = FirebaseAuth.getInstance().getCurrentUser();
//                if (currentUser != null) {
//                    String userId1 = currentUser.getUid();
//                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
//
//                    userRef.child("fullName").setValue(newFullName);
//                    userRef.child("city").setValue(newCity).addOnCompleteListener(task -> {
//                        if (task.isSuccessful()) {
//                            Toast.makeText(requireContext(), "Details updated successfully!", Toast.LENGTH_SHORT).show();
//                            // עדכון תצוגה
//                            tvFullName.setText(newFullName);
//                            tvCity.setText(newCity);
//
//                            etFullName.setVisibility(View.GONE);
//                            etCity.setVisibility(View.GONE);
//                            btnSave.setVisibility(View.GONE);
//                            tvFullName.setVisibility(View.VISIBLE);
//                            tvCity.setVisibility(View.VISIBLE);
//                        } else {
//                            Toast.makeText(requireContext(), "Failed to update details.", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }
//            } else {
//                Toast.makeText(requireContext(), "Fields cannot be empty!", Toast.LENGTH_SHORT).show();
//            }
//        });//   }

    // הצגת אפשרויות לבחירת תמונה, צילום או מחיקה
//    private void showImagePickerOptions() {
//        CharSequence[] options = {"בחר תמונה", "צלם תמונה", "מחק תמונה"};
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        builder.setTitle("בחר אופציה")
//                .setItems(options, (dialog, which) -> {
//                    switch (which) {
//                        case 0:
//                            // בחר תמונה מהגלריה
//                            pickImageFromGallery();
//                            break;
//                        case 1:
//                            // צלם תמונה
//                            captureImage();
//                            break;
//                        case 2:
//                            // מחק תמונה
//                            deleteImage();
//                            break;
//                    }
//                })
//                .show();
   }
}

