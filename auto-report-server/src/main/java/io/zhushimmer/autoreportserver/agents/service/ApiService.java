package io.zhushimmer.autoreportserver.agents.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

public class ApiService {
    private final WebClient webClient;

    public ApiService() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8000")
                .build();
    }

    public String sendJsonAndGetResponse(String uri, Map<String, String> data) {

        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonPayload = mapper.writeValueAsString(data);
            return webClient
                .post()
                .uri(uri)
                .header("Content-Type", "application/json")
                .bodyValue(jsonPayload)
                .retrieve()
                .bodyToMono(String.class) // 接收响应为 String
                .block(); // 阻塞等待结果
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
