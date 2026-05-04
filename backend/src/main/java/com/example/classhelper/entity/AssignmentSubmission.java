package com.example.classhelper.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("assignment_submission")
public class AssignmentSubmission {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("assignment_id")
    private Long assignmentId;

    @TableField("student_id")
    private Long studentId;

    @TableField("class_id")
    private Long classId;

    private String content;

    @TableField("attachments")
    private String attachmentUrl;

    private Integer score;

    private String feedback;

    @TableField("graded_by")
    private Long gradedBy;

    @TableField("graded_at")
    private LocalDateTime gradedAt;

    @TableField("submit_time")
    private LocalDateTime submitTime;

    private Integer status;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

}
