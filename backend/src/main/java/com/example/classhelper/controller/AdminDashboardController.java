package com.example.classhelper.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.classhelper.annotation.RequiresRole;
import com.example.classhelper.common.R;
import com.example.classhelper.entity.Announcement;
import com.example.classhelper.entity.Clazz;
import com.example.classhelper.entity.LoginLog;
import com.example.classhelper.mapper.AnnouncementMapper;
import com.example.classhelper.mapper.ClazzMapper;
import com.example.classhelper.mapper.UserMapper;
import com.example.classhelper.service.LoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@RequiresRole("admin")
public class AdminDashboardController {

    private final UserMapper userMapper;
    private final ClazzMapper clazzMapper;
    private final AnnouncementMapper announcementMapper;
    private final LoginLogService loginLogService;

    @GetMapping("/overview")
    public R<Map<String, Long>> getOverview() {
        Map<String, Long> result = new HashMap<>();
        result.put("teacherCount", safeCount(userMapper.countByRoleCode("teacher")));
        result.put("studentCount", safeCount(userMapper.countByRoleCode("student")));
        result.put("classCount", clazzMapper.selectCount(
                new LambdaQueryWrapper<Clazz>().eq(Clazz::getStatus, 1)
        ));
        result.put("announcementCount", announcementMapper.selectCount(
                new LambdaQueryWrapper<Announcement>().eq(Announcement::getStatus, 1)
        ));
        return R.ok(result);
    }

    @GetMapping("/user-growth")
    public R<List<Map<String, Object>>> getUserGrowth() {
        List<Map<String, Object>> dbRows = userMapper.countUserGrowthByMonth();
        Map<String, Long> monthCountMap = new HashMap<>();
        for (Map<String, Object> row : dbRows) {
            String month = String.valueOf(row.get("month"));
            long value = ((Number) row.get("value")).longValue();
            monthCountMap.put(month, value);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        YearMonth currentMonth = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            String month = currentMonth.minusMonths(i).format(formatter);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", month);
            item.put("value", monthCountMap.getOrDefault(month, 0L));
            result.add(item);
        }
        return R.ok(result);
    }

    @GetMapping("/class-distribution")
    public R<List<Map<String, Object>>> getClassDistribution() {
        List<Map<String, Object>> dbRows = userMapper.countStudentDistributionByClass();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> row : dbRows) {
            String name = String.valueOf(row.get("name"));
            long studentCount = ((Number) row.get("studentCount")).longValue();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", name);
            item.put("value", studentCount);
            result.add(item);
        }

        return R.ok(result);
    }

    @GetMapping("/latest-announcements")
    public R<List<Announcement>> getLatestAnnouncements(@RequestParam(defaultValue = "2") Integer limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, 1)
                .orderByDesc(Announcement::getPublishTime)
                .orderByDesc(Announcement::getCreatedAt)
                .last("LIMIT " + safeLimit);
        return R.ok(announcementMapper.selectList(wrapper));
    }

    @GetMapping("/recent-login-logs")
    public R<List<LoginLog>> getRecentLoginLogs(@RequestParam(defaultValue = "5") Integer limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(LoginLog::getCreatedAt)
                .last("LIMIT " + safeLimit);
        return R.ok(loginLogService.list(wrapper));
    }

    private long safeCount(Long count) {
        return count == null ? 0L : count;
    }
}
