import random

password_hash = "$2a$10$OGaMTNfX5yjb19yqv4lBUeRFSmCJUfV4LMC4iGmZvUsmPuZ5rC.ei" # 123456

sql = ["USE class_helper_db;\n"]

# 1. Insert 15 new classes
classes = [
    ("计算机网络", "NET2024", "计算机网络基础课程", 2),
    ("操作系统", "OS2024", "操作系统原理与实践", 2),
    ("数据库原理", "DB2024", "数据库系统概论", 3),
    ("软件工程", "SE2024", "软件工程与项目管理", 3),
    ("人工智能", "AI2024", "人工智能导论", 2),
    ("数据结构", "DS2024", "数据结构与算法分析", 2),
    ("编译原理", "CP2024", "编译技术与实践", 3),
    ("计算机组成原理", "COA2024", "计算机组成与体系结构", 3),
    ("Web开发技术", "WEB2024", "Web前端与后端开发", 2),
    ("移动应用开发", "MOB2024", "移动应用开发技术", 3),
    ("网络安全", "SEC2024", "网络安全与密码学", 3),
    ("云计算", "CLOUD2024", "云计算与分布式系统", 2),
    ("大数据技术", "BIGDATA2024", "大数据处理与分析", 3),
    ("机器学习", "ML2024", "机器学习理论与实践", 3),
    ("软件测试", "TEST2024", "软件测试与质量保证", 2)
]

sql.append("-- Insert new classes")
for i, (name, code, desc, t_id) in enumerate(classes, start=3): # Assuming id 1, 2 exist, but let's use INSERT without ID to auto increment and then fetch. Wait, better to just hardcode IDs 10 to 14 to avoid conflict.
    pass

# To avoid auto-increment guessing, let's just use INSERT INTO sys_class (class_name...) VALUES (...)
# Then we can just select the IDs back? No, let's just generate the SQL to insert a batch and get max id.
# Even simpler: explicitly assign IDs 101 to 105 for classes, and 1001 to 1050 for students.
class_start_id = 101
student_start_id = 1001

for i, (name, code, desc, t_id) in enumerate(classes):
    c_id = class_start_id + i
    sql.append(f"INSERT INTO sys_class (id, class_name, invite_code, description, teacher_id, max_students, current_count, status) VALUES ({c_id}, '{name}', '{code}', '{desc}', {t_id}, 50, 20, 1);")

# 2. Insert 5 teachers
sql.append("\n-- Insert 5 new teachers")
teacher_data = [
    (201, "teacher_wang", "王老师", "王明", 1),
    (202, "teacher_li", "李老师", "李华", 1), 
    (203, "teacher_zhang", "张老师", "张伟", 1),
    (204, "teacher_liu", "刘老师", "刘芳", 2),
    (205, "teacher_chen", "陈老师", "陈静", 2)
]

for teacher_id, username, nickname, real_name, gender in teacher_data:
    sql.append(f"INSERT INTO sys_user (id, username, password, real_name, nickname, gender, status, class_id) VALUES ({teacher_id}, '{username}', '{password_hash}', '{real_name}', '{nickname}', {gender}, 1, NULL);")
    sql.append(f"INSERT INTO sys_user_role (user_id, role_id) VALUES ({teacher_id}, 2);")

# 3. Insert 60 students
first_names = ["伟", "芳", "娜", "秀英", "敏", "静", "丽", "强", "磊", "军", "洋", "勇", "艳", "杰", "娟", "涛", "明", "超", "秀兰", "霞", "平", "刚", "桂英"]
last_names = ["王", "李", "张", "刘", "陈", "杨", "黄", "赵", "吴", "周", "徐", "孙", "马", "朱", "胡", "郭", "何", "高", "林", "郑", "谢"]

sql.append("\n-- Insert 60 new students")
sql.append("INSERT INTO sys_user (id, username, password, real_name, nickname, gender, status, class_id) VALUES")
user_values = []
for i in range(60):
    s_id = student_start_id + i
    c_id = class_start_id + (i // 12)  # Distribute 12 students per class (60/5=12)
    real_name = random.choice(last_names) + random.choice(first_names)
    username = f"student_{s_id}"
    nickname = f"小{real_name[-1]}"
    gender = random.choice([1, 2])
    user_values.append(f"({s_id}, '{username}', '{password_hash}', '{real_name}', '{nickname}', {gender}, 1, {c_id})")

sql.append(",\n".join(user_values) + ";")

# 4. Insert user roles (role_id = 3 for student)
sql.append("\n-- Insert user roles for students")
sql.append("INSERT INTO sys_user_role (user_id, role_id) VALUES")
role_values = [f"({student_start_id + i}, 3)" for i in range(60)]
sql.append(",\n".join(role_values) + ";")

# 5. Insert student_class associations
sql.append("\n-- Insert student_class associations")
sql.append("INSERT INTO student_class (student_id, class_id, status) VALUES")
sc_values = [f"({student_start_id + i}, {class_start_id + (i // 12)}, 1)" for i in range(60)]
sql.append(",\n".join(sc_values) + ";")

# 6. Insert 6 system announcements
sql.append("\n-- Insert 6 system announcements")
announcements = [
    ("开学通知", "新学期即将开始，请各位同学做好上课准备，按时到校报到。", 1, 0, 1),
    ("系统维护公告", "本系统将于本周六晚上10点至周日凌晨2点进行维护，期间可能无法正常访问。", 1, 0, 1),
    ("作业提交提醒", "请同学们注意作业提交截止时间，逾期将影响平时成绩。", 1, 0, 1),
    ("期末考试安排", "期末考试时间已确定，请同学们合理安排复习时间。", 1, 0, 1),
    ("假期安排通知", "根据学校安排，国庆节假期为10月1日至10月7日。", 1, 0, 1),
    ("校园活动预告", "下周将举办校园科技文化节，欢迎同学们积极参与。", 1, 0, 1)
]

for i, (title, content, publisher_id, is_top, scope) in enumerate(announcements, start=1):
    sql.append(f"INSERT INTO sys_announcement (title, content, publisher_id, is_top, scope, status) VALUES ('{title}', '{content}', {publisher_id}, {is_top}, {scope}, 1);")

with open("generate_data.sql", "w", encoding="utf-8") as f:
    f.write("\n".join(sql))
print("SQL script generated: generate_data.sql")
