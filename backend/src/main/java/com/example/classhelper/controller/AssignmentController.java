package com.example.classhelper.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.classhelper.annotation.RequiresRole;
import com.example.classhelper.common.R;
import com.example.classhelper.dto.AssignmentManageDTO;
import com.example.classhelper.dto.AssignmentSubmitDTO;
import com.example.classhelper.entity.Assignment;
import com.example.classhelper.entity.AssignmentSubmission;
import com.example.classhelper.entity.Clazz;
import com.example.classhelper.entity.Course;
import com.example.classhelper.entity.User;
import com.example.classhelper.exception.BusinessException;
import com.example.classhelper.service.AssignmentService;
import com.example.classhelper.service.AssignmentSubmissionService;
import com.example.classhelper.service.ClazzService;
import com.example.classhelper.service.CourseService;
import com.example.classhelper.service.OssService;
import com.example.classhelper.service.UserManageService;
import com.example.classhelper.utils.SecurityUtil;
import com.example.classhelper.vo.AssignmentAttachmentVO;
import com.example.classhelper.vo.AssignmentManageVO;
import com.example.classhelper.vo.AssignmentSubmissionVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/assignment")
@RequiredArgsConstructor
public class AssignmentController {

    private static final long MAX_ATTACHMENT_SIZE = 100L * 1024 * 1024;
    private static final int MAX_ATTACHMENT_COUNT = 10;
    private static final Set<String> ALLOWED_ATTACHMENT_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "jpg", "jpeg", "png", "gif", "webp"
    );

    private final AssignmentService assignmentService;
    private final AssignmentSubmissionService submissionService;
    private final UserManageService userManageService;
    private final CourseService courseService;
    private final ClazzService clazzService;
    private final OssService ossService;
    private final ObjectMapper objectMapper;

    @GetMapping("/page")
    @RequiresRole({"teacher", "admin"})
    public R<Page<AssignmentManageVO>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Integer status) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "请先登录");
        }

        String normalizedTitle = StringUtils.hasText(title) ? title.trim() : null;
        Page<Assignment> page = new Page<>(current, size);
        LambdaQueryWrapper<Assignment> wrapper = new LambdaQueryWrapper<>();
        if (SecurityUtil.hasRole("teacher") && !SecurityUtil.hasRole("admin")) {
            wrapper.eq(Assignment::getTeacherId, currentUserId);
        }
        wrapper.like(StringUtils.hasText(normalizedTitle), Assignment::getTitle, normalizedTitle);
        wrapper.eq(courseId != null, Assignment::getCourseId, courseId);
        applyStatusFilter(wrapper, status, LocalDateTime.now());
        wrapper.orderByDesc(Assignment::getCreatedAt).orderByDesc(Assignment::getId);

        Page<Assignment> assignmentPage = assignmentService.page(page, wrapper);
        Page<AssignmentManageVO> result = new Page<>(assignmentPage.getCurrent(), assignmentPage.getSize(), assignmentPage.getTotal());
        result.setPages(assignmentPage.getPages());
        result.setRecords(buildAssignmentPageRecords(assignmentPage.getRecords()));
        return R.ok(result);
    }

    @GetMapping("/list")
    @RequiresRole({"teacher", "admin"})
    public R<List<Assignment>> list(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Integer type) {
        LambdaQueryWrapper<Assignment> wrapper = new LambdaQueryWrapper<>();
        if (SecurityUtil.hasRole("teacher") && !SecurityUtil.hasRole("admin")) {
            wrapper.eq(Assignment::getTeacherId, requireCurrentUserId());
        }
        wrapper.eq(classId != null, Assignment::getClassId, classId);
        wrapper.eq(type != null, Assignment::getType, type);
        wrapper.orderByDesc(Assignment::getCreatedAt);
        return R.ok(assignmentService.list(wrapper));
    }

    @GetMapping("/{id}")
    @RequiresRole({"teacher", "admin"})
    public R<AssignmentManageVO> getById(@PathVariable Long id) {
        Assignment assignment = assignmentService.getById(id);
        if (assignment == null) {
            throw new BusinessException(404, "作业不存在");
        }
        verifyAssignmentPermission(assignment);
        return R.ok(buildAssignmentPageRecords(List.of(assignment)).stream().findFirst().orElseThrow());
    }

    @PostMapping
    @RequiresRole("teacher")
    public R<Boolean> save(@Valid @RequestBody AssignmentManageDTO dto) {
        Long teacherId = getCurrentTeacherId();
        Course course = getTeacherOwnedCourse(dto.getCourseId(), teacherId);
        LocalDateTime now = LocalDateTime.now();
        validateManageForm(dto, now);
        List<AssignmentAttachmentVO> attachmentList = sanitizeAttachments(dto.getAttachmentList());
        Assignment assignment = new Assignment();
        assignment.setTitle(dto.getTitle().trim());
        assignment.setDescription(dto.getContent().trim());
        assignment.setCourseId(course.getId());
        assignment.setClassId(course.getClassId());
        assignment.setTeacherId(teacherId);
        assignment.setTotalScore(dto.getTotalScore());
        assignment.setEndTime(dto.getDeadline());
        assignment.setStartTime(now);
        assignment.setStatus(1);
        assignment.setFileUrl(serializeAttachments(attachmentList));
        assignment.setAttachmentUrl(getLegacyAttachmentUrl(attachmentList));
        return R.ok(assignmentService.save(assignment));
    }

    @PutMapping
    @RequiresRole("teacher")
    public R<Boolean> update(@Valid @RequestBody AssignmentManageDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(400, "作业ID不能为空");
        }
        Long teacherId = getCurrentTeacherId();
        Assignment existing = getTeacherOwnedAssignment(dto.getId(), teacherId);
        Course course = getTeacherOwnedCourse(dto.getCourseId(), teacherId);
        validateManageForm(dto, LocalDateTime.now());

        List<AssignmentAttachmentVO> oldAttachments = parseAttachments(existing.getFileUrl(), existing.getAttachmentUrl());
        List<AssignmentAttachmentVO> newAttachments = sanitizeAttachments(dto.getAttachmentList());

        Assignment assignment = new Assignment();
        assignment.setId(existing.getId());
        assignment.setTitle(dto.getTitle().trim());
        assignment.setDescription(dto.getContent().trim());
        assignment.setCourseId(course.getId());
        assignment.setClassId(course.getClassId());
        assignment.setTeacherId(teacherId);
        assignment.setTotalScore(dto.getTotalScore());
        assignment.setEndTime(dto.getDeadline());
        assignment.setStartTime(existing.getStartTime() == null ? LocalDateTime.now() : existing.getStartTime());
        assignment.setStatus(existing.getStatus() == null ? 1 : existing.getStatus());
        assignment.setFileUrl(serializeAttachments(newAttachments));
        assignment.setAttachmentUrl(getLegacyAttachmentUrl(newAttachments));

        boolean updated = assignmentService.updateById(assignment);
        if (updated) {
            deleteRemovedAttachments(oldAttachments, newAttachments);
        }
        return R.ok(updated);
    }

    @DeleteMapping("/{id}")
    @RequiresRole("teacher")
    public R<Boolean> delete(@PathVariable Long id) {
        Long teacherId = getCurrentTeacherId();
        Assignment existing = getTeacherOwnedAssignment(id, teacherId);
        boolean removed = assignmentService.removeById(id);
        if (removed) {
            deleteRemovedAttachments(parseAttachments(existing.getFileUrl(), existing.getAttachmentUrl()), List.of());
        }
        return R.ok(removed);
    }

    @PostMapping("/upload")
    @RequiresRole({"teacher", "student"})
    public R<List<AssignmentAttachmentVO>> uploadAttachments(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new BusinessException(400, "请选择要上传的附件");
        }
        List<AssignmentAttachmentVO> result = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            validateUploadFile(file);
            String fileUrl = ossService.uploadFile(file, "assignments");
            AssignmentAttachmentVO item = new AssignmentAttachmentVO();
            item.setName(file.getOriginalFilename());
            item.setUrl(fileUrl);
            item.setSize(file.getSize());
            result.add(item);
        }
        return R.ok(result);
    }

    @DeleteMapping("/upload")
    @RequiresRole({"teacher", "student"})
    public R<Boolean> deleteUploadedAttachment(@RequestParam String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            throw new BusinessException(400, "文件地址不能为空");
        }
        ossService.deleteFile(fileUrl);
        return R.ok(true);
    }

    @GetMapping("/attachment/access-url")
    @RequiresRole({"admin", "teacher", "student"})
    public R<String> getAttachmentAccessUrl(
            @RequestParam String fileUrl,
            @RequestParam(required = false) String fileName,
            @RequestParam(defaultValue = "false") boolean download) {
        if (!StringUtils.hasText(fileUrl)) {
            throw new BusinessException(400, "文件地址不能为空");
        }
        String normalizedFileUrl = fileUrl.trim();
        assertAttachmentAccessible(normalizedFileUrl);
        if (download && isLocalUploadUrl(normalizedFileUrl)) {
            String localDownloadUrl = UriComponentsBuilder.fromPath("/api/assignment/attachment/download")
                    .queryParam("fileUrl", normalizedFileUrl)
                    .queryParam("fileName", StringUtils.hasText(fileName) ? fileName : extractFileName(normalizedFileUrl))
                    .build()
                    .toUriString();
            return R.ok(localDownloadUrl);
        }
        return R.ok(ossService.getSignedUrl(normalizedFileUrl, download, fileName));
    }

    @GetMapping("/attachment/download")
    @RequiresRole({"admin", "teacher", "student"})
    public ResponseEntity<InputStreamResource> downloadAttachment(
            @RequestParam String fileUrl,
            @RequestParam(required = false) String fileName) throws IOException {
        if (!StringUtils.hasText(fileUrl)) {
            throw new BusinessException(400, "文件地址不能为空");
        }
        String normalizedFileUrl = fileUrl.trim();
        assertAttachmentAccessible(normalizedFileUrl);
        if (!isLocalUploadUrl(normalizedFileUrl)) {
            throw new BusinessException(400, "仅支持下载本地附件");
        }

        Path localPath = resolveLocalFilePath(normalizedFileUrl);
        if (!Files.exists(localPath)) {
            throw new BusinessException(404, "文件已删除或已过期");
        }

        InputStream inputStream = Files.newInputStream(localPath);
        String resolvedFileName = StringUtils.hasText(fileName) ? fileName.trim() : extractFileName(normalizedFileUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(resolvedFileName, StandardCharsets.UTF_8)
                .build());
        headers.setCacheControl("no-store, no-cache, must-revalidate");
        headers.setPragma("no-cache");
        headers.setExpires(0);

        String contentType = Files.probeContentType(localPath);
        MediaType mediaType = StringUtils.hasText(contentType)
                ? MediaType.parseMediaType(contentType)
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(mediaType)
                .body(new InputStreamResource(inputStream));
    }

    @PutMapping("/{id}/publish")
    @RequiresRole("teacher")
    public R<Boolean> publish(@PathVariable Long id) {
        Assignment assignment = getTeacherOwnedAssignment(id, getCurrentTeacherId());
        Assignment update = new Assignment();
        update.setId(assignment.getId());
        update.setStatus(1);
        update.setStartTime(assignment.getStartTime() == null ? LocalDateTime.now() : assignment.getStartTime());
        return R.ok(assignmentService.updateById(update));
    }

    @PutMapping("/{id}/withdraw")
    @RequiresRole("teacher")
    public R<Boolean> withdraw(@PathVariable Long id) {
        Assignment assignment = getTeacherOwnedAssignment(id, getCurrentTeacherId());
        Assignment update = new Assignment();
        update.setId(assignment.getId());
        update.setStatus(2);
        return R.ok(assignmentService.updateById(update));
    }

    @GetMapping("/{id}/submissions")
    @RequiresRole("teacher")
    public R<Page<AssignmentSubmissionVO>> getSubmissions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status) {
        Assignment assignment = getTeacherOwnedAssignment(id, getCurrentTeacherId());

        Page<AssignmentSubmission> page = new Page<>(current, size);
        LambdaQueryWrapper<AssignmentSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssignmentSubmission::getAssignmentId, assignment.getId());
        wrapper.eq(status != null, AssignmentSubmission::getStatus, status);
        wrapper.orderByDesc(AssignmentSubmission::getSubmitTime).orderByDesc(AssignmentSubmission::getId);
        Page<AssignmentSubmission> submissionPage = submissionService.page(page, wrapper);

        Set<Long> studentIds = submissionPage.getRecords().stream()
                .map(AssignmentSubmission::getStudentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        Map<Long, User> studentMap = studentIds.isEmpty() ? Map.of() : userManageService.listByIds(studentIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));

        List<AssignmentSubmissionVO> records = submissionPage.getRecords().stream().map(submission -> {
            AssignmentSubmissionVO vo = new AssignmentSubmissionVO();
            vo.setId(submission.getId());
            vo.setAssignmentId(submission.getAssignmentId());
            vo.setStudentId(submission.getStudentId());
            User student = studentMap.get(submission.getStudentId());
            vo.setStudentName(student != null && StringUtils.hasText(student.getRealName()) ? student.getRealName() : "未知学生");
            vo.setStudentNo(student != null && StringUtils.hasText(student.getUsername()) ? student.getUsername() : "-");
            vo.setContent(submission.getContent());
            vo.setSubmitTime(submission.getSubmitTime());
            vo.setScore(submission.getScore());
            vo.setComment(submission.getFeedback());
            vo.setStatus(submission.getStatus());
            vo.setAttachmentList(parseAttachments(null, submission.getAttachmentUrl()));
            return vo;
        }).collect(Collectors.toList());

        Page<AssignmentSubmissionVO> result = new Page<>(submissionPage.getCurrent(), submissionPage.getSize(), submissionPage.getTotal());
        result.setPages(submissionPage.getPages());
        result.setRecords(records);
        return R.ok(result);
    }

    @GetMapping("/{id}/stats")
    @RequiresRole("teacher")
    public R<Map<String, Object>> getStats(@PathVariable Long id) {
        Assignment assignment = getTeacherOwnedAssignment(id, getCurrentTeacherId());
        Clazz clazz = assignment.getClassId() == null ? null : clazzService.getById(assignment.getClassId());
        int totalStudents = clazz != null && clazz.getCurrentCount() != null ? clazz.getCurrentCount() : 0;
        List<AssignmentSubmission> gradedSubmissions = submissionService.lambdaQuery()
                .eq(AssignmentSubmission::getAssignmentId, assignment.getId())
                .eq(AssignmentSubmission::getStatus, 1)
                .list();
        int submittedCount = Math.toIntExact(submissionService.lambdaQuery()
                .eq(AssignmentSubmission::getAssignmentId, assignment.getId())
                .count());
        int gradedCount = gradedSubmissions.size();
        double avgScore = gradedSubmissions.stream()
                .filter(item -> item.getScore() != null)
                .mapToInt(AssignmentSubmission::getScore)
                .average()
                .orElse(0D);
        long passCount = gradedSubmissions.stream()
                .filter(item -> item.getScore() != null && item.getScore() >= 60)
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", totalStudents);
        stats.put("submittedCount", submittedCount);
        stats.put("gradedCount", gradedCount);
        stats.put("avgScore", String.format(Locale.ROOT, "%.1f", avgScore));
        stats.put("passRate", gradedCount > 0
                ? String.format(Locale.ROOT, "%.1f", passCount * 100.0 / gradedCount)
                : "0.0");
        stats.put("submitRate", totalStudents > 0
                ? String.format(Locale.ROOT, "%.1f%%", submittedCount * 100.0 / totalStudents)
                : "0%");
        return R.ok(stats);
    }

    @GetMapping("/pending")
    @RequiresRole("student")
    public R<List<Assignment>> getPendingAssignments(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long studentId) {
        User currentStudent = getCurrentStudent();
        List<Assignment> assignments = assignmentService.lambdaQuery()
                .eq(Assignment::getClassId, currentStudent.getClassId())
                .eq(Assignment::getStatus, 1)
                .ge(Assignment::getEndTime, LocalDateTime.now())
                .list();

        assignments.removeIf(assignment -> submissionService.lambdaQuery()
                .eq(AssignmentSubmission::getAssignmentId, assignment.getId())
                .eq(AssignmentSubmission::getStudentId, currentStudent.getId())
                .count() > 0);

        return R.ok(assignments);
    }

    @GetMapping("/student/list")
    @RequiresRole("student")
    public R<List<Map<String, Object>>> getStudentHomeworkList(@RequestParam(defaultValue = "0") Integer status) {
        Long studentId = SecurityUtil.getCurrentUserId();
        User user = userManageService.getById(studentId);
        if (user == null || user.getClassId() == null) {
            return R.ok(List.of());
        }
        Long classId = user.getClassId();

        List<Assignment> assignments = assignmentService.lambdaQuery()
                .eq(Assignment::getClassId, classId)
                .eq(Assignment::getStatus, 1)
                .orderByDesc(Assignment::getCreatedAt)
                .list();

        List<Long> courseIds = assignments.stream()
                .map(Assignment::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Course> courseMap = courseIds.isEmpty() ? Map.of() : courseService.listByIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity(), (a, b) -> a));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Assignment assignment : assignments) {
            AssignmentSubmission submission = submissionService.lambdaQuery()
                    .eq(AssignmentSubmission::getAssignmentId, assignment.getId())
                    .eq(AssignmentSubmission::getStudentId, studentId)
                    .one();

            boolean hasSubmitted = submission != null;
            if (status == 0 && hasSubmitted) {
                continue;
            }
            if (status == 1 && !hasSubmitted) {
                continue;
            }

            Course course = assignment.getCourseId() == null ? null : courseMap.get(assignment.getCourseId());
            User teacher = userManageService.getById(assignment.getTeacherId());
            List<AssignmentAttachmentVO> attachmentList = parseAttachments(assignment.getFileUrl(), assignment.getAttachmentUrl());

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", assignment.getId());
            map.put("title", assignment.getTitle());
            map.put("content", assignment.getDescription());
            map.put("courseId", assignment.getCourseId());
            map.put("courseName", course != null && StringUtils.hasText(course.getCourseName()) ? course.getCourseName() : "未关联课程");
            map.put("teacherId", assignment.getTeacherId());
            map.put("teacherName", teacher != null && StringUtils.hasText(teacher.getRealName()) ? teacher.getRealName() : "未知");
            map.put("deadline", assignment.getEndTime());
            map.put("maxScore", assignment.getTotalScore());
            map.put("totalScore", assignment.getTotalScore());
            map.put("attachments", attachmentList.isEmpty() ? null : attachmentList.get(0).getUrl());
            map.put("attachmentList", attachmentList);
            map.put("status", hasSubmitted ? 1 : 0);

            if (hasSubmitted) {
                map.put("submitTime", submission.getSubmitTime());
                map.put("score", submission.getScore());
                map.put("feedback", submission.getFeedback());
                List<AssignmentAttachmentVO> submissionAttachmentList = parseAttachments(null, submission.getAttachmentUrl());
                map.put("submissionAttachments", submissionAttachmentList.isEmpty() ? null : submissionAttachmentList.get(0).getUrl());
                map.put("submissionAttachmentList", submissionAttachmentList);
            }
            result.add(map);
        }

        return R.ok(result);
    }

    @PostMapping("/submit")
    @RequiresRole("student")
    public R<Boolean> submitHomework(@Valid @RequestBody AssignmentSubmitDTO dto) {
        Long studentId = SecurityUtil.getCurrentUserId();
        if (studentId == null) {
            throw new BusinessException(401, "请先登录");
        }

        User student = userManageService.getById(studentId);
        if (student == null || student.getClassId() == null) {
            throw new BusinessException(400, "当前学生未加入班级");
        }

        Assignment assignment = assignmentService.getById(dto.getHomeworkId());
        if (assignment == null) {
            throw new BusinessException(404, "作业不存在");
        }
        if (!Objects.equals(assignment.getClassId(), student.getClassId())) {
            throw new BusinessException(403, "只能提交当前班级的作业");
        }
        if (!Objects.equals(assignment.getStatus(), 1)) {
            throw new BusinessException(400, "该作业暂不可提交");
        }
        if (assignment.getEndTime() != null && assignment.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(400, "作业已截止，无法提交");
        }

        boolean exists = submissionService.lambdaQuery()
                .eq(AssignmentSubmission::getAssignmentId, assignment.getId())
                .eq(AssignmentSubmission::getStudentId, studentId)
                .count() > 0;
        if (exists) {
            throw new BusinessException(400, "您已提交过该作业");
        }

        String normalizedContent = StringUtils.hasText(dto.getContent()) ? dto.getContent().trim() : null;
        List<AssignmentAttachmentVO> submissionAttachments = parseAttachments(dto.getAttachments(), null);
        if (!StringUtils.hasText(normalizedContent) && submissionAttachments.isEmpty()) {
            throw new BusinessException(400, "请至少提交文字内容或上传一个附件");
        }

        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setAssignmentId(assignment.getId());
        submission.setStudentId(studentId);
        submission.setClassId(student.getClassId());
        submission.setContent(normalizedContent);
        submission.setAttachmentUrl(serializeAttachments(submissionAttachments));
        submission.setSubmitTime(LocalDateTime.now());
        submission.setStatus(0);
        return R.ok(submissionService.save(submission));
    }

    private void applyStatusFilter(LambdaQueryWrapper<Assignment> wrapper, Integer status, LocalDateTime now) {
        if (status == null) {
            return;
        }
        if (status == 1) {
            wrapper.ne(Assignment::getStatus, 2);
            wrapper.and(w -> w.isNull(Assignment::getEndTime).or().ge(Assignment::getEndTime, now));
        } else if (status == 2) {
            wrapper.ne(Assignment::getStatus, 2);
            wrapper.lt(Assignment::getEndTime, now);
        } else if (status == 3) {
            wrapper.eq(Assignment::getStatus, 2);
        }
    }

    private List<AssignmentManageVO> buildAssignmentPageRecords(List<Assignment> assignments) {
        if (assignments == null || assignments.isEmpty()) {
            return List.of();
        }

        List<Long> courseIds = assignments.stream()
                .map(Assignment::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<Long> classIds = assignments.stream()
                .map(Assignment::getClassId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Course> courseMap = courseIds.isEmpty() ? Map.of() : courseService.listByIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity(), (a, b) -> a));
        Map<Long, Clazz> clazzMap = classIds.isEmpty() ? Map.of() : clazzService.listByIds(classIds).stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity(), (a, b) -> a));

        List<AssignmentManageVO> records = new ArrayList<>();
        for (Assignment assignment : assignments) {
            Course course = assignment.getCourseId() == null ? null : courseMap.get(assignment.getCourseId());
            Clazz clazz = assignment.getClassId() == null ? null : clazzMap.get(assignment.getClassId());

            AssignmentManageVO vo = new AssignmentManageVO();
            vo.setId(assignment.getId());
            vo.setTitle(assignment.getTitle());
            vo.setContent(assignment.getDescription());
            vo.setCourseId(assignment.getCourseId());
            vo.setCourseName(course != null && StringUtils.hasText(course.getCourseName()) ? course.getCourseName() : "未关联课程");
            vo.setClassId(assignment.getClassId());
            vo.setClassName(clazz != null && StringUtils.hasText(clazz.getClassName()) ? clazz.getClassName() : "未关联班级");
            vo.setDeadline(assignment.getEndTime());
            vo.setTotalScore(assignment.getTotalScore());
            vo.setSubmittedCount(Math.toIntExact(submissionService.lambdaQuery()
                    .eq(AssignmentSubmission::getAssignmentId, assignment.getId())
                    .count()));
            vo.setPendingCount(Math.toIntExact(submissionService.lambdaQuery()
                    .eq(AssignmentSubmission::getAssignmentId, assignment.getId())
                    .eq(AssignmentSubmission::getStatus, 0)
                    .count()));
            vo.setTotalCount(clazz != null && clazz.getCurrentCount() != null ? clazz.getCurrentCount() : 0);
            vo.setRawStatus(assignment.getStatus());
            vo.setStatusText(resolveStatusText(assignment));
            vo.setCreatedAt(assignment.getCreatedAt());
            vo.setAttachmentList(parseAttachments(assignment.getFileUrl(), assignment.getAttachmentUrl()));
            records.add(vo);
        }
        return records;
    }

    private String resolveStatusText(Assignment assignment) {
        if (assignment.getStatus() != null && assignment.getStatus() == 2) {
            return "已关闭";
        }
        if (assignment.getEndTime() != null && assignment.getEndTime().isBefore(LocalDateTime.now())) {
            return "已截止";
        }
        return "已发布";
    }

    private Assignment getTeacherOwnedAssignment(Long assignmentId, Long teacherId) {
        Assignment assignment = assignmentService.getById(assignmentId);
        if (assignment == null) {
            throw new BusinessException(404, "作业不存在");
        }
        if (!Objects.equals(assignment.getTeacherId(), teacherId)) {
            throw new BusinessException(403, "只能操作自己发布的作业");
        }
        return assignment;
    }

    private void verifyAssignmentPermission(Assignment assignment) {
        if (SecurityUtil.hasRole("admin")) {
            return;
        }
        if (SecurityUtil.hasRole("teacher") && Objects.equals(assignment.getTeacherId(), SecurityUtil.getCurrentUserId())) {
            return;
        }
        throw new BusinessException(403, "无权查看该作业");
    }

    private Course getTeacherOwnedCourse(Long courseId, Long teacherId) {
        Course course = courseService.getById(courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }
        if (!Objects.equals(course.getTeacherId(), teacherId)) {
            throw new BusinessException(403, "只能给自己的课程发布作业");
        }
        if (Objects.equals(course.getStatus(), 0)) {
            throw new BusinessException(400, "课程已结课，不能新增或修改作业");
        }
        return course;
    }

    private Long getCurrentTeacherId() {
        return requireCurrentUserId();
    }

    private Long requireCurrentUserId() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return userId;
    }

    private User getCurrentStudent() {
        Long studentId = requireCurrentUserId();
        User student = userManageService.getById(studentId);
        if (student == null || student.getClassId() == null) {
            throw new BusinessException(400, "当前学生未加入班级");
        }
        return student;
    }

    private void assertAttachmentAccessible(String fileUrl) {
        Long currentUserId = requireCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        if (roles.contains("admin")) {
            return;
        }

        List<Assignment> matchedAssignments = assignmentService.lambdaQuery()
                .and(wrapper -> wrapper.eq(Assignment::getAttachmentUrl, fileUrl)
                        .or()
                        .like(Assignment::getFileUrl, fileUrl))
                .list();

        List<AssignmentSubmission> matchedSubmissions = submissionService.lambdaQuery()
                .and(wrapper -> wrapper.eq(AssignmentSubmission::getAttachmentUrl, fileUrl)
                        .or()
                        .like(AssignmentSubmission::getAttachmentUrl, fileUrl))
                .list();

        if (roles.contains("teacher")) {
            boolean canAccessAssignmentAttachment = matchedAssignments.stream()
                    .anyMatch(item -> Objects.equals(item.getTeacherId(), currentUserId));
            if (canAccessAssignmentAttachment) {
                return;
            }

            Set<Long> assignmentIds = matchedSubmissions.stream()
                    .map(AssignmentSubmission::getAssignmentId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Map<Long, Assignment> assignmentMap = assignmentIds.isEmpty() ? Map.of() : assignmentService.listByIds(assignmentIds).stream()
                    .collect(Collectors.toMap(Assignment::getId, Function.identity(), (a, b) -> a));
            boolean canAccessSubmissionAttachment = matchedSubmissions.stream()
                    .anyMatch(item -> {
                        Assignment assignment = assignmentMap.get(item.getAssignmentId());
                        return assignment != null && Objects.equals(assignment.getTeacherId(), currentUserId);
                    });
            if (canAccessSubmissionAttachment) {
                return;
            }
        }

        if (roles.contains("student")) {
            User student = getCurrentStudent();
            boolean canAccessAssignmentAttachment = matchedAssignments.stream()
                    .anyMatch(item -> Objects.equals(item.getClassId(), student.getClassId()) && Objects.equals(item.getStatus(), 1));
            if (canAccessAssignmentAttachment) {
                return;
            }
            boolean canAccessSubmissionAttachment = matchedSubmissions.stream()
                    .anyMatch(item -> Objects.equals(item.getStudentId(), student.getId()));
            if (canAccessSubmissionAttachment) {
                return;
            }
        }

        throw new BusinessException(403, "无权限访问该附件");
    }

    private void validateManageForm(AssignmentManageDTO dto, LocalDateTime now) {
        if (dto.getDeadline() != null && !dto.getDeadline().isAfter(now)) {
            throw new BusinessException(400, "截止时间必须晚于当前时间");
        }
        sanitizeAttachments(dto.getAttachmentList());
    }

    private void validateUploadFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename) || !originalFilename.contains(".")) {
            throw new BusinessException(400, "文件名不合法");
        }
        if (file.getSize() > MAX_ATTACHMENT_SIZE) {
            throw new BusinessException(400, "单个附件不能超过100MB");
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_ATTACHMENT_EXTENSIONS.contains(extension)) {
            throw new BusinessException(400, "仅支持 Word、PDF、PPT、Excel、图片格式附件");
        }
    }

    private void deleteRemovedAttachments(List<AssignmentAttachmentVO> oldAttachments, List<AssignmentAttachmentVO> newAttachments) {
        Set<String> newUrls = newAttachments.stream()
                .map(AssignmentAttachmentVO::getUrl)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        for (AssignmentAttachmentVO attachment : oldAttachments) {
            if (attachment == null || !StringUtils.hasText(attachment.getUrl()) || newUrls.contains(attachment.getUrl())) {
                continue;
            }
            try {
                ossService.deleteFile(attachment.getUrl());
            } catch (Exception ignored) {
                // 删除作业时尽量清理孤儿文件，不因存储删除异常中断主流程。
            }
        }
    }

    private String serializeAttachments(List<AssignmentAttachmentVO> attachments) {
        List<AssignmentAttachmentVO> normalized = sanitizeAttachments(attachments);
        if (normalized.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(normalized);
        } catch (IOException e) {
            throw new BusinessException(500, "附件信息序列化失败", e);
        }
    }

    private String getLegacyAttachmentUrl(List<AssignmentAttachmentVO> attachments) {
        return sanitizeAttachments(attachments).stream()
                .map(AssignmentAttachmentVO::getUrl)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }

    private List<AssignmentAttachmentVO> sanitizeAttachments(List<AssignmentAttachmentVO> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return List.of();
        }
        List<AssignmentAttachmentVO> normalized = attachments.stream()
                .filter(Objects::nonNull)
                .filter(item -> StringUtils.hasText(item.getUrl()))
                .map(item -> {
                    AssignmentAttachmentVO copy = new AssignmentAttachmentVO();
                    copy.setUrl(item.getUrl().trim());
                    copy.setName(StringUtils.hasText(item.getName()) ? item.getName().trim() : extractFileName(item.getUrl()));
                    copy.setSize(item.getSize());
                    return copy;
                })
                .collect(Collectors.toList());
        if (normalized.size() > MAX_ATTACHMENT_COUNT) {
            throw new BusinessException(400, "最多上传10个附件");
        }
        for (AssignmentAttachmentVO item : normalized) {
            validateAttachmentItem(item);
        }
        return normalized;
    }

    private List<AssignmentAttachmentVO> parseAttachments(String fileUrl, String legacyAttachmentUrl) {
        String raw = StringUtils.hasText(fileUrl) ? fileUrl.trim() : legacyAttachmentUrl;
        if (!StringUtils.hasText(raw)) {
            return List.of();
        }
        try {
            if (raw.startsWith("[")) {
                List<AssignmentAttachmentVO> parsed = objectMapper.readValue(raw, new TypeReference<List<AssignmentAttachmentVO>>() {
                });
                return sanitizeAttachments(parsed);
            }
            if (raw.startsWith("{")) {
                AssignmentAttachmentVO parsed = objectMapper.readValue(raw, AssignmentAttachmentVO.class);
                return sanitizeAttachments(List.of(parsed));
            }
        } catch (IOException ignored) {
            // 兼容历史单链接数据。
        }
        AssignmentAttachmentVO attachment = new AssignmentAttachmentVO();
        attachment.setUrl(raw);
        attachment.setName(extractFileName(raw));
        return List.of(attachment);
    }

    private String extractFileName(String url) {
        if (!StringUtils.hasText(url)) {
            return "附件";
        }
        try {
            String path = URI.create(url).getPath();
            if (StringUtils.hasText(path) && path.contains("/")) {
                return path.substring(path.lastIndexOf('/') + 1);
            }
        } catch (Exception ignored) {
            // ignore
        }
        int lastSlash = url.lastIndexOf('/');
        return lastSlash >= 0 ? url.substring(lastSlash + 1) : url;
    }

    private void validateAttachmentItem(AssignmentAttachmentVO attachment) {
        String fileName = StringUtils.hasText(attachment.getName()) ? attachment.getName().trim() : extractFileName(attachment.getUrl());
        if (!fileName.contains(".")) {
            throw new BusinessException(400, "附件文件名不合法");
        }
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_ATTACHMENT_EXTENSIONS.contains(extension)) {
            throw new BusinessException(400, "仅支持 Word、PDF、PPT、Excel、图片格式附件");
        }
        if (attachment.getSize() != null && attachment.getSize() > MAX_ATTACHMENT_SIZE) {
            throw new BusinessException(400, "单个附件不能超过100MB");
        }
    }

    private boolean isLocalUploadUrl(String fileUrl) {
        return StringUtils.hasText(fileUrl) && (fileUrl.startsWith("/api/uploads/") || fileUrl.contains("/api/uploads/"));
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
}
