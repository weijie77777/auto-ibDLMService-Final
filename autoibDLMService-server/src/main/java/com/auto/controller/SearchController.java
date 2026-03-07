package com.auto.controller;


import com.auto.entity.DisplayOut1;
import com.auto.service.DisplayOut1Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class SearchController {

    @Autowired
    private DisplayOut1Service displayOutService;

    /**
     * 模糊查询 获得具体的布局信息 这里纯粹是因为不知道这部分业务到底应该做什么 业务功能太少了
     * @param title
     * @return List<DisplayOut1>
     */
    @PostMapping("/searchByTitle")
    public List<DisplayOut1> searchDisplayOut(@RequestBody String title) {
        // 去掉title字符串中的双引号
        title = title.replace("\"", "");
        log.info("要查找的title是: {}", title);
        return displayOutService.searchDisplayOut(title);
    }
}
