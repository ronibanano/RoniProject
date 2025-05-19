package com.example.roniproject.Obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Book {

    private String bookName, author, genre;
    private String bookCoverUrl;
    private String bookKID;
    private Map<String, Boolean> owners;
    private Map<String, Boolean> cities;

    public Book() {
    }

    public Book(String bookKID,String bookName, String author, String genre, String bookCoverUrl) {
        this.bookKID = bookKID;
        this.bookName = bookName;
        this.author = author;
        this.genre = genre;
        this.bookCoverUrl = bookCoverUrl;
        this.owners = new HashMap<>();
        this.cities = new HashMap<>();
        //this.owners.put(userID, true); // הוספת המשתמש הראשון


    }
    public String getBookKID() {
        return bookKID;
    }
    public void setBookKID(String bookKID) {
        this.bookKID = bookKID;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getBookCoverUrl() {
        return bookCoverUrl;
    }

    public void setBookCoverUrl(String bookCoverUrl) {
        this.bookCoverUrl = bookCoverUrl;
    }

    public Map<String, Boolean> getOwners() {
        return owners;
    }

    public void setOwners(Map<String, Boolean> owners) {
        this.owners = owners;
    }

    public Map<String, Boolean> getCities() {
        return cities;
    }

    public void setCities(Map<String, Boolean> cities) {
        this.cities = cities;
    }


    // public void addOwner(String userId) { this.owners.put(userId, true); }
}
