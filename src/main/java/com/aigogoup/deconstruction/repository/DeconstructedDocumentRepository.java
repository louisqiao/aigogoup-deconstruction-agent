package com.aigogoup.deconstruction.repository;

import com.aigogoup.deconstruction.entity.DeconstructedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * =====================================================================
 * 解构文档仓库接口 - aigogoup.com
 * =====================================================================
 * 继承JpaRepository自动获得CRUD方法：
 * - save() 保存/更新
 * - findById() 按ID查询
 * - findAll() 查询所有
 * - deleteById() 按ID删除
 * - count() 统计总数
 * 
 * 我们只需要定义自定义查询方法，Spring Data JPA会自动实现
 * =====================================================================
 */
@Repository
public interface DeconstructedDocumentRepository 
        extends JpaRepository<DeconstructedDocument, String> {

    /**
     * 按租户ID查询所有文档，按创建时间倒序排列
     * 
     * 方法名解析规则：
     * - findBy：开始查询
     * - TenantId：按tenantId字段查询
     * - OrderByCreatedAtDesc：按createdAt字段降序排序
     * 
     * @param tenantId 租户ID
     * @return 该租户的所有文档（最新的在前）
     */
    List<DeconstructedDocument> findByTenantIdOrderByCreatedAtDesc(String tenantId);

    /**
     * 按关键词搜索（使用SQL的LIKE模糊匹配）
     * 
     * 这是简单实现，适合MVP阶段。
     * 生产环境建议：
     * - 使用全文检索（PostgreSQL的tsvector）
     * - 或使用Elasticsearch
     * 
     * @param tenantId 租户ID（数据隔离）
     * @param keyword 搜索关键词
     * @return 匹配的文档列表
     */
    @Query("SELECT d FROM DeconstructedDocument d " +
           "WHERE d.tenantId = :tenantId " +
           "AND d.coreKeywords LIKE %:keyword%")
    List<DeconstructedDocument> searchByKeyword(
            @Param("tenantId") String tenantId,
            @Param("keyword") String keyword);

    /**
     * 统计租户在指定时间范围内的处理量（用于计费）
     * 
     * @param tenantId 租户ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 处理文档总数
     */
    @Query("SELECT COUNT(d) FROM DeconstructedDocument d " +
           "WHERE d.tenantId = :tenantId " +
           "AND d.createdAt BETWEEN :startDate AND :endDate")
    long countByTenantAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 统计租户的token总使用量（用于计费）
     * 
     * @param tenantId 租户ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return token总量
     */
    @Query("SELECT COALESCE(SUM(d.tokenUsage), 0) FROM DeconstructedDocument d " +
           "WHERE d.tenantId = :tenantId " +
           "AND d.createdAt BETWEEN :startDate AND :endDate")
    long sumTokenUsageByTenantAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 删除租户的所有文档（用于数据清理）
     * 
     * @param tenantId 租户ID
     */
    void deleteByTenantId(String tenantId);
}