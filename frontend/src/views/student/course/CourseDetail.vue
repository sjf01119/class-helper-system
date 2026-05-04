<template>
  <div class="student-course-detail-page" v-loading="loading">
    <el-card class="detail-shell" shadow="never">
      <div class="page-toolbar">
        <el-button class="back-button" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <div class="page-toolbar__actions" v-if="currentCourse">
          <el-button type="primary" @click="goHomework(currentCourse)">去做作业</el-button>
          <el-button @click="viewMaterials(currentCourse)">查看资料</el-button>
          <el-button @click="goGrade(currentCourse)">成绩查询</el-button>
        </div>
      </div>

      <div v-if="loading" class="course-detail-skeleton">
        <el-skeleton animated>
          <template #template>
            <div class="detail-skeleton-top">
              <el-skeleton-item variant="image" class="detail-skeleton-cover" />
              <div class="detail-skeleton-side">
                <el-skeleton-item variant="h1" style="width: 60%; height: 32px" />
                <el-skeleton-item variant="text" style="width: 75%" />
                <el-skeleton-item variant="text" style="width: 88%" />
                <el-skeleton-item variant="rect" style="width: 100%; height: 120px; margin-top: 12px; border-radius: 16px" />
              </div>
            </div>
            <el-skeleton-item variant="rect" style="width: 100%; height: 220px; margin-top: 18px; border-radius: 18px" />
            <div class="detail-skeleton-grid">
              <el-skeleton-item variant="rect" style="width: 100%; height: 280px; border-radius: 18px" />
              <el-skeleton-item variant="rect" style="width: 100%; height: 280px; border-radius: 18px" />
            </div>
          </template>
        </el-skeleton>
      </div>

      <template v-else-if="currentCourse">
        <div class="detail-top">
          <div class="detail-cover" :class="{ 'is-default-cover': !resolveCourseCoverUrl(currentCourse.coverUrl) }">
            <el-image
              v-if="resolveCourseCoverUrl(currentCourse.coverUrl)"
              class="course-cover-image"
              :src="resolveCourseCoverUrl(currentCourse.coverUrl)"
              fit="cover"
              lazy
            >
              <template #error>
                <div class="course-cover-image__fallback" />
              </template>
            </el-image>
            <div class="course-cover__mask" />
            <div class="detail-cover__content">
              <div class="detail-cover__eyebrow">课程详情</div>
              <div class="detail-course-name">{{ currentCourse.courseName }}</div>
              <div class="detail-cover__meta">
                <el-tag :type="currentCourse.status === 1 ? 'success' : 'info'" size="small">
                  <el-icon>
                    <VideoPlay v-if="currentCourse.status === 1" />
                    <CircleCheckFilled v-else />
                  </el-icon>
                  <span>{{ currentCourse.status === 1 ? '进行中' : '已结课' }}</span>
                </el-tag>
                <span><el-icon><User /></el-icon>{{ currentCourse.teacherName || '暂无授课教师' }}</span>
                <span><el-icon><School /></el-icon>{{ currentCourse.className || '暂无班级信息' }}</span>
              </div>
            </div>
          </div>

          <div class="detail-overview">
            <div class="detail-overview__header">
              <div>
                <div class="detail-overview__title">课程总览</div>
                <div class="detail-overview__subtitle">以更直观的方式查看课程进度、成绩表现和待办节奏</div>
              </div>
              <span class="score-level-badge" :class="`score-level-badge--${getScoreLevel(getCourseAverageScoreValue(currentCourse.id)).tone}`">
                {{ getScoreLevel(getCourseAverageScoreValue(currentCourse.id)).label }}
              </span>
            </div>
            <div class="overview-grid">
              <div class="overview-item overview-item--wide">
                <span class="overview-icon">
                  <el-icon><Document /></el-icon>
                </span>
                <span class="overview-label">课程简介</span>
                <span class="overview-value">
                  {{ currentCourse.description || '课程简介暂未填写，建议联系任课教师补充课程说明。' }}
                </span>
              </div>
              <div class="overview-item">
                <span class="overview-icon overview-icon--primary">
                  <el-icon><Collection /></el-icon>
                </span>
                <span class="overview-label">课程进度</span>
                <span class="overview-value">{{ getCompletedChapterCount(currentCourse.id) }}/{{ getTotalChapterCount(currentCourse.id) }} 单元</span>
                <span class="overview-helper">当前完成度 {{ getCourseProgress(currentCourse.id) }}%</span>
              </div>
              <div class="overview-item">
                <span class="overview-icon overview-icon--warning">
                  <el-icon><Calendar /></el-icon>
                </span>
                <span class="overview-label">最近作业截止</span>
                <span class="overview-value">{{ getNearestDeadlineText(currentCourse.id) }}</span>
                <span class="overview-helper">优先处理即将截止的任务</span>
              </div>
              <div class="overview-item">
                <span class="overview-icon overview-icon--success">
                  <el-icon><Histogram /></el-icon>
                </span>
                <span class="overview-label">平均分</span>
                <span class="overview-value overview-value--score">{{ getCourseAverageScore(currentCourse.id) }}</span>
                <span class="overview-helper">你的平均分 {{ getMyCourseAverageScore(currentCourse.id) }}</span>
              </div>
            </div>
            <div class="score-comparison-panel">
              <div class="score-comparison-panel__header">
                <span>成绩对比</span>
                <strong>{{ getScoreCompareText(currentCourse.id) }}</strong>
              </div>
              <div class="score-compare-row">
                <div class="score-compare-label">我的平均分</div>
                <div class="score-compare-track">
                  <el-progress
                    :percentage="getMyCourseAverageScoreValue(currentCourse.id)"
                    :show-text="false"
                    :stroke-width="10"
                    color="#4f7df0"
                  />
                </div>
                <div class="score-compare-value">{{ getMyCourseAverageScore(currentCourse.id) }}</div>
              </div>
              <div class="score-compare-row">
                <div class="score-compare-label">课程平均分</div>
                <div class="score-compare-track">
                  <el-progress
                    :percentage="getCourseAverageScoreValue(currentCourse.id)"
                    :show-text="false"
                    :stroke-width="10"
                    color="#67c23a"
                  />
                </div>
                <div class="score-compare-value">{{ getCourseAverageScore(currentCourse.id) }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="detail-body">
          <div class="detail-section">
            <div class="section-header">
              <div class="section-title">学习章节</div>
              <div class="section-subtitle">点击章节可直达学习资料，并同步记录学习进度</div>
            </div>
            <div v-if="getCourseMaterialsList(currentCourse.id).length" class="chapter-list">
              <button
                v-for="(item, index) in getCourseMaterialsList(currentCourse.id)"
                :key="item.id"
                type="button"
                class="chapter-item"
                :class="[
                  { active: getCurrentChapterId(currentCourse.id) === item.id },
                  `chapter-item--${getChapterStatus(currentCourse.id, item.id)}`
                ]"
                @click="openChapter(currentCourse, item)"
              >
                <span class="chapter-index">第 {{ index + 1 }} 节</span>
                <div class="chapter-main">
                  <div class="chapter-main__top">
                    <div class="chapter-name">{{ item.title }}</div>
                    <div class="chapter-meta">{{ formatDateTime(item.createdAt) }}</div>
                  </div>
                  <div class="chapter-main__bottom">
                    <div class="chapter-progress-meta">
                      <span>{{ getChapterStatusText(currentCourse.id, item.id) }}</span>
                      <span>{{ getChapterProgress(currentCourse.id, item.id) }}%</span>
                    </div>
                    <el-progress
                      :percentage="getChapterProgress(currentCourse.id, item.id)"
                      :show-text="false"
                      :stroke-width="8"
                      :color="getChapterProgressColor(currentCourse.id, item.id)"
                    />
                  </div>
                </div>
                <div class="chapter-status">
                  <el-icon v-if="isChapterCompleted(currentCourse.id, item.id)" class="chapter-status__done">
                    <CircleCheckFilled />
                  </el-icon>
                  <el-icon v-else-if="getCurrentChapterId(currentCourse.id) === item.id" class="chapter-status__current">
                    <Reading />
                  </el-icon>
                  <el-icon v-else class="chapter-status__waiting">
                    <Clock />
                  </el-icon>
                  <span class="chapter-status__text">{{ getChapterStatusText(currentCourse.id, item.id) }}</span>
                </div>
              </button>
            </div>
            <el-empty v-else description="当前课程暂无章节资料，等待教师发布学习资料" />
          </div>

          <div class="detail-grid">
            <div class="detail-section">
              <div class="section-header">
                <div class="section-title">作业统计</div>
                <div class="section-subtitle">仅展示你在本课程下的个人任务情况</div>
              </div>
              <div class="stats-grid">
                <div class="stats-card stats-card--warning">
                  <div class="stats-card__icon">
                    <el-icon><Clock /></el-icon>
                  </div>
                  <span>待提交</span>
                  <strong>{{ animatedHomeworkStats.pending }}</strong>
                  <small>当前待处理任务</small>
                </div>
                <div class="stats-card stats-card--primary">
                  <div class="stats-card__icon">
                    <el-icon><Finished /></el-icon>
                  </div>
                  <span>已提交</span>
                  <strong>{{ animatedHomeworkStats.submitted }}</strong>
                  <small>已完成提交记录</small>
                </div>
                <div class="stats-card stats-card--success">
                  <div class="stats-card__icon">
                    <el-icon><CircleCheckFilled /></el-icon>
                  </div>
                  <span>已批改</span>
                  <strong>{{ animatedHomeworkStats.graded }}</strong>
                  <small>已有评分反馈</small>
                </div>
              </div>
            </div>

            <div class="detail-section">
              <div class="section-header">
                <div class="section-title">成绩概览</div>
                <div class="section-subtitle">查看该课程历次成绩和平均分</div>
              </div>
              <div class="grade-summary">
                <div class="grade-summary__avg">
                  <div>
                    <span>课程平均分</span>
                    <p>{{ getScoreLevel(getCourseAverageScoreValue(currentCourse.id)).description }}</p>
                  </div>
                  <div class="grade-summary__avg-main">
                    <strong>{{ getCourseAverageScore(currentCourse.id) }}</strong>
                    <span class="score-level-badge" :class="`score-level-badge--${getScoreLevel(getCourseAverageScoreValue(currentCourse.id)).tone}`">
                      {{ getScoreLevel(getCourseAverageScoreValue(currentCourse.id)).label }}
                    </span>
                  </div>
                </div>
                <div class="grade-summary__compare">
                  <div class="grade-summary__compare-head">
                    <span>我的分数与课程平均分对比</span>
                    <strong>{{ getScoreCompareText(currentCourse.id) }}</strong>
                  </div>
                  <div class="grade-summary__compare-bars">
                    <div class="score-compare-row">
                      <div class="score-compare-label">我的平均分</div>
                      <div class="score-compare-track">
                        <el-progress
                          :percentage="getMyCourseAverageScoreValue(currentCourse.id)"
                          :show-text="false"
                          :stroke-width="8"
                          color="#4f7df0"
                        />
                      </div>
                      <div class="score-compare-value">{{ getMyCourseAverageScore(currentCourse.id) }}</div>
                    </div>
                    <div class="score-compare-row">
                      <div class="score-compare-label">课程平均分</div>
                      <div class="score-compare-track">
                        <el-progress
                          :percentage="getCourseAverageScoreValue(currentCourse.id)"
                          :show-text="false"
                          :stroke-width="8"
                          color="#67c23a"
                        />
                      </div>
                      <div class="score-compare-value">{{ getCourseAverageScore(currentCourse.id) }}</div>
                    </div>
                  </div>
                </div>
                <div class="grade-summary__list">
                  <div
                    v-for="item in getCourseGradeRecords(currentCourse.id)"
                    :key="item.id"
                    class="grade-record"
                    :class="{
                      'is-pending': item.status === 'pending',
                      'is-best': item.isHighest,
                      'is-lowest': item.isLowest
                    }"
                    @click="openHomeworkDetail(currentCourse, item.id)"
                  >
                    <div class="grade-record__main">
                      <div class="grade-record__title-row">
                        <div class="grade-record__title">{{ item.title }}</div>
                        <div class="grade-record__tags">
                          <span class="grade-record__status" :class="`grade-record__status--${item.status}`">
                            {{ item.status === 'graded' ? '已批改' : '待批改' }}
                          </span>
                          <span v-if="item.isHighest" class="grade-record__flag grade-record__flag--best">最高分</span>
                          <span v-if="item.isLowest" class="grade-record__flag grade-record__flag--lowest">最低分</span>
                        </div>
                      </div>
                      <div class="grade-record__meta">
                        <span>得分率：{{ item.status === 'graded' ? `${item.rate}%` : '待计算' }}</span>
                        <span>{{ item.status === 'graded' ? '点击查看作业详情' : '等待教师批改后查看结果' }}</span>
                      </div>
                    </div>
                    <div class="grade-record__score-box">
                      <div class="grade-record__score">{{ item.scoreText }}</div>
                      <el-icon class="grade-record__arrow"><ArrowRight /></el-icon>
                    </div>
                  </div>
                  <el-empty
                    v-if="getCourseGradeRecords(currentCourse.id).length === 0"
                    description="当前课程暂无成绩记录"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>

      <el-empty v-else description="未找到该课程或你暂无访问权限">
        <el-button type="primary" @click="goBack">返回我的课程</el-button>
      </el-empty>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  ArrowRight,
  Calendar,
  CircleCheckFilled,
  Clock,
  Collection,
  Document,
  Finished,
  Histogram,
  Reading,
  School,
  User,
  VideoPlay
} from '@element-plus/icons-vue'
import { getMyStudentCourses, type CourseVO } from '@/api/course'
import { getStudentHomeworkList, type HomeworkVO } from '@/api/homework'
import { getStudentReportOverview, type StudentCourseGradeStat } from '@/api/dashboard'
import { getStudentMaterials, type MaterialVO } from '@/api/material'
import { useUserStore } from '@/stores/user'
import { resolveMediaUrl } from '@/utils/media'

