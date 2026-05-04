USE class_helper_db;

-- Insert new classes
INSERT IGNORE INTO sys_class (id, class_name, invite_code, description, max_students, current_count, status) VALUES (101, '计算机网络', 'NET2024', '计算机网络基础课程', 50, 20, 1);
INSERT IGNORE INTO sys_class (id, class_name, invite_code, description, max_students, current_count, status) VALUES (102, '操作系统', 'OS2024', '操作系统原理与实践', 50, 20, 1);
INSERT IGNORE INTO sys_class (id, class_name, invite_code, description, max_students, current_count, status) VALUES (103, '数据库原理', 'DB2024', '数据库系统概论', 50, 20, 1);
INSERT IGNORE INTO sys_class (id, class_name, invite_code, description, max_students, current_count, status) VALUES (104, '软件工程', 'SE2024', '软件工程与项目管理', 50, 20, 1);
INSERT IGNORE INTO sys_class (id, class_name, invite_code, description, max_students, current_count, status) VALUES (105, '人工智能', 'AI2024', '人工智能导论', 50, 20, 1);
INSERT IGNORE INTO sys_class (id, class_name, invite_code, description, max_students, current_count, status) VALUES (106, '数据结构', 'DS2024', '数据结构与算法分析', 50, 20, 1);
INSERT IGNORE INTO sys_class (id, class_name, invite_code, description, max_students, current_count, status) VALUES (107, '编译原理', 'CP2024', '编译技术与实践', 50, 20, 1);
INSERT IGNORE INTO sys_class (id, class_name, invite_code, description, max_students, current_count, status) VALUES (108, '计算机组成原理', 'COA2024', '计算机组成与体系结构', 50, 20, 1);
INSERT IGNORE INTO sys_class (id, class_name, invite_code, description, max_students, current_count, status) VALUES (109, 'Web开发技术', 'WEB2024', 'Web前端与后端开发', 50, 20, 1);
INSERT IGNORE INTO sys_class (id, class_name, invite_code, description, max_students, current_count, status) VALUES (110, '移动应用开发', 'MOB2024', '移动应用开发技术', 50, 20, 1);
INSERT IGNORE INTO sys_class (id, class_name, invite_code, description, max_students, current_count, status) VALUES (111, '网络安全', 'SEC2024', '网络安全与密码学', 50, 20, 1);
INSERT IGNORE INTO sys_class (id, class_name, invite_code, description, max_students, current_count, status) VALUES (112, '云计算', 'CLOUD2024', '云计算与分布式系统', 50, 20, 1);
INSERT IGNORE INTO sys_class (id, class_name, invite_code, description, max_students, current_count, status) VALUES (113, '大数据技术', 'BIGDATA2024', '大数据处理与分析', 50, 20, 1);
INSERT IGNORE INTO sys_class (id, class_name, invite_code, description, max_students, current_count, status) VALUES (114, '机器学习', 'ML2024', '机器学习理论与实践', 50, 20, 1);
INSERT IGNORE INTO sys_class (id, class_name, invite_code, description, max_students, current_count, status) VALUES (115, '软件测试', 'TEST2024', '软件测试与质量保证', 50, 20, 1);

-- Insert 5 new teachers
INSERT IGNORE INTO sys_user (id, username, password, real_name, nickname, gender, status, class_id) VALUES (201, 'teacher_wang', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '王明', '王老师', 1, 1, NULL);
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (201, 2);
INSERT IGNORE INTO sys_user (id, username, password, real_name, nickname, gender, status, class_id) VALUES (202, 'teacher_li', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '李华', '李老师', 1, 1, NULL);
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (202, 2);
INSERT IGNORE INTO sys_user (id, username, password, real_name, nickname, gender, status, class_id) VALUES (203, 'teacher_zhang', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '张伟', '张老师', 1, 1, NULL);
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (203, 2);
INSERT IGNORE INTO sys_user (id, username, password, real_name, nickname, gender, status, class_id) VALUES (204, 'teacher_liu', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '刘芳', '刘老师', 2, 1, NULL);
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (204, 2);
INSERT IGNORE INTO sys_user (id, username, password, real_name, nickname, gender, status, class_id) VALUES (205, 'teacher_chen', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '陈静', '陈老师', 2, 1, NULL);
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (205, 2);

