package com.auto.entity;

import lombok.Data;

/**
 * PythonResponse 用于映射python服务端的数据
 */
@Data
public class PythonResponse {
    private Integer code;
    private String message;
    private PredictionData data;

    @Data
    public static class PredictionData {
        private Integer eventNum;
        private Integer eventPredictNum;
        private Double loss;
        private Double recall;
        private Double precision;
        private Double accuracy;
        private String imageUrl;
    }
}