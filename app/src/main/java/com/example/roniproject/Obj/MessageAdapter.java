package com.example.roniproject.Obj;

import android.content.Context;
import android.view.*;
import android.widget.*;

import com.example.roniproject.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

/**
 * Custom {@link BaseAdapter} for displaying {@link Message} objects in a ListView,
 * typically within a chat interface like {@link com.example.roniproject.Activities.ChatActivity}.
 * <p>
 * This adapter is responsible for rendering chat messages differently based on whether
 * the current authenticated user is the sender or the receiver of the message.
 * It inflates one of two possible layouts:
 * <ul>
 *     <li>{@code R.layout.item_message_sent}: Used when the current user is the sender.</li>
 *     <li>{@code R.layout.item_message_received}: Used when the current user is the receiver.</li>
 * </ul>
 * The message text is then populated into the appropriate {@link TextView} within the chosen layout.
 * </p>
 * <p>
 * This adapter does not use the ViewHolder pattern explicitly in the provided code,
 * meaning {@code findViewById} will be called for each item. For long lists,
 * performance might be impacted. Consider implementing the ViewHolder pattern if
 * performance issues arise with many messages.
 * </p>
 * <p>
 * It uses {@link FirebaseAuth} to determine the current user's ID to differentiate
 * between sent and received messages.
 * </p>
 *
 * @see com.example.roniproject.Obj.Message
 * @see com.example.roniproject.Activities.ChatActivity
 * @see android.widget.BaseAdapter
 * @see com.google.firebase.auth.FirebaseAuth
 */
public class MessageAdapter extends BaseAdapter {

    private Context context;
    private List<Message> messages;
    private LayoutInflater inflater;

    /**
     * Constructs a new {@code MessageAdapter}.
     *
     * @param context  The current context.
     * @param messages The list of {@link Message} objects to display.
     */
    public MessageAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
        inflater = LayoutInflater.from(context);
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() { return messages.size(); }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param i Position of the item whose data we want within the adapter's
     * data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int i) { return messages.get(i); }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param i The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int i) { return i; }

    /**
     * Get a View that displays the data at the specified position in the data set.
     * <p>
     * This method is called by the ListView to get the view for each chat message.
     * It determines if the message was sent or received by the current user
     * using {@link FirebaseAuth#getCurrentUser()}. Based on this, it inflates
     * either {@code R.layout.item_message_sent} or {@code R.layout.item_message_received}.
     * The message text is then set in the {@code R.id.text_message_body} TextView
     * found within the inflated layout.
     * </p>
     * <p>
     * Note: This implementation inflates a new view for each call if {@code convertView}
     * is not properly managed by the caller or if the view type changes (which it does based on sender).
     * For optimal performance with distinct layouts for sent/received messages, ensure
     * {@link #getViewTypeCount()} and {@link #getItemViewType(int)} are also overridden.
     * However, the current logic of always inflating based on sender/receiver without
     * checking {@code convertView} means a new view is created almost every time,
     * bypassing {@code convertView}'s reuse for different view types.
     * </p>
     *
     * @param position    The position of the item within the adapter's data set of the
     *                    item whose view we want.
     * @param convertView The old view to reuse, if possible. <b>Note: This implementation
     *                    currently re-inflates the layout unconditionally based on the message
     *                    sender, not fully utilizing the {@code convertView} for recycling
     *                    between different view types (sent/received).</b>
     * @param parent      The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message msg = messages.get(position);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Determine which layout to inflate based on whether the current user is the sender.
        // This part means convertView is effectively ignored if the message type (sent/received)
        // differs from the type convertView might have represented.
        // For proper view recycling with different item types, getItemViewType and getViewTypeCount
        // should be implemented.
        if (msg.getSenderId().equals(currentUserId)) {
            convertView = inflater.inflate(R.layout.item_message_sent, parent, false);
        } else {
            convertView = inflater.inflate(R.layout.item_message_received, parent, false);
        }

        // Find the TextView for the message body in the inflated layout
        TextView tvMessage = convertView.findViewById(R.id.text_message_body);
        tvMessage.setText(msg.getText()); // Set the message text

        return convertView;
    }
}