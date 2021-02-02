package com.crimson.picshu.gallery;

/**
 * Created by Crimson on 2/26/2018.
 */

public class BeanPurchasedBooks {

    String sBooks, sDateTime;

    public BeanPurchasedBooks(String sBooks, String sDateTime) {

        this.sBooks = sBooks;
        this.sDateTime = sDateTime;
    }

    public String getsBooks() {
        return sBooks;
    }

    public void setsBooks(String sBooks) {
        this.sBooks = sBooks;
    }

    public String getsDateTime() {
        return sDateTime;
    }

    public void setsDateTime(String sDateTime) {
        this.sDateTime = sDateTime;
    }
}
