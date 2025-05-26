package com.example.roniproject.Obj;

// No static import for java.security.AccessController.getContext() needed.
// Use getContext() from the ArrayAdapter base class or pass Context in constructor.

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable; // Added for clarity on convertView

import com.example.roniproject.R;

import java.util.List;

/**
 * Custom ArrayAdapter for displaying {@link ChatPreview} objects in a ListView.
 * <p>
 * This adapter is responsible for inflating a custom layout ({@code R.layout.item_chat_preview})
 * for each chat preview in the provided list and populating the views within that layout
 * (e.g., TextViews for the other user's name and city).
 * </p>
 * <p>
 * It correctly handles view recycling for {@link android.widget.ListView} efficiency
 * by checking if the {@code convertView} is null before inflating a new layout.
 * </p>
 *
 * @see com.example.roniproject.Obj.ChatPreview
 * @see com.example.roniproject.Frag.ChatFragment
 * @see android.widget.ArrayAdapter
 */
public class ChatPreviewAdapter extends ArrayAdapter<ChatPreview> {

    /**
     * Constructs a new {@code ChatPreviewAdapter}.
     *
     * @param context The current context.
     * @param chats   The list of {@link ChatPreview} objects to display.
     */
    public ChatPreviewAdapter(@NonNull Context context, @NonNull List<ChatPreview> chats) {
        // The third parameter '0' in super() is the resource ID for a TextView.
        // It's used if this adapter were to display a single TextView per item directly.
        // Since getView() is overridden to inflate a custom layout, this '0' is effectively ignored
        // for the main item view, but it's a required parameter for this constructor.
        // It's common practice to pass 0 when getView() is fully customized.
        super(context, 0, chats);
    }

    /**
     * Get a View that displays the data at the specified position in the data set.
     * <p>
     * This method is called by the ListView to get the view for each item.
     * It inflates the custom layout {@code R.layout.item_chat_preview} if the
     * {@code convertView} is null, or reuses {@code convertView} if it's not null.
     * It then populates the TextViews within the layout with data from the
     * {@link ChatPreview} object at the given position.
     * </p>
     *
     * @param position    The position of the item within the adapter's data set whose view we want.
     * @param convertView The old view to reuse, if possible. If it is not possible to
     *                    convert this view to display the correct data, this method can create a new
     *                    view.
     * @param parent      The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        ChatPreview chat = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            // Inflate the custom layout from the context provided by the parent.
            // Using getContext() from the ArrayAdapter (which is the context passed in constructor)
            // or parent.getContext() are both valid options here.
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_chat_preview, parent, false);
        }

        // Lookup view for data population
        TextView nameView = convertView.findViewById(R.id.chat_user_name);
        TextView cityView = convertView.findViewById(R.id.chat_user_city);

        // Populate the data into the template view using the data object
        // Null checks for 'chat' object are good practice if there's any chance it could be null,
        // though getItem(position) from ArrayAdapter should typically return a valid object
        // if position is within bounds.
        if (chat != null) {
            nameView.setText(chat.getUserName());
            cityView.setText(chat.getCity());
        } else {
            // Handle the case where the chat object is null, perhaps clear the views
            // or set default text, though this situation should ideally not occur.
            nameView.setText("");
            cityView.setText("");
        }

        // Return the completed view to render on screen
        return convertView;
    }
}