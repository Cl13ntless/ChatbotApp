package com.chatbot.websocket;

import com.chatbot.service.RasaService;
import com.chatbot.service.WeatherService;
import com.chatbot.websocket.responseMapperIntent.Response;
import com.chatbot.websocket.responseMapperWeather.ResponseWeather;
import com.chatbot.websocket.responseMapperWeather.Weather;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

@Controller
public class ServerResponseController {
    RasaService rasaService = new RasaService();
    WeatherService weatherservice = new WeatherService();

    @MessageMapping("/inquiry")
    @SendTo("/topic/weather")
    public ServerResponse serverResponse(ClientPrompt prompt) throws Exception{
        Thread.sleep(1000); // simulated delayyy
        System.out.println(HtmlUtils.htmlEscape(prompt.getText()));
        rasaService.executePost(HtmlUtils.htmlEscape(prompt.getText()));
        return new ServerResponse("The Temperature for tommorow is going to be " + weatherservice.weatherApiCall("2023-01-20T15:00") + " Degrees Celcius.");
    }
}
