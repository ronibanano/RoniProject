package com.example.roniproject;

import android.widget.ImageView;

public class Books {
    public Books() {
    }
    private String title, author, gener;
    private ImageView bookCoverUrl;

    public Books(String title, String author, String gener, ImageView bookCoverUrl) {
        this.title = title;
        this.author = author;
        this.gener = gener;
        this.bookCoverUrl = bookCoverUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGener() {
        return gener;
    }

    public void setGener(String gener) {
        this.gener = gener;
    }

    public ImageView getBookCoverUrl() {
        return bookCoverUrl;
    }

    public void setBookCoverUrl(ImageView bookCoverUrl) {
        this.bookCoverUrl = bookCoverUrl;
    }
}
