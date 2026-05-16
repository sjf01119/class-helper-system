package com.example.classhelper.controller;

import com.example.classhelper.annotation.RequiresRole;
import com.example.classhelper.common.R;
import com.example.classhelper.dto.CourseBatchDTO;
import com.example.classhelper.dto.CourseDTO;
import com.example.classhelper.dto.CourseQueryDTO;
import com.example.classhelper.entity.Course;
import com.example.classhelper.entity.User;
import com.example.classhelper.exception.BusinessException;
import com.example.classhelper.mapper.TeacherClassMapper;
import com.example.classhelper.service.CourseService;
import com.example.classhelper.service.OssService;
import com.example.classhelper.service.UserManageService;
import com.example.classhelper.utils.SecurityUtil;
import com.example.classhelper.vo.PageVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 课程管理 Controller
 * 提供课程管理的 CRUD 接口，带权限控制
 */
@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final UserManageService userManageService;
    private final TeacherClassMapper teacherClassMapper;
    private final OssService ossService;

    /**
     * 分页查询课程列表（管理员/教师）
     */
    @GetMapping("/list")
    @RequiresRole({"admin", "teacher"})
    public R<PageVO<Course>> list(CourseQueryDTO queryDTO) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        PageVO<Course> pageVO = courseService.pageList(queryDTO, currentUserId, roles);
        return R.ok(pageVO);
    }

    /**
     * 新增课程（管理员/教师）
     */
    @PostMapping("/add")
    @RequiresRole({"admin", "teacher"})
    public R<Void> add(@Valid @RequestBody CourseDTO dto) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        boolean success = courseService.add(dto, currentUserId, roles);
        return success ? R.ok("新增成功", null) : R.error("新增失败");
    }

    /**
     * 编辑课程（管理员/教师）
     */
    @PutMapping("/update")
    @RequiresRole({"admin", "teacher"})
    public R<Void> update(@Valid @RequestBody CourseDTO dto) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        boolean success = courseService.update(dto, currentUserId, roles);
        return success ? R.ok("更新成功", null) : R.error("更新失败");
    }

    /**
     * 删除课程（管理员/教师）
     */
    @DeleteMapping("/{id}")
    @RequiresRole({"admin", "teacher"})
    public R<Void> delete(@PathVariable Long id) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        boolean success = courseService.remove(id, currentUserId, roles);
        return success ? R.ok("删除成功", null) : R.error("删除失败");
    }

    @PutMapping("/batch/status")
    @RequiresRole({"admin", "teacher"})
    public R<Void> batchUpdateStatus(@Valid @RequestBody CourseBatchDTO dto) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        boolean success = courseService.batchUpdateStatus(dto.getIds(), dto.getStatus(), currentUserId, roles);
        return success ? R.ok("批量更新成功", null) : R.error("批量更新失败");
    }

    @PostMapping("/batch-delete")
    @RequiresRole({"admin", "teacher"})
    public R<Void> batchDelete(@RequestBody List<Long> ids) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        boolean success = courseService.batchRemove(ids, currentUserId, roles);
        return success ? R.ok("批量删除成功", null) : R.error("批量删除失败");
    }

    /**
     * 获取课程详情（管理员/教师/学生）
     */
    @GetMapping("/{id}")
    @RequiresRole({"admin", "teacher", "student"})
    public R<Course> getById(@PathVariable Long id) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        Course course = courseService.getDetail(id, currentUserId, roles);
        return R.ok(course);
    }

    /**
     * 获取我的课程（教师查看自己授课的课程）
     */
    @GetMapping("/my-teacher")
    @RequiresRole("teacher")
    public R<List<Course>> getMyTeacherCourses() {
        Long teacherId = SecurityUtil.getCurrentUserId();
        List<Course> courseList = courseService.getByTeacherId(teacherId);
        return R.ok(courseList);
    }

    @PostMapping("/cover")
    @RequiresRole({"admin", "teacher"})
    public R<String> uploadCourseCover(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择课程封面");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!contentType.startsWith("image/")) {
            throw new BusinessException(400, "课程封面仅支持图片格式");
        }
        if (file.getSize() > 50L * 1024 * 1024) {
            throw new BusinessException(400, "课程封面大小不能超过50MB");
        }
        String originalName = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename().toLowerCase() : "";
        if (!(originalName.endsWith(".png") || originalName.endsWith(".jpg") || originalName.endsWith(".jpeg")
                || originalName.endsWith(".gif") || originalName.endsWith(".webp"))) {
            throw new BusinessException(400, "课程封面仅支持 PNG/JPG/JPEG/GIF/WEBP 格式");
        }
        String coverUrl = ossService.uploadFile(file, "course-covers");
        return R.ok("上传成功", coverUrl);
    }

    /**
     * 获取我的课程（学生查看班级课程）
     */
    @GetMapping("/my-student")
    @RequiresRole("student")
    public R<List<Course>> getMyStudentCourses() {
        Long studentId = SecurityUtil.getCurrentUserId();
        User user = userManageService.getById(studentId);
        if (user == null || user.getClassId() == null) {
            return R.ok(List.of());
        }
        List<Course> courseList = courseService.getByClassId(user.getClassId());
        return R.ok(courseList);
    }

    /**
     * 根据班级ID获取课程列表
     */
    @GetMapping("/by-class/{classId}")
    @RequiresRole({"admin", "teacher", "student"})
    public R<List<Course>> getByClassId(@PathVariable Long classId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        if (currentUserId == null) {
            throw new BusinessException(401, "请先登录");
        }
        if (!roles.contains("admin")) {
            if (roles.contains("teacher")) {
                Set<Long> teacherClassIds = teacherClassMapper.selectClassIdsByTeacherId(currentUserId).stream()
                        .filter(id -> id != null && id > 0)
                        .collect(Collectors.toSet());
                if (!teacherClassIds.contains(classId)) {
                    throw new BusinessException(403, "无权查看该班级课程");
                }
            } else if (roles.contains("student")) {
                User user = userManageService.getById(currentUserId);
                if (user == null || user.getClassId() == null || !user.getClassId().equals(classId)) {
                    throw new BusinessException(403, "只能查看本人班级课程");
                }
            } else {
                throw new BusinessException(403, "无权限访问该资源");
            }
        }
        List<Course> courseList = courseService.getByClassId(classId);
        return R.ok(courseList);
    }

}
