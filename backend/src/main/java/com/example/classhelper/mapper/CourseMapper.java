package com.example.classhelper.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.classhelper.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 课程 Mapper 接口
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    /**
     * 分页查询课程列表（带教师名称和班级名称）
     */
    @Select("SELECT c.*, u.real_name as teacher_name, cl.class_name " +
            "FROM sys_course c " +
            "LEFT JOIN sys_user u ON c.teacher_id = u.id AND u.is_deleted = 0 " +
            "LEFT JOIN sys_class cl ON c.class_id = cl.id AND cl.is_deleted = 0 " +
            "WHERE c.is_deleted = 0 " +
            "ORDER BY c.created_at DESC")
    IPage<Course> selectPageWithDetails(Page<Course> page);

    /**
     * 根据教师ID查询课程
     */
    @Select("SELECT c.*, u.real_name AS teacher_name, cl.class_name, cl.current_count AS student_count, " +
            "COALESCE((SELECT COUNT(*) FROM assignment a WHERE a.course_id = c.id AND a.is_deleted = 0), 0) AS assignment_count, " +
            "COALESCE((SELECT COUNT(*) FROM assignment a WHERE a.course_id = c.id AND a.is_deleted = 0 " +
            "AND (a.end_time IS NULL OR a.end_time >= NOW())), 0) AS active_assignment_count, " +
            "COALESCE((SELECT ROUND(AVG(sub_rate.rate)) FROM (" +
            "SELECT CASE WHEN IFNULL(cl.current_count, 0) = 0 THEN 0 " +
            "ELSE COUNT(s.id) * 100.0 / cl.current_count END AS rate " +
            "FROM assignment a " +
            "LEFT JOIN assignment_submission s ON s.assignment_id = a.id AND s.is_deleted = 0 " +
            "WHERE a.course_id = c.id AND a.is_deleted = 0 " +
            "GROUP BY a.id) sub_rate), 0) AS completion_rate " +
            "FROM sys_course c " +
            "LEFT JOIN sys_user u ON c.teacher_id = u.id AND u.is_deleted = 0 " +
            "LEFT JOIN sys_class cl ON c.class_id = cl.id AND cl.is_deleted = 0 " +
            "WHERE c.teacher_id = #{teacherId} AND c.is_deleted = 0 " +
            "ORDER BY c.created_at DESC")
    List<Course> selectByTeacherId(@Param("teacherId") Long teacherId);

    @Select("SELECT c.*, u.real_name AS teacher_name, cl.class_name, cl.current_count AS student_count, " +
            "COALESCE((SELECT COUNT(*) FROM assignment a WHERE a.course_id = c.id AND a.is_deleted = 0), 0) AS assignment_count, " +
            "COALESCE((SELECT COUNT(*) FROM assignment a WHERE a.course_id = c.id AND a.is_deleted = 0 " +
            "AND (a.end_time IS NULL OR a.end_time >= NOW())), 0) AS active_assignment_count, " +
            "COALESCE((SELECT ROUND(AVG(sub_rate.rate)) FROM (" +
            "SELECT CASE WHEN IFNULL(cl.current_count, 0) = 0 THEN 0 " +
            "ELSE COUNT(s.id) * 100.0 / cl.current_count END AS rate " +
            "FROM assignment a " +
            "LEFT JOIN assignment_submission s ON s.assignment_id = a.id AND s.is_deleted = 0 " +
            "WHERE a.course_id = c.id AND a.is_deleted = 0 " +
            "GROUP BY a.id) sub_rate), 0) AS completion_rate " +
            "FROM sys_course c " +
            "LEFT JOIN sys_user u ON c.teacher_id = u.id AND u.is_deleted = 0 " +
            "LEFT JOIN sys_class cl ON c.class_id = cl.id AND cl.is_deleted = 0 " +
            "WHERE c.id = #{id} AND c.is_deleted = 0")
    Course selectDetailById(@Param("id") Long id);

    /**
     * 根据班级ID查询课程
     */
    @Select("SELECT c.*, u.real_name AS teacher_name, cl.class_name, cl.current_count AS student_count " +
            "FROM sys_course c " +
            "LEFT JOIN sys_user u ON c.teacher_id = u.id AND u.is_deleted = 0 " +
            "LEFT JOIN sys_class cl ON c.class_id = cl.id AND cl.is_deleted = 0 " +
            "WHERE c.class_id = #{classId} AND c.is_deleted = 0 " +
            "ORDER BY c.created_at DESC")
    List<Course> selectByClassId(@Param("classId") Long classId);

}
