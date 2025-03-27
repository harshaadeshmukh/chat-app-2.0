package com.example.chatt_application.models;

import java.io.Serializable;

public class User implements Serializable {
    public String name , image , email , token;
    public String id;

    private double rating;
    private String feedback;

    // Empty constructor (Firestore needs it)
    public User() {}

    public User(String name, String email, double rating, String feedback) {
        this.name = name;
        this.email = email;
        this.rating = rating;
        this.feedback = feedback;
    }

    // Getters
    public String getName() { return name; }
    public String getEmail() { return email; }
    public double getRating() { return rating; }
    public String getFeedback() { return feedback; }
}
