package com.example.classhelper.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.classhelper.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND is_deleted = 0")
    User selectByUsername(@Param("username") String username);

    /**
     * 根据角色查询用户列表
     */
    @Select("SELECT u.* FROM sys_user u " +
            "INNER JOIN sys_user_role ur ON u.id = ur.user_id AND ur.is_deleted = 0 " +
            "INNER JOIN sys_role r ON ur.role_id = r.id AND r.is_deleted = 0 " +
            "WHERE r.role_code = #{roleCode} AND u.is_deleted = 0")
    List<User> selectByRole(@Param("roleCode") String roleCode);

    /**
     * 根据班级ID查询学生列表
     */
    @Select("SELECT u.* FROM sys_user u " +
            "INNER JOIN sys_user_role ur ON u.id = ur.user_id AND ur.is_deleted = 0 " +
            "INNER JOIN sys_role r ON ur.role_id = r.id AND r.is_deleted = 0 " +
            "WHERE r.role_code = 'student' AND u.class_id = #{classId} AND u.is_deleted = 0")
    List<User> selectStudentsByClassId(@Param("classId") Long classId);

    /**
     * 分页查询用户列表（带角色和班级信息）
     */
    @Select("SELECT u.*, c.class_name " +
            "FROM sys_user u " +
            "LEFT JOIN sys_class c ON u.class_id = c.id AND c.is_deleted = 0 " +
            "WHERE u.is_deleted = 0 " +
            "ORDER BY u.created_at DESC")
    IPage<User> selectPageWithDetails(Page<User> page);

    /**
     * 根据角色编码统计用户数量
     */
    @Select("SELECT COUNT(*) FROM sys_user u " +
            "INNER JOIN sys_user_role ur ON u.id = ur.user_id AND ur.is_deleted = 0 " +
            "INNER JOIN sys_role r ON ur.role_id = r.id AND r.is_deleted = 0 " +
            "WHERE r.role_code = #{roleCode} AND u.is_deleted = 0")
    Long countByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 统计近6个月用户新增趋势
     */
    @Select("SELECT DATE_FORMAT(u.created_at, '%Y-%m') AS month, COUNT(*) AS value " +
            "FROM sys_user u " +
            "WHERE u.is_deleted = 0 " +
            "AND u.created_at >= DATE_SUB(DATE_FORMAT(CURDATE(), '%Y-%m-01'), INTERVAL 5 MONTH) " +
            "GROUP BY DATE_FORMAT(u.created_at, '%Y-%m')")
    List<Map<String, Object>> countUserGrowthByMonth();

    /**
     * 按班级统计学生数量
     */
    @Select("SELECT COALESCE(c.class_name, '未分班') AS name, COUNT(*) AS studentCount " +
            "FROM sys_user u " +
            "INNER JOIN sys_user_role ur ON u.id = ur.user_id AND ur.is_deleted = 0 " +
            "INNER JOIN sys_role r ON ur.role_id = r.id AND r.is_deleted = 0 AND r.role_code = 'student' " +
            "LEFT JOIN sys_class c ON u.class_id = c.id AND c.is_deleted = 0 AND c.status = 1 " +
            "WHERE u.is_deleted = 0 " +
            "GROUP BY COALESCE(c.class_name, '未分班') " +
            "ORDER BY studentCount DESC")
    List<Map<String, Object>> countStudentDistributionByClass();

}
