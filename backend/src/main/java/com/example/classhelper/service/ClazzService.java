package com.example.classhelper.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.classhelper.dto.ClazzDTO;
import com.example.classhelper.dto.ClazzQueryDTO;
import com.example.classhelper.dto.HeadTeacherBindDTO;
import com.example.classhelper.entity.Clazz;
import com.example.classhelper.vo.PageVO;

import java.util.List;

/**
 * 班级 Service 接口
 */
public interface ClazzService extends IService<Clazz> {

    /**
     * 分页查询班级列表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageVO<Clazz> pageList(ClazzQueryDTO queryDTO);

    /**
     * 新增班级
     * @param dto 班级信息
     * @return 是否成功
     */
    boolean add(ClazzDTO dto);

    /**
     * 编辑班级
     * @param dto 班级信息
     * @return 是否成功
     */
    boolean update(ClazzDTO dto);

    /**
     * 删除班级
     * @param id 班级ID
     * @return 是否成功
     */
    boolean remove(Long id);

    /**
     * 获取班级详情
     * @param id 班级ID
     * @return 班级信息
     */
    Clazz getDetail(Long id);

    /**
     * 获取教师管理的班级
     * @param teacherId 教师ID
     * @return 班级列表
     */
    List<Clazz> getByTeacherId(Long teacherId);

    /**
     * 获取教师担任班主任的班级
     * @param teacherId 教师ID
     * @return 班级列表
     */
    List<Clazz> getHeadTeacherClasses(Long teacherId);

    /**
     * 判断教师是否担任任一班级班主任
     * @param teacherId 教师ID
     * @return 是否为班主任
     */
    boolean isHeadTeacher(Long teacherId);

    /**
     * 绑定教师为指定班级班主任
     * @param dto 绑定信息
     */
    void bindHeadTeacher(HeadTeacherBindDTO dto);

    /**
     * 获取所有未删除班级
     * @return 班级列表
     */
    List<Clazz> getAllAvailableClasses();

    /**
     * 通过邀请码加入班级
     * @param inviteCode 邀请码
     * @param studentId 学生ID
     * @return 是否成功
     */
    boolean joinByInviteCode(String inviteCode, Long studentId);

    /**
     * 重置班级邀请码
     * @param id 班级ID
     * @return 最新班级信息
     */
    Clazz resetInviteCode(Long id);

    /**
     * 同步单个班级学生人数
     * @param classId 班级ID
     */
    void syncStudentCount(Long classId);

    /**
     * 批量同步班级学生人数
     * @param classIds 班级ID列表
     */
    void syncStudentCounts(List<Long> classIds);

}
