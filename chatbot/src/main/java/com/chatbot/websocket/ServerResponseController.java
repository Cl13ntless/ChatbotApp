package com.chatbot.websocket;

import com.chatbot.exception.GeolocationException;
import com.chatbot.exception.ReverseGeolocationException;
import com.chatbot.exception.WeatherAPIException;
import com.chatbot.geolocation.Geolocation;
import com.chatbot.service.RasaService;
import com.chatbot.service.TranslationService;
import com.chatbot.service.WeatherService;
import com.chatbot.websocket.ResponseLatestMessage.Latest_Message;
import com.chatbot.websocket.responseMapperIntent.ResponseIntent;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.Objects;

@Controller
public class ServerResponseController {
    TranslationService translationService = new TranslationService();
    RasaService rasaService = new RasaService();
    WeatherService weatherService = new WeatherService();
    String[] intentNames = {"weather", "city_weather", "other_day", "other_city"};
    String city;
    String countryCode;
    String requestedDay;
    String hour;
    String temperature;
    Double CONFIDENCE_THRESHOLD = 0.9;
    String currentLang = "de";

    @MessageMapping("/inquiry")
    @SendTo("/topic/weather")
    public ServerResponse serverResponse(ClientPrompt prompt) {
        Latest_Message lm = rasaService.getLatestMessage();
        ResponseIntent mappedResponse = rasaService.getInitialParameters(HtmlUtils.htmlEscape(prompt.getText()));

        try {
            mapToSlots(mappedResponse);
        } catch (GeolocationException e) {
            return createErrorResponse();
        } catch (ReverseGeolocationException e){
            return createErrorResponse();
        }

        city = StringUtils.capitalize(weatherService.getCity()
                .replaceAll("&uuml;", "ü")
                .replaceAll("&Auml;", "ä")
                .replaceAll("&ouml;", "ö"));
        countryCode = weatherService.getCountryCode().toUpperCase();
        requestedDay = weatherService.getDay();
        hour = weatherService.getHour();

        try {

            switch (mappedResponse.getIntent().getName()) {
                case "weather":
                    if (mappedResponse.getEntities().length != 1) {
                        return createErrorResponse();
                    }
                    temperature = String.valueOf(weatherService.cityWeatherApiCall(true).getTemperature());
                    break;

                case "city_weather":
                    if (mappedResponse.getEntities().length != 2) {
                        return createErrorResponse();
                    }
                    temperature = String.valueOf(weatherService.cityWeatherApiCall(false).getTemperature());
                    break;

                case "other_day", "other_city":
                    if (mappedResponse.getEntities().length != 1) {
                        return createErrorResponse();
                    }
                    if (Objects.equals(lm.getIntent().getName(), "weather")) {
                        temperature = String.valueOf(weatherService.cityWeatherApiCall(true).getTemperature());
                        break;
                    } else if (Objects.equals(lm.getIntent().getName(), "city_weather")) {
                        temperature = String.valueOf(weatherService.cityWeatherApiCall(false).getTemperature());
                    }
                    return createErrorResponse();

                default:
                    if ( mappedResponse.getEntities().length != 0 || mappedResponse.getIntent().getConfidence() < CONFIDENCE_THRESHOLD ) {
                        return createErrorResponse();
                    }
                    //Request zurück an Rasa für die standard Chatbot Antwort
                    return new ServerResponse(translateMessageIfNeeded(rasaService.getChatResponse(HtmlUtils.htmlEscape(prompt.getText()))));

            }
        } catch (WeatherAPIException e) {
            e.printStackTrace();
            return createErrorResponse();
        }

        String response = "Die aktuell vorausgesagte Temperatur für " + city + "," + countryCode + " am "
                + requestedDay + " um " + hour + " Uhr beträgt " + temperature + " Grad Celcius";

        if(Objects.equals(currentLang, "en")){
            String responseEn = "Die aktuell vorausgesagte Temperatur für " + city + "," + countryCode + " am "
                    + requestedDay + " um " + hour + " Uhr - beträgt " + temperature + " Grad Celcius";
            response = translationService.translateLongMessage(responseEn);
        }
        return new ServerResponse(response);
    }

