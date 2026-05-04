<template>
  <div class="my-course">
    <el-card class="search-card" shadow="never">
      <div class="toolbar-header">
        <div>
          <div class="header-title">我的课程</div>
          <div class="header-subtitle">按课程查看学习进度、作业提醒和成绩概览</div>
        </div>
        <el-tag type="primary" effect="plain">共 {{ filteredCourses.length }} 门课程</el-tag>
      </div>

      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="课程名称">
          <el-input
            v-model="queryParams.keyword"
            placeholder="请输入课程名称"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="课程状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="进行中" :value="1" />
            <el-option label="已结课" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序方式">
          <el-select v-model="queryParams.sortBy" style="width: 170px">
            <el-option label="最近更新时间" value="updatedAt" />
            <el-option label="课程名称" value="courseName" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序方向">
          <el-select v-model="queryParams.sortOrder" style="width: 120px">
            <el-option label="降序" value="desc" />
            <el-option label="升序" value="asc" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="course-card" shadow="never">
      <div v-loading="loading" class="course-content">
        <el-row v-if="pagedCourses.length > 0" :gutter="20">
          <el-col
            v-for="course in pagedCourses"
            :key="course.id"
            :xs="24"
            :sm="12"
            :md="8"
            :xl="6"
            class="course-col"
          >
            <div class="course-item" @click="toCourseDetail(course.id)">
              <div class="course-cover" :class="{ 'is-default-cover': !resolveCourseCoverUrl(course.coverUrl) }">
                <el-image
                  v-if="resolveCourseCoverUrl(course.coverUrl)"
                  class="course-cover-image"
                  :src="resolveCourseCoverUrl(course.coverUrl)"
                  fit="cover"
                  lazy
                >
                  <template #error>
                    <div class="course-cover-image__fallback" />
                  </template>
                </el-image>
                <div class="course-cover__mask" />
                <div class="course-cover__top">
                  <el-tag
                    :type="course.status === 1 ? 'success' : 'info'"
                    size="small"
                    class="status-tag"
                  >
                    <el-icon>
                      <VideoPlay v-if="course.status === 1" />
                      <CircleCheckFilled v-else />
                    </el-icon>
                    <span>{{ course.status === 1 ? '进行中' : '已结课' }}</span>
                  </el-tag>
                </div>
                <div class="course-cover__bottom">
                  <div class="course-title" :title="course.courseName">{{ course.courseName }}</div>
                  <div class="course-desc" :title="course.description">
                    {{ course.description || '课程简介暂未填写，建议联系任课教师补充课程说明。' }}
                  </div>
                </div>
              </div>

              <div class="course-info">
                <div class="info-row">
                  <span class="info-label">任课教师</span>
                  <span class="info-value">
                    <el-icon><User /></el-icon>
                    {{ course.teacherName || '暂无数据' }}
                  </span>
                </div>
                <div class="info-row">
                  <span class="info-label">所属班级</span>
                  <span class="info-value">
                    <el-icon><School /></el-icon>
                    {{ course.className || '暂无数据' }}
                  </span>
                </div>
                <div class="info-row">
                  <span class="info-label">课程进度</span>
                  <span class="info-value">{{ getCompletedChapterCount(course.id) }}/{{ getTotalChapterCount(course.id) }} 单元</span>
                </div>
                <div class="info-row">
                  <span class="info-label">最近作业截止</span>
                  <span class="info-value deadline-value">{{ getNearestDeadlineText(course.id) }}</span>
                </div>
              </div>

              <div class="progress-panel">
                <div class="progress-panel__header">
                  <span>学习完成度</span>
                  <strong>{{ getCourseProgress(course.id) }}%</strong>
                </div>
                <el-progress
                  :percentage="getCourseProgress(course.id)"
                  :stroke-width="8"
                  :show-text="false"
                  :color="course.status === 1 ? '#4f7df0' : '#a6afbf'"
                />
              </div>

              <div class="course-actions">
                <el-button type="primary" @click.stop="toCourseDetail(course.id)">查看详情</el-button>
                <el-button @click.stop="viewMaterials(course)">学习资料</el-button>
              </div>
              <div class="course-shortcuts">
                <button type="button" class="shortcut-btn" @click.stop="goHomework(course)">
                  <el-icon><EditPen /></el-icon>
                  <span>作业中心</span>
                </button>
                <button type="button" class="shortcut-btn" @click.stop="goGrade(course)">
                  <el-icon><DataLine /></el-icon>
                  <span>成绩查询</span>
                </button>
              </div>
            </div>
          </el-col>
        </el-row>

        <el-empty
          v-else-if="!loading"
          description="你还没有加入任何课程，请联系你的班主任"
        />

        <div v-if="filteredCourses.length > 0" class="pagination-container">
          <el-pagination
            :current-page="queryParams.current"
            :page-size="queryParams.size"
            :page-sizes="[8, 12, 16, 20]"
            :total="filteredCourses.length"
            layout="total, sizes, prev, pager, next"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  CircleCheckFilled,
  DataLine,
  EditPen,
  RefreshRight,
  School,
  Search,
  User,
  VideoPlay
} from '@element-plus/icons-vue'
import { getMyStudentCourses, type CourseVO } from '@/api/course'
import { getStudentHomeworkList, type HomeworkVO } from '@/api/homework'
import { getStudentMaterials, type MaterialVO } from '@/api/material'
import { useUserStore } from '@/stores/user'
import { resolveMediaUrl } from '@/utils/media'

