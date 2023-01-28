package com.chatbot.websocket;

public class ClientPrompt {

    private String text;

    public ClientPrompt() {
    }

    public ClientPrompt(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
