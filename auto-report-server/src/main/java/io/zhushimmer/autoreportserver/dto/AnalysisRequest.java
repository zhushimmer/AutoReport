package io.zhushimmer.autoreportserver.dto;

import lombok.Data;

@Data
public class AnalysisRequest {
    private String uid;
    private String prompt;
    private boolean think;
    private boolean search;
}