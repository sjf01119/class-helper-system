package com.example.classhelper.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 用户 DTO
 * 用于新增/编辑用户
 */
@Data
public class UserDTO {

    /**
     * 用户ID（编辑时使用）
     */
    private Long id;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20之间")
    private String username;

    /**
     * 密码（不填写时后端会使用默认密码）
     */
    @Pattern(regexp = "^(?:.{6,20})?$", message = "密码长度必须在6-20之间")
    private String password;

    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过50")
    private String realName;

    /**
     * 手机号
     */
    @Pattern(regexp = "^(|1[3-9]\\d{9})$", message = "手机号格式不正确")
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 角色列表
     */
    @NotNull(message = "角色不能为空")
    private List<String> roles;

    /**
     * 班级ID（学生必填）
     */
    private Long classId;

    /**
     * 教师管理班级ID列表（教师可多选）
     */
    private List<Long> classIds;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

}
