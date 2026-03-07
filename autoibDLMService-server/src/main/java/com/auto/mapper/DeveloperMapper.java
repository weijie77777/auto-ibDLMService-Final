package com.auto.mapper;

import com.auto.entity.Developer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

//数据端的映射交互
@Mapper
public interface DeveloperMapper extends BaseMapper<Developer> {
}
