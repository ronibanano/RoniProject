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

/**
 * Custom ArrayAdapter for displaying {@link Book} objects in a ListView.
 * <p>
 * This adapter is responsible for inflating a custom layout ({@code R.layout.book_item})
 * for each book in the provided list and populating the views within that layout
 * (e.g., TextViews for book name, author, genre, and an ImageView for the book cover).
 * </p>
 * <p>
 * It utilizes the ViewHolder pattern to improve performance by caching view lookups,
 * making scrolling smoother. The <a href="https://github.com/bumptech/glide">Glide</a>
 * library is used for efficiently loading and displaying book cover images from URLs,
 * including handling placeholders and error images.
 * </p>
 *
 * @see com.example.roniproject.Obj.Book
 * @see android.widget.ArrayAdapter
 * @see com.bumptech.glide.Glide
 */
public class BookAdapter extends ArrayAdapter<Book> {
    private final Context context;
    private final List<Book> books;

    /**
     * Constructs a new {@code BookAdapter}.
     *
     * @param context The current context.
     * @param books   The list of {@link Book} objects to display.
     */
    public BookAdapter(Context context, List<Book> books) {
        super(context, R.layout.book_item, books);
        this.context = context;
        this.books = books;
    }

    /**
     * Get a View that displays the data at the specified position in the data set.
     * <p>
     * This method is called by the ListView to get the view for each item. It uses
     * the ViewHolder pattern to recycle views and improve performance.
     * It populates the views in the custom layout ({@code R.layout.book_item})
     * with data from the {@link Book} object at the given position.
     * </p>
     * <p>
     * The book's cover image is loaded using Glide. If the URL is invalid or loading fails,
     * a default placeholder image ({@code R.drawable.ic_add_image}) is shown.
     * </p>
     *
     * @param position    The position of the item within the adapter's data set of the
     *                    item whose view we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible
     *                    to convert this view to display the correct data, this method can create a new
     *                    view.
     * @param parent      The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
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

        // Set text for book details
        holder.tvName.setText(book.getBookName());
        holder.tvAuthor.setText(book.getAuthor()); // Assuming getAuthorName() is the correct getter
        holder.tvGenre.setText(book.getGenre());

        // Load book cover image using Glide
        // Includes null and empty checks for the URL, placeholder, and error drawables.
        if (book.getBookCoverUrl() != null && !book.getBookCoverUrl().isEmpty()) {
            Glide.with(context)
                    .load(book.getBookCoverUrl())
                    .placeholder(R.drawable.ic_add_image) // Displayed while the image is loading
                    .error(R.drawable.ic_add_image)       // Displayed if the URL is invalid or loading fails
                    .into(holder.ivCover);
        } else {
            // Set a default image if the URL is null or empty
            holder.ivCover.setImageResource(R.drawable.ic_add_image);
        }

        return convertView;
    }

    /**
     * ViewHolder pattern to cache view references for efficient recycling.
     * <p>
     * Holds references to the views within the {@code R.layout.book_item} layout,
     * avoiding repeated calls to {@code findViewById} during ListView scrolling.
     * </p>
     */
    static class ViewHolder {
        TextView tvName;
        TextView tvAuthor;
        TextView tvGenre;
        ImageView ivCover;
    }
}
