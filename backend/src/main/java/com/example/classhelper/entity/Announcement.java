package com.example.classhelper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_announcement")
public class Announcement {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private Integer type;

    @TableField("class_id")
    private Long classId;

    @TableField("publisher_id")
    private Long publisherId;

    @TableField(exist = false)
    private String publisherName;

    @TableField(exist = false)
    private Integer isTop;

    @TableField(exist = false)
    private Integer publishScope;

    private Integer priority;

    private Integer status;

    @TableField("view_count")
    private Integer viewCount;

    @TableField("publish_time")
    private LocalDateTime publishTime;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

}
