<template>
  <div class="teacher-my-course">
    <el-card class="search-card" shadow="never">
      <div class="toolbar-header">
        <div>
          <div class="header-title">我的课程</div>
          <div class="header-subtitle">聚合课程概况、进度和操作入口，提升管理效率</div>
        </div>
        <div class="toolbar-actions">
          <el-button @click="selectionMode = !selectionMode">
            {{ selectionMode ? '退出批量' : '批量管理' }}
          </el-button>
          <el-button type="primary" @click="openCreateDialog">
            <el-icon><Plus /></el-icon>
            创建课程
          </el-button>
        </div>
      </div>

      <el-form :inline="true" :model="filters" class="toolbar-form">
        <el-form-item label="课程名称">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索课程名称 / 班级名称"
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="课程状态">
          <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="进行中" :value="1" />
            <el-option label="已结课" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属班级">
          <el-select v-model="filters.classId" placeholder="全部班级" clearable style="width: 180px">
            <el-option
              v-for="item in classOptions"
              :key="item.id"
              :label="item.className"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-select v-model="filters.sortOrder" style="width: 140px">
            <el-option label="最新优先" value="desc" />
            <el-option label="最早优先" value="asc" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <div class="toolbar-stats">
        <div class="stat-chip">
          <span class="stat-chip__label">课程总数</span>
          <strong>{{ filteredCourses.length }}</strong>
        </div>
        <div class="stat-chip">
          <span class="stat-chip__label">进行中</span>
          <strong>{{ activeCourseCount }}</strong>
        </div>
        <div class="stat-chip">
          <span class="stat-chip__label">已结课</span>
          <strong>{{ closedCourseCount }}</strong>
        </div>
        <div class="stat-chip">
          <span class="stat-chip__label">覆盖班级</span>
          <strong>{{ classCoverageCount }}</strong>
        </div>
      </div>
    </el-card>

    <el-card v-if="selectionMode" class="batch-card" shadow="never">
      <div class="batch-bar">
        <div class="batch-info">
          <span>已选课程 {{ selectedIds.length }} 门</span>
          <el-button link type="primary" @click="toggleSelectCurrentPage(true)">全选当前页</el-button>
          <el-button link type="info" @click="toggleSelectCurrentPage(false)">清空当前页</el-button>
          <el-button link type="info" @click="clearSelection">清空全部</el-button>
        </div>
        <div class="batch-actions">
          <el-button
            type="warning"
            :disabled="selectedIds.length === 0"
            @click="handleBatchClose"
          >
            批量结课
          </el-button>
          <el-button
            type="danger"
            :disabled="selectedIds.length === 0"
            @click="handleBatchDelete"
          >
            批量删除
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="list-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <div class="header-title">课程列表</div>
            <div class="header-subtitle">点击课程卡片进入详情页，集中查看作业、学生与成绩概况</div>
          </div>
          <el-tag type="primary" effect="plain">当前筛选 {{ filteredCourses.length }} 门</el-tag>
        </div>
      </template>

      <div v-loading="loading">
        <el-empty v-if="!loading && filteredCourses.length === 0" description="当前还没有课程，先创建一门课程开始管理吧">
          <el-button type="primary" @click="openCreateDialog">去创建课程</el-button>
        </el-empty>

        <el-row v-else :gutter="16">
          <el-col
            v-for="course in pagedCourses"
            :key="course.id"
            :xs="24"
            :sm="12"
            :md="8"
            :xl="6"
            class="course-col"
          >
            <div class="course-card" @click="openDetail(course)">
              <div class="course-card__cover" :class="{ 'is-default-cover': !resolveCourseCoverUrl(course.coverUrl) }">
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
                <div class="cover-mask" />
                <div class="cover-top">
                  <el-checkbox
                    v-if="selectionMode"
                    :model-value="selectedIds.includes(course.id)"
                    @click.stop
                    @change="(checked: boolean) => toggleSelection(course.id, checked)"
                  />
                    <el-dropdown trigger="click" @command="(command: string | number | object) => handleCourseCommand(String(command), course)">
                    <el-button class="cover-menu-btn" @click.stop>
                      课程操作
                      <el-icon><ArrowDown /></el-icon>
                    </el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="detail">查看详情</el-dropdown-item>
                        <el-dropdown-item command="edit">编辑课程</el-dropdown-item>
                        <el-dropdown-item command="homework">管理作业</el-dropdown-item>
                        <el-dropdown-item command="students">查看学生</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
                <div class="cover-content">
                  <div class="course-name" :title="course.courseName">{{ course.courseName }}</div>
                  <el-tag
                    size="small"
                    :type="course.status === 1 ? 'success' : 'info'"
                    class="status-tag"
                  >
                    <el-icon>
                      <Reading v-if="course.status === 1" />
                      <Calendar v-else />
                    </el-icon>
                    <span>{{ course.status === 1 ? '进行中' : '已结课' }}</span>
                  </el-tag>
                </div>
              </div>

              <div class="course-body">
                <div class="meta-grid">
                  <div class="meta-item">
                    <span class="meta-item__label">所属班级</span>
                    <span class="meta-item__value">{{ course.className || '暂无班级' }}</span>
                  </div>
                  <div class="meta-item">
                    <span class="meta-item__label">学生人数</span>
                    <span class="meta-item__value">{{ course.studentCount ?? 0 }} 人</span>
                  </div>
                  <div class="meta-item">
                    <span class="meta-item__label">学分 / 课时</span>
                    <span class="meta-item__value">{{ course.credit ?? 2 }} 学分 / {{ course.courseHours ?? 32 }} 课时</span>
                  </div>
                </div>

                <div class="progress-panel">
                  <div class="progress-panel__header">
                    <span>作业完成进度</span>
                    <strong>{{ getCompletionRate(course) }}%</strong>
                  </div>
                  <el-progress
                    :percentage="getCompletionRate(course)"
                    :stroke-width="8"
                    :show-text="false"
                    :color="course.status === 1 ? '#4f7df0' : '#8b97ad'"
                  />
                  <div class="progress-panel__footer">
                    <span>作业 {{ course.assignmentCount ?? 0 }} 个</span>
                    <span>活跃 {{ course.activeAssignmentCount ?? 0 }} 个</span>
                  </div>
                </div>

                <div class="course-desc">{{ course.description || '暂无课程简介，建议补充课程目标、教学安排和考核方式。' }}</div>
              </div>

              <div class="course-footer">
                <el-button type="primary" @click.stop="openDetail(course)">查看详情</el-button>
                <el-button @click.stop="goToHomework(course)">管理作业</el-button>
              </div>
            </div>
          </el-col>
        </el-row>

        <div v-if="filteredCourses.length > 0" class="pagination-wrapper">
          <el-pagination
            :current-page="pagination.current"
            :page-size="pagination.size"
            :page-sizes="[8, 12, 16, 20]"
            :total="filteredCourses.length"
            layout="total, sizes, prev, pager, next"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑课程' : '创建课程'"
      width="760px"
      :close-on-click-modal="false"
      destroy-on-close
      @closed="handleDialogClosed"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-position="top" class="course-form">
        <div class="form-grid">
          <div class="cover-uploader">
            <div class="cover-preview" :style="getCoverStyle(coverPreviewUrl)">
              <div class="cover-preview__mask" />
              <div class="cover-preview__text">
                <el-icon><Picture /></el-icon>
                <span>{{ coverPreviewUrl ? '课程封面预览' : '上传课程封面' }}</span>
              </div>
            </div>
            <el-upload
              :show-file-list="false"
              :auto-upload="true"
              accept="image/png,image/jpeg,image/jpg,image/gif,image/webp"
              :http-request="handleCoverUpload"
            >
              <el-button :loading="coverUploading">上传封面</el-button>
            </el-upload>
            <div class="form-tip">支持 PNG/JPG/JPEG/GIF/WEBP，大小不超过 5MB</div>
          </div>

          <div>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="课程名称" prop="courseName">
                  <el-input v-model="formData.courseName" maxlength="50" show-word-limit placeholder="请输入课程名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="所属班级" prop="classId">
                  <el-select v-model="formData.classId" placeholder="请选择班级" style="width: 100%">
                    <el-option
                      v-for="item in classOptions"
                      :key="item.id"
                      :label="item.className"
                      :value="item.id"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="授课教师">
                  <el-input :model-value="teacherName" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="课程状态" prop="status">
                  <el-radio-group v-model="formData.status">
                    <el-radio :label="1">进行中</el-radio>
                    <el-radio :label="0">已结课</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="学分" prop="credit">
                  <el-input-number v-model="formData.credit" :min="0" :max="20" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="课时" prop="courseHours">
                  <el-input-number v-model="formData.courseHours" :min="0" :max="200" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="课程简介" prop="description">
                  <el-input
                    v-model="formData.description"
                    type="textarea"
                    :rows="5"
                    maxlength="500"
                    show-word-limit
                    placeholder="请输入课程简介、教学安排或考核说明"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '保存修改' : '确认创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import type { UploadRequestOptions } from 'element-plus'
