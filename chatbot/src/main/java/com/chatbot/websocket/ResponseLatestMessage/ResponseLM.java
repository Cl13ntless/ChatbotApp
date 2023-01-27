package com.chatbot.websocket.ResponseLatestMessage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseLM {
    public ResponseLM(){}

    String sender_id;
    Latest_Message latest_message;

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public Latest_Message getLatest_message() {
        return latest_message;
    }

    public void setLatest_message(Latest_Message latest_message) {
        this.latest_message = latest_message;
    }
}
