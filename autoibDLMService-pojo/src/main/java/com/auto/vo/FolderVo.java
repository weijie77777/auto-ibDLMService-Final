package com.auto.vo;

import lombok.Data;

import java.util.List;


@Data
public class FolderVo {
    private String folderName;
    private String modifyDate;
    private String path;
    // 子文件夹
    private List<FolderVo> children;
}
