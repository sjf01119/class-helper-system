package com.example.classhelper.vo;

import lombok.Data;

import java.util.List;

/**
 * 登录响应 VO
 */
@Data
public class LoginVO {

    /**
     * JWT Token
     */
    private String token;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 角色列表
     */
    private List<String> roles;

    /**
     * 用户信息
     */
    private UserInfoVO userInfo;

    /**
     * 用户信息内部类
     */
    @Data
    public static class UserInfoVO {
        private Long id;
        private String username;
        private String nickname;
        private String realName;
        private String avatarUrl;
        private String email;
        private String phone;
        private Integer gender;
        private Boolean isHeadTeacher;
    }

}
