package com.example.classhelper.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class AssignmentSubmissionVO {

    private Long id;

    private Long assignmentId;

    private Long studentId;

    private String studentName;

    private String studentNo;

    private String content;

    private LocalDateTime submitTime;

    private Integer score;

    private String comment;

    private Integer status;

    private List<AssignmentAttachmentVO> attachmentList = new ArrayList<>();
}
