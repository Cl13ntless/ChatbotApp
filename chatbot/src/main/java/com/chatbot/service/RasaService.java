package com.chatbot.service;

import com.chatbot.websocket.ResponseLatestMessage.Latest_Message;
import com.chatbot.websocket.ResponseLatestMessage.ResponseLM;
import com.chatbot.websocket.responseMapperChatbot.ResponseChatbot;
import com.chatbot.websocket.responseMapperIntent.ResponseIntent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

public class RasaService {
    String RASA_URL = "http://localhost:5005/model/parse";
    String RASA_CONVERSATIONS_URL = "http://localhost:5005/webhooks/rest/webhook";
    String RASA_SLOT_URL = "http://localhost:5005/conversations/test_user/tracker";

    String RASA_EMPTY_SLOT_URL = "http://localhost:5005/conversations/test_user/tracker/events";
    ObjectMapper objectMapper = new ObjectMapper();

    public ResponseIntent getInitialParameters(String urlParameters) throws Exception {
        var values = new HashMap<String, String>() {{
            put("text", urlParameters);
        }};

        //send text to rasa to set slots
        var values2 = new HashMap<String, String>() {{
            put("sender", "test_user");
            put("message", urlParameters);
        }};
        String chatRequest = objectMapper.writeValueAsString(values2);
        HttpResponse<String> chatResponse = postRequestRasa(RASA_CONVERSATIONS_URL, chatRequest);



        String requestBody = objectMapper.writeValueAsString(values);

        HttpResponse<String> initialResponse = postRequestRasa(RASA_URL, requestBody);

        ResponseIntent mappedResponse = mapResponse(initialResponse.body());

        System.out.println(initialResponse.body());

        return mappedResponse;

    }

    public String getChatResponse(String clientMessage) throws Exception {
        var values = new HashMap<String, String>() {{
            put("sender", "test_user");
            put("message", clientMessage);
        }};

        String requestBody = objectMapper.writeValueAsString(values);

        HttpResponse<String> chatResponse = postRequestRasa(RASA_CONVERSATIONS_URL, requestBody);

        ResponseChatbot[] chatResponseArray = objectMapper.readValue(chatResponse.body(), ResponseChatbot[].class);

        return chatResponseArray[0].getText();
    }

    public HttpResponse<String> postRequestRasa(String url, String jsonValues) throws Exception {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(jsonValues))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        return response;
    }

    public ResponseIntent mapResponse(String responseJson) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ResponseIntent mappedResponse = mapper.readValue(responseJson, ResponseIntent.class);
        return mappedResponse;
    }
    public Latest_Message getLatestMessage() throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(RASA_SLOT_URL))
                .GET()
                .build();
        System.out.println(request);
        HttpResponse<String> response = client
                .send(request, HttpResponse.BodyHandlers.ofString());


        ResponseLM responseLatestMessage = objectMapper.readValue(response.body(), ResponseLM.class);
        System.out.println(responseLatestMessage.getLatest_message().getIntent().getName());
        return responseLatestMessage.getLatest_message();

    }
}
