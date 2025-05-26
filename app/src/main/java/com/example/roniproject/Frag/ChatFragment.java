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

/**
 * Fragment responsible for displaying a list of chat previews for the current user.
 * <p>
 * This fragment retrieves all chat sessions involving the currently authenticated Firebase user.
 * For each chat, it identifies the other participant and fetches their user details (full name and city)
 * to display a preview. The previews are presented in a ListView.
 * </p>
 * <p>
 * When a user taps on a chat preview in the list, they are navigated to the
 * {@link com.example.roniproject.Activities.ChatActivity} for that specific conversation,
 * passing the other user's ID to the activity.
 * </p>
 * <p>
 * The fragment interacts with Firebase Realtime Database to:
 * <ul>
 *     <li>Fetch chat data from the "Chats" node. Chat IDs are expected to be structured
 *         in a way that includes both participants' UIDs (e.g., "uid1_uid2").</li>
 *     <li>Fetch user data (full name, city) from the "Users" node to display information
 *         about the other participant in each chat.</li>
 *     <li>Get the current authenticated user via {@link FirebaseAuth}.</li>
 * </ul>
 * </p>
 *
 * @see com.example.roniproject.Activities.ChatActivity
 * @see com.example.roniproject.Obj.ChatPreview
 * @see com.example.roniproject.Obj.ChatPreviewAdapter
 * @see com.example.roniproject.Obj.User
 * @see FirebaseDatabase
 * @see FirebaseAuth
 */
public class ChatFragment extends Fragment {

    private ListView chatListView;
    private ChatPreviewAdapter chatPreviewAdapter;
    private List<ChatPreview> chatPreviews;

    private FirebaseUser currentUser;

    /**
     * Default constructor.
     * <p>
     * Required empty public constructor for Fragment instantiation.
     * </p>
     */
    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * <p>
     * This method inflates the layout for the fragment's UI.
     * </p>
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     *                           any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's
     *                           UI should be attached to. The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * <p>
     * This method initializes the UI components, specifically the {@link ListView} and its
     * {@link ChatPreviewAdapter}. It then fetches the current user and proceeds to load
     * chat previews if the user is authenticated. An item click listener is set on the
     * ListView to handle navigation to individual chat screens.
     * </p>
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chatListView = view.findViewById(R.id.chatListView);
        chatPreviews = new ArrayList<>();
        // requireContext() is used to get a non-null Context, assuming the fragment is attached.
        chatPreviewAdapter = new ChatPreviewAdapter(requireContext(), chatPreviews);
        chatListView.setAdapter(chatPreviewAdapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Optionally, display a message to the user that they need to be logged in
            // or handle this state appropriately (e.g., disable UI, show login prompt).
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        loadChatPreviews();

        chatListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * Callback method to be invoked when an item in this AdapterView has been clicked.
             * <p>
             * Retrieves the {@link ChatPreview} object for the clicked position and starts
             * the {@link com.example.roniproject.Activities.ChatActivity}, passing the other user's ID
             * as an extra in the Intent.
             * </p>
             * @param parent The AdapterView where the click happened.
             * @param view The view within the AdapterView that was clicked (this
             *            will be a view provided by the adapter).
             * @param position The position of the view in the adapter.
             * @param id The row id of the item that was clicked.
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatPreview chatPreview = chatPreviews.get(position);

                Intent intent = new Intent(requireContext(), ChatActivity.class);
                // Pass the ID of the other user to the ChatActivity
                intent.putExtra("otherUserId", chatPreview.getUserId());
                startActivity(intent);
            }
        });
    }

    /**
     * Fetches and displays the chat previews for the current user.
     * <p>
     * It retrieves all chat nodes from the "Chats" path in Firebase Realtime Database.
     * For each chat where the current user is a participant, it determines the other user's ID.
     * It then fetches that other user's details (name and city) from the "Users" node
     * and creates a {@link ChatPreview} object, which is added to the list and displayed
     * by the adapter.
     * </p>
     */
    private void loadChatPreviews() {
        if (currentUser == null) return; // Should be handled by onViewCreated, but good for safety

        String currentUserId = currentUser.getUid();
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("Chats");

        // Use addListenerForSingleValueEvent if you only need to load the chats once.
        // Use addValueEventListener if you want real-time updates to the chat list.
        chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatPreviews.clear(); // Clear previous data before adding new items

                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    String chatId = chatSnapshot.getKey();

                    // Check if the current user is part of this chat
                    if (chatId != null && chatId.contains(currentUserId)) {
                        // Determine the other user's ID from the chatId
                        // Assumes chatId is like "userId1_userId2"
                        String otherUserId = chatId.replace(currentUserId, "").replace("_", "");

                        if (!otherUserId.isEmpty()) {
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(otherUserId);
                            // Fetch the details of the other user
                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                    User otherUser = userSnapshot.getValue(User.class);
                                    if (otherUser != null) {
                                        // Create a ChatPreview object and add it to the list
                                        chatPreviews.add(new ChatPreview(otherUserId, otherUser.getFullName(), otherUser.getCity()));
                                        chatPreviewAdapter.notifyDataSetChanged(); // Update the ListView
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    // Log error or show a toast message for failing to load a specific user
                                    Toast.makeText(getContext(), "Failed to load user: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
                // Optional: Show a message if no chats are found after checking all
                if (chatPreviews.isEmpty()) {
                    // Toast.makeText(getContext(), "No chats found.", Toast.LENGTH_SHORT).show();
                    // Or update a TextView to indicate no chats
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Log error or show a toast message for failing to load the chats list
                Toast.makeText(getContext(), "Failed to load chats: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
