package com.volunteer.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tencent.cloud.Response;
import com.volunteer.backend.config.CosProperties;
import com.volunteer.backend.service.CosStsService;

@RestController
@RequestMapping("/api/sts")
public class CosStsController {
    private final CosProperties cosProperties;
    private final CosStsService cosStsService;

    public CosStsController(CosProperties cosProperties, CosStsService cosStsService) {
        this.cosProperties = cosProperties;
        this.cosStsService = cosStsService;
    }

    @GetMapping("/credential")
    public ResponseEntity<?> getStsCredential() {
        try {
            Response r = cosStsService.getStsCredential();

            HashMap<String, Object> resp = new HashMap<>();
            resp.put("credentials", r.credentials);
            resp.put("startTime", r.startTime);
            resp.put("expiredTime", r.expiredTime);
            resp.put("region", cosProperties.getRegion());
            resp.put("bucket", cosProperties.getBucketName());

            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "获取临时密钥失败", "detail", e.getMessage()));
        }
    }
}
