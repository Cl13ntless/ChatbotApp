package com.chatbot.websocket;

import com.chatbot.websocket.responseMapper.Response;
import com.chatbot.websocket.responseMapperWeather.ResponseWeather;
import com.chatbot.websocket.responseMapperWeather.Weather;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

@Controller
public class ServerResponseController {

    @MessageMapping("/inquiry")
    @SendTo("/topic/weather")
    public ServerResponse serverResponse(ClientPrompt prompt) throws Exception{
        Thread.sleep(1000); // simulated delay
        System.out.println(HtmlUtils.htmlEscape(prompt.getText()));
        executePost(HtmlUtils.htmlEscape(prompt.getText()));
        return new ServerResponse("Hello, " + HtmlUtils.htmlEscape(prompt.getText() + "!"));
    }

    public void executePost(String urlParameters) throws Exception{
        var values = new HashMap<String, String>() {{
            put("text", urlParameters);
        }};

        var objectMapper = new ObjectMapper();
        String requestBody = objectMapper
                .writeValueAsString(values);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:5005/model/parse"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());

        Response mappedResponse = mapResponse(response.body());

        System.out.println(mappedResponse.getIntent().getName());
        System.out.println(weatherApiCall("2023-01-18T15:00"));

    }

    public Response mapResponse(String responseJson) throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        Response response = mapper.readValue(responseJson, Response.class);
        return response;
    }

    public int weatherApiCall(String date) throws  Exception{
        HttpClient client = HttpClient.newHttpClient();
        String lat = "48.13923210048965";
        String lon = "11.581793217175251";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.brightsky.dev/weather?lat="+ lat +"&lon="+ lon +"&date="+ date +"&last_date="+ date))
                .GET()
                .build();

        System.out.println(request);
        HttpResponse<String> response = client
                .send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        ResponseWeather responseWeather = mapper.readValue(response.body(), ResponseWeather.class);

        return responseWeather.getWeather().getTemperature();
    }
}
