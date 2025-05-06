package com.example.roniproject.Activities;

import static com.example.roniproject.FBRef.refBooks;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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

import com.example.roniproject.Obj.Book;
import com.example.roniproject.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

//    private String lastStamp, lastGallery;
//    private String currentPath;
//    private StorageReference refImg;
//    private File localFile;
//    private static final int REQUEST_STAMP_CAPTURE = 201;
//    private static final int REQUEST_PICK_IMAGE = 301;

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
        Intent takePicIntent = new Intent();
        takePicIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePicIntent, REQUEST_CAMERA);
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
                //ByteArrayOutputStream baos = new ByteArrayOutputStream();
                //imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                //byte[] dataBytes = baos.toByteArray();
//                String s = null;
//                try {
//                    s = new String(dataBytes, "UTF-8");
//                } catch (UnsupportedEncodingException e) {
//                    throw new RuntimeException(e);
//                }
//                imageUri = Uri.parse(s);
                imageUri=getImageUri(imageBitmap);
                ivBookCover.setImageBitmap(imageBitmap);
            } else if (requestCode == REQUEST_GALLERY) {
                chooseGallery = true;
                imageUri = data.getData();
                ivBookCover.setImageURI(imageUri);
            }
        }
    }

//    private Uri getImageUri(byte[] dataBytes) {
//        // Placeholder function to create Uri from byte array if needed.
//        return null;
//    }

    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "book_image", null);
        return Uri.parse(path);
    }

    public void checkBook(){
        String bookName = etBookName.getText().toString().trim();
        String author = etAuthorName.getText().toString().trim();
        String genre = etGenre.getText().toString().trim();

        if (bookName.isEmpty() || author.isEmpty() || genre.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        String userID = currentUser.getUid();

        booksReference.orderByChild("bookName").equalTo(bookName).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                for (DataSnapshot bookSnapshot : task.getResult().getChildren()) {
                    Book existingBook = bookSnapshot.getValue(Book.class);
                    if (existingBook != null && existingBook.getAuthor().equals(author) && existingBook.getGenre().equals(genre)) {
                        // הספר כבר קיים, נוסיף את ה-UID של המשתמש לרשימת ה-owners
                        DatabaseReference existingBookRef = bookSnapshot.getRef();
                        existingBookRef.child("owners").child(userID).setValue(true);
                        Toast.makeText(AddBook.this, "הספר כבר קיים - נוסף לבעלים", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish(); // חוזר למסך הקודם
                        return;
                    }
                    else {
                        saveBookToFirebase();
                    }
                }
            }
            else{
                saveBookToFirebase();
            }
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

        Book book = new Book(bookKID,bookName, author, genre,"", userID);

//        booksReference.child(bookKID).setValue(book);
//        uploadImage(bookName,bookKID);
//        finish();
        booksReference.child(bookKID).setValue(book).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                if (chooseGallery || chooseCamera) {
                    uploadImage(bookName,bookKID);
                } else {
                    Toast.makeText(AddBook.this, "Book added without image", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish(); // חוזר למסך הקודם
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddBook.this, "Failed to add book", Toast.LENGTH_SHORT).show();
            }
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



//    public void readImage(View view) {
//        int id = view.getId();
//        if (id == R.id.readStamp) {
//            refImg = refBooks.child(lastStamp + ".png");
//            try {
//                localFile = File.createTempFile(lastStamp, "png");
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        } else if (id == R.id.readGallery) {
//            refImg = refBooks.child(lastGallery + ".jpg");
//            try {
//                localFile = File.createTempFile(lastGallery, "jpg");
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        // Download the image file and  display it
//        final ProgressDialog pd = ProgressDialog.show(this, "Image download", "downloading...", true);
//        refImg.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                pd.dismiss();
//                Toast.makeText(AddBook.this, "Image download success", Toast.LENGTH_LONG).show();
//                String filePath = localFile.getPath();
//                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
//                ivBookCover.setImageBitmap(bitmap);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                pd.dismiss();
//                Toast.makeText(AddBook.this, "Image download failed", Toast.LENGTH_LONG).show();
//            }
//        });
//    }



//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == RESULT_OK) {
//            if (requestCode == REQUEST_CAMERA || requestCode == REQUEST_GALLERY) {
//                // קבלת התמונה מהמצלמה או הגלריה
//                Uri selectedImageUri = (requestCode == REQUEST_CAMERA) ? imageUri : data.getData();
//
//                // הצגת התמונה ב-ImageView
//                ivBookCover.setImageURI(selectedImageUri);
//
//                // העלאת התמונה לשירות חיצוני (Imgur) ושמירת הקישור ב-Firebase
//                uploadImageToImgur(selectedImageUri);
//            }
//        }
//    }

//    private void uploadImageToImgur(Uri imageUri) {
//        String clientId = "YOUR_IMGUR_CLIENT_ID"; // Replace with your Imgur client ID
//        String url = "https://api.imgur.com/3/image";
//
//        try {
//            InputStream inputStream = getContentResolver().openInputStream(imageUri);
//            byte[] imageData = IOUtils.toByteArray(inputStream);
//
//            RequestBody requestBody = RequestBody.create(imageData, MediaType.parse("image/*"));
//            MultipartBody.Part body = MultipartBody.Part.createFormData("image", "image.jpg", requestBody);
//
//            OkHttpClient client = new OkHttpClient();
//            Request request = new Request.Builder()
//                    .url(url)
//                    .addHeader("Authorization", "Client-ID " + clientId)
//                    .post(new MultipartBody.Builder()
//                            .setType(MultipartBody.FORM)
//                            .addFormDataPart("image", "image.jpg", RequestBody.create(imageData, MediaType.parse("image/*")))
//                            .build())
//                    .build();
//
//            client.newCall(request).enqueue(new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    if (response.isSuccessful()) {
//                        String responseBody = response.body().string();
//                        JSONObject jsonObject = new JSONObject(responseBody);
//                        String imageUrl = jsonObject.getJSONObject("data").getString("link");
//
//                        // Save the image URL to Firebase Realtime Database
//                        saveImageLinkToDatabase(imageUrl);
//                    } else {
//                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Upload failed.", Toast.LENGTH_SHORT).show());
//                    }
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }
//
//
//    private void openCamera() {
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (intent.resolveActivity(getPackageManager()) != null) {
//            File photoFile = createImageFile();
//            if (photoFile != null) {
//                imageUri = FileProvider.getUriForFile(this, "com.example.yourapp.fileprovider", photoFile);
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//                startActivityForResult(intent, REQUEST_CAMERA);
//            }
//        }
//    }
//
//    private void openGallery() {
//        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        startActivityForResult(intent, REQUEST_GALLERY);
//    }


}