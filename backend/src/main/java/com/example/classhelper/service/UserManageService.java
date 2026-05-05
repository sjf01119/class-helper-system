package com.example.classhelper.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.classhelper.dto.UserDTO;
import com.example.classhelper.dto.UserQueryDTO;
import com.example.classhelper.entity.User;
import com.example.classhelper.vo.PageVO;

import java.util.List;

/**
 * 用户管理 Service 接口
 * 提供带权限控制的用户管理功能
 */
public interface UserManageService extends IService<User> {

    /**
     * 分页查询用户列表
     * 管理员：查看所有用户
     * 教师：查看本班学生
     * 学生：查看自己信息
     * @param queryDTO 查询条件
     * @param currentUserId 当前用户ID
     * @param currentUserRoles 当前用户角色
     * @return 分页结果
     */
    PageVO<User> pageList(UserQueryDTO queryDTO, Long currentUserId, List<String> currentUserRoles);

    /**
     * 新增用户（仅管理员）
     * @param dto 用户信息
     * @return 是否成功
     */
    boolean add(UserDTO dto);

    /**
     * 编辑用户
     * 管理员：编辑所有用户
     * 教师：编辑本班学生
     * @param dto 用户信息
     * @param currentUserId 当前用户ID
     * @param currentUserRoles 当前用户角色
     * @return 是否成功
     */
    boolean update(UserDTO dto, Long currentUserId, List<String> currentUserRoles);

    /**
     * 删除用户（仅管理员）
     * @param id 用户ID
     * @return 是否成功
     */
    boolean remove(Long id);

    /**
     * 批量删除用户
     * @param ids 用户ID列表
     * @return 是否成功
     */
    boolean removeBatch(List<Long> ids);

    /**
     * 获取用户详情
     * @param id 用户ID
     * @param currentUserId 当前用户ID
     * @param currentUserRoles 当前用户角色
     * @return 用户信息
     */
    User getDetail(Long id, Long currentUserId, List<String> currentUserRoles);

    /**
     * 重置密码（仅管理员）
     * @param id 用户ID
     * @param newPassword 新密码（加密后）
     * @return 是否成功
     */
    boolean resetPassword(Long id, String newPassword);

    /**
     * 获取本班学生列表（教师使用）
     * @param teacherId 教师ID
     * @return 学生列表
     */
    List<User> getStudentsByTeacherId(Long teacherId);

    /**
     * 根据班级ID获取学生列表
     * @param classId 班级ID
     * @return 学生列表
     */
    List<User> getStudentsByClassId(Long classId);

    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

}
