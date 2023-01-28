package com.chatbot.websocket.responseMapperIntent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseIntent {
    String text;
    Intent intent;
    Entity[] entities;

    public ResponseIntent() {
    }

    public Entity[] getEntities() {
        return entities;
    }

    public String getText() {
        return text;
    }

    public Intent getIntent() {
        return intent;
    }


}
