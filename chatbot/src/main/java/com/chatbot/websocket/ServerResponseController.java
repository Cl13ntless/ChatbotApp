package com.chatbot.websocket;

import com.chatbot.exception.GeolocationException;
import com.chatbot.exception.ReverseGeolocationException;
import com.chatbot.exception.WeatherAPIException;
import com.chatbot.geolocation.Geolocation;
import com.chatbot.service.RasaService;
import com.chatbot.service.WeatherService;
import com.chatbot.websocket.ResponseLatestMessage.Latest_Message;
import com.chatbot.websocket.responseMapperIntent.ResponseIntent;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.Objects;

@Controller
public class ServerResponseController {
    RasaService rasaService = new RasaService();
    WeatherService weatherService = new WeatherService();
    String[] intentNames = {"weather","city_weather","other_day","other_city"};

    @MessageMapping("/inquiry")
    @SendTo("/topic/weather")
    public ServerResponse serverResponse(ClientPrompt prompt){
        Latest_Message lm = rasaService.getLatestMessage();
        ResponseIntent mappedResponse = rasaService.getInitialParameters(HtmlUtils.htmlEscape(prompt.getText()));
        mapToSlots(mappedResponse);

        if (Objects.equals(mappedResponse.getIntent().getName(), "weather")) {
            if (mappedResponse.getEntities().length != 1) {
                return createErrorResponse();
            }
            //Wetter für standarmäßigen Standort / Standort wird von Wetter API abgefragt
            String requestedDay = weatherService.getDay();
            try{

                return new ServerResponse("Die aktuell vorausgesagte Temperatur für " + weatherService.getReverseGeolocation()
                    + " am " + requestedDay + " um " + weatherService.getHour() +
                    " Uhr beträgt " +  weatherService.cityWeatherApiCall(true).getTemperature() + " Grad Celcius");

            } catch (ReverseGeolocationException e){
                e.printStackTrace();
                System.out.println("Couldn't get Reverse Geolocation");
            } catch (WeatherAPIException e){
                e.printStackTrace();
                System.out.println("Couldn't reach Weather API");
            }

        } else if (Objects.equals(mappedResponse.getIntent().getName(), "city_weather")) {

            if (mappedResponse.getEntities().length != 2) {
                return createErrorResponse();
            }
            //Wenn eine Stadt übergeben wird müssen lat und lon geholt und der WetterAPI Call abhängig von ihnen gemacht werden
            String day = weatherService.getDay();
            String city = weatherService.getCity()
                    .replaceAll("&uuml;","ü")
                    .replaceAll("&Auml;","ä")
                    .replaceAll("&ouml;","ö");
            try {
                return new ServerResponse("Die aktuell vorausgesagte Temperatur für " + city + " am " + day + " um " + weatherService.getHour() +
                        " Uhr beträgt " + weatherService.cityWeatherApiCall(false).getTemperature() + " Grad Celcius");
            } catch (WeatherAPIException e){
                e.printStackTrace();
                System.out.println("Couldn't reach Weather API");
            }

        } else if ((Objects.equals(mappedResponse.getIntent().getName(), "other_day") || Objects.equals(mappedResponse.getIntent().getName(), "other_city")) && (Objects.equals(lm.getIntent().getName(),"weather") || Objects.equals(lm.getIntent().getName(),"city_weather"))) {

            if (mappedResponse.getEntities().length != 1) {
                return createErrorResponse();
            }
            //Wenn ein anderer Tag übergeben wird, wird mit den vorhandenen Daten eine Anfrage gestartet

            String day = weatherService.getDay();
            String city = weatherService.getCity()
                    .replaceAll("&uuml;","ü")
                    .replaceAll("&Auml;","ä")
                    .replaceAll("&ouml;","ö");

            try {
                return new ServerResponse("Die aktuell vorausgesagte Temperatur für " + city + " am " + day + " um " + weatherService.getHour() +
                        " Uhr beträgt " + weatherService.cityWeatherApiCall(false).getTemperature() + " Grad Celcius");
            } catch (WeatherAPIException e){
                e.printStackTrace();
                System.out.println("Couldn't reach Weather API");
            }
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
        weatherService.setCurrentLat(lat);
        try{
        weatherService.getReverseGeolocation();
        } catch ( ReverseGeolocationException e){
            e.printStackTrace();
            System.out.println("Couldn't Get Reverse Geolocation");
        }

    }

    @MessageMapping("/lon")
    @SendTo("/topic/currentLoc")
    public ServerResponse getLon(String lon){
        System.out.println(lon);
        weatherService.setCurrentLon(lon);
        try{
            weatherService.getReverseGeolocation();
        } catch ( ReverseGeolocationException e ){
            e.printStackTrace();
            System.out.println("Couldn't Get Reverse Geolocation");
        }
        return new ServerResponse("Aktuelle Position: " + weatherService.getCity() + " " + weatherService.getStreet()
                + " " + weatherService.getHousenumber()+ " , " + weatherService.getCountry());
    }

    @MessageMapping("/icon")
    @SendTo("/topic/icon")
    public ServerResponse sendIcon(){
        if(List.of(intentNames).contains(rasaService.getLatestMessage().getIntent().getName())){
            return new ServerResponse(weatherService.getWeatherIcon());
        }
        return null;
    }
    public ServerResponse createErrorResponse() {
        weatherService.setWeatherIcon(null);
        return new ServerResponse("Huch! Ich scheine dich nicht richtig verstanden zu haben. Versuche es nochmal! Achte auf deine Rechtschreibung!");
    }

    public void mapToSlots(ResponseIntent response){
        if(response.getEntities().length == 2){
            weatherService.setCity(response.getEntities()[0].getValue().replaceAll("\\?",""));
            weatherService.setDay(response.getEntities()[1].getValue());
            try{
                Geolocation geolocation = weatherService.getGeolocation(weatherService.getCity());
                System.out.println(weatherService.getLon());
                weatherService.setLat(String.valueOf(geolocation.getLatitude()));
                weatherService.setLon(String.valueOf(geolocation.getLongitude()));
                System.out.println(weatherService.getLon());
            } catch ( GeolocationException e ){
                e.printStackTrace();
                System.out.println("Geolocation could not be mapped!");
            }

        }
        if(response.getEntities().length == 1 && Objects.equals(response.getIntent().getName(),"weather" )){
            weatherService.setDay(response.getEntities()[0].getValue());
        }

        if(response.getEntities().length == 1 && Objects.equals(response.getIntent().getName(),"other_day" )){
            weatherService.setDay(response.getEntities()[0].getValue());
        }

        if(response.getEntities().length == 1 && Objects.equals(response.getIntent().getName(),"other_city" )){
            weatherService.setCity(response.getEntities()[0].getValue().replaceAll("\\?",""));

            try{
                Geolocation geolocation = weatherService.getGeolocation(weatherService.getCity());
                System.out.println(weatherService.getLon());
                weatherService.setLat(String.valueOf(geolocation.getLatitude()));
                weatherService.setLon(String.valueOf(geolocation.getLongitude()));
                System.out.println(weatherService.getLon());
            } catch ( GeolocationException e ){
                e.printStackTrace();
                System.out.println("Geolocation could not be mapped!");
            }
        }
    }

}
