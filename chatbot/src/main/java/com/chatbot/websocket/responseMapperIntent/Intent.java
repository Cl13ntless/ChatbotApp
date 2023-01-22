package com.chatbot.websocket.responseMapperIntent;

public class Intent {
    String name;
    int confidence;

    public Intent() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getConfidence() {
        return confidence;
    }

    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }
}
