package com.chatbot.websocket.responseMapperIntent;

public class Intent {
    String name;
    double confidence;

    public Intent() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}
