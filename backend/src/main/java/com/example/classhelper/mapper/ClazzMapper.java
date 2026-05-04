package com.example.classhelper.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.classhelper.entity.Clazz;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 班级 Mapper 接口
 */
@Mapper
public interface ClazzMapper extends BaseMapper<Clazz> {

    @Select("SELECT * FROM sys_class WHERE invite_code = #{inviteCode} AND is_deleted = 0")
    Clazz selectByInviteCode(@Param("inviteCode") String inviteCode);

}
