package com.auto.service;

import com.auto.dto.ExportResultDTO;

import javax.servlet.http.HttpServletResponse;

public interface ReportService {
    void exportPredictData(ExportResultDTO exportResultDTO, HttpServletResponse response);
    void exportAllPredictData(String username, HttpServletResponse response);
}
