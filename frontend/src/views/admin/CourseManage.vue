<template>
  <div class="course-manage">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="关键字">
          <el-input v-model="queryParams.keyword" placeholder="课程名称" clearable />
        </el-form-item>
        <el-form-item label="教师">
          <el-select v-model="queryParams.teacherId" placeholder="全部教师" clearable>
            <el-option v-for="item in teacherList" :key="item.id" :label="item.realName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级">
          <el-select v-model="queryParams.classId" placeholder="全部班级" clearable>
            <el-option v-for="item in classList" :key="item.id" :label="item.className" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新增课程</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table :data="courseList" v-loading="loading" border>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="courseName" label="课程名称" min-width="150" />
        <el-table-column prop="teacherName" label="授课教师" min-width="120" />
        <el-table-column prop="className" label="所属班级" min-width="120" />
        <el-table-column prop="description" label="课程描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          :current-page="queryParams.current"
          :page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="formData.courseName" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="授课教师" prop="teacherId">
          <el-select v-model="formData.teacherId" placeholder="请选择授课教师" @change="handleTeacherChange">
            <el-option v-for="item in teacherList" :key="item.id" :label="item.realName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属班级" prop="classId">
          <el-select v-model="formData.classId" placeholder="请选择所属班级">
            <el-option v-for="item in availableClassList" :key="item.id" :label="item.className" :value="item.id" />
          </el-select>
          <div class="form-tip">选择教师后，只显示该教师已绑定管理的班级</div>
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入课程描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCourseList, addCourse, updateCourse, deleteCourse, type CourseVO, type CourseDTO } from '@/api/course'
import { getUserList, type UserVO } from '@/api/user'
import { getAllClasses, type ClassVO } from '@/api/class'

const loading = ref(false)
const courseList = ref<CourseVO[]>([])
const teacherList = ref<UserVO[]>([])
const classList = ref<ClassVO[]>([])
const total = ref(0)

const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: '',
  teacherId: undefined as number | undefined,
  classId: undefined as number | undefined
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑课程' : '新增课程')
const formRef = ref()

const formData = reactive<CourseDTO>({
  courseName: '',
  teacherId: undefined as number | undefined,
  classId: undefined as number | undefined,
  description: '',
  status: 1
})

const availableClassList = computed(() => {
  const teacher = teacherList.value.find(item => item.id === formData.teacherId)
  if (!formData.teacherId) {
    return classList.value
  }
  const classIds = teacher?.classIds || []
  if (classIds.length === 0) {
    return []
  }
  return classList.value.filter(item => classIds.includes(item.id))
})

const formRules = {
  courseName: [
    { required: true, message: '请输入课程名称', trigger: 'blur' }
  ],
  teacherId: [
    { required: true, message: '请选择授课教师', trigger: 'change' }
  ],
  classId: [
    { required: true, message: '请选择所属班级', trigger: 'change' }
  ]
}

const loadCourseList = async () => {
  loading.value = true
  try {
    const res = await getCourseList(queryParams)
    courseList.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const loadTeacherList = async () => {
  try {
    const res = await getUserList({ current: 1, size: 100, role: 'teacher' })
    teacherList.value = res.records
  } catch (error) {
    console.error('加载教师列表失败', error)
  }
}

const handleTeacherChange = () => {
  if (!availableClassList.value.some(item => item.id === formData.classId)) {
    formData.classId = undefined
  }
}

const loadClassList = async () => {
  try {
    const res = await getAllClasses()
    classList.value = res
  } catch (error) {
    console.error('加载班级列表失败', error)
  }
}

const handleSearch = () => {
  queryParams.current = 1
  loadCourseList()
}

const handleReset = () => {
  queryParams.keyword = ''
  queryParams.teacherId = undefined
  queryParams.classId = undefined
  queryParams.current = 1
  loadCourseList()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(formData, {
    id: undefined,
    courseName: '',
    teacherId: undefined,
    classId: undefined,
    description: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row: CourseVO) => {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    courseName: row.courseName,
    teacherId: row.teacherId,
    classId: row.classId,
    description: row.description || '',
    status: row.status
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    if (isEdit.value) {
      await updateCourse(formData)
      ElMessage.success('更新成功')
    } else {
      await addCourse(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadCourseList()
  } catch (error) {
    console.error('保存失败', error)
  }
}

const handleDelete = (row: CourseVO) => {
  ElMessageBox.confirm(`确定删除课程 "${row.courseName}" 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteCourse(row.id)
    ElMessage.success('删除成功')
    loadCourseList()
  }).catch(() => {})
}

const handleSizeChange = (val: number) => {
  queryParams.size = val
  loadCourseList()
}

const handleCurrentChange = (val: number) => {
  queryParams.current = val
  loadCourseList()
}

onMounted(() => {
  loadCourseList()
  loadTeacherList()
  loadClassList()
})

watch(availableClassList, (value) => {
  if (value.length > 0 && !value.some(item => item.id === formData.classId)) {
    formData.classId = undefined
  }
})
</script>

<style scoped>
.course-manage {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}
</style>
