package com.chatbot.service;

import com.chatbot.geolocation.Geolocation;
import com.chatbot.geolocation.ReverseGeolocation;
import com.chatbot.websocket.responseMapperWeather.ResponseWeather;
import com.chatbot.websocket.responseMapperWeather.Weather;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherService {
    ObjectMapper mapper = new ObjectMapper();
    String lat = "50.93685333230222";
    String lon = "6.9625096284618975";
    String city = "Bielefeld";
    String country = "Deutschland";
    String housenumber = "69";
    String street = "Herforder Straße";

    public void setLat(String lat) {
        this.lat = lat;
    }
    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getHousenumber() {
        return housenumber;
    }

    public String getStreet() {
        return street;
    }

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
        System.out.println("DEBUG:" + location);
        location = location.replaceAll("&uuml;","ue").replaceAll("&Auml;","ae").replaceAll("&ouml;","oe");
        System.out.println("DEBUG:" + location);
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

    public String getReverseGeolocation(String lat, String lon) throws Exception{
        System.out.println(lat);
        System.out.println(lon);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.geoapify.com/v1/geocode/reverse?lat=" + lat +"&lon=" + lon + "&lang=de&apiKey=5a48358bff2b4ff3a8d33b8b8a623dfc"))
                .GET()
                .build();

        HttpResponse<String> response = client
                .send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());

        ReverseGeolocation reverseGeolocations = mapper.readValue(response.body(), ReverseGeolocation.class);
        city = reverseGeolocations.getFeatures()[0].getProperties().getCity();
        street = reverseGeolocations.getFeatures()[0].getProperties().getStreet();
        housenumber = reverseGeolocations.getFeatures()[0].getProperties().getHousenumber();
        country = reverseGeolocations.getFeatures()[0].getProperties().getCountry();
        return city;
    }
}
