package com.example.classhelper.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.classhelper.annotation.RequiresRole;
import com.example.classhelper.common.R;
import com.example.classhelper.entity.Assignment;
import com.example.classhelper.exception.BusinessException;
import com.example.classhelper.service.AssignmentService;
import com.example.classhelper.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/homework")
@RequiredArgsConstructor
public class HomeworkController {

    private final AssignmentService assignmentService;

    @GetMapping("/listByClass/{classId}")
    @RequiresRole({"teacher", "admin"})
    public R<List<Assignment>> listByClass(@PathVariable Long classId) {
        if (classId == null) {
            throw new BusinessException(400, "班级ID不能为空");
        }

        LambdaQueryWrapper<Assignment> wrapper = new LambdaQueryWrapper<>();
        if (SecurityUtil.hasRole("teacher") && !SecurityUtil.hasRole("admin")) {
            Long currentUserId = SecurityUtil.getCurrentUserId();
            if (currentUserId == null) {
                throw new BusinessException(401, "请先登录");
            }
            wrapper.eq(Assignment::getTeacherId, currentUserId);
        }
        wrapper.eq(Assignment::getClassId, classId)
                .orderByDesc(Assignment::getCreatedAt)
                .orderByDesc(Assignment::getId);
        return R.ok(assignmentService.list(wrapper));
    }
}