-- Insert teacher-class relations (many-to-many)
INSERT IGNORE INTO sys_teacher_class (teacher_id, class_id, is_deleted) VALUES
(201, 101, 0), (201, 102, 0), (201, 105, 0), (201, 106, 0), (201, 109, 0), (201, 112, 0), (201, 115, 0),
(202, 101, 0), (202, 102, 0), (202, 105, 0), (202, 106, 0), (202, 109, 0), (202, 112, 0), (202, 115, 0),
(203, 103, 0), (203, 104, 0), (203, 107, 0), (203, 108, 0), (203, 110, 0), (203, 111, 0), (203, 113, 0), (203, 114, 0),
(204, 101, 0), (204, 103, 0),
(205, 104, 0), (205, 105, 0);

-- Insert 60 new students
INSERT IGNORE INTO sys_user (id, username, password, real_name, nickname, gender, status, class_id) VALUES
(1001, 'student_1001', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '郑秀兰', '小兰', 1, 1, 101),
(1002, 'student_1002', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '孙杰', '小杰', 1, 1, 101),
(1003, 'student_1003', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '马明', '小明', 1, 1, 101),
(1004, 'student_1004', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '高军', '小军', 2, 1, 101),
(1005, 'student_1005', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '徐强', '小强', 2, 1, 101),
(1006, 'student_1006', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '何静', '小静', 1, 1, 101),
(1007, 'student_1007', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '徐敏', '小敏', 2, 1, 101),
(1008, 'student_1008', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '赵刚', '小刚', 1, 1, 101),
(1009, 'student_1009', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '胡杰', '小杰', 1, 1, 101),
(1010, 'student_1010', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '陈霞', '小霞', 1, 1, 101),
(1011, 'student_1011', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '高娜', '小娜', 1, 1, 101),
(1012, 'student_1012', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '郑静', '小静', 1, 1, 101),
(1013, 'student_1013', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '高强', '小强', 1, 1, 102),
(1014, 'student_1014', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '郭勇', '小勇', 1, 1, 102),
(1015, 'student_1015', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '黄涛', '小涛', 2, 1, 102),
(1016, 'student_1016', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '黄涛', '小涛', 1, 1, 102),
(1017, 'student_1017', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '周秀英', '小英', 1, 1, 102),
(1018, 'student_1018', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '王静', '小静', 1, 1, 102),
(1019, 'student_1019', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '陈刚', '小刚', 1, 1, 102),
(1020, 'student_1020', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '周伟', '小伟', 1, 1, 102),
(1021, 'student_1021', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '朱秀兰', '小兰', 1, 1, 102),
(1022, 'student_1022', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '朱超', '小超', 1, 1, 102),
(1023, 'student_1023', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '郑芳', '小芳', 1, 1, 102),
(1024, 'student_1024', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '胡勇', '小勇', 1, 1, 102),
(1025, 'student_1025', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '刘秀兰', '小兰', 1, 1, 103),
(1026, 'student_1026', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '黄磊', '小磊', 2, 1, 103),
(1027, 'student_1027', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '郑秀英', '小英', 1, 1, 103),
(1028, 'student_1028', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '刘杰', '小杰', 2, 1, 103),
(1029, 'student_1029', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '刘杰', '小杰', 1, 1, 103),
(1030, 'student_1030', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '徐杰', '小杰', 2, 1, 103),
(1031, 'student_1031', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '杨杰', '小杰', 2, 1, 103),
(1032, 'student_1032', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '孙超', '小超', 2, 1, 103),
(1033, 'student_1033', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '王艳', '小艳', 1, 1, 103),
(1034, 'student_1034', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '孙伟', '小伟', 1, 1, 103),
(1035, 'student_1035', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '朱平', '小平', 2, 1, 103),
(1036, 'student_1036', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '胡磊', '小磊', 2, 1, 103),
(1037, 'student_1037', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '马刚', '小刚', 2, 1, 104),
(1038, 'student_1038', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '徐秀兰', '小兰', 1, 1, 104),
(1039, 'student_1039', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '李洋', '小洋', 2, 1, 104),
(1040, 'student_1040', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '谢霞', '小霞', 1, 1, 104),
(1041, 'student_1041', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '黄静', '小静', 2, 1, 104),
(1042, 'student_1042', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '李磊', '小磊', 2, 1, 104),
(1043, 'student_1043', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '何平', '小平', 2, 1, 104),
(1044, 'student_1044', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '林娜', '小娜', 2, 1, 104),
(1045, 'student_1045', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '赵霞', '小霞', 1, 1, 104),
(1046, 'student_1046', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '徐桂英', '小英', 2, 1, 104),
(1047, 'student_1047', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '郑军', '小军', 1, 1, 104),
(1048, 'student_1048', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '郑勇', '小勇', 1, 1, 104),
(1049, 'student_1049', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '李艳', '小艳', 2, 1, 105),
(1050, 'student_1050', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '孙伟', '小伟', 1, 1, 105),
(1051, 'student_1051', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '郑娜', '小娜', 1, 1, 105),
(1052, 'student_1052', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '林静', '小静', 2, 1, 105),
(1053, 'student_1053', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '李杰', '小杰', 2, 1, 105),
(1054, 'student_1054', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '李军', '小军', 2, 1, 105),
(1055, 'student_1055', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '张秀兰', '小兰', 1, 1, 105),
(1056, 'student_1056', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '王勇', '小勇', 2, 1, 105),
(1057, 'student_1057', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '李秀英', '小英', 2, 1, 105),
(1058, 'student_1058', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '郭洋', '小洋', 1, 1, 105),
(1059, 'student_1059', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '周平', '小平', 2, 1, 105),
(1060, 'student_1060', '$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei', '黄秀英', '小英', 1, 1, 105);

