package com.auto.dto;

import lombok.Data;

import java.util.Map;

@Data
public class TrainResultDTO {
    private String feature_selector_url;
    private String model_url;
    public static TrainResultDTO fromMap(Map<String, Object> data) {
        TrainResultDTO dto = new TrainResultDTO();


        dto.feature_selector_url = (String) data.get("feature_selector_url");

        dto.model_url = (String) data.get("model_url");

        return dto;
    }
}
