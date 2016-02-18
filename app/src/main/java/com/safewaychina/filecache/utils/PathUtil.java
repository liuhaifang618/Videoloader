package com.safewaychina.filecache.utils;

import com.safewaychina.filecache.naming.FileNameGenerator;

import java.io.File;

/**
 * @author liu_haifang
 * @version 1.0
 * @Title：SAFEYE@
 * @Description：
 * @date 2015-09-24
 */
public class PathUtil {

    public static String createPath(String url,File file, FileNameGenerator generator) {
        String md5 = generator.generate(url); //生成随机数
        String grandpaFolder = md5.substring(0, 2);
        String fatherFolder = md5.substring(md5.length() - 2, md5.length());
        String totalPath = file.getAbsolutePath() + File.separator + grandpaFolder + File.separator + fatherFolder;
        File folderCreater = new File(totalPath);//创建多级目录，仿微信
        folderCreater.mkdirs();
        return folderCreater.getAbsolutePath() + File.separator + md5;
    }
}
