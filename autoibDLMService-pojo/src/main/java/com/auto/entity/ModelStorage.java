package com.auto.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;



@Data
@TableName("modelstorage")
public class ModelStorage {
    @TableId(type = IdType.AUTO)
    private int id;
    @TableField(value = "username")
    private String username;
    @TableField(value = "model_path")
    private String modelPath;

}
