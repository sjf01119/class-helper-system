<template>
  <div class="homework-manage">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="作业标题">
          <el-input v-model="queryParams.title" placeholder="请输入作业标题" clearable style="width: 220px" />
        </el-form-item>
        <el-form-item label="所属课程">
          <el-select v-model="queryParams.courseId" placeholder="全部课程" clearable style="width: 180px">
            <el-option v-for="course in courseList" :key="course.id" :label="course.courseName" :value="course.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="已发布" :value="1" />
            <el-option label="已截止" :value="2" />
            <el-option label="已关闭" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>重置
          </el-button>
          <el-button type="success" @click="handleAdd">
            <el-icon><Plus /></el-icon>发布作业
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="homeworkList" v-loading="loading" border stripe>
        <el-table-column type="index" label="序号" width="64" align="center" />
        <el-table-column prop="title" label="作业标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="courseName" label="所属课程" min-width="180" show-overflow-tooltip />
        <el-table-column label="截止时间" width="176" align="center">
          <template #default="{ row }">
            {{ formatTime(row.deadline) }}
          </template>
        </el-table-column>
        <el-table-column label="提交情况" width="138" align="center">
          <template #default="{ row }">
            <button class="count-pill primary" type="button" @click="viewSubmissions(row)">
              {{ row.submittedCount || 0 }}/{{ row.totalCount || 0 }}
            </button>
          </template>
        </el-table-column>
        <el-table-column label="待批改" width="138" align="center">
          <template #default="{ row }">
            <button class="count-pill warning" type="button" @click="viewSubmissions(row)">
              {{ row.pendingCount || 0 }} 份
            </button>
          </template>
        </el-table-column>
        <el-table-column label="总分" width="90" align="center">
          <template #default="{ row }">
            {{ row.totalScore ?? row.maxScore ?? 100 }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.statusText)" effect="light">{{ row.statusText || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="176" align="center">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="success" @click="viewSubmissions(row)">批改</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="860px"
      class="publish-dialog"
      :close-on-click-modal="false"
      destroy-on-close
      @closed="handleDialogClosed"
    >
      <el-alert
        class="dialog-alert"
        type="info"
        :closable="false"
        title="发布后将自动关联所选课程，并同步展示给该课程所属班级的学生。"
      />

      <el-form ref="formRef" :model="formData" :rules="formRules" label-position="top" class="dialog-form">
        <section class="form-section">
          <div class="section-head">
            <div class="section-title">基本信息</div>
            <div class="section-desc">填写作业标题、课程和截止时间</div>
          </div>
          <el-row :gutter="16">
            <el-col :span="24">
              <el-form-item label="作业标题" prop="title">
                <el-input
                  v-model="formData.title"
                  placeholder="请输入 1-100 字的作业标题"
                  maxlength="100"
                  show-word-limit
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="所属课程" prop="courseId">
                <el-select v-model="formData.courseId" placeholder="请选择当前授课课程" style="width: 100%">
                  <el-option
                    v-for="course in activeCourseOptions"
                    :key="course.id"
                    :label="course.courseName"
                    :value="course.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="截止时间" prop="deadline">
                <el-date-picker
                  v-model="formData.deadline"
                  type="datetime"
                  placeholder="请选择晚于当前时间的截止时间"
                  style="width: 100%"
                  value-format="YYYY-MM-DD HH:mm:ss"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section class="form-section">
          <div class="section-head">
            <div class="section-title">作业说明</div>
            <div class="section-desc">说明作业要求、答题提示或参考信息</div>
          </div>
          <el-form-item label="作业内容" prop="content">
            <el-input
              v-model="formData.content"
              type="textarea"
              :rows="7"
              placeholder="请输入 1-2000 字的作业说明"
              maxlength="2000"
              show-word-limit
            />
          </el-form-item>
        </section>

        <section class="form-section">
          <div class="section-head">
            <div class="section-title">附件</div>
            <div class="section-desc">支持多文件上传，上传中将自动锁定提交按钮</div>
          </div>
          <div class="attachment-panel">
            <div class="attachment-toolbar">
              <el-upload
                action="#"
                multiple
                :show-file-list="false"
                :before-upload="beforeAttachmentUpload"
                :http-request="uploadAttachment"
                :disabled="attachmentItems.length >= MAX_ATTACHMENT_COUNT"
              >
                <el-button type="primary" plain :disabled="attachmentItems.length >= MAX_ATTACHMENT_COUNT">
                  <el-icon><Upload /></el-icon>上传附件
                </el-button>
              </el-upload>
              <span class="attachment-tip">
                支持 Word、PDF、PPT、Excel、图片，单个文件不超过 100MB，最多 {{ MAX_ATTACHMENT_COUNT }} 个
              </span>
            </div>

            <div v-if="attachmentItems.length" class="attachment-list">
              <div v-for="item in attachmentItems" :key="item.uid" class="attachment-item">
                <div class="attachment-main">
                  <div class="attachment-name" :title="item.name">{{ item.name }}</div>
                  <div class="attachment-meta">
                    <span>{{ formatFileSize(item.size) }}</span>
                    <span>{{ item.progress >= 100 ? '上传完成' : `上传中 ${item.progress}%` }}</span>
                  </div>
                  <el-progress
                    :percentage="item.progress"
                    :stroke-width="6"
                    :status="item.progress >= 100 ? 'success' : undefined"
                  />
                </div>
                <div class="attachment-actions">
                  <el-button link type="primary" :disabled="!item.url" @click="openAttachment(item, false)">预览</el-button>
                  <el-button link type="success" :disabled="!item.url" @click="openAttachment(item, true)">下载</el-button>
                  <el-button
                    link
                    type="danger"
                    :disabled="item.progress < 100 || attachmentRemoving"
                    @click="removeAttachment(item)"
                  >
                    删除
                  </el-button>
                </div>
              </div>
            </div>

            <el-empty v-else description="暂未上传附件" :image-size="72" />
          </div>
        </section>

        <section class="form-section">
          <div class="section-head">
            <div class="section-title">总分</div>
            <div class="section-desc">默认 100 分，支持按钮调节和手动输入</div>
          </div>
          <el-form-item label="总分" prop="totalScore" class="score-form-item">
            <el-input-number
              v-model="formData.totalScore"
              :min="1"
              :max="100"
              :step="1"
              controls-position="right"
            />
            <span class="score-tip">分值范围 1-100</span>
          </el-form-item>
        </section>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="submitDisabled" :loading="submitLoading" @click="handleSubmit">
          {{ submitLoading ? '提交中...' : '确定' }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="submissionDialogVisible" title="作业提交列表" width="980px" destroy-on-close>
      <div class="submission-header" v-if="currentHomework">
        <span>作业：{{ currentHomework.title }}</span>
        <span>课程：{{ currentHomework.courseName }}</span>
        <span>待批改：{{ currentHomework.pendingCount || 0 }} 份</span>
      </div>
      <el-table :data="submissionList" v-loading="submissionLoading" border stripe max-height="520">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="studentNo" label="学号" width="140" />
        <el-table-column label="提交内容" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.content || '无文字内容' }}
          </template>
        </el-table-column>
        <el-table-column label="附件" width="180" align="center">
          <template #default="{ row }">
            <div v-if="row.attachmentList?.length" class="submission-attachment">
              <el-button link type="primary" @click="openAttachment(row.attachmentList[0], false)">
                {{ row.attachmentList[0].name }}
              </el-button>
              <span v-if="row.attachmentList.length > 1" class="text-gray">等 {{ row.attachmentList.length }} 个</span>
            </div>
            <span v-else class="text-gray">-</span>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="176" align="center">
          <template #default="{ row }">
            {{ formatTime(row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column label="得分" width="90" align="center">
          <template #default="{ row }">
            <span :class="row.score != null ? 'score-text' : 'text-gray'">
              {{ row.score != null ? row.score : '未批改' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleGrade(row)">批改</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="gradeDialogVisible" title="作业批改" width="520px" destroy-on-close>
      <div v-if="currentSubmission" class="grade-info">
        <div class="info-row">
          <span class="label">学生：</span>
          <span class="value">{{ currentSubmission.studentName }} ({{ currentSubmission.studentNo }})</span>
        </div>
        <div class="info-row">
          <span class="label">提交时间：</span>
          <span class="value">{{ formatTime(currentSubmission.submitTime) }}</span>
        </div>
        <div class="info-row">
          <span class="label">提交内容：</span>
          <div class="content-box">{{ currentSubmission.content || '无文字内容' }}</div>
        </div>
      </div>
      <el-form :model="gradeForm" label-width="80px" class="grade-form">
        <el-form-item label="得分">
          <el-input-number v-model="gradeForm.score" :min="0" :max="currentHomework?.totalScore || 100" :step="1" />
          <span class="score-tip">/ {{ currentHomework?.totalScore || 100 }} 分</span>
        </el-form-item>
        <el-form-item label="评语">
          <el-input
            v-model="gradeForm.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入评语"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="gradeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="gradeLoading" @click="submitGrade">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules, UploadRequestOptions } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshRight, Plus, Upload } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { useRoute } from 'vue-router'
import { getMyTeacherCourses, type CourseVO } from '@/api/course'
import {
  deleteHomework,
  deleteHomeworkAttachment,
  downloadHomeworkAttachmentByUrl,
  getHomeworkAttachmentAccessUrl,
  getHomeworkList,
  getHomeworkSubmissions,
  gradeHomework,
  publishHomework,
  type AttachmentItem,
  type HomeworkDTO,
  type HomeworkSubmissionVO,
  type HomeworkVO,
  updateHomework,
  uploadHomeworkAttachments
} from '@/api/homework'

interface AttachmentRecord extends AttachmentItem {
  uid: string
  progress: number
}

const route = useRoute()
const MAX_ATTACHMENT_COUNT = 10
const MAX_ATTACHMENT_SIZE = 100 * 1024 * 1024
const ALLOWED_ATTACHMENT_EXTENSIONS = ['pdf', 'doc', 'docx', 'ppt', 'pptx', 'xls', 'xlsx', 'jpg', 'jpeg', 'png', 'gif', 'webp']

const loading = ref(false)
const submitLoading = ref(false)
const submissionLoading = ref(false)
const gradeLoading = ref(false)
const attachmentRemoving = ref(false)

const homeworkList = ref<HomeworkVO[]>([])
const submissionList = ref<HomeworkSubmissionVO[]>([])
const courseList = ref<CourseVO[]>([])
const total = ref(0)

const queryParams = reactive({
  current: 1,
  size: 10,
  title: '',
  courseId: undefined as number | undefined,
  status: undefined as number | undefined
})

const dialogVisible = ref(false)
const submissionDialogVisible = ref(false)
const gradeDialogVisible = ref(false)
const isEdit = ref(false)
const dialogSaved = ref(false)
const dialogTitle = computed(() => (isEdit.value ? '编辑作业' : '发布作业'))

const formRef = ref<FormInstance>()
const currentHomework = ref<HomeworkVO>()
const currentSubmission = ref<HomeworkSubmissionVO>()
const attachmentItems = ref<AttachmentRecord[]>([])
const originalAttachmentUrls = ref<string[]>([])

const formData = reactive<HomeworkDTO>({
  title: '',
  courseId: 0,
  content: '',
  deadline: '',
  totalScore: 100,
  attachmentList: []
})

const gradeForm = reactive({
  submissionId: 0,
  score: 0,
  comment: ''
})

const hasUploadingAttachment = computed(() => attachmentItems.value.some(item => item.progress < 100))
const activeCourseOptions = computed(() => courseList.value.filter(item => item.status === 1))

const isFormValid = computed(() => {
  const title = formData.title.trim()
  const content = formData.content.trim()
  const deadline = dayjs(formData.deadline)
  return (
    title.length >= 1 &&
    title.length <= 100 &&
    !!formData.courseId &&
    deadline.isValid() &&
    deadline.isAfter(dayjs()) &&
    content.length >= 1 &&
    content.length <= 2000 &&
    Number.isInteger(formData.totalScore) &&
    formData.totalScore >= 1 &&
    formData.totalScore <= 100
  )
})

const submitDisabled = computed(() => submitLoading.value || hasUploadingAttachment.value || !isFormValid.value)

const formRules: FormRules = {
  title: [
    {
      validator: (_rule, value: string, callback) => {
        const normalized = value?.trim?.() || ''
        if (!normalized) {
          callback(new Error('请输入作业标题'))
          return
        }
        if (normalized.length > 100) {
          callback(new Error('作业标题长度需在1-100个字符之间'))
          return
        }
        callback()
      },
      trigger: ['blur', 'change']
    }
  ],
  courseId: [
    {
      validator: (_rule, value: number, callback) => {
        if (!value) {
          callback(new Error('请选择所属课程'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  deadline: [
    {
      validator: (_rule, value: string, callback) => {
        if (!value) {
          callback(new Error('请选择截止时间'))
          return
        }
        const deadline = dayjs(value)
        if (!deadline.isValid() || !deadline.isAfter(dayjs())) {
          callback(new Error('截止时间必须晚于当前时间'))
          return
        }
        callback()
      },
      trigger: ['change', 'blur']
    }
  ],
  content: [
    {
      validator: (_rule, value: string, callback) => {
        const normalized = value?.trim?.() || ''
        if (!normalized) {
          callback(new Error('请输入作业内容'))
          return
        }
        if (normalized.length > 2000) {
          callback(new Error('作业内容长度需在1-2000个字符之间'))
          return
        }
        callback()
      },
      trigger: ['blur', 'change']
    }
  ],
  totalScore: [
    {
      validator: (_rule, value: number, callback) => {
        if (!Number.isInteger(value) || value < 1 || value > 100) {
          callback(new Error('总分必须为1-100的整数'))
          return
        }
        callback()
      },
      trigger: ['blur', 'change']
    }
  ]
}

const formatTime = (time?: string) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const formatFileSize = (size?: number) => {
  if (!size) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const index = Math.min(Math.floor(Math.log(size) / Math.log(1024)), units.length - 1)
  return `${(size / Math.pow(1024, index)).toFixed(index === 0 ? 0 : 2)} ${units[index]}`
}

const normalizeDeadlineValue = (value?: string) => {
  if (!value) return ''
  const parsed = dayjs(value)
  return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm:ss') : value
}

const getStatusType = (statusText?: string) => {
  if (statusText === '已截止') return 'info'
  if (statusText === '已关闭') return 'danger'
  return 'success'
}

const resolveAccessUrl = (url?: string) => {
  if (!url) return ''
  if (/^https?:\/\//i.test(url)) return url
  const isDevApiPath = import.meta.env.DEV && url.startsWith('/api/')
  const baseOrigin = isDevApiPath
    ? `${window.location.protocol}//${window.location.hostname}:8080`
    : window.location.origin
  return new URL(url, baseOrigin).href
}

const triggerBlobDownload = (blob: Blob, fileName: string) => {
  const blobUrl = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.style.display = 'none'
  link.href = blobUrl
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(blobUrl)
}

const syncAttachmentList = () => {
  formData.attachmentList = attachmentItems.value
    .filter(item => !!item.url)
    .map(item => ({
      name: item.name,
      url: item.url,
      size: item.size
    }))
}

const loadHomeworkList = async () => {
  loading.value = true
  try {
    const res = await getHomeworkList({
      current: queryParams.current,
      size: queryParams.size,
      title: queryParams.title || undefined,
      courseId: queryParams.courseId,
      status: queryParams.status
    })
    homeworkList.value = (res?.records || []).map(item => ({
      ...item,
      totalScore: item.totalScore ?? item.maxScore ?? 100,
      attachmentList: item.attachmentList || []
    }))
    total.value = res?.total || 0
  } catch (error) {
    console.error('加载作业列表失败', error)
    ElMessage.error('加载作业列表失败')
  } finally {
    loading.value = false
  }
}

const loadCourseList = async () => {
  try {
    const res = await getMyTeacherCourses()
    courseList.value = res || []
  } catch (error) {
    console.error('加载课程列表失败', error)
    ElMessage.error('加载课程列表失败')
  }
}

const handleSearch = () => {
  queryParams.current = 1
  loadHomeworkList()
}

const handleReset = () => {
  queryParams.title = ''
  queryParams.courseId = undefined
  queryParams.status = undefined
  queryParams.current = 1
  loadHomeworkList()
}

const resetDialogForm = () => {
  dialogSaved.value = false
  Object.assign(formData, {
    id: undefined,
    title: '',
    courseId: courseList.value[0]?.id || 0,
    content: '',
    deadline: '',
    totalScore: 100,
    attachmentList: []
  })
  attachmentItems.value = []
  originalAttachmentUrls.value = []
}

const handleAdd = () => {
  isEdit.value = false
  resetDialogForm()
  dialogVisible.value = true
}

const handleEdit = (row: HomeworkVO) => {
  isEdit.value = true
  dialogSaved.value = false
  Object.assign(formData, {
    id: row.id,
    title: row.title,
    courseId: row.courseId,
    content: row.content,
    deadline: normalizeDeadlineValue(row.deadline),
    totalScore: row.totalScore ?? row.maxScore ?? 100,
    attachmentList: row.attachmentList || []
  })
  attachmentItems.value = (row.attachmentList || []).map(item => ({
    ...item,
    uid: `${item.url}-${Math.random().toString(16).slice(2)}`,
    progress: 100
  }))
  originalAttachmentUrls.value = (row.attachmentList || []).map(item => item.url)
  dialogVisible.value = true
}

const handleDelete = (row: HomeworkVO) => {
  ElMessageBox.confirm(`确定删除作业 "${row.title}" 吗？`, '提示', {
    type: 'warning'
  })
    .then(async () => {
      await deleteHomework(row.id)
      ElMessage.success('作业删除成功')
      loadHomeworkList()
    })
    .catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return
  if (hasUploadingAttachment.value) {
    ElMessage.warning('附件仍在上传中，请稍后再提交')
    return
  }
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  syncAttachmentList()
  formData.deadline = normalizeDeadlineValue(formData.deadline)
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateHomework(formData)
      ElMessage.success('作业更新成功')
    } else {
      await publishHomework(formData)
      ElMessage.success('作业发布成功')
    }
    dialogSaved.value = true
    dialogVisible.value = false
    await loadHomeworkList()
  } catch (error) {
    console.error('保存作业失败', error)
    ElMessage.error(isEdit.value ? '作业更新失败' : '作业发布失败')
  } finally {
    submitLoading.value = false
  }
}

const beforeAttachmentUpload = (rawFile: File) => {
  if (attachmentItems.value.length >= MAX_ATTACHMENT_COUNT) {
    ElMessage.warning(`最多上传 ${MAX_ATTACHMENT_COUNT} 个附件`)
    return false
  }
  const extension = rawFile.name.split('.').pop()?.toLowerCase() || ''
  if (!ALLOWED_ATTACHMENT_EXTENSIONS.includes(extension)) {
    ElMessage.error('仅支持 Word、PDF、PPT、Excel、图片格式附件')
    return false
  }
  if (rawFile.size > MAX_ATTACHMENT_SIZE) {
    ElMessage.error('单个附件不能超过 100MB')
    return false
  }
  return true
}

const uploadAttachment = async (options: UploadRequestOptions) => {
  const file = options.file as File
  const record: AttachmentRecord = {
    uid: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
    name: file.name,
    size: file.size,
    url: '',
    progress: 0
  }
  attachmentItems.value.push(record)

  try {
    const payload = new FormData()
    payload.append('files', file)
    const res = await uploadHomeworkAttachments(payload, event => {
      const percent = event.total ? Math.round((event.loaded * 100) / event.total) : 0
      record.progress = Math.min(percent, 99)
      options.onProgress?.({ percent: record.progress } as never)
    })
    const uploaded = res?.[0]
    if (!uploaded) {
      throw new Error('上传结果为空')
    }
    record.url = uploaded.url
    record.name = uploaded.name || record.name
    record.size = uploaded.size || record.size
    record.progress = 100
    syncAttachmentList()
    options.onSuccess?.(uploaded as never)
  } catch (error) {
    attachmentItems.value = attachmentItems.value.filter(item => item.uid !== record.uid)
    options.onError?.(error as any)
    ElMessage.error('附件上传失败')
  }
}

const removeAttachment = async (item: AttachmentRecord) => {
  attachmentRemoving.value = true
  try {
    const isOriginal = originalAttachmentUrls.value.includes(item.url)
    if (!isOriginal && item.url) {
      await deleteHomeworkAttachment(item.url)
    }
    attachmentItems.value = attachmentItems.value.filter(record => record.uid !== item.uid)
    syncAttachmentList()
    ElMessage.success('附件已移除')
  } catch (error) {
    console.error('删除附件失败', error)
    ElMessage.error('删除附件失败')
  } finally {
    attachmentRemoving.value = false
  }
}

const cleanupUnsavedAttachments = async () => {
  const newUploadedItems = attachmentItems.value.filter(item => item.url && !originalAttachmentUrls.value.includes(item.url))
  if (!newUploadedItems.length) return
  await Promise.allSettled(newUploadedItems.map(item => deleteHomeworkAttachment(item.url)))
}

const handleDialogClosed = async () => {
  if (!dialogSaved.value) {
    await cleanupUnsavedAttachments()
  }
  resetDialogForm()
}

const openAttachment = async (item: AttachmentItem, download = false) => {
  const pendingWindow = window.open('', '_blank')
  try {
    const accessUrl = resolveAccessUrl(
      await getHomeworkAttachmentAccessUrl({
        fileUrl: item.url,
        fileName: item.name,
        download
      })
    )
    const isLocalDownload = download && accessUrl.includes('/api/assignment/attachment/download')
    if (isLocalDownload) {
      pendingWindow?.close()
      const response = await downloadHomeworkAttachmentByUrl(accessUrl)
      triggerBlobDownload(response.data, item.name || '附件')
      return
    }
    if (pendingWindow) {
      pendingWindow.location.href = accessUrl
      return
    }
    window.location.href = accessUrl
  } catch (error) {
    pendingWindow?.close()
    console.error('获取附件地址失败', error)
    ElMessage.error(download ? '下载附件失败' : '预览附件失败')
  }
}

const viewSubmissions = async (row: HomeworkVO) => {
  currentHomework.value = row
  submissionDialogVisible.value = true
  submissionLoading.value = true
  try {
    const res = await getHomeworkSubmissions(row.id, {
      current: 1,
      size: 200
    })
    submissionList.value = res?.records || []
  } catch (error) {
    console.error('加载提交列表失败', error)
    ElMessage.error('加载提交列表失败')
  } finally {
    submissionLoading.value = false
  }
}

const handleGrade = (row: HomeworkSubmissionVO) => {
  currentSubmission.value = row
  gradeForm.submissionId = row.id
  gradeForm.score = row.score ?? 0
  gradeForm.comment = row.comment || ''
  gradeDialogVisible.value = true
}

const submitGrade = async () => {
  if (!gradeForm.submissionId) return
  gradeLoading.value = true
  try {
    await gradeHomework({
      submissionId: gradeForm.submissionId,
      score: gradeForm.score,
      feedback: gradeForm.comment
    })
    ElMessage.success('作业批改成功')
    gradeDialogVisible.value = false
    if (currentHomework.value) {
      await viewSubmissions(currentHomework.value)
    }
    await loadHomeworkList()
  } catch (error) {
    console.error('批改失败', error)
    ElMessage.error('作业批改失败')
  } finally {
    gradeLoading.value = false
  }
}

const handleSizeChange = (val: number) => {
  queryParams.size = val
  loadHomeworkList()
}

const handleCurrentChange = (val: number) => {
  queryParams.current = val
  loadHomeworkList()
}

onMounted(async () => {
  await loadCourseList()
  const courseId = Number(route.query.courseId)
  if (!Number.isNaN(courseId) && courseId > 0) {
    queryParams.courseId = courseId
  }
  await loadHomeworkList()
})
</script>

<style scoped>
.homework-manage {
  padding: 0;
}

.search-card,
.table-card {
  border-radius: 16px;
}

.search-card {
  margin-bottom: 16px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.count-pill {
  min-width: 92px;
  padding: 6px 12px;
  border: none;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.count-pill:hover {
  transform: translateY(-1px);
  opacity: 0.92;
}

.count-pill.primary {
  color: #1d4ed8;
  background: rgba(59, 130, 246, 0.14);
}

.count-pill.warning {
  color: #b45309;
  background: rgba(251, 191, 36, 0.18);
}

.dialog-alert {
  margin-bottom: 18px;
}

.dialog-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-section {
  padding: 18px 20px 6px;
  border-radius: 16px;
  border: 1px solid #ebeef5;
  background: linear-gradient(180deg, #ffffff 0%, #fafcff 100%);
}

.section-head {
  margin-bottom: 16px;
}

.section-title {
  color: #303133;
  font-size: 16px;
  font-weight: 700;
}

.section-desc {
  margin-top: 4px;
  color: #909399;
  font-size: 13px;
}

.attachment-panel {
  width: 100%;
  padding: 14px 16px;
  border: 1px dashed #d8dde8;
  border-radius: 14px;
  background: linear-gradient(180deg, #fafcff 0%, #f6f8fc 100%);
}

.attachment-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.attachment-tip {
  color: #909399;
  font-size: 13px;
}

.attachment-list {
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.attachment-item {
  display: flex;
  gap: 16px;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #ebeff5;
}

.attachment-main {
  flex: 1;
  min-width: 0;
}

.attachment-name {
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.attachment-meta {
  margin: 6px 0 10px;
  display: flex;
  gap: 10px;
  color: #909399;
  font-size: 12px;
  flex-wrap: wrap;
}

.attachment-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.score-form-item :deep(.el-form-item__content) {
  display: flex;
  align-items: center;
  gap: 12px;
}

.score-tip {
  color: #909399;
  font-size: 13px;
}

.text-gray {
  color: #909399;
}

.score-text {
  color: #67c23a;
  font-weight: 700;
}

.submission-header {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
  padding: 12px 16px;
  border-radius: 12px;
  background: #f5f7fa;
  font-weight: 500;
  flex-wrap: wrap;
}

.submission-attachment {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.grade-info {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.info-row {
  margin-bottom: 12px;
}

.info-row .label {
  color: #909399;
}

.info-row .value {
  color: #303133;
  font-weight: 500;
}

.content-box {
  margin-top: 8px;
  padding: 12px;
  border-radius: 8px;
  background: #f5f7fa;
  color: #606266;
  max-height: 120px;
  overflow-y: auto;
}

.grade-form {
  margin-top: 16px;
}

:deep(.el-table) {
  border-radius: 10px;
  overflow: hidden;
}

:deep(.publish-dialog .el-dialog__body) {
  padding-top: 12px;
}

</style>