interface CourseLearningProgress {
  currentId?: number
  completedIds: number[]
}

interface HomeworkCourseStats {
  pending: number
  submitted: number
  graded: number
}

interface GradeRecordView {
  id: number
  title: string
  status: 'pending' | 'graded'
  scoreText: string
  rate: number
  isHighest: boolean
  isLowest: boolean
}

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const allCourses = ref<CourseVO[]>([])
const pendingHomeworkList = ref<HomeworkVO[]>([])
const submittedHomeworkList = ref<HomeworkVO[]>([])
const courseGradeStats = ref<StudentCourseGradeStat[]>([])
const materialMap = ref<Record<number, MaterialVO[]>>({})
const animatedHomeworkStats = reactive<HomeworkCourseStats>({
  pending: 0,
  submitted: 0,
  graded: 0
})
let statsAnimationFrame = 0

const courseId = computed(() => Number(route.params.courseId))
const currentCourse = computed(() => {
  return allCourses.value.find((course) => course.id === courseId.value)
})

const progressStorageKey = (id: number) => `student-course-progress:${userStore.userInfo?.id || 'guest'}:${id}`

const getLearningProgress = (id: number): CourseLearningProgress => {
  try {
    const raw = window.localStorage.getItem(progressStorageKey(id))
    if (!raw) {
      return { completedIds: [] }
    }
    const parsed = JSON.parse(raw) as CourseLearningProgress
    return {
      currentId: parsed.currentId,
      completedIds: Array.isArray(parsed.completedIds) ? parsed.completedIds : []
    }
  } catch {
    return { completedIds: [] }
  }
}

