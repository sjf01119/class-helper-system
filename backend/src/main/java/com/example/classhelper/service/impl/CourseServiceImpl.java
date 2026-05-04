package com.example.classhelper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.classhelper.dto.CourseDTO;
import com.example.classhelper.dto.CourseQueryDTO;
import com.example.classhelper.entity.Clazz;
import com.example.classhelper.entity.Course;
import com.example.classhelper.exception.BusinessException;
import com.example.classhelper.mapper.ClazzMapper;
import com.example.classhelper.mapper.CourseMapper;
import com.example.classhelper.mapper.TeacherClassMapper;
import com.example.classhelper.service.CourseService;
import com.example.classhelper.service.OssService;
import com.example.classhelper.vo.PageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 课程 Service 实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    private final TeacherClassMapper teacherClassMapper;
    private final ClazzMapper clazzMapper;
    private final OssService ossService;

    @Override
    public PageVO<Course> pageList(CourseQueryDTO queryDTO, Long currentUserId, List<String> roles) {
        Page<Course> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        
        // 教师只能看到自己的课程
        if (roles != null && roles.contains("teacher") && !roles.contains("admin")) {
            List<Long> managedClassIds = getTeacherManagedClassIds(currentUserId);
            if (CollectionUtils.isEmpty(managedClassIds)) {
                return new PageVO<>(List.of(), 0L, page.getCurrent(), page.getSize());
            }
            wrapper.eq(Course::getTeacherId, currentUserId);
            wrapper.in(Course::getClassId, managedClassIds);
        }
        
        // 关键字搜索（课程名称）
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.like(Course::getCourseName, queryDTO.getKeyword());
        }
        
        // 按教师筛选
        if (queryDTO.getTeacherId() != null) {
            wrapper.eq(Course::getTeacherId, queryDTO.getTeacherId());
        }
        
        // 按班级筛选
        if (queryDTO.getClassId() != null) {
            wrapper.eq(Course::getClassId, queryDTO.getClassId());
        }
        
        // 按状态筛选
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Course::getStatus, queryDTO.getStatus());
        }
        
        // 未删除
        wrapper.eq(Course::getIsDeleted, 0);
        
        // 按创建时间倒序
        wrapper.orderByDesc(Course::getCreatedAt);
        
        Page<Course> resultPage = this.page(page, wrapper);
        resultPage.getRecords().forEach(this::normalizeCourseCover);
        
        return new PageVO<>(resultPage.getRecords(), resultPage.getTotal(),
                           resultPage.getCurrent(), resultPage.getSize());
    }

    @Override
    public boolean add(CourseDTO dto, Long currentUserId, List<String> roles) {
        validateCoursePayload(dto);
        Course course = new Course();
        if (roles != null && roles.contains("teacher") && !roles.contains("admin")) {
            validateTeacherClassAccess(currentUserId, dto.getClassId());
            course.setTeacherId(currentUserId);
        } else {
            if (dto.getTeacherId() == null) {
                throw new BusinessException("授课教师不能为空");
            }
            course.setTeacherId(dto.getTeacherId());
        }

        applyEditableFields(course, dto, null);
        return this.save(course);
    }

    @Override
    public boolean update(CourseDTO dto, Long currentUserId, List<String> roles) {
        if (dto.getId() == null) {
            throw new BusinessException("课程ID不能为空");
        }
        validateCoursePayload(dto);
        Course course = this.getById(dto.getId());
        if (course == null || course.getIsDeleted() == 1) {
            throw new BusinessException("课程不存在");
        }
        
        // 教师只能修改自己的课程
        if (roles != null && roles.contains("teacher") && !roles.contains("admin")) {
            if (!currentUserId.equals(course.getTeacherId())) {
                throw new BusinessException("无权限修改该课程");
            }
            validateTeacherClassAccess(currentUserId, dto.getClassId());
            course.setTeacherId(currentUserId);
        } else {
            if (dto.getTeacherId() == null) {
                throw new BusinessException("授课教师不能为空");
            }
            course.setTeacherId(dto.getTeacherId());
        }
        String oldCoverUrl = course.getCoverUrl();
        applyEditableFields(course, dto, oldCoverUrl);
        return this.updateById(course);
    }

    @Override
    public boolean remove(Long id, Long currentUserId, List<String> roles) {
        Course course = this.getById(id);
        if (course == null || course.getIsDeleted() == 1) {
            throw new BusinessException("课程不存在");
        }
        
        // 教师只能删除自己的课程
        if (roles != null && roles.contains("teacher") && !roles.contains("admin")) {
            if (!currentUserId.equals(course.getTeacherId())) {
                throw new BusinessException("无权限删除该课程");
            }
        }
        
        return this.removeById(id);
    }

    @Override
    public Course getDetail(Long id, Long currentUserId, List<String> roles) {
        Course course = baseMapper.selectDetailById(id);
        if (course == null || course.getIsDeleted() == 1) {
            throw new BusinessException("课程不存在");
        }
        
        // 教师只能查看自己的课程详情
        if (roles != null && roles.contains("teacher") && !roles.contains("admin")) {
            if (!currentUserId.equals(course.getTeacherId())) {
                throw new BusinessException("无权限查看该课程");
            }
        }

        return normalizeCourseCover(course);
    }

    @Override
    public boolean batchUpdateStatus(List<Long> ids, Integer status, Long currentUserId, List<String> roles) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException("请选择课程");
        }
        if (!Objects.equals(status, 0) && !Objects.equals(status, 1)) {
            throw new BusinessException("课程状态不合法");
        }
        List<Course> courses = loadAccessibleCourses(ids, currentUserId, roles);
        if (courses.isEmpty()) {
            throw new BusinessException("未找到可操作课程");
        }
        LocalDateTime now = LocalDateTime.now();
        List<Course> updates = new ArrayList<>();
        for (Course course : courses) {
            Course update = new Course();
            update.setId(course.getId());
            update.setStatus(status);
            if (status == 0) {
                update.setEndTime(course.getEndTime() == null ? now : course.getEndTime());
                update.setStartTime(course.getStartTime() == null ? now : course.getStartTime());
            } else {
                update.setStartTime(course.getStartTime() == null ? now : course.getStartTime());
                update.setEndTime(null);
            }
            updates.add(update);
        }
        return this.updateBatchById(updates);
    }

    @Override
    public boolean batchRemove(List<Long> ids, Long currentUserId, List<String> roles) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException("请选择课程");
        }
        List<Course> courses = loadAccessibleCourses(ids, currentUserId, roles);
        if (courses.isEmpty()) {
            throw new BusinessException("未找到可操作课程");
        }
        return this.removeByIds(courses.stream().map(Course::getId).toList());
    }

    @Override
    public List<Course> getByTeacherId(Long teacherId) {
        return normalizeCourseCovers(baseMapper.selectByTeacherId(teacherId));
    }

    @Override
    public List<Course> getByClassId(Long classId) {
        return normalizeCourseCovers(baseMapper.selectByClassId(classId));
    }

    private void applyEditableFields(Course course, CourseDTO dto, String oldCoverUrl) {
        course.setCourseName(dto.getCourseName().trim());
        course.setClassId(dto.getClassId());
        course.setCredit(dto.getCredit() == null ? 2 : dto.getCredit());
        course.setCourseHours(dto.getCourseHours() == null ? 32 : dto.getCourseHours());
        course.setDescription(StringUtils.hasText(dto.getDescription()) ? dto.getDescription().trim() : null);
        course.setCoverUrl(normalizeCourseCoverUrl(dto.getCoverUrl()));
        course.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        course.setStartTime(dto.getStartTime());
        course.setEndTime(dto.getEndTime());
        normalizeTimeline(course);

        if (StringUtils.hasText(oldCoverUrl)
                && StringUtils.hasText(course.getCoverUrl())
                && !Objects.equals(oldCoverUrl, course.getCoverUrl())) {
            try {
                ossService.deleteFile(oldCoverUrl);
            } catch (Exception e) {
                log.warn("旧课程封面清理失败: {}", e.getMessage());
            }
        }
    }

    private void validateCoursePayload(CourseDTO dto) {
        if (dto.getStartTime() != null && dto.getEndTime() != null && dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new BusinessException("结课时间不能早于开课时间");
        }
    }

    private void normalizeTimeline(Course course) {
        LocalDateTime now = LocalDateTime.now();
        if (course.getStatus() == null) {
            course.setStatus(1);
        }
        if (course.getStatus() == 1) {
            course.setStartTime(course.getStartTime() == null ? now : course.getStartTime());
            course.setEndTime(null);
            return;
        }
        if (course.getStartTime() == null) {
            course.setStartTime(now);
        }
        if (course.getEndTime() == null) {
            course.setEndTime(now);
        }
    }

    private List<Course> loadAccessibleCourses(List<Long> ids, Long currentUserId, List<String> roles) {
        List<Course> courses = this.listByIds(ids).stream()
                .filter(Objects::nonNull)
                .filter(course -> !Objects.equals(course.getIsDeleted(), 1))
                .toList();
        if (courses.size() != ids.stream().filter(Objects::nonNull).distinct().count()) {
            throw new BusinessException("部分课程不存在");
        }
        if (roles != null && roles.contains("teacher") && !roles.contains("admin")) {
            boolean hasUnauthorized = courses.stream().anyMatch(course -> !Objects.equals(course.getTeacherId(), currentUserId));
            if (hasUnauthorized) {
                throw new BusinessException("只能操作自己的课程");
            }
        }
        return courses;
    }

    private void validateTeacherClassAccess(Long teacherId, Long classId) {
        if (classId == null) {
            throw new BusinessException("所属班级不能为空");
        }
        List<Long> managedClassIds = getTeacherManagedClassIds(teacherId);
        if (CollectionUtils.isEmpty(managedClassIds) || !managedClassIds.contains(classId)) {
            throw new BusinessException("只能为已关联管理的班级创建或维护课程");
        }
    }

    private List<Long> getTeacherManagedClassIds(Long teacherId) {
        return teacherClassMapper.selectClassIdsByTeacherId(teacherId).stream()
                .filter(Objects::nonNull)
                .distinct()
                .filter(this::existsClass)
                .toList();
    }

    private boolean existsClass(Long classId) {
        if (classId == null) {
            return false;
        }
        return clazzMapper.selectCount(new LambdaQueryWrapper<Clazz>()
                .eq(Clazz::getId, classId)
                .eq(Clazz::getIsDeleted, 0)) > 0;
    }

    private List<Course> normalizeCourseCovers(List<Course> courses) {
        if (courses == null || courses.isEmpty()) {
            return List.of();
        }
        courses.forEach(this::normalizeCourseCover);
        return courses;
    }

    private Course normalizeCourseCover(Course course) {
        if (course == null) {
            return null;
        }
        course.setCoverUrl(normalizeCourseCoverUrl(course.getCoverUrl()));
        return course;
    }

    private String normalizeCourseCoverUrl(String coverUrl) {
        if (!StringUtils.hasText(coverUrl)) {
            return null;
        }
        String normalized = coverUrl.trim().replace("\\", "/");
        if (normalized.startsWith("http://")
                || normalized.startsWith("https://")
                || normalized.startsWith("data:")
                || normalized.startsWith("blob:")
                || normalized.startsWith("/api/")) {
            return normalized;
        }
        if (normalized.startsWith("/uploads/")) {
            return "/api" + normalized;
        }
        if (normalized.startsWith("uploads/")) {
            return "/api/" + normalized;
        }
        return normalized;
    }
}
