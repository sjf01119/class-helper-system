package com.example.classhelper.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.classhelper.common.R;
import com.example.classhelper.entity.LoginLog;
import com.example.classhelper.entity.OperationLog;
import com.example.classhelper.service.LoginLogService;
import com.example.classhelper.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/log")
@RequiredArgsConstructor
public class LogController {

    private final OperationLogService operationLogService;
    private final LoginLogService loginLogService;

    // ==================== 操作日志 ====================

    @GetMapping("/operation/page")
    public R<Page<OperationLog>> operationPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) Integer status) {
        Page<OperationLog> page = new Page<>(current, size);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(username != null && !username.isEmpty(), OperationLog::getUsername, username);
        wrapper.like(operation != null && !operation.isEmpty(), OperationLog::getOperation, operation);
        wrapper.eq(status != null, OperationLog::getStatus, status);
        wrapper.orderByDesc(OperationLog::getCreatedAt);
        return R.ok(operationLogService.page(page, wrapper));
    }

    @GetMapping("/operation/list")
    public R<List<OperationLog>> operationList(@RequestParam(required = false) Long userId) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, OperationLog::getUserId, userId);
        wrapper.orderByDesc(OperationLog::getCreatedAt);
        return R.ok(operationLogService.list(wrapper));
    }

    @DeleteMapping("/operation/{id}")
    public R<Boolean> deleteOperationLog(@PathVariable Long id) {
        return R.ok(operationLogService.removeById(id));
    }

    @DeleteMapping("/operation/batch")
    public R<Boolean> deleteOperationLogBatch(@RequestParam List<Long> ids) {
        return R.ok(operationLogService.removeByIds(ids));
    }

    // ==================== 登录日志 ====================

    @GetMapping("/login/page")
    public R<Page<LoginLog>> loginPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer loginStatus) {
        Page<LoginLog> page = new Page<>(current, size);
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(username != null && !username.isEmpty(), LoginLog::getUsername, username);
        wrapper.eq(loginStatus != null, LoginLog::getLoginStatus, loginStatus);
        wrapper.orderByDesc(LoginLog::getCreatedAt);
        return R.ok(loginLogService.page(page, wrapper));
    }

    @GetMapping("/login/list")
    public R<List<LoginLog>> loginList(@RequestParam(required = false) Long userId) {
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, LoginLog::getUserId, userId);
        wrapper.orderByDesc(LoginLog::getCreatedAt);
        return R.ok(loginLogService.list(wrapper));
    }

    @DeleteMapping("/login/{id}")
    public R<Boolean> deleteLoginLog(@PathVariable Long id) {
        return R.ok(loginLogService.removeById(id));
    }

    @DeleteMapping("/login/batch")
    public R<Boolean> deleteLoginLogBatch(@RequestParam List<Long> ids) {
        return R.ok(loginLogService.removeByIds(ids));
    }

}