const setLearningProgress = (id: number, value: CourseLearningProgress) => {
  window.localStorage.setItem(progressStorageKey(id), JSON.stringify(value))
}

const getCourseMaterialsList = (id: number) => materialMap.value[id] || []

const getTotalChapterCount = (id: number) => getCourseMaterialsList(id).length

const getCompletedChapterCount = (id: number) => {
  const progress = getLearningProgress(id)
  const materialIds = new Set(getCourseMaterialsList(id).map((item) => item.id))
  return progress.completedIds.filter((itemId) => materialIds.has(itemId)).length
}

const getCourseProgress = (id: number) => {
  const totalCount = getTotalChapterCount(id)
  if (!totalCount) return 0
  return Math.round((getCompletedChapterCount(id) / totalCount) * 100)
}

const getCurrentChapterId = (id: number) => {
  const progress = getLearningProgress(id)
  if (progress.currentId) {
    return progress.currentId
  }
  const firstPending = getCourseMaterialsList(id).find((item) => !progress.completedIds.includes(item.id))
  return firstPending?.id || getCourseMaterialsList(id)[0]?.id
}

const isChapterCompleted = (id: number, materialId: number) => {
  return getLearningProgress(id).completedIds.includes(materialId)
}

const getCourseHomeworkStats = (id: number): HomeworkCourseStats => {
  const pending = pendingHomeworkList.value.filter((item) => item.courseId === id).length
  const submittedRows = submittedHomeworkList.value.filter((item) => item.courseId === id)
  const graded = submittedRows.filter((item) => item.score !== undefined && item.score !== null).length
  return {
    pending,
    submitted: submittedRows.length,
    graded
  }
}

