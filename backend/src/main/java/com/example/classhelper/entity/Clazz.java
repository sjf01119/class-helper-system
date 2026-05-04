package com.example.classhelper.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 班级实体类
 * 对应数据库表 sys_class
 */
@Data
@TableName("sys_class")
public class Clazz {

    /**
     * 班级ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 班级名称
     */
    @TableField("class_name")
    private String className;

    /**
     * 班级描述
     */
    @TableField("description")
    private String description;

    /**
     * 邀请码，用于学生加入班级
     */
    @TableField("invite_code")
    private String inviteCode;

    /**
     * 班主任教师ID
     */
    @TableField("teacher_id")
    private Long teacherId;

    /**
     * 最大学生数
     */
    @TableField("max_students")
    private Integer maxStudents;

    /**
     * 当前学生数
     */
    @TableField("current_count")
    private Integer currentCount;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标志：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    // ==================== 非数据库字段 ====================

    /**
     * 班主任姓名
     */
    @TableField(exist = false)
    private String headTeacherName;

    /**
     * 授课教师姓名列表（顿号拼接）
     */
    @TableField(exist = false)
    private String teacherNames;

}
