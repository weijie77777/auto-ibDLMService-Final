package com.auto.dto;

import lombok.Data;

import java.util.Map;

@Data
public class PredictionResultDTO {
    private Integer threshold;
    private Double accuracy;
    private Integer eventNum;
    private Integer eventPredictNum;
    private String imageUrl;
    private Double loss;
    private Double precision1;
    private Double recall;
    // 从 Map 构造
    public static PredictionResultDTO fromMap(Map<String, Object> data) {
        PredictionResultDTO dto = new PredictionResultDTO();

        // 用 Number 接收，再转目标类型
        dto.accuracy = ((Number) data.get("accuracy")).doubleValue();
        dto.eventNum = ((Number) data.get("event_num")).intValue();
        dto.eventPredictNum = ((Number) data.get("event_predict_num")).intValue();
        dto.imageUrl = (String) data.get("image_url");
        dto.loss = ((Number) data.get("loss")).doubleValue();

        // 注意：-1 可能是 Integer
        Object prec = data.get("precision");
        dto.precision1 = prec instanceof Number ? ((Number) prec).doubleValue() : null;

        Object rec = data.get("recall");
        dto.recall = rec instanceof Number ? ((Number) rec).doubleValue() : null;

        return dto;
    }
}
