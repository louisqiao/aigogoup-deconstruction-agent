package com.aigogoup.deconstruction.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * =====================================================================
 * 解构文档实体类 - aigogoup.com
 * =====================================================================
 * 对应数据库中的 deconstructed_docs 表，存储每次解构操作的结果。
 * 使用JPA注解实现对象-关系映射（ORM）。
 * 
 * @Entity        标记这是一个JPA实体类
 * @Table         指定对应的数据库表名
 * @Data          Lombok注解，自动生成getter/setter/toString/equals/hashCode
 * @Builder       提供建造者模式，方便创建对象
 * =====================================================================
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "deconstructed_docs")
public class DeconstructedDocument {

    /**
     * 主键ID - 使用UUID策略
     * 为什么不用自增ID？
     * - UUID在分布式环境下不会冲突
     * - 避免URL中暴露数据量（如 /api/docs/1 表示这是第1个文档）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * 文档名称（用户自定义或从文件名提取）
     * nullable = false 表示数据库字段不能为null
     */
    @Column(nullable = false)
    private String documentName;

    /**
     * 原始文件名（上传时的文件名）
     */
    private String originalFileName;

    /**
     * 原始内容 - 用户上传的文本
     * columnDefinition = "TEXT" 指定数据库字段类型为长文本
     */
    @Column(columnDefinition = "TEXT")
    private String originalContent;

    /**
     * 结构化报告 - AI生成的JSON结果
     * 这是最核心的产出物，包含原则、案例、解释等
     */
    @Column(columnDefinition = "TEXT")
    private String structuredReport;

    /**
     * 核心关键词 - 用逗号分隔，方便数据库查询
     * 例如："第一性原理,系统思维,复利效应"
     */
    private String coreKeywords;

    /**
     * 提炼出的原则数量 - 用于统计和分析
     */
    private Integer principleCount;

    /**
     * 处理耗时（秒） - 用于性能监控
     */
    private Long processingTimeSeconds;

    /**
     * 使用的Token数量 - 用于计费
     * 商业化必备，按token计费
     */
    private Integer tokenUsage;

    /**
     * 租户ID - 支持多租户商业化
     * 未来不同客户的数据隔离
     */
    private String tenantId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * JPA生命周期回调：在插入前自动设置时间
     * @PrePersist 注解的方法会在实体持久化之前自动调用
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * JPA生命周期回调：在更新前自动更新时间
     * @PreUpdate 注解的方法会在实体更新之前自动调用
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}