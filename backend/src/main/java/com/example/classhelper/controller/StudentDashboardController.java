package com.example.classhelper.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.classhelper.annotation.RequiresRole;
import com.example.classhelper.common.R;
import com.example.classhelper.entity.*;
import com.example.classhelper.exception.BusinessException;
import com.example.classhelper.service.*;
import com.example.classhelper.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/student/dashboard")
@RequiredArgsConstructor
@RequiresRole("student")
public class StudentDashboardController {

    private final ClazzService clazzService;
    private final CourseService courseService;
    private final AssignmentService assignmentService;
    private final AssignmentSubmissionService submissionService;
    private final AnnouncementService announcementService;
    private final UserManageService userManageService;

    @GetMapping("/overview")
    public R<Map<String, Object>> getOverview(
            @RequestParam(defaultValue = "5") Integer pendingLimit,
            @RequestParam(defaultValue = "5") Integer announcementLimit) {
        Long studentId = getCurrentStudentId();
        User student = userManageService.getById(studentId);
        Long classId = student == null ? null : student.getClassId();
        LocalDateTime now = LocalDateTime.now();

        Clazz myClass = classId == null ? null : clazzService.getDetail(classId);
        List<Course> courses = classId == null ? List.of() : courseService.getByClassId(classId);
        Map<Long, Course> courseMap = courses.stream()
                .filter(course -> course.getId() != null)
                .collect(Collectors.toMap(Course::getId, course -> course, (a, b) -> a));

        List<Assignment> assignments = classId == null ? List.of() : assignmentService.lambdaQuery()
                .eq(Assignment::getClassId, classId)
                .eq(Assignment::getStatus, 1)
                .list();

        List<Long> assignmentIds = assignments.stream()
                .map(Assignment::getId)
                .filter(Objects::nonNull)
                .toList();
        List<AssignmentSubmission> submissions = assignmentIds.isEmpty() ? List.of() : submissionService.lambdaQuery()
                .eq(AssignmentSubmission::getStudentId, studentId)
                .in(AssignmentSubmission::getAssignmentId, assignmentIds)
                .list();
        Map<Long, AssignmentSubmission> submissionMap = buildSubmissionMap(submissions);

        List<Assignment> pendingAssignmentSource = assignments.stream()
                .filter(assignment -> !submissionMap.containsKey(assignment.getId()))
                .filter(assignment -> assignment.getEndTime() == null || !assignment.getEndTime().isBefore(now))
                .sorted(Comparator
                        .comparing(Assignment::getEndTime, Comparator.nullsLast(LocalDateTime::compareTo))
                        .thenComparing(Assignment::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
        List<Map<String, Object>> pendingAssignments = pendingAssignmentSource.stream()
                .map(assignment -> buildPendingAssignmentItem(assignment, courseMap, now))
                .limit(Math.max(1, Math.min(pendingLimit, 20)))
                .collect(Collectors.toList());

        long submittedHomeworkCount = submissionMap.values().stream()
                .filter(submission -> Objects.equals(submission.getStatus(), 1))
                .count();

        LambdaQueryWrapper<Announcement> announcementScope = buildAnnouncementScope(classId);
        long announcementCount = announcementService.count(announcementScope);
        List<Announcement> announcements = announcementService.list(
                buildAnnouncementScope(classId)
                        .orderByDesc(Announcement::getPublishTime)
                        .orderByDesc(Announcement::getCreatedAt)
                        .last("LIMIT " + Math.max(1, Math.min(announcementLimit, 20)))
        );

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("courseCount", courses.size());
        stats.put("pendingHomework", pendingAssignmentSource.size());
        stats.put("submittedHomework", submittedHomeworkCount);
        stats.put("announcementCount", announcementCount);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stats", stats);
        result.put("myClass", myClass);
        result.put("courseList", courses);
        result.put("pendingHomeworkList", pendingAssignments);
        result.put("announcementList", announcements);
        return R.ok(result);
    }

    private Long getCurrentStudentId() {
        Long studentId = SecurityUtil.getCurrentUserId();
        if (studentId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return studentId;
    }

    private Map<Long, AssignmentSubmission> buildSubmissionMap(List<AssignmentSubmission> submissions) {
        Map<Long, AssignmentSubmission> submissionMap = new HashMap<>();
        for (AssignmentSubmission submission : submissions) {
            if (submission.getAssignmentId() == null) {
                continue;
            }
            AssignmentSubmission existing = submissionMap.get(submission.getAssignmentId());
            if (existing == null || isAfter(submission.getSubmitTime(), existing.getSubmitTime())) {
                submissionMap.put(submission.getAssignmentId(), submission);
            }
        }
        return submissionMap;
    }

    private boolean isAfter(LocalDateTime current, LocalDateTime existing) {
        if (current == null) {
            return false;
        }
        if (existing == null) {
            return true;
        }
        return current.isAfter(existing);
    }

    private Map<String, Object> buildPendingAssignmentItem(Assignment assignment, Map<Long, Course> courseMap, LocalDateTime now) {
        Course course = assignment.getCourseId() == null ? null : courseMap.get(assignment.getCourseId());
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", assignment.getId());
        item.put("title", assignment.getTitle());
        item.put("courseId", assignment.getCourseId());
        item.put("courseName", course != null && course.getCourseName() != null ? course.getCourseName() : "未关联课程");
        item.put("deadline", assignment.getEndTime());
        item.put("deadlineStatus", assignment.getEndTime() != null && assignment.getEndTime().isBefore(now) ? "已截止" : "待提交");
        return item;
    }

    private LambdaQueryWrapper<Announcement> buildAnnouncementScope(Long classId) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, 1);
        if (classId == null) {
            wrapper.eq(Announcement::getType, 1);
            return wrapper;
        }
        wrapper.and(w -> w.eq(Announcement::getType, 1)
                .or(x -> x.eq(Announcement::getType, 2).eq(Announcement::getClassId, classId)));
        return wrapper;
    }
}
