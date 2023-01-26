package com.chatbot.service;

import com.chatbot.geolocation.Geolocation;
import com.chatbot.websocket.ServerResponse;
import com.chatbot.websocket.responseMapperIntent.Entity;
import com.chatbot.websocket.responseMapperIntent.ResponseIntent;

import java.util.Objects;

public class ContextService {
    RasaService rasaService;
    WeatherService weatherService;

    public ServerResponse getContextResponse(ResponseIntent response) throws Exception{
        Entity requestedDay = response.getEntities()[0];
        String lat = weatherService.getLat();
        String lon = weatherService.getLon();
        String city = rasaService.getSetSlots().getLocation();
        if(Objects.equals(response.getIntent().getName(),"other_day")){
            if(rasaService.getSetSlots().getLocation() == null){
                rasaService.emptySetSlots();
                return new ServerResponse("Die aktuell vorausgesagte Temperatur für " + weatherService.getReverseGeolocation(lat,lon)+ " "
                        + requestedDay.getEntity() + " sind " + rasaService.getRequestedCityWeather(requestedDay, lat, lon).getTemperature() + " Grad Celsius.");
            }
            rasaService.emptySetSlots();
            Geolocation geolocation =weatherService.getGeolocation(city.replaceAll(" ","/"))
            return new ServerResponse("Die aktuell vorausgesagte Temperatur für " + city + " " + requestedDay.getEntity() +
                    " sind " + rasaService.getRequestedCityWeather(requestedDay,geolocation.getLatitude(), geolocation.getLongitude()) );

        } else if (Objects.equals(response.getIntent().getName(),"other_city")) {

        }
        return null;
    }
}
