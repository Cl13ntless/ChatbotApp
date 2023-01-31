package com.chatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class RasaServiceTest {
    RasaService rasaService;

    @BeforeEach
    public void beforeEach(){
        rasaService = new RasaService();
    }

/*
    @Test
    public void testGetInitialParameters(){
        Assertions.assertNotNull(rasaService.getInitialParameters("Hallo"));
    }

    @Test
    public void testGetChatResponse(){
        Assertions.assertEquals("Hey!",rasaService.getChatResponse("Hey"));
    }

    @Test
    public void testGetLatestMessage(){
        rasaService.getInitialParameters("Hallo");
        Assertions.assertEquals("greet", rasaService.getLatestMessage().getIntent().getName());
    }

 */
}
