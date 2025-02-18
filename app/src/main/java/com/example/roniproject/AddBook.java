package com.example.roniproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;

public class AddBook extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 101;

    private DatabaseReference booksReference;

    private EditText etBookName, etAuthorName;
    private ImageView ivBookCover;
    private Uri imageUri;
    private Button btnUploadImage, btnAddBook;
    private DatabaseReference mRef;
    AlertDialog.Builder adb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        etBookName = findViewById(R.id.etBookName);
        etAuthorName = findViewById(R.id.etAuthorName);
        ivBookCover = findViewById(R.id.ivBookCover);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnAddBook = findViewById(R.id.btnAddBook);
        adb = new AlertDialog.Builder(this);

        //booksReference = FirebaseDatabase.getInstance().getReference("Books");

//        ivBookCover.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                adb.setTitle("Choose an option");
//                adb.setItems(new CharSequence[]{"Take Photo", "Choose from Gallery"}, (dialog, which) -> {
//                    if (which == 0) {
//                        openCamera();
//                    } else {
//                        openGallery();
//                    }
//                });
//                adb.show();
//            }
//        });

    }
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