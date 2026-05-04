<template>
  <div class="grade-manage">
    <el-card shadow="never" class="filter-card">
      <el-form :model="filters" inline>
        <el-form-item label="班级">
          <el-select
            v-model="filters.classId"
            placeholder="请选择班级"
            clearable
            style="width: 220px"
            @change="handleClassChange"
          >
            <el-option
              v-for="item in classOptions"
              :key="item.id"
              :label="item.className"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="作业">
          <el-select
            v-model="filters.assignmentId"
            placeholder="请选择作业"
            clearable
            style="width: 260px"
            @change="handleQuery"
          >
            <el-option
              v-for="item in assignmentOptions"
              :key="item.id"
              :label="item.title"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card shadow="never" class="stat-item">
          <div class="stat-title">提交人数</div>
          <div class="stat-value">{{ stats.submittedCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-item">
          <div class="stat-title">已评分人数</div>
          <div class="stat-value">{{ stats.gradedCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-item">
          <div class="stat-title">班级均分</div>
          <div class="stat-value">{{ stats.avgScore }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-item">
          <div class="stat-title">及格率</div>
          <div class="stat-value">{{ stats.passRate }}%</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" label="序号" width="64" align="center" />
        <el-table-column prop="assignmentTitle" label="作业标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="studentNo" label="学号" width="140" />
        <el-table-column prop="className" label="班级" width="140" />
        <el-table-column prop="submitTime" label="提交时间" width="170" />
        <el-table-column label="得分" width="100" align="center">
          <template #default="{ row }">
            {{ row.score ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '已评分' : '待评分' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && tableData.length === 0" description="暂无成绩数据" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import { getMyTeacherClasses, type ClassVO } from '@/api/class'
import { getClassStudents, type UserVO } from '@/api/user'
import { getHomeworkListByClassId, getHomeworkStats, type AssignmentStatsVO } from '@/api/homework'

interface AssignmentVO {
  id: number
  title: string
  classId?: number
}

interface SubmissionVO {
  id: number
  assignmentId: number
  studentId: number
  classId: number
  score?: number
  status: number
  submitTime?: string
}

interface SubmissionPage {
  records?: SubmissionVO[]
}

interface GradeRow {
  assignmentTitle: string
  studentName: string
  studentNo: string
  className: string
  submitTime: string
  score?: number
  status: number
}

const loading = ref(false)
const route = useRoute()
const classOptions = ref<ClassVO[]>([])
const assignmentOptions = ref<AssignmentVO[]>([])
const tableData = ref<GradeRow[]>([])

const filters = reactive({
  classId: undefined as number | undefined,
  assignmentId: undefined as number | undefined
})

const stats = reactive({
  submittedCount: 0,
  gradedCount: 0,
  avgScore: '0.0',
  passRate: '0.0'
})

const formatTime = (time?: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const resetStats = () => {
  stats.submittedCount = 0
  stats.gradedCount = 0
  stats.avgScore = '0.0'
  stats.passRate = '0.0'
}

const loadClassOptions = async () => {
  classOptions.value = (await getMyTeacherClasses()) || []
}

const resetGradeView = () => {
  assignmentOptions.value = []
  filters.assignmentId = undefined
  tableData.value = []
  resetStats()
}

const loadAssignmentsByClass = async (classId: number, preferredAssignmentId?: number) => {
  const list = (await getHomeworkListByClassId(classId)) || []
  assignmentOptions.value = list
  if (list.length === 0) {
    filters.assignmentId = undefined
    return
  }
  const matchedAssignment = preferredAssignmentId
    ? list.find(item => item.id === preferredAssignmentId)
    : undefined
  filters.assignmentId = matchedAssignment?.id || list[0].id
}

const applyStats = (statsRes?: AssignmentStatsVO) => {
  stats.submittedCount = statsRes?.submittedCount || 0
  stats.gradedCount = statsRes?.gradedCount || 0
  stats.avgScore = statsRes?.avgScore || '0.0'
  stats.passRate = statsRes?.passRate || '0.0'
}

const handleQuery = async () => {
  if (!filters.classId || !filters.assignmentId) {
    tableData.value = []
    resetStats()
    return
  }

  loading.value = true
  try {
    const [students, submissionPage, statsRes] = await Promise.all([
      getClassStudents(filters.classId),
      request.get<SubmissionPage>(`/assignment/${filters.assignmentId}/submissions`, {
        params: { current: 1, size: 500 }
      }),
      getHomeworkStats(filters.assignmentId)
    ])

    const studentMap = new Map<number, UserVO>((students || []).map(item => [item.id, item]))
    const className = classOptions.value.find(item => item.id === filters.classId)?.className || '暂无数据'
    const assignmentTitle = assignmentOptions.value.find(item => item.id === filters.assignmentId)?.title || '暂无数据'
    const submissions = submissionPage?.records || []

    applyStats(statsRes)
    tableData.value = submissions.map(item => {
      const student = studentMap.get(item.studentId)
      return {
        assignmentTitle,
        studentName: student?.realName || '未知学生',
        studentNo: student?.username || '暂无数据',
        className,
        submitTime: formatTime(item.submitTime),
        score: item.score,
        status: item.status
      }
    })
  } finally {
    loading.value = false
  }
}

const handleClassChange = async (classId?: number) => {
  resetGradeView()
  if (!classId) {
    return
  }
  await loadAssignmentsByClass(classId)
  if (filters.assignmentId) {
    await handleQuery()
  }
}

const handleReset = async () => {
  filters.classId = undefined
  resetGradeView()
}

onMounted(async () => {
  await loadClassOptions()
  const routeClassId = Number(route.query.classId)
  if (!Number.isNaN(routeClassId) && routeClassId > 0) {
    filters.classId = routeClassId
    const routeAssignmentId = Number(route.query.assignmentId)
    await loadAssignmentsByClass(
      routeClassId,
      !Number.isNaN(routeAssignmentId) && routeAssignmentId > 0 ? routeAssignmentId : undefined
    )
    if (filters.assignmentId) {
      await handleQuery()
    }
  }
})
</script>

<style scoped>
.grade-manage {
  padding: 0;
}

.filter-card {
  margin-bottom: 16px;
}

.stats-row {
  margin-bottom: 16px;
}

.stat-item {
  border-radius: 10px;
}

.stat-title {
  font-size: 13px;
  color: #909399;
}

.stat-value {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}
</style>