const animateHomeworkStats = (target: HomeworkCourseStats) => {
  if (typeof window === 'undefined') {
    Object.assign(animatedHomeworkStats, target)
    return
  }

  if (statsAnimationFrame) {
    window.cancelAnimationFrame(statsAnimationFrame)
  }

  const startValues = { ...animatedHomeworkStats }
  const startTime = window.performance.now()
  const duration = 480

  const step = (timestamp: number) => {
    const progress = Math.min(1, (timestamp - startTime) / duration)
    const eased = 1 - Math.pow(1 - progress, 3)

    animatedHomeworkStats.pending = Math.round(startValues.pending + (target.pending - startValues.pending) * eased)
    animatedHomeworkStats.submitted = Math.round(startValues.submitted + (target.submitted - startValues.submitted) * eased)
    animatedHomeworkStats.graded = Math.round(startValues.graded + (target.graded - startValues.graded) * eased)

    if (progress < 1) {
      statsAnimationFrame = window.requestAnimationFrame(step)
    }
  }

  statsAnimationFrame = window.requestAnimationFrame(step)
}

const getNearestDeadlineText = (id: number) => {
  const pendingRows = pendingHomeworkList.value
    .filter((item) => item.courseId === id && item.deadline)
    .sort((a, b) => getTime(a.deadline) - getTime(b.deadline))
  if (!pendingRows.length) {
    return '暂无待提交作业'
  }
  return formatDateTime(pendingRows[0].deadline)
}

const getCourseGradeRecords = (id: number) => {
  const rows = submittedHomeworkList.value
    .filter((item) => item.courseId === id)
    .map((item) => ({
      id: item.id,
      title: item.title,
      status: item.score === undefined || item.score === null ? 'pending' as const : 'graded' as const,
      score: item.score === undefined || item.score === null ? undefined : Number(item.score),
      maxScore: Number(item.maxScore ?? item.totalScore ?? 100) || 100
    }))

  const gradedScores = rows
    .filter((item) => item.status === 'graded' && item.score !== undefined)
    .map((item) => item.score as number)
  const highest = gradedScores.length ? Math.max(...gradedScores) : undefined
  const lowest = gradedScores.length ? Math.min(...gradedScores) : undefined

  return rows.map<GradeRecordView>((item) => {
    const score = item.score
    const rate = item.status === 'graded' && score !== undefined
      ? Math.round((score / item.maxScore) * 100)
      : 0
    return {
      id: item.id,
      title: item.title,
      status: item.status,
      scoreText: item.status === 'graded' && score !== undefined ? `${score}/${item.maxScore}` : '待批改',
      rate,
      isHighest: item.status === 'graded' && score !== undefined && highest !== undefined && score === highest && highest !== lowest,
      isLowest: item.status === 'graded' && score !== undefined && lowest !== undefined && score === lowest && highest !== lowest
    }
  })
}

