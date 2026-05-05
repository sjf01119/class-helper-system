<template>
  <div class="homework-center">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <div class="stat-card warning">
          <div class="stat-icon">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ pendingCount }}</div>
            <div class="stat-label">待提交</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card success">
          <div class="stat-icon">
            <el-icon><Check /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ submittedCount }}</div>
            <div class="stat-label">已提交</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card primary">
          <div class="stat-icon">
            <el-icon><Trophy /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ gradedCount }}</div>
            <div class="stat-label">已批改</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 标签页切换 -->
    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 待提交作业 -->
        <el-tab-pane label="待提交" name="pending">
          <div v-if="displayPendingList.length > 0" class="homework-list">
            <div
              v-for="item in displayPendingList"
              :key="item.id"
              class="homework-item"
              :class="{ 'urgent': isUrgent(item.deadline) }"
            >
              <div class="homework-header">
                <div class="homework-title">
                  <span class="title-text">{{ item.title }}</span>
                  <el-tag v-if="isUrgent(item.deadline)" type="danger" size="small" effect="dark">即将截止</el-tag>
                </div>
                <span class="countdown-text">{{ getDeadlineText(item.deadline) }}</span>
              </div>
              <div class="homework-meta">
                <span><el-icon><Reading /></el-icon> {{ item.courseName }}</span>
                <span><el-icon><User /></el-icon> {{ item.teacherName }}</span>
                <span><el-icon><Timer /></el-icon> 截止：{{ formatDate(item.deadline) }}</span>
                <span><el-icon><Trophy /></el-icon> 满分：{{ item.maxScore }}分</span>
              </div>
              <div class="homework-content">{{ item.content }}</div>
              <div class="homework-footer">
                <el-button type="primary" @click="showSubmitDialog(item)">
                  <el-icon><Upload /></el-icon>立即提交
                </el-button>
                <el-button link type="info" @click="showDetail(item)">
                  查看详情
                </el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无待提交作业" />
        </el-tab-pane>

        <!-- 已提交作业 -->
        <el-tab-pane label="已提交" name="submitted">
          <div v-if="displaySubmittedList.length > 0" class="homework-list">
            <div
              v-for="item in displaySubmittedList"
              :key="item.id"
              class="homework-item"
            >
              <div class="homework-header">
                <div class="homework-title">
                  <span class="title-text">{{ item.title }}</span>
                  <el-tag :type="getResultTagType(item)" size="small">
                    {{ getResultText(item) }}
                  </el-tag>
                </div>
                <div v-if="item.score !== undefined && item.score !== null" class="homework-score">
                  <span class="score-value">{{ item.score }}</span>
                  <span class="score-total">/{{ item.maxScore }}</span>
                </div>
              </div>
              <div class="homework-meta">
                <span><el-icon><Reading /></el-icon> {{ item.courseName }}</span>
                <span><el-icon><User /></el-icon> {{ item.teacherName }}</span>
                <span><el-icon><Check /></el-icon> 提交：{{ formatDate(item.submitTime) }}</span>
              </div>
              <div v-if="item.feedback" class="homework-feedback">
                <el-icon><ChatDotRound /></el-icon>
                <span>教师评语：{{ item.feedback }}</span>
              </div>
              <div class="homework-footer">
                <el-button link type="primary" @click="showSubmissionDetail(item)">
                  查看提交
                </el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无已提交作业" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 提交作业对话框 -->
    <el-dialog v-model="submitDialogVisible" title="提交作业" width="600px" :close-on-click-modal="false">
      <div v-if="currentHomework" class="submit-form">
        <div class="homework-brief">
          <h4>{{ currentHomework.title }}</h4>
          <p>{{ currentHomework.content }}</p>
          <p class="deadline">截止时间：{{ formatDate(currentHomework.deadline) }}</p>
        </div>
        <el-form ref="submitFormRef" :model="submitForm" label-width="80px">
          <el-form-item label="作业内容">
            <el-input
              v-model="submitForm.content"
              type="textarea"
              :rows="6"
              placeholder="可填写文字说明、答案或补充说明"
              maxlength="2000"
              show-word-limit
            />
          </el-form-item>
          <el-form-item label="附件">
            <el-upload
              ref="uploadRef"
              action="/api/assignment/upload"
              :headers="uploadHeaders"
              name="files"
              multiple
              :limit="5"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              :on-remove="handleUploadRemove"
              :file-list="fileList"
            >
              <el-button type="primary">
                <el-icon><Upload /></el-icon>上传附件
              </el-button>
              <template #tip>
                <div class="el-upload__tip">
                  可只写文字、只传文件，或两者一起提交；支持 Word、PDF、PPT、Excel、图片格式，单个文件不超过 100MB，最多上传 5 个
                </div>
              </template>
            </el-upload>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认提交</el-button>
      </template>
    </el-dialog>

    <!-- 作业详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="作业详情" width="700px">
      <div v-if="currentHomework" class="homework-detail">
        <h3>{{ currentHomework.title }}</h3>
        <div class="detail-meta">
          <span><el-icon><Reading /></el-icon> {{ currentHomework.courseName }}</span>
          <span><el-icon><User /></el-icon> {{ currentHomework.teacherName }}</span>
          <span><el-icon><Timer /></el-icon> 截止时间：{{ formatDate(currentHomework.deadline) }}</span>
          <span><el-icon><Trophy /></el-icon> 满分：{{ currentHomework.maxScore }}分</span>
        </div>
        <el-divider />
        <div class="detail-content">
          <h4>作业要求</h4>
          <p>{{ currentHomework.content }}</p>
        </div>
        <div v-if="getAssignmentAttachments(currentHomework).length" class="detail-attachments">
          <h4>附件</h4>
          <div class="attachment-action-list">
            <el-button
              v-for="item in getAssignmentAttachments(currentHomework)"
              :key="item.url"
              type="primary"
              link
              @click="downloadAttachment(item.url)"
            >
              <el-icon><Download /></el-icon>{{ item.name }}
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 提交详情对话框 -->
    <el-dialog v-model="submissionDialogVisible" title="提交详情" width="600px">
      <div v-if="currentSubmission" class="submission-detail">
        <div class="result-section">
          <div v-if="currentSubmission.score !== undefined" class="score-display">
            <div class="score-circle" :class="getScoreClass(currentSubmission.score, getHomeworkMaxScore(currentSubmission))">
              <span class="score-number">{{ currentSubmission.score }}</span>
              <span class="score-label">分</span>
            </div>
            <p class="score-text">满分 {{ getHomeworkMaxScore(currentSubmission) }} 分</p>
          </div>
          <div v-else class="pending-grade">
            <el-icon :size="48" color="#E6A23C"><Clock /></el-icon>
            <p>待批改</p>
          </div>
        </div>
        <el-divider />
        <div class="submission-content">
          <h4>提交内容</h4>
          <p>{{ currentSubmission.content || '无文字内容' }}</p>
        </div>
        <div v-if="getSubmissionAttachments(currentSubmission).length" class="submission-attachments">
          <h4>附件</h4>
          <div class="attachment-action-list">
            <el-button
              v-for="item in getSubmissionAttachments(currentSubmission)"
              :key="item.url"
              type="primary"
              link
              @click="downloadAttachment(item.url)"
            >
              <el-icon><Download /></el-icon>{{ item.name }}
            </el-button>
          </div>
        </div>
        <div v-if="currentSubmission.feedback" class="submission-feedback">
          <h4>教师评语</h4>
          <el-alert :title="currentSubmission.feedback" type="success" :closable="false" />
        </div>
        <div class="submission-meta">
          <p>提交时间：{{ formatDate(currentSubmission.submitTime) }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type UploadFile } from 'element-plus'
