package com.auto.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("PredictResults")
public class PredictResult {
    @TableId(type = IdType.AUTO) // 使用数据库自带的id自增策略 mp的雪花算法会加上时间戳 导致id过大 容易溢出
    private Long id;
    private String username;
    private Integer threshold;
    private Double accuracy;
    private Integer eventNum;
    private Integer eventPredictNum;
    private String imageUrl;
    private Double loss;
    // 这里取为precision1 因为在SQL中precision是保留字
    private Double precision1;
    private Double recall;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createTime;
}
