package com.chatbot.websocket.responseMapperSlots;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseSlots {
    public ResponseSlots(){}

    Slots slots;

    public Slots getSlots() {
        return slots;
    }
}
