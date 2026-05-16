package com.example.classhelper.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.classhelper.annotation.RequiresRole;
import com.example.classhelper.common.R;
import com.example.classhelper.entity.Assignment;
import com.example.classhelper.entity.AssignmentSubmission;
import com.example.classhelper.entity.Clazz;
import com.example.classhelper.entity.User;
import com.example.classhelper.exception.BusinessException;
import com.example.classhelper.mapper.TeacherClassMapper;
import com.example.classhelper.service.AssignmentService;
import com.example.classhelper.service.AssignmentSubmissionService;
import com.example.classhelper.service.ClazzService;
import com.example.classhelper.service.UserManageService;
import com.example.classhelper.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/submission")
@RequiredArgsConstructor
public class AssignmentSubmissionController {

    private final AssignmentSubmissionService submissionService;
    private final AssignmentService assignmentService;
    private final UserManageService userManageService;
    private final ClazzService clazzService;
    private final TeacherClassMapper teacherClassMapper;

    @GetMapping("/page")
    @RequiresRole({"admin", "teacher", "student"})
    public R<Page<AssignmentSubmission>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long assignmentId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Integer status) {
        Long currentUserId = requireCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        Page<AssignmentSubmission> page = new Page<>(current, size);
        LambdaQueryWrapper<AssignmentSubmission> wrapper = new LambdaQueryWrapper<>();
        applySubmissionScope(wrapper, currentUserId, roles, assignmentId, studentId);
        wrapper.eq(status != null, AssignmentSubmission::getStatus, status);
        wrapper.orderByDesc(AssignmentSubmission::getSubmitTime);
        return R.ok(submissionService.page(page, wrapper));
    }

    @GetMapping("/list")
    @RequiresRole({"admin", "teacher", "student"})
    public R<List<AssignmentSubmission>> list(
            @RequestParam(required = false) Long assignmentId,
            @RequestParam(required = false) Long studentId) {
        Long currentUserId = requireCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        LambdaQueryWrapper<AssignmentSubmission> wrapper = new LambdaQueryWrapper<>();
        applySubmissionScope(wrapper, currentUserId, roles, assignmentId, studentId);
        wrapper.orderByDesc(AssignmentSubmission::getSubmitTime);
        return R.ok(submissionService.list(wrapper));
    }

    @GetMapping("/pending")
    @RequiresRole({"admin", "teacher"})
    public R<Page<AssignmentSubmission>> getPending(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long classId) {
        Long currentUserId = requireCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        Page<AssignmentSubmission> page = new Page<>(current, size);
        LambdaQueryWrapper<AssignmentSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssignmentSubmission::getStatus, 0); // 待批阅
        if (roles.contains("teacher") && !roles.contains("admin")) {
            List<Long> teacherAssignmentIds = assignmentService.lambdaQuery()
                    .eq(Assignment::getTeacherId, currentUserId)
                    .eq(classId != null, Assignment::getClassId, classId)
                    .list()
                    .stream()
                    .map(Assignment::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (teacherAssignmentIds.isEmpty()) {
                return R.ok(new Page<>(current, size));
            }
            if (classId != null) {
                Set<Long> teacherClassIds = getTeacherClassIds(currentUserId);
                if (!teacherClassIds.contains(classId)) {
                    throw new BusinessException(403, "无权查看该班级提交记录");
                }
                wrapper.eq(AssignmentSubmission::getClassId, classId);
            }
            wrapper.in(AssignmentSubmission::getAssignmentId, teacherAssignmentIds);
        } else {
            wrapper.eq(classId != null, AssignmentSubmission::getClassId, classId);
        }
        wrapper.orderByAsc(AssignmentSubmission::getSubmitTime);
        return R.ok(submissionService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    @RequiresRole({"admin", "teacher", "student"})
    public R<AssignmentSubmission> getById(@PathVariable Long id) {
        AssignmentSubmission submission = getAccessibleSubmission(id, requireCurrentUserId(), SecurityUtil.getCurrentUserRoles());
        return R.ok(submission);
    }

    @PostMapping
    @RequiresRole("student")
    public R<Boolean> save(@RequestBody AssignmentSubmission submission) {
        Long studentId = requireCurrentUserId();
        User student = userManageService.getById(studentId);
        if (student == null || student.getClassId() == null) {
            throw new BusinessException(400, "当前学生未加入班级");
        }
        if (submission.getAssignmentId() == null) {
            throw new BusinessException(400, "作业不能为空");
        }
        Assignment assignment = assignmentService.getById(submission.getAssignmentId());
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
                .eq(AssignmentSubmission::getAssignmentId, submission.getAssignmentId())
                .eq(AssignmentSubmission::getStudentId, studentId)
                .count() > 0;
        if (exists) {
            throw new BusinessException(400, "您已提交过该作业");
        }
        if (!StringUtils.hasText(submission.getContent()) && !StringUtils.hasText(submission.getAttachmentUrl())) {
            throw new BusinessException(400, "请至少提交文字内容或一个附件");
        }
        submission.setStudentId(studentId);
        submission.setClassId(student.getClassId());
        submission.setSubmitTime(LocalDateTime.now());
        submission.setStatus(0); // 待批阅
        return R.ok(submissionService.save(submission));
    }

    @PutMapping("/{id}/grade")
    @RequiresRole("teacher")
    public R<Boolean> grade(@PathVariable Long id, @RequestBody AssignmentSubmission submission) {
        AssignmentSubmission existing = submissionService.getById(id);
        if (existing == null) {
            throw new BusinessException(404, "作业提交不存在");
        }
        Assignment assignment = assignmentService.getById(existing.getAssignmentId());
        Long teacherId = SecurityUtil.getCurrentUserId();
        if (assignment == null || teacherId == null || !teacherId.equals(assignment.getTeacherId())) {
            throw new BusinessException(403, "只能批改自己发布作业的提交");
        }
        AssignmentSubmission update = new AssignmentSubmission();
        update.setId(id);
        update.setScore(submission.getScore());
        update.setFeedback(submission.getFeedback());
        update.setGradedBy(teacherId);
        update.setGradedAt(LocalDateTime.now());
        update.setStatus(1); // 已批阅
        return R.ok(submissionService.updateById(update));
    }

    @PutMapping("/{id}/reject")
    @RequiresRole("teacher")
    public R<Boolean> reject(@PathVariable Long id, @RequestParam String reason) {
        AssignmentSubmission existing = submissionService.getById(id);
        if (existing == null) {
            throw new BusinessException(404, "作业提交不存在");
        }
        Assignment assignment = assignmentService.getById(existing.getAssignmentId());
        Long teacherId = requireCurrentUserId();
        if (assignment == null || !teacherId.equals(assignment.getTeacherId())) {
            throw new BusinessException(403, "只能退回自己发布作业的提交");
        }
        AssignmentSubmission update = new AssignmentSubmission();
        update.setId(id);
        update.setFeedback(reason);
        update.setStatus(2); // 打回重做
        return R.ok(submissionService.updateById(update));
    }

    @DeleteMapping("/{id}")
    @RequiresRole({"admin", "teacher", "student"})
    public R<Boolean> delete(@PathVariable Long id) {
        getAccessibleSubmission(id, requireCurrentUserId(), SecurityUtil.getCurrentUserRoles());
        return R.ok(submissionService.removeById(id));
    }

    @GetMapping("/my-submission")
    @RequiresRole({"admin", "teacher", "student"})
    public R<AssignmentSubmission> getMySubmission(
            @RequestParam Long assignmentId,
            @RequestParam Long studentId) {
        Long currentUserId = requireCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        Long targetStudentId = roles.contains("student") && !roles.contains("admin") ? currentUserId : studentId;
        if (targetStudentId == null) {
            throw new BusinessException(400, "学生ID不能为空");
        }
        Assignment assignment = assignmentService.getById(assignmentId);
        if (assignment == null) {
            throw new BusinessException(404, "作业不存在");
        }
        assertAssignmentAccessible(assignment, currentUserId, roles, targetStudentId);
        AssignmentSubmission submission = submissionService.lambdaQuery()
                .eq(AssignmentSubmission::getAssignmentId, assignmentId)
                .eq(AssignmentSubmission::getStudentId, targetStudentId)
                .one();
        return R.ok(submission);
    }

    @GetMapping("/teacher/grade-overview")
    @RequiresRole("teacher")
    public R<Map<String, Object>> getTeacherGradeOverview(
            @RequestParam Long classId,
            @RequestParam(required = false) Long assignmentId) {
        Long teacherId = requireCurrentUserId();
        if (classId == null || classId <= 0) {
            throw new BusinessException(400, "班级ID不能为空");
        }

        Set<Long> teacherClassIds = getTeacherClassIds(teacherId);
        if (!teacherClassIds.contains(classId)) {
            throw new BusinessException(403, "无权查看该班级成绩数据");
        }

        List<Assignment> assignments = assignmentService.lambdaQuery()
                .eq(Assignment::getTeacherId, teacherId)
                .eq(Assignment::getClassId, classId)
                .orderByDesc(Assignment::getCreatedAt)
                .list();
        if (assignments.isEmpty()) {
            return R.ok(buildEmptyGradeOverview(classId, assignmentId));
        }

        Map<Long, Assignment> assignmentMap = assignments.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(Assignment::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));

        List<Long> assignmentIds = new ArrayList<>(assignmentMap.keySet());
        if (assignmentId != null) {
            if (!assignmentMap.containsKey(assignmentId)) {
                throw new BusinessException(403, "无权查看该作业成绩数据");
            }
            assignmentIds = List.of(assignmentId);
        }

        List<AssignmentSubmission> submissions = submissionService.lambdaQuery()
                .in(AssignmentSubmission::getAssignmentId, assignmentIds)
                .eq(AssignmentSubmission::getClassId, classId)
                .orderByDesc(AssignmentSubmission::getSubmitTime)
                .orderByDesc(AssignmentSubmission::getId)
                .list();

        String className = resolveClassName(classId);

        Set<Long> studentIds = submissions.stream()
                .map(AssignmentSubmission::getStudentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        Map<Long, User> studentMap = studentIds.isEmpty() ? Map.of() : userManageService.listByIds(studentIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));

        List<Map<String, Object>> records = submissions.stream()
                .map(item -> buildGradeRecord(item, assignmentMap, studentMap, className))
                .collect(Collectors.toList());

        List<Map<String, Object>> groups = buildGradeGroups(records, assignmentMap, assignmentId);
        Map<String, Object> stats = buildGradeStats(submissions);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("classId", classId);
        result.put("className", className);
        result.put("assignmentId", assignmentId);
        result.put("records", records);
        result.put("groups", groups);
        result.put("stats", stats);
        return R.ok(result);
    }

    private void applySubmissionScope(LambdaQueryWrapper<AssignmentSubmission> wrapper,
                                      Long currentUserId,
                                      List<String> roles,
                                      Long assignmentId,
                                      Long studentId) {
        if (roles.contains("admin")) {
            wrapper.eq(assignmentId != null, AssignmentSubmission::getAssignmentId, assignmentId);
            wrapper.eq(studentId != null, AssignmentSubmission::getStudentId, studentId);
            return;
        }
        if (roles.contains("teacher")) {
            List<Long> assignmentIds = assignmentService.lambdaQuery()
                    .eq(Assignment::getTeacherId, currentUserId)
                    .list()
                    .stream()
                    .map(Assignment::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (assignmentIds.isEmpty()) {
                wrapper.eq(AssignmentSubmission::getId, -1L);
                return;
            }
            if (assignmentId != null && !assignmentIds.contains(assignmentId)) {
                throw new BusinessException(403, "无权查看该作业的提交记录");
            }
            wrapper.in(AssignmentSubmission::getAssignmentId, assignmentId != null ? List.of(assignmentId) : assignmentIds);
            wrapper.eq(studentId != null, AssignmentSubmission::getStudentId, studentId);
            return;
        }
        if (roles.contains("student")) {
            wrapper.eq(AssignmentSubmission::getStudentId, currentUserId);
            wrapper.eq(assignmentId != null, AssignmentSubmission::getAssignmentId, assignmentId);
            return;
        }
        throw new BusinessException(403, "无权限访问该资源");
    }

    private AssignmentSubmission getAccessibleSubmission(Long id, Long currentUserId, List<String> roles) {
        AssignmentSubmission submission = submissionService.getById(id);
        if (submission == null) {
            throw new BusinessException(404, "作业提交不存在");
        }
        Assignment assignment = assignmentService.getById(submission.getAssignmentId());
        if (assignment == null) {
            throw new BusinessException(404, "作业不存在");
        }
        assertAssignmentAccessible(assignment, currentUserId, roles, submission.getStudentId());
        return submission;
    }

    private void assertAssignmentAccessible(Assignment assignment, Long currentUserId, List<String> roles, Long targetStudentId) {
        if (roles.contains("admin")) {
            return;
        }
        if (roles.contains("teacher")) {
            if (!Objects.equals(assignment.getTeacherId(), currentUserId)) {
                throw new BusinessException(403, "无权访问该作业提交");
            }
            return;
        }
        if (roles.contains("student")) {
            if (!Objects.equals(currentUserId, targetStudentId)) {
                throw new BusinessException(403, "只能查看自己的作业提交");
            }
            User student = userManageService.getById(currentUserId);
            if (student == null || student.getClassId() == null || !Objects.equals(student.getClassId(), assignment.getClassId())) {
                throw new BusinessException(403, "无权访问该作业提交");
            }
            return;
        }
        throw new BusinessException(403, "无权限访问该资源");
    }

    private Set<Long> getTeacherClassIds(Long teacherId) {
        return teacherClassMapper.selectClassIdsByTeacherId(teacherId).stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Long requireCurrentUserId() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return currentUserId;
    }

    private Map<String, Object> buildEmptyGradeOverview(Long classId, Long assignmentId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("classId", classId);
        result.put("className", resolveClassName(classId));
        result.put("assignmentId", assignmentId);
        result.put("records", List.of());
        result.put("groups", List.of());
        result.put("stats", buildGradeStats(List.of()));
        return result;
    }

    private String resolveClassName(Long classId) {
        if (classId == null) {
            return "暂无数据";
        }
        Clazz clazz = clazzService.getById(classId);
        if (clazz != null && StringUtils.hasText(clazz.getClassName())) {
            return clazz.getClassName();
        }
        return "暂无数据";
    }

    private Map<String, Object> buildGradeRecord(AssignmentSubmission submission,
                                                 Map<Long, Assignment> assignmentMap,
                                                 Map<Long, User> studentMap,
                                                 String className) {
        Assignment assignment = assignmentMap.get(submission.getAssignmentId());
        User student = studentMap.get(submission.getStudentId());
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", submission.getId());
        row.put("assignmentId", submission.getAssignmentId());
        row.put("assignmentTitle", assignment != null ? assignment.getTitle() : "未知作业");
        row.put("studentId", submission.getStudentId());
        row.put("studentName", student != null && StringUtils.hasText(student.getRealName()) ? student.getRealName() : "未知学生");
        row.put("studentNo", student != null && StringUtils.hasText(student.getUsername()) ? student.getUsername() : "暂无数据");
        row.put("classId", submission.getClassId());
        row.put("className", className);
        row.put("submitTime", submission.getSubmitTime());
        row.put("score", submission.getScore());
        row.put("status", submission.getStatus());
        return row;
    }

    private List<Map<String, Object>> buildGradeGroups(List<Map<String, Object>> records,
                                                       Map<Long, Assignment> assignmentMap,
                                                       Long assignmentId) {
        if (records.isEmpty()) {
            return List.of();
        }
        Map<Long, List<Map<String, Object>>> grouped = records.stream()
                .collect(Collectors.groupingBy(
                        item -> (Long) item.get("assignmentId"),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<Map<String, Object>> groups = new ArrayList<>();
        List<Long> orderIds = new ArrayList<>(grouped.keySet());
        orderIds.sort((a, b) -> {
            Assignment left = assignmentMap.get(a);
            Assignment right = assignmentMap.get(b);
            LocalDateTime leftTime = left == null ? null : left.getCreatedAt();
            LocalDateTime rightTime = right == null ? null : right.getCreatedAt();
            return Comparator.nullsLast(LocalDateTime::compareTo).reversed().compare(leftTime, rightTime);
        });
        for (Long id : orderIds) {
            if (assignmentId != null && !Objects.equals(id, assignmentId)) {
                continue;
            }
            Assignment assignment = assignmentMap.get(id);
            Map<String, Object> group = new LinkedHashMap<>();
            group.put("assignmentId", id);
            group.put("assignmentTitle", assignment != null ? assignment.getTitle() : "未知作业");
            group.put("records", grouped.getOrDefault(id, List.of()));
            groups.add(group);
        }
        return groups;
    }

    private Map<String, Object> buildGradeStats(List<AssignmentSubmission> submissions) {
        long submittedCount = submissions.size();
        List<AssignmentSubmission> graded = submissions.stream()
                .filter(item -> Objects.equals(item.getStatus(), 1))
                .collect(Collectors.toList());
        long gradedCount = graded.size();
        double avgScore = graded.stream()
                .filter(item -> item.getScore() != null)
                .mapToInt(AssignmentSubmission::getScore)
                .average()
                .orElse(0D);
        long passCount = graded.stream()
                .filter(item -> item.getScore() != null && item.getScore() >= 60)
                .count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("submittedCount", submittedCount);
        stats.put("gradedCount", gradedCount);
        stats.put("avgScore", String.format(Locale.ROOT, "%.1f", avgScore));
        stats.put("passRate", gradedCount > 0
                ? String.format(Locale.ROOT, "%.1f", passCount * 100.0 / gradedCount)
                : "0.0");
        return stats;
    }

}
