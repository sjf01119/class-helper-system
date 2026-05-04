<template>
  <div class="my-class">
    <!-- 班级信息卡片 -->
    <el-card class="class-info-card" shadow="never" v-if="currentClass">
      <div class="class-header">
        <div class="class-title">
          <h2>{{ currentClass.className }}</h2>
          <el-tag type="success" size="small">我的班级</el-tag>
        </div>
        <div class="class-meta">
          <el-select
            v-if="myClasses.length > 1"
            v-model="queryParams.classId"
            size="small"
            style="width: 180px"
            @change="handleClassChange"
          >
            <el-option
              v-for="item in myClasses"
              :key="item.id"
              :label="item.className"
              :value="item.id"
            />
          </el-select>
          <span class="meta-item">
            <el-icon><User /></el-icon>
            学生 {{ studentTotal }} 人
          </span>
          <span class="meta-item">
            <el-icon><Key /></el-icon>
            邀请码：
            <span class="invite-code">{{ currentClass.inviteCode }}</span>
            <el-button link type="primary" size="small" @click="copyCode">复制</el-button>
          </span>
        </div>
      </div>
    </el-card>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="关键字">
          <el-input 
            v-model="queryParams.keyword" 
            placeholder="学号/姓名" 
            clearable 
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 学生列表 -->
    <el-card class="table-card" shadow="never">
      <el-table 
        :data="studentList" 
        v-loading="loading" 
        border 
        stripe
        highlight-current-row
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="username" label="学号" min-width="120" />
        <el-table-column prop="realName" label="姓名" min-width="100" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="加入时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              <el-icon><View /></el-icon>查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          :current-page="queryParams.current"
          :page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 学生详情弹窗 -->
    <el-dialog title="学生详情" v-model="viewDialogVisible" width="500px">
      <div class="student-detail" v-if="currentStudent">
        <div class="detail-item">
          <span class="label">学号：</span>
          <span class="value">{{ currentStudent.username }}</span>
        </div>
        <div class="detail-item">
          <span class="label">姓名：</span>
          <span class="value">{{ currentStudent.realName }}</span>
        </div>
        <div class="detail-item">
          <span class="label">手机号：</span>
          <span class="value">{{ currentStudent.phone || '-' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">邮箱：</span>
          <span class="value">{{ currentStudent.email || '-' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">状态：</span>
          <el-tag :type="currentStudent.status === 1 ? 'success' : 'danger'" size="small">
            {{ currentStudent.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </div>
        <div class="detail-item">
          <span class="label">加入时间：</span>
          <span class="value">{{ formatTime(currentStudent.createdAt) }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, RefreshRight, View, Key, User } from '@element-plus/icons-vue'
import { getMyHeadTeacherClasses, type ClassVO } from '@/api/class'
import { getUserList, type UserVO } from '@/api/user'

const loading = ref(false)
const myClasses = ref<ClassVO[]>([])
const currentClass = ref<ClassVO>()
const studentList = ref<UserVO[]>([])
const total = ref(0)
const studentTotal = ref(0)

const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: '',
  role: 'student',
  classId: undefined as number | undefined,
  status: undefined as number | undefined
})

const viewDialogVisible = ref(false)
const currentStudent = ref<UserVO>()

const formatTime = (time?: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const loadClassInfo = async () => {
  try {
    const res = await getMyHeadTeacherClasses()
    if (res && res.length > 0) {
      myClasses.value = res
      queryParams.classId = res[0].id
      syncCurrentClass()
      loadStudentList()
    } else {
      myClasses.value = []
      currentClass.value = undefined
      queryParams.classId = undefined
      studentTotal.value = 0
    }
  } catch (error) {
    console.error('加载班级信息失败', error)
  }
}

const loadStudentList = async () => {
  if (!queryParams.classId) return
  loading.value = true
  try {
    const res = await getUserList(queryParams)
    studentList.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const copyCode = () => {
  if (!currentClass.value?.inviteCode) return
  navigator.clipboard.writeText(currentClass.value.inviteCode).then(() => {
    ElMessage.success('邀请码已复制')
  })
}

const handleClassChange = () => {
  queryParams.current = 1
  syncCurrentClass()
  loadStudentList()
}

const handleSearch = () => {
  queryParams.current = 1
  loadStudentList()
}

const handleReset = () => {
  queryParams.keyword = ''
  queryParams.status = undefined
  queryParams.current = 1
  loadStudentList()
}

const handleView = (row: UserVO) => {
  currentStudent.value = row
  viewDialogVisible.value = true
}

// 教师端不可修改学生状态

const handleSizeChange = (val: number) => {
  queryParams.size = val
  loadStudentList()
}

const handleCurrentChange = (val: number) => {
  queryParams.current = val
  loadStudentList()
}

onMounted(() => {
  loadClassInfo()
})

const syncCurrentClass = () => {
  currentClass.value = myClasses.value.find(item => item.id === queryParams.classId)
  studentTotal.value = currentClass.value?.currentCount || 0
}
</script>

<style scoped>
.my-class {
  padding: 0;
}

.class-info-card {
  margin-bottom: 16px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.class-info-card :deep(.el-card__body) {
  padding: 20px;
}

.class-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.class-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.class-title h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.class-meta {
  display: flex;
  gap: 24px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.invite-code {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  letter-spacing: 2px;
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 8px;
  border-radius: 4px;
}

.search-card {
  margin-bottom: 16px;
  border-radius: 12px;
}

.table-card {
  border-radius: 12px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.student-detail {
  padding: 10px 0;
}

.detail-item {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
}

.detail-item .label {
  color: #909399;
  width: 80px;
  flex-shrink: 0;
}

.detail-item .value {
  color: #303133;
  font-weight: 500;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}
</style>
