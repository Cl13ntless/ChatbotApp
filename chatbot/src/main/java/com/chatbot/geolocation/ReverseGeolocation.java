package com.chatbot.geolocation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReverseGeolocation {
    private Feature[] features;
    public ReverseGeolocation(){/* Is empty because of Mapper */}
    public Feature[] getFeatures() {
        return features;
    }
}
