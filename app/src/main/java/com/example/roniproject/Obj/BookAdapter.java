package com.example.roniproject.Obj;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.roniproject.R;

import java.util.List;

public class BookAdapter extends ArrayAdapter<Book> {
    private final Context context;
    private final List<Book> books;

    public BookAdapter(Context context, List<Book> books) {
        super(context, R.layout.book_item, books);
        this.context = context;
        this.books = books;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.book_item, parent, false);
            holder = new ViewHolder();
            holder.tvName = convertView.findViewById(R.id.tvBookName);
            holder.tvAuthor = convertView.findViewById(R.id.tvBookAuthor);
            holder.tvGenre = convertView.findViewById(R.id.tvBookGenre);
            holder.ivCover = convertView.findViewById(R.id.ivBookCover);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Book book = books.get(position);

        holder.tvName.setText(book.getBookName());
        holder.tvAuthor.setText(book.getAuthor());
        holder.tvGenre.setText(book.getGenre());

        // טעינת תמונה עם בדיקת תקינות URL
        if (book.getBookCoverUrl() != null && !book.getBookCoverUrl().isEmpty()) {
            Glide.with(context)
                    .load(book.getBookCoverUrl())
                    .placeholder(R.drawable.ic_add_image) // תמונת ברירת מחדל אם נטען לאט
                    .error(R.drawable.ic_add_image) // תמונת שגיאה אם ה-URL לא תקף
                    .into(holder.ivCover);
        } else {
            holder.ivCover.setImageResource(R.drawable.ic_add_image);
        }

        return convertView;
    }

    // ViewHolder - מחזיק הפניות לרכיבים לשימוש חוזר
    static class ViewHolder {
        TextView tvName;
        TextView tvAuthor;
        TextView tvGenre;
        ImageView ivCover;
    }
}