import {
  ArrowDown,
  Calendar,
  Picture,
  Plus,
  Reading,
  RefreshRight,
  Search
} from '@element-plus/icons-vue'
import {
  addCourse,
  batchDeleteCourses,
  batchUpdateCourseStatus,
  getMyTeacherCourses,
  type CourseDTO,
  type CourseVO,
  updateCourse,
  uploadCourseCover
} from '@/api/course'
import { getMyTeacherClasses, type ClassVO } from '@/api/class'
import { useUserStore } from '@/stores/user'
import { resolveMediaUrl } from '@/utils/media'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const submitLoading = ref(false)
const coverUploading = ref(false)

const courseList = ref<CourseVO[]>([])
const classOptions = ref<ClassVO[]>([])

const selectionMode = ref(false)
const selectedIds = ref<number[]>([])

const pagination = reactive({
  current: 1,
  size: 8
})

const filters = reactive({
  keyword: '',
  status: undefined as number | undefined,
  classId: undefined as number | undefined,
  sortOrder: 'desc' as 'asc' | 'desc'
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const coverPreviewUrl = ref('')

const formData = reactive<CourseDTO>({
  courseName: '',
  classId: undefined,
  credit: 2,
  courseHours: 32,
  description: '',
  coverUrl: '',
  status: 1
})

const formRules: FormRules = {
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  classId: [{ required: true, message: '请选择所属班级', trigger: 'change' }]
}

const teacherName = computed(() => userStore.userInfo?.realName || '当前教师')

const filteredCourses = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()
  const next = courseList.value.filter((course) => {
    const matchKeyword = !keyword
      || (course.courseName || '').toLowerCase().includes(keyword)
      || (course.className || '').toLowerCase().includes(keyword)
    const matchStatus = filters.status === undefined || course.status === filters.status
    const matchClass = filters.classId === undefined || course.classId === filters.classId
    return matchKeyword && matchStatus && matchClass
  })

  return next.sort((a, b) => {
    const timeA = getTime(a.createdAt || a.createTime || a.startTime)
    const timeB = getTime(b.createdAt || b.createTime || b.startTime)
    return filters.sortOrder === 'asc' ? timeA - timeB : timeB - timeA
  })
})

