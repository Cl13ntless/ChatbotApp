package com.chatbot.service;

import com.chatbot.exception.RasaRequestException;
import com.chatbot.websocket.ResponseLatestMessage.Latest_Message;
import com.chatbot.websocket.ResponseLatestMessage.ResponseLM;
import com.chatbot.websocket.responseMapperChatbot.ResponseChatbot;
import com.chatbot.websocket.responseMapperIntent.ResponseIntent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLOutput;
import java.util.HashMap;

public class RasaService {
    String RASA_URL = "http://rasa:5005/model/parse";
    String RASA_CONVERSATIONS_URL = "http://rasa:5005/webhooks/rest/webhook";
    String RASA_SLOT_URL = "http://rasa:5005/conversations/test_user/tracker";

    String RASA_EMPTY_SLOT_URL = "http://rasa:5005/conversations/test_user/tracker/events";
    ObjectMapper objectMapper = new ObjectMapper();

    public ResponseIntent getInitialParameters(String urlParameters) {
        var values = new HashMap<String, String>() {{
            put("text", urlParameters);
        }};

        //send text to rasa to set slots
        var values2 = new HashMap<String, String>() {{
            put("sender", "test_user");
            put("message", urlParameters);
        }};

        try{
        String chatRequest = objectMapper.writeValueAsString(values2);
        HttpResponse<String> chatResponse = postRequestRasa(RASA_CONVERSATIONS_URL, chatRequest);



        String requestBody = objectMapper.writeValueAsString(values);

        HttpResponse<String> initialResponse = postRequestRasa(RASA_URL, requestBody);

        ResponseIntent mappedResponse = mapResponse(initialResponse.body());

        System.out.println(initialResponse.body());

        return mappedResponse;
        } catch(RasaRequestException e){
            e.printStackTrace();
            System.out.println("Rasa couldnt be rached");
        } catch(JsonProcessingException e){
            e.printStackTrace();
            System.out.println("Object Mapper malfunction");
        }
        return null;
    }

    public String getChatResponse(String clientMessage) {
        var values = new HashMap<String, String>() {{
            put("sender", "test_user");
            put("message", clientMessage);
        }};
        try {
        String requestBody = objectMapper.writeValueAsString(values);

            HttpResponse<String> chatResponse = postRequestRasa(RASA_CONVERSATIONS_URL, requestBody);
            ResponseChatbot[] chatResponseArray = objectMapper.readValue(chatResponse.body(), ResponseChatbot[].class);
            return chatResponseArray[0].getText();

        } catch (RasaRequestException e){
            e.printStackTrace();
            System.out.println("Rasa couldnt be rached");
        } catch (JsonProcessingException e){
            e.printStackTrace();
            System.out.println("Object Mapper malfunction");
        }
        return null;
    }

    public HttpResponse<String> postRequestRasa(String url, String jsonValues) throws RasaRequestException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(jsonValues))
                .build();

        try{
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        return response;
        } catch (InterruptedException e){
            e.printStackTrace();
            throw new RasaRequestException("Interrupted Connection to Rasa");
        } catch (IOException e){
            e.printStackTrace();
            throw new RasaRequestException("Problem with IO in Rasa Request");
        }
    }

    public ResponseIntent mapResponse(String responseJson){
        try{
        ObjectMapper mapper = new ObjectMapper();
        ResponseIntent mappedResponse = mapper.readValue(responseJson, ResponseIntent.class);
        return mappedResponse;
        } catch (JsonProcessingException e){
            e.printStackTrace();
            System.out.println("Mapper malfunction");
            return null;
        }
    }
    public Latest_Message getLatestMessage(){
        try{
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
        } catch (IOException e){
            e.printStackTrace();
            System.out.println("Latest message couldn't be acquired");
        } catch (InterruptedException e){
            e.printStackTrace();
            System.out.println("Latest message couldn't be acquired");
        } catch (URISyntaxException e){
            e.printStackTrace();
            System.out.println("Latest message URI couldn't be build");
        }
        return null;
    }
}
