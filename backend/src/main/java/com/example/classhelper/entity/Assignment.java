package com.example.classhelper.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("assignment")
public class Assignment {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("class_id")
    private Long classId;

    @TableField("course_id")
    private Long courseId;

    @TableField("teacher_id")
    private Long teacherId;

    private String title;

    @TableField("content")
    private String description;

    private Integer type;

    @TableField("max_score")
    private Integer totalScore;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("attachments")
    private String attachmentUrl;

    @TableField("file_url")
    private String fileUrl;

    private Integer status;

    @TableField("submit_count")
    private Integer submitCount;

    @TableField("graded_count")
    private Integer gradedCount;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    // 非持久化字段：学生的提交信息
    @TableField(exist = false)
    private AssignmentSubmission mySubmission;

}
