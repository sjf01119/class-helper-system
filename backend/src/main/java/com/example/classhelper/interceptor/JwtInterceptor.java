package com.example.classhelper.interceptor;

import com.example.classhelper.common.R;
import com.example.classhelper.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取请求头中的 Authorization
        String authHeader = request.getHeader("Authorization");

        // 2. 检查 Authorization 是否存在且以 Bearer 开头
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("请求缺少 Authorization Header 或格式错误");
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            R<Void> result = R.error(401, "请先登录");
            response.getWriter().write(new ObjectMapper().writeValueAsString(result));
            return false;
        }

        // 3. 提取 Token
        String token = authHeader.substring(7);

        // 4. 验证 Token
        if (!jwtUtil.validateToken(token)) {
            log.error("Token 无效或已过期");
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            R<Void> result = R.error(401, "登录已过期，请重新登录");
            response.getWriter().write(new ObjectMapper().writeValueAsString(result));
            return false;
        }

        // 5. 将用户信息存入 request 属性（可选，方便后续获取）
        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);
        java.util.List<String> roles = jwtUtil.getRoles(token);
        String role = (roles != null && !roles.isEmpty()) ? roles.get(0) : null;

        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("role", role);

        log.debug("用户 {} 请求 {} {}，Token 验证通过", username, request.getMethod(), request.getRequestURI());

        return true;
    }

}
