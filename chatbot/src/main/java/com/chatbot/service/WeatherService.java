package com.chatbot.service;

import com.chatbot.exception.GeolocationException;
import com.chatbot.exception.ReverseGeolocationException;
import com.chatbot.exception.WeatherAPIException;
import com.chatbot.geolocation.Geolocation;
import com.chatbot.geolocation.ReverseGeolocation;
import com.chatbot.websocket.responseMapperWeather.ResponseWeather;
import com.chatbot.websocket.responseMapperWeather.Weather;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.core5.net.URIBuilder;


import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class WeatherService {
    ObjectMapper mapper = new ObjectMapper();

    String currentLat = "52.03096758574192";
    String currentLon = "8.537116459846818";
    String lat = "52.03096758574192";
    String lon = "8.537116459846818";
    String city = "Bielefeld";
    String country = "Deutschland";
    String housenumber = "69";
    String street = "Herforder Straße";
    String day = "2023-01-27T08:00:00.000+01:00";
    String weatherIcon;

    String hour;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy").withLocale(Locale.GERMANY);

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

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setHousenumber(String housenumber) {
        this.housenumber = housenumber;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getDay() {
        OffsetDateTime dt = OffsetDateTime.parse(day, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return formatter.format(dt);
    }

    public String getHour() {
        OffsetDateTime dt = OffsetDateTime.parse(day, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return DateTimeFormatter.ofPattern("HH").withLocale(Locale.GERMANY).format(dt);
    }

    public String getCurrentLat() {
        return currentLat;
    }

    public void setCurrentLat(String currentLat) {
        this.currentLat = currentLat;
    }

    public String getCurrentLon() {
        return currentLon;
    }

    public void setCurrentLon(String currentLon) {
        this.currentLon = currentLon;
    }

    public String getWeatherIcon() {
        return weatherIcon;
    }

    public void setWeatherIcon(String weatherIcon) {
        this.weatherIcon = weatherIcon;
    }

    public Weather cityWeatherApiCall(boolean currentPos) throws WeatherAPIException {
        System.out.println("LAT: " + lat);
        System.out.println("LON: " + lon);
        System.out.println("DAY: " + day);
        URIBuilder builder = new URIBuilder();
        if(!currentPos){
        builder.setScheme("https")
                .setHost("api.brightsky.dev")
                .setPath("/weather")
                .addParameter("lat",lat)
                .addParameter("lon",lon)
                .addParameter("date",day)
                .addParameter("last_date",day);
        } else {
            builder.setScheme("https")
                    .setHost("api.brightsky.dev")
                    .setPath("/weather")
                    .addParameter("lat",currentLat)
                    .addParameter("lon",currentLon)
                    .addParameter("date",day)
                    .addParameter("last_date",day);
        }

        try{
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(builder.build().toURL().toURI())
                .GET()
                .build();

        HttpResponse<String> response = client
                .send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(request);
        System.out.println(response.body());
        ResponseWeather responseWeather = mapper.readValue(response.body(), ResponseWeather.class);
        Weather[] weathers = responseWeather.getWeather();
        weatherIcon = weathers[0].getIcon();
        return weathers[0];

        } catch(IOException e){
            System.out.println("IO Exception");
            e.printStackTrace();
            throw new WeatherAPIException("IO Exception!");
        } catch(URISyntaxException e){
            System.out.println("Malformed Syntax!");
            e.printStackTrace();
            throw new WeatherAPIException("Malformed URI!");
        } catch(InterruptedException e){
            System.out.println("Http Connection got Interrupted");
            e.printStackTrace();
            throw new WeatherAPIException("Connection got Interrupted");
        }
    }

    public Geolocation getGeolocation(String location) throws GeolocationException {
        location = location.replaceAll("&uuml;","ue")
                .replaceAll("&Auml;","ae")
                .replaceAll("&ouml;","oe")
                .replaceAll(" ","/");
        URIBuilder builder = new URIBuilder();
        builder.setScheme("https")
                .setHost("api.api-ninjas.com")
                .setPath("/v1/geocoding")
                .addParameter("city",location);

        try {
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
        } catch(IOException e){
            System.out.println("IO Exception");
            e.printStackTrace();
            throw new GeolocationException("IO Exception!");
        } catch(URISyntaxException e){
            System.out.println("Malformed Syntax!");
            e.printStackTrace();
            throw new GeolocationException("Malformed URI!");
        } catch(InterruptedException e){
            System.out.println("Http Connection got Interrupted");
            e.printStackTrace();
            throw new GeolocationException("Connection got Interrupted");
        }
    }

    public String getReverseGeolocation() throws ReverseGeolocationException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme("https")
                .setHost("api.geoapify.com")
                .setPath("/v1/geocode/reverse")
                .addParameter("lat",currentLat)
                .addParameter("lon",currentLon)
                .addParameter("lang","de")
                .addParameter("apiKey","5a48358bff2b4ff3a8d33b8b8a623dfc");

        try{
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
        } catch(IOException e){
            System.out.println("IO Exception");
            e.printStackTrace();
            throw new ReverseGeolocationException("IO Exception!");
        } catch(URISyntaxException e){
            System.out.println("Malformed Syntax!");
            e.printStackTrace();
            throw new ReverseGeolocationException("Malformed URI!");
        } catch(InterruptedException e){
            System.out.println("Http Connection got Interrupted");
            e.printStackTrace();
            throw new ReverseGeolocationException("Connection got Interrupted");
        }
    }
}
