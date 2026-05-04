package com.example.classhelper.aspect;

import com.example.classhelper.annotation.RequiresRole;
import com.example.classhelper.exception.BusinessException;
import com.example.classhelper.utils.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色权限切面
 * 拦截带有 @RequiresRole 注解的方法，进行角色权限校验
 */
@Slf4j
@Aspect
@Component
public class RoleAspect {

    /**
     * 拦截 @RequiresRole 注解的方法
     *
     * @param requiresRole 角色注解
     */
    @Before("@annotation(requiresRole)")
    public void checkRole(RequiresRole requiresRole) {
        List<String> requiredRoles = List.of(requiresRole.value()).stream()
                .map(SecurityUtil::normalizeRole)
                .filter(role -> !role.isEmpty())
                .collect(Collectors.toList());
        
        // 获取当前用户角色
        List<String> userRoles = SecurityUtil.getCurrentUserRoles();
        
        if (userRoles.isEmpty()) {
            log.warn("用户未登录或没有角色，无法访问该接口");
            throw new BusinessException(403, "无权限访问该资源");
        }
        
        // 管理员拥有最高权限，可以直接访问所有带 @RequiresRole 的接口
        if (userRoles.contains("admin")) {
            log.debug("管理员权限校验通过");
            return;
        }
        
        // 检查用户是否具有所需角色之一
        boolean hasPermission = false;
        for (String requiredRole : requiredRoles) {
            if (userRoles.contains(requiredRole)) {
                hasPermission = true;
                break;
            }
        }
        
        if (!hasPermission) {
            log.warn("用户角色 {} 不符合要求 {}，拒绝访问", userRoles, requiredRoles);
            throw new BusinessException(403, "无权限访问该资源");
        }
        
        log.debug("用户角色校验通过：{} 匹配 {}", userRoles, requiredRoles);
    }

}