interface CourseLearningProgress {
  currentId?: number
  completedIds: number[]
}

const router = useRouter()
const userStore = useUserStore()

const queryParams = reactive({
  current: 1,
  size: 12,
  keyword: '',
  status: undefined as number | undefined,
  sortBy: 'updatedAt' as 'updatedAt' | 'courseName',
  sortOrder: 'desc' as 'asc' | 'desc'
})

const allCourses = ref<CourseVO[]>([])
const pagedCourses = ref<CourseVO[]>([])
const loading = ref(false)
const pendingHomeworkList = ref<HomeworkVO[]>([])
const materialMap = ref<Record<number, MaterialVO[]>>({})

const filteredCourses = computed(() => {
  const keyword = queryParams.keyword.trim().toLowerCase()
  const next = allCourses.value.filter((course) => {
    const matchKeyword = !keyword
      || (course.courseName || '').toLowerCase().includes(keyword)
      || (course.teacherName || '').toLowerCase().includes(keyword)
    const matchStatus = queryParams.status === undefined || course.status === queryParams.status
    return matchKeyword && matchStatus
  })

  return next.sort((a, b) => {
    if (queryParams.sortBy === 'courseName') {
      const result = (a.courseName || '').localeCompare(b.courseName || '', 'zh-CN')
      return queryParams.sortOrder === 'asc' ? result : -result
    }
    const timeA = getTime(a.updatedAt || a.createdAt || a.startTime || a.createTime)
    const timeB = getTime(b.updatedAt || b.createdAt || b.startTime || b.createTime)
    return queryParams.sortOrder === 'asc' ? timeA - timeB : timeB - timeA
  })
})

const progressStorageKey = (courseId: number) => `student-course-progress:${userStore.userInfo?.id || 'guest'}:${courseId}`

