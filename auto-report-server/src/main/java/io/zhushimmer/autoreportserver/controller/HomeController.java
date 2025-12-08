package io.zhushimmer.autoreportserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.agent.Event;
import io.agentscope.core.agent.EventType;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.zhushimmer.autoreportserver.agents.AutoReport;
import io.zhushimmer.autoreportserver.agents.ConfigManager;
import io.zhushimmer.autoreportserver.dto.*;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
@Tag(name = "Api Endpoint", description = "This is `AutoReport` api endpoint")
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "This is `AutoReport` api endpoint";
    }

    @GetMapping("/config")
    public ConfigRequest getConfig() {
        ConfigManager configManager = new ConfigManager();
        ConfigRequest configRequest = new ConfigRequest();

        ApiConfig apiConfig = new ApiConfig();
        apiConfig.setUrl(configManager.getProperty("api.url"));
        apiConfig.setKey(configManager.getProperty("api.key"));
        apiConfig.setModel_name(configManager.getProperty("api.model_name"));
        configRequest.setApi(apiConfig);

        DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.setType(configManager.getProperty("database.type"));
        databaseConfig.setHost(configManager.getProperty("database.host"));
        databaseConfig.setPort(Integer.parseInt(configManager.getProperty("database.port")));
        databaseConfig.setDatabase(configManager.getProperty("database.database"));
        databaseConfig.setUsername(configManager.getProperty("database.username"));
        databaseConfig.setPassword(configManager.getProperty("database.password"));
        configRequest.setDatabase(databaseConfig);
        return configRequest;
    }

    @PostMapping("/config")
    public String updateConfig(@RequestBody ConfigRequest request) {
        ConfigManager configManager = new ConfigManager();

        // OpenAI 模型和兼容 API 配置
        configManager.setProperty("api.url", request.getApi().getUrl());
        configManager.setProperty("api.key", request.getApi().getKey());
        configManager.setProperty("api.model_name", request.getApi().getModel_name());

        // 数据库配置
        configManager.setProperty("database.type", request.getDatabase().getType());
        configManager.setProperty("database.host", request.getDatabase().getHost());
        configManager.setProperty("database.port", String.valueOf(request.getDatabase().getPort()));
        configManager.setProperty("database.database", request.getDatabase().getDatabase());
        configManager.setProperty("database.username", request.getDatabase().getUsername());
        configManager.setProperty("database.password", request.getDatabase().getPassword());

        // 保存到文件
        configManager.saveConfig();
        return "success";
    }

    @PostMapping(value = "/sse/normal")
    public Flux<ServerSentEvent<String>> sse(@RequestBody AnalysisRequest request) {
        AutoReport autoReport = new AutoReport();
        return autoReport.runTask(request.getPrompt())
                .filter(event -> event.getType() == EventType.REASONING && !event.isLast())
                .map(event -> {
                    // 将agentscope的Event转换为json字符串
                    String eventData = convertEventToString(event);
                        return ServerSentEvent.<String>builder()
                                .event("message")
                                .data(eventData)
                                .build();
                })
                .onErrorResume(throwable -> {
                    // 错误处理
                    String errorData = "Error: " + throwable.getMessage();
                    return Flux.just(ServerSentEvent.<String>builder()
                            .data(errorData)
                            .event("error")
                            .build());
                });
    }

    private String convertEventToString(Event event) {
        ObjectMapper objectMapper = new ObjectMapper();
        SseResult sseResult = new SseResult();
        sseResult.setType("text");
        sseResult.setMsg(event.getMessage().getTextContent());
        try {
            return objectMapper.writeValueAsString(sseResult);
        } catch (Exception e) {}
        return "";
    }
}
