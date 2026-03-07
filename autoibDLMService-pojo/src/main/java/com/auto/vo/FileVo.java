package com.auto.vo;

import lombok.Data;

/**
 创建File实体类 与前端一一对应
 */
@Data
public class FileVo {
    private String fileName;
    private String type;
    private String modifyDate;
}
