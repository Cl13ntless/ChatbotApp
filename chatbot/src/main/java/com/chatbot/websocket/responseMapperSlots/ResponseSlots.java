package com.chatbot.websocket.responseMapperSlots;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


public class ResponseSlots {
    Slot slots;
    String sender_id;

    public ResponseSlots(){}

    public Slot getSlot() {return slots;}

    public String getSender_id() {
        return sender_id;
    }
}
