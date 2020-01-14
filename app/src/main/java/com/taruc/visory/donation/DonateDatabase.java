package com.taruc.visory.donation;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class DonateDatabase {
    private String email;
    private double amount;
    private String dateTime;

    public DonateDatabase() {

    }

    public DonateDatabase(String email, double amount, String dateTime) {
        this.email = email;
        this.amount = amount;
        this.dateTime = dateTime;
    }

    public String getEmail() {
        return email;
    }

    public double getAmount() {
        return amount;
    }

    public String getDateTime() {
        return dateTime;
    }
}