const pagedCourses = computed(() => {
  const start = (pagination.current - 1) * pagination.size
  return filteredCourses.value.slice(start, start + pagination.size)
})

const activeCourseCount = computed(() => filteredCourses.value.filter((item) => item.status === 1).length)
const closedCourseCount = computed(() => filteredCourses.value.filter((item) => item.status === 0).length)
const classCoverageCount = computed(() => new Set(filteredCourses.value.map((item) => item.classId)).size)

const loadCourses = async () => {
  loading.value = true
  try {
    courseList.value = (await getMyTeacherCourses()) || []
  } catch (error) {
    console.error('加载教师课程失败', error)
    ElMessage.error('加载课程失败，请稍后重试')
    courseList.value = []
  } finally {
    loading.value = false
  }
}

const loadClasses = async () => {
  try {
    classOptions.value = (await getMyTeacherClasses()) || []
  } catch (error) {
    console.error('加载教师班级失败', error)
    classOptions.value = []
  }
}

const handleSearch = () => {
  pagination.current = 1
}

const handleReset = () => {
  filters.keyword = ''
  filters.status = undefined
  filters.classId = undefined
  filters.sortOrder = 'desc'
  pagination.current = 1
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
}

const handleCurrentChange = (page: number) => {
  pagination.current = page
}

const openDetail = (course: CourseVO, tab?: 'overview' | 'students' | 'homework') => {
  router.push({
    path: `/teacher/my-course/${course.id}`,
    query: tab ? { tab } : undefined
  })
}

const goToHomework = (course: CourseVO) => {
  if (course.status === 0) {
    ElMessage.warning('该课程已结课，可查看历史作业，但不能继续新增作业')
  }
  router.push({
    path: '/teacher/homework',
    query: { courseId: String(course.id) }
  })
}

const handleCourseCommand = (command: string, course: CourseVO) => {
  if (command === 'detail') {
    openDetail(course)
    return
  }
  if (command === 'edit') {
    openEditDialog(course)
    return
  }
  if (command === 'homework') {
    goToHomework(course)
    return
  }
  if (command === 'students') {
    openDetail(course, 'students')
  }
}

