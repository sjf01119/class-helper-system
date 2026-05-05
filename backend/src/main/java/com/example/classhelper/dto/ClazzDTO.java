package com.example.classhelper.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 班级 DTO
 * 用于新增/编辑班级
 */
@Data
public class ClazzDTO {

    /**
     * 班级ID（编辑时使用）
     */
    private Long id;

    /**
     * 班级名称
     */
    @NotBlank(message = "班级名称不能为空")
    @Size(max = 50, message = "班级名称长度不能超过50")
    private String className;

    /**
     * 班级描述
     */
    @Size(max = 500, message = "班级描述长度不能超过500")
    private String description;

    /**
     * 邀请码
     */
    private String inviteCode;

    /**
     * 班主任教师ID
     */
    private Long teacherId;

    /**
     * 是否确认替换原班主任
     */
    private Boolean forceReplaceHeadTeacher;

    /**
     * 是否确认取消班主任
     */
    private Boolean confirmClearHeadTeacher;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

}
