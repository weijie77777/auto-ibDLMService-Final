package com.auto.service.impl;


import com.auto.entity.DisplayOut1;
import com.auto.mapper.DisplayOut1Mapper;
import com.auto.service.DisplayOut1Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DisplayOutService1Impl implements DisplayOut1Service {

    @Autowired
    private DisplayOut1Mapper displayOutMapper;

    @Override
    public List<DisplayOut1> searchDisplayOut(String title) {
        // 创建条件构造器
        QueryWrapper<DisplayOut1> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("title", title);
        return displayOutMapper.selectList(queryWrapper);
    }
}