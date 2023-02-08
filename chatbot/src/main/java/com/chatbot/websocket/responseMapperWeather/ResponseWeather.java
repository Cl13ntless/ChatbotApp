package com.chatbot.websocket.responseMapperWeather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseWeather {
    private Weather[] weather;

    public ResponseWeather() { /* Is empty because of Mapper */}

    public Weather[] getWeather() {
        return weather;
    }

    public void setWeather(Weather[] weather) {
        this.weather = weather;
    }
}
