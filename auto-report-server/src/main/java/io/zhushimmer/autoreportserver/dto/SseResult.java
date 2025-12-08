package io.zhushimmer.autoreportserver.dto;

import lombok.Data;

@Data
public class SseResult {
    private String type;
    private String msg;
}
