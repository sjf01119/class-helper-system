package com.example.classhelper.controller;

import com.example.classhelper.annotation.RequiresRole;
import com.example.classhelper.common.R;
import com.example.classhelper.dto.ClazzDTO;
import com.example.classhelper.dto.ClazzQueryDTO;
import com.example.classhelper.entity.Clazz;
import com.example.classhelper.entity.User;
import com.example.classhelper.exception.BusinessException;
import com.example.classhelper.service.ClazzService;
import com.example.classhelper.service.UserManageService;
import com.example.classhelper.utils.SecurityUtil;
import com.example.classhelper.vo.PageVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 班级管理 Controller
 * 提供班级管理的 CRUD 接口，带权限控制
 */
@RestController
@RequestMapping("/class")
@RequiredArgsConstructor
public class ClazzController {

    private final ClazzService clazzService;
    private final UserManageService userManageService;

    /**
     * 分页查询班级列表（管理员）
     */
    @GetMapping("/list")
    @RequiresRole("admin")
    public R<PageVO<Clazz>> list(ClazzQueryDTO queryDTO) {
        PageVO<Clazz> pageVO = clazzService.pageList(queryDTO);
        return R.ok(pageVO);
    }

    /**
     * 新增班级（管理员）
     */
    @PostMapping("/add")
    @RequiresRole("admin")
    public R<Void> add(@Valid @RequestBody ClazzDTO dto) {
        boolean success = clazzService.add(dto);
        return success ? R.ok("新增成功", null) : R.error("新增失败");
    }

    /**
     * 编辑班级（管理员）
     */
    @PutMapping("/update")
    @RequiresRole("admin")
    public R<Void> update(@Valid @RequestBody ClazzDTO dto) {
        boolean success = clazzService.update(dto);
        return success ? R.ok("更新成功", null) : R.error("更新失败");
    }

    /**
     * 删除班级（管理员）
     */
    @DeleteMapping("/{id}")
    @RequiresRole("admin")
    public R<Void> delete(@PathVariable Long id) {
        boolean success = clazzService.remove(id);
        return success ? R.ok("删除成功", null) : R.error("删除失败");
    }

    /**
     * 获取班级详情（管理员）
     */
    @GetMapping("/{id}")
    @RequiresRole({"admin", "teacher", "student"})
    public R<Clazz> getById(@PathVariable Long id) {
        validateClassAccess(id);
        Clazz clazz = clazzService.getDetail(id);
        return R.ok(clazz);
    }

    /**
     * 获取教师关联班级（授课/管理班级范围）
     */
    @GetMapping("/my-teacher")
    @RequiresRole("teacher")
    public R<List<Clazz>> getMyTeacherClasses() {
        Long teacherId = SecurityUtil.getCurrentUserId();
        List<Clazz> clazzList = clazzService.getByTeacherId(teacherId);
        return R.ok(clazzList);
    }

    /**
     * 获取教师担任班主任的班级
     */
    @GetMapping("/my-head-teacher")
    @RequiresRole("teacher")
    public R<List<Clazz>> getMyHeadTeacherClasses() {
        Long teacherId = SecurityUtil.getCurrentUserId();
        List<Clazz> clazzList = clazzService.getHeadTeacherClasses(teacherId);
        return R.ok(clazzList);
    }

    @GetMapping("/head-teacher/status")
    @RequiresRole("teacher")
    public R<Boolean> getHeadTeacherStatus() {
        Long teacherId = SecurityUtil.getCurrentUserId();
        return R.ok(clazzService.isHeadTeacher(teacherId));
    }

    /**
     * 获取我的班级（学生查看自己所在班级）
     */
    @GetMapping("/my-student")
    @RequiresRole("student")
    public R<Clazz> getMyStudentClass() {
        Long studentId = SecurityUtil.getCurrentUserId();
        User user = userManageService.getById(studentId);
        if (user == null || user.getClassId() == null) {
            return R.ok(null);
        }
        Clazz clazz = clazzService.getDetail(user.getClassId());
        return R.ok(clazz);
    }

    /**
     * 通过邀请码加入班级
     */
    @PostMapping("/join")
    @RequiresRole("student")
    public R<Void> joinClassByInviteCode(@RequestBody Map<String, String> params) {
        String inviteCode = params.get("inviteCode");
        Long studentId = SecurityUtil.getCurrentUserId();
        boolean success = clazzService.joinByInviteCode(inviteCode, studentId);
        return success ? R.ok("加入班级成功", null) : R.error("邀请码无效或加入失败");
    }

    /**
     * 获取所有班级列表（用于下拉选择）
     */
    @GetMapping("/all")
    @RequiresRole("admin")
    public R<List<Clazz>> getAllClasses() {
        return R.ok(clazzService.getAllAvailableClasses());
    }

    @PutMapping("/{id}/reset-invite-code")
    @RequiresRole("admin")
    public R<Clazz> resetInviteCode(@PathVariable Long id) {
        return R.ok("邀请码已重置", clazzService.resetInviteCode(id));
    }

    private void validateClassAccess(Long classId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        if (roles.contains("admin")) {
            return;
        }
        if (roles.contains("teacher")) {
            boolean hasAccess = clazzService.getByTeacherId(currentUserId).stream()
                    .anyMatch(item -> classId.equals(item.getId()));
            if (!hasAccess) {
                throw new BusinessException("无权限查看该班级");
            }
            return;
        }
        if (roles.contains("student")) {
            User currentUser = userManageService.getById(currentUserId);
            if (currentUser == null || !classId.equals(currentUser.getClassId())) {
                throw new BusinessException("无权限查看该班级");
            }
        }
    }

}
