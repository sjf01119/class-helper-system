package com.example.classhelper.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.classhelper.annotation.RequiresRole;
import com.example.classhelper.common.R;
import com.example.classhelper.dto.LoginDTO;
import com.example.classhelper.dto.RegisterDTO;
import com.example.classhelper.entity.LoginLog;
import com.example.classhelper.entity.Role;
import com.example.classhelper.entity.User;
import com.example.classhelper.entity.UserRole;
import com.example.classhelper.exception.BusinessException;
import com.example.classhelper.service.LoginLogService;
import com.example.classhelper.service.RoleService;
import com.example.classhelper.service.UserRoleService;
import com.example.classhelper.service.UserService;
import com.example.classhelper.service.ClazzService;
import com.example.classhelper.utils.JwtUtil;
import com.example.classhelper.utils.SecurityUtil;
import com.example.classhelper.vo.LoginVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 认证控制器
 * 处理用户登录、登出、获取当前用户信息等
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserRoleService userRoleService;
    private final RoleService roleService;
    private final ClazzService clazzService;
    private final LoginLogService loginLogService;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 登录结果（包含 JWT Token）
     */
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        log.info("用户登录: {}", loginDTO.getUsername());
        
        // 1. 根据用户名查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = userService.getOne(wrapper);

        if (user == null) {
            recordLoginLog(null, loginDTO.getUsername(), 0, "用户名或密码错误", request);
            throw new BusinessException("用户名或密码错误");
        }

        // 2. 校验用户状态
        if (user.getStatus() == 0) {
            recordLoginLog(user.getId(), user.getUsername(), 0, "账号已被禁用，请联系管理员", request);
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 3. 校验密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            recordLoginLog(user.getId(), user.getUsername(), 0, "用户名或密码错误", request);
            throw new BusinessException("用户名或密码错误");
        }

        // 4. 查询用户角色
        LambdaQueryWrapper<UserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.eq(UserRole::getUserId, user.getId());
        List<UserRole> userRoles = userRoleService.list(userRoleWrapper);

        List<String> roleCodes = userRoles.stream()
                .map(ur -> {
                    Role role = roleService.getById(ur.getRoleId());
                    return role != null ? role.getRoleCode() : "";
                })
                .filter(code -> !code.isEmpty())
                .map(code -> code.trim().toLowerCase(Locale.ROOT))
                .map(code -> code.startsWith("role_") ? code.substring(5) : code)
                .distinct()
                .collect(Collectors.toList());

        // 5. 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roleCodes);

        // 6. 组装返回数据
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRoles(roleCodes);

        LoginVO.UserInfoVO userInfoVO = new LoginVO.UserInfoVO();
        userInfoVO.setId(user.getId());
        userInfoVO.setUsername(user.getUsername());
        userInfoVO.setNickname(user.getNickname());
        userInfoVO.setRealName(user.getRealName());
        userInfoVO.setAvatarUrl(user.getAvatarUrl());
        userInfoVO.setEmail(user.getEmail());
        userInfoVO.setPhone(user.getPhone());
        userInfoVO.setGender(user.getGender());
        userInfoVO.setIsHeadTeacher(roleCodes.contains("teacher") && clazzService.isHeadTeacher(user.getId()));
        loginVO.setUserInfo(userInfoVO);

        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(resolveClientIp(request));
        userService.updateById(user);
        recordLoginLog(user.getId(), user.getUsername(), 1, "登录成功", request);

        log.info("用户登录成功: {}, 角色: {}", user.getUsername(), roleCodes);
        return R.ok("登录成功", loginVO);
    }

    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        log.info("学生注册: {}", registerDTO.getUsername());

        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(User::getUsername, registerDTO.getUsername());
        if (userService.count(usernameWrapper) > 0) {
            throw new BusinessException("用户名已被注册");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        String displayName = registerDTO.getRealName();
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = registerDTO.getUsername();
        }
        user.setRealName(displayName);
        user.setNickname(displayName);
        user.setPhone(registerDTO.getPhone());
        user.setStatus(1);

        boolean saved = userService.save(user);
        if (!saved) {
            throw new BusinessException("注册失败，请稍后重试");
        }

        LambdaQueryWrapper<Role> studentRoleWrapper = new LambdaQueryWrapper<>();
        studentRoleWrapper.eq(Role::getRoleCode, "student");
        Role studentRole = roleService.getOne(studentRoleWrapper);
        if (studentRole != null) {
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(studentRole.getId());
            userRoleService.save(userRole);
        }

        log.info("学生注册成功: {}", registerDTO.getUsername());
        return R.ok("注册成功，请登录", null);
    }

    /**
     * 用户登出（前端清除 Token）
     */
    @PostMapping("/logout")
    public R<Void> logout() {
        // JWT 是无状态的，登出只需前端清除 Token
        // 如果需要实现 Token 黑名单，可以在此将 Token 加入黑名单
        log.info("用户登出");
        return R.ok("登出成功", null);
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/info")
    public R<LoginVO.UserInfoVO> getUserInfo() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }

        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }

        LoginVO.UserInfoVO userInfoVO = new LoginVO.UserInfoVO();
        userInfoVO.setId(user.getId());
        userInfoVO.setUsername(user.getUsername());
        userInfoVO.setNickname(user.getNickname());
        userInfoVO.setRealName(user.getRealName());
        userInfoVO.setAvatarUrl(user.getAvatarUrl());
        userInfoVO.setEmail(user.getEmail());
        userInfoVO.setPhone(user.getPhone());
        userInfoVO.setGender(user.getGender());
        userInfoVO.setIsHeadTeacher(SecurityUtil.hasRole("teacher") && clazzService.isHeadTeacher(user.getId()));

        return R.ok(userInfoVO);
    }

    /**
     * 管理员专用接口示例
     */
    @GetMapping("/admin-only")
    @RequiresRole("admin")
    public R<String> adminOnly() {
        return R.ok("这是管理员专用接口");
    }

    /**
     * 教师专用接口示例
     */
    @GetMapping("/teacher-only")
    @RequiresRole("teacher")
    public R<String> teacherOnly() {
        return R.ok("这是教师专用接口");
    }

    /**
     * 学生专用接口示例
     */
    @GetMapping("/student-only")
    @RequiresRole("student")
    public R<String> studentOnly() {
        return R.ok("这是学生专用接口");
    }

    private void recordLoginLog(Long userId, String username, Integer status, String message, HttpServletRequest request) {
        try {
            LoginLog logEntity = new LoginLog();
            logEntity.setUserId(userId);
            logEntity.setUsername(username);
            logEntity.setLoginStatus(status);
            logEntity.setMsg(message);
            logEntity.setIp(resolveClientIp(request));
            logEntity.setLocation("未知");
            String userAgent = request.getHeader("User-Agent");
            logEntity.setBrowser(parseBrowser(userAgent));
            logEntity.setOs(parseOs(userAgent));
            logEntity.setCreatedAt(LocalDateTime.now());
            loginLogService.save(logEntity);
        } catch (Exception e) {
            log.warn("写入登录日志失败: {}", e.getMessage());
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
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

    private String parseBrowser(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "未知";
        }
        String ua = userAgent.toLowerCase(Locale.ROOT);
        if (ua.contains("edg/")) {
            return "Edge";
        }
        if (ua.contains("chrome/")) {
            return "Chrome";
        }
        if (ua.contains("firefox/")) {
            return "Firefox";
        }
        if (ua.contains("safari/") && !ua.contains("chrome/")) {
            return "Safari";
        }
        if (ua.contains("msie") || ua.contains("trident/")) {
            return "IE";
        }
        return "其他";
    }

    private String parseOs(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "未知";
        }
        String ua = userAgent.toLowerCase(Locale.ROOT);
        if (ua.contains("windows")) {
            return "Windows";
        }
        if (ua.contains("mac os")) {
            return "macOS";
        }
        if (ua.contains("android")) {
            return "Android";
        }
        if (ua.contains("iphone") || ua.contains("ipad")) {
            return "iOS";
        }
        if (ua.contains("linux")) {
            return "Linux";
        }
        return "其他";
    }

}
