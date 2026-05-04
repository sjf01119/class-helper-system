package com.example.classhelper.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.ResponseHeaderOverrides;
import com.example.classhelper.config.OssConfig;
import com.example.classhelper.exception.BusinessException;
import com.example.classhelper.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OssServiceImpl implements OssService {

    private static final String DEFAULT_DIRECTORY = "materials";

    private final OSS ossClient;
    private final OssConfig ossConfig;

    @Override
    public String uploadFile(MultipartFile file, String directory) {
        try {
            // 获取文件原始名称和扩展名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                originalFilename = "unknown";
            }
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf(".");
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex);
            }

            String normalizedDirectory = normalizeDirectory(directory);
            // 生成唯一文件名，防止同名文件覆盖
            String objectName = normalizedDirectory + "/" + UUID.randomUUID().toString().replace("-", "") + extension;

            // OSS 配置不可用时，自动回退到本地文件存储，避免开发阶段无法上传
            if (!isOssConfigured()) {
                // 未配置 OSS，回退到本地存储
                String localUrl = saveToLocalStorage(file.getInputStream(), objectName);
                log.warn("OSS 未正确配置，已回退到本地存储: {}", localUrl);
                return localUrl;
            }

            // 获取文件输入流
            InputStream inputStream = file.getInputStream();

            // 上传文件到 OSS
            ossClient.putObject(ossConfig.getBucketName(), objectName, inputStream);

            // 拼接文件访问路径
            // https://[bucketName].[endpoint]/[objectName]
            String url = "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint() + "/" + objectName;
            log.info("文件上传成功，访问路径: {}", url);
            return url;
        } catch (Exception e) {
            log.error("文件上传失败: ", e);
            throw new BusinessException(500, "文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            if (isLocalUploadUrl(fileUrl)) {
                deleteLocalFile(fileUrl);
                return;
            }

            if (fileUrl == null || !fileUrl.contains(ossConfig.getEndpoint())) {
                log.warn("非 OSS 文件地址，忽略删除: {}", fileUrl);
                return;
            }

            // 解析 ObjectName
            // 从 https://[bucketName].[endpoint]/[objectName] 中提取 objectName
            URL url = new URL(fileUrl);
            String path = url.getPath();
            // 去掉路径开头的 '/'
            String objectName = path.startsWith("/") ? path.substring(1) : path;

            ossClient.deleteObject(ossConfig.getBucketName(), objectName);
            log.info("文件删除成功: {}", objectName);
        } catch (Exception e) {
            log.error("文件删除失败: ", e);
            throw new BusinessException(500, "文件删除失败");
        }
    }

    @Override
    public String getSignedUrl(String fileUrl, boolean isDownload, String originalFilename) {
        if (!StringUtils.hasText(fileUrl)) {
            throw new BusinessException(400, "文件地址不能为空");
        }

        // 如果是本地上传的文件，直接返回其地址
        if (isLocalUploadUrl(fileUrl)) {
            return fileUrl;
        }

        if (!isOssConfigured()) {
            return fileUrl;
        }

        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();
            String objectName = path.startsWith("/") ? path.substring(1) : path;

            // 设置过期时间，如 1小时后
            Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);

            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(ossConfig.getBucketName(), objectName, HttpMethod.GET);
            request.setExpiration(expiration);

            // 如果是下载，需要设置 ResponseHeaderOverrides 的 Content-Disposition
            if (isDownload && StringUtils.hasText(originalFilename)) {
                ResponseHeaderOverrides overrides = new ResponseHeaderOverrides();
                String encodedFilename = URLEncoder.encode(originalFilename, "UTF-8").replaceAll("\\+", "%20");
                overrides.setContentDisposition("attachment; filename=\"" + encodedFilename + "\"; filename*=utf-8''" + encodedFilename);
                request.setResponseHeaders(overrides);
            }

            URL signedUrl = ossClient.generatePresignedUrl(request);
            return signedUrl.toString();
        } catch (Exception e) {
            log.error("生成签名链接失败: ", e);
            throw new BusinessException(500, "获取文件地址失败: " + e.getMessage());
        }
    }

    private boolean isOssConfigured() {
        return ossConfig.isConfigured()
                && isRealConfigValue(ossConfig.getEndpoint())
                && isRealConfigValue(ossConfig.getAccessKeyId())
                && isRealConfigValue(ossConfig.getAccessKeySecret())
                && isRealConfigValue(ossConfig.getBucketName());
    }

    private boolean isRealConfigValue(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        String normalized = value.trim().toLowerCase();
        return !normalized.contains("your-")
                && !normalized.contains("your_")
                && !normalized.contains("xxx")
                && !normalized.contains("placeholder");
    }

    private String normalizeDirectory(String directory) {
        if (!StringUtils.hasText(directory)) {
            return DEFAULT_DIRECTORY;
        }
        String normalized = directory.trim().toLowerCase();
        return switch (normalized) {
            case "avatars", "materials", "assignments", "course-covers" -> normalized;
            default -> DEFAULT_DIRECTORY;
        };
    }

    private String saveToLocalStorage(InputStream inputStream, String objectName) throws IOException {
        Path uploadRoot = Paths.get(System.getProperty("user.dir"), "uploads");
        Path targetPath = uploadRoot.resolve(objectName).normalize();
        Files.createDirectories(targetPath.getParent());
        Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        return "/api/uploads/" + objectName;
    }

    private boolean isLocalUploadUrl(String fileUrl) {
        return StringUtils.hasText(fileUrl) && (fileUrl.startsWith("/api/uploads/") || fileUrl.contains("/api/uploads/"));
    }

    private void deleteLocalFile(String fileUrl) {
        String marker = "/api/uploads/";
        int index = fileUrl.indexOf(marker);
        if (index < 0) {
            return;
        }
        String relativePath = fileUrl.substring(index + marker.length());
        Path targetPath = Paths.get(System.getProperty("user.dir"), "uploads", relativePath).normalize();
        try {
            Files.deleteIfExists(targetPath);
            log.info("本地文件删除完成: {}", targetPath);
        } catch (IOException e) {
            log.error("删除本地文件失败: {}", targetPath, e);
            throw new BusinessException(500, "文件删除失败: " + e.getMessage());
        }
    }
}
