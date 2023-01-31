package com.chatbot.geolocation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Geolocation {
    String name;
    double latitude;
    double longitude;
    String country;

    public Geolocation() {
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }
}
