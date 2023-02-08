package com.chatbot.websocket.responseMapperIntent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseIntent {
    private String text;
    private Intent intent;
    private Entity[] entities;

    public ResponseIntent() {/* Is empty because of Mapper */}

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
