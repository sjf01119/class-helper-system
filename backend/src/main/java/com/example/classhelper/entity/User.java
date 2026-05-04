package com.example.classhelper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户实体类 - 对应 sys_user 表
 * 存储系统用户信息：管理员、教师、学生
 */
@Data
@TableName("sys_user")
public class User {

    /**
     * 用户ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名，唯一
     */
    private String username;

    /**
     * 密码（BCrypt加密）
     */
    private String password;

    /**
     * 真实姓名
     */
    @TableField("real_name")
    private String realName;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 性别：0-女，1-男，2-保密
     */
    private Integer gender;

    /**
     * 状态：0-禁用，1-正常
     */
    private Integer status;

    /**
     * 所属班级ID（学生）
     */
    @TableField("class_id")
    private Long classId;

    /**
     * 最后登录时间
     */
    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 最后登录IP
     */
    @TableField("last_login_ip")
    private String lastLoginIp;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标志：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    // ==================== 非数据库字段 ====================

    /**
     * 角色列表（关联查询）
     */
    @TableField(exist = false)
    private List<String> roles;

    /**
     * 班级名称（关联查询）
     */
    @TableField(exist = false)
    private String className;

    /**
     * 教师关联班级ID列表（关联查询）
     */
    @TableField(exist = false)
    private List<Long> classIds;

    /**
     * 是否担任班主任
     */
    @TableField(exist = false)
    private Boolean isHeadTeacher;

    /**
     * 班主任班级ID（关联查询）
     */
    @TableField(exist = false)
    private Long headTeacherClassId;

    /**
     * 班主任班级名称（关联查询）
     */
    @TableField(exist = false)
    private String headTeacherClassName;

}
