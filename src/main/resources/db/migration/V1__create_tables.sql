-- =====================================================================
-- 数据库初始化脚本 - aigogoup.com
-- 版本: V1
-- 描述: 创建deconstructed_docs表和向量支持
-- 使用Flyway命名规范: V{版本}__{描述}.sql
-- =====================================================================

-- 创建向量扩展（如果不存在）
CREATE EXTENSION IF NOT EXISTS vector;

-- =====================================================================
-- 创建主表: deconstructed_docs
-- 存储所有解构文档的核心数据
-- =====================================================================
CREATE TABLE IF NOT EXISTS deconstructed_docs (
    -- 主键: 使用UUID格式
    id VARCHAR(36) PRIMARY KEY,
    
    -- 文档基本信息
    document_name VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255),
    
    -- 内容字段
    original_content TEXT,
    structured_report TEXT,
    
    -- 元数据字段
    core_keywords TEXT,
    principle_count INTEGER,
    
    -- 性能监控
    processing_time_seconds BIGINT,
    token_usage INTEGER,
    
    -- 多租户支持
    tenant_id VARCHAR(50),
    
    -- 时间戳
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- =====================================================================
-- 创建索引提升查询性能
-- =====================================================================

-- 按租户查询的索引
CREATE INDEX idx_deconstructed_docs_tenant 
    ON deconstructed_docs(tenant_id);

-- 按时间倒序的索引
CREATE INDEX idx_deconstructed_docs_created 
    ON deconstructed_docs(created_at DESC);

-- 关键词搜索的GIN索引（提升全文检索性能）
CREATE INDEX idx_deconstructed_docs_keywords 
    ON deconstructed_docs USING GIN (to_tsvector('simple', core_keywords));

-- =====================================================================
-- 创建向量存储表（为RAG做准备）
-- =====================================================================
CREATE TABLE IF NOT EXISTS document_embeddings (
    id VARCHAR(36) PRIMARY KEY,
    
    -- 关联到主表
    document_id VARCHAR(36) REFERENCES deconstructed_docs(id) ON DELETE CASCADE,
    
    -- 内容片段
    content TEXT,
    
    -- 向量（1536维，OpenAI embedding维度）
    embedding vector(1536),
    
    -- 元数据
    chunk_index INTEGER,           -- 片段序号
    tenant_id VARCHAR(50),
    created_at TIMESTAMP
);

-- 向量索引（使用IVFFlat算法，适合高维向量）
CREATE INDEX idx_document_embeddings_vector 
    ON document_embeddings USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

-- 按租户查询的索引
CREATE INDEX idx_document_embeddings_tenant 
    ON document_embeddings(tenant_id);