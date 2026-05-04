package com.example.classhelper.annotation;

import java.lang.annotation.*;

/**
 * 角色权限注解
 * 用于标注需要特定角色才能访问的方法
 *
 * 使用示例：
 * @RequiresRole("admin")    // 仅管理员可访问
 * @RequiresRole("teacher")  // 仅教师可访问
 * @RequiresRole("student")  // 仅学生可访问
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresRole {

    /**
     * 需要的角色列表
     * 可选值：admin、teacher、student
     */
    String[] value();

}
