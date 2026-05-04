package com.example.classhelper.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程实体类
 * 对应数据库表 sys_course
 */
@Data
@TableName("sys_course")
public class Course {

    /**
     * 课程ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课程名称
     */
    @TableField("course_name")
    private String courseName;

    /**
     * 学分
     */
    @TableField("credit")
    private Integer credit;

    /**
     * 课时
     */
    @TableField("course_hours")
    private Integer courseHours;

    /**
     * 授课教师ID
     */
    @TableField("teacher_id")
    private Long teacherId;

    /**
     * 所属班级ID
     */
    @TableField("class_id")
    private Long classId;

    /**
     * 课程描述
     */
    @TableField("description")
    private String description;

    /**
     * 课程封面
     */
    @TableField("cover_url")
    private String coverUrl;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 开课时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 结课时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

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
     * 教师姓名（关联查询）
     */
    @TableField(exist = false)
    private String teacherName;

    /**
     * 班级名称（关联查询）
     */
    @TableField(exist = false)
    private String className;

    /**
     * 班级学生人数
     */
    @TableField(exist = false)
    private Integer studentCount;

    /**
     * 课程作业数
     */
    @TableField(exist = false)
    private Integer assignmentCount;

    /**
     * 进行中的作业数
     */
    @TableField(exist = false)
    private Integer activeAssignmentCount;

    /**
     * 课程平均完成率
     */
    @TableField(exist = false)
    private Integer completionRate;

}
