package com.example.classhelper.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.classhelper.entity.TeacherClass;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TeacherClassMapper extends BaseMapper<TeacherClass> {

    @Select("SELECT class_id FROM sys_teacher_class WHERE teacher_id = #{teacherId} AND is_deleted = 0")
    List<Long> selectClassIdsByTeacherId(@Param("teacherId") Long teacherId);

    @Select("SELECT teacher_id FROM sys_teacher_class WHERE class_id = #{classId} AND is_deleted = 0")
    List<Long> selectTeacherIdsByClassId(@Param("classId") Long classId);

    @Delete("DELETE FROM sys_teacher_class WHERE teacher_id = #{teacherId}")
    int deleteByTeacherId(@Param("teacherId") Long teacherId);
}
