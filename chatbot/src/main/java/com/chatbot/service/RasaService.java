package com.chatbot.service;

import com.chatbot.exception.RasaRequestException;
import com.chatbot.websocket.ResponseLatestMessage.Latest_Message;
import com.chatbot.websocket.ResponseLatestMessage.ResponseLM;
import com.chatbot.websocket.responseMapperChatbot.ResponseChatbot;
import com.chatbot.websocket.responseMapperIntent.ResponseIntent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

public class RasaService {
    private static final HashMap<String, String> initialClientPrompt = new HashMap<>();
    private static final HashMap<String, String> initialChatMessage = new HashMap<>();
    private static final HashMap<String, String> chatMessage = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(RasaService.class);
    private static final String RASA_URL = "http://rasa:5005/model/parse";
    private static final String RASA_CONVERSATION_URL = "http://rasa:5005/webhooks/rest/webhook";
    private static final String RASA_SLOT_URL = "http://rasa:5005/conversations/test_user/tracker";
    ObjectMapper objectMapper = new ObjectMapper();

    //Makes the first request to Rasa with user input to retrieve the correct intent
    public ResponseIntent getInitialParameters(String clientPrompt) {
        initialClientPrompt.put("text", clientPrompt);
        initialChatMessage.put("sender", "test_user");
        initialChatMessage.put("message", clientPrompt);

        //send text to rasa to set the correct latest message
        try {
            String chatRequest = objectMapper.writeValueAsString(initialChatMessage);
            postRequestRasa(RASA_CONVERSATION_URL, chatRequest);

            String requestBody = objectMapper.writeValueAsString(initialClientPrompt);
            HttpResponse<String> initialResponse = postRequestRasa(RASA_URL, requestBody);

            logger.info("Initial Response with Body: {}", initialResponse.body());
            return mapResponse(initialResponse.body());

        } catch (RasaRequestException e) {
            e.printStackTrace();
            logger.error("Rasa couldnt be rached");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error("Object Mapper malfunction");
        }
        return null;
    }

    //Send chat message to rasa chat endpoint and return rasa's response
    public String getChatResponse(String clientMessage) {
        chatMessage.put("sender", "test_user");
        chatMessage.put("message", clientMessage);

        try {
            String requestBody = objectMapper.writeValueAsString(chatMessage);
            HttpResponse<String> chatResponse = postRequestRasa(RASA_CONVERSATION_URL, requestBody);
            ResponseChatbot[] chatResponseArray = objectMapper.readValue(chatResponse.body(), ResponseChatbot[].class);
            return chatResponseArray[0].getText();

        } catch (RasaRequestException e) {
            e.printStackTrace();
            logger.error("Rasa couldnt be rached");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error("Object Mapper malfunction");
        }
        return null;
    }

    //Send a post request to a specified url with specified json String
    public HttpResponse<String> postRequestRasa(String url, String jsonValues) throws RasaRequestException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(jsonValues))
                .build();

        try {
            return client.send(request,
                    HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            throw new RasaRequestException("Interrupted Connection to Rasa");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RasaRequestException("Problem with IO in Rasa Request");
        }
    }

    //Map a response Intent Item from its JSON to a POJO
    public ResponseIntent mapResponse(String responseJson) {
        try {
            return objectMapper.readValue(responseJson, ResponseIntent.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error("Mapper malfunction");
            return null;
        }
    }

    //Get the Latest message received by RASA through the SLOT URL endpoint
    public Latest_Message getLatestMessage() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(RASA_SLOT_URL))
                    .GET()
                    .build();
            HttpResponse<String> response = client
                    .send(request, HttpResponse.BodyHandlers.ofString());

            ResponseLM responseLatestMessage = objectMapper.readValue(response.body(), ResponseLM.class);
            return responseLatestMessage.getLatest_message();
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            logger.error("Latest message couldn't be acquired");
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        return null;
    }
}
