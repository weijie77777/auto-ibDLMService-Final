package com.auto.dto;

import lombok.Data;

/**
 * 提取网络特征的请求参数封装
 */
@Data
public class AnalysisRequest {
    private String taskId;
    private String windowSize; // "30m", "1h", "6h", "1d" 划分窗口的大
}
