package com.example.roniproject.Obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Represents a Book object with its properties.
 * <p>
 * This class is a Plain Old Java Object (POJO) used to model book data,
 * primarily for interaction with Firebase Realtime Database. It includes fields
 * for the book's name ({@code bookName}), author ({@code author}),
 * genre ({@code genre}), a URL to its cover image ({@code bookCoverUrl}),
 * and a unique identifier ({@code bookKID}) used in Firebase.
 * It also includes maps for {@code owners} and {@code cities} associated with the book.
 * </p>
 * <p>
 * It provides constructors for creating {@code Book} instances and getter/setter
 * methods for accessing and modifying its properties.
 * </p>
 *
 */
public class Book {

    private String bookName, author, genre;
    private String bookCoverUrl;
    private String bookKID;
    private Map<String, Boolean> owners;
    private Map<String, Boolean> cities;

    /**
     * Default constructor.
     * <p>
     * Required for calls to {@link com.google.firebase.database.DataSnapshot#getValue(Class)}.
     * Initializes all fields to their default values (null or 0).
     * </p>
     */
    public Book() {
    }

    /**
     * Constructs a new {@code Book} instance with specified details.
     * The {@code owners} and {@code cities} maps are initialized as empty HashMaps.
     *
     * @param bookKID      The unique key (ID) of the book in Firebase.
     * @param bookName     The name of the book.
     * @param author       The name of the book's author.
     * @param genre        The genre of the book.
     * @param bookCoverUrl The URL of the book's cover image. May be null if no image is available.
     */
    public Book(String bookKID,String bookName, String author, String genre, String bookCoverUrl) {
        this.bookKID = bookKID;
        this.bookName = bookName;
        this.author = author;
        this.genre = genre;
        this.bookCoverUrl = bookCoverUrl;
        this.owners = new HashMap<>();
        this.cities = new HashMap<>();
    }

    /**
     * Gets the unique key (ID) of the book ({@code bookKID}) as stored in Firebase Realtime Database.
     *
     * @return The Firebase key (ID) for the book ({@code bookKID}).
     */
    public String getBookKID() {
        return bookKID;
    }

    /**
     * Sets the unique key (ID) of the book ({@code bookKID}), typically its Firebase Realtime Database key.
     *
     * @param bookKID The new Firebase key (ID) for the book.
     */
    public void setBookKID(String bookKID) {
        this.bookKID = bookKID;
    }

    /**
     * Gets the name of the book ({@code bookName}).
     *
     * @return The book's {@code bookName}.
     */
    public String getBookName() {
        return bookName;
    }

    /**
     * Sets the name of the book ({@code bookName}).
     *
     * @param bookName The new {@code bookName} for the book.
     */
    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    /**
     * Gets the name of the book's author ({@code author}).
     *
     * @return The author's name ({@code author}).
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the name of the book's author ({@code author}).
     *
     * @param author The new {@code author} name for the book.
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Gets the genre of the book ({@code genre}).
     *
     * @return The book's {@code genre}.
     */
    public String getGenre() {
        return genre;
    }

    /**
     * Sets the genre of the book ({@code genre}).
     *
     * @param genre The new {@code genre} for the book.
     */
    public void setGenre(String genre) {
        this.genre = genre;
    }

    /**
     * Gets the URL of the book's cover image ({@code bookCoverUrl}).
     *
     * @return The URL string for the {@code bookCoverUrl}, or null if not set.
     */
    public String getBookCoverUrl() {
        return bookCoverUrl;
    }

    /**
     * Sets the URL of the book's cover image ({@code bookCoverUrl}).
     *
     * @param bookCoverUrl The new URL for the book's {@code bookCoverUrl}.
     */
    public void setBookCoverUrl(String bookCoverUrl) {
        this.bookCoverUrl = bookCoverUrl;
    }

    /**
     * Gets the map of owners for this book.
     * The keys are typically user IDs, and the boolean value might indicate ownership status.
     *
     * @return A {@code Map<String, Boolean>} representing the owners.
     */
    public Map<String, Boolean> getOwners() {
        return owners;
    }

    /**
     * Sets the map of owners for this book.
     *
     * @param owners A {@code Map<String, Boolean>} where keys are owner identifiers
     *               (e.g., user IDs) and values indicate a boolean status.
     */
    public void setOwners(Map<String, Boolean> owners) {
        this.owners = owners;
    }

    /**
     * Gets the map of cities associated with this book.
     * The keys are typically city names or identifiers, and the boolean value might indicate availability or relevance.
     *
     * @return A {@code Map<String, Boolean>} representing the cities.
     */
    public Map<String, Boolean> getCities() {
        return cities;
    }

    /**
     * Sets the map of cities associated with this book.
     *
     * @param cities A {@code Map<String, Boolean>} where keys are city identifiers
     *               and values indicate a boolean status.
     */
    public void setCities(Map<String, Boolean> cities) {
        this.cities = cities;
    }
}
