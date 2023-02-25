package com.chatbot.geolocation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Geolocation {
    private String name;
    private double latitude;
    private double longitude;
    private String country_code;

    public Geolocation() {/* Is empty because of Mapper */}

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public String getCountry_code() {
        return country_code;
    }
}
