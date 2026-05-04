package com.example.classhelper.dto;

import com.example.classhelper.vo.AssignmentAttachmentVO;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class AssignmentManageDTO {

    private Long id;

    @NotBlank(message = "作业标题不能为空")
    @Size(min = 1, max = 100, message = "作业标题长度需在1-100个字符之间")
    private String title;

    @NotBlank(message = "作业内容不能为空")
    @Size(min = 1, max = 2000, message = "作业内容长度需在1-2000个字符之间")
    private String content;

    @NotNull(message = "所属课程不能为空")
    private Long courseId;

    @NotNull(message = "截止时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Future(message = "截止时间必须晚于当前时间")
    private LocalDateTime deadline;

    @NotNull(message = "总分不能为空")
    @Min(value = 1, message = "总分必须为1-100的整数")
    @Max(value = 100, message = "总分必须为1-100的整数")
    private Integer totalScore;

    @Size(max = 10, message = "最多上传10个附件")
    private List<AssignmentAttachmentVO> attachmentList = new ArrayList<>();
}
