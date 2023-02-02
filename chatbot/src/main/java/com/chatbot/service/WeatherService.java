package com.chatbot.service;

import com.chatbot.exception.GeolocationException;
import com.chatbot.exception.ReverseGeolocationException;
import com.chatbot.exception.WeatherAPIException;
import com.chatbot.geolocation.Geolocation;
import com.chatbot.geolocation.ReverseGeolocation;
import com.chatbot.websocket.responseMapperWeather.ResponseWeather;
import com.chatbot.websocket.responseMapperWeather.Weather;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.hc.core5.net.URIBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
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

    String countryCode = "DE";

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy").withLocale(Locale.GERMANY);

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getHousenumber() {
        return housenumber;
    }

    public void setHousenumber(String housenumber) {
        this.housenumber = housenumber;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getDay() {
        OffsetDateTime dt = OffsetDateTime.parse(day, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return formatter.format(dt);
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getHour() {
        OffsetDateTime dt = OffsetDateTime.parse(day, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return DateTimeFormatter.ofPattern("HH:mm").withLocale(Locale.GERMANY).format(dt);
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

    //Weather API Call mit Längen und Breitengraden
    public Weather cityWeatherApiCall(boolean currentPos) throws WeatherAPIException {
        System.out.println("LAT: " + lat);
        System.out.println("LON: " + lon);
        System.out.println("DAY: " + day);
        OffsetDateTime dateTime = OffsetDateTime.parse(day);
        Date requested = new Date(dateTime.toInstant().toEpochMilli());
        requested = DateUtils.round(requested,Calendar.HOUR);
        dateTime = requested.toInstant().atOffset(ZoneOffset.ofHours(1));
        String roundedDay = dateTime.toString();

        URIBuilder builder = new URIBuilder();
        if (!currentPos) {
            builder.setScheme("https")
                    .setHost("api.brightsky.dev")
                    .setPath("/weather")
                    .addParameter("lat", lat)
                    .addParameter("lon", lon)
                    .addParameter("date", roundedDay)
                    .addParameter("last_date", roundedDay);
        } else {
            builder.setScheme("https")
                    .setHost("api.brightsky.dev")
                    .setPath("/weather")
                    .addParameter("lat", currentLat)
                    .addParameter("lon", currentLon)
                    .addParameter("date", roundedDay)
                    .addParameter("last_date", roundedDay);
        }

        try {
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

        } catch (IOException e) {
            System.out.println("IO Exception");
            e.printStackTrace();
            throw new WeatherAPIException("IO Exception!");
        } catch (URISyntaxException e) {
            System.out.println("Malformed Syntax!");
            e.printStackTrace();
            throw new WeatherAPIException("Malformed URI!");
        } catch (InterruptedException e) {
            System.out.println("Http Connection got Interrupted");
            e.printStackTrace();
            throw new WeatherAPIException("Connection got Interrupted");
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No Weather Data Returned!");
            e.printStackTrace();
            throw new WeatherAPIException("No Weather Data");
        }
    }

    public Geolocation getGeolocation(String location) throws GeolocationException {
        location = location.replaceAll("&uuml;", "ue")
                .replaceAll("&Auml;", "ae")
                .replaceAll("&ouml;", "oe")
                .replaceAll(" ", "/");
        URIBuilder builder = new URIBuilder();
        builder.setScheme("https")
                .setHost("api.api-ninjas.com")
                .setPath("/v1/geocoding")
                .addParameter("city", location);

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
            setCountryCode(geolocations[0].getCountry());

            System.out.println(response.body());
            return geolocations[0];
        } catch (IOException e) {
            System.out.println("IO Exception");
            e.printStackTrace();
            throw new GeolocationException("IO Exception!");
        } catch (URISyntaxException e) {
            System.out.println("Malformed Syntax!");
            e.printStackTrace();
            throw new GeolocationException("Malformed URI!");
        } catch (InterruptedException e) {
            System.out.println("Http Connection got Interrupted");
            e.printStackTrace();
            throw new GeolocationException("Connection got Interrupted");
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("There was no Geolocation Returned!");
            e.printStackTrace();
            throw new GeolocationException("No Geolocation Returned!");
        }
    }

    //Getting Reverse Geolocation from current lat and lon values test
    public String getReverseGeolocation() throws ReverseGeolocationException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme("https")
                .setHost("api.geoapify.com")
                .setPath("/v1/geocode/reverse")
                .addParameter("lat", currentLat)
                .addParameter("lon", currentLon)
                .addParameter("lang", "de")
                .addParameter("apiKey", "5a48358bff2b4ff3a8d33b8b8a623dfc");

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(builder.build().toURL().toURI())
                    .GET()
                    .build();

            HttpResponse<String> response = client
                    .send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println(response.body());

            ReverseGeolocation reverseGeolocations = mapper.readValue(response.body(), ReverseGeolocation.class);
            setCountryCode(reverseGeolocations.getFeatures()[0].getProperties().getCountry_code());
            city = reverseGeolocations.getFeatures()[0].getProperties().getCity();
            street = reverseGeolocations.getFeatures()[0].getProperties().getStreet();
            housenumber = reverseGeolocations.getFeatures()[0].getProperties().getHousenumber();
            country = reverseGeolocations.getFeatures()[0].getProperties().getCountry();
            System.out.println("Reverse Geolocation von: " + city);
            return city;
        } catch (IOException e) {
            System.out.println("IO Exception");
            e.printStackTrace();
            throw new ReverseGeolocationException("IO Exception!");
        } catch (URISyntaxException e) {
            System.out.println("Malformed Syntax!");
            e.printStackTrace();
            throw new ReverseGeolocationException("Malformed URI!");
        } catch (InterruptedException e) {
            System.out.println("Http Connection got Interrupted");
            e.printStackTrace();
            throw new ReverseGeolocationException("Connection got Interrupted");
        } catch (NullPointerException e) {
            System.out.println("Lat and Lon Values couldnt be read");
            e.printStackTrace();
            throw new ReverseGeolocationException("Lat and Lon Values couldnt be read");
        }
    }
}
