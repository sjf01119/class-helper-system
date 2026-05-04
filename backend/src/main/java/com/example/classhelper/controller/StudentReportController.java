package com.example.classhelper.controller;

import com.example.classhelper.annotation.RequiresRole;
import com.example.classhelper.common.R;
import com.example.classhelper.entity.Assignment;
import com.example.classhelper.entity.AssignmentSubmission;
import com.example.classhelper.entity.Course;
import com.example.classhelper.entity.User;
import com.example.classhelper.exception.BusinessException;
import com.example.classhelper.service.AssignmentService;
import com.example.classhelper.service.AssignmentSubmissionService;
import com.example.classhelper.service.CourseService;
import com.example.classhelper.service.UserManageService;
import com.example.classhelper.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/student/report")
@RequiredArgsConstructor
public class StudentReportController {

    private final AssignmentService assignmentService;
    private final AssignmentSubmissionService submissionService;
    private final UserManageService userManageService;
    private final CourseService courseService;

    @GetMapping
    @RequiresRole("student")
    public R<Map<String, Object>> getReport(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long classId,
            @RequestParam(defaultValue = "week") String period) {
        Long currentStudentId = SecurityUtil.getCurrentUserId();
        if (currentStudentId == null) {
            throw new BusinessException(401, "请先登录");
        }
        User student = userManageService.getById(currentStudentId);
        if (student == null || student.getClassId() == null) {
            throw new BusinessException(400, "当前学生未加入班级");
        }
        Long currentClassId = student.getClassId();

        Map<String, Object> report = new HashMap<>();
        Map<String, Object> stats = new HashMap<>();

        long completedCount = submissionService.lambdaQuery()
                .eq(AssignmentSubmission::getStudentId, currentStudentId)
                .count();

        long totalCount = assignmentService.lambdaQuery()
                .eq(Assignment::getClassId, currentClassId)
                .count();

        int completedRate = totalCount > 0 ? (int) ((completedCount * 100) / totalCount) : 0;
        stats.put("completedRate", completedRate);

        List<AssignmentSubmission> submissions = submissionService.lambdaQuery()
                .eq(AssignmentSubmission::getStudentId, currentStudentId)
                .eq(AssignmentSubmission::getStatus, 1)
                .list();

        int avgScore = 0;
        if (!submissions.isEmpty()) {
            avgScore = (int) submissions.stream()
                    .mapToInt(AssignmentSubmission::getScore)
                    .average()
                    .orElse(0);
        }
        stats.put("avgScore", avgScore);
        stats.put("submittedCount", completedCount);
        stats.put("gradedCount", submissions.size());
        stats.put("passRate", submissions.isEmpty()
                ? 0
                : (int) ((submissions.stream().filter(item -> item.getScore() != null && item.getScore() >= 60).count() * 100) / submissions.size()));

        stats.put("rank", calculateClassRank(currentStudentId, currentClassId, submissions));

        report.put("stats", stats);

        Map<String, Object> scoreTrend = new HashMap<>();
        LocalDate startDate = resolveStartDate(period);
        Map<LocalDate, Double> scoreByDate = submissions.stream()
                .filter(item -> resolveScoreDate(item) != null)
                .filter(item -> startDate == null || !resolveScoreDate(item).isBefore(startDate))
                .collect(Collectors.groupingBy(
                        this::resolveScoreDate,
                        TreeMap::new,
                        Collectors.averagingInt(item -> item.getScore() == null ? 0 : item.getScore())
                ));
        List<String> dates = scoreByDate.keySet().stream()
                .map(LocalDate::toString)
                .collect(Collectors.toList());
        List<Integer> scores = scoreByDate.values().stream()
                .map(value -> (int) Math.round(value))
                .collect(Collectors.toList());
        scoreTrend.put("dates", dates);
        scoreTrend.put("scores", scores);
        report.put("scoreTrend", scoreTrend);

        Map<String, Object> weakPoints = new HashMap<>();
        weakPoints.put("indicators", List.of());
        weakPoints.put("values", List.of());
        report.put("weakPoints", weakPoints);
        report.put("courseStats", buildCourseStats(currentClassId, submissions));

        return R.ok(report);
    }