const toggleSelection = (id: number, checked: boolean) => {
  if (checked) {
    if (!selectedIds.value.includes(id)) {
      selectedIds.value = [...selectedIds.value, id]
    }
    return
  }
  selectedIds.value = selectedIds.value.filter((item) => item !== id)
}

const toggleSelectCurrentPage = (checked: boolean) => {
  const currentIds = pagedCourses.value.map((item) => item.id)
  if (checked) {
    selectedIds.value = Array.from(new Set([...selectedIds.value, ...currentIds]))
    return
  }
  selectedIds.value = selectedIds.value.filter((item) => !currentIds.includes(item))
}

const clearSelection = () => {
  selectedIds.value = []
}

const handleBatchClose = async () => {
  await ElMessageBox.confirm(`确认将已选中的 ${selectedIds.value.length} 门课程批量设为结课吗？`, '批量结课确认', {
    type: 'warning'
  })
  await batchUpdateCourseStatus({ ids: selectedIds.value, status: 0 })
  ElMessage.success('批量结课成功')
  clearSelection()
  await loadCourses()
}

const handleBatchDelete = async () => {
  await ElMessageBox.confirm(`确认删除已选中的 ${selectedIds.value.length} 门课程吗？删除前请确认历史数据已处理。`, '批量删除确认', {
    type: 'warning'
  })
  await batchDeleteCourses(selectedIds.value)
  ElMessage.success('批量删除成功')
  clearSelection()
  await loadCourses()
}

const resetFormData = () => {
  Object.assign(formData, {
    id: undefined,
    courseName: '',
    classId: undefined,
    credit: 2,
    courseHours: 32,
    description: '',
    coverUrl: '',
    status: 1
  })
  coverPreviewUrl.value = ''
}

const openCreateDialog = () => {
  isEdit.value = false
  resetFormData()
  dialogVisible.value = true
}

const openEditDialog = (course: CourseVO) => {
  isEdit.value = true
  Object.assign(formData, {
    id: course.id,
    courseName: course.courseName,
    classId: course.classId,
    credit: course.credit ?? 2,
    courseHours: course.courseHours ?? 32,
    description: course.description || '',
    coverUrl: course.coverUrl || '',
    status: course.status
  })
  coverPreviewUrl.value = course.coverUrl || ''
  dialogVisible.value = true
}

const handleDialogClosed = () => {
  formRef.value?.clearValidate()
}

const handleCoverUpload = async (options: UploadRequestOptions) => {
  coverUploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    const coverUrl = await uploadCourseCover(formData)
    coverPreviewUrl.value = coverUrl
    formDataAppendCover(coverUrl)
    ElMessage.success('课程封面上传成功')
  } catch (error: any) {
    ElMessage.error(error?.message || '课程封面上传失败')
  } finally {
    coverUploading.value = false
  }
}

const formDataAppendCover = (coverUrl: string) => {
  formData.coverUrl = coverUrl
}

const handleSubmit = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const payload: CourseDTO = {
      ...formData,
      description: formData.description?.trim() || undefined,
      coverUrl: formData.coverUrl || undefined
    }
    if (isEdit.value) {
      await updateCourse(payload)
      ElMessage.success('课程信息已更新')
    } else {
      await addCourse(payload)
      ElMessage.success('课程创建成功')
    }
    dialogVisible.value = false
    clearSelection()
    await loadCourses()
  } catch (error: any) {
    ElMessage.error(error?.message || '保存课程失败')
  } finally {
    submitLoading.value = false
  }
}

const getCompletionRate = (course: CourseVO) => {
  const value = Number(course.completionRate ?? 0)
  if (Number.isNaN(value)) return 0
  return Math.max(0, Math.min(100, Math.round(value)))
}

const resolveCourseCoverUrl = (coverUrl?: string) => resolveMediaUrl(coverUrl)

const getCoverStyle = (coverUrl?: string) => {
  const resolvedCoverUrl = resolveCourseCoverUrl(coverUrl)
  if (resolvedCoverUrl) {
    return {
      backgroundImage: `url(${resolvedCoverUrl})`
    }
  }
  return {
    background: 'linear-gradient(135deg, #1d4f91 0%, #4f7df0 100%)'
  }
}

const getTime = (value?: string) => {
  if (!value) return 0
  const timestamp = new Date(value).getTime()
  return Number.isNaN(timestamp) ? 0 : timestamp
}

