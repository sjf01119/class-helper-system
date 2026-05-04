package com.example.classhelper.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程 DTO
 * 用于新增/编辑课程
 */
@Data
public class CourseDTO {

    /**
     * 课程ID（编辑时使用）
     */
    private Long id;

    /**
     * 课程名称
     */
    @NotBlank(message = "课程名称不能为空")
    @Size(max = 50, message = "课程名称长度不能超过50")
    private String courseName;

    /**
     * 授课教师ID
     */
    private Long teacherId;

    /**
     * 所属班级ID
     */
    @NotNull(message = "所属班级不能为空")
    private Long classId;

    /**
     * 学分
     */
    @PositiveOrZero(message = "学分不能小于0")
    private Integer credit;

    /**
     * 课时
     */
    @PositiveOrZero(message = "课时不能小于0")
    private Integer courseHours;

    /**
     * 课程描述
     */
    @Size(max = 500, message = "课程描述长度不能超过500")
    private String description;

    /**
     * 课程封面
     */
    private String coverUrl;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 开课时间
     */
    private LocalDateTime startTime;

    /**
     * 结课时间
     */
    private LocalDateTime endTime;

}