import {
  Reading,
  User,
  Timer,
  Trophy,
  Upload,
  Check,
  ChatDotRound,
  Download,
  Clock
} from '@element-plus/icons-vue'
import {
  deleteHomeworkAttachment,
  downloadHomeworkAttachmentByUrl,
  getStudentHomeworkList,
  getHomeworkAttachmentAccessUrl,
  submitHomework,
  type AttachmentItem,
  type HomeworkVO
} from '@/api/homework'
import { getScopeFromPath, getScopedToken } from '@/utils/auth'

const route = useRoute()

// 标签页
const activeTab = ref('pending')

// 作业列表
const pendingList = ref<HomeworkVO[]>([])
const submittedList = ref<HomeworkVO[]>([])
const loading = ref(false)

// 统计
const selectedCourseId = computed(() => {
  const courseId = Number(route.query.courseId)
  return Number.isNaN(courseId) || courseId <= 0 ? undefined : courseId
})
const displayPendingList = computed(() => {
  if (!selectedCourseId.value) {
    return pendingList.value
  }
  return pendingList.value.filter(item => item.courseId === selectedCourseId.value)
})
const displaySubmittedList = computed(() => {
  if (!selectedCourseId.value) {
    return submittedList.value
  }
  return submittedList.value.filter(item => item.courseId === selectedCourseId.value)
})
const pendingCount = computed(() => displayPendingList.value.length)
const submittedCount = computed(() => displaySubmittedList.value.length)
const gradedCount = computed(() => displaySubmittedList.value.filter(h => h.score !== undefined && h.score !== null).length)

