package io.zhushimmer.autoreportserver.dto;

import lombok.Data;

@Data
public class ConfigRequest {
    private ApiConfig api;
    private DatabaseConfig database;
}