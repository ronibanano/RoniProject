package com.example.roniproject.Activities;

import static com.example.roniproject.FBRef.refBooks;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.roniproject.Obj.Book;
import com.example.roniproject.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddBook extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;
    boolean chooseGallery = false;
    boolean chooseCamera = false;


    private EditText etBookName, etAuthorName, etGenre;
    private ImageView ivBookCover;
    private Uri imageUri;
    private Button btnAddBook;
    AlertDialog.Builder adb;

    private DatabaseReference booksReference;
    private StorageReference storageReference;
    private FirebaseUser currentUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        etBookName = findViewById(R.id.etBookName);
        etAuthorName = findViewById(R.id.etAuthorName);
        etGenre = findViewById(R.id.etGenre);
        ivBookCover = findViewById(R.id.ivBookCover);
        btnAddBook = findViewById(R.id.btnAddBook);
        adb = new AlertDialog.Builder(this);

        booksReference = FirebaseDatabase.getInstance().getReference("Books");
        storageReference = FirebaseStorage.getInstance().getReference("BookCovers");
        currentUser= FirebaseAuth.getInstance().getCurrentUser();

        btnAddBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBook();
            }
        });
    }

    public void takeStamp(View view) {
        // בדיקת הרשאות מצלמה
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            // הרשאה כבר קיימת - הפעלת המצלמה
            Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePicIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePicIntent, REQUEST_CAMERA);
            }
        }
    }

    public void gallery(View view) {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_GALLERY);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == REQUEST_CAMERA) {
                chooseCamera = true;
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                imageUri=getImageUri(imageBitmap);
                ivBookCover.setImageBitmap(imageBitmap);
            } else if (requestCode == REQUEST_GALLERY) {
                chooseGallery = true;
                imageUri = data.getData();
                ivBookCover.setImageURI(imageUri);
            }
        }

        if (requestCode == REQUEST_CAMERA) {
            chooseCamera = true;
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            imageUri = getImageUri(imageBitmap);
            if (imageUri != null) {
                ivBookCover.setImageBitmap(imageBitmap);
            } else {
                Toast.makeText(this, "Failed to get image URI", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private Uri getImageUri(Bitmap bitmap) {
        try {
            File tempFile = File.createTempFile("book_image", ".jpg", getCacheDir());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            byte[] bitmapData = out.toByteArray();

            java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile);
            fos.write(bitmapData);
            fos.flush();
            fos.close();

            return Uri.fromFile(tempFile);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show();
            return null;
        }
    }



//    private Uri getImageUri(Bitmap bitmap) {
//        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
//        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "book_image", null);
//        return Uri.parse(path);
//    }

    public void checkBook() {
        String bookName = etBookName.getText().toString().trim();
        String author = etAuthorName.getText().toString().trim();
        String genre = etGenre.getText().toString().trim();

        if (bookName.isEmpty() || author.isEmpty() || genre.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userID = currentUser.getUid();
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users").child(userID);

        userReference.get().addOnSuccessListener(userSnapshot -> {
            if (userSnapshot.exists()) {
                String cityUser = userSnapshot.child("city").getValue(String.class);

                booksReference.orderByChild("bookName").equalTo(bookName).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        for (DataSnapshot bookSnapshot : task.getResult().getChildren()) {
                            Book existingBook = bookSnapshot.getValue(Book.class);
                            if (existingBook != null && existingBook.getAuthor().equals(author) && existingBook.getGenre().equals(genre)) {
                                DatabaseReference existingBookRef = bookSnapshot.getRef();

                                // הוספת המשתמש לרשימת הבעלים
                                existingBookRef.child("owners").child(userID).setValue(true);

                                // בדיקה והוספה של העיר
                                existingBookRef.child("cities").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot citySnapshot) {
                                        if (!citySnapshot.hasChild(cityUser)) {
                                            existingBookRef.child("cities").child(cityUser).setValue(true);
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(AddBook.this, "Error checking cities", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                Toast.makeText(AddBook.this, "הספר כבר קיים - נוסף לבעלים", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                                return;
                            }
                        }

                        // לא נמצא ספר עם אותו מחבר וז׳אנר
                        saveBookToFirebase();
                    } else {
                        // הספר לא קיים בכלל
                        saveBookToFirebase();
                    }
                });
            } else {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
        });
    }

    public void saveBookToFirebase() {

        String bookName = etBookName.getText().toString().trim();
        String author = etAuthorName.getText().toString().trim();
        String genre = etGenre.getText().toString().trim();

        if (bookName.isEmpty() || author.isEmpty() || genre.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null) {
            Toast.makeText(this, "משתמש לא מחובר", Toast.LENGTH_SHORT).show();
            return;
        }

        String bookKID = booksReference.push().getKey();
        String userID = currentUser.getUid();

        Book book = new Book(bookKID, bookName, author, genre, "");

        DatabaseReference bookRef = booksReference.child(bookKID);

        // קודם מוסיפים את פרטי הספר
        bookRef.setValue(book).addOnSuccessListener(unused -> {

            // מוסיפים את המשתמש לרשימת הבעלים
            bookRef.child("owners").child(userID).setValue(true);

            // שולפים את עיר המשתמש
            FirebaseDatabase.getInstance().getReference("Users").child(userID)
                    .child("city").get().addOnSuccessListener(citySnapshot -> {

                        String city = citySnapshot.getValue(String.class);
                        if (city != null && !city.isEmpty()) {
                            // מוסיפים את העיר לרשימת הערים
                            bookRef.child("cities").child(city).setValue(true);
                        }

                        // מעלים תמונה אם יש
                        if (chooseGallery || chooseCamera) {
                            uploadImage(bookName, bookKID);
                        } else {
                            Toast.makeText(AddBook.this, "Book added without image", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        }

                    }).addOnFailureListener(e -> {
                        Toast.makeText(AddBook.this, "נכשל בקריאת עיר המשתמש", Toast.LENGTH_SHORT).show();
                    });

        }).addOnFailureListener(e -> {
            Toast.makeText(AddBook.this, "Failed to add book", Toast.LENGTH_SHORT).show();
        });
    }


    private void uploadImage(String bookName,String bookKID) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference imageRef = storageReference.child(bookKID).child(uid + ".jpg");
//        StorageReference imageRef = storageReference.child(bookKID + ".jpg");
//        StorageReference imageRef = storageReference.child(bookName + ".jpg");

        ProgressDialog pd = ProgressDialog.show(this, "Uploading Image", "Please wait...", true);
        imageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                booksReference.child(bookKID).child("bookCoverUrl").setValue(uri.toString());
                                pd.dismiss();
                                Toast.makeText(AddBook.this, "Book added successfully", Toast.LENGTH_SHORT).show();

                                setResult(RESULT_OK);
                                finish();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddBook.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takeStamp(null); // נקרא שוב למתודה אחרי שהמשתמש אישר
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }


}