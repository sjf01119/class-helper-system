<template>
  <div class="course-detail-page" v-loading="loading">
    <el-card class="overview-card" shadow="never">
      <div class="overview-top">
        <div class="overview-main">
          <el-button text @click="goBack">返回课程列表</el-button>
          <h2 class="course-title">{{ courseDetail?.courseName || '课程详情' }}</h2>
          <div class="course-subtitle">
            <span>{{ courseDetail?.className || '未关联班级' }}</span>
            <span>{{ teacherName }}</span>
          </div>
        </div>
      </div>

      <div class="metric-grid">
        <div class="metric-card">
          <span class="metric-label">学生人数</span>
          <strong>{{ studentCount }}</strong>
        </div>
        <div class="metric-card">
          <span class="metric-label">作业总数</span>
          <strong>{{ assignmentCount }}</strong>
        </div>
        <div class="metric-card">
          <span class="metric-label">平均完成率</span>
          <strong>{{ overallCompletionRate }}%</strong>
        </div>
        <div class="metric-card">
          <span class="metric-label">课程平均分</span>
          <strong>{{ overallAverageScore }}</strong>
        </div>
      </div>
    </el-card>

    <el-row :gutter="16">
      <el-col :lg="16" :xs="24">
        <el-card class="detail-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div>
                <div class="header-title">课程概况</div>
                <div class="header-subtitle">补充课程关键信息，帮助快速判断课程状态和活跃度</div>
              </div>
            </div>
          </template>

          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">所属班级</span>
              <span class="info-value">{{ courseDetail?.className || '暂无数据' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">学分 / 课时</span>
              <span class="info-value">{{ courseDetail?.credit ?? 2 }} 学分 / {{ courseDetail?.courseHours ?? 32 }} 课时</span>
            </div>
            <div class="info-item">
              <span class="info-label">课程状态</span>
              <span class="info-value">{{ courseDetail?.status === 1 ? '进行中' : '已结课' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">结课时间</span>
              <span class="info-value">{{ formatDateTime(courseDetail?.endTime) }}</span>
            </div>
          </div>

          <div class="desc-panel">
            <div class="desc-title">课程简介</div>
            <div class="desc-content">{{ courseDetail?.description || '暂无课程简介，建议补充教学目标、课堂安排和考核方式。' }}</div>
          </div>
        </el-card>
      </el-col>

      <el-col :lg="8" :xs="24">
        <el-card class="detail-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div>
                <div class="header-title">成绩统计</div>
                <div class="header-subtitle">按课程下所有作业聚合的成绩概况</div>
              </div>
            </div>
          </template>

          <div class="score-stat-list">
            <div class="score-stat-item">
              <span>已评分作业数</span>
              <strong>{{ gradedHomeworkCount }}</strong>
            </div>
            <div class="score-stat-item">
              <span>平均提交率</span>
              <strong>{{ averageSubmitRate }}%</strong>
            </div>
            <div class="score-stat-item">
              <span>平均及格率</span>
              <strong>{{ averagePassRate }}%</strong>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="detail-card" shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="作业列表" name="homework">
          <el-table :data="homeworkRows" border stripe>
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="title" label="作业标题" min-width="220" show-overflow-tooltip />
            <el-table-column prop="deadline" label="截止时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.deadline) }}
              </template>
            </el-table-column>
            <el-table-column label="提交进度" width="150" align="center">
              <template #default="{ row }">
                {{ row.submittedCount ?? 0 }}/{{ row.totalCount ?? studentCount }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="120" align="center">
              <template #default="{ row }">
                {{ row.statusText || '待发布' }}
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!homeworkRows.length" description="当前课程暂无作业" />
        </el-tab-pane>

        <el-tab-pane label="学生列表" name="students">
          <el-table :data="studentRows" border stripe>
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="realName" label="姓名" min-width="120" />
            <el-table-column prop="username" label="学号" min-width="140" />
            <el-table-column prop="phone" label="手机号" min-width="140" />
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                {{ row.status === 1 ? '正常' : '禁用' }}
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!studentRows.length" description="当前班级暂无学生数据" />
        </el-tab-pane>

        <el-tab-pane label="成绩统计" name="grades">
          <el-table :data="gradeRows" border stripe>
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="title" label="作业标题" min-width="220" show-overflow-tooltip />
            <el-table-column prop="avgScore" label="平均分" width="120" align="center" />
            <el-table-column prop="submitRate" label="提交率" width="120" align="center">
              <template #default="{ row }">
                {{ row.submitRate }}%
              </template>
            </el-table-column>
            <el-table-column prop="passRate" label="及格率" width="120" align="center">
              <template #default="{ row }">
                {{ row.passRate }}%
              </template>
            </el-table-column>
            <el-table-column prop="gradedCount" label="已评分人数" width="120" align="center" />
          </el-table>
          <el-empty v-if="!gradeRows.length" description="当前课程暂无可统计成绩" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCourseById, type CourseVO } from '@/api/course'
import { getHomeworkList, getHomeworkStats, type AssignmentStatsVO, type HomeworkVO } from '@/api/homework'
import { getClassStudents, type UserVO } from '@/api/user'
import { useUserStore } from '@/stores/user'

interface GradeRow {
  id: number
  title: string
  avgScore: string
  submitRate: string
  passRate: string
  gradedCount: number
}

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const courseDetail = ref<CourseVO>()
const homeworkRows = ref<HomeworkVO[]>([])
const studentRows = ref<UserVO[]>([])
const gradeRows = ref<GradeRow[]>([])
const activeTab = ref<'homework' | 'students' | 'grades'>('homework')

const teacherName = computed(() => userStore.userInfo?.realName || '当前教师')
const studentCount = computed(() => courseDetail.value?.studentCount ?? studentRows.value.length)
const assignmentCount = computed(() => homeworkRows.value.length)
const gradedHomeworkCount = computed(() => gradeRows.value.filter((item) => Number(item.gradedCount || 0) > 0).length)

const averageSubmitRate = computed(() => {
  if (!gradeRows.value.length) return '0.0'
  const values = gradeRows.value
    .map((item) => parseRate(item.submitRate))
    .filter((item): item is number => item !== null)
  if (!values.length) return '0.0'
  const total = values.reduce((sum, item) => sum + item, 0)
  return (total / values.length).toFixed(1)
})

const averagePassRate = computed(() => {
  if (!gradeRows.value.length) return '0.0'
  const values = gradeRows.value
    .map((item) => parseRate(item.passRate))
    .filter((item): item is number => item !== null)
  if (!values.length) return '0.0'
  const total = values.reduce((sum, item) => sum + item, 0)
  return (total / values.length).toFixed(1)
})

const overallCompletionRate = computed(() => {
  if (!homeworkRows.value.length) return 0
  const total = homeworkRows.value.reduce((sum, item) => {
    const totalCount = item.totalCount || studentCount.value || 0
    if (!totalCount) return sum
    return sum + ((item.submittedCount || 0) / totalCount) * 100
  }, 0)
  return Math.round(total / homeworkRows.value.length)
})

const overallAverageScore = computed(() => {
  if (!gradeRows.value.length) return '0.0'
  const rows = gradeRows.value.filter((item) => Number(item.gradedCount || 0) > 0)
  if (!rows.length) return '0.0'
  const totalWeight = rows.reduce((sum, item) => sum + Number(item.gradedCount || 0), 0)
  if (!totalWeight) return '0.0'
  const totalScore = rows.reduce((sum, item) => {
    return sum + Number(item.avgScore || 0) * Number(item.gradedCount || 0)
  }, 0)
  return (totalScore / totalWeight).toFixed(1)
})

const courseId = computed(() => Number(route.params.id))

const loadCourseDetail = async () => {
  courseDetail.value = await getCourseById(courseId.value)
}

const loadHomeworkData = async () => {
  const res = await getHomeworkList({ current: 1, size: 100, courseId: courseId.value })
  homeworkRows.value = res.records || []
}

const loadStudentData = async () => {
  if (!courseDetail.value?.classId) {
    studentRows.value = []
    return
  }
  studentRows.value = (await getClassStudents(courseDetail.value.classId)) || []
}

const loadGradeData = async () => {
  if (!homeworkRows.value.length) {
    gradeRows.value = []
    return
  }
  const statsList = await Promise.all(
    homeworkRows.value.map(async (item) => {
      try {
        const stats = await getHomeworkStats(item.id)
        return { homework: item, stats }
      } catch {
        return {
          homework: item,
          stats: {
            avgScore: '0.0',
            passRate: '0.0',
            submitRate: '0.0',
            gradedCount: 0
          } as Partial<AssignmentStatsVO>
        }
      }
    })
  )

  gradeRows.value = statsList.map(({ homework, stats }) => ({
    id: homework.id,
    title: homework.title,
    avgScore: String(stats.avgScore ?? '0.0'),
    submitRate: normalizeRateText(stats.submitRate),
    passRate: normalizeRateText(stats.passRate),
    gradedCount: Number(stats.gradedCount ?? 0)
  }))
}

const handleTabChange = (name: string | number) => {
  router.replace({
    query: {
      ...route.query,
      tab: String(name)
    }
  })
}

const goBack = () => {
  router.push('/teacher/my-course')
}

const parseRate = (value?: string | number | null) => {
  if (value === null || value === undefined) return null
  const parsed = Number(String(value).replace(/%/g, '').trim())
  return Number.isFinite(parsed) ? parsed : null
}

const normalizeRateText = (value?: string | number | null) => {
  const parsed = parseRate(value)
  return parsed === null ? '0.0' : parsed.toFixed(1)
}

const formatDateTime = (value?: string | null) => {
  if (!value) return ''
  const normalized = value.replace('T', ' ')
  return normalized.slice(0, 19)
}

const getErrorMessage = (error: unknown) => {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return '请稍后重试'
}

onMounted(async () => {
  if (!courseId.value) {
    ElMessage.error('课程参数无效')
    router.push('/teacher/my-course')
    return
  }

  const tab = route.query.tab
  if (tab === 'students' || tab === 'grades' || tab === 'homework') {
    activeTab.value = tab
  }

  loading.value = true
  try {
    await loadCourseDetail()
    const [homeworkResult, studentResult] = await Promise.allSettled([loadHomeworkData(), loadStudentData()])

    if (homeworkResult.status === 'rejected') {
      console.error('加载课程作业列表失败', homeworkResult.reason)
      homeworkRows.value = []
      gradeRows.value = []
      ElMessage.warning(`作业列表加载失败：${getErrorMessage(homeworkResult.reason)}`)
    }

    if (studentResult.status === 'rejected') {
      console.error('加载课程学生列表失败', studentResult.reason)
      studentRows.value = []
      ElMessage.warning(`学生列表加载失败：${getErrorMessage(studentResult.reason)}`)
    }

    await loadGradeData()
  } catch (error) {
    console.error('加载课程详情失败', error)
    ElMessage.error('加载课程详情失败，请稍后重试')
    router.push('/teacher/my-course')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.course-detail-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.overview-top,
.card-header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.overview-top {
  flex-direction: column;
}

.course-title {
  margin: 10px 0 8px;
  font-size: 28px;
  color: #1f2f49;
}

.course-subtitle {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 16px;
  color: #7f8ca8;
  font-size: 13px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-top: 18px;
}

.metric-card {
  padding: 16px;
  border: 1px solid #e6edf7;
  border-radius: 14px;
  background: #f8fbff;
}

.metric-label {
  display: block;
  margin-bottom: 10px;
  font-size: 13px;
  color: #7f8ca8;
}

.metric-card strong {
  font-size: 24px;
  color: #253652;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.info-item {
  padding: 14px;
  border-radius: 12px;
  border: 1px solid #edf2fb;
  background: #fbfcff;
}

.info-label {
  display: block;
  margin-bottom: 6px;
  color: #7f8ca8;
  font-size: 12px;
}

.info-value {
  color: #22314d;
  font-size: 15px;
  font-weight: 600;
}

.desc-panel {
  margin-top: 16px;
  padding: 16px;
  border-radius: 14px;
  background: #f6f9fe;
  border: 1px solid #e8eef8;
}

.desc-title {
  margin-bottom: 10px;
  font-size: 14px;
  font-weight: 700;
  color: #253652;
}

.desc-content {
  line-height: 1.8;
  color: #53627d;
}

.score-stat-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.score-stat-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px;
  border: 1px solid #edf2fb;
  border-radius: 12px;
  background: #fbfcff;
  color: #53627d;
}

.score-stat-item strong {
  color: #22314d;
  font-size: 20px;
}

@media (max-width: 992px) {
  .metric-grid,
  .info-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .overview-top,
  .card-header {
    flex-direction: column;
  }

  .metric-grid,
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
