package com.example.classhelper.controller;

import com.example.classhelper.annotation.RequiresRole;
import com.example.classhelper.common.R;
import com.example.classhelper.utils.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RBAC 权限控制演示 Controller
 * 展示如何使用 @RequiresRole 注解控制接口访问权限
 */
@Slf4j
@RestController
@RequestMapping("/demo")
public class DemoController {

    /**
     * 公开接口 - 任何人可访问（不需要登录）
     * 已在 SecurityConfig 中放行
     */
    @GetMapping("/public")
    public R<String> publicApi() {
        return R.ok("这是公开接口，任何人可访问");
    }

    /**
     * 管理员专用接口
     * 使用 @RequiresRole("admin") 注解限制仅管理员可访问
     */
    @GetMapping("/admin-only")
    @RequiresRole("admin")
    public R<String> adminOnly() {
        log.info("管理员 {} 访问了 admin-only 接口", SecurityUtil.getCurrentUsername());
        return R.ok("这是管理员专用接口，只有管理员角色可访问");
    }

    /**
     * 教师专用接口
     * 使用 @RequiresRole("teacher") 注解限制仅教师可访问
     */
    @GetMapping("/teacher-only")
    @RequiresRole("teacher")
    public R<String> teacherOnly() {
        log.info("教师 {} 访问了 teacher-only 接口", SecurityUtil.getCurrentUsername());
        return R.ok("这是教师专用接口，只有教师角色可访问");
    }

    /**
     * 学生专用接口
     * 使用 @RequiresRole("student") 注解限制仅学生可访问
     */
    @GetMapping("/student-only")
    @RequiresRole("student")
    public R<String> studentOnly() {
        log.info("学生 {} 访问了 student-only 接口", SecurityUtil.getCurrentUsername());
        return R.ok("这是学生专用接口，只有学生角色可访问");
    }

    /**
     * 获取当前登录用户信息
     * 需要登录（已在 SecurityConfig 中配置需要认证）
     */
    @GetMapping("/me")
    public R<Map<String, Object>> getCurrentUserInfo() {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", SecurityUtil.getCurrentUserId());
        userInfo.put("username", SecurityUtil.getCurrentUsername());
        userInfo.put("roles", SecurityUtil.getCurrentUserRoles());
        userInfo.put("isAuthenticated", SecurityUtil.isAuthenticated());
        
        // 检查是否拥有特定角色
        userInfo.put("isAdmin", SecurityUtil.hasRole("admin"));
        userInfo.put("isTeacher", SecurityUtil.hasRole("teacher"));
        userInfo.put("isStudent", SecurityUtil.hasRole("student"));
        
        return R.ok(userInfo);
    }

}
