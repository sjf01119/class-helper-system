package com.example.classhelper.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.classhelper.entity.AssignmentSubmission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AssignmentSubmissionMapper extends BaseMapper<AssignmentSubmission> {

    @Select("SELECT s.real_name, sub.score, sub.submit_time " +
            "FROM assignment_submission sub " +
            "JOIN sys_user s ON sub.student_id = s.id " +
            "WHERE sub.assignment_id = #{assignmentId} AND sub.status = 1 " +
            "ORDER BY sub.score DESC")
    List<Map<String, Object>> selectScoresByAssignment(@Param("assignmentId") Long assignmentId);

}
