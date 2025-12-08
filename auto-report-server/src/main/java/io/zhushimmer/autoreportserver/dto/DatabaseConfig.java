package io.zhushimmer.autoreportserver.dto;

import lombok.Data;

@Data
public class DatabaseConfig {
    private String type;
    private String host;
    private Integer port;
    private String database;
    private String username;
    private String password;
}