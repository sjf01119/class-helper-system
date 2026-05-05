package com.example.classhelper.controller;

import com.example.classhelper.annotation.RequiresRole;
import com.example.classhelper.common.R;
import com.example.classhelper.dto.UserDTO;
import com.example.classhelper.dto.UserQueryDTO;
import com.example.classhelper.entity.Clazz;
import com.example.classhelper.entity.User;
import com.example.classhelper.exception.BusinessException;
import com.example.classhelper.service.ClazzService;
import com.example.classhelper.service.OssService;
import com.example.classhelper.service.UserManageService;
import com.example.classhelper.utils.SecurityUtil;
import com.example.classhelper.vo.PageVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户管理 Controller
 * 提供用户管理的 CRUD 接口，带权限控制
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserManageService userManageService;
    private final ClazzService clazzService;
    private final OssService ossService;

    /**
     * 分页查询用户列表
     * 管理员：查看所有用户
     * 教师：查看本班学生
     * 学生：仅查看自己
     */
    @GetMapping("/list")
    @RequiresRole({"admin", "teacher"})
    public R<PageVO<User>> list(UserQueryDTO queryDTO) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        PageVO<User> pageVO = userManageService.pageList(queryDTO, currentUserId, roles);
        return R.ok(pageVO);
    }

    /**
     * 新增用户（仅管理员）
     */
    @PostMapping("/add")
    @RequiresRole("admin")
    public R<Void> add(@Valid @RequestBody UserDTO dto) {
        boolean success = userManageService.add(dto);
        return success ? R.ok("新增成功", null) : R.error("新增失败");
    }

    /**
     * 编辑用户
     * 管理员：编辑所有用户
     * 教师：编辑本班学生
     */
    @PutMapping("/update")
    @RequiresRole({"admin", "teacher"})
    public R<Void> update(@Valid @RequestBody UserDTO dto) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        boolean success = userManageService.update(dto, currentUserId, roles);
        return success ? R.ok("更新成功", null) : R.error("更新失败");
    }

    /**
     * 删除用户（仅管理员）
     */
    @DeleteMapping("/{id}")
    @RequiresRole("admin")
    public R<Void> delete(@PathVariable Long id) {
        boolean success = userManageService.remove(id);
        return success ? R.ok("删除成功", null) : R.error("删除失败");
    }

    @DeleteMapping("/batch")
    @RequiresRole("admin")
    public R<Void> deleteBatch(@RequestParam List<Long> ids) {
        boolean success = userManageService.removeBatch(ids);
        return success ? R.ok("批量删除成功", null) : R.error("批量删除失败");
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    @RequiresRole({"admin", "teacher", "student"})
    public R<User> getById(@PathVariable Long id) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        User user = userManageService.getDetail(id, currentUserId, roles);
        return R.ok(user);
    }

    /**
     * 重置密码（仅管理员）
     */
    @PostMapping("/resetPwd/{id}")
    @RequiresRole("admin")
    public R<Void> resetPassword(@PathVariable Long id) {
        // 默认密码 123456
        boolean success = userManageService.resetPassword(id, "123456");
        return success ? R.ok("密码重置成功", null) : R.error("密码重置失败");
    }

    /**
     * 获取教师管理的学生列表（教师使用）
     */
    @GetMapping("/my-students")
    @RequiresRole("teacher")
    public R<List<User>> getMyStudents() {
        Long teacherId = SecurityUtil.getCurrentUserId();
        List<User> students = userManageService.getStudentsByTeacherId(teacherId);
        return R.ok(students);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    @RequiresRole({"admin", "teacher", "student"})
    public R<User> getCurrentUserInfo() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        User user = userManageService.getDetail(currentUserId, currentUserId, roles);
        return R.ok(user);
    }

    /**
     * 获取班级学生列表
     */
    @GetMapping("/class-students/{classId}")
    @RequiresRole({"admin", "teacher", "student"})
    public R<List<User>> getClassStudents(@PathVariable Long classId) {
        validateClassAccess(classId);
        List<User> students = userManageService.getStudentsByClassId(classId);
        return R.ok(students);
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    @RequiresRole({"admin", "teacher", "student"})
    public R<Void> changePassword(@RequestBody ChangePasswordDTO dto) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        boolean success = userManageService.changePassword(currentUserId, dto.getOldPassword(), dto.getNewPassword());
        return success ? R.ok("密码修改成功", null) : R.error("密码修改失败");
    }

    /**
     * 修改当前登录用户个人信息
     */
    @PutMapping("/profile")
    @RequiresRole({"admin", "teacher", "student"})
    public R<Void> updateProfile(@Valid @RequestBody UpdateProfileDTO dto) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        User user = userManageService.getById(currentUserId);
        if (user == null || Integer.valueOf(1).equals(user.getIsDeleted())) {
            throw new BusinessException("用户不存在");
        }

        user.setRealName(dto.getRealName().trim());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setAvatarUrl(dto.getAvatarUrl());
        user.setNickname(dto.getNickname());
        user.setGender(dto.getGender());
        boolean success = userManageService.updateById(user);
        return success ? R.ok("更新成功", null) : R.error("更新失败");
    }

    @PostMapping("/profile/avatar")
    @RequiresRole({"admin", "teacher", "student"})
    public R<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择头像文件");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        String originalName = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename().toLowerCase() : "";
        boolean validContentType = "image/jpeg".equals(contentType) || "image/png".equals(contentType) || "image/jpg".equals(contentType);
        boolean validExtension = originalName.endsWith(".jpg") || originalName.endsWith(".jpeg") || originalName.endsWith(".png");
        if (!validContentType || !validExtension) {
            throw new BusinessException(400, "头像仅支持 JPG、PNG、JPEG 格式");
        }
        if (file.getSize() > 50L * 1024 * 1024) {
            throw new BusinessException(400, "头像大小不能超过50MB");
        }

        Long currentUserId = SecurityUtil.getCurrentUserId();
        User user = userManageService.getById(currentUserId);
        if (user == null || Integer.valueOf(1).equals(user.getIsDeleted())) {
            throw new BusinessException("用户不存在");
        }

        String oldAvatarUrl = user.getAvatarUrl();
        String avatarUrl = ossService.uploadFile(file, "avatars");
        user.setAvatarUrl(avatarUrl);
        boolean success = userManageService.updateById(user);
        if (!success) {
            throw new BusinessException("头像保存失败");
        }
        if (oldAvatarUrl != null && !oldAvatarUrl.isBlank() && !oldAvatarUrl.equals(avatarUrl)) {
            try {
                ossService.deleteFile(oldAvatarUrl);
            } catch (Exception ignored) {
                // 头像已完成新地址保存，旧文件清理失败不阻断主流程。
            }
        }
        return R.ok("上传成功", avatarUrl);
    }

    /**
     * 修改密码 DTO
     */
    @Data
    public static class ChangePasswordDTO {
        @NotBlank(message = "原密码不能为空")
        private String oldPassword;

        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 20, message = "新密码长度必须为6-20位")
        private String newPassword;
    }

    /**
     * 更新当前用户资料 DTO
     */
    @Data
    public static class UpdateProfileDTO {
        @NotBlank(message = "姓名不能为空")
        @Size(max = 50, message = "姓名长度不能超过50")
        private String realName;

        @Email(message = "邮箱格式不正确")
        private String email;

        @Pattern(regexp = "^(|1[3-9]\\d{9})$", message = "手机号格式不正确")
        private String phone;

        private String avatarUrl;

        @Size(max = 50, message = "昵称长度不能超过50")
        private String nickname;

        private Integer gender;
    }

    private void validateClassAccess(Long classId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        if (roles.contains("admin")) {
            return;
        }
        if (roles.contains("teacher")) {
            boolean hasAccess = clazzService.getByTeacherId(currentUserId).stream()
                    .map(Clazz::getId)
                    .anyMatch(classId::equals);
            if (!hasAccess) {
                throw new BusinessException("无权限查看该班级学生");
            }
            return;
        }
        if (roles.contains("student")) {
            User currentUser = userManageService.getById(currentUserId);
            if (currentUser == null || !classId.equals(currentUser.getClassId())) {
                throw new BusinessException("无权限查看该班级学生");
            }
        }
    }

}
