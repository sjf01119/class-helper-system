package com.example.classhelper;

import com.example.classhelper.entity.Clazz;
import com.example.classhelper.service.ClazzService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FixClassNameRunner implements CommandLineRunner {

    private final ClazzService clazzService;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        fixDatabaseSchema();
        fixClassNames();
    }

    private void fixDatabaseSchema() {
        log.info("开始检查并修复数据库表结构...");
        try {
            // 补齐 sys_class 兼容字段
            checkAndAddColumn("sys_class", "invite_code", "VARCHAR(255) DEFAULT NULL COMMENT '邀请码'");
            checkAndAddColumn("sys_class", "description", "VARCHAR(500) DEFAULT NULL COMMENT '班级描述'");
            checkAndAddColumn("sys_class", "teacher_id", "BIGINT DEFAULT NULL COMMENT '班主任教师ID'");
            checkAndAddColumn("sys_class", "max_students", "INT DEFAULT 50 COMMENT '最大学生数'");
            checkAndAddColumn("sys_class", "current_count", "INT DEFAULT 0 COMMENT '当前学生数'");
            checkAndAddColumn("sys_class", "status", "INT DEFAULT 1 COMMENT '状态：0-禁用，1-启用'");
            checkAndAddColumn("sys_class", "is_deleted", "INT DEFAULT 0 COMMENT '逻辑删除标志'");

            // 补齐 sys_user 兼容字段，避免登录和用户信息查询因旧库缺列失败
            checkAndAddColumn("sys_user", "real_name", "VARCHAR(50) DEFAULT NULL COMMENT '真实姓名'");
            checkAndAddColumn("sys_user", "nickname", "VARCHAR(50) DEFAULT NULL COMMENT '昵称'");
            checkAndAddColumn("sys_user", "avatar_url", "VARCHAR(255) DEFAULT NULL COMMENT '头像URL'");
            checkAndAddColumn("sys_user", "email", "VARCHAR(100) DEFAULT NULL COMMENT '邮箱'");
            checkAndAddColumn("sys_user", "phone", "VARCHAR(20) DEFAULT NULL COMMENT '手机号'");
            checkAndAddColumn("sys_user", "gender", "TINYINT DEFAULT 2 COMMENT '性别：0-女，1-男，2-保密'");
            checkAndAddColumn("sys_user", "status", "TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常'");
            checkAndAddColumn("sys_user", "class_id", "BIGINT DEFAULT NULL COMMENT '所属班级ID（学生）'");
            checkAndAddColumn("sys_user", "last_login_at", "DATETIME DEFAULT NULL COMMENT '最后登录时间'");
            checkAndAddColumn("sys_user", "last_login_ip", "VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP'");
            checkAndAddColumn("sys_user", "is_deleted", "TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'");

            // 补齐 sys_course 扩展字段，支持教师端课程详情与维护能力
            checkAndAddColumn("sys_course", "course_hours", "INT DEFAULT 32 COMMENT '课时'");
            checkAndAddColumn("sys_course", "cover_url", "VARCHAR(255) DEFAULT NULL COMMENT '课程封面'");
            checkAndAddColumn("sys_course", "start_time", "DATETIME DEFAULT NULL COMMENT '开课时间'");
            checkAndAddColumn("sys_course", "end_time", "DATETIME DEFAULT NULL COMMENT '结课时间'");

            // 补齐 assignment 兼容字段，避免课程详情/作业管理在旧库上因缺列失败
            checkAndAddColumn("assignment", "course_id", "BIGINT DEFAULT NULL COMMENT '课程ID'");
            checkAndAddColumn("assignment", "content", "TEXT COMMENT '作业内容'");
            checkAndAddColumn("assignment", "type", "TINYINT DEFAULT 1 COMMENT '类型：1-作业，2-测验'");
            checkAndAddColumn("assignment", "max_score", "INT DEFAULT 100 COMMENT '总分'");
            checkAndAddColumn("assignment", "start_time", "DATETIME DEFAULT NULL COMMENT '开始时间'");
            checkAndAddColumn("assignment", "end_time", "DATETIME DEFAULT NULL COMMENT '截止时间'");
            checkAndAddColumn("assignment", "attachments", "TEXT COMMENT '附件信息'");
            checkAndAddColumn("assignment", "file_url", "TEXT COMMENT '附件列表JSON'");
            checkAndAddColumn("assignment", "status", "TINYINT DEFAULT 1 COMMENT '状态：0-草稿，1-已发布，2-已关闭'");
            checkAndAddColumn("assignment", "submit_count", "INT DEFAULT 0 COMMENT '已提交人数'");
            checkAndAddColumn("assignment", "graded_count", "INT DEFAULT 0 COMMENT '已批改人数'");
            checkAndAddColumn("assignment", "is_deleted", "TINYINT DEFAULT 0 COMMENT '逻辑删除标志'");

            // 补齐 assignment_submission 兼容字段，避免作业统计与提交详情查询失败
            checkAndAddColumn("assignment_submission", "content", "TEXT COMMENT '提交内容'");
            checkAndAddColumn("assignment_submission", "attachments", "TEXT COMMENT '提交附件'");
            checkAndAddColumn("assignment_submission", "feedback", "TEXT COMMENT '教师评语'");
            checkAndAddColumn("assignment_submission", "graded_by", "BIGINT DEFAULT NULL COMMENT '批改教师ID'");
            checkAndAddColumn("assignment_submission", "graded_at", "DATETIME DEFAULT NULL COMMENT '批改时间'");
            checkAndAddColumn("assignment_submission", "submit_time", "DATETIME DEFAULT NULL COMMENT '提交时间'");
            checkAndAddColumn("assignment_submission", "status", "TINYINT DEFAULT 0 COMMENT '状态：0-待批改，1-已批改'");
            checkAndAddColumn("assignment_submission", "is_deleted", "TINYINT DEFAULT 0 COMMENT '逻辑删除标志'");

        } catch (Exception e) {
            log.error("修复数据库表结构失败: {}", e.getMessage());
        }
    }

    private void checkAndAddColumn(String tableName, String columnName, String columnDefinition) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = ? AND column_name = ? AND table_schema = DATABASE()",
                Integer.class, tableName, columnName
        );
        if (count != null && count == 0) {
            log.info("检测到 {} 表缺少 {} 列，正在添加...", tableName, columnName);
            jdbcTemplate.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition);
            log.info("成功添加 {} 列", columnName);
        }
    }

    private void fixClassNames() {
        log.info("开始修复班级名称乱码...");
        try {
            List<Clazz> list = clazzService.list();
            for (Clazz clazz : list) {
                if (clazz.getId() == 1L) {
                    clazz.setClassName("计算机科学与技术1班");
                    clazzService.updateById(clazz);
                    log.info("修复班级 ID 1: {}", clazz.getClassName());
                } else if (clazz.getId() == 2L) {
                    clazz.setClassName("软件工程1班");
                    clazzService.updateById(clazz);
                    log.info("修复班级 ID 2: {}", clazz.getClassName());
                }
            }
            log.info("班级名称修复完成！");
        } catch (Exception e) {
            log.error("修复班级名称失败: {}", e.getMessage());
        }
    }
}
