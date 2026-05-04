package com.example.classhelper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("question")
public class Question {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("class_id")
    private Long classId;

    @TableField("student_id")
    private Long studentId;

    private String title;

    private String content;

    @TableField("is_anonymous")
    private Integer isAnonymous;

    @TableField("reply_content")
    private String replyContent;

    @TableField("reply_by")
    private Long replyBy;

    @TableField("reply_time")
    private LocalDateTime replyTime;

    private Integer status;

    @TableField("view_count")
    private Integer viewCount;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
