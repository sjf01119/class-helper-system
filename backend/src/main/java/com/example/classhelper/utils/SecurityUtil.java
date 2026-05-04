package com.example.classhelper.utils;

import com.example.classhelper.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Security 工具类
 * 用于获取当前登录用户信息
 */
@Slf4j
@Component
public class SecurityUtil {

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 获取当前登录用户名
     *
     * @return 用户名
     */
    public static String getCurrentUsername() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 获取当前登录用户角色列表
     *
     * @return 角色列表
     */
    public static List<String> getCurrentUserRoles() {
        UserPrincipal user = getCurrentUser();
        return user != null ? normalizeRoles(user.getRoles()) : List.of();
    }

    /**
     * 判断当前用户是否登录
     *
     * @return 是否登录
     */
    public static boolean isAuthenticated() {
        return getCurrentUser() != null;
    }

    /**
     * 判断当前用户是否有指定角色
     *
     * @param role 角色
     * @return 是否有该角色
     */
    public static boolean hasRole(String role) {
        return getCurrentUserRoles().contains(normalizeRole(role));
    }

    public static List<String> normalizeRoles(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        List<String> normalized = new ArrayList<>();
        for (String role : roles) {
            String value = normalizeRole(role);
            if (!value.isEmpty() && !normalized.contains(value)) {
                normalized.add(value);
            }
        }
        return normalized;
    }

    public static String normalizeRole(String role) {
        if (role == null) {
            return "";
        }
        String normalized = role.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("role_")) {
            normalized = normalized.substring(5);
        }
        return normalized;
    }

    /**
     * 用户主体信息
     */
    public static class UserPrincipal {
        private Long userId;
        private String username;
        private List<String> roles;

        public UserPrincipal(Long userId, String username, List<String> roles) {
            this.userId = userId;
            this.username = username;
            this.roles = roles;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }

}
