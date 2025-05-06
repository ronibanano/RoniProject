package com.example.roniproject;

import com.google.firebase.Firebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FBRef {
    public static FirebaseStorage FBST = FirebaseStorage.getInstance();
    public static StorageReference refST = FBST.getReference();
    public static StorageReference refBooks = refST.child("Books");

    public static DatabaseReference refMessages = FirebaseDatabase.getInstance().getReference().child("Messages");
    public static DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference().child("Users");

}
