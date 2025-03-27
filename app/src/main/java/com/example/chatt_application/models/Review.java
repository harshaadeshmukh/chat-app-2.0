package com.example.chatt_application.models;

public class Review {
    private String name;
    private double rating;
    private String feedback;


    public Review(String name, double rating, String feedback) {
        this.name = name;
        this.rating = rating;
        this.feedback = feedback;
    }

    public String getName() {
        return name;
    }

    public double getRating() {
        return rating;
    }

    public String getFeedback() {
        return feedback;
    }
}