-- Insert user roles for students
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES
(1001, 3),
(1002, 3),
(1003, 3),
(1004, 3),
(1005, 3),
(1006, 3),
(1007, 3),
(1008, 3),
(1009, 3),
(1010, 3),
(1011, 3),
(1012, 3),
(1013, 3),
(1014, 3),
(1015, 3),
(1016, 3),
(1017, 3),
(1018, 3),
(1019, 3),
(1020, 3),
(1021, 3),
(1022, 3),
(1023, 3),
(1024, 3),
(1025, 3),
(1026, 3),
(1027, 3),
(1028, 3),
(1029, 3),
(1030, 3),
(1031, 3),
(1032, 3),
(1033, 3),
(1034, 3),
(1035, 3),
(1036, 3),
(1037, 3),
(1038, 3),
(1039, 3),
(1040, 3),
(1041, 3),
(1042, 3),
(1043, 3),
(1044, 3),
(1045, 3),
(1046, 3),
(1047, 3),
(1048, 3),
(1049, 3),
(1050, 3),
(1051, 3),
(1052, 3),
(1053, 3),
(1054, 3),
(1055, 3),
(1056, 3),
(1057, 3),
(1058, 3),
(1059, 3),
(1060, 3);
-- Insert 6 system announcements
INSERT INTO sys_announcement (title, content, publisher_id, is_top, scope, status) VALUES ('开学通知', '新学期即将开始，请各位同学做好上课准备，按时到校报到。', 1, 0, 1, 1);
INSERT INTO sys_announcement (title, content, publisher_id, is_top, scope, status) VALUES ('系统维护公告', '本系统将于本周六晚上10点至周日凌晨2点进行维护，期间可能无法正常访问。', 1, 0, 1, 1);
INSERT INTO sys_announcement (title, content, publisher_id, is_top, scope, status) VALUES ('作业提交提醒', '请同学们注意作业提交截止时间，逾期将影响平时成绩。', 1, 0, 1, 1);
INSERT INTO sys_announcement (title, content, publisher_id, is_top, scope, status) VALUES ('期末考试安排', '期末考试时间已确定，请同学们合理安排复习时间。', 1, 0, 1, 1);
INSERT INTO sys_announcement (title, content, publisher_id, is_top, scope, status) VALUES ('假期安排通知', '根据学校安排，国庆节假期为10月1日至10月7日。', 1, 0, 1, 1);
INSERT INTO sys_announcement (title, content, publisher_id, is_top, scope, status) VALUES ('校园活动预告', '下周将举办校园科技文化节，欢迎同学们积极参与。', 1, 0, 1, 1);
