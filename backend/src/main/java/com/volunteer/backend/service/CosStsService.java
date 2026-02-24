package com.volunteer.backend.service;

import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.tencent.cloud.CosStsClient;
import com.tencent.cloud.Response;

import com.volunteer.backend.config.CosProperties;

@Service
public class CosStsService {
    private final CosProperties cosProperties;

    public CosStsService(CosProperties cosProperties) {
        this.cosProperties = cosProperties;
    }

    public Response getStsCredential() throws RuntimeException {
        TreeMap<String, Object> config = new TreeMap<>();
        try {
            config.put("secretId", cosProperties.getSecretId());
            config.put("secretKey", cosProperties.getSecretKey());
            config.put("region", cosProperties.getRegion());
            config.put("bucket", cosProperties.getBucketName());
            config.put("durationSeconds", cosProperties.getDurationSeconds());

            // 只允许上传到images目录
            config.put("allowPrefixes", new String[] { "images/*" });

            // 策略配置（限制上传权限）
            // @formatter:off
            config.put("allowActions", new String[] {
                "name/cos:GetObject",
                "name/cos:HeadObject",
                "name/cos:PutObject",
                "name/cos:PostObject",
                "name/cos:InitiateMultipartUpload",
                "name/cos:ListMultipartUploads",
                "name/cos:ListParts",
                "name/cos:UploadPart",
                "name/cos:CompleteMultipartUpload"
            });
            // @formatter:on

            return CosStsClient.getCredential(config);
        } catch (Exception e) {
            throw new RuntimeException("获取临时密钥失败: " + e.getMessage(), e);
        }
    }
}
