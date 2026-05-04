<template>
  <div class="grade-query">
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="label">已提交作业</div>
          <div class="value">{{ stats.submittedCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="label">已评分作业</div>
          <div class="value">{{ stats.gradedCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="label">作业平均分</div>
          <div class="value">{{ stats.avgScore }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="label">及格率</div>
          <div class="value">{{ stats.passRate }}%</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-title">作业成绩</div>
      </template>
      <el-table :data="displayHomeworkRows" v-loading="loading" border stripe>
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="title" label="作业标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="courseName" label="课程" width="180" />
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column label="成绩" width="120" align="center">
          <template #default="{ row }">
            {{ row.score ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="maxScore" label="满分" width="100" align="center" />
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.score == null ? 'warning' : 'success'">
              {{ row.score == null ? '待批改' : '已评分' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && displayHomeworkRows.length === 0" description="暂无成绩数据" />
    </el-card>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-title">课程成绩统计</div>
      </template>
      <el-table :data="displayCourseRows" border stripe>
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="courseName" label="课程名称" min-width="200" />
        <el-table-column prop="gradedCount" label="已评分次数" width="120" align="center" />
        <el-table-column prop="avgScore" label="平均分" width="120" align="center" />
        <el-table-column prop="maxScore" label="最高分" width="120" align="center" />
        <el-table-column prop="minScore" label="最低分" width="120" align="center" />
      </el-table>
      <el-empty v-if="displayCourseRows.length === 0" description="暂无课程成绩统计" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getStudentHomeworkList, type HomeworkVO } from '@/api/homework'
import { getStudentReportOverview, type StudentCourseGradeStat } from '@/api/dashboard'

const route = useRoute()
const loading = ref(false)
const homeworkRows = ref<HomeworkVO[]>([])
const courseRows = ref<StudentCourseGradeStat[]>([])
const selectedCourseId = ref<number>()

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

const displayHomeworkRows = computed(() => {
  if (!selectedCourseId.value) {
    return homeworkRows.value
  }
  return homeworkRows.value.filter((item) => item.courseId === selectedCourseId.value)
})

const displayCourseRows = computed(() => {
  if (!selectedCourseId.value) {
    return courseRows.value
  }
  return courseRows.value.filter((item) => item.courseId === selectedCourseId.value)
})

const loadData = async () => {
  loading.value = true
  try {
    const [homeworkRes, reportRes] = await Promise.all([
      getStudentHomeworkList(1),
      getStudentReportOverview()
    ])
    homeworkRows.value = (homeworkRes || []).map(item => ({
      ...item,
      submitTime: formatTime(item.submitTime)
    }))
    stats.submittedCount = reportRes?.stats?.submittedCount ?? 0
    stats.gradedCount = reportRes?.stats?.gradedCount ?? 0
    stats.avgScore = String(reportRes?.stats?.avgScore ?? 0)
    stats.passRate = String(reportRes?.stats?.passRate ?? 0)
    courseRows.value = reportRes?.courseStats || []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const courseId = Number(route.query.courseId)
  if (!Number.isNaN(courseId) && courseId > 0) {
    selectedCourseId.value = courseId
  }
  loadData()
})
</script>

<style scoped>
.grade-query {
  padding: 0;
}

.stats-row {
  margin-bottom: 16px;
}

.stat-card {
  border-radius: 10px;
}

.label {
  color: #909399;
  font-size: 13px;
}

.value {
  margin-top: 8px;
  font-size: 24px;
  color: #303133;
  font-weight: 700;
}

.table-card {
  margin-bottom: 16px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
</style>
