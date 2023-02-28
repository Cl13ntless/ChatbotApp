package com.chatbot.websocket;

import com.chatbot.exception.GeolocationException;
import com.chatbot.exception.ReverseGeolocationException;
import com.chatbot.exception.WeatherAPIException;
import com.chatbot.geolocation.Geolocation;
import com.chatbot.service.RasaService;
import com.chatbot.service.TranslationService;
import com.chatbot.service.WeatherService;
import com.chatbot.websocket.responseMapperIntent.Intent;
import com.chatbot.websocket.responseMapperIntent.ResponseIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;


//Geschrieben von: Alexandra Gleber, Fabian Hirmer und Percy Covic
@Controller
public class ServerResponseController {
    private static final Logger logger = LoggerFactory.getLogger(ServerResponseController.class);
    private static final String WEATHER_INTENT = "weather";
    private static final String CITY_WEATHER_INTENT = "city_weather";
    private static final String OTHER_DAY_INTENT = "other_day";
    private static final String OTHER_CITY_INTENT = "other_city";
    private static final double CONFIDENCE_THRESHOLD = 0.9;
    private static final String ERROR_MESSAGE = "Entschuldigung ich konnte dich nicht wirklich verstehen.Versuche es nochmal und Achte auf deine Rechtschreibung!";
    private static final String RESPONSE_TEMPLATE = "Die aktuell vorausgesagte Temperatur für %s,%s am %s um %s Uhr beträgt %s Grad Celsius";
    private static final String RESPONSE_TEMPLATE_EN = "Die aktuell vorausgesagte Temperatur für %s,%s am %s um %s Uhr - beträgt %s Grad Celsius";
    private static final String LOCATION_TEMPLATE = "Aktuelle Position: %s %s %s , %s ";
    private static final TranslationService translationService = new TranslationService();
    private static final RasaService rasaService = new RasaService();
    private static final WeatherService weatherService = new WeatherService();
    private static final String[] intentNames = {WEATHER_INTENT, CITY_WEATHER_INTENT, OTHER_DAY_INTENT, OTHER_CITY_INTENT};

