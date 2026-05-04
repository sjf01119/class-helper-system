package com.example.classhelper.filter;

import com.example.classhelper.utils.JwtUtil;
import com.example.classhelper.utils.SecurityUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 认证过滤器
 * 从请求头中解析 JWT Token，验证用户身份并设置到 SecurityContext
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    /**
     * 放行路径（不需要认证）
     */
    private static final String[] ALLOWED_PATHS = {
            "/auth/login",
            "/auth/register",
            "/auth/logout",
            "/swagger-ui",
            "/v3/api-docs",
            "/error"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        String contextPath = request.getContextPath();
        String relativePath = requestPath.startsWith(contextPath) ? 
            requestPath.substring(contextPath.length()) : requestPath;
        
        for (String allowedPath : ALLOWED_PATHS) {
            if (relativePath.equals(allowedPath) || relativePath.startsWith(allowedPath + "/")) {
                filterChain.doFilter(request, response);
                return;
            }
        }
        
        // 获取 Authorization 请求头
        String authHeader = request.getHeader("Authorization");
        
        // 如果没有 Authorization 头或格式不正确
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.warn("请求缺少 Authorization Header 或格式错误: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }
        
        // 提取 Token
        String token = authHeader.substring(7);
        
        // 验证 Token
        if (!jwtUtil.validateToken(token)) {
            log.warn("Token 无效或已过期: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }
        
        // 解析 Token 获取用户信息
        Claims claims = jwtUtil.parseToken(token);
        if (claims == null) {
            log.warn("Token 解析失败: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }
        
        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);
        List<String> roles = SecurityUtil.normalizeRoles(jwtUtil.getRoles(token));
        
        if (userId == null || username == null) {
            log.warn("Token 中缺少用户信息: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }
        
        // 创建 UserPrincipal
        SecurityUtil.UserPrincipal userPrincipal = new SecurityUtil.UserPrincipal(userId, username, roles);
        
        // 将角色转换为 Spring Security 权限
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        
        // 创建认证对象
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userPrincipal, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        
        // 设置到 SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        log.debug("JWT 认证成功 - 用户: {}, 路径: {}", username, requestPath);
        
        filterChain.doFilter(request, response);
    }

}
