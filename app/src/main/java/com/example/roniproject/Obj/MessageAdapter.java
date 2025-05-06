package com.example.roniproject.Obj;

import android.content.Context;
import android.view.*;
import android.widget.*;

import com.example.roniproject.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MessageAdapter extends BaseAdapter {

    private Context context;
    private List<Message> messages;
    private LayoutInflater inflater;

    public MessageAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() { return messages.size(); }

    @Override
    public Object getItem(int i) { return messages.get(i); }

    @Override
    public long getItemId(int i) { return i; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message msg = messages.get(position);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (msg.getSenderId().equals(currentUserId)) {
            convertView = inflater.inflate(R.layout.item_message_sent, parent, false);
        } else {
            convertView = inflater.inflate(R.layout.item_message_received, parent, false);
        }

        TextView tvMessage = convertView.findViewById(R.id.text_message_body);
        tvMessage.setText(msg.getText());

        return convertView;
    }
}
