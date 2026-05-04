package com.example.classhelper.dto;

import lombok.Data;

/**
 * 班级查询 DTO
 */
@Data
public class ClazzQueryDTO {

    /**
     * 页码
     */
    private Long current = 1L;

    /**
     * 每页大小
     */
    private Long size = 10L;

    /**
     * 班级名称关键字
     */
    private String keyword;

    /**
     * 状态
     */
    private Integer status;

}
