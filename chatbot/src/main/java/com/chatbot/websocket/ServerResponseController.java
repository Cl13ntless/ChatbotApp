package com.chatbot.websocket;

import com.chatbot.websocket.responseMapper.Response;
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

        Response mappedResponse = mapResponse(response.body());

        System.out.println(mappedResponse.getIntent().getName());
    }

    public Response mapResponse(String responseJson) throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        Response response = mapper.readValue(responseJson, Response.class);
        return response;
    }
}
