package com.auto.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;



@Data
@TableName("display_out1")
public class DisplayOut1 {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField(value = "title")
    private String title;
    @TableField(value = "image_url")
    private String imageUrl;
    @TableField(value = "comdescription")
    private String comdescription;
}
