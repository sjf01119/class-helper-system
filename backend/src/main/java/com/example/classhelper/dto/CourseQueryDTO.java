package com.example.classhelper.dto;

import lombok.Data;

/**
 * 课程查询 DTO
 */
@Data
public class CourseQueryDTO {

    /**
     * 页码
     */
    private Long current = 1L;

    /**
     * 每页大小
     */
    private Long size = 10L;

    /**
     * 课程名称关键字
     */
    private String keyword;

    /**
     * 授课教师ID
     */
    private Long teacherId;

    /**
     * 所属班级ID
     */
    private Long classId;

    /**
     * 状态
     */
    private Integer status;

}