    private List<Map<String, Object>> buildCourseStats(Long classId, List<AssignmentSubmission> submissions) {
        if (submissions.isEmpty()) {
            return List.of();
        }
        List<Assignment> classAssignments = assignmentService.lambdaQuery()
                .eq(Assignment::getClassId, classId)
                .list();
        if (classAssignments.isEmpty()) {
            return List.of();
        }
        Map<Long, Assignment> assignmentMap = classAssignments.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(Assignment::getId, item -> item, (a, b) -> a));
        Set<Long> courseIds = classAssignments.stream()
                .map(Assignment::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Course> courseMap = courseIds.isEmpty()
                ? Map.of()
                : courseService.listByIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, item -> item, (a, b) -> a));

        Map<Long, List<Integer>> scoreMap = new LinkedHashMap<>();
        for (AssignmentSubmission submission : submissions) {
            Assignment assignment = assignmentMap.get(submission.getAssignmentId());
            if (assignment == null || assignment.getCourseId() == null || submission.getScore() == null) {
                continue;
            }
            scoreMap.computeIfAbsent(assignment.getCourseId(), key -> new ArrayList<>()).add(submission.getScore());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        scoreMap.forEach((courseId, scores) -> {
            if (scores.isEmpty()) {
                return;
            }
            Course course = courseMap.get(courseId);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("courseId", courseId);
            row.put("courseName", course != null && course.getCourseName() != null ? course.getCourseName() : "未命名课程");
            row.put("gradedCount", scores.size());
            row.put("avgScore", String.format(Locale.ROOT, "%.1f", scores.stream().mapToInt(Integer::intValue).average().orElse(0D)));
            row.put("maxScore", scores.stream().mapToInt(Integer::intValue).max().orElse(0));
            row.put("minScore", scores.stream().mapToInt(Integer::intValue).min().orElse(0));
            result.add(row);
        });
        return result;
    }

    private int calculateClassRank(Long currentStudentId, Long classId, List<AssignmentSubmission> currentStudentSubmissions) {
        if (currentStudentSubmissions.isEmpty()) {
            return 0;
        }
        List<User> classmates = userManageService.getStudentsByClassId(classId);
        if (classmates == null || classmates.isEmpty()) {
            return 0;
        }
        Set<Long> studentIds = classmates.stream()
                .map(User::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (studentIds.isEmpty()) {
            return 0;
        }
        Map<Long, Double> avgScoreMap = submissionService.lambdaQuery()
                .in(AssignmentSubmission::getStudentId, studentIds)
                .eq(AssignmentSubmission::getStatus, 1)
                .list()
                .stream()
                .filter(item -> item.getScore() != null)
                .collect(Collectors.groupingBy(
                        AssignmentSubmission::getStudentId,
                        Collectors.averagingInt(AssignmentSubmission::getScore)
                ));
        Double currentAvg = avgScoreMap.get(currentStudentId);
        if (currentAvg == null) {
            return 0;
        }
        List<Double> rankingScores = avgScoreMap.values().stream()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        return rankingScores.indexOf(currentAvg) + 1;
    }

    private LocalDate resolveStartDate(String period) {
        LocalDate today = LocalDate.now();
        if ("month".equalsIgnoreCase(period)) {
            return today.minusDays(29);
        }
        if ("all".equalsIgnoreCase(period)) {
            return null;
        }
        return today.minusDays(6);
    }

    private LocalDate resolveScoreDate(AssignmentSubmission submission) {
        LocalDateTime scoreTime = submission.getGradedAt() != null ? submission.getGradedAt() : submission.getSubmitTime();
        return scoreTime == null ? null : scoreTime.toLocalDate();
    }
}
