package com.auto.dto;

import lombok.Data;

import java.util.List;

@Data
/**
 * 用于接收特征分析传来的两个特征权重
 */
public class WeightResult {
    private List<Double> weightsMean;
    private List<Double> weightsSum;
    private List<String> featureNames;
}
