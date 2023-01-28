package com.chatbot.geolocation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReverseGeolocation {

    public ReverseGeolocation(){}

    Feature[] features;

    public Feature[] getFeatures() {
        return features;
    }
}
