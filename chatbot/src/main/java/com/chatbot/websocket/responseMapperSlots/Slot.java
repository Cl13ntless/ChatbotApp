package com.chatbot.websocket.responseMapperSlots;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class Slot {

    String location;
    String today;
    String tomorrow;
    String next_week;


    public Slot(){}

    public String getLocation() {
        return location;
    }

    public String getToday() {
        return today;
    }

    public String getTomorrow() {
        return tomorrow;
    }

    public String getNext_week() {
        return next_week;
    }
}
