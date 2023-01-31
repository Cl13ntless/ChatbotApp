package com.chatbot.service;

import com.chatbot.exception.GeolocationException;
import com.chatbot.exception.ReverseGeolocationException;
import com.chatbot.exception.WeatherAPIException;
import com.chatbot.geolocation.Geolocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

public class WeatherServiceTest {

    private WeatherService weatherService;

    @BeforeEach
    public void beforeEach(){
        weatherService = new WeatherService();
    }


    @Test
    public void cityWeatherApiCallTest() throws WeatherAPIException {
        Assert.notNull(weatherService.cityWeatherApiCall(true),"Current Position should not be null");
        Assert.notNull(weatherService.cityWeatherApiCall(false),"Current Position should not be null");
    }

    @Test
    public void getGeolocationTest()throws GeolocationException {
        String location = "Lagos";
        String expectedLat = "6.4550575";
        String expectedLon = "3.3941795";

        Geolocation geolocation = weatherService.getGeolocation(location);
        String actualLat = String.valueOf(geolocation.getLatitude());
        String actualLon = String.valueOf(geolocation.getLongitude());

        Assertions.assertEquals(expectedLat,actualLat);
        Assertions.assertEquals(expectedLon, actualLon);
    }

    @Test
    public void falseGeolocationtest() throws GeolocationException{
        String nonLocation = "haerlas";

        Assertions.assertThrows(GeolocationException.class, () -> {
            weatherService.getGeolocation(nonLocation);
        });

    }

    @Test
    public void getReverseGeolocationTest() throws ReverseGeolocationException {
        weatherService.setCurrentLat("52.03096758574192");
        weatherService.setCurrentLon("8.537116459846818");
        weatherService.getReverseGeolocation();

        Assertions.assertEquals("Bielefeld",weatherService.getCity());
    }

    @Test
    public void getReverseGeolocationException(){
        weatherService.setCurrentLon("123412");
        weatherService.setCurrentLat("124562");

        Assertions.assertThrows(ReverseGeolocationException.class, () -> {
            weatherService.getReverseGeolocation();
        });
    }
}