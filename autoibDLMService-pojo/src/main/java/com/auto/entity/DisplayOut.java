package com.auto.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
@Data
@TableName("display_out")
public class DisplayOut {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField(value = "title")
    private String title;
    @TableField(value = "image_url")
    private String imageUrl;
    @TableField(value = "description")
    private String description;
}