const getCourseAverageScore = (id: number) => {
  const stat = courseGradeStats.value.find((item) => item.courseId === id)
  return stat?.avgScore || '0.0'
}

const getCourseAverageScoreValue = (id: number) => {
  const value = Number(getCourseAverageScore(id))
  if (Number.isNaN(value)) return 0
  return Math.max(0, Math.min(100, Math.round(value)))
}

const getMyCourseAverageScore = (id: number) => {
  const gradedRows = submittedHomeworkList.value
    .filter((item) => item.courseId === id && item.score !== undefined && item.score !== null)
    .map((item) => Number(item.score))
    .filter((item) => !Number.isNaN(item))
  if (!gradedRows.length) return '0.0'
  const total = gradedRows.reduce((sum, item) => sum + item, 0)
  return (total / gradedRows.length).toFixed(1)
}

const getMyCourseAverageScoreValue = (id: number) => {
  const value = Number(getMyCourseAverageScore(id))
  if (Number.isNaN(value)) return 0
  return Math.max(0, Math.min(100, Math.round(value)))
}

const getScoreLevel = (score: number) => {
  if (score >= 90) return { label: '优秀', tone: 'excellent', description: '成绩稳定领先，继续保持冲刺状态' }
  if (score >= 80) return { label: '良好', tone: 'good', description: '整体表现良好，距离优秀还有一步' }
  if (score >= 60) return { label: '及格', tone: 'pass', description: '基础要求已达成，可以继续提升高分题' }
  return { label: '待提升', tone: 'warn', description: '建议优先回顾薄弱点，及时补齐遗漏任务' }
}

const getScoreCompareText = (id: number) => {
  const myScore = Number(getMyCourseAverageScore(id))
  const avgScore = Number(getCourseAverageScore(id))
  if (Number.isNaN(myScore) || Number.isNaN(avgScore)) {
    return '暂无可对比数据'
  }
  const delta = Number((myScore - avgScore).toFixed(1))
  if (Math.abs(delta) < 0.1) {
    return '与你所在课程平均分持平'
  }
  return delta > 0 ? `高于课程平均分 ${delta.toFixed(1)} 分` : `低于课程平均分 ${Math.abs(delta).toFixed(1)} 分`
}

const getChapterStatus = (id: number, materialId: number) => {
  if (isChapterCompleted(id, materialId)) return 'done'
  if (getCurrentChapterId(id) === materialId) return 'current'
  return 'waiting'
}

const getChapterStatusText = (id: number, materialId: number) => {
  const status = getChapterStatus(id, materialId)
  if (status === 'done') return '已完成'
  if (status === 'current') return '进行中'
  return '未开始'
}

const getChapterProgress = (id: number, materialId: number) => {
  const status = getChapterStatus(id, materialId)
  if (status === 'done') return 100
  if (status === 'current') return 60
  return 0
}

const getChapterProgressColor = (id: number, materialId: number) => {
  const status = getChapterStatus(id, materialId)
  if (status === 'done') return '#67c23a'
  if (status === 'current') return '#4f7df0'
  return '#dcdfe6'
}

const loadCourseMaterials = async () => {
  const entries = await Promise.all(
    allCourses.value.map(async (course) => {
      const res = await getStudentMaterials({
        current: 1,
        size: 200,
        courseId: course.id
      }).catch(() => undefined)
      const records = res?.records || []
      return [course.id, records.sort((a, b) => getTime(a.createdAt) - getTime(b.createdAt))] as const
    })
  )
  materialMap.value = Object.fromEntries(entries)
}

const loadPageData = async () => {
  if (!courseId.value || Number.isNaN(courseId.value)) {
    ElMessage.error('课程参数无效')
    router.replace('/student/my-course')
    return
  }

  loading.value = true
  try {
    const [coursesRes, pendingRes, submittedRes, reportRes] = await Promise.all([
      getMyStudentCourses(),
      getStudentHomeworkList(0),
      getStudentHomeworkList(1),
      getStudentReportOverview()
    ])

    allCourses.value = coursesRes || []
    pendingHomeworkList.value = pendingRes || []
    submittedHomeworkList.value = submittedRes || []
    courseGradeStats.value = reportRes?.courseStats || []

    await loadCourseMaterials()

    if (!currentCourse.value) {
      ElMessage.warning('未找到该课程或你暂无访问权限')
      router.replace('/student/my-course')
      return
    }

    animateHomeworkStats(getCourseHomeworkStats(currentCourse.value.id))
  } catch (error) {
    console.error('加载课程详情失败', error)
    ElMessage.error('加载课程详情失败，请稍后重试')
    router.replace('/student/my-course')
  } finally {
    loading.value = false
  }
}