// 当前作业
const currentHomework = ref<HomeworkVO>()
const currentSubmission = ref<HomeworkVO>()

// 对话框显示状态
const submitDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const submissionDialogVisible = ref(false)

// 提交表单
const submitFormRef = ref<FormInstance>()
const submitLoading = ref(false)
const submitForm = reactive({
  homeworkId: 0,
  content: '',
  attachments: ''
})

// 文件上传
const uploadRef = ref()
const fileList = ref<UploadFile[]>([])
const uploadedAttachmentList = ref<AttachmentItem[]>([])
const uploadHeaders = computed(() => {
  const token = getScopedToken(getScopeFromPath(window.location.pathname))
  return token ? { Authorization: `Bearer ${token}` } : {}
})

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

// 加载作业列表
const loadHomeworkList = async () => {
  loading.value = true
  try {
    const [pendingRes, submittedRes] = await Promise.all([
      getStudentHomeworkList(0),
      getStudentHomeworkList(1)
    ])
    pendingList.value = pendingRes || []
    submittedList.value = submittedRes || []
  } catch (error) {
    console.error('加载作业列表失败', error)
    ElMessage.error('加载作业列表失败')
  } finally {
    loading.value = false
  }
}

// 切换标签页
const handleTabChange = () => {
  // 可以在这里做额外的处理
}

// 显示提交对话框
const showSubmitDialog = (item: HomeworkVO) => {
  currentHomework.value = item
  submitForm.homeworkId = item.id
  submitForm.content = ''
  submitForm.attachments = ''
  fileList.value = []
  uploadedAttachmentList.value = []
  submitDialogVisible.value = true
}

// 显示详情
const showDetail = (item: HomeworkVO) => {
  currentHomework.value = item
  detailDialogVisible.value = true
}

const openRouteHomeworkDetail = () => {
  const homeworkId = Number(route.query.homeworkId)
  if (Number.isNaN(homeworkId) || homeworkId <= 0) {
    return
  }

  const pendingItem = pendingList.value.find((item) => item.id === homeworkId)
  if (pendingItem) {
    activeTab.value = 'pending'
    showDetail(pendingItem)
    return
  }

  const submittedItem = submittedList.value.find((item) => item.id === homeworkId)
  if (submittedItem) {
    activeTab.value = 'submitted'
    showDetail(submittedItem)
  }
}

// 显示提交详情
const showSubmissionDetail = async (item: HomeworkVO) => {
  currentHomework.value = item
  currentSubmission.value = item
  submissionDialogVisible.value = true
}

// 提交作业
const handleSubmit = async () => {
  const content = submitForm.content.trim()
  const hasContent = !!content
  const hasAttachment = uploadedAttachmentList.value.length > 0

  if (!hasContent && !hasAttachment) {
    ElMessage.warning('请至少填写文字内容或上传一个附件')
    return
  }

  submitLoading.value = true
  try {
    await submitHomework({
      homeworkId: submitForm.homeworkId,
      content: content || undefined,
      attachments: submitForm.attachments || undefined
    })
    ElMessage.success('作业提交成功')
    submitDialogVisible.value = false
    loadHomeworkList()
  } catch (error: any) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitLoading.value = false
  }
}

