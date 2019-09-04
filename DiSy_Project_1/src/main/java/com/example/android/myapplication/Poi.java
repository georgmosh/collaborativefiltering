package com.example.android.myapplication;

import java.io.Serializable;

public class Poi implements Serializable {
    //Class fields
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private String category;
    private String image;

    //Default constructor
    public Poi() {}

    //Overloaded constructor
    public Poi(String id, String name, double latitude, double longitude, String category,String image) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
        this.image = image;
    }

    //Setters
    public void setID(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setImage(String image) {
        this.image = image;
    }


    //Getters
    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getCategory() {
        return category;
    }

    public String getImage(){
        return image;
    }

}