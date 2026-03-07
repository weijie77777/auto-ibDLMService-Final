package com.auto.util;



import com.auto.vo.FileVo;
import com.auto.vo.FolderVo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 文件上传相关工具类
 */
public class FileUtils {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
/*
返回某一个文件下的所有文件列表
 */
    public static List<FileVo> files(String parentPath) {

        List<FileVo> res = new ArrayList<>();

        File parentFolder = new File(parentPath);
        if (!parentFolder.isDirectory()) {
            return Collections.emptyList();
        }
        //文件夹下的所有文件
        File[] files = parentFolder.listFiles();
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }
        for (File file : files) {
            if (file.isDirectory()) continue;
            FileVo fileVo = new FileVo();
            fileVo.setFileName(file.getName());
            fileVo.setModifyDate(getModifyDate(file));
            fileVo.setType(getFileType(file));
            res.add(fileVo);
        }
        return res;
    }

    public static List<FolderVo> folders(String parentPath) {
        List<FolderVo> res = new ArrayList<>();

        File parentFolder = new File(parentPath);
        if (!parentFolder.isDirectory()) {
            return Collections.emptyList();
        }
        File[] files = parentFolder.listFiles();
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }
        for (File file : files) {
            if (file.isFile()) continue;
            FolderVo folderVo = new FolderVo();
            folderVo.setFolderName(file.getName());
            folderVo.setModifyDate(getModifyDate(file));
            folderVo.setPath(convert(file.getAbsolutePath()));
            folderVo.setChildren(folderChildren(file.getAbsolutePath()));
            res.add(folderVo);
        }

        return res;
    }

    public static List<FolderVo> folderChildren(String parentPath) {
        List<FolderVo> res = new ArrayList<>();

        File parentFolder = new File(parentPath);
        if (!parentFolder.isDirectory()) {
            return Collections.emptyList();
        }
        File[] files = parentFolder.listFiles();
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }
        for (File file : files) {
            if (file.isFile()) continue;
            FolderVo folderVo = new FolderVo();
            folderVo.setFolderName(file.getName());
            folderVo.setModifyDate(getModifyDate(file));
            folderVo.setPath(convert(file.getAbsolutePath()));
            res.add(folderVo);
        }

        return res;
    }

    public static void main(String[] args) {
        File[] files = File.listRoots();
        System.out.println("===");
    }

    private static String getFileType(File file) {
        if (file == null) return "";
        String fileName = file.getName();
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        return fileType;
    }

    private static String getModifyDate(File file) {
        if (file == null) return "";
        String modifyDate = dateFormat.format(file.lastModified());
        return modifyDate;
    }

    private static String convert(String filePath) {
        String replace = filePath.replace("\\", "/");
        return replace;
    }
}
