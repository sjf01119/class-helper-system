package com.example.classhelper.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 班主任绑定 DTO
 */
@Data
public class HeadTeacherBindDTO {

    /**
     * 教师ID
     */
    @NotNull(message = "教师ID不能为空")
    private Long teacherId;

    /**
     * 班级ID
     */
    @NotNull(message = "班级ID不能为空")
    private Long classId;
}
