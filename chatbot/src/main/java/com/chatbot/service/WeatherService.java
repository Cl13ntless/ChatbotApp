package com.chatbot.service;

import com.chatbot.geolocation.Geolocation;
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

    public Weather weatherApiCall(String date) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.brightsky.dev/weather?lat=" + lat + "&lon=" + lon + "&date=" + date + "&last_date=" + date))
                .GET()
                .build();

        HttpResponse<String> response = client
                .send(request, HttpResponse.BodyHandlers.ofString());

        ResponseWeather responseWeather = mapper.readValue(response.body(), ResponseWeather.class);
        Weather[] weathers = responseWeather.getWeather();
        return weathers[0];
    }

    public Weather cityWeatherApiCall(String date, String longitude, String latitude) throws Exception {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.brightsky.dev/weather?lat=" + latitude + "&lon=" + longitude + "&date=" + date + "&last_date=" + date))
                .GET()
                .build();

        HttpResponse<String> response = client
                .send(request, HttpResponse.BodyHandlers.ofString());

        ResponseWeather responseWeather = mapper.readValue(response.body(), ResponseWeather.class);
        Weather[] weathers = responseWeather.getWeather();
        return weathers[0];
    }

    public Geolocation getGeolocation(String location) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.api-ninjas.com/v1/geocoding?city=" + location))
                .setHeader("X-Api-Key", "FDWhrAttUr5pWXScGAim0A==9pba7IQ8WtriLrRi")
                .GET()
                .build();

        HttpResponse<String> response = client
                .send(request, HttpResponse.BodyHandlers.ofString());

        Geolocation[] geolocations = mapper.readValue(response.body(), Geolocation[].class);

        System.out.println(response.body());
        System.out.println(geolocations[0]);

        System.out.println(geolocations[0].getLatitude());
        System.out.println(geolocations[0].getLongitude());
        return geolocations[0];
    }
}
