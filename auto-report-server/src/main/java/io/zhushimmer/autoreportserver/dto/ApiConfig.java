package io.zhushimmer.autoreportserver.dto;

import lombok.Data;

@Data
public class ApiConfig {
    private String url;
    private String key;
    private String model_name;
}