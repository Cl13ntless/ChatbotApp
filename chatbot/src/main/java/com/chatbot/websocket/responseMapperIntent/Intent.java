package com.chatbot.websocket.responseMapperIntent;

public class Intent {
    private String name;
    private double confidence;

    public Intent() {/* Is empty because of Mapper */}

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
