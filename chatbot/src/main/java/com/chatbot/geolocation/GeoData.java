package com.chatbot.geolocation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoData {
    Geolocation[] data;
    public GeoData(){/* Is empty because of Mapper */}

    public Geolocation[] getData() {
        return data;
    }
}
