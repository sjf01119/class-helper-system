package com.example.classhelper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.classhelper.dto.ClazzDTO;
import com.example.classhelper.dto.ClazzQueryDTO;
import com.example.classhelper.dto.HeadTeacherBindDTO;
import com.example.classhelper.entity.Clazz;
import com.example.classhelper.entity.Course;
import com.example.classhelper.entity.Role;
import com.example.classhelper.entity.TeacherClass;
import com.example.classhelper.entity.User;
import com.example.classhelper.entity.UserRole;
import com.example.classhelper.exception.BusinessException;
import com.example.classhelper.mapper.ClazzMapper;
import com.example.classhelper.mapper.TeacherClassMapper;
import com.example.classhelper.service.ClazzService;
import com.example.classhelper.service.CourseService;
import com.example.classhelper.service.RoleService;
import com.example.classhelper.service.UserService;
import com.example.classhelper.service.UserRoleService;
import com.example.classhelper.vo.PageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 班级 Service 实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClazzServiceImpl extends ServiceImpl<ClazzMapper, Clazz> implements ClazzService {

    private final UserService userService;
    private final TeacherClassMapper teacherClassMapper;
    private final CourseService courseService;
    private final RoleService roleService;
    private final UserRoleService userRoleService;

    @Override
    public PageVO<Clazz> pageList(ClazzQueryDTO queryDTO) {
        Page<Clazz> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        
        LambdaQueryWrapper<Clazz> wrapper = new LambdaQueryWrapper<>();
        
        // 关键字搜索（班级名称）
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.like(Clazz::getClassName, queryDTO.getKeyword());
        }
        
        // 按状态筛选
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Clazz::getStatus, queryDTO.getStatus());
        }
        
        // 未删除
        wrapper.eq(Clazz::getIsDeleted, 0);
        
        // 按创建时间倒序
        wrapper.orderByDesc(Clazz::getCreatedAt);
        
        Page<Clazz> resultPage = this.page(page, wrapper);
        prepareClazzList(resultPage.getRecords(), true);

        return new PageVO<>(resultPage.getRecords(), resultPage.getTotal(),
                           resultPage.getCurrent(), resultPage.getSize());
    }

    @Override
    public boolean add(ClazzDTO dto) {
        if (dto.getTeacherId() == null) {
            throw new BusinessException("请选择班主任");
        }

        // 检查班级名称是否已存在
        LambdaQueryWrapper<Clazz> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Clazz::getClassName, dto.getClassName());
        wrapper.eq(Clazz::getIsDeleted, 0);
        if (this.count(wrapper) > 0) {
            throw new BusinessException("班级名称已存在");
        }
        
        Clazz clazz = new Clazz();
        BeanUtils.copyProperties(dto, clazz);

        clazz.setDescription(StringUtils.hasText(clazz.getDescription()) ? clazz.getDescription().trim() : null);
        clazz.setInviteCode(generateInviteCode());
        clazz.setCurrentCount(0);
        clazz.setCreatedAt(LocalDateTime.now());
        clazz.setUpdatedAt(clazz.getCreatedAt());

        if (clazz.getStatus() == null) {
            clazz.setStatus(1);
        }

        return this.save(clazz);
    }

    @Override
    public boolean update(ClazzDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("班级ID不能为空");
        }
        
        Clazz clazz = this.getById(dto.getId());
        if (clazz == null || clazz.getIsDeleted() == 1) {
            throw new BusinessException("班级不存在");
        }
        
        // 检查班级名称是否与其他班级重复
        LambdaQueryWrapper<Clazz> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Clazz::getClassName, dto.getClassName());
        wrapper.ne(Clazz::getId, dto.getId());
        wrapper.eq(Clazz::getIsDeleted, 0);
        if (this.count(wrapper) > 0) {
            throw new BusinessException("班级名称已存在");
        }

        clazz.setClassName(dto.getClassName());
        clazz.setDescription(StringUtils.hasText(dto.getDescription()) ? dto.getDescription().trim() : null);
        if (dto.getTeacherId() != null) {
            clazz.setTeacherId(dto.getTeacherId());
        }
        clazz.setStatus(dto.getStatus());
        clazz.setUpdatedAt(LocalDateTime.now());
        return this.updateById(clazz);
    }

    @Override
    public boolean remove(Long id) {
        Clazz clazz = this.getById(id);
        if (clazz == null || clazz.getIsDeleted() == 1) {
            throw new BusinessException("班级不存在");
        }
        
        // 逻辑删除
        return this.removeById(id);
    }

    @Override
    public Clazz getDetail(Long id) {
        Clazz clazz = this.getById(id);
        if (clazz == null || clazz.getIsDeleted() == 1) {
            throw new BusinessException("班级不存在");
        }
        prepareClazz(clazz, true);
        fillStudentCount(clazz);
        return clazz;
    }

    @Override
    public List<Clazz> getByTeacherId(Long teacherId) {
        if (teacherId == null) {
            return List.of();
        }

        List<Long> classIds = teacherClassMapper.selectClassIdsByTeacherId(teacherId).stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (classIds.isEmpty()) {
            return List.of();
        }

        List<Clazz> clazzList = this.lambdaQuery()
                .in(Clazz::getId, classIds)
                .eq(Clazz::getIsDeleted, 0)
                .orderByDesc(Clazz::getCreatedAt)
                .list();
        prepareClazzList(clazzList, true);
        return clazzList;
    }

    @Override
    public List<Clazz> getHeadTeacherClasses(Long teacherId) {
        if (teacherId == null) {
            return List.of();
        }

        List<Clazz> clazzList = this.lambdaQuery()
                .eq(Clazz::getTeacherId, teacherId)
                .eq(Clazz::getIsDeleted, 0)
                .orderByDesc(Clazz::getCreatedAt)
                .list();
        prepareClazzList(clazzList, true);
        return clazzList;
    }

    @Override
    public boolean isHeadTeacher(Long teacherId) {
        if (teacherId == null) {
            return false;
        }
        return this.lambdaQuery()
                .eq(Clazz::getTeacherId, teacherId)
                .eq(Clazz::getIsDeleted, 0)
                .count() > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindHeadTeacher(HeadTeacherBindDTO dto) {
        if (dto == null || dto.getTeacherId() == null || dto.getClassId() == null) {
            throw new BusinessException("绑定信息不完整");
        }

        User teacher = userService.getById(dto.getTeacherId());
        if (teacher == null || Integer.valueOf(1).equals(teacher.getIsDeleted())) {
            throw new BusinessException("教师不存在");
        }
        if (!isTeacherUser(dto.getTeacherId())) {
            throw new BusinessException("所选用户不是教师");
        }

        Clazz clazz = this.getById(dto.getClassId());
        if (clazz == null || Integer.valueOf(1).equals(clazz.getIsDeleted())) {
            throw new BusinessException("班级不存在");
        }

        if (clazz.getTeacherId() != null && !Objects.equals(clazz.getTeacherId(), dto.getTeacherId())) {
            throw new BusinessException("该班级已绑定其他班主任，请先在班级管理中调整");
        }

        if (!Objects.equals(clazz.getTeacherId(), dto.getTeacherId())) {
            clazz.setTeacherId(dto.getTeacherId());
            clazz.setUpdatedAt(LocalDateTime.now());
            this.updateById(clazz);
        }

        ensureTeacherClassRelation(dto.getTeacherId(), dto.getClassId());
    }

    @Override
    public List<Clazz> getAllAvailableClasses() {
        List<Clazz> clazzList = this.lambdaQuery()
                .eq(Clazz::getIsDeleted, 0)
                .orderByAsc(Clazz::getId)
                .list();
        prepareClazzList(clazzList, false);
        return clazzList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean joinByInviteCode(String inviteCode, Long studentId) {
        if (!StringUtils.hasText(inviteCode) || studentId == null) {
            throw new BusinessException("邀请码或学生ID不能为空");
        }

        // 根据邀请码查询班级
        LambdaQueryWrapper<Clazz> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Clazz::getInviteCode, inviteCode);
        wrapper.eq(Clazz::getIsDeleted, 0);
        Clazz clazz = this.getOne(wrapper);

        if (clazz == null) {
            throw new BusinessException("邀请码无效");
        }

        if (clazz.getStatus() == 0) {
            throw new BusinessException("该班级已被禁用");
        }

        // 更新学生的classId
        var user = userService.getById(studentId);
        if (user == null) {
            throw new BusinessException("学生不存在");
        }
        if (user.getClassId() != null) {
            if (user.getClassId().equals(clazz.getId())) {
                return true;
            }
            throw new BusinessException("您已加入其他班级，不能重复加入");
        }
        user.setClassId(clazz.getId());
        userService.updateById(user);
        syncStudentCount(clazz.getId());

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Clazz resetInviteCode(Long id) {
        Clazz clazz = this.getDetail(id);
        clazz.setInviteCode(generateInviteCode());
        clazz.setUpdatedAt(LocalDateTime.now());
        this.updateById(clazz);
        fillStudentCount(clazz);
        return clazz;
    }

    @Override
    public void syncStudentCount(Long classId) {
        if (classId == null) {
            return;
        }
        Clazz clazz = this.getById(classId);
        if (clazz == null || Integer.valueOf(1).equals(clazz.getIsDeleted())) {
            return;
        }

        int studentCount = countStudents(classId);
        if (Objects.equals(clazz.getCurrentCount(), studentCount)) {
            return;
        }

        Clazz updateEntity = new Clazz();
        updateEntity.setId(classId);
        updateEntity.setCurrentCount(studentCount);
        updateEntity.setUpdatedAt(LocalDateTime.now());
        this.updateById(updateEntity);
    }

    @Override
    public void syncStudentCounts(List<Long> classIds) {
        if (classIds == null || classIds.isEmpty()) {
            return;
        }
        classIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet())
                .forEach(this::syncStudentCount);
    }

    /**
     * 生成邀请码
     */
    private String generateInviteCode() {
        String inviteCode;
        do {
            inviteCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (this.lambdaQuery()
                .eq(Clazz::getInviteCode, inviteCode)
                .eq(Clazz::getIsDeleted, 0)
                .count() > 0);
        return inviteCode;
    }

    private void fillStudentCounts(List<Clazz> clazzList) {
        clazzList.forEach(this::fillStudentCount);
    }

    private void fillStudentCount(Clazz clazz) {
        if (clazz == null || clazz.getId() == null) {
            return;
        }
        clazz.setCurrentCount(countStudents(clazz.getId()));
        fillTeacherInfo(clazz);
    }

    private int countStudents(Long classId) {
        long studentCount = userService.lambdaQuery()
                .eq(User::getClassId, classId)
                .eq(User::getIsDeleted, 0)
                .count();
        return Math.toIntExact(studentCount);
    }

    private boolean isTeacherUser(Long userId) {
        Role teacherRole = roleService.getByCode("teacher");
        if (teacherRole == null || userId == null) {
            return false;
        }
        return userRoleService.lambdaQuery()
                .eq(UserRole::getUserId, userId)
                .eq(UserRole::getRoleId, teacherRole.getId())
                .eq(UserRole::getIsDeleted, 0)
                .count() > 0;
    }

    private void ensureTeacherClassRelation(Long teacherId, Long classId) {
        if (teacherId == null || classId == null) {
            return;
        }
        boolean alreadyLinked = teacherClassMapper.selectClassIdsByTeacherId(teacherId).stream()
                .filter(Objects::nonNull)
                .anyMatch(classId::equals);
        if (alreadyLinked) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        TeacherClass relation = new TeacherClass();
        relation.setTeacherId(teacherId);
        relation.setClassId(classId);
        relation.setIsDeleted(0);
        relation.setCreatedAt(now);
        relation.setUpdatedAt(now);
        teacherClassMapper.insert(relation);
    }

    private void prepareClazzList(List<Clazz> clazzList, boolean includeStudentCount) {
        if (clazzList == null || clazzList.isEmpty()) {
            return;
        }

        List<Long> classIdsToSync = new ArrayList<>();
        clazzList.forEach(clazz -> prepareClazz(clazz, classIdsToSync));
        if (!classIdsToSync.isEmpty()) {
            syncStudentCounts(classIdsToSync);
        }
        if (includeStudentCount) {
            fillStudentCounts(clazzList);
        }
    }

    private void prepareClazz(Clazz clazz, boolean includeStudentCount) {
        List<Long> classIdsToSync = new ArrayList<>();
        prepareClazz(clazz, classIdsToSync);
        if (!classIdsToSync.isEmpty()) {
            syncStudentCounts(classIdsToSync);
        }
        if (includeStudentCount) {
            fillStudentCount(clazz);
        }
    }

    private void prepareClazz(Clazz clazz, List<Long> classIdsToSync) {
        if (clazz == null || clazz.getId() == null) {
            return;
        }

        boolean shouldUpdate = false;
        if (!StringUtils.hasText(clazz.getInviteCode())) {
            clazz.setInviteCode(generateInviteCode());
            shouldUpdate = true;
        }
        if (clazz.getCreatedAt() == null) {
            clazz.setCreatedAt(clazz.getUpdatedAt() != null ? clazz.getUpdatedAt() : LocalDateTime.now());
            shouldUpdate = true;
        }
        if (shouldUpdate) {
            clazz.setUpdatedAt(LocalDateTime.now());
            this.updateById(clazz);
        }
        if (clazz.getCurrentCount() == null) {
            classIdsToSync.add(clazz.getId());
        }
        fillTeacherInfo(clazz);
    }

    private void fillTeacherInfo(Clazz clazz) {
        if (clazz == null || clazz.getId() == null) {
            return;
        }

        if (clazz.getTeacherId() != null) {
            User headTeacher = userService.getById(clazz.getTeacherId());
            clazz.setHeadTeacherName(
                    headTeacher != null
                            && Integer.valueOf(0).equals(headTeacher.getIsDeleted())
                            && StringUtils.hasText(headTeacher.getRealName())
                            ? headTeacher.getRealName()
                            : null
            );
        } else {
            clazz.setHeadTeacherName(null);
        }

        String courseTeacherNames = courseService.getByClassId(clazz.getId()).stream()
                .map(Course::getTeacherName)
                .filter(StringUtils::hasText)
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(LinkedHashSet::new),
                        names -> String.join("、", names)
                ));
        clazz.setTeacherNames(StringUtils.hasText(courseTeacherNames)
                ? courseTeacherNames
                : clazz.getHeadTeacherName());
    }
}
