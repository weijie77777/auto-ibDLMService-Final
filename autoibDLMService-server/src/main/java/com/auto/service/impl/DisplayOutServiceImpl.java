package com.auto.service.impl;


import com.auto.entity.DisplayOut;
import com.auto.mapper.DisplayOutMapper;
import com.auto.service.DisplayOutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DisplayOutServiceImpl implements DisplayOutService {

    private final DisplayOutMapper displayOutMapper;

    @Autowired
    public DisplayOutServiceImpl(DisplayOutMapper displayOutMapper) {
        this.displayOutMapper = displayOutMapper;
    }

    @Override
    public List<DisplayOut> getAllDisplayOut() {
        return displayOutMapper.selectList(null);
    }
}