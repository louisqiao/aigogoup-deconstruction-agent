package com.aigogoup.deconstruction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * =====================================================================
 * 应用启动类 - aigogoup.com
 * =====================================================================
 * Spring Boot 应用的入口点。
 * 
 * @SpringBootApplication 是一个组合注解，包含：
 * - @Configuration：标记为配置类
 * - @EnableAutoConfiguration：启用自动配置
 * - @ComponentScan：自动扫描当前包及其子包的组件
 * =====================================================================
 */
@SpringBootApplication
public class DeconstructionApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeconstructionApplication.class, args);
        
        System.out.println("\n" +
            "===================================================\n" +
            "🚀 aigogoup.com AI深度解构智能体 启动成功！\n" +
            "===================================================\n" +
            "访问地址：http://localhost:8080\n" +
            "健康检查：http://localhost:8080/actuator/health\n" +
            "===================================================\n");
    }
}