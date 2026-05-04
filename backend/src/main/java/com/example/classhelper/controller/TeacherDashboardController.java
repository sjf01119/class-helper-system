package com.example.classhelper.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.classhelper.annotation.RequiresRole;
import com.example.classhelper.common.R;
import com.example.classhelper.entity.*;
import com.example.classhelper.exception.BusinessException;
import com.example.classhelper.mapper.TeacherClassMapper;
import com.example.classhelper.service.*;
import com.example.classhelper.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/teacher/dashboard")
@RequiredArgsConstructor
public class TeacherDashboardController {

    private final ClazzService clazzService;
    private final CourseService courseService;
    private final AssignmentService assignmentService;
    private final AssignmentSubmissionService submissionService;
    private final LearningResourceService resourceService;
    private final AnnouncementService announcementService;
    private final TeacherClassMapper teacherClassMapper;

    @GetMapping("/overview")
    @RequiresRole("teacher")
    public R<Map<String, Object>> getOverview() {
        Long teacherId = getCurrentTeacherId();
        Map<String, Object> overview = new HashMap<>();

        List<Long> classIds = teacherClassMapper.selectClassIdsByTeacherId(teacherId).stream()
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .distinct()
                .collect(Collectors.toList());
        List<Clazz> classes = classIds.isEmpty() ? List.of() : clazzService.lambdaQuery()
                .in(Clazz::getId, classIds)
                .list();
        int studentCount = classes.stream()
                .map(Clazz::getCurrentCount)
                .filter(count -> count != null && count > 0)
                .mapToInt(Integer::intValue)
                .sum();
        long courseCount = courseService.lambdaQuery()
                .eq(Course::getTeacherId, teacherId)
                .count();
        long homeworkCount = assignmentService.lambdaQuery()
                .eq(Assignment::getTeacherId, teacherId)
                .count();
        long resourceCount = resourceService.lambdaQuery()
                .eq(LearningResource::getUploadBy, teacherId)
                .count();
        long classCount = classIds.size();

        List<Long> assignmentIds = assignmentService.lambdaQuery()
                .eq(Assignment::getTeacherId, teacherId)
                .select(Assignment::getId)
                .list()
                .stream()
                .map(Assignment::getId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toList());
        long pendingReviewCount = assignmentIds.isEmpty() ? 0L : submissionService.lambdaQuery()
                .in(AssignmentSubmission::getAssignmentId, assignmentIds)
                .eq(AssignmentSubmission::getStatus, 0)
                .count();

        // 最近公告卡片口径：当前教师发布的班级公告 + 可见的全系统公告
        long globalAnnouncementCount = announcementService.lambdaQuery()
                .eq(Announcement::getStatus, 1)
                .eq(Announcement::getType, 1)
                .count();
        long teacherClassAnnouncementCount = announcementService.lambdaQuery()
                .eq(Announcement::getStatus, 1)
                .eq(Announcement::getType, 2)
                .eq(Announcement::getPublisherId, teacherId)
                .count();
        long announcementCount = globalAnnouncementCount + teacherClassAnnouncementCount;

        overview.put("studentCount", studentCount);
        overview.put("courseCount", courseCount);
        overview.put("homeworkCount", homeworkCount);
        overview.put("materialCount", resourceCount);
        overview.put("classCount", classCount);
        overview.put("pendingReviewCount", pendingReviewCount);
        overview.put("announcementCount", announcementCount);
        return R.ok(overview);
    }

    @GetMapping("/classes")
    @RequiresRole("teacher")
    public R<List<Clazz>> getMyClasses() {
        Long teacherId = getCurrentTeacherId();
        return R.ok(clazzService.getByTeacherId(teacherId));
    }

    @GetMapping("/pending-homework")
    @RequiresRole("teacher")
    public R<List<Map<String, Object>>> getPendingHomework(@RequestParam(defaultValue = "5") Integer limit) {
        Long teacherId = getCurrentTeacherId();
        int safeLimit = Math.max(1, Math.min(limit, 20));

        List<Course> myCourses = courseService.getByTeacherId(teacherId);
        Map<Long, String> courseNameMap = myCourses.stream()
                .filter(course -> course.getId() != null)
                .collect(Collectors.toMap(Course::getId, course -> course.getCourseName() == null ? "未命名课程" : course.getCourseName(), (a, b) -> a));

        List<Assignment> assignments = assignmentService.lambdaQuery()
                .eq(Assignment::getTeacherId, teacherId)
                .orderByDesc(Assignment::getCreatedAt)
                .list();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Assignment assignment : assignments) {
            long pendingCount = submissionService.lambdaQuery()
                    .eq(AssignmentSubmission::getAssignmentId, assignment.getId())
                    .eq(AssignmentSubmission::getStatus, 0)
                    .count();
            if (pendingCount <= 0) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", assignment.getId());
            item.put("title", assignment.getTitle());
            item.put("courseName", courseNameMap.getOrDefault(assignment.getCourseId(), "未知课程"));
            item.put("deadline", assignment.getEndTime());
            item.put("pendingCount", pendingCount);
            result.add(item);
            if (result.size() >= safeLimit) {
                break;
            }
        }
        return R.ok(result);
    }

    @GetMapping("/recent-announcements")
    @RequiresRole("teacher")
    public R<List<Announcement>> getRecentAnnouncements(@RequestParam(defaultValue = "5") Integer limit) {
        Long teacherId = getCurrentTeacherId();
        int safeLimit = Math.max(1, Math.min(limit, 20));
        List<Long> classIds = clazzService.getByTeacherId(teacherId).stream()
                .map(Clazz::getId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, 1);
        if (classIds.isEmpty()) {
            wrapper.eq(Announcement::getType, 1);
        } else {
            wrapper.and(w -> w.eq(Announcement::getType, 1)
                    .or(x -> x.eq(Announcement::getType, 2).in(Announcement::getClassId, classIds)));
        }
        wrapper.orderByDesc(Announcement::getPublishTime)
                .orderByDesc(Announcement::getCreatedAt)
                .last("LIMIT " + safeLimit);
        return R.ok(announcementService.list(wrapper));
    }

    @GetMapping("/my-courses")
    @RequiresRole("teacher")
    public R<List<Course>> getMyCourses() {
        Long teacherId = getCurrentTeacherId();
        return R.ok(courseService.getByTeacherId(teacherId));
    }

    private Long getCurrentTeacherId() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return userId;
    }

}
