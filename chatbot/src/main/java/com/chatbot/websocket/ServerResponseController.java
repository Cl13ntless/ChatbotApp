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
    public ServerResponse serverResponse(ClientPrompt prompt) throws Exception{

        System.out.println(HtmlUtils.htmlEscape(prompt.getText()));
        rasaService.getInitialParameters(HtmlUtils.htmlEscape(prompt.getText()));
        ResponseIntent mappedResponse = rasaService.getInitialParameters(HtmlUtils.htmlEscape(prompt.getText()));
        if(mappedResponse.getEntities().length > 2){
            return createErrorResponse();
        }

        if(Objects.equals(mappedResponse.getIntent().getName(), "weather")){
            if(mappedResponse.getEntities().length != 1){
                return createErrorResponse();
            }
            //Wetter für standarmäßigen Standort wird von Wetter API abgefragt

            Entity requestedDay = mappedResponse.getEntities()[0];
            return new ServerResponse("Die aktuell vorausgesagte Temperatur für München " + requestedDay.getEntity() + " sind " + rasaService.getRequestedWeather(requestedDay).getTemperature()+ " Grad Celsius.");

        } else if (Objects.equals(mappedResponse.getIntent().getName(), "city_weather")) {
            if(mappedResponse.getEntities().length != 2){
                return createErrorResponse();
            }
            //Wenn eine Stadt übergeben wird müssen lat und lon geholt und der WetterAPI Call abhängig von ihnen gemacht werden

            Geolocation geolocation = weatherservice.getGeolocation(mappedResponse.getEntities()[0].getValue());
            String lat = String.valueOf(geolocation.getLatitude());
            String lon = String.valueOf(geolocation.getLongitude());
            Entity day = mappedResponse.getEntities()[1];
            String city = geolocation.getName();

            return new ServerResponse("Die aktuell vorausgesagte Temperatur für " + day.getEntity() + " in "+ city + " sind " + rasaService.getRequestedCityWeather(day,lat,lon).getTemperature() + " Grad Celcius");
        }

        if(mappedResponse.getEntities().length != 0){
            return createErrorResponse();
        }
        //Request zurück an Rasa für die standard Chatbot Antwort

        return new ServerResponse(rasaService.getChatResponse(HtmlUtils.htmlEscape(prompt.getText())));
    }

    public ServerResponse createErrorResponse(){
        return new ServerResponse("Huch! Ich scheine dich nicht richtig verstanden zu haben. Versuche es nochmal! Achte auf deine Rechtschreibung!");
    }
}
