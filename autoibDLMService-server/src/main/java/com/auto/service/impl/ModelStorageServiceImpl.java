package com.auto.service.impl;

import com.auto.entity.ModelStorage;
import com.auto.mapper.ModelStorageMapper;
import com.auto.service.ModelStorageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

@Service
public class ModelStorageServiceImpl implements ModelStorageService {

    private final ModelStorageMapper modelStorageMapper;

    public ModelStorageServiceImpl(ModelStorageMapper modelStorageMapper) {
        this.modelStorageMapper = modelStorageMapper;
    }

    @Override
    public void addModelStorage(ModelStorage modelStorage) {
        modelStorageMapper.insert(modelStorage);
    }

    @Override
    public ModelStorage findByUsername(String username) {
        return modelStorageMapper.selectOne(new QueryWrapper<ModelStorage>().eq("username", username));
    }
    @Override
    public void updateModelPathByUsername(String username, String newModelPath) {
        modelStorageMapper.update(null,
                Wrappers.<ModelStorage>lambdaUpdate()
                        .eq(ModelStorage::getUsername, username)
                        .set(ModelStorage::getModelPath, newModelPath));
    }
}
