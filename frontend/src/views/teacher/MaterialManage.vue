<template>
  <div class="material-manage">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="资料名称">
          <el-input v-model="queryParams.name" placeholder="请输入资料名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="所属课程">
          <el-select v-model="queryParams.courseId" placeholder="全部课程" clearable style="width: 150px">
            <el-option v-for="course in courseList" :key="course.id" :label="course.courseName" :value="course.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件类型">
          <el-select v-model="queryParams.fileType" placeholder="全部" clearable style="width: 120px">
            <el-option label="PDF" value="pdf" />
            <el-option label="Word" value="doc" />
            <el-option label="PPT" value="ppt" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>重置
          </el-button>
          <el-button type="success" @click="handleUpload">
            <el-icon><Upload /></el-icon>上传资料
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 资料列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="materialList" v-loading="loading" :empty-text="loading ? '正在加载资料...' : '暂无资料数据'" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="资料名称" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="file-name">
              <el-icon class="file-icon" :size="20">
                <Document v-if="row.fileType === 'pdf'" />
                <DocumentCopy v-else-if="row.fileType === 'doc'" />
                <DataLine v-else-if="row.fileType === 'ppt'" />
                <Folder v-else />
              </el-icon>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="所属课程" min-width="150">
          <template #default="{ row }">
            {{ row.courseName }}
          </template>
        </el-table-column>
        <el-table-column label="文件类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getFileTypeTag(row.fileType)" size="small">
              {{ getFileTypeText(row.fileType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileSize" label="文件大小" width="100" align="center">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="downloadCount" label="下载次数" width="100" align="center" />
        <el-table-column prop="createdAt" label="上传时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.createdAt) }}
            </template>
          </el-table-column>
        <el-table-column label="操作" width="250" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="handlePreview(row)">
              预览
            </el-button>
            <el-button type="success" link :icon="Download" @click="handleDownload(row)">
              下载
            </el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 上传资料弹窗 -->
    <el-dialog title="上传学习资料" v-model="uploadDialogVisible" width="550px" :close-on-click-modal="false"
      destroy-on-close>
      <el-form :model="uploadForm" :rules="uploadRules" ref="uploadFormRef" label-width="90px">
        <el-form-item label="资料名称" prop="name">
          <el-input v-model="uploadForm.name" placeholder="请输入资料名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="所属课程" prop="courseId">
          <el-select v-model="uploadForm.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option v-for="course in courseList" :key="course.id" :label="course.courseName"
              :value="course.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="资料描述" prop="description">
          <el-input v-model="uploadForm.description" type="textarea" :rows="3" placeholder="请输入资料描述"
            maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="选择文件" prop="file">
          <el-upload ref="uploadRef" action="#" :auto-upload="false" :on-change="handleFileChange"
            :on-remove="handleFileRemove" :limit="1" :file-list="fileList" accept=".pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.jpg,.jpeg,.png">
            <el-button type="primary">
              <el-icon><Plus /></el-icon>选择文件
            </el-button>
            <template #tip>
              <div class="el-upload__tip">支持 PDF、Word、PPT、Excel、图片格式，单个文件不超过 100MB</div>
            </template>
          </el-upload>
          <el-progress v-if="uploadLoading" :percentage="uploadProgress" style="width: 100%; margin-top: 10px;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitUpload" :loading="uploadLoading">上传</el-button>
      </template>
    </el-dialog>

    <!-- 文件预览弹窗 -->
    <el-dialog
      v-model="previewDialogVisible"
      title="文件预览"
      width="80%"
      destroy-on-close
    >
      <div class="preview-container">
        <iframe
          v-if="currentMaterial?.fileUrl && isPdfFile(currentMaterial.fileType)"
          :src="currentMaterial.fileUrl"
          class="preview-iframe"
          frameborder="0"
        ></iframe>
        <img
          v-else-if="currentMaterial?.fileUrl && isImageFile(currentMaterial.fileType)"
          :src="currentMaterial.fileUrl"
          class="preview-image"
          alt="资料预览"
        />
        <div v-else class="preview-empty">
          <el-empty description="当前文件暂不支持弹窗预览，请使用“下载文件”或“新窗口打开”查看" />
        </div>
      </div>
      <template #footer>
        <div class="preview-footer">
          <el-button type="primary" :disabled="!currentMaterial?.fileUrl" @click="handleDownload(currentMaterial!)">下载文件</el-button>
          <el-button :disabled="!currentMaterial?.fileUrl" @click="openInNewWindow(currentMaterial!.fileUrl)">新窗口打开</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshRight, Upload, Download, Delete, View, Plus, Document, DocumentCopy, DataLine, Folder } from '@element-plus/icons-vue'
import { getMyTeacherCourses, type CourseVO } from '@/api/course'
import { getMaterialList, addMaterial, deleteMaterial, downloadMaterialFile, previewMaterial } from '@/api/material'
import dayjs from 'dayjs'

// 资料类型定义
interface Material {
  id: number
  name: string
  title: string
  courseId: number
  courseName?: string
  description?: string
  fileType: string
  fileSize: number
  fileUrl: string
  downloadCount: number
  uploadTime: string
  createdAt: string
}

const loading = ref(false)
const uploadLoading = ref(false)
const uploadProgress = ref(0)
const materialList = ref<Material[]>([])
const courseList = ref<CourseVO[]>([])
const total = ref(0)
const uploadRef = ref()
const fileList = ref<any[]>([])

const queryParams = reactive({
  current: 1,
  size: 10,
  name: '',
  courseId: undefined as number | undefined,
  fileType: ''
})

const uploadDialogVisible = ref(false)
const previewDialogVisible = ref(false)
const currentMaterial = ref<Material>()
const uploadFormRef = ref()

const uploadForm = reactive({
  name: '',
  courseId: undefined as number | undefined,
  description: '',
  file: null as File | null
})

const uploadRules = {
  name: [{ required: true, message: '请输入资料名称', trigger: 'blur' }],
  courseId: [{ required: true, message: '请选择所属课程', trigger: 'change' }]
}

const formatTime = (time?: string) => {
  if (!time) return '暂无数据'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
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

// 格式化文件大小
const formatFileSize = (size?: number) => {
  if (!size || size <= 0) return '暂无数据'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(size) / Math.log(1024))
  return `${(size / Math.pow(1024, i)).toFixed(2)} ${units[i]}`
}

const getFileTypeTag = (type?: string) => {
  const map: Record<string, string> = {
    pdf: 'danger',
    doc: 'primary',
    docx: 'primary',
    ppt: 'warning',
    pptx: 'warning'
  }
  return map[type || ''] || 'info'
}

const getFileTypeText = (type?: string) => {
  const map: Record<string, string> = {
    pdf: 'PDF',
    doc: 'Word',
    docx: 'Word',
    ppt: 'PPT',
    pptx: 'PPT'
  }
  return map[type || ''] || type?.toUpperCase() || '其他'
}

const isImageFile = (type?: string) => ['jpg', 'jpeg', 'png', 'gif', 'webp'].includes((type || '').toLowerCase())

const isPdfFile = (type?: string) => (type || '').toLowerCase() === 'pdf'

const loadMaterialList = async () => {
  loading.value = true
  try {
    const res = await getMaterialList({
      current: queryParams.current,
      size: queryParams.size,
      title: queryParams.name,
      courseId: queryParams.courseId,
      fileType: queryParams.fileType || undefined
    })
    
    if (res && res.records) {
      materialList.value = res.records.map(item => ({
        id: item.id,
        name: item.fileName || item.title,
        title: item.title,
        courseId: item.courseId,
        courseName: item.courseName || '暂无课程信息',
        description: item.description,
        fileType: (item.fileType || 'other').toLowerCase(),
        fileSize: item.fileSize || 0,
        fileUrl: item.fileUrl,
        downloadCount: item.downloadCount || 0,
        uploadTime: item.createdAt || '',
        createdAt: item.createdAt || ''
      }))
      total.value = res.total
    } else {
      materialList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('加载资料列表失败', error)
    ElMessage.error('加载资料列表失败，请稍后重试')
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
    ElMessage.error('加载课程列表失败，请稍后重试')
  }
}

const handleSearch = () => {
  queryParams.current = 1
  loadMaterialList()
}

const handleReset = () => {
  queryParams.name = ''
  queryParams.courseId = undefined
  queryParams.fileType = ''
  queryParams.current = 1
  loadMaterialList()
}

const handleUpload = () => {
  uploadForm.name = ''
  uploadForm.courseId = undefined
  uploadForm.description = ''
  uploadForm.file = null
  fileList.value = []
  uploadProgress.value = 0
  uploadDialogVisible.value = true
}

const handleFileChange = (file: any) => {
  const allowedExtensions = ['.pdf', '.doc', '.docx', '.ppt', '.pptx', '.xls', '.xlsx', '.jpg', '.jpeg', '.png']
  const fileName = file.name.toLowerCase()
  const isAllowed = allowedExtensions.some(ext => fileName.endsWith(ext))
  
  if (!isAllowed) {
    ElMessage.error('文件格式不支持，请上传支持的格式')
    uploadRef.value.clearFiles()
    uploadForm.file = null
    return
  }

  const isLt100M = file.size / 1024 / 1024 < 100
  if (!isLt100M) {
    ElMessage.error('文件过大，单个文件不超过 100MB')
    uploadRef.value.clearFiles()
    uploadForm.file = null
    return
  }

  uploadForm.file = file.raw
  // 自动填充文件名
  if (!uploadForm.name && file.name) {
    uploadForm.name = file.name.replace(/\.[^/.]+$/, '')
  }
}

const handleFileRemove = () => {
  uploadForm.file = null
}

const handleSubmitUpload = async () => {
  await uploadFormRef.value.validate()
  if (!uploadForm.file) {
    ElMessage.warning('请选择要上传的文件')
    return
  }
  uploadLoading.value = true
  uploadProgress.value = 0
  try {
    const formData = new FormData()
    const selectedCourse = courseList.value.find(course => course.id === uploadForm.courseId)
    formData.append('file', uploadForm.file)
    formData.append('title', uploadForm.name)
    formData.append('courseId', String(uploadForm.courseId))
    formData.append('classId', String(selectedCourse?.classId || 0))
    formData.append('category', 'default')
    if (uploadForm.description) {
      formData.append('description', uploadForm.description)
    }

    await addMaterial(formData, (progressEvent: any) => {
      const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
      uploadProgress.value = percentCompleted
    })
    ElMessage.success('上传成功')
    uploadDialogVisible.value = false
    loadMaterialList()
  } catch (error: any) {
    console.error('上传失败', error)
    if (error.message && error.message.includes('Maximum upload size exceeded')) {
      ElMessage.error('上传失败：文件过大，单个文件不超过 100MB')
    } else {
      ElMessage.error('上传失败：' + (error.message || '未知错误'))
    }
  } finally {
    uploadLoading.value = false
  }
}

const handlePreview = async (row: Material) => {
  try {
    const res = await previewMaterial(row.id)
    if (res) {
      const materialWithAccessUrl = {
        ...row,
        fileUrl: resolveAccessUrl(res)
      }
      if (isImageFile(row.fileType) || isPdfFile(row.fileType)) {
        currentMaterial.value = materialWithAccessUrl
        previewDialogVisible.value = true
      } else {
        openInNewWindow(materialWithAccessUrl.fileUrl)
        ElMessage.success('已在新窗口打开文件')
      }
    } else {
      ElMessage.error('获取预览地址失败')
    }
  } catch (error: any) {
    if (error.response?.data?.code === 404) {
      ElMessage.error('文件已删除或已过期')
    } else {
      ElMessage.error('获取文件失败，请稍后重试')
    }
  }
}

const handleDownload = async (row: Material) => {
  try {
    const response = await downloadMaterialFile(row.id)
    const blobUrl = window.URL.createObjectURL(response.data)
    const link = document.createElement('a')
    link.style.display = 'none'
    link.href = blobUrl
    link.download = row.name
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(blobUrl)
    ElMessage.success(`开始下载：${row.name}`)
    loadMaterialList()
  } catch (error: any) {
    if (error.response?.data?.code === 404) {
      ElMessage.error('文件已删除或已过期')
    } else {
      ElMessage.error('获取文件失败，请稍后重试')
    }
  }
}

const handleDelete = (row: Material) => {
  ElMessageBox.confirm(`确定删除资料 "${row.name}" 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteMaterial(row.id)
      ElMessage.error('删除成功')
      loadMaterialList()
    } catch (error) {
      console.error('删除失败', error)
      ElMessage.error('删除资料失败，请稍后重试')
    }
  }).catch(() => { })
}

const openInNewWindow = (url: string) => {
  if (!url) {
    ElMessage.warning('暂无可打开的文件地址')
    return
  }
  window.open(resolveAccessUrl(url), '_blank')
}

const handleSizeChange = (val: number) => {
  queryParams.size = val
  loadMaterialList()
}

const handleCurrentChange = (val: number) => {
  queryParams.current = val
  loadMaterialList()
}

onMounted(() => {
  loadMaterialList()
  loadCourseList()
})
</script>

<style scoped>
.material-manage {
  padding: 0;
}

.search-card {
  margin-bottom: 16px;
  border-radius: 12px;
}

.table-card {
  border-radius: 12px;
}

.file-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-icon {
  color: #409EFF;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.preview-container {
  height: 70vh;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  overflow: hidden;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
  display: block;
}

.preview-image {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: contain;
  background: #fff;
}

.preview-empty {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-footer {
  display: flex;
  justify-content: center;
  gap: 12px;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}
</style>
