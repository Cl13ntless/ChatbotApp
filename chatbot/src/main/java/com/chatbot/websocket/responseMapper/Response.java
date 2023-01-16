package com.chatbot.websocket.responseMapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {
    String text;
    Intent intent;

    public Response(){}

    public String getText() {
        return text;
    }

    public Intent getIntent() {
        return intent;
    }



}
