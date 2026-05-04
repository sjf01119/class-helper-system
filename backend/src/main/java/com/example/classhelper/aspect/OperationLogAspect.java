package com.example.classhelper.aspect;

import com.example.classhelper.common.R;
import com.example.classhelper.entity.OperationLog;
import com.example.classhelper.service.OperationLogService;
import com.example.classhelper.utils.SecurityUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    @Around("execution(* com.example.classhelper.controller..*(..)) && !execution(* com.example.classhelper.controller.LogController.*(..))")
    public Object recordOperationLog(ProceedingJoinPoint joinPoint) throws Throwable {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return joinPoint.proceed();
        }
        HttpServletRequest request = servletRequestAttributes.getRequest();
        if (shouldSkip(request)) {
            return joinPoint.proceed();
        }

        long start = System.currentTimeMillis();
        Object result = null;
        Throwable throwable = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            throwable = ex;
            throw ex;
        } finally {
            saveOperationLog(joinPoint, request, result, throwable, (int) (System.currentTimeMillis() - start));
        }
    }

    private void saveOperationLog(ProceedingJoinPoint joinPoint,
                                  HttpServletRequest request,
                                  Object result,
                                  Throwable throwable,
                                  int spendTime) {
        try {
            OperationLog entity = new OperationLog();
            entity.setUserId(SecurityUtil.getCurrentUserId());
            entity.setUsername(resolveUsername(request));
            entity.setOperation(resolveOperation(joinPoint));
            entity.setMethod(request.getMethod() + " " + request.getRequestURI());
            entity.setParams(resolveParams(joinPoint.getArgs()));
            entity.setIp(resolveClientIp(request));
            entity.setSpendTime(Math.max(spendTime, 0));
            entity.setCreatedAt(LocalDateTime.now());

            if (throwable != null) {
                entity.setStatus(0);
                entity.setErrorMsg(truncate(throwable.getMessage(), 1000));
            } else if (result instanceof R<?> r && !r.isSuccess()) {
                entity.setStatus(0);
                entity.setErrorMsg(truncate(r.getMessage(), 1000));
            } else {
                entity.setStatus(1);
            }

            operationLogService.save(entity);
        } catch (Exception e) {
            log.warn("写入操作日志失败: {}", e.getMessage());
        }
    }

    private boolean shouldSkip(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath() == null ? "" : request.getContextPath();
        String relativePath = uri.startsWith(contextPath) ? uri.substring(contextPath.length()) : uri;
        return relativePath.startsWith("/log/");
    }

    private String resolveOperation(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringType().getSimpleName() + "." + signature.getName();
    }

    private String resolveUsername(HttpServletRequest request) {
        String username = SecurityUtil.getCurrentUsername();
        if (username != null && !username.isBlank()) {
            return username;
        }
        String usernameParam = request.getParameter("username");
        return (usernameParam == null || usernameParam.isBlank()) ? "anonymous" : usernameParam;
    }

    private String resolveParams(Object[] args) {
        List<Object> filtered = new ArrayList<>();
        for (Object arg : args) {
            if (arg == null
                    || arg instanceof HttpServletRequest
                    || arg instanceof HttpServletResponse
                    || arg instanceof BindingResult
                    || arg instanceof MultipartFile
                    || arg instanceof MultipartFile[]
                    || arg instanceof byte[]) {
                continue;
            }
            filtered.add(arg);
        }
        if (filtered.isEmpty()) {
            return "";
        }
        try {
            return truncate(objectMapper.writeValueAsString(filtered.size() == 1 ? filtered.get(0) : filtered), 2000);
        } catch (JsonProcessingException e) {
            return truncate(Arrays.toString(filtered.toArray()), 2000);
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        String remoteAddr = request.getRemoteAddr();
        return (remoteAddr == null || remoteAddr.isBlank()) ? "unknown" : remoteAddr;
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
