package com.chatbot.geolocation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Feature {
    Property properties;

    public Feature(){ /* Needs to be empty for mapping */ }

    public Property getProperties() {
        return properties;
    }


}