    String city;
    String countryCode;
    String requestedDay;
    String hour;
    String temperature;
    String currentLang = "de";
    Intent lastIntent;


//    Geschrieben von: Alexandra Gleber, Fabian Hirmer und Percy Covic.
//    Die jeweiligen Mapper Klassen wurden von den jeweiligen verantowrtlichen verfasst, also:
//    Weatherservice Mapper Klassen: Alexandra Gleber + Percy Covic
//    RasaService Mapper Klassen: Fabian Hirmer + Percy Covic
//    Exceptions: Percy Covic
//    Dockerization + Pipeline Erstellung: Percy Covic
    @MessageMapping("/inquiry")
    @SendToUser("/topic/weather")
    public ServerResponse serverResponse(ClientPrompt prompt) {
        ResponseIntent mappedResponse = rasaService.getInitialParameters(HtmlUtils.htmlEscape(prompt.getText()));
        try {
            mapToSlots(mappedResponse);
        } catch (GeolocationException | ReverseGeolocationException | IndexOutOfBoundsException e) {
            return createErrorResponse();
        }

        city = StringUtils.capitalize(weatherService.getCity()
                .replace("&uuml;", "ü")
                .replace("&Auml;", "ä")
                .replace("&ouml;", "ö"));
        try {
            countryCode = weatherService.getCountryCode().toUpperCase();
            requestedDay = weatherService.getDay();
            hour = weatherService.getHour();

            switch (mappedResponse.getIntent().getName()) {
                case WEATHER_INTENT ->
                        temperature = String.valueOf(weatherService.cityWeatherApiCall(true).getTemperature());
                case CITY_WEATHER_INTENT ->
                        temperature = String.valueOf(weatherService.cityWeatherApiCall(false).getTemperature());
                case OTHER_DAY_INTENT, OTHER_CITY_INTENT -> {
                    if (Objects.equals(lastIntent.getName(), WEATHER_INTENT)) {
                        temperature = String.valueOf(weatherService.cityWeatherApiCall(true).getTemperature());
                        break;
                    } else if (Objects.equals(lastIntent.getName(), CITY_WEATHER_INTENT)) {
                        temperature = String.valueOf(weatherService.cityWeatherApiCall(false).getTemperature());
                        break;
                    }
                    return createErrorResponse();
                }
                default -> {
                    if (mappedResponse.getEntities().length != 0 || mappedResponse.getIntent().getConfidence() < CONFIDENCE_THRESHOLD) {
                        return createErrorResponse();
                    }
                    //Request zurück an Rasa für die standard Chatbot Antwort und den Intent der Nachricht für die nächste Nachricht setzen
                    lastIntent = mappedResponse.getIntent();
                    return new ServerResponse(translateMessageIfNeeded(rasaService.getChatResponse(HtmlUtils.htmlEscape(prompt.getText()))));
                }
            }
        } catch (DateTimeParseException | WeatherAPIException | NullPointerException | IndexOutOfBoundsException e) {
            e.printStackTrace();
            return createErrorResponse();
        }

        //Intent für die nächste Nachricht setzen
        lastIntent = mappedResponse.getIntent();
        String response = String.format(RESPONSE_TEMPLATE, city, countryCode, requestedDay, hour, temperature);

        /*Es muss geprüft werden, ob die Sprache aktuell auf Englisch gestellt,
        * wenn das der Fall ist, wird mithilfe des TranslationService übersetzt*/
        if (Objects.equals(currentLang, "gb")) {
            String responseEn = String.format(RESPONSE_TEMPLATE_EN, city, countryCode, requestedDay, hour, temperature);
            response = translationService.translateLongMessage(responseEn);
        }
        return new ServerResponse(response);
    }
//    Geschrieben von: Sinem Bozkurt und Percy Covic
//    Lat Endpoint → Weather Service Slot
    @MessageMapping("/lat")
    public void getLat(String lat) {
        logger.info("Lat received from FE: {}", lat);
        weatherService.setCurrentLat(lat);
        try {
            weatherService.getReverseGeolocation();
        } catch (ReverseGeolocationException e) {
            e.printStackTrace();
        }

    }
//    Geschrieben von: Sinem Bozkurt und Percy Covic
//    Lon Endpoint -> Weather Service Slot
    @MessageMapping("/lon")
    @SendToUser("/topic/currentLoc")
    public ServerResponse getLon(String lon) {
        logger.info("Lon received from FE: {}", lon);
        weatherService.setCurrentLon(lon);
        try {
            weatherService.getReverseGeolocation();
        } catch (ReverseGeolocationException e) {
            e.printStackTrace();
        }
        String currentLocation = String.format(LOCATION_TEMPLATE, weatherService.getCity(), weatherService.getStreet(), weatherService.getHouseNumber(), weatherService.getCountry());
        return new ServerResponse(translateMessageIfNeeded(currentLocation));
    }

//    Geschrieben von: Sinem Bozkurt und Percy Covic.
//    Das Frontend fragt hier bei jeder gestellten Nachricht an, ob es ein Wettericon gibt
    @MessageMapping("/icon")
    @SendToUser("/topic/icon")
    public ServerResponse sendIcon() {
        if (List.of(intentNames).contains(rasaService.getLatestMessage().getIntent().getName())) {
            return new ServerResponse(weatherService.getWeatherIcon());
        }
        return null;
    }

//    Geschrieben von: Alexandra Gleber und Percy Covic.
//    app/topic language change endpoint
    @MessageMapping("/lang")
    public void changeLanguage(String language) {
        currentLang = language;
        logger.info("Language after Language change: {}", currentLang);
    }

//    Geschrieben von: Fabian Hirmer und Percy Covic und Alexandra Gleber.
//    Herstellen einer generischen Error Message - Prinzipiell auch genau Error Message möglich
    public ServerResponse createErrorResponse() {
        weatherService.setWeatherIcon(null);
        return new ServerResponse(translateMessageIfNeeded(ERROR_MESSAGE));
    }

//    Geschrieben von: Alexandra Gleber und Percy Covic.
//    Unsere Antwort von Rasa wird auf die im Weather Service definierten Slots geschrieben
    public void mapToSlots(ResponseIntent response) throws GeolocationException, ReverseGeolocationException {
        if (Objects.equals(response.getIntent().getName(), CITY_WEATHER_INTENT)) {
            Geolocation geolocation = weatherService.getGeolocation(response.getEntities()[0].getValue().replace("?", ""));
            logger.info("Mapped lon before: {}", weatherService.getLon());
            weatherService.setLat(String.valueOf(geolocation.getLatitude()));
            weatherService.setLon(String.valueOf(geolocation.getLongitude()));
            logger.info("Mapped lat after: {}", weatherService.getLon());
            weatherService.setCity(response.getEntities()[0].getValue().replace("?", ""));
            weatherService.setDay(response.getEntities()[1].getValue());
        }
        if (Objects.equals(response.getIntent().getName(), WEATHER_INTENT)) {
            weatherService.setDay(response.getEntities()[0].getValue());
            weatherService.getReverseGeolocation();
        }

        if (Objects.equals(response.getIntent().getName(), OTHER_DAY_INTENT)) {
            weatherService.setDay(response.getEntities()[0].getValue());
        }

        if (Objects.equals(response.getIntent().getName(), OTHER_CITY_INTENT)) {
            Geolocation geolocation = weatherService.getGeolocation(response.getEntities()[0].getValue().replace("?", ""));
            logger.info("Mapped lon before: {}", weatherService.getLon());
            weatherService.setLat(String.valueOf(geolocation.getLatitude()));
            weatherService.setLon(String.valueOf(geolocation.getLongitude()));
            logger.info("Mapped lon after: {}", weatherService.getLon());
            weatherService.setCity(response.getEntities()[0].getValue().replace("?", ""));
        }
    }

//    Geschrieben von: Alexandra Gleber und Percy Covic
    public String translateMessageIfNeeded(String toTranslate) {
        //Mehrere Sprachen mit Switch case möglich, aber prinzipiell ersetzbar durch if Statement.
        return switch (currentLang) {
            case "gb" -> translationService.translate(toTranslate);
            default -> toTranslate;
        };
    }
}

