package com.example.classhelper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.classhelper.entity.Role;
import com.example.classhelper.entity.User;
import com.example.classhelper.entity.UserRole;
import com.example.classhelper.service.RoleService;
import com.example.classhelper.service.UserRoleService;
import com.example.classhelper.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * UserDetailsService 实现类
 * 用于 Spring Security 加载用户信息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;
    private final UserRoleService userRoleService;
    private final RoleService roleService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userService.getOne(wrapper);
        
        if (user == null) {
            log.error("用户不存在: {}", username);
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        
        // 2. 查询用户角色
        LambdaQueryWrapper<UserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.eq(UserRole::getUserId, user.getId());
        List<UserRole> userRoles = userRoleService.list(userRoleWrapper);
        
        // 3. 获取角色编码列表
        List<String> roleCodes = userRoles.stream()
                .map(ur -> {
                    Role role = roleService.getById(ur.getRoleId());
                    return role != null ? role.getRoleCode() : "";
                })
                .filter(code -> !code.isEmpty())
                .collect(Collectors.toList());
        
        // 4. 转换为 Spring Security 权限
        List<SimpleGrantedAuthority> authorities = roleCodes.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        
        // 5. 构建 UserDetails 对象
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountLocked(user.getStatus() == 0)
                .disabled(user.getStatus() == 0)
                .build();
    }

}
