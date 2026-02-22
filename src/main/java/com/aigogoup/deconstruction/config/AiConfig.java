package com.aigogoup.deconstruction.config;

import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;

/**
 * =====================================================================
 * AI模型配置类 - aigogoup.com
 * =====================================================================
 * 配置OpenAI聊天模型，根据环境注入不同的参数。
 * 
 * 设计原则：
 * - 外部化配置：所有参数从application.yml读取
 * - 环境隔离：不同环境使用不同配置
 * - 单一职责：只负责AI模型的创建
 * =====================================================================
 */
@Configuration
public class AiConfig {

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name}")
    private String modelName;

    @Value("${langchain4j.open-ai.chat-model.temperature}")
    private Double temperature;

    @Value("${langchain4j.open-ai.chat-model.max-tokens}")
    private Integer maxTokens;

    @Value("${langchain4j.open-ai.chat-model.timeout}")
    private String timeout;

    /**
     * 创建OpenAI聊天模型Bean
     */
    @Bean
    public OpenAiChatModel openAiChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(parseTimeout(timeout))
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    /**
     * 测试环境使用mock模型（避免真实调用）
     */
    @Bean
    @Profile("test")
    public OpenAiChatModel testOpenAiChatModel() {
        return OpenAiChatModel.builder()
                .apiKey("dummy-key")
                .modelName("gpt-4o-mini")
                .temperature(0.0)
                .maxTokens(100)
                .build();
    }

    /**
     * 解析超时字符串（如"120s" -> Duration.ofSeconds(120)）
     */
    private Duration parseTimeout(String timeout) {
        if (timeout.endsWith("s")) {
            int seconds = Integer.parseInt(timeout.substring(0, timeout.length() - 1));
            return Duration.ofSeconds(seconds);
        }
        return Duration.ofSeconds(60);
    }
}