const getLearningProgress = (courseId: number): CourseLearningProgress => {
  try {
    const raw = window.localStorage.getItem(progressStorageKey(courseId))
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

const getCourseMaterialsList = (courseId: number) => materialMap.value[courseId] || []

const getTotalChapterCount = (courseId: number) => getCourseMaterialsList(courseId).length

const getCompletedChapterCount = (courseId: number) => {
  const progress = getLearningProgress(courseId)
  const materialIds = new Set(getCourseMaterialsList(courseId).map((item) => item.id))
  return progress.completedIds.filter((id) => materialIds.has(id)).length
}

const getCourseProgress = (courseId: number) => {
  const totalCount = getTotalChapterCount(courseId)
  if (!totalCount) return 0
  return Math.round((getCompletedChapterCount(courseId) / totalCount) * 100)
}

const getNearestDeadlineText = (courseId: number) => {
  const pendingRows = pendingHomeworkList.value
    .filter((item) => item.courseId === courseId && item.deadline)
    .sort((a, b) => getTime(a.deadline) - getTime(b.deadline))
  if (!pendingRows.length) {
    return '暂无待提交作业'
  }
  return formatDateTime(pendingRows[0].deadline)
}

const loadCourses = async () => {
  loading.value = true
  try {
    const [coursesRes, pendingRes] = await Promise.all([
      getMyStudentCourses(),
      getStudentHomeworkList(0)
    ])

    allCourses.value = coursesRes || []
    pendingHomeworkList.value = pendingRes || []

    await loadCourseMaterials()
    syncPagedCourses()
  } catch (error) {
    console.error('加载课程失败', error)
    ElMessage.error('加载课程失败，请稍后重试')
  } finally {
    loading.value = false
  }
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

const syncPagedCourses = () => {
  const maxPage = Math.max(1, Math.ceil(filteredCourses.value.length / queryParams.size))
  if (queryParams.current > maxPage) {
    queryParams.current = maxPage
  }
  const start = (queryParams.current - 1) * queryParams.size
  const end = start + queryParams.size
  pagedCourses.value = filteredCourses.value.slice(start, end)
}

const handleSearch = () => {
  queryParams.current = 1
  syncPagedCourses()
}

const handleReset = () => {
  queryParams.keyword = ''
  queryParams.status = undefined
  queryParams.sortBy = 'updatedAt'
  queryParams.sortOrder = 'desc'
  queryParams.current = 1
  syncPagedCourses()
}

const handleSizeChange = (val: number) => {
  queryParams.size = val
  syncPagedCourses()
}

const handleCurrentChange = (val: number) => {
  queryParams.current = val
  syncPagedCourses()
}

const toCourseDetail = (courseId: number) => {
  router.push(`/student/course-detail/${courseId}`)
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

onMounted(() => {
  loadCourses()
})
</script>

<style scoped>
.my-course {
  padding: 20px;
}

.toolbar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  color: #253652;
}

.header-subtitle {
  margin-top: 6px;
  color: #7f8ca8;
  font-size: 13px;
}

.search-card {
  margin-bottom: 16px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.course-content {
  min-height: 260px;
}

.course-col {
  margin-bottom: 20px;
}

.course-item {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  border: 1px solid #e4eaf2;
  border-radius: 16px;
  background: #fff;
  cursor: pointer;
  transition: transform 0.24s ease, box-shadow 0.24s ease, border-color 0.24s ease;
}

.course-item:hover {
  transform: translateY(-3px);
  border-color: #d2def2;
  box-shadow: 0 12px 28px rgba(31, 52, 87, 0.1);
}

.course-cover {
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

.course-cover__mask {
  position: absolute;
  inset: 0;
  z-index: 1;
  background: linear-gradient(180deg, rgba(13, 27, 53, 0.18) 0%, rgba(13, 27, 53, 0.82) 100%);
}

.course-cover__top,
.course-cover__bottom {
  position: relative;
  z-index: 2;
}

.course-cover__top {
  display: flex;
  justify-content: flex-end;
}

.course-cover__bottom {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 8px;
  height: calc(100% - 28px);
}

.course-title {
  color: #fff;
  font-size: 20px;
  line-height: 1.45;
  font-weight: 700;
}

.course-desc {
  color: rgba(255, 255, 255, 0.85);
  font-size: 13px;
  line-height: 1.7;
  display: -webkit-box;
  overflow: hidden;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.status-tag :deep(.el-icon) {
  margin-right: 4px;
}

.course-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 16px;
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: 13px;
}

.info-label {
  color: #7f8ca8;
  flex-shrink: 0;
}

.info-value {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #33435e;
  font-weight: 500;
  text-align: right;
}

.deadline-value {
  color: #d97b47;
}

.progress-panel {
  margin: 0 16px 16px;
  padding: 14px;
  border-radius: 12px;
  border: 1px solid #e7eef8;
  background: #f7faff;
}

.progress-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
  color: #53627d;
  font-size: 13px;
}

.progress-panel__header strong {
  color: #253652;
  font-size: 16px;
}

.course-actions {
  display: flex;
  gap: 12px;
  padding: 0 16px 12px;
}

.course-actions .el-button {
  flex: 1;
}

.course-shortcuts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  padding: 0 16px 16px;
}

.shortcut-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 42px;
  border: 1px solid #e4eaf2;
  border-radius: 12px;
  background: #fff;
  color: #4a5a75;
  cursor: pointer;
  transition: background 0.2s ease, border-color 0.2s ease, color 0.2s ease;
}

.shortcut-btn:hover {
  background: #f5f8fd;
  border-color: #d6e2f4;
  color: #2d4f88;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .my-course {
    padding: 16px;
  }

  .toolbar-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .course-shortcuts {
    grid-template-columns: 1fr;
  }
}
</style>
