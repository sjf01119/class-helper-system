package com.example.classhelper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_login_log")
public class LoginLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    private String username;

    @TableField("ip")
    private String ip;

    @TableField("location")
    private String location;

    @TableField("browser")
    private String browser;

    @TableField("os")
    private String os;

    @TableField("status")
    private Integer loginStatus;

    @TableField("msg")
    private String msg;

    @TableField("created_at")
    private LocalDateTime createdAt;

}
