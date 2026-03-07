package com.auto.service.impl;

import com.auto.dto.ExportResultDTO;
import com.auto.entity.PredictResult;
import com.auto.service.PredictResultService;
import com.auto.service.ReportService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private PredictResultService predictResultService;
    @Override
    public void exportPredictData(ExportResultDTO exportResultDTO, HttpServletResponse response) {
        // 1.查询数据库，获取预测数据
        QueryWrapper<PredictResult> wrapper = new QueryWrapper<>();
        wrapper.eq("username", exportResultDTO.getUsername());
        wrapper.eq("create_time", exportResultDTO.getCreateTime());
        PredictResult result = predictResultService.getOne(wrapper);
        // 2.通过POI将数据写入到Excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/预测数据报表模板.xlsx");//在类路径下读取资源返回输入流对象
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=report.xlsx");
        try {
            //基于模板文件创建一个新的Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);
            //获取表格文件的Sheet文件
            XSSFSheet sheet = excel.getSheet("Sheet1");
            //填充数据--时间
            String dateTime = exportResultDTO.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
            sheet.getRow(1).getCell(1).setCellValue("Time：" + dateTime);
            //填充明细数据
            XSSFRow row = sheet.getRow(4);
            if(row == null){
                row = sheet.createRow(4);
            }
            row.getCell(1).setCellValue(result.getUsername());
            row.getCell(2).setCellValue(dateTime);
            row.getCell(3).setCellValue(result.getThreshold());
            row.getCell(4).setCellValue(result.getEventNum());
            row.getCell(5).setCellValue(result.getEventPredictNum());
            row.getCell(6).setCellValue(result.getLoss());
            row.getCell(7).setCellValue(result.getRecall());
            row.getCell(8).setCellValue(result.getPrecision1());
            row.getCell(9).setCellValue(result.getAccuracy());
            row.getCell(10).setCellValue(result.getImageUrl());
            //3.通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            //关闭资源
            out.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void exportAllPredictData(String username, HttpServletResponse response) {
        // 1.查询数据库，获取预测数据
        QueryWrapper<PredictResult> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        wrapper.orderByDesc("create_time");
        List<PredictResult> results = predictResultService.list(wrapper);
        int size = results.size();
        // 2.通过POI将数据写入到Excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/预测数据报表模板.xlsx");//在类路径下读取资源返回输入流对象
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=reportAll.xlsx");

        try {
            //基于模板文件创建一个新的Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);
            //获取表格文件的Sheet文件
            XSSFSheet sheet = excel.getSheet("Sheet1");
            //填充数据--时间
            String begin = results.get(size-1).getCreateTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
            String end = results.get(0).getCreateTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
            sheet.getRow(1).getCell(1).setCellValue("Time：" + begin + " To：" + end);

            // 获取模板第4行的样式，用于复制给新行
            XSSFRow templateRow = sheet.getRow(4);
            CellStyle[] styles = new CellStyle[11]; // 存储第1-10列的样式
            if (templateRow != null) {
                for (int j = 1; j <= 10; j++) {
                    XSSFCell cell = templateRow.getCell(j);
                    if (cell != null) {
                        styles[j] = cell.getCellStyle();
                    }
                }
            }

            //填充明细数据
            for(int i = 0; i<size; i++){
                XSSFRow row = sheet.getRow(4 + i);
                if(row == null){
                    row = sheet.createRow(4 + i);
                }
                PredictResult result = results.get(i);
                String dateTime = result.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
               // 设置单元格（带样式复制）
                setCellValue(row, 1, result.getUsername(), styles[1]);
                setCellValue(row, 2, dateTime, styles[2]);
                setCellValue(row, 3, result.getThreshold(), styles[3]);
                setCellValue(row, 4, result.getEventNum(), styles[4]);
                setCellValue(row, 5, result.getEventPredictNum(), styles[5]);
                setCellValue(row, 6, result.getLoss(), styles[6]);
                setCellValue(row, 7, result.getRecall(), styles[7]);
                setCellValue(row, 8, result.getPrecision1(), styles[8]);
                setCellValue(row, 9, result.getAccuracy(), styles[9]);
                setCellValue(row, 10, result.getImageUrl(), styles[10]);
            }

            // 3. 通过输出流将Excel文件下载到客户端
            // HttpServletResponse的getOutputStream方法可以获取一个输出流对象 用来将数据写入到客户端
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            // 关闭资源
            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 辅助方法：设置单元格值并应用样式
    private void setCellValue(XSSFRow row, int col, Object value, CellStyle style) {
        XSSFCell cell = row.getCell(col);
        if (cell == null) {
            cell = row.createCell(col);
        }
        if (style != null) {
            cell.setCellStyle(style);
        }

        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(value != null ? value.toString() : "");
        }
    }
}