const markCurrentChapter = (id: number, materialId: number) => {
  const progress = getLearningProgress(id)
  const completedIds = progress.completedIds.includes(materialId)
    ? progress.completedIds
    : [...progress.completedIds, materialId]
  setLearningProgress(id, {
    currentId: materialId,
    completedIds
  })
}

const openChapter = (course: CourseVO, material: MaterialVO) => {
  markCurrentChapter(course.id, material.id)
  router.push({
    path: '/student/material',
    query: {
      courseId: String(course.id),
      materialId: String(material.id)
    }
  })
}

const viewMaterials = (course: CourseVO) => {
  router.push({
    path: '/student/material',
    query: { courseId: String(course.id) }
  })
}

const goHomework = (course: CourseVO) => {
  router.push({
    path: '/student/homework',
    query: { courseId: String(course.id) }
  })
}

const goGrade = (course: CourseVO) => {
  router.push({
    path: '/student/grade',
    query: { courseId: String(course.id) }
  })
}

const openHomeworkDetail = (course: CourseVO, homeworkId: number) => {
  router.push({
    path: '/student/homework',
    query: {
      courseId: String(course.id),
      homeworkId: String(homeworkId)
    }
  })
}

const goBack = () => {
  router.back()
}

const resolveCourseCoverUrl = (coverUrl?: string) => resolveMediaUrl(coverUrl)

const getTime = (value?: string) => {
  if (!value) return 0
  const time = new Date(value).getTime()
  return Number.isNaN(time) ? 0 : time
}

