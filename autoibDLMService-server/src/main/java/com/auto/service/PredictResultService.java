package com.auto.service;

import com.auto.dto.PredictionResultDTO;
import com.auto.entity.PredictResult;
import com.auto.entity.UserImage;
import com.auto.vo.PredictionResultVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface PredictResultService extends IService<PredictResult> {
    void saveResult(PredictionResultDTO predictionResultDTO);
    Page<PredictionResultVo> getByUsername(long current, long size);
    // 根据图片路径删除记录，并返回删除后的分页结果
    Page<PredictionResultVo> deleteByImagePath(String imagePath, long current, long size);
}
