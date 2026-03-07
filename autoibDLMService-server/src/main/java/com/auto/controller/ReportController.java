package com.auto.controller;

import com.auto.dto.ExportResultDTO;
import com.auto.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@Slf4j
public class ReportController {
    @Autowired
    private ReportService reportService;
    /**
     * 导出单行预测数据
     * @param exportResultDTO
     * @param response
     */
    @PostMapping("/export")
    public void export(@RequestBody ExportResultDTO exportResultDTO, HttpServletResponse response){
        log.info("开始导出数据：{}", exportResultDTO);
        reportService.exportPredictData(exportResultDTO, response);
    }
    @GetMapping("/exportAll")
    public void exportAll(@RequestParam("username") String username, HttpServletResponse response){
        log.info("开始导出所有数据");
        reportService.exportAllPredictData(username, response);
    }
}
