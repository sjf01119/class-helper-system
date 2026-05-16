package com.example.classhelper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.classhelper.dto.UserDTO;
import com.example.classhelper.dto.UserQueryDTO;
import com.example.classhelper.entity.Clazz;
import com.example.classhelper.entity.Role;
import com.example.classhelper.entity.TeacherClass;
import com.example.classhelper.entity.User;
import com.example.classhelper.entity.UserRole;
import com.example.classhelper.exception.BusinessException;
import com.example.classhelper.mapper.TeacherClassMapper;
import com.example.classhelper.mapper.UserRoleMapper;
import com.example.classhelper.mapper.UserMapper;
import com.example.classhelper.service.*;
import com.example.classhelper.vo.PageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 用户管理 Service 实现类
 * 包含权限控制逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserManageServiceImpl extends ServiceImpl<UserMapper, User> implements UserManageService {

    private static final String TEACHER_USERNAME_PATTERN = "^[\\u4e00-\\u9fa5A-Za-z0-9_]{2,20}$";
    private static final String TEACHER_REAL_NAME_PATTERN = "^[\\u4e00-\\u9fa5A-Za-z]{2,20}$";
    private static final String STUDENT_USERNAME_PATTERN = "^[\\u4e00-\\u9fa5A-Za-z0-9_]{2,20}$";
    private static final String STUDENT_REAL_NAME_PATTERN = "^[\\u4e00-\\u9fa5A-Za-z]{2,20}$";

    private final RoleService roleService;
    private final UserRoleService userRoleService;
    private final UserRoleMapper userRoleMapper;
    private final TeacherClassMapper teacherClassMapper;
    @Lazy
    private final ClazzService clazzService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public PageVO<User> pageList(UserQueryDTO queryDTO, Long currentUserId, List<String> currentUserRoles) {
        // 学生只能查看自己
        if (currentUserRoles.contains("student")) {
            User user = this.getById(currentUserId);
            return new PageVO<>(List.of(user), 1L, 1L, 10L);
        }
        
        Page<User> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        // 教师只能查看本班学生
        if (currentUserRoles.contains("teacher") && !currentUserRoles.contains("admin")) {
            // 获取教师管理的班级
            List<Clazz> clazzList = clazzService.getByTeacherId(currentUserId);
            if (CollectionUtils.isEmpty(clazzList)) {
                return new PageVO<>(List.of(), 0L, 1L, 10L);
            }
            List<Long> classIds = clazzList.stream().map(Clazz::getId).toList();
            wrapper.in(User::getClassId, classIds);
        }
        
        // 关键字搜索（用户名/真实姓名）
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(User::getUsername, queryDTO.getKeyword())
                             .or()
                             .like(User::getRealName, queryDTO.getKeyword()));
        }
        
        // 按角色筛选 - 使用更简单的方式，通过用户角色关联表直接筛选
        if (StringUtils.hasText(queryDTO.getRole())) {
            // 直接通过用户角色关联表查询
            wrapper.inSql(User::getId, 
                "SELECT user_id FROM sys_user_role ur " +
                "INNER JOIN sys_role r ON ur.role_id = r.id AND r.is_deleted = 0 " +
                "WHERE r.role_code = '" + queryDTO.getRole() + "' AND ur.is_deleted = 0"
            );
        }
        
        // 按班级筛选
        if (queryDTO.getClassId() != null) {
            wrapper.eq(User::getClassId, queryDTO.getClassId());
        }
        
        // 按状态筛选
        if (queryDTO.getStatus() != null) {
            wrapper.eq(User::getStatus, queryDTO.getStatus());
        }
        
        // 未删除
        wrapper.eq(User::getIsDeleted, 0);
        
        // 按创建时间倒序
        wrapper.orderByDesc(User::getCreatedAt);
        
        Page<User> resultPage = this.page(page, wrapper);

        resultPage.getRecords().forEach(user -> {
            List<String> roles = getUserRoles(user.getId());
            user.setRoles(roles);

            if (user.getClassId() != null) {
                Clazz clazz = clazzService.getById(user.getClassId());
                if (clazz != null && clazz.getIsDeleted() == 0) {
                    user.setClassName(clazz.getClassName());
                }
            }

            if (roles.contains("teacher")) {
                fillTeacherExtraInfo(user);
            }
        });
        
        return new PageVO<>(resultPage.getRecords(), resultPage.getTotal(),
                           resultPage.getCurrent(), resultPage.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(UserDTO dto) {
        validateTeacherUsername(dto.getUsername(), dto.getRoles());
        validateTeacherRealName(dto.getRealName(), dto.getRoles());
        validateStudentUsername(dto.getUsername(), dto.getRoles());
        validateStudentRealName(dto.getRealName(), dto.getRoles());

        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        wrapper.eq(User::getIsDeleted, 0);
        if (this.count(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }
        
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        if (!CollectionUtils.isEmpty(dto.getRoles()) && dto.getRoles().contains("teacher")) {
            user.setClassId(null);
        }
        if (!CollectionUtils.isEmpty(dto.getRoles()) && dto.getRoles().contains("student") && user.getClassId() != null) {
            clazzService.assertClassNotFull(user.getClassId());
        }
        
        // 密码加密
        if (!StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode("123456"));
        } else {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        
        // 默认启用
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        
        // 保存用户
        this.save(user);
        
        // 保存角色关联
        saveUserRoles(user.getId(), dto.getRoles());

        if (!CollectionUtils.isEmpty(dto.getRoles()) && dto.getRoles().contains("teacher")) {
            saveTeacherClassRelations(user.getId(), dto.getClassIds());
        }

        if (!CollectionUtils.isEmpty(dto.getRoles()) && dto.getRoles().contains("student") && user.getClassId() != null) {
            clazzService.syncStudentCount(user.getClassId());
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(UserDTO dto, Long currentUserId, List<String> currentUserRoles) {
        if (dto.getId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        
        User user = this.getById(dto.getId());
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }

        List<String> oldRoles = getUserRoles(user.getId());
        Long oldClassId = user.getClassId();
        boolean oldIsStudent = oldRoles.contains("student");
        boolean oldIsTeacher = oldRoles.contains("teacher");
        boolean hasNewRoles = !CollectionUtils.isEmpty(dto.getRoles());
        boolean newIsTeacher = hasNewRoles ? dto.getRoles().contains("teacher") : oldRoles.contains("teacher");
        boolean newIsStudent = hasNewRoles ? dto.getRoles().contains("student") : oldIsStudent;
        
        // 权限检查：教师不能修改学生信息
        if (currentUserRoles.contains("teacher") && !currentUserRoles.contains("admin")) {
            throw new BusinessException("教师不能更改学生信息");
        }
        
        // 检查用户名是否与其他用户重复
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        wrapper.ne(User::getId, dto.getId());
        wrapper.eq(User::getIsDeleted, 0);
        if (this.count(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }
        
        BeanUtils.copyProperties(dto, user, "password");
        if (StringUtils.hasText(dto.getPassword())) {
            if (!currentUserRoles.contains("admin")) {
                throw new BusinessException("无权修改用户密码");
            }
            user.setPassword(passwordEncoder.encode(dto.getPassword().trim()));
        }
        if (newIsTeacher) {
            user.setClassId(null);
        }
        if (newIsStudent && user.getClassId() != null && (oldClassId == null || !oldClassId.equals(user.getClassId()))) {
            clazzService.assertClassNotFull(user.getClassId());
        }
        user.setUpdatedAt(LocalDateTime.now());

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, user.getId())
                .set(User::getUsername, user.getUsername())
                .set(User::getRealName, user.getRealName())
                .set(User::getEmail, user.getEmail())
                .set(User::getPhone, user.getPhone())
                .set(User::getStatus, user.getStatus())
                .set(User::getClassId, user.getClassId())
                .set(User::getUpdatedAt, user.getUpdatedAt());
        if (StringUtils.hasText(dto.getPassword())) {
            updateWrapper.set(User::getPassword, user.getPassword());
        }
        this.update(null, updateWrapper);
        
        // 更新角色（如果有）
        if (hasNewRoles) {
            userRoleMapper.deleteByUserId(user.getId());
            
            // 保存新角色
            saveUserRoles(user.getId(), dto.getRoles());
        }

        Long newClassId = newIsStudent ? user.getClassId() : null;
        if (oldIsStudent || newIsStudent) {
            clazzService.syncStudentCounts(Stream.of(oldClassId, newClassId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList());
        }

        if (oldIsTeacher || newIsTeacher) {
            if (!newIsTeacher) {
                teacherClassMapper.deleteByTeacherId(user.getId());
            } else if (dto.getClassIds() != null) {
                saveTeacherClassRelations(user.getId(), dto.getClassIds());
            }
        }
        
        return true;
    }

    @Override
    public boolean remove(Long id) {
        User user = this.getById(id);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        
        // 不能删除自己
        // TODO: 获取当前登录用户ID进行比较
        
        boolean removed = this.removeById(id);
        if (removed && user.getClassId() != null) {
            clazzService.syncStudentCount(user.getClassId());
        }
        return removed;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeBatch(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException("请选择要删除的用户");
        }

        List<User> users = this.listByIds(ids);
        if (users.size() != ids.size()) {
            throw new BusinessException("部分用户不存在");
        }

        boolean removed = this.removeByIds(ids);
        if (removed) {
            List<Long> classIds = users.stream()
                    .map(User::getClassId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            if (!classIds.isEmpty()) {
                clazzService.syncStudentCounts(classIds);
            }
        }
        return removed;
    }

    @Override
    public User getDetail(Long id, Long currentUserId, List<String> currentUserRoles) {
        User user = this.getById(id);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        
        // 学生只能查看自己
        if (currentUserRoles.contains("student") && !id.equals(currentUserId)) {
            throw new BusinessException("无权查看其他用户信息");
        }
        
        // 教师只能查看自己或自己关联班级内的学生
        if (currentUserRoles.contains("teacher") && !currentUserRoles.contains("admin") && !id.equals(currentUserId)) {
            List<Clazz> clazzList = clazzService.getByTeacherId(currentUserId);
            List<Long> classIds = clazzList.stream().map(Clazz::getId).toList();
            if (!classIds.contains(user.getClassId())) {
                throw new BusinessException("无权查看该学生信息");
            }
        }
        
        // 查询角色
        user.setRoles(getUserRoles(user.getId()));
        
        // 查询班级名称
        if (user.getClassId() != null) {
            Clazz clazz = clazzService.getById(user.getClassId());
            if (clazz != null) {
                user.setClassName(clazz.getClassName());
            }
        }

        if (user.getRoles().contains("teacher")) {
            fillTeacherExtraInfo(user);
        }
        
        return user;
    }

    @Override
    public boolean resetPassword(Long id, String newPassword) {
        User user = this.getById(id);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        return this.updateById(user);
    }

    @Override
    public List<User> getStudentsByTeacherId(Long teacherId) {
        // 获取教师管理的班级
        List<Clazz> clazzList = clazzService.getByTeacherId(teacherId);
        if (CollectionUtils.isEmpty(clazzList)) {
            return List.of();
        }

        List<Long> classIds = clazzList.stream().map(Clazz::getId).toList();

        // 查询这些班级的学生
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(User::getClassId, classIds);
        wrapper.eq(User::getIsDeleted, 0);

        return this.list(wrapper);
    }

    @Override
    public List<User> getStudentsByClassId(Long classId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getClassId, classId);
        wrapper.eq(User::getIsDeleted, 0);
        return this.list(wrapper);
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        // 更新为新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        return this.updateById(user);
    }

    /**
     * 获取用户角色列表
     */
    private List<String> getUserRoles(Long userId) {
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        wrapper.eq(UserRole::getIsDeleted, 0);
        
        return userRoleService.list(wrapper).stream()
                .map(ur -> {
                    Role role = roleService.getById(ur.getRoleId());
                    return role != null ? role.getRoleCode() : "";
                })
                .filter(code -> !code.isEmpty())
                .toList();
    }

    /**
     * 保存用户角色关联
     */
    private void saveUserRoles(Long userId, List<String> roleCodes) {
        if (CollectionUtils.isEmpty(roleCodes)) {
            return;
        }
        
        for (String roleCode : roleCodes) {
            Role role = roleService.getByCode(roleCode);
            if (role != null) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(role.getId());
                userRoleService.save(userRole);
            }
        }
    }

    private void validateTeacherUsername(String username, List<String> roles) {
        if (CollectionUtils.isEmpty(roles) || !roles.contains("teacher")) {
            return;
        }
        if (!StringUtils.hasText(username) || !username.matches(TEACHER_USERNAME_PATTERN)) {
            throw new BusinessException("教师用户名只能包含中文、字母、数字、下划线，长度2-20");
        }
    }

    private void validateTeacherRealName(String realName, List<String> roles) {
        if (CollectionUtils.isEmpty(roles) || !roles.contains("teacher")) {
            return;
        }
        if (!StringUtils.hasText(realName) || !realName.matches(TEACHER_REAL_NAME_PATTERN)) {
            throw new BusinessException("教师真实姓名只能包含中文、字母，长度2-20");
        }
    }

    private void validateStudentUsername(String username, List<String> roles) {
        if (CollectionUtils.isEmpty(roles) || !roles.contains("student")) {
            return;
        }
        if (!StringUtils.hasText(username) || !username.matches(STUDENT_USERNAME_PATTERN)) {
            throw new BusinessException("学生用户名只能包含中文、字母、数字、下划线，长度2-20");
        }
    }

    private void validateStudentRealName(String realName, List<String> roles) {
        if (CollectionUtils.isEmpty(roles) || !roles.contains("student")) {
            return;
        }
        if (!StringUtils.hasText(realName) || !realName.matches(STUDENT_REAL_NAME_PATTERN)) {
            throw new BusinessException("学生真实姓名只能包含中文、字母，长度2-20");
        }
    }

    private void saveTeacherClassRelations(Long teacherId, List<Long> classIds) {
        teacherClassMapper.deleteByTeacherId(teacherId);
        List<Long> requestedClassIds = Stream.concat(
                        CollectionUtils.isEmpty(classIds)
                                ? Stream.empty()
                                : classIds.stream().filter(Objects::nonNull),
                        clazzService.getHeadTeacherClasses(teacherId).stream().map(Clazz::getId)
                )
                .distinct()
                .toList();
        if (requestedClassIds.isEmpty()) {
            return;
        }

        List<Long> validClassIds = clazzService.lambdaQuery()
                .in(Clazz::getId, requestedClassIds)
                .eq(Clazz::getIsDeleted, 0)
                .list()
                .stream()
                .map(Clazz::getId)
                .toList();

        LocalDateTime now = LocalDateTime.now();
        validClassIds.forEach(classId -> {
            TeacherClass relation = new TeacherClass();
            relation.setTeacherId(teacherId);
            relation.setClassId(classId);
            relation.setIsDeleted(0);
            relation.setCreatedAt(now);
            relation.setUpdatedAt(now);
            teacherClassMapper.insert(relation);
        });
    }

    private void fillTeacherExtraInfo(User user) {
        List<Clazz> managedClazzList = clazzService.getByTeacherId(user.getId());
        List<Long> managedClassIds = managedClazzList.stream().map(Clazz::getId).toList();
        user.setClassIds(managedClassIds);
        if (!CollectionUtils.isEmpty(managedClazzList)) {
            user.setClassId(managedClazzList.get(0).getId());
            user.setClassName(managedClazzList.stream()
                    .map(Clazz::getClassName)
                    .collect(Collectors.joining("、")));
        } else {
            user.setClassId(null);
            user.setClassName("未关联班级");
        }

        List<Clazz> headTeacherClazzList = clazzService.getHeadTeacherClasses(user.getId());
        user.setIsHeadTeacher(!CollectionUtils.isEmpty(headTeacherClazzList));
        if (!CollectionUtils.isEmpty(headTeacherClazzList)) {
            user.setHeadTeacherClassId(headTeacherClazzList.get(0).getId());
            user.setHeadTeacherClassName(headTeacherClazzList.stream()
                    .map(Clazz::getClassName)
                    .collect(Collectors.joining("、")));
        } else {
            user.setHeadTeacherClassId(null);
            user.setHeadTeacherClassName("未设置");
        }
    }
}
