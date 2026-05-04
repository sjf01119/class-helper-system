package com.example.classhelper.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.classhelper.entity.Role;

public interface RoleService extends IService<Role> {

    /**
     * 根据角色编码获取角色
     * @param roleCode 角色编码
     * @return 角色实体
     */
    Role getByCode(String roleCode);
}
