package com.example.classhelper.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssConfig {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    @Bean
    public OSS ossClient() {
        String resolvedEndpoint = StringUtils.hasText(endpoint) ? endpoint : "oss-cn-hangzhou.aliyuncs.com";
        String resolvedAccessKeyId = StringUtils.hasText(accessKeyId) ? accessKeyId : "local-placeholder-access-key";
        String resolvedAccessKeySecret = StringUtils.hasText(accessKeySecret) ? accessKeySecret : "local-placeholder-access-secret";
        return new OSSClientBuilder().build(resolvedEndpoint, resolvedAccessKeyId, resolvedAccessKeySecret);
    }

    public boolean isConfigured() {
        return StringUtils.hasText(endpoint)
                && StringUtils.hasText(accessKeyId)
                && StringUtils.hasText(accessKeySecret)
                && StringUtils.hasText(bucketName);
    }
}
