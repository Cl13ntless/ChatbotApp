package com.chatbot.websocket;

public class ServerResponse {
    private String content;

    public ServerResponse() {
    }

    public ServerResponse(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
