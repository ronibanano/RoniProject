package com.example.roniproject.Frag;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.roniproject.Activities.ChatActivity;
import com.example.roniproject.Obj.ChatPreview;
import com.example.roniproject.Obj.ChatPreviewAdapter;
import com.example.roniproject.Obj.Users;
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

public class ChatFragment extends Fragment {

    private ListView chatListView;
    private ChatPreviewAdapter chatPreviewAdapter;
    private List<ChatPreview> chatPreviews;

    private FirebaseUser currentUser;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chatListView = view.findViewById(R.id.chatListView);
        chatPreviews = new ArrayList<>();
        chatPreviewAdapter = new ChatPreviewAdapter(requireContext(), chatPreviews);
        chatListView.setAdapter(chatPreviewAdapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String currentUserId = currentUser.getUid();
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("Chats");

        chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatPreviews.clear();

                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    String chatId = chatSnapshot.getKey();

                    if (chatId != null && chatId.contains(currentUserId)) {
                        String otherUserId = chatId.replace(currentUserId, "").replace("_", "");

                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(otherUserId);
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                Users otherUser = userSnapshot.getValue(Users.class);
                                if (otherUser != null) {
                                    chatPreviews.add(new ChatPreview(otherUserId, otherUser.getFullName(), otherUser.getCity()));
                                    chatPreviewAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(requireContext(), "Failed to load users", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load chats", Toast.LENGTH_SHORT).show();
            }
        });

        chatListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatPreview chatPreview = chatPreviews.get(position);

                Intent intent = new Intent(requireContext(), ChatActivity.class);
                intent.putExtra("otherUserId", chatPreview.getUserId());
                startActivity(intent);
            }
        });
    }
}
