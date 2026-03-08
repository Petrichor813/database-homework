package com.volunteer.backend.service;

import java.net.URI;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.DeleteObjectRequest;
import com.qcloud.cos.region.Region;
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
                "name/cos:CompleteMultipartUpload",
                "name/cos:DeleteObject"
            });
            // @formatter:on

            return CosStsClient.getCredential(config);
        } catch (Exception e) {
            throw new RuntimeException("获取临时密钥失败: " + e.getMessage(), e);
        }
    }

    public void deleteObject(String fileUrl) throws RuntimeException {
        try {
            URI uri = new URI(fileUrl);
            String path = uri.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            COSCredentials cred = new BasicCOSCredentials(cosProperties.getSecretId(), cosProperties.getSecretKey());
            ClientConfig clientConfig = new ClientConfig(new Region(cosProperties.getRegion()));
            COSClient cosClient = new COSClient(cred, clientConfig);

            try {
                DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(cosProperties.getBucketName(), path);
                cosClient.deleteObject(deleteObjectRequest);
            } finally {
                cosClient.shutdown();
            }
        } catch (Exception e) {
            throw new RuntimeException("删除文件失败: " + e.getMessage(), e);
        }
    }
}