const formatDateTime = (date?: string) => {
  if (!date) return '暂无数据'
  const value = new Date(date)
  if (Number.isNaN(value.getTime())) return '暂无数据'
  return value.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

watch(
  () => route.params.courseId,
  () => {
    loadPageData()
  }
)

onMounted(() => {
  loadPageData()
})

onBeforeUnmount(() => {
  if (statsAnimationFrame) {
    window.cancelAnimationFrame(statsAnimationFrame)
  }
})
</script>

<style scoped>
.student-course-detail-page {
  padding: 20px;
  background: linear-gradient(180deg, #f5f8ff 0%, #fbfcff 100%);
}

.detail-shell {
  border: 1px solid #e4eaf2;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 18px 40px rgba(31, 52, 87, 0.08);
}

.page-toolbar,
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.page-toolbar {
  margin-bottom: 18px;
}

.page-toolbar__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.back-button {
  min-height: 42px;
}

.course-cover,
.detail-cover {
  position: relative;
  min-height: 162px;
  padding: 16px;
  background: linear-gradient(135deg, #1f4d8e 0%, #4f7df0 100%);
  background-size: cover;
  background-position: center;
}

.course-cover-image,
.course-cover-image__fallback {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.course-cover-image {
  z-index: 0;
}

.course-cover-image__fallback {
  background: linear-gradient(135deg, #1f4d8e 0%, #4f7df0 100%);
}

.detail-cover {
  min-height: 280px;
  padding: 24px;
  border-radius: 22px;
  overflow: hidden;
  box-shadow: 0 18px 36px rgba(31, 52, 87, 0.14);
}

.course-cover__mask {
  position: absolute;
  inset: 0;
  z-index: 1;
  background: linear-gradient(180deg, rgba(13, 27, 53, 0.18) 0%, rgba(13, 27, 53, 0.82) 100%);
}

.detail-cover__content {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  height: 100%;
  color: #fff;
}

.course-detail-skeleton {
  padding: 8px 4px;
}

.detail-skeleton-top,
.detail-skeleton-grid {
  display: grid;
  gap: 18px;
}

.detail-skeleton-top {
  grid-template-columns: 1.2fr 1fr;
}

.detail-skeleton-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin-top: 18px;
}

.detail-skeleton-cover {
  width: 100%;
  height: 280px;
  border-radius: 20px;
}

.detail-skeleton-side {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-top {
  display: grid;
  grid-template-columns: minmax(0, 1.18fr) minmax(0, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.detail-overview {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 22px;
  border: 1px solid #e4eaf2;
  border-radius: 20px;
  background: linear-gradient(180deg, #fcfdff 0%, #f7faff 100%);
  box-shadow: 0 12px 28px rgba(31, 52, 87, 0.06);
}

.detail-overview__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.detail-overview__title {
  font-size: 18px;
  font-weight: 700;
  color: #22314d;
}

.detail-overview__subtitle,
.section-subtitle {
  margin-top: 8px;
  color: #7f8ca8;
  font-size: 13px;
  line-height: 1.7;
}

.detail-cover__eyebrow {
  display: inline-flex;
  align-items: center;
  width: fit-content;
  padding: 6px 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.detail-course-name {
  margin-top: 12px;
  color: #fff;
  font-size: 30px;
  line-height: 1.35;
  font-weight: 700;
  letter-spacing: 0.01em;
}

.detail-cover__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 16px;
  margin-top: 16px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.88);
}

.detail-cover__meta span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.overview-item {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 128px;
  padding: 18px 18px 18px 58px;
  border: 1px solid #e7eef8;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 6px 18px rgba(31, 52, 87, 0.04);
}

.overview-item--wide {
  grid-column: span 2;
}

.overview-icon {
  position: absolute;
  top: 18px;
  left: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 10px;
  background: #eef4ff;
  color: #4f7df0;
  font-size: 16px;
}

.overview-icon--warning {
  background: #fff5eb;
  color: #e18a32;
}

.overview-icon--success {
  background: #eefbf2;
  color: #4caf69;
}

.overview-label {
  color: #7f8ca8;
  font-size: 12px;
  letter-spacing: 0.02em;
}

.overview-value {
  color: #253652;
  font-size: 15px;
  line-height: 1.75;
  font-weight: 600;
}

.overview-value--score {
  font-size: 26px;
  line-height: 1;
}

.overview-helper {
  color: #8c9ab3;
  font-size: 12px;
  line-height: 1.6;
}

.score-level-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 32px;
  padding: 0 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}

.score-level-badge--excellent {
  background: #edf8ef;
  color: #2c8f50;
}

.score-level-badge--good {
  background: #eef4ff;
  color: #3561cc;
}

.score-level-badge--pass {
  background: #fff7ea;
  color: #d38a26;
}

.score-level-badge--warn {
  background: #fff0ef;
  color: #d65a54;
}

.score-comparison-panel,
.grade-summary__compare {
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid #e7eef8;
  background: rgba(255, 255, 255, 0.92);
}

.score-comparison-panel__header,
.grade-summary__compare-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  color: #53627d;
  font-size: 13px;
}

.score-comparison-panel__header strong,
.grade-summary__compare-head strong {
  color: #22314d;
  font-size: 14px;
}

.score-compare-row + .score-compare-row {
  margin-top: 12px;
}

.score-compare-row {
  display: grid;
  grid-template-columns: 88px 1fr auto;
  align-items: center;
  gap: 12px;
}

.score-compare-label {
  color: #7f8ca8;
  font-size: 12px;
}

.score-compare-track {
  min-width: 0;
}

.score-compare-value {
  min-width: 40px;
  color: #22314d;
  font-size: 13px;
  font-weight: 700;
  text-align: right;
}

.detail-body {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.detail-section {
  padding: 22px;
  border: 1px solid #e4eaf2;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 10px 24px rgba(31, 52, 87, 0.05);
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #253652;
}

.chapter-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: 16px;
}

.chapter-item {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 16px;
  align-items: center;
  width: 100%;
  padding: 18px;
  border: 1px solid #e8eef8;
  border-radius: 18px;
  background: linear-gradient(180deg, #fcfdff 0%, #f8fbff 100%);
  text-align: left;
  cursor: pointer;
  transition: border-color 0.22s ease, box-shadow 0.22s ease, transform 0.22s ease, background 0.22s ease;
}

.chapter-item:hover,
.chapter-item.active {
  transform: translateY(-3px);
  border-color: #cfe0f7;
  box-shadow: 0 16px 28px rgba(31, 52, 87, 0.08);
}

.chapter-item:focus-visible,
.grade-record:focus-visible {
  outline: 0;
  border-color: #4f7df0;
  box-shadow: 0 0 0 3px rgba(79, 125, 240, 0.14);
}

.chapter-item--done {
  border-color: #d8efde;
}

.chapter-item--current {
  border-color: #d6e4fb;
}

.chapter-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 76px;
  height: 36px;
  border-radius: 999px;
  background: #edf4ff;
  color: #2d4f88;
  font-size: 12px;
  font-weight: 600;
}

.chapter-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chapter-main__top,
.chapter-progress-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.chapter-name {
  color: #253652;
  font-weight: 600;
  line-height: 1.6;
}

.chapter-meta {
  color: #7f8ca8;
  font-size: 12px;
  white-space: nowrap;
}

.chapter-main__bottom {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.chapter-progress-meta {
  color: #7f8ca8;
  font-size: 12px;
}

.chapter-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 72px;
  text-align: center;
}

.chapter-status__done {
  color: #63b779;
}

.chapter-status__current {
  color: #4f7df0;
}

.chapter-status__waiting {
  color: #a6afbf;
}

.chapter-status__text {
  color: #6c7c98;
  font-size: 12px;
  font-weight: 600;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 16px;
}

.stats-card {
  position: relative;
  padding: 18px;
  border-radius: 18px;
  border: 1px solid #e7eef8;
  text-align: center;
  overflow: hidden;
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
}

.stats-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 14px 26px rgba(31, 52, 87, 0.08);
}

.stats-card--warning {
  background: linear-gradient(180deg, #fff9f0 0%, #fffdf8 100%);
  border-color: #f7e4bf;
}

.stats-card--primary {
  background: linear-gradient(180deg, #f1f6ff 0%, #fcfdff 100%);
  border-color: #dce8ff;
}

.stats-card--success {
  background: linear-gradient(180deg, #f0fbf3 0%, #fcfefc 100%);
  border-color: #d7efde;
}

.stats-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  margin-bottom: 12px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.85);
  color: #22314d;
  font-size: 18px;
}

.stats-card span {
  display: block;
  color: #7f8ca8;
  font-size: 12px;
}

.stats-card strong {
  display: block;
  margin-top: 10px;
  color: #253652;
  font-size: 30px;
  line-height: 1;
}

.stats-card small {
  display: block;
  margin-top: 10px;
  color: #8a96ad;
  font-size: 12px;
}

.grade-summary {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.grade-summary__avg {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px;
  border-radius: 18px;
  background: linear-gradient(180deg, #f8fbff 0%, #fcfdff 100%);
  border: 1px solid #e7eef8;
}

.grade-summary__avg span {
  color: #7f8ca8;
}

.grade-summary__avg p {
  margin: 8px 0 0;
  color: #8a96ad;
  font-size: 12px;
  line-height: 1.6;
}

.grade-summary__avg-main {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.grade-summary__avg strong {
  color: #253652;
  font-size: 28px;
}

.grade-summary__list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 14px;
}

.grade-record {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 16px 18px;
  border: 1px solid #edf2fb;
  border-radius: 16px;
  background: #fbfcff;
  cursor: pointer;
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease, background 0.22s ease;
}

.grade-record:hover {
  transform: translateY(-2px);
  border-color: #d6e3f7;
  box-shadow: 0 12px 22px rgba(31, 52, 87, 0.07);
}

.grade-record.is-pending {
  background: #f7f8fb;
  border-color: #e6eaf2;
}

.grade-record.is-best {
  border-color: #d9efdf;
  background: linear-gradient(180deg, #f4fcf6 0%, #fcfffd 100%);
}

.grade-record.is-lowest {
  border-color: #f7dfd7;
  background: linear-gradient(180deg, #fff6f4 0%, #fffdfd 100%);
}

.grade-record__main {
  min-width: 0;
  flex: 1;
}

.grade-record__title-row,
.grade-record__tags,
.grade-record__score-box {
  display: flex;
  align-items: center;
}

.grade-record__title-row {
  justify-content: space-between;
  gap: 12px;
}

.grade-record__tags {
  flex-wrap: wrap;
  gap: 8px;
}

.grade-record__title {
  color: #33435e;
  font-weight: 600;
  line-height: 1.6;
}

.grade-record__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  margin-top: 8px;
  color: #8a96ad;
  font-size: 12px;
}

.grade-record__status,
.grade-record__flag {
  display: inline-flex;
  align-items: center;
  min-height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.grade-record__status--graded,
.grade-record__flag--best {
  background: #edf8ef;
  color: #2c8f50;
}

.grade-record__status--pending {
  background: #eef1f6;
  color: #7d879a;
}

.grade-record__flag--lowest {
  background: #fff1ee;
  color: #d65a54;
}

.grade-record__score-box {
  gap: 10px;
  flex-shrink: 0;
}

.grade-record__score {
  color: #253652;
  font-weight: 700;
  font-size: 18px;
}

.grade-record.is-pending .grade-record__score,
.grade-record.is-pending .grade-record__title {
  color: #8b95a7;
}

.grade-record__arrow {
  color: #9aa7bc;
  font-size: 15px;
}

@media (max-width: 992px) {
  .detail-skeleton-top,
  .detail-skeleton-grid,
  .detail-top,
  .detail-grid,
  .overview-grid {
    grid-template-columns: 1fr;
  }

  .overview-item--wide {
    grid-column: span 1;
  }
}

@media (max-width: 768px) {
  .student-course-detail-page {
    padding: 14px;
  }

  .page-toolbar,
  .section-header,
  .detail-overview__header,
  .grade-summary__avg,
  .grade-summary__compare-head,
  .chapter-main__top,
  .chapter-progress-meta,
  .grade-record__title-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .page-toolbar__actions,
  .stats-grid {
    width: 100%;
    grid-template-columns: 1fr;
  }

  .page-toolbar__actions .el-button {
    width: 100%;
  }

  .chapter-item {
    grid-template-columns: 1fr;
  }

  .detail-cover {
    min-height: 240px;
    padding: 18px;
  }

  .detail-course-name {
    font-size: 24px;
  }

  .detail-overview,
  .detail-section,
  .grade-summary__avg,
  .score-comparison-panel,
  .grade-summary__compare {
    padding: 18px;
  }

  .score-compare-row {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
  }

  .score-compare-value {
    text-align: left;
  }

  .grade-record {
    flex-direction: column;
    align-items: flex-start;
  }

  .grade-record__score-box {
    width: 100%;
    justify-content: space-between;
  }
}

@media (prefers-reduced-motion: reduce) {
  .chapter-item,
  .stats-card,
  .grade-record {
    transition: none;
  }
}
</style>
