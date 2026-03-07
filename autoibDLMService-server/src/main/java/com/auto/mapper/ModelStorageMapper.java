package com.auto.mapper;

import com.auto.entity.ModelStorage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

//数据端的映射交互
@Mapper
public interface ModelStorageMapper extends BaseMapper<ModelStorage> {
    void updateModelPathByUsername(@Param("username") String username, @Param("newModelPath") String newModelPath);
}
