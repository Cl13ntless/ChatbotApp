package com.chatbot.websocket.responseMapperWeather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseWeather {
    Weather[] weather ;

    public ResponseWeather(){}

    public Weather[] getWeather() {
        return weather;
    }

    public void setWeather(Weather[] weather) {
        this.weather = weather;
    }
}
