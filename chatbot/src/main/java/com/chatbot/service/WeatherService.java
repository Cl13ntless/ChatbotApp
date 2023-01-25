package com.chatbot.service;

import com.chatbot.geolocation.Geolocation;
import com.chatbot.geolocation.ReverseGeolocation;
import com.chatbot.websocket.responseMapperWeather.ResponseWeather;
import com.chatbot.websocket.responseMapperWeather.Weather;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.core5.net.URIBuilder;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherService {
    ObjectMapper mapper = new ObjectMapper();
    String lat = "52.03096758574192";
    String lon = "8.537116459846818";
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

    public Weather cityWeatherApiCall(String date, String longitude, String latitude) throws Exception {
        URIBuilder builder = new URIBuilder();
        builder.setScheme("https")
                .setHost("api.brightsky.dev")
                .setPath("/weather")
                .addParameter("lat",latitude)
                .addParameter("lon",longitude)
                .addParameter("date",date+"T14:00")
                .addParameter("last_date",date+"T14:00");

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(builder.build().toURL().toURI())
                .GET()
                .build();

        HttpResponse<String> response = client
                .send(request, HttpResponse.BodyHandlers.ofString());

        ResponseWeather responseWeather = mapper.readValue(response.body(), ResponseWeather.class);
        Weather[] weathers = responseWeather.getWeather();
        return weathers[0];
    }

    public Geolocation getGeolocation(String location) throws Exception {
        location = location.replaceAll("&uuml;","ue").replaceAll("&Auml;","ae").replaceAll("&ouml;","oe");
        URIBuilder builder = new URIBuilder();
        builder.setScheme("https")
                .setHost("api.api-ninjas.com")
                .setPath("/v1/geocoding")
                .addParameter("city",location);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(builder.build().toURL().toURI())
                .setHeader("X-Api-Key", "FDWhrAttUr5pWXScGAim0A==9pba7IQ8WtriLrRi")
                .GET()
                .build();

        HttpResponse<String> response = client
                .send(request, HttpResponse.BodyHandlers.ofString());

        Geolocation[] geolocations = mapper.readValue(response.body(), Geolocation[].class);

        return geolocations[0];
    }

    public String getReverseGeolocation(String lat, String lon) throws Exception{
        URIBuilder builder = new URIBuilder();
        builder.setScheme("https")
                .setHost("api.geoapify.com")
                .setPath("/v1/geocode/reverse")
                .addParameter("lat",lat)
                .addParameter("lon",lon)
                .addParameter("lang","de")
                .addParameter("apiKey","5a48358bff2b4ff3a8d33b8b8a623dfc");

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(builder.build().toURL().toURI())
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
