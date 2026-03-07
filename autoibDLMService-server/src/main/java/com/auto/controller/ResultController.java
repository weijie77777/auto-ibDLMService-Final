package com.auto.controller;

import com.auto.entity.UserImage;
import com.auto.service.PredictResultService;
import com.auto.vo.PredictionResultVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/result")
public class ResultController {
    @Autowired
    private PredictResultService predictResultService;
    @GetMapping
    public Page<PredictionResultVo> getUserImagesByUsername(
            @RequestParam(defaultValue = "0") long page,
            @RequestParam(defaultValue = "10") long size) {
        return predictResultService.getByUsername(page, size);
    }
    @DeleteMapping
    public Page<PredictionResultVo> deleteUserImageByPath(
            @RequestParam String imagePath,
            @RequestParam(defaultValue = "0") long page,
            @RequestParam(defaultValue = "10") long size) {
        return predictResultService.deleteByImagePath(imagePath, page, size);
    }
}
