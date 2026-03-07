package com.auto.service;


import com.auto.entity.ModelStorage;

public interface ModelStorageService {
    void addModelStorage(ModelStorage modelStorage);
    ModelStorage findByUsername(String username);
    void updateModelPathByUsername(String username, String newModelPath);
}
