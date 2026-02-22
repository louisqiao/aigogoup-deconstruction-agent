package com.aigogoup.deconstruction.service;

import com.aigogoup.deconstruction.agent.DeconstructionExpert;
import com.aigogoup.deconstruction.entity.DeconstructedDocument;
import com.aigogoup.deconstruction.repository.DeconstructedDocumentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;

/**
 * =====================================================================
 * 深度解构服务 - aigogoup.com 核心业务逻辑层
 * =====================================================================
 * 这个服务类协调各个组件完成解构工作：
 * 1. 调用AI模型分析文本
 * 2. 解析AI返回的JSON
 * 3. 保存结果到数据库
 * 4. 处理异常情况
 * 
 * 设计原则：
 * - 单一职责：只负责解构相关的业务逻辑
 * - 依赖注入：通过构造函数注入依赖，便于测试
 * - 事务管理：@Transactional保证数据一致性
 * - 日志记录：记录关键步骤，便于追踪问题
 * 
 * @Service      标记这是一个Spring服务Bean
 * @Slf4j        Lombok自动生成log对象
 * @Transactional 声明式事务管理
 * =====================================================================
 */
@Slf4j
@Service
public class DeconstructionService {

    private final OpenAiChatModel chatModel;
    private final DeconstructedDocumentRepository repository;
    private final ObjectMapper objectMapper;

    public DeconstructionService(
            OpenAiChatModel chatModel,
            DeconstructedDocumentRepository repository,
            ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    /**
     * ====================================================================
     * 核心方法：解构文档
     * ====================================================================
     * @param documentName 文档名称（用于展示）
     * @param originalFileName 原始文件名
     * @param content 要分析的文本内容
     * @param tenantId 租户ID（多租户支持）
     * @return 保存后的文档实体
     */
    @Transactional
    public DeconstructedDocument deconstruct(
            String documentName,
            String originalFileName,
            String content,
            String tenantId) {
        
        Instant start = Instant.now();
        log.info("aigogoup 开始解构文档：{}，文本长度：{}字符，租户：{}", 
                 documentName, content.length(), tenantId);

        try {
            // 创建AI服务实例
            DeconstructionExpert expert = AiServices.create(
                DeconstructionExpert.class, 
                chatModel
            );
            
            // 调用AI执行解构
            log.debug("正在调用OpenAI API...");
            String jsonResult = expert.deconstruct(content);
            log.debug("AI返回原始结果长度：{}字符", jsonResult.length());
            
            // 计算AI处理时间
            Duration aiDuration = Duration.between(start, Instant.now());
            log.info("AI分析完成，耗时：{}秒", aiDuration.getSeconds());

            // 验证和增强JSON
            String processedJson = validateAndEnhanceJson(jsonResult, documentName);
            
            // 从JSON中提取元数据
            JsonNode rootNode = objectMapper.readTree(processedJson);
            String keywords = extractKeywords(rootNode);
            int principleCount = rootNode.has("principles") ? 
                                 rootNode.get("principles").size() : 0;

            // 估算Token使用量
            int estimatedTokens = estimateTokens(content) + estimateTokens(jsonResult);

            // 创建实体并保存到数据库
            DeconstructedDocument document = DeconstructedDocument.builder()
                    .documentName(documentName)
                    .originalFileName(originalFileName)
                    .originalContent(content)
                    .structuredReport(processedJson)
                    .coreKeywords(keywords)
                    .principleCount(principleCount)
                    .processingTimeSeconds(aiDuration.getSeconds())
                    .tokenUsage(estimatedTokens)
                    .tenantId(tenantId)
                    .build();
            
            DeconstructedDocument saved = repository.save(document);
            log.info("aigogoup 文档解构完成并保存，ID：{}，原则数量：{}", 
                     saved.getId(), principleCount);
            
            return saved;

        } catch (Exception e) {
            log.error("解构文档时发生错误：{}", documentName, e);
            throw new RuntimeException("文档解构失败：" + e.getMessage(), e);
        }
    }

    /**
     * ====================================================================
     * 验证和增强JSON
     * ====================================================================
     * 这个私有方法用于：
     * 1. 确保AI返回的是有效JSON
     * 2. 添加文档名称等元数据
     * 3. 格式化JSON使其更美观
     */
    private String validateAndEnhanceJson(String rawJson, String documentName) 
            throws Exception {
        
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(rawJson);
        } catch (Exception e) {
            log.error("AI返回的不是有效JSON：{}", 
                     rawJson.substring(0, Math.min(100, rawJson.length())));
            throw new RuntimeException("AI返回结果格式错误，不是有效的JSON");
        }

        if (rootNode instanceof ObjectNode) {
            ObjectNode objectNode = (ObjectNode) rootNode;
            if (!objectNode.has("documentName") || 
                objectNode.get("documentName").asText().isEmpty()) {
                objectNode.put("documentName", documentName);
            }
        }

        return objectMapper.writerWithDefaultPrettyPrinter()
                          .writeValueAsString(rootNode);
    }

    /**
     * ====================================================================
     * 提取关键词（将JSON数组转换为逗号分隔的字符串）
     * ====================================================================
     * 数据库存储关键词时，用逗号分隔的字符串比JSON数组更容易查询
     */
    private String extractKeywords(JsonNode rootNode) {
        if (rootNode.has("coreKeywords") && rootNode.get("coreKeywords").isArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode keyword : rootNode.get("coreKeywords")) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(keyword.asText());
            }
            return sb.toString();
        }
        return "";
    }

    /**
     * ====================================================================
     * 估算Token数量（简化版）
     * ====================================================================
     * OpenAI的Token计算规则复杂，这里用简单规则估算：
     * 中英文混合时，平均2个字符算1个token
     */
    private int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return (int) Math.ceil(text.length() / 2.0);
    }
}