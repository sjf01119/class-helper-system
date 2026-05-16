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
            placeholder="全部作业（可不选）"
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
      <div v-loading="loading">
        <template v-if="groupedTableData.length > 0">
          <div v-for="group in groupedTableData" :key="group.assignmentId" class="grade-group">
            <div class="group-title">{{ group.assignmentTitle }}</div>
            <el-table :data="group.records" border stripe>
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
          </div>
        </template>
        <el-empty v-if="!loading && groupedTableData.length === 0" description="暂无成绩数据" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getMyTeacherClasses, type ClassVO } from '@/api/class'
import { getHomeworkListByClassId, getTeacherGradeOverview, type GradeOverviewGroupVO } from '@/api/homework'

interface AssignmentVO {
  id: number
  title: string
  classId?: number
}

const loading = ref(false)
const route = useRoute()
const classOptions = ref<ClassVO[]>([])
const assignmentOptions = ref<AssignmentVO[]>([])
const groupedTableData = ref<GradeOverviewGroupVO[]>([])

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
  groupedTableData.value = []
  resetStats()
}

const loadAssignmentsByClass = async (classId: number, preferredAssignmentId?: number) => {
  const list = (await getHomeworkListByClassId(classId)) || []
  assignmentOptions.value = list
  const matchedAssignment = preferredAssignmentId ? list.find(item => item.id === preferredAssignmentId) : undefined
  filters.assignmentId = matchedAssignment?.id
}

const applyStats = (statsRes?: { submittedCount: number; gradedCount: number; avgScore: string; passRate: string }) => {
  stats.submittedCount = statsRes?.submittedCount || 0
  stats.gradedCount = statsRes?.gradedCount || 0
  stats.avgScore = statsRes?.avgScore || '0.0'
  stats.passRate = statsRes?.passRate || '0.0'
}

const handleQuery = async () => {
  if (!filters.classId) {
    groupedTableData.value = []
    resetStats()
    return
  }

  loading.value = true
  try {
    const overview = await getTeacherGradeOverview({
      classId: filters.classId,
      assignmentId: filters.assignmentId
    })
    applyStats(overview?.stats)
    groupedTableData.value = (overview?.groups || []).map(group => ({
      ...group,
      records: (group.records || []).map(record => ({
        ...record,
        submitTime: formatTime(record.submitTime)
      }))
    }))
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
  await handleQuery()
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
    await handleQuery()
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

.grade-group + .grade-group {
  margin-top: 20px;
}

.group-title {
  margin-bottom: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
</style>
