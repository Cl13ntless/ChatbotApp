package com.chatbot.websocket.ResponseLatestMessage;

import com.chatbot.websocket.responseMapperIntent.Intent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Latest_Message {
    public Latest_Message(){ /* Is empty because of Mapper */ }
    private Intent intent;

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }
}