watch(filteredCourses, (list) => {
  const maxPage = Math.max(1, Math.ceil(list.length / pagination.size))
  if (pagination.current > maxPage) {
    pagination.current = maxPage
  }
})

watch(selectionMode, (enabled) => {
  if (!enabled) {
    clearSelection()
  }
})

onMounted(async () => {
  await Promise.all([loadCourses(), loadClasses()])
})
</script>

<style scoped>
.teacher-my-course {
  padding: 0;
}

.toolbar-header,
.card-header,
.batch-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.toolbar-actions,
.batch-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toolbar-form {
  margin-top: 18px;
}

.toolbar-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 6px;
}

.stat-chip {
  min-width: 120px;
  padding: 10px 14px;
  border: 1px solid #e4eaf2;
  border-radius: 12px;
  background: #f8fbff;
}

.stat-chip__label {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  color: #7f8ca8;
}

.stat-chip strong {
  font-size: 18px;
  color: #22314d;
}

.batch-card {
  margin-bottom: 16px;
}

.batch-info {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  color: #53627d;
}

.course-col {
  margin-bottom: 16px;
}

.course-card {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  border: 1px solid #e4eaf2;
  border-radius: 16px;
  background: #fff;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.course-card:hover {
  transform: translateY(-2px);
  border-color: #d2def2;
  box-shadow: 0 10px 26px rgba(31, 52, 87, 0.08);
}

.course-card__cover {
  position: relative;
  min-height: 176px;
  padding: 16px;
  background: linear-gradient(135deg, #1d4f91 0%, #4f7df0 100%);
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
  background: linear-gradient(135deg, #1d4f91 0%, #4f7df0 100%);
}

.cover-mask {
  position: absolute;
  inset: 0;
  z-index: 1;
  background: linear-gradient(180deg, rgba(16, 31, 61, 0.2) 0%, rgba(15, 28, 58, 0.82) 100%);
}

.cover-top,
.cover-content {
  position: relative;
  z-index: 2;
}

.cover-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.cover-menu-btn {
  border-color: rgba(255, 255, 255, 0.22);
  background: rgba(255, 255, 255, 0.14);
  color: #fff;
}

.cover-content {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 12px;
  height: calc(100% - 36px);
}

.course-name {
  font-size: 20px;
  line-height: 1.45;
  font-weight: 700;
  color: #fff;
}

.status-tag :deep(.el-icon) {
  margin-right: 4px;
}

.course-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 18px;
  flex: 1;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.meta-item {
  padding: 12px;
  border: 1px solid #edf2fb;
  border-radius: 12px;
  background: #fbfcff;
}

.meta-item__label {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  color: #7f8ca8;
}

.meta-item__value {
  font-size: 14px;
  line-height: 1.6;
  color: #253652;
  font-weight: 600;
}

.progress-panel {
  padding: 14px;
  border-radius: 12px;
  background: #f7faff;
  border: 1px solid #e8eef8;
}

.progress-panel__header,
.progress-panel__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.progress-panel__header {
  margin-bottom: 10px;
  color: #4a5a75;
  font-size: 13px;
}

.progress-panel__header strong {
  font-size: 16px;
  color: #1f2f49;
}

.progress-panel__footer {
  margin-top: 10px;
  color: #7f8ca8;
  font-size: 12px;
}

.course-desc {
  min-height: 64px;
  color: #53627d;
  line-height: 1.8;
  font-size: 13px;
}

.course-footer {
  display: flex;
  gap: 10px;
  padding: 0 18px 18px;
}

.course-footer .el-button {
  flex: 1;
}

.form-grid {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 20px;
}

.cover-uploader {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cover-preview {
  position: relative;
  height: 260px;
  border-radius: 16px;
  overflow: hidden;
  background: linear-gradient(135deg, #1d4f91 0%, #4f7df0 100%);
  background-size: cover;
  background-position: center;
}

.cover-preview__mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(24, 39, 74, 0.1) 0%, rgba(24, 39, 74, 0.7) 100%);
}

.cover-preview__text {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
  font-size: 14px;
}

.cover-preview__text .el-icon {
  font-size: 28px;
}

.form-tip {
  font-size: 12px;
  line-height: 1.7;
  color: #7f8ca8;
}

@media (max-width: 992px) {
  .form-grid,
  .meta-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .toolbar-header,
  .card-header,
  .batch-bar {
    align-items: flex-start;
    flex-direction: column;
  }

  .toolbar-actions,
  .batch-actions {
    width: 100%;
    flex-wrap: wrap;
  }
}
</style>
