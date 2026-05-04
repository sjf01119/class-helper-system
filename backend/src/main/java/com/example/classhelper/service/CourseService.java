package com.example.classhelper.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.classhelper.dto.CourseDTO;
import com.example.classhelper.dto.CourseQueryDTO;
import com.example.classhelper.entity.Course;
import com.example.classhelper.vo.PageVO;

import java.util.List;

/**
 * 课程 Service 接口
 */
public interface CourseService extends IService<Course> {

    /**
     * 分页查询课程列表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageVO<Course> pageList(CourseQueryDTO queryDTO, Long currentUserId, List<String> roles);

    /**
     * 新增课程
     * @param dto 课程信息
     * @return 是否成功
     */
    boolean add(CourseDTO dto, Long currentUserId, List<String> roles);

    /**
     * 编辑课程
     * @param dto 课程信息
     * @return 是否成功
     */
    boolean update(CourseDTO dto, Long currentUserId, List<String> roles);

    /**
     * 删除课程
     * @param id 课程ID
     * @return 是否成功
     */
    boolean remove(Long id, Long currentUserId, List<String> roles);

    /**
     * 获取课程详情
     * @param id 课程ID
     * @return 课程信息
     */
    Course getDetail(Long id, Long currentUserId, List<String> roles);

    /**
     * 批量更新课程状态
     */
    boolean batchUpdateStatus(List<Long> ids, Integer status, Long currentUserId, List<String> roles);

    /**
     * 批量删除课程
     */
    boolean batchRemove(List<Long> ids, Long currentUserId, List<String> roles);

    /**
     * 获取教师授课的课程
     * @param teacherId 教师ID
     * @return 课程列表
     */
    List<Course> getByTeacherId(Long teacherId);

    /**
     * 获取班级的课程
     * @param classId 班级ID
     * @return 课程列表
     */
    List<Course> getByClassId(Long classId);

}
