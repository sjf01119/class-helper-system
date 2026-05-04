package com.example.classhelper.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class AssignmentManageVO {

    private Long id;

    private String title;

    private String content;

    private Long courseId;

    private String courseName;

    private Long classId;

    private String className;

    private LocalDateTime deadline;

    private Integer totalScore;

    private Integer submittedCount;

    private Integer totalCount;

    private Integer pendingCount;

    private Integer rawStatus;

    private String statusText;

    private LocalDateTime createdAt;

    private List<AssignmentAttachmentVO> attachmentList = new ArrayList<>();
}
