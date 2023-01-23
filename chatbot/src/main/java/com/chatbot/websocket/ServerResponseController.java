package com.chatbot.websocket;

import com.chatbot.geolocation.Geolocation;
import com.chatbot.service.RasaService;
import com.chatbot.service.WeatherService;
import com.chatbot.websocket.responseMapperIntent.Entity;
import com.chatbot.websocket.responseMapperIntent.ResponseIntent;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.util.Objects;

@Controller
public class ServerResponseController {
    RasaService rasaService = new RasaService();
    WeatherService weatherservice = new WeatherService();

    @MessageMapping("/inquiry")
    @SendTo("/topic/weather")
    public ServerResponse serverResponse(ClientPrompt prompt) throws Exception {

        System.out.println(HtmlUtils.htmlEscape(prompt.getText()));
        rasaService.getInitialParameters(HtmlUtils.htmlEscape(prompt.getText()));
        ResponseIntent mappedResponse = rasaService.getInitialParameters(HtmlUtils.htmlEscape(prompt.getText()));
        if (mappedResponse.getEntities().length > 2) {
            return createErrorResponse();
        }

        if (Objects.equals(mappedResponse.getIntent().getName(), "weather")) {
            if (mappedResponse.getEntities().length != 1) {
                return createErrorResponse();
            }
            //Wetter für standarmäßigen Standort / Standort wird von Wetter API abgefragt

            Entity requestedDay = mappedResponse.getEntities()[0];
            String lat = weatherservice.getLat();
            String lon = weatherservice.getLon();
            return new ServerResponse("Die aktuell vorausgesagte Temperatur für " + weatherservice.getReverseGeolocation(lat,lon)+ " "
                    + requestedDay.getEntity() + " sind " + rasaService.getRequestedCityWeather(requestedDay, lat, lon).getTemperature() + " Grad Celsius.");

        } else if (Objects.equals(mappedResponse.getIntent().getName(), "city_weather")) {
            if (mappedResponse.getEntities().length != 2) {
                return createErrorResponse();
            }
            //Wenn eine Stadt übergeben wird müssen lat und lon geholt und der WetterAPI Call abhängig von ihnen gemacht werden

            Geolocation geolocation = weatherservice.getGeolocation(mappedResponse.getEntities()[0].getValue().replaceAll(" ","/"));
            String lat = String.valueOf(geolocation.getLatitude());
            String lon = String.valueOf(geolocation.getLongitude());
            Entity day = mappedResponse.getEntities()[1];
            String city = mappedResponse.getEntities()[0].getValue().replaceAll("\\?","");

            return new ServerResponse("Die aktuell vorausgesagte Temperatur für " + day.getEntity() + " in " + city + " sind "
                    + rasaService.getRequestedCityWeather(day, lat, lon).getTemperature() + " Grad Celcius");
        }

        if (mappedResponse.getEntities().length != 0) {
            return createErrorResponse();
        }
        //Request zurück an Rasa für die standard Chatbot Antwort

        return new ServerResponse(rasaService.getChatResponse(HtmlUtils.htmlEscape(prompt.getText())));
    }

    @MessageMapping("/lat")
    public void getLat(String lat){
        System.out.println(lat);
        weatherservice.setLat(lat);
    }

    @MessageMapping("/lon")
    @SendTo("/topic/currentLoc")
    public ServerResponse getLon(String lon) throws Exception{
        System.out.println(lon);
        weatherservice.setLon(lon);
        weatherservice.getReverseGeolocation(weatherservice.getLat(), lon);
        return new ServerResponse("Aktuelle Position: " + weatherservice.getCity() + " " + weatherservice.getStreet()
                + " " + weatherservice.getHousenumber()+ " , " + weatherservice.getCountry());
    }

    public ServerResponse createErrorResponse() {
        return new ServerResponse("Huch! Ich scheine dich nicht richtig verstanden zu haben. Versuche es nochmal! Achte auf deine Rechtschreibung!");
    }
}
