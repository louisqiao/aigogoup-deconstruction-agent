package com.aigogoup.deconstruction.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * =====================================================================
 * AI深度解构专家接口 - aigogoup.com
 * =====================================================================
 * 这是整个智能体的核心，通过LangChain4j的注解定义AI的行为模式。
 * 
 * 核心设计思想：
 * 1. 声明式编程：用注解定义AI行为，就像定义普通Java接口一样
 * 2. 结构化输出：强制AI返回JSON，保证结果可以被程序处理
 * 3. 角色扮演：给AI设定专家角色，提高输出质量
 * 4. 链式思维：SystemMessage + UserMessage 组合成完整指令
 * 
 * 工作流程：
 * 1. Java代码调用deconstruct(text)方法
 * 2. LangChain4j自动将参数注入到{{text}}位置
 * 3. 将SystemMessage和UserMessage组合发送给OpenAI
 * 4. 获取AI响应并作为方法返回值
 * =====================================================================
 */
public interface DeconstructionExpert {

    /**
     * 系统消息：定义AI的角色、任务和输出格式
     * 
     * SystemMessage相当于给AI的"岗位说明书"：
     * - 角色设定：让AI进入专家模式
     * - 任务描述：明确要做什么
     * - 输出格式：强制JSON，保证结果可解析
     * - 质量标准："宁多勿少"原则
     * 
     * @return 系统提示词
     */
    @SystemMessage("""
        你是一位顶尖的知识解构专家，服务于 aigogoup.com 平台。
        你的任务是将用户提供的任何学习资料（如文章、课程讲义、书籍章节）进行深度、地毯式的分析。
        你需要挖掘出其中所有核心原则、配套案例和深层解释。
        
        请严格遵守以下输出格式（JSON格式），确保输出的结构化和可复用性：
        {
          "documentName": "资料名称",
          "coreKeywords": ["关键词1", "关键词2"],  // 提取5-10个最核心的关键词
          "summary": "300-500字的通俗总结", // 用大白话解释核心思想
          "principles": [  // 原则列表，数量根据原文长度从3到50+不等
            {
              "id": 1,
              "principle": "第一条核心原则的标题或核心观点",
              "cases": [  // 配套案例，可以有一个或多个
                {
                  "title": "案例标题",
                  "description": "案例详细描述",
                  "source": "案例来源（如果原文提及）"
                }
              ],
              "explanation": "对这条原则的深层解释，包括为什么重要、如何应用、与其它原则的关联等。不少于200字。"
            }
          ]
        }
        
        你的分析必须遵循"宁多勿少"的原则，力求穷尽所有要点。输出的JSON必须有效且完整。
        """ 
    )
    
    /**
     * 用户消息：定义用户如何输入待分析的资料
     * 
     * @param text 用户上传的原始文本（通过@V注解注入）
     * @return AI返回的结构化JSON字符串
     * 
     * @UserMessage 中的 {{text}} 会被@V("text")参数替换
     * 这种设计让我们可以动态传入用户内容
     */
    @UserMessage("""
        请分析以下学习资料：
        
        --- 资料开始 ---
        {{text}}
        --- 资料结束 ---
        
        请开始你的深度解构分析。
        """)
    String deconstruct(@V("text") String text);
}