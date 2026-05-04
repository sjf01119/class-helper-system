package com.example.classhelper.service;

import org.springframework.web.multipart.MultipartFile;

public interface OssService {

    /**
     * 上传文件到阿里云 OSS
     *
     * @param file 待上传的文件
     * @return 文件的 OSS 访问地址
     */
    default String uploadFile(MultipartFile file) {
        return uploadFile(file, "materials");
    }

    /**
     * 上传文件到指定业务目录
     *
     * @param file 待上传文件
     * @param directory 业务目录，如 avatars/materials/assignments
     * @return 文件访问地址
     */
    String uploadFile(MultipartFile file, String directory);

    /**
     * 删除阿里云 OSS 上的文件
     *
     * @param fileUrl 文件的 OSS 访问地址
     */
    void deleteFile(String fileUrl);

    /**
     * 获取文件的临时访问地址（带签名）
     *
     * @param fileUrl 文件的原始访问地址
     * @param isDownload 是否为下载请求
     * @param originalFilename 原始文件名，用于下载时设置响应头
     * @return 带有签名的临时访问地址
     */
    String getSignedUrl(String fileUrl, boolean isDownload, String originalFilename);
}
