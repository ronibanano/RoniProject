package com.example.roniproject.Obj;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.roniproject.R;

import java.util.List;

public class ChatPreviewAdapter extends ArrayAdapter<ChatPreview> {
    public ChatPreviewAdapter(Context context, List<ChatPreview> chats) {
        super(context, 0, chats);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatPreview chat = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_chat_preview, parent, false);
        }

        TextView nameView = convertView.findViewById(R.id.chat_user_name);
        TextView cityView = convertView.findViewById(R.id.chat_user_city);

        nameView.setText(chat.getUserName());
        cityView.setText(chat.getCity());

        return convertView;
    }
}
