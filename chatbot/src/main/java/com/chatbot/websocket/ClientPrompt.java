package com.chatbot.websocket;

public class ClientPrompt {

    private String prompt;

    public ClientPrompt(){}

    public ClientPrompt(String prompt){
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
