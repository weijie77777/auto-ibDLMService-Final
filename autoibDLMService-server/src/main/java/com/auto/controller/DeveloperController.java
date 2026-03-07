package com.auto.controller;


import com.auto.entity.Developer;
import com.auto.service.DeveloperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/developers")
public class DeveloperController {
    @Autowired
    private DeveloperService developerService;
    /**
     * 查看网页开发者信息
     * @return List<Developer>
     */
    @GetMapping
    public List<Developer> getAllDevelopers() {
        log.info("查看网页开发者信息");
        return developerService.getAllDevelopers();
    }
}

