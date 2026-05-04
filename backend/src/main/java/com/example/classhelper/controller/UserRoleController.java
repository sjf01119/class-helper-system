package com.example.classhelper.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.classhelper.annotation.RequiresRole;
import com.example.classhelper.common.R;
import com.example.classhelper.entity.UserRole;
import com.example.classhelper.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user-role")
@RequiredArgsConstructor
@RequiresRole("admin")
public class UserRoleController {

    private final UserRoleService userRoleService;

    @GetMapping("/page")
    public R<Page<UserRole>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long roleId) {
        Page<UserRole> page = new Page<>(current, size);
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, UserRole::getUserId, userId);
        wrapper.eq(roleId != null, UserRole::getRoleId, roleId);
        wrapper.orderByDesc(UserRole::getCreatedAt);
        return R.ok(userRoleService.page(page, wrapper));
    }

    @GetMapping("/list")
    public R<List<UserRole>> list(@RequestParam(required = false) Long userId) {
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, UserRole::getUserId, userId);
        return R.ok(userRoleService.list(wrapper));
    }

    @GetMapping("/{id}")
    public R<UserRole> getById(@PathVariable Long id) {
        return R.ok(userRoleService.getById(id));
    }

    @PostMapping
    public R<Boolean> save(@RequestBody UserRole userRole) {
        return R.ok(userRoleService.save(userRole));
    }

    @PostMapping("/batch")
    public R<Boolean> saveBatch(@RequestBody List<UserRole> userRoles) {
        return R.ok(userRoleService.saveBatch(userRoles));
    }

    @PutMapping
    public R<Boolean> update(@RequestBody UserRole userRole) {
        return R.ok(userRoleService.updateById(userRole));
    }

    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(userRoleService.removeById(id));
    }

    @DeleteMapping("/batch")
    public R<Boolean> deleteBatch(@RequestParam List<Long> ids) {
        return R.ok(userRoleService.removeByIds(ids));
    }

}
