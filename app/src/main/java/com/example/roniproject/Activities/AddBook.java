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


/**
 * Activity responsible for allowing users to add new book listings to the application.
 * <p>
 * This activity provides a form for users to input book details such as the book's name,
 * author, and genre. Users can also select or capture an image for the book cover.
 * The activity handles interactions with Firebase Realtime Database to store book information
 * and Firebase Storage to store book cover images. It also includes logic to check if a
 * similar book already exists and updates existing entries accordingly.
 * </p>
 *
 * @see Book
 * @see FirebaseDatabase
 * @see FirebaseStorage
 */
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


    /**
     * Called when the activity is first created.
     * <p>
     * Initializes the UI components, Firebase references (Database, Storage, Auth),
     * and sets up listeners for UI interactions, such as the "Add Book" button.
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
        setContentView(R.layout.activity_add_book);

        etBookName = findViewById(R.id.etBookName);
        etAuthorName = findViewById(R.id.etAuthorName);
        etGenre = findViewById(R.id.etGenre);
        ivBookCover = findViewById(R.id.ivBookCover);
        btnAddBook = findViewById(R.id.btnAddBook);
        adb = new AlertDialog.Builder(this);

        booksReference = FirebaseDatabase.getInstance().getReference("Books");
        storageReference = FirebaseStorage.getInstance().getReference("BookCovers");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        btnAddBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBook();
            }
        });
    }

    /**
     * Initiates the process of capturing an image using the device camera.
     * <p>
     * Checks for camera permissions before launching the camera intent. If permissions
     * are not granted, it requests them.
     * </p>
     *
     * @param view The view that triggered this method (e.g., a button click).
     */
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

    /**
     * Launches an intent to pick an image from the device's gallery.
     *
     * @param view The view that triggered this method (e.g., a button click).
     */
    public void gallery(View view) {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_GALLERY);
    }

    /**
     * Handles the result from activities started for a result, specifically from the
     * camera or gallery image selection.
     * <p>
     * If an image is successfully captured or selected, it updates the {@link #ivBookCover}
     * ImageView with the image and stores the image URI.
     * </p>
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     */
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == REQUEST_CAMERA) {
                chooseCamera = true;
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                imageUri = getImageUri(imageBitmap); // Convert bitmap to URI
                if (imageUri != null) { // Check if URI conversion was successful
                    ivBookCover.setImageBitmap(imageBitmap);
                }
            } else if (requestCode == REQUEST_GALLERY) {
                chooseGallery = true;
                imageUri = data.getData();
                ivBookCover.setImageURI(imageUri);
            }
        }
        // This part seems redundant with the block above, consider refactoring.
        // If the goal was to set chooseCamera = true regardless of imageUri nullness,
        // it's already done if resultCode is OK.
        // else if (requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK && data != null) {
        //    chooseCamera = true;
        //    Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
        //    imageUri = getImageUri(imageBitmap);
        //    if (imageUri != null) {
        //        ivBookCover.setImageBitmap(imageBitmap);
        //    } else {
        //        Toast.makeText(this, "Failed to get image URI", Toast.LENGTH_SHORT).show();
        //    }
        //}
    }

    /**
     * Converts a {@link Bitmap} image into a {@link Uri} by saving it to a temporary file.
     * <p>
     * This is useful when an image is captured from the camera (which returns a Bitmap)
     * and needs to be uploaded to Firebase Storage (which often requires a Uri).
     * </p>
     *
     * @param bitmap The bitmap image to convert.
     * @return The Uri of the saved image file, or null if an error occurs during file creation or writing.
     */
    private Uri getImageUri(Bitmap bitmap) {
        if (bitmap == null) return null; // Add null check for bitmap
        try {
            File tempFile = File.createTempFile("book_image", ".jpg", getCacheDir());
            tempFile.deleteOnExit(); // Ensure the temporary file is deleted when the VM exits
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
            Toast.makeText(this, "Failed to create image file from bitmap", Toast.LENGTH_SHORT).show();
            return null;
        }
    }


    /**
     * Validates the book input fields and checks if the book already exists in the database.
     * <p>
     * If the book (based on name, author, and genre) already exists, it updates the existing
     * entry by adding the current user to its list of owners and their city to its list of available cities.
     * Otherwise, it proceeds to {@link #saveBookToFirebase()} to add it as a new book.
     * It ensures all fields are filled and the user is logged in before proceeding.
     * </p>
     */
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

        // First, get the current user's city
        userReference.child("city").get().addOnSuccessListener(userCitySnapshot -> { // Changed to child("city").get()
            if (userCitySnapshot.exists()) {
                String cityUser = userCitySnapshot.getValue(String.class);
                if (cityUser == null || cityUser.isEmpty()) {
                    Toast.makeText(AddBook.this, "User city not found. Cannot add book.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Then, check for the book
                booksReference.orderByChild("bookName").equalTo(bookName).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        boolean bookFoundAndUpdated = false;
                        for (DataSnapshot bookSnapshot : task.getResult().getChildren()) {
                            Book existingBook = bookSnapshot.getValue(Book.class);
                            if (existingBook != null && existingBook.getAuthor().equals(author) && existingBook.getGenre().equals(genre)) {
                                DatabaseReference existingBookRef = bookSnapshot.getRef();

                                // Add the user to the owners list
                                existingBookRef.child("owners").child(userID).setValue(true);

                                // Check and add the city
                                existingBookRef.child("cities").child(cityUser).setValue(true); // Simplified city addition

                                Toast.makeText(AddBook.this, "הספר כבר קיים - עודכן", Toast.LENGTH_SHORT).show(); // "Book already exists - updated"
                                setResult(RESULT_OK);
                                finish();
                                bookFoundAndUpdated = true;
                                break; // Found and updated the exact match
                            }
                        }
                        if (!bookFoundAndUpdated) {
                            // No exact match for author and genre, save as new book
                            saveBookToFirebase();
                        }
                    } else if (task.isSuccessful()) { // Task successful but book does not exist
                        saveBookToFirebase();
                    } else { // Task failed
                        Toast.makeText(AddBook.this, "Error checking for existing book: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "User city data not found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to retrieve user city: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Saves a new book's details to the Firebase Realtime Database and uploads its cover image
     * to Firebase Storage if one was selected/captured.
     * <p>
     * Validates input fields and ensures a user is logged in. It creates a new book entry,
     * adds the current user as an owner, and includes the user's city. If an image URI is present,
     * it calls {@link #uploadImage(String, String)} to handle the upload.
     * </p>
     */
    public void saveBookToFirebase() {
        String bookName = etBookName.getText().toString().trim();
        String author = etAuthorName.getText().toString().trim();
        String genre = etGenre.getText().toString().trim();

        if (bookName.isEmpty() || author.isEmpty() || genre.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null) {
            Toast.makeText(this, "משתמש לא מחובר", Toast.LENGTH_SHORT).show(); // "User not logged in"
            return;
        }

        String bookKID = booksReference.push().getKey();
        if (bookKID == null) {
            Toast.makeText(this, "Failed to generate book ID", Toast.LENGTH_SHORT).show();
            return;
        }
        String userID = currentUser.getUid();

        // Create book object (initially without cover URL)
        Book book = new Book(bookKID, bookName, author, genre, ""); // bookCoverUrl will be set after upload

        DatabaseReference bookRef = booksReference.child(bookKID);

        // Save book details first
        bookRef.setValue(book).addOnSuccessListener(unused -> {
            // Add current user to owners
            bookRef.child("owners").child(userID).setValue(true);

            // Get user's city and add it to the book's cities list
            FirebaseDatabase.getInstance().getReference("Users").child(userID)
                    .child("city").get().addOnSuccessListener(citySnapshot -> {
                        String city = citySnapshot.getValue(String.class);
                        if (city != null && !city.isEmpty()) {
                            bookRef.child("cities").child(city).setValue(true);
                        }

                        // Proceed to upload image if selected
                        if ((chooseGallery || chooseCamera) && imageUri != null) {
                            uploadImage(bookName, bookKID); // Pass bookName for potential use in storage path, bookKID for DB update
                        } else {
                            Toast.makeText(AddBook.this, "Book added without image", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        }
                    }).addOnFailureListener(e -> {
                        Toast.makeText(AddBook.this, "נכשל בקריאת עיר המשתמש: " + e.getMessage(), Toast.LENGTH_SHORT).show(); // "Failed to read user's city"
                        // Still attempt to upload image if available, or finish if not
                        if ((chooseGallery || chooseCamera) && imageUri != null) {
                            uploadImage(bookName, bookKID);
                        } else {
                            // If city fetch failed and no image, still consider book added but incomplete
                            Toast.makeText(AddBook.this, "Book added (city info failed, no image)", Toast.LENGTH_LONG).show();
                            setResult(RESULT_OK); // Or a different result code to indicate partial success
                            finish();
                        }
                    });
        }).addOnFailureListener(e -> {
            Toast.makeText(AddBook.this, "Failed to add book details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


    /**
     * Uploads the selected book cover image to Firebase Storage and updates the book's
     * database entry with the image download URL upon successful upload.
     * <p>
     * Displays a progress dialog during the upload process.
     * </p>
     *
     * @param bookName The name of the book, potentially for naming the image file (currently not used for file naming directly).
     * @param bookKID  The unique key (ID) of the book in the Firebase Realtime Database. This is used
     *                 to update the correct book entry with the image URL.
     */
    private void uploadImage(String bookName, String bookKID) {
        if (imageUri == null || bookKID == null || bookKID.isEmpty()) {
            Toast.makeText(this, "Image URI or Book ID is missing, cannot upload.", Toast.LENGTH_SHORT).show();
            // Potentially finish activity or allow user to retry
            setResult(RESULT_OK); // Book details might be saved, but image isn't.
            finish();
            return;
        }

        // Using bookKID to ensure unique path for each book's images,
        // and userID to differentiate if multiple users upload images for the same book
        // (though current logic seems to be one main cover image).
        // Consider if bookName is needed in the path or if bookKID is sufficient.
        // For a single cover per book, bookKID + a standard name like "cover.jpg" might be simpler.
        StorageReference imageFileRef = storageReference.child(bookKID + "/" + currentUser.getUid() + ".jpg");

        ProgressDialog pd = ProgressDialog.show(this, "Uploading Image", "Please wait...", true, false); // Not cancelable

        imageFileRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageFileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                booksReference.child(bookKID).child("bookCoverUrl").setValue(uri.toString())
                                        .addOnSuccessListener(aVoid -> {
                                            pd.dismiss();
                                            Toast.makeText(AddBook.this, "Book added successfully with image", Toast.LENGTH_SHORT).show();
                                            setResult(RESULT_OK);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            pd.dismiss();
                                            Toast.makeText(AddBook.this, "Image uploaded, but failed to update book URL: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                            // Decide how to handle this state. Book exists, image exists, but link is broken.
                                            setResult(RESULT_CANCELED); // Or a custom result code
                                            finish();
                                        });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
                                Toast.makeText(AddBook.this, "Failed to get image download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                setResult(RESULT_CANCELED); // Indicate failure
                                finish();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddBook.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        setResult(RESULT_CANCELED); // Indicate failure
                        finish();
                    }
                });
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * Specifically handles the result of the camera permission request. If granted,
     * it calls {@link #takeStamp(View)} again to proceed with capturing an image.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) { // Corresponds to CAMERA permission request
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takeStamp(null); // Permission granted, try taking picture again
            } else {
                Toast.makeText(this, "Camera permission is required to take a picture.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}