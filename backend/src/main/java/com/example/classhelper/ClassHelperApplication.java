package com.example.classhelper;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 学习辅助系统启动类
 * 启用 MyBatis-Plus Mapper 扫描和 AOP 切面
 */
@SpringBootApplication
@MapperScan("com.example.classhelper.mapper")
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class ClassHelperApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClassHelperApplication.class, args);
        System.out.println("========================================");
        System.out.println("=   学习辅助系统后端启动成功！          =");
        System.out.println("=   访问地址: http://localhost:8080/api =");
        System.out.println("=   登录接口: POST /api/auth/login      =");
        System.out.println("========================================");
    }

}