// 上传成功
const handleUploadSuccess = (response: any, file: UploadFile) => {
  if (response.code === 200) {
    const uploadedList = Array.isArray(response.data) ? response.data : []
    const latest = uploadedList[0]
    if (latest?.url) {
      uploadedAttachmentList.value.push(latest)
      file.url = latest.url
    }
    submitForm.attachments = uploadedAttachmentList.value.length ? JSON.stringify(uploadedAttachmentList.value) : ''
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 上传失败
const handleUploadError = () => {
  ElMessage.error('上传失败')
}

const handleUploadRemove = async (file: UploadFile) => {
  const fileUrl = file.url
  if (fileUrl) {
    try {
      await deleteHomeworkAttachment(fileUrl)
    } catch (error) {
      console.error('删除上传附件失败', error)
    }
  }
  uploadedAttachmentList.value = uploadedAttachmentList.value.filter(item => item.url !== fileUrl)
  submitForm.attachments = uploadedAttachmentList.value.length ? JSON.stringify(uploadedAttachmentList.value) : ''
}

// 上传前校验
const beforeUpload = (file: File) => {
  const validExtensions = ['pdf', 'doc', 'docx', 'ppt', 'pptx', 'xls', 'xlsx', 'jpg', 'jpeg', 'png', 'gif', 'webp']
  const extension = file.name.split('.').pop()?.toLowerCase() || ''
  const isValidType = validExtensions.includes(extension)
  const isLt100M = file.size / 1024 / 1024 <= 100

  if (!isValidType) {
    ElMessage.error('仅支持 Word、PDF、PPT、Excel、图片格式附件')
  }
  if (!isLt100M) {
    ElMessage.error('单个附件不能超过 100MB')
  }
  return isValidType && isLt100M
}

// 下载附件
const downloadAttachment = async (url: string) => {
  const pendingWindow = window.open('', '_blank')
  try {
    const fileName = url.split('/').pop() || '附件'
    const accessUrl = resolveAccessUrl(await getHomeworkAttachmentAccessUrl({
      fileUrl: url,
      fileName,
      download: true
    }))
    if (accessUrl.includes('/api/assignment/attachment/download')) {
      pendingWindow?.close()
      const response = await downloadHomeworkAttachmentByUrl(accessUrl)
      triggerBlobDownload(response.data, fileName)
      return
    }
    if (pendingWindow) {
      pendingWindow.location.href = accessUrl
      return
    }
    window.location.href = accessUrl
  } catch (error) {
    pendingWindow?.close()
    console.error('下载附件失败', error)
    ElMessage.error('下载附件失败')
  }
}

const buildLegacyAttachment = (url?: string): AttachmentItem[] => {
  if (!url) return []
  const fileName = url.split('/').pop() || '附件'
  return [{ name: fileName, url }]
}

const getAssignmentAttachments = (item?: HomeworkVO) => {
  if (!item) return []
  return item.attachmentList?.length ? item.attachmentList : buildLegacyAttachment(item.attachments)
}

const getSubmissionAttachments = (item?: HomeworkVO) => {
  if (!item) return []
  return item.submissionAttachmentList?.length
    ? item.submissionAttachmentList
    : buildLegacyAttachment(item.submissionAttachments || item.attachments)
}

// 判断是否即将截止（24小时内）
const isUrgent = (deadline: string) => {
  const deadlineTime = new Date(deadline).getTime()
  const now = Date.now()
  const diff = deadlineTime - now
  return diff > 0 && diff < 24 * 60 * 60 * 1000
}

const getDeadlineText = (deadline?: string) => {
  if (!deadline) return '截止时间待定'
  const diff = new Date(deadline).getTime() - Date.now()
  if (Number.isNaN(diff)) return '截止时间待定'
  if (diff <= 0) return '已截止'

  const totalMinutes = Math.floor(diff / (1000 * 60))
  const days = Math.floor(totalMinutes / (60 * 24))
  const hours = Math.floor((totalMinutes % (60 * 24)) / 60)
  const minutes = totalMinutes % 60

  if (days > 0) return `${days}天${hours}小时后截止`
  if (hours > 0) return `${hours}小时${minutes}分钟后截止`
  return `${Math.max(minutes, 1)}分钟后截止`
}

// 获取结果标签类型
const getResultTagType = (item: HomeworkVO) => {
  if (item.score === undefined || item.score === null) return 'info'
  const maxScore = getHomeworkMaxScore(item)
  if (item.score >= maxScore * 0.9) return 'success'
  if (item.score >= maxScore * 0.6) return 'warning'
  return 'danger'
}

// 获取结果文本
const getResultText = (item: HomeworkVO) => {
  if (item.score === undefined || item.score === null) return '待批改'
  return `得分 ${item.score}分`
}

const getHomeworkMaxScore = (item?: HomeworkVO) => item?.maxScore ?? item?.totalScore ?? 100

// 获取分数等级样式
const getScoreClass = (score: number, maxScore: number) => {
  const ratio = score / maxScore
  if (ratio >= 0.9) return 'excellent'
  if (ratio >= 0.8) return 'good'
  if (ratio >= 0.6) return 'pass'
  return 'fail'
}

// 格式化日期
const formatDate = (date: string | undefined) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(async () => {
  await loadHomeworkList()
  openRouteHomeworkDetail()
})
</script>

<style scoped>
.homework-center {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

  &.warning {
    border-left: 4px solid #E6A23C;
  }

  &.success {
    border-left: 4px solid #67C23A;
  }

  &.primary {
    border-left: 4px solid #409EFF;
  }

  .stat-icon {
    font-size: 36px;
  }

  .stat-info {
    .stat-value {
      font-size: 28px;
      font-weight: bold;
      color: #303133;
    }

    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-top: 4px;
    }
  }
}

.homework-list {
  .homework-item {
    background: #f5f7fa;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 15px;
    transition: all 0.3s;

    &:hover {
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    }

    &.urgent {
      border: 1px solid #F56C6C;
      background: #fef0f0;
    }

    .homework-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;

      .homework-title {
        display: flex;
        align-items: center;
        gap: 10px;

        .title-text {
          font-size: 16px;
          font-weight: bold;
          color: #303133;
        }
      }

      .countdown-text {
        color: #e67e22;
        font-size: 13px;
        font-weight: 500;
        white-space: nowrap;
      }

      .homework-score {
        .score-value {
          font-size: 28px;
          font-weight: bold;
          color: #67C23A;
        }

        .score-total {
          font-size: 14px;
          color: #909399;
        }
      }
    }

    .homework-meta {
      display: flex;
      flex-wrap: wrap;
      gap: 15px;
      margin-bottom: 10px;
      color: #606266;
      font-size: 13px;

      span {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }

    .homework-content {
      color: #606266;
      line-height: 1.6;
      margin-bottom: 15px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .homework-feedback {
      background: #ecf5ff;
      padding: 10px;
      border-radius: 4px;
      margin-bottom: 15px;
      display: flex;
      align-items: flex-start;
      gap: 8px;
      color: #409EFF;
      font-size: 13px;

      .el-icon {
        margin-top: 2px;
      }
    }

    .homework-footer {
      display: flex;
      gap: 10px;
    }
  }
}

.submit-form {
  .homework-brief {
    background: #f5f7fa;
    padding: 15px;
    border-radius: 4px;
    margin-bottom: 20px;

    h4 {
      margin: 0 0 10px 0;
      color: #303133;
    }

    p {
      margin: 5px 0;
      color: #606266;
    }

    .deadline {
      color: #F56C6C;
      font-weight: bold;
    }

  }
}

.homework-detail {
  h3 {
    margin: 0 0 15px 0;
    font-size: 18px;
    color: #303133;
  }

  .detail-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 15px;
    color: #606266;
    font-size: 13px;
    margin-bottom: 15px;

    span {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }

  .detail-content,
  .detail-attachments {
    h4 {
      margin: 15px 0 10px 0;
      color: #303133;
      font-size: 14px;
    }

    p {
      color: #606266;
      line-height: 1.8;
    }
  }
}

.attachment-action-list {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
}

.submission-detail {
  .result-section {
    display: flex;
    justify-content: center;
    padding: 20px 0;

    .score-display {
      text-align: center;

      .score-circle {
        width: 100px;
        height: 100px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 10px;
        border: 4px solid;

        &.excellent {
          border-color: #67C23A;
          color: #67C23A;
        }

        &.good {
          border-color: #409EFF;
          color: #409EFF;
        }

        &.pass {
          border-color: #E6A23C;
          color: #E6A23C;
        }

        &.fail {
          border-color: #F56C6C;
          color: #F56C6C;
        }

        .score-number {
          font-size: 36px;
          font-weight: bold;
        }

        .score-label {
          font-size: 14px;
        }
      }

      .score-text {
        color: #909399;
      }
    }

    .pending-grade {
      text-align: center;
      color: #E6A23C;

      p {
        margin-top: 10px;
      }
    }
  }

  .submission-content,
  .submission-attachments,
  .submission-feedback {
    h4 {
      margin: 15px 0 10px 0;
      color: #303133;
      font-size: 14px;
    }

    p {
      color: #606266;
      line-height: 1.8;
      white-space: pre-wrap;
    }
  }

  .submission-meta {
    margin-top: 20px;
    padding-top: 15px;
    border-top: 1px solid #ebeef5;
    color: #909399;
    font-size: 13px;
  }
}
</style>
