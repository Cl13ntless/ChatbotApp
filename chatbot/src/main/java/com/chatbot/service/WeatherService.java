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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WeatherService {
    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);
    private static final String HTTPS = "https";
    private static final String WEATHER_HOST = "api.brightsky.dev";
    private static final String WEATHER_PATH = "/weather";
    ObjectMapper mapper = new ObjectMapper();

    String currentLat = "52.03096758574192";
    String currentLon = "8.537116459846818";
    String lat = "52.03096758574192";
    String lon = "8.537116459846818";
    String city = "Bielefeld";
    String country = "Deutschland";
    String houseNumber = "69";
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

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
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
    public Weather cityWeatherApiCall(boolean currentPos) throws WeatherAPIException, DateTimeParseException {
        logger.info("LAT: {}", lat);
        logger.info("LON: {}", lon);
        logger.info("DAY: {}", day);
        OffsetDateTime dateTime = OffsetDateTime.parse(day);
        Date requested = new Date(dateTime.toInstant().toEpochMilli());
        requested = DateUtils.round(requested, Calendar.HOUR);
        dateTime = requested.toInstant().atOffset(ZoneOffset.ofHours(1));
        String roundedDay = dateTime.toString();

        URIBuilder builder = new URIBuilder();
        if (!currentPos) {
            builder.setScheme(HTTPS)
                    .setHost(WEATHER_HOST)
                    .setPath(WEATHER_PATH)
                    .addParameter("lat", lat)
                    .addParameter("lon", lon)
                    .addParameter("date", roundedDay)
                    .addParameter("last_date", roundedDay);
        } else {
            builder.setScheme(HTTPS)
                    .setHost(WEATHER_HOST)
                    .setPath(WEATHER_PATH)
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

            logger.info("Weather response: {}", response.body());
            ResponseWeather responseWeather = mapper.readValue(response.body(), ResponseWeather.class);
            Weather[] weathers = responseWeather.getWeather();
            weatherIcon = weathers[0].getIcon();
            return weathers[0];

        } catch (IOException e) {
            logger.error("IO Exception in City Weather API call!");
            e.printStackTrace();
            throw new WeatherAPIException("");
        } catch (URISyntaxException e) {
            logger.error("Malformed Syntax in City Weather API call!");
            e.printStackTrace();
            throw new WeatherAPIException("");
        } catch (InterruptedException e) {
            logger.error("Connection got Interrupted in City Weather API call!");
            Thread.currentThread().interrupt();
            e.printStackTrace();
            throw new WeatherAPIException("");
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("No Weather Data Returned!");
            e.printStackTrace();
            throw new WeatherAPIException("No Weather Data");
        }
    }

//    Geolocation API Call → Stadtname wird an API geschickt um Lat und Lon zu bekommen
    public Geolocation getGeolocation(String location) throws GeolocationException {
        location = location
                .replace("&uuml;", "ue")
                .replace("&Auml;", "ae")
                .replace("&ouml;", "oe")
                .replace(" ", "/");
        URIBuilder builder = new URIBuilder();
        builder.setScheme(HTTPS)
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

            logger.error(response.body());
            return geolocations[0];

        } catch (URISyntaxException | ArrayIndexOutOfBoundsException | IOException e) {
            logger.error("Exception in getGeolocation");
            e.printStackTrace();
            throw new GeolocationException("");
        } catch (InterruptedException e) {
            logger.error("Http Connection got Interrupted in getGeolocation");
            Thread.currentThread().interrupt();
            e.printStackTrace();
            throw new GeolocationException("");
        }
    }

//    Getting Reverse Geolocation from current lat and lon values
    public void getReverseGeolocation() throws ReverseGeolocationException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(HTTPS)
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

            logger.info("Reverse Geolocation Body: {}", response.body());

            ReverseGeolocation reverseGeolocations = mapper.readValue(response.body(), ReverseGeolocation.class);
            setCountryCode(reverseGeolocations.getFeatures()[0].getProperties().getCountry_code());
            city = reverseGeolocations.getFeatures()[0].getProperties().getCity();
            street = reverseGeolocations.getFeatures()[0].getProperties().getStreet();
            houseNumber = reverseGeolocations.getFeatures()[0].getProperties().getHousenumber();
            country = reverseGeolocations.getFeatures()[0].getProperties().getCountry();
            logger.info("Reverse Geolocation von: {}", city);

        } catch (URISyntaxException | NullPointerException | IOException e) {
            logger.error("Exception in Reverse Geolocation");
            e.printStackTrace();
            throw new ReverseGeolocationException("IO Exception!");
        } catch (InterruptedException e) {
            logger.error("Http Connection got Interrupted");
            e.printStackTrace();
            Thread.currentThread().interrupt();
            throw new ReverseGeolocationException("Connection got Interrupted");
        }
    }
}
