package com.chatbot.service;

import com.chatbot.websocket.responseMapperChatbot.ResponseChatbot;
import com.chatbot.websocket.responseMapperIntent.Entity;
import com.chatbot.websocket.responseMapperIntent.ResponseIntent;
import com.chatbot.websocket.responseMapperWeather.Weather;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class RasaService {
    WeatherService weatherService = new WeatherService();
    String RASA_URL = "http://localhost:5005/model/parse";
    String RASA_CONVERSATIONS_URL = "http://localhost:5005/webhooks/rest/webhook";
    LocalDate today = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    ObjectMapper objectMapper = new ObjectMapper();

    public ResponseIntent getInitialParameters(String urlParameters) throws Exception {
        var values = new HashMap<String, String>() {{
            put("text", urlParameters);
        }};

        String requestBody = objectMapper.writeValueAsString(values);

        HttpResponse<String> initialResponse = postRequestRasa(RASA_URL, requestBody);

        System.out.println(initialResponse.body());

        ResponseIntent mappedResponse = mapResponse(initialResponse.body());


        System.out.println(mappedResponse.getIntent().getName());
        System.out.println(weatherService.weatherApiCall("2023-01-20T15:00"));
        return mappedResponse;

    }

    public String getChatResponse(String clientMessage) throws Exception {
        var values = new HashMap<String, String>() {{
            put("sender", "test_user");
            put("message", clientMessage);
        }};

        String requestBody = objectMapper.writeValueAsString(values);

        HttpResponse<String> chatResponse = postRequestRasa(RASA_CONVERSATIONS_URL, requestBody);

        System.out.println(chatResponse.body());

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

    public Weather getRequestedWeather(Entity entity) throws Exception {
        System.out.println(today);
        System.out.println(today.plusDays(7));
        switch (entity.getEntity()) {
            case "heute":
                return weatherService.weatherApiCall(today.format(formatter));

            case "morgen":
                return weatherService.weatherApiCall(today.plusDays(1).format(formatter));

            case "kommende Woche":
                return weatherService.weatherApiCall(today.plusDays(7).format(formatter));

            default:
                return null;
        }
    }

    public Weather getRequestedCityWeather(Entity entity, String lat, String lon) throws Exception {
        System.out.println(today);
        System.out.println(today.plusDays(7));

        switch (entity.getEntity()) {
            case "heute":
                return weatherService.cityWeatherApiCall(today.format(formatter), lon, lat);

            case "morgen":
                return weatherService.cityWeatherApiCall(today.plusDays(1).format(formatter), lon, lat);

            case "kommende Woche":
                return weatherService.cityWeatherApiCall(today.plusDays(7).format(formatter), lon, lat);

            default:
                return null;
        }
    }
}
