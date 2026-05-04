package com.example.classhelper.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.classhelper.annotation.RequiresRole;
import com.example.classhelper.common.R;
import com.example.classhelper.entity.User;
import com.example.classhelper.exception.BusinessException;
import com.example.classhelper.mapper.TeacherClassMapper;
import com.example.classhelper.entity.Question;
import com.example.classhelper.service.QuestionService;
import com.example.classhelper.service.UserManageService;
import com.example.classhelper.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;

@RestController
@RequestMapping("/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final UserManageService userManageService;
    private final TeacherClassMapper teacherClassMapper;

    @GetMapping("/page")
    @RequiresRole({"admin", "teacher", "student"})
    public R<Page<Question>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Integer status) {
        Long currentUserId = requireCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        Page<Question> page = new Page<>(current, size);
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        applyQuestionScope(wrapper, currentUserId, roles, classId, studentId);
        wrapper.eq(status != null, Question::getStatus, status);
        wrapper.orderByDesc(Question::getCreatedAt);
        return R.ok(questionService.page(page, wrapper));
    }

    @GetMapping("/list")
    @RequiresRole({"admin", "teacher", "student"})
    public R<List<Question>> list(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Integer status) {
        Long currentUserId = requireCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        applyQuestionScope(wrapper, currentUserId, roles, classId, null);
        wrapper.eq(status != null, Question::getStatus, status);
        wrapper.orderByDesc(Question::getCreatedAt);
        return R.ok(questionService.list(wrapper));
    }

    @GetMapping("/{id}")
    @RequiresRole({"admin", "teacher", "student"})
    public R<Question> getById(@PathVariable Long id) {
        Question question = getAccessibleQuestion(id, requireCurrentUserId(), SecurityUtil.getCurrentUserRoles());
        if (question != null) {
            Question update = new Question();
            update.setId(question.getId());
            update.setViewCount((question.getViewCount() == null ? 0 : question.getViewCount()) + 1);
            questionService.updateById(update);
            question.setViewCount(update.getViewCount());
        }
        return R.ok(question);
    }

    @PostMapping
    @RequiresRole("student")
    public R<Boolean> save(@RequestBody Question question) {
        Long currentUserId = requireCurrentUserId();
        User student = getCurrentStudent(currentUserId);
        question.setStudentId(currentUserId);
        question.setClassId(student.getClassId());
        question.setReplyBy(null);
        question.setReplyTime(null);
        question.setReplyContent(null);
        question.setViewCount(0);
        question.setStatus(0); // 待回复
        return R.ok(questionService.save(question));
    }

    @PutMapping
    @RequiresRole({"admin", "student"})
    public R<Boolean> update(@RequestBody Question question) {
        if (question.getId() == null) {
            throw new BusinessException(400, "问题ID不能为空");
        }
        Long currentUserId = requireCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        Question existing = questionService.getById(question.getId());
        if (existing == null) {
            throw new BusinessException(404, "问题不存在");
        }
        if (!roles.contains("admin") && !Objects.equals(existing.getStudentId(), currentUserId)) {
            throw new BusinessException(403, "只能修改自己的提问");
        }
        Question update = new Question();
        update.setId(existing.getId());
        update.setTitle(question.getTitle());
        update.setContent(question.getContent());
        update.setIsAnonymous(question.getIsAnonymous());
        return R.ok(questionService.updateById(update));
    }

    @DeleteMapping("/{id}")
    @RequiresRole({"admin", "teacher", "student"})
    public R<Boolean> delete(@PathVariable Long id) {
        getAccessibleQuestion(id, requireCurrentUserId(), SecurityUtil.getCurrentUserRoles());
        return R.ok(questionService.removeById(id));
    }

    @PutMapping("/{id}/reply")
    @RequiresRole({"admin", "teacher"})
    public R<Boolean> reply(@PathVariable Long id, @RequestBody Question question) {
        Question existing = getAccessibleQuestion(id, requireCurrentUserId(), SecurityUtil.getCurrentUserRoles());
        Question update = new Question();
        update.setId(existing.getId());
        update.setReplyContent(question.getReplyContent());
        update.setReplyBy(SecurityUtil.getCurrentUserId());
        update.setReplyTime(LocalDateTime.now());
        update.setStatus(1); // 已回复
        return R.ok(questionService.updateById(update));
    }

    @PutMapping("/{id}/close")
    @RequiresRole({"admin", "teacher"})
    public R<Boolean> close(@PathVariable Long id) {
        Question existing = getAccessibleQuestion(id, requireCurrentUserId(), SecurityUtil.getCurrentUserRoles());
        Question update = new Question();
        update.setId(existing.getId());
        update.setStatus(2); // 已关闭
        return R.ok(questionService.updateById(update));
    }

    @GetMapping("/my-list")
    @RequiresRole("student")
    public R<Page<Question>> getMyQuestions(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam Long studentId,
            @RequestParam(required = false) Integer status) {
        Long currentUserId = requireCurrentUserId();
        Page<Question> page = new Page<>(current, size);
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getStudentId, currentUserId);
        wrapper.eq(status != null, Question::getStatus, status);
        wrapper.orderByDesc(Question::getCreatedAt);
        return R.ok(questionService.page(page, wrapper));
    }

    private void applyQuestionScope(LambdaQueryWrapper<Question> wrapper,
                                    Long currentUserId,
                                    List<String> roles,
                                    Long classId,
                                    Long studentId) {
        if (roles.contains("admin")) {
            wrapper.eq(classId != null, Question::getClassId, classId);
            wrapper.eq(studentId != null, Question::getStudentId, studentId);
            return;
        }
        if (roles.contains("teacher")) {
            Set<Long> teacherClassIds = teacherClassMapper.selectClassIdsByTeacherId(currentUserId).stream()
                    .filter(id -> id != null && id > 0)
                    .collect(Collectors.toSet());
            if (teacherClassIds.isEmpty()) {
                wrapper.eq(Question::getId, -1L);
                return;
            }
            if (classId != null) {
                if (!teacherClassIds.contains(classId)) {
                    throw new BusinessException(403, "无权查看该班级提问");
                }
                wrapper.eq(Question::getClassId, classId);
            } else {
                wrapper.in(Question::getClassId, teacherClassIds);
            }
            wrapper.eq(studentId != null, Question::getStudentId, studentId);
            return;
        }
        if (roles.contains("student")) {
            wrapper.eq(Question::getStudentId, currentUserId);
            return;
        }
        throw new BusinessException(403, "无权限访问该资源");
    }

    private Question getAccessibleQuestion(Long id, Long currentUserId, List<String> roles) {
        Question question = questionService.getById(id);
        if (question == null) {
            throw new BusinessException(404, "问题不存在");
        }
        if (roles.contains("admin")) {
            return question;
        }
        if (roles.contains("teacher")) {
            Set<Long> teacherClassIds = teacherClassMapper.selectClassIdsByTeacherId(currentUserId).stream()
                    .filter(classId -> classId != null && classId > 0)
                    .collect(Collectors.toSet());
            if (!teacherClassIds.contains(question.getClassId())) {
                throw new BusinessException(403, "无权访问该问题");
            }
            return question;
        }
        if (roles.contains("student")) {
            if (!Objects.equals(question.getStudentId(), currentUserId)) {
                throw new BusinessException(403, "只能访问自己的提问");
            }
            return question;
        }
        throw new BusinessException(403, "无权限访问该资源");
    }

    private User getCurrentStudent(Long currentUserId) {
        User student = userManageService.getById(currentUserId);
        if (student == null || student.getClassId() == null) {
            throw new BusinessException(400, "当前学生未加入班级");
        }
        return student;
    }

    private Long requireCurrentUserId() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return currentUserId;
    }

}