    @MessageMapping("/lat")
    public void getLat(String lat) {
        System.out.println(lat);
        weatherService.setCurrentLat(lat);
        try {
            weatherService.getReverseGeolocation();
        } catch (ReverseGeolocationException e) {
            e.printStackTrace();
        }

    }

    @MessageMapping("/lon")
    @SendTo("/topic/currentLoc")
    public ServerResponse getLon(String lon) {
        System.out.println(lon);
        weatherService.setCurrentLon(lon);
        try {
            weatherService.getReverseGeolocation();
        } catch (ReverseGeolocationException e) {
            e.printStackTrace();
            return createErrorResponse();
        }
        return new ServerResponse(translateMessageIfNeeded("Aktuelle Position: " + weatherService.getCity() + " " + weatherService.getStreet()
                + " " + weatherService.getHousenumber() + " , " + weatherService.getCountry()));
    }

    @MessageMapping("/icon")
    @SendTo("/topic/icon")
    public ServerResponse sendIcon() {
        if (List.of(intentNames).contains(rasaService.getLatestMessage().getIntent().getName())) {
            return new ServerResponse(weatherService.getWeatherIcon());
        }
        return null;
    }

    public ServerResponse createErrorResponse() {
        weatherService.setWeatherIcon(null);
        return new ServerResponse(translateMessageIfNeeded("Huch! Ich scheine dich nicht richtig verstanden zu haben. Versuche es nochmal! Achte auf deine Rechtschreibung!"));
    }

    public void mapToSlots(ResponseIntent response) throws GeolocationException, ReverseGeolocationException {
        if (response.getEntities().length == 2) {
            Geolocation geolocation = weatherService.getGeolocation(response.getEntities()[0].getValue().replaceAll("\\?", ""));
            System.out.println(weatherService.getLon());
            weatherService.setLat(String.valueOf(geolocation.getLatitude()));
            weatherService.setLon(String.valueOf(geolocation.getLongitude()));
            System.out.println(weatherService.getLon());
            weatherService.setCity(response.getEntities()[0].getValue().replaceAll("\\?", ""));
            weatherService.setDay(response.getEntities()[1].getValue());
        }
        if (response.getEntities().length == 1 && Objects.equals(response.getIntent().getName(), "weather")) {
            weatherService.setDay(response.getEntities()[0].getValue());
            weatherService.getReverseGeolocation();
        }

        if (response.getEntities().length == 1 && Objects.equals(response.getIntent().getName(), "other_day")) {
            weatherService.setDay(response.getEntities()[0].getValue());
            weatherService.getReverseGeolocation();
        }

        if (response.getEntities().length == 1 && Objects.equals(response.getIntent().getName(), "other_city")) {
            Geolocation geolocation = weatherService.getGeolocation(response.getEntities()[0].getValue().replaceAll("\\?", ""));
            System.out.println(weatherService.getLon());
            weatherService.setLat(String.valueOf(geolocation.getLatitude()));
            weatherService.setLon(String.valueOf(geolocation.getLongitude()));
            System.out.println(weatherService.getLon());
            weatherService.setCity(response.getEntities()[0].getValue().replaceAll("\\?", ""));
        }
    }

    @MessageMapping("/lang")
    public void changeLanguage(){
        switch(currentLang) {
            case "de":
                currentLang = "en";
                break;
            case "en":
                currentLang = "de";
                break;
        }
    }

    public String translateMessageIfNeeded(String toTranslate){
        switch(currentLang){
            case "de":
                return toTranslate;
            case "en":
                return translationService.translate(toTranslate);
        }
        return toTranslate;
    }
}

