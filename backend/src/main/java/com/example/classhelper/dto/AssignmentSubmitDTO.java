package com.example.classhelper.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AssignmentSubmitDTO {

    @NotNull(message = "作业ID不能为空")
    private Long homeworkId;

    @Size(max = 2000, message = "作业内容长度不能超过2000个字符")
    private String content;

    private String attachments;
}
