package com.auto.entity;

import lombok.Data;

@Data
public class MetricResult {
   private final String direction;
   private final String feature;
   private final Double mean;
   private final Double std;

    public MetricResult(String direction, String feature, Double mean, Double std) {
        this.direction = direction;
        this.feature = feature;
        this.mean = mean;
        this.std = std;
    }
}
