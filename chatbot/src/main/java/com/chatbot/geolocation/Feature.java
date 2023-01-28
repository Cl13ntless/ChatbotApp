package com.chatbot.geolocation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Feature {
    Property properties;

    public Feature(){}

    public Property getProperties() {
        return properties;
    }


}
