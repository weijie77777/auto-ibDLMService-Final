package com.auto.service.impl;


import com.auto.entity.Developer;
import com.auto.mapper.DeveloperMapper;
import com.auto.service.DeveloperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeveloperServiceImpl implements DeveloperService {

    @Autowired
    private DeveloperMapper developerMapper;

    @Override
    public List<Developer> getAllDevelopers() {
        return developerMapper.selectList(null);
    }
}
