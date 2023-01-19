package com.chatbot.service;

import com.chatbot.websocket.responseMapperIntent.Response;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

public class RasaService {
    //execute Post Funktion
    WeatherService weatherService = new WeatherService();
    String RASA_URL = "http://localhost:5005/model/parse";

    public void executePost(String urlParameters) throws Exception{
        var values = new HashMap<String, String>() {{
            put("text", urlParameters);
        }};

        var objectMapper = new ObjectMapper();
        String requestBody = objectMapper
                .writeValueAsString(values);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:5005/model/parse"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());

        Response mappedResponse = mapResponse(response.body());

        System.out.println(mappedResponse.getIntent().getName());
        System.out.println(weatherService.weatherApiCall("2023-01-20T15:00"));

    }
    public Response mapResponse(String responseJson) throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        Response response = mapper.readValue(responseJson, Response.class);
        return response;
    }

}
