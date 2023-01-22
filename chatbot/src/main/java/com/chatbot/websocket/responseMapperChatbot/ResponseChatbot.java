package com.chatbot.websocket.responseMapperChatbot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseChatbot {
    
    String text;

    public ResponseChatbot(){}

    public String getText() {
        return text;
    }
}
