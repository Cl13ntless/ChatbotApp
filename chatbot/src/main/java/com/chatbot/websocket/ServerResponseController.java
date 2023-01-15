package com.chatbot.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

@Controller
public class ServerResponseController {

    @MessageMapping("/inquiry")
    @SendTo("/topics/weather")
    public ServerResponse serverResponse(ClientPrompt prompt) throws Exception{
        Thread.sleep(1000); // simulated delay
        executePost(HtmlUtils.htmlEscape(prompt.getPrompt()));
        return new ServerResponse("Hello, " + HtmlUtils.htmlEscape(prompt.getPrompt() + "!"));
    }

    public static void executePost(String urlParameters) throws Exception{
        var values = new HashMap<String, String>() {{
            put("text", urlParameters);
        }};

        var objectMapper = new ObjectMapper();
        String requestBody = objectMapper
                .writeValueAsString(values);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:80/model/parse"))
                .POST(HttpRequest.BodyPublishers.ofString(urlParameters))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());
    }
}
