package com.aigogoup.deconstruction.controller;

import com.aigogoup.deconstruction.entity.DeconstructedDocument;
import com.aigogoup.deconstruction.service.DeconstructionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * =====================================================================
 * 深度解构控制器 - aigogoup.com REST API端点
 * =====================================================================
 * 这个控制器处理HTTP请求：
 * 1. 文件上传
 * 2. 调用解构服务
 * 3. 返回结果
 * 
 * 设计原则：
 * - 关注点分离：只处理HTTP相关逻辑，业务逻辑委托给Service
 * - RESTful设计：使用标准的HTTP方法和状态码
 * - 统一响应格式：使用ResponseEntity包装响应
 * =====================================================================
 */
@Slf4j
@RestController
@RequestMapping("/api/agent/deconstruction")
public class DeconstructionController {

    private final DeconstructionService deconstructionService;

    public DeconstructionController(DeconstructionService deconstructionService) {
        this.deconstructionService = deconstructionService;
    }

    /**
     * 健康检查端点
     * 用于监控系统是否存活，Docker健康检查会调用这个接口
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("aigogoup AI Agent is running");
    }

    /**
     * 上传并解构文档
     * 端点：POST /api/agent/deconstruction/upload
     * 消费类型：multipart/form-data（文件上传）
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAndDeconstruct(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "documentName", required = false) String customName) {
        
        log.info("aigogoup 收到文件上传请求：文件名={}，大小={}字节", 
                 file.getOriginalFilename(), file.getSize());

        try {
            // 1. 验证文件是否为空
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("文件不能为空"));
            }

            // 2. 验证文件类型
            String fileName = file.getOriginalFilename();
            if (fileName != null && !fileName.endsWith(".txt") && 
                !fileName.endsWith(".md")) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("只支持TXT和MD格式文件"));
            }

            // 3. 读取文件内容（UTF-8编码）
            String content;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                content = reader.lines()
                               .collect(Collectors.joining("\n"));
            }

            // 4. 确定文档名称
            String documentName = customName != null ? customName : fileName;

            // 5. 获取租户ID（多租户支持）
            String tenantId = extractTenantIdFromRequest();

            // 6. 调用解构服务
            DeconstructedDocument result = deconstructionService.deconstruct(
                documentName, 
                fileName, 
                content, 
                tenantId
            );

            // 7. 构建响应
            SuccessResponse response = new SuccessResponse(
                result.getId(),
                result.getDocumentName(),
                result.getStructuredReport(),
                result.getPrincipleCount()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("处理上传文件时发生错误", e);
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("处理失败：" + e.getMessage()));
        }
    }

    /**
     * 获取文档历史列表（预留接口）
     */
    @GetMapping("/history")
    public ResponseEntity<?> getHistory() {
        return ResponseEntity.ok("待实现 - aigogoup.com 历史记录功能");
    }

    /**
     * 获取单个文档详情（预留接口）
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDocument(@PathVariable String id) {
        return ResponseEntity.ok("待实现 - aigogoup.com 文档详情功能");
    }

    /**
     * 从请求中提取租户ID
     * MVP阶段返回默认租户
     */
    private String extractTenantIdFromRequest() {
        return "aigogoup-default-tenant";
    }

    /**
     * 成功响应DTO
     */
    public record SuccessResponse(
        String documentId,
        String documentName,
        String report,
        Integer principleCount
    ) {}

    /**
     * 错误响应DTO
     */
    public record ErrorResponse(
        String message
    ) {}
}