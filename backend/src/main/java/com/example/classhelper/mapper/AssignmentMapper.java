package com.example.classhelper.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.classhelper.entity.Assignment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AssignmentMapper extends BaseMapper<Assignment> {

    @Select("SELECT status, COUNT(*) as count FROM assignment_submission WHERE assignment_id = #{assignmentId} GROUP BY status")
    List<Map<String, Object>> selectSubmissionStats(@Param("assignmentId") Long assignmentId);

}
