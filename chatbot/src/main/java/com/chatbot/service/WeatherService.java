package com.chatbot.service;

import com.chatbot.websocket.responseMapperWeather.ResponseWeather;
import com.chatbot.websocket.responseMapperWeather.Weather;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherService {
    ObjectMapper mapper = new ObjectMapper();
    String lat = "48.13923210048965";
    String lon = "11.581793217175251";
    //Weather API call
    public double weatherApiCall(String date) throws  Exception{
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.brightsky.dev/weather?lat="+ lat +"&lon="+ lon +"&date="+ date +"&last_date="+ date))
                .GET()
                .build();

        HttpResponse<String> response = client
                .send(request, HttpResponse.BodyHandlers.ofString());

        ResponseWeather responseWeather = mapper.readValue(response.body(), ResponseWeather.class);
        Weather[] weathers = responseWeather.getWeather();
        return weathers[0].getTemperature();
    }
}
