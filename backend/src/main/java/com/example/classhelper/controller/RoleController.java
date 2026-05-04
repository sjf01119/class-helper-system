package com.example.classhelper.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.classhelper.annotation.RequiresRole;
import com.example.classhelper.common.R;
import com.example.classhelper.entity.Role;
import com.example.classhelper.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
@RequiresRole("admin")
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/page")
    public R<Page<Role>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String roleName) {
        Page<Role> page = new Page<>(current, size);
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(roleName != null, Role::getRoleName, roleName);
        wrapper.orderByAsc(Role::getSortOrder);
        return R.ok(roleService.page(page, wrapper));
    }

    @GetMapping("/list")
    public R<List<Role>> list() {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getStatus, 1);
        wrapper.orderByAsc(Role::getSortOrder);
        return R.ok(roleService.list(wrapper));
    }

    @GetMapping("/{id}")
    public R<Role> getById(@PathVariable Long id) {
        return R.ok(roleService.getById(id));
    }

    @PostMapping
    public R<Boolean> save(@RequestBody Role role) {
        return R.ok(roleService.save(role));
    }

    @PutMapping
    public R<Boolean> update(@RequestBody Role role) {
        return R.ok(roleService.updateById(role));
    }

    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(roleService.removeById(id));
    }

    @DeleteMapping("/batch")
    public R<Boolean> deleteBatch(@RequestParam List<Long> ids) {
        return R.ok(roleService.removeByIds(ids));
    }

}
