package com.example.classhelper.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.classhelper.annotation.RequiresRole;
import com.example.classhelper.common.R;
import com.example.classhelper.entity.Clazz;
import com.example.classhelper.entity.Course;
import com.example.classhelper.entity.LearningResource;
import com.example.classhelper.entity.User;
import com.example.classhelper.exception.BusinessException;
import com.example.classhelper.mapper.TeacherClassMapper;
import com.example.classhelper.service.ClazzService;
import com.example.classhelper.service.CourseService;
import com.example.classhelper.service.LearningResourceService;
import com.example.classhelper.service.OssService;
import com.example.classhelper.service.UserService;
import com.example.classhelper.utils.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/resource")
@RequiredArgsConstructor
public class LearningResourceController {

    private final LearningResourceService resourceService;
    private final OssService ossService;
    private final UserService userService;
    private final CourseService courseService;
    private final ClazzService clazzService;
    private final TeacherClassMapper teacherClassMapper;

    @GetMapping("/page")
    @RequiresRole({"admin", "teacher", "student"})
    public R<Page<LearningResource>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String fileType) {
        
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }

        Page<LearningResource> page = new Page<>(current, size);
        LambdaQueryWrapper<LearningResource> wrapper = new LambdaQueryWrapper<>();
        
        // 权限隔离处理
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        if (roles.contains("student")) {
            User student = userService.getById(userId);
            if (student == null || student.getClassId() == null) {
                return R.ok(emptyPage(page)); // 未分班的学生看不到资料
            }
            applyStudentScope(wrapper, student);
            wrapper.eq(LearningResource::getStatus, 1);
        } else if (roles.contains("teacher")) {
            applyTeacherScope(wrapper, userId);
        } else if (roles.contains("admin")) {
            // 管理员可以查看全部
        } else {
            throw new BusinessException(403, "无权限访问资料");
        }

        String normalizedCategory = normalizeText(category);
        String normalizedTitle = normalizeText(title);
        String normalizedFileType = normalizeFileType(fileType);

        wrapper.eq(classId != null, LearningResource::getClassId, classId);
        wrapper.eq(courseId != null, LearningResource::getCourseId, courseId);
        wrapper.eq(StringUtils.hasText(normalizedCategory), LearningResource::getCategory, normalizedCategory);
        wrapper.eq(StringUtils.hasText(normalizedFileType), LearningResource::getFileType, normalizedFileType);
        wrapper.like(StringUtils.hasText(normalizedTitle), LearningResource::getTitle, normalizedTitle);
        wrapper.orderByDesc(LearningResource::getCreatedAt);
        Page<LearningResource> result = resourceService.page(page, wrapper);
        enrichResources(result.getRecords());
        return R.ok(result);
    }

    @GetMapping("/list")
    @RequiresRole({"admin", "teacher", "student"})
    public R<List<LearningResource>> list(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String fileType) {
        LambdaQueryWrapper<LearningResource> wrapper = new LambdaQueryWrapper<>();
        
        // 权限隔离处理
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        if (roles.contains("student")) {
            User student = userService.getById(userId);
            if (student == null || student.getClassId() == null) {
                return R.ok(List.of());
            }
            applyStudentScope(wrapper, student);
            wrapper.eq(LearningResource::getStatus, 1);
        } else if (roles.contains("teacher")) {
            applyTeacherScope(wrapper, userId);
        } else if (!roles.contains("admin")) {
            throw new BusinessException(403, "无权限访问资料");
        }

        String normalizedCategory = normalizeText(category);
        String normalizedTitle = normalizeText(title);
        String normalizedFileType = normalizeFileType(fileType);

        wrapper.eq(classId != null, LearningResource::getClassId, classId);
        wrapper.eq(courseId != null, LearningResource::getCourseId, courseId);
        wrapper.eq(StringUtils.hasText(normalizedCategory), LearningResource::getCategory, normalizedCategory);
        wrapper.eq(StringUtils.hasText(normalizedFileType), LearningResource::getFileType, normalizedFileType);
        wrapper.like(StringUtils.hasText(normalizedTitle), LearningResource::getTitle, normalizedTitle);
        wrapper.orderByDesc(LearningResource::getCreatedAt);
        List<LearningResource> result = resourceService.list(wrapper);
        enrichResources(result);
        return R.ok(result);
    }

    @GetMapping("/{id}")
    @RequiresRole({"admin", "teacher", "student"})
    public R<LearningResource> getById(@PathVariable Long id) {
        LearningResource resource = checkResourcePermission(id);
        incrementViewCount(resource);
        enrichResource(resource);
        return R.ok(resource);
    }

    @PostMapping("/upload")
    @RequiresRole({"admin", "teacher"})
    public R<LearningResource> uploadAndSave(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "classId", required = false) Long classId,
            @RequestParam("courseId") Long courseId,
            @RequestParam("category") String category,
            @RequestParam(value = "status", defaultValue = "1") Integer status) {

        if (file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }

        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "请先登录");
        }

        List<String> roles = SecurityUtil.getCurrentUserRoles();
        Course course = courseService.getById(courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }
        if (roles.contains("teacher") && !roles.contains("admin") && !Objects.equals(course.getTeacherId(), currentUserId)) {
            throw new BusinessException(403, "只能给自己的课程上传资料");
        }

        // 1. 上传到阿里云 OSS
        String fileUrl = ossService.uploadFile(file, "materials");

        // 2. 提取文件信息
        String originalFilename = file.getOriginalFilename();
        String fileType = "";
        if (originalFilename != null && originalFilename.lastIndexOf(".") > 0) {
            fileType = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toUpperCase();
        }
        long fileSize = file.getSize();

        // 3. 构造数据库记录并保存
        LearningResource resource = new LearningResource();
        resource.setTitle(normalizeText(title));
        resource.setDescription(normalizeText(description));
        resource.setClassId(course.getClassId() != null ? course.getClassId() : classId);
        resource.setCourseId(courseId);
        resource.setCategory(StringUtils.hasText(normalizeText(category)) ? normalizeText(category) : "default");
        resource.setStatus(status);
        resource.setUploadBy(currentUserId);
        resource.setFileName(originalFilename);
        resource.setFileUrl(fileUrl);
        resource.setFileType(fileType);
        resource.setFileSize(fileSize);
        resource.setDownloadCount(0);
        resource.setViewCount(0);

        resourceService.save(resource);
        enrichResource(resource);

        return R.ok(resource);
    }

    @PutMapping
    @RequiresRole({"admin", "teacher"})
    public R<Boolean> update(@RequestBody LearningResource resource) {
        Long userId = SecurityUtil.getCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        
        // 教师只能修改自己的资源
        if (roles.contains("teacher") && !roles.contains("admin")) {
            LearningResource existing = resourceService.getById(resource.getId());
            if (existing == null || !existing.getUploadBy().equals(userId)) {
                throw new BusinessException(403, "只能修改自己上传的资源");
            }
        }
        return R.ok(resourceService.updateById(resource));
    }

    @DeleteMapping("/{id}")
    @RequiresRole({"admin", "teacher"})
    public R<Boolean> delete(@PathVariable Long id) {
        LearningResource resource = resourceService.getById(id);
        if (resource == null) {
            throw new BusinessException(404, "资料不存在");
        }

        Long userId = SecurityUtil.getCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();

        // 教师只能删除自己的资源
        if (roles.contains("teacher") && !roles.contains("admin")) {
            if (!resource.getUploadBy().equals(userId)) {
                throw new BusinessException(403, "只能删除自己上传的资源");
            }
        }

        // 1. 删除阿里云 OSS 源文件
        if (resource.getFileUrl() != null && !resource.getFileUrl().isEmpty()) {
            ossService.deleteFile(resource.getFileUrl());
        }

        // 2. 删除数据库记录
        return R.ok(resourceService.removeById(id));
    }

    @GetMapping("/{id}/download")
    @RequiresRole({"admin", "teacher", "student"})
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable Long id) throws IOException {
        LearningResource resource = checkResourcePermission(id);
        incrementDownloadCount(resource);

        InputStream inputStream = openResourceStream(resource);
        HttpHeaders headers = new HttpHeaders();
        String fileName = StringUtils.hasText(resource.getFileName()) ? resource.getFileName() : resource.getTitle();
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build());
        headers.setCacheControl("no-store, no-cache, must-revalidate");
        headers.setPragma("no-cache");
        headers.setExpires(0);

        MediaType mediaType = resolveMediaType(resource);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(mediaType)
                .body(new InputStreamResource(inputStream));
    }

    @PostMapping("/{id}/download")
    @RequiresRole({"admin", "teacher", "student"})
    public R<Boolean> incrementDownload(@PathVariable Long id) {
        LearningResource resource = checkResourcePermission(id);
        incrementDownloadCount(resource);
        return R.ok(true);
    }

    @PostMapping("/{id}/view")
    @RequiresRole({"admin", "teacher", "student"})
    public R<Boolean> incrementView(@PathVariable Long id) {
        LearningResource resource = checkResourcePermission(id);
        incrementViewCount(resource);
        return R.ok(true);
    }

    @GetMapping("/{id}/downloadUrl")
    @RequiresRole({"admin", "teacher", "student"})
    public R<String> getDownloadUrl(@PathVariable Long id) {
        LearningResource resource = checkResourcePermission(id);
        incrementDownloadCount(resource);
        String url = ossService.getSignedUrl(resource.getFileUrl(), true, resource.getFileName());
        return R.ok(url);
    }

    @GetMapping("/{id}/previewUrl")
    @RequiresRole({"admin", "teacher", "student"})
    public R<String> getPreviewUrl(@PathVariable Long id) {
        LearningResource resource = checkResourcePermission(id);
        
        String url = ossService.getSignedUrl(resource.getFileUrl(), false, null);
        return R.ok(url);
    }

    private LearningResource checkResourcePermission(Long id) {
        LearningResource resource = resourceService.getById(id);
        if (resource == null) {
            throw new BusinessException(404, "文件已删除或已过期");
        }

        Long userId = SecurityUtil.getCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();

        if (roles.contains("admin")) {
            return resource;
        }

        if (roles.contains("teacher")) {
            Set<Long> teacherCourseIds = getTeacherCourseIds(userId);
            Set<Long> teacherClassIds = getTeacherClassIds(userId);
            boolean isOwner = Objects.equals(resource.getUploadBy(), userId);
            boolean matchCourse = resource.getCourseId() != null && teacherCourseIds.contains(resource.getCourseId());
            boolean matchClass = resource.getClassId() != null && teacherClassIds.contains(resource.getClassId());
            if (!isOwner && !matchCourse && !matchClass) {
                throw new BusinessException(403, "无权限访问该资料");
            }
            return resource;
        }

        if (roles.contains("student")) {
            User student = userService.getById(userId);
            if (!canStudentAccessResource(student, resource)) {
                throw new BusinessException(403, "无权限访问该资料");
            }
            if (!Objects.equals(resource.getStatus(), 1)) {
                throw new BusinessException(403, "该资料未公开");
            }
            return resource;
        }
        
        throw new BusinessException(403, "无权限访问该资料");
    }

    private void applyTeacherScope(LambdaQueryWrapper<LearningResource> wrapper, Long teacherId) {
        Set<Long> teacherCourseIds = getTeacherCourseIds(teacherId);
        Set<Long> teacherClassIds = getTeacherClassIds(teacherId);

        wrapper.and(w -> {
            w.eq(LearningResource::getUploadBy, teacherId);
            if (!teacherCourseIds.isEmpty()) {
                w.or().in(LearningResource::getCourseId, teacherCourseIds);
            }
            if (!teacherClassIds.isEmpty()) {
                w.or().in(LearningResource::getClassId, teacherClassIds);
            }
        });
    }

    private void applyStudentScope(LambdaQueryWrapper<LearningResource> wrapper, User student) {
        Set<Long> studentCourseIds = getStudentCourseIds(student);
        wrapper.and(w -> {
            w.eq(LearningResource::getClassId, student.getClassId());
            if (!studentCourseIds.isEmpty()) {
                w.or().in(LearningResource::getCourseId, studentCourseIds);
            }
        });
    }

    private boolean canStudentAccessResource(User student, LearningResource resource) {
        if (student == null || student.getClassId() == null || resource == null) {
            return false;
        }
        if (Objects.equals(student.getClassId(), resource.getClassId())) {
            return true;
        }
        if (resource.getCourseId() == null) {
            return false;
        }
        return getStudentCourseIds(student).contains(resource.getCourseId());
    }

    private Set<Long> getStudentCourseIds(User student) {
        if (student == null || student.getClassId() == null) {
            return Set.of();
        }
        return courseService.getByClassId(student.getClassId()).stream()
                .map(Course::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Set<Long> getTeacherCourseIds(Long teacherId) {
        return courseService.getByTeacherId(teacherId).stream()
                .map(Course::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Set<Long> getTeacherClassIds(Long teacherId) {
        return teacherClassMapper.selectClassIdsByTeacherId(teacherId).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private void enrichResources(List<LearningResource> resources) {
        if (resources == null || resources.isEmpty()) {
            return;
        }

        List<Long> courseIds = resources.stream()
                .map(LearningResource::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<Long> classIds = resources.stream()
                .map(LearningResource::getClassId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<Long> uploaderIds = resources.stream()
                .map(LearningResource::getUploadBy)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Course> courseMap = courseIds.isEmpty() ? Map.of() : courseService.listByIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity(), (a, b) -> a));
        Map<Long, Clazz> classMap = classIds.isEmpty() ? Map.of() : clazzService.listByIds(classIds).stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity(), (a, b) -> a));
        Map<Long, User> userMap = uploaderIds.isEmpty() ? Map.of() : userService.listByIds(uploaderIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));

        resources.forEach(resource -> enrichResource(resource, courseMap, classMap, userMap));
    }

    private void enrichResource(LearningResource resource) {
        if (resource == null) {
            return;
        }
        enrichResources(List.of(resource));
    }

    private void enrichResource(LearningResource resource, Map<Long, Course> courseMap, Map<Long, Clazz> classMap, Map<Long, User> userMap) {
        Course course = resource.getCourseId() == null ? null : courseMap.get(resource.getCourseId());
        Clazz clazz = resource.getClassId() == null ? null : classMap.get(resource.getClassId());
        User uploader = resource.getUploadBy() == null ? null : userMap.get(resource.getUploadBy());

        resource.setCourseName(course != null && StringUtils.hasText(course.getCourseName()) ? course.getCourseName() : "未关联课程");
        resource.setClassName(clazz != null && StringUtils.hasText(clazz.getClassName()) ? clazz.getClassName() : "未关联班级");
        resource.setUploaderName(uploader != null && StringUtils.hasText(uploader.getRealName()) ? uploader.getRealName() : "未知");
    }

    private void incrementViewCount(LearningResource resource) {
        if (resource == null) {
            return;
        }
        resource.setViewCount((resource.getViewCount() == null ? 0 : resource.getViewCount()) + 1);
        resourceService.updateById(resource);
    }

    private void incrementDownloadCount(LearningResource resource) {
        if (resource == null) {
            return;
        }
        resource.setDownloadCount((resource.getDownloadCount() == null ? 0 : resource.getDownloadCount()) + 1);
        resourceService.updateById(resource);
    }

    private InputStream openResourceStream(LearningResource resource) throws IOException {
        if (isLocalUploadUrl(resource.getFileUrl())) {
            Path localPath = resolveLocalFilePath(resource.getFileUrl());
            if (!Files.exists(localPath)) {
                throw new BusinessException(404, "文件已删除或已过期");
            }
            return Files.newInputStream(localPath);
        }

        String signedUrl = ossService.getSignedUrl(resource.getFileUrl(), true, resource.getFileName());
        try {
            return new URL(signedUrl).openStream();
        } catch (IOException e) {
            log.error("读取远程资料失败: {}", resource.getFileUrl(), e);
            throw e;
        }
    }

    private Path resolveLocalFilePath(String fileUrl) {
        String marker = "/api/uploads/";
        int index = fileUrl.indexOf(marker);
        if (index < 0) {
            throw new BusinessException(400, "非法的本地文件地址");
        }
        String relativePath = fileUrl.substring(index + marker.length());
        return Paths.get(System.getProperty("user.dir"), "uploads", relativePath).normalize();
    }

    private boolean isLocalUploadUrl(String fileUrl) {
        return StringUtils.hasText(fileUrl) && (fileUrl.startsWith("/api/uploads/") || fileUrl.contains("/api/uploads/"));
    }

    private MediaType resolveMediaType(LearningResource resource) {
        try {
            if (isLocalUploadUrl(resource.getFileUrl())) {
                Path localPath = resolveLocalFilePath(resource.getFileUrl());
                String contentType = Files.probeContentType(localPath);
                if (StringUtils.hasText(contentType)) {
                    return MediaType.parseMediaType(contentType);
                }
            }
        } catch (Exception ignored) {
            // 无法识别时回退为通用二进制流
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private Page<LearningResource> emptyPage(Page<LearningResource> page) {
        page.setRecords(List.of());
        page.setTotal(0);
        page.setPages(0);
        return page;
    }

    private String normalizeText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeFileType(String value) {
        String normalized = normalizeText(value);
        return normalized == null ? null : normalized.toUpperCase();
    }
}
