package com.chatbot.websocket.responseMapperChatbot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseChatbot {

    private String text;

    public ResponseChatbot() {/* Is empty because of Mapper */}

    public String getText() {
        return text;
    }
}
