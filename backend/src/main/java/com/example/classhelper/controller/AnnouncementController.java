package com.example.classhelper.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.classhelper.annotation.RequiresRole;
import com.example.classhelper.common.R;
import com.example.classhelper.entity.Announcement;
import com.example.classhelper.entity.User;
import com.example.classhelper.exception.BusinessException;
import com.example.classhelper.mapper.TeacherClassMapper;
import com.example.classhelper.service.AnnouncementService;
import com.example.classhelper.service.ClazzService;
import com.example.classhelper.service.UserService;
import com.example.classhelper.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@RestController
@RequestMapping("/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final UserService userService;
    private final TeacherClassMapper teacherClassMapper;
    private final ClazzService clazzService;

    @GetMapping("/page")
    @RequiresRole({"admin", "teacher", "student"})
    public R<Page<Announcement>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer isTop,
            @RequestParam(required = false) Integer publishScope,
            @RequestParam(required = false) Long classId) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        List<String> roles = SecurityUtil.getCurrentUserRoles();

        Page<Announcement> page = new Page<>(current, size);
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(title != null && !title.isEmpty(), Announcement::getTitle, title);
        wrapper.eq(type != null, Announcement::getType, type);
        wrapper.eq(status != null, Announcement::getStatus, status);
        wrapper.eq(isTop != null, Announcement::getPriority, isTop);
        if (publishScope != null) {
            wrapper.eq(Announcement::getType, publishScope == 1 ? 2 : 1);
        }
        wrapper.eq(classId != null, Announcement::getClassId, classId);
        applyDataScope(wrapper, userId, roles);
        wrapper.orderByDesc(Announcement::getPriority)
                .orderByDesc(Announcement::getPublishTime)
                .orderByDesc(Announcement::getCreatedAt);
        Page<Announcement> result = announcementService.page(page, wrapper);
        result.getRecords().forEach(this::fillCompatibleFields);
        return R.ok(result);
    }

    @GetMapping("/list")
    @RequiresRole({"admin", "teacher", "student"})
    public R<List<Announcement>> list(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer isTop,
            @RequestParam(required = false) Integer publishScope,
            @RequestParam(required = false) Long classId) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        List<String> roles = SecurityUtil.getCurrentUserRoles();

        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(type != null, Announcement::getType, type);
        wrapper.eq(Announcement::getStatus, status != null ? status : 1);
        wrapper.eq(isTop != null, Announcement::getPriority, isTop);
        if (publishScope != null) {
            wrapper.eq(Announcement::getType, publishScope == 1 ? 2 : 1);
        }
        wrapper.eq(classId != null, Announcement::getClassId, classId);
        applyDataScope(wrapper, userId, roles);
        wrapper.orderByDesc(Announcement::getPriority)
                .orderByDesc(Announcement::getPublishTime)
                .orderByDesc(Announcement::getCreatedAt);
        List<Announcement> records = announcementService.list(wrapper);
        records.forEach(this::fillCompatibleFields);
        return R.ok(records);
    }

    @GetMapping("/{id}")
    @RequiresRole({"admin", "teacher", "student"})
    public R<Announcement> getById(@PathVariable Long id) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "请先登录");
        }
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        Announcement announcement = getAccessibleAnnouncement(id, currentUserId, roles);
        if (announcement == null) {
            return R.ok(null);
        }
        Integer currentViewCount = announcement.getViewCount() == null ? 0 : announcement.getViewCount();
        Announcement updateEntity = new Announcement();
        updateEntity.setId(id);
        updateEntity.setViewCount(currentViewCount + 1);
        announcementService.updateById(updateEntity);
        announcement.setViewCount(currentViewCount + 1);
        fillCompatibleFields(announcement);
        return R.ok(announcement);
    }

    @PostMapping
    @RequiresRole({"admin", "teacher"})
    public R<Boolean> save(@RequestBody Announcement announcement) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "请先登录");
        }
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        normalizeAnnouncementFields(announcement, null);
        validatePublishScope(announcement, currentUserId, roles);

        announcement.setPublisherId(currentUserId);
        if (announcement.getViewCount() == null) {
            announcement.setViewCount(0);
        }
        if (announcement.getStatus() == null) {
            announcement.setStatus(1);
        }

        // 如果状态为已发布，设置发布时间
        if (announcement.getStatus() != null && announcement.getStatus() == 1) {
            announcement.setPublishTime(LocalDateTime.now());
        }
        return R.ok(announcementService.save(announcement));
    }

    @PutMapping
    @RequiresRole({"admin", "teacher"})
    public R<Boolean> update(@RequestBody Announcement announcement) {
        if (announcement.getId() == null) {
            throw new BusinessException(400, "公告ID不能为空");
        }
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "请先登录");
        }
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        Announcement old = announcementService.getById(announcement.getId());
        if (old == null) {
            throw new BusinessException(404, "公告不存在");
        }
        validateManagePermission(old, currentUserId, roles);
        normalizeAnnouncementFields(announcement, old);
        validatePublishScope(announcement, currentUserId, roles);

        // 如果状态变为已发布，设置发布时间
        if (announcement.getStatus() != null && announcement.getStatus() == 1) {
            if (old != null && old.getStatus() != 1) {
                announcement.setPublishTime(LocalDateTime.now());
            }
        }
        return R.ok(announcementService.updateById(announcement));
    }

    @DeleteMapping("/{id}")
    @RequiresRole({"admin", "teacher"})
    public R<Boolean> delete(@PathVariable Long id) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "请先登录");
        }
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        Announcement announcement = announcementService.getById(id);
        if (announcement == null) {
            throw new BusinessException(404, "公告不存在");
        }
        validateManagePermission(announcement, currentUserId, roles);
        return R.ok(announcementService.removeById(id));
    }

    @DeleteMapping("/batch")
    @RequiresRole({"admin", "teacher"})
    public R<Boolean> deleteBatch(@RequestParam List<Long> ids) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "请先登录");
        }
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        List<Announcement> announcements = announcementService.listByIds(ids);
        if (announcements.size() != ids.size()) {
            throw new BusinessException(404, "部分公告不存在");
        }
        announcements.forEach(announcement -> validateManagePermission(announcement, currentUserId, roles));
        return R.ok(announcementService.removeByIds(ids));
    }

    private void applyDataScope(LambdaQueryWrapper<Announcement> wrapper, Long userId, List<String> roles) {
        if (roles.contains("admin")) {
            return;
        }
        if (roles.contains("student")) {
            User user = userService.getById(userId);
            Long studentClassId = user == null ? null : user.getClassId();
            if (studentClassId == null) {
                wrapper.eq(Announcement::getType, 1);
                return;
            }
            wrapper.and(w -> w.eq(Announcement::getType, 1)
                    .or(x -> x.eq(Announcement::getType, 2).eq(Announcement::getClassId, studentClassId)));
            return;
        }
        if (roles.contains("teacher")) {
            List<Long> teacherClassIds = getTeacherAccessibleClassIds(userId).stream().toList();
            if (teacherClassIds.isEmpty()) {
                wrapper.eq(Announcement::getType, 1);
                return;
            }
            wrapper.and(w -> w.eq(Announcement::getType, 1)
                    .or(x -> x.eq(Announcement::getType, 2).in(Announcement::getClassId, teacherClassIds)));
            return;
        }
        wrapper.eq(Announcement::getType, 1);
    }

    private void normalizeAnnouncementFields(Announcement announcement, Announcement existing) {
        Integer normalizedIsTop = announcement.getIsTop();
        if (normalizedIsTop == null && announcement.getPriority() != null) {
            normalizedIsTop = announcement.getPriority() == 1 ? 1 : 0;
        }
        if (normalizedIsTop == null && existing != null) {
            if (existing.getIsTop() != null) {
                normalizedIsTop = existing.getIsTop();
            } else if (existing.getPriority() != null) {
                normalizedIsTop = existing.getPriority() == 1 ? 1 : 0;
            }
        }
        if (normalizedIsTop == null) {
            normalizedIsTop = 0;
        }
        announcement.setPriority(normalizedIsTop);
        announcement.setIsTop(normalizedIsTop);

        Integer normalizedPublishScope = announcement.getPublishScope();
        if (normalizedPublishScope == null && announcement.getType() != null) {
            normalizedPublishScope = announcement.getType() == 2 ? 1 : 0;
        }
        if (normalizedPublishScope == null && announcement.getClassId() != null) {
            normalizedPublishScope = 1;
        }
        if (normalizedPublishScope == null && existing != null) {
            if (existing.getPublishScope() != null) {
                normalizedPublishScope = existing.getPublishScope();
            } else if (existing.getType() != null) {
                normalizedPublishScope = existing.getType() == 2 ? 1 : 0;
            } else if (existing.getClassId() != null) {
                normalizedPublishScope = 1;
            }
        }
        if (normalizedPublishScope == null) {
            normalizedPublishScope = 0;
        }
        announcement.setType(normalizedPublishScope == 1 ? 2 : 1);
        announcement.setPublishScope(normalizedPublishScope);

        if (normalizedPublishScope == 0) {
            announcement.setClassId(null);
        } else if (announcement.getClassId() == null && existing != null) {
            announcement.setClassId(existing.getClassId());
        }
    }

    private void validatePublishScope(Announcement announcement, Long userId, List<String> roles) {
        if (announcement.getPublishScope() != null && announcement.getPublishScope() == 1
                && announcement.getClassId() == null) {
            throw new BusinessException(400, "班级公告必须指定班级");
        }
        if (!roles.contains("admin") && roles.contains("teacher") && announcement.getClassId() != null) {
            Set<Long> teacherClassIds = getTeacherAccessibleClassIds(userId);
            if (!teacherClassIds.contains(announcement.getClassId())) {
                throw new BusinessException(403, "只能发布到自己管理的班级");
            }
        }
    }

    private Announcement getAccessibleAnnouncement(Long id, Long userId, List<String> roles) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getId, id);
        applyDataScope(wrapper, userId, roles);
        Announcement announcement = announcementService.getOne(wrapper, false);
        if (announcement == null) {
            throw new BusinessException(403, "无权访问该公告");
        }
        return announcement;
    }

    private void validateManagePermission(Announcement announcement, Long userId, List<String> roles) {
        if (roles.contains("admin")) {
            return;
        }
        if (!roles.contains("teacher")) {
            throw new BusinessException(403, "无权限操作该公告");
        }
        if (Objects.equals(announcement.getPublisherId(), userId)) {
            return;
        }
        if (announcement.getPublisherId() == null && announcement.getClassId() != null) {
            Set<Long> teacherClassIds = getTeacherAccessibleClassIds(userId);
            if (teacherClassIds.contains(announcement.getClassId())) {
                return;
            }
        }
        throw new BusinessException(403, "只能操作自己发布的公告");
    }

    private Set<Long> getTeacherAccessibleClassIds(Long teacherId) {
        Set<Long> classIds = new HashSet<>(teacherClassMapper.selectClassIdsByTeacherId(teacherId));
        clazzService.getHeadTeacherClasses(teacherId).stream()
                .map(AnnouncementController::safeClassId)
                .filter(Objects::nonNull)
                .forEach(classIds::add);
        classIds.removeIf(id -> id == null || id <= 0);
        return classIds;
    }

    private static Long safeClassId(com.example.classhelper.entity.Clazz clazz) {
        return clazz == null ? null : clazz.getId();
    }

    private void fillCompatibleFields(Announcement announcement) {
        if (announcement == null) {
            return;
        }
        Integer priority = announcement.getPriority();
        announcement.setIsTop(priority != null && priority == 1 ? 1 : 0);
        Integer type = announcement.getType();
        announcement.setPublishScope(type != null && type == 2 ? 1 : 0);
        fillPublisherName(announcement);
    }

    private void fillPublisherName(Announcement announcement) {
        if (announcement.getPublisherId() == null) {
            announcement.setPublisherName(null);
            return;
        }
        User publisher = userService.getById(announcement.getPublisherId());
        announcement.setPublisherName(publisher == null ? null : publisher.getRealName());
    }

}
