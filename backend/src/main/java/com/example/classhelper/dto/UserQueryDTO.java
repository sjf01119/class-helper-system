package com.example.classhelper.dto;

import lombok.Data;

/**
 * 用户查询 DTO
 */
@Data
public class UserQueryDTO {

    /**
     * 页码
     */
    private Long current = 1L;

    /**
     * 每页大小
     */
    private Long size = 10L;

    /**
     * 用户名/真实姓名关键字
     */
    private String keyword;

    /**
     * 角色
     */
    private String role;

    /**
     * 班级ID
     */
    private Long classId;

    /**
     * 状态
     */
    private Integer status;

}
