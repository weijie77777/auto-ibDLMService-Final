package com.auto.controller;


import com.auto.entity.DisplayOut;
import com.auto.service.DisplayOutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/displayout")
public class DisplayOutController {
    // 构造器注入
    private final DisplayOutService displayOutService;
    @Autowired
    public DisplayOutController(DisplayOutService displayOutService) {
        this.displayOutService = displayOutService;
    }
    @GetMapping
    public List<DisplayOut> getAllDisplayOut() {
        log.info("查询首页的相关布局信息");
        return displayOutService.getAllDisplayOut();
    }
}

