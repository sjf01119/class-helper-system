package com.example.classhelper.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CourseBatchDTO {

    @NotEmpty(message = "请选择课程")
    private List<Long> ids;

    @NotNull(message = "课程状态不能为空")
    private Integer status;
}
