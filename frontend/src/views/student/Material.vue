<template>
  <div class="student-material">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="课程">
          <el-select
            v-model="queryParams.courseId"
            placeholder="选择课程"
            clearable
            style="width: 200px"
            @change="handleCourseChange"
          >
            <el-option
              v-for="course in courseList"
              :key="course.id"
              :label="course.courseName"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索资料名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 资料列表 -->
    <el-card class="material-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>学习资料</span>
          <span class="header-count">共 {{ total }} 个资料</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="materialList" stripe border>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="资料名称" min-width="200">
          <template #default="{ row }">
            <div class="material-name">
              <span class="file-icon">{{ getFileIcon(row.fileType || row.type) }}</span>
              <span class="name-text" :title="row.title">{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="所属课程" prop="courseName" width="150" />
        <el-table-column label="资料类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="getTypeTagType(row.fileType || row.type)">
              {{ getTypeText(row.fileType || row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="文件大小" width="100" align="center">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column label="上传者" prop="uploaderName" width="120" />
        <el-table-column label="上传时间" width="150">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="previewMaterial(row)">
              <el-icon><View /></el-icon>预览
            </el-button>
            <el-button type="success" link size="small" @click="downloadMaterial(row)">
              <el-icon><Download /></el-icon>下载
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          :current-page="queryParams.current"
          :page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 资料预览对话框 -->
    <el-dialog v-model="previewDialogVisible" title="文件预览" width="800px">
      <div class="preview-content">
        <iframe
          v-if="isPdfFile(currentMaterial?.fileType || currentMaterial?.type)"
          :src="currentMaterial?.fileUrl"
          width="100%"
          height="600px"
        />
        <el-image
          v-else-if="isImageFile(currentMaterial?.fileType || currentMaterial?.type)"
          :src="currentMaterial?.fileUrl"
          fit="contain"
          style="width: 100%; max-height: 600px"
        />
        <div v-else class="preview-empty"></div>
      </div>
      <template #footer>
        <div class="preview-footer">
          <el-button type="primary" @click="downloadCurrentMaterial">下载文件</el-button>
          <el-button @click="openInNewWindow(currentMaterial!.fileUrl)">新窗口打开</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Search,
  View,
  Download
} from '@element-plus/icons-vue'
import {
  getStudentMaterials,
  downloadMaterialFile,
  previewMaterial as previewMaterialApi,
  type MaterialVO
} from '@/api/material'
import {
  getMyStudentCourses,
  type CourseVO
} from '@/api/course'

const route = useRoute()

// 查询参数
const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: '',
  courseId: undefined as number | undefined
})

// 课程列表
const courseList = ref<CourseVO[]>([])

// 资料列表
const materialList = ref<MaterialVO[]>([])
const total = ref(0)
const loading = ref(false)

// 当前预览的资料
const currentMaterial = ref<MaterialVO>()
const previewDialogVisible = ref(false)

// 加载课程列表
const loadCourses = async () => {
  try {
    const res = await getMyStudentCourses()
    courseList.value = res || []
  } catch (error) {
    console.error('加载课程失败', error)
  }
}

// 加载资料列表
const loadMaterials = async () => {
  loading.value = true
  try {
    const res = await getStudentMaterials({
      current: queryParams.current,
      size: queryParams.size,
      courseId: queryParams.courseId,
      title: queryParams.keyword
    })
    materialList.value = res?.records || []
    total.value = res?.total || 0
  } catch (error) {
    console.error('加载资料失败', error)
    ElMessage.error('加载资料失败')
  } finally {
    loading.value = false
  }
}

// 课程选择变化
const handleCourseChange = () => {
  queryParams.current = 1
  loadMaterials()
}

// 搜索
const handleSearch = () => {
  queryParams.current = 1
  loadMaterials()
}

// 重置
const handleReset = () => {
  queryParams.keyword = ''
  queryParams.courseId = undefined
  queryParams.current = 1
  loadMaterials()
}

// 分页大小变化
const handleSizeChange = (val: number) => {
  queryParams.size = val
  loadMaterials()
}

// 页码变化
const handleCurrentChange = (val: number) => {
  queryParams.current = val
  loadMaterials()
}

// 获取文件图标
const getFileIcon = (type: string) => {
  const iconMap: Record<string, string> = {
    'pdf': 'PDF',
    'doc': 'DOC',
    'docx': 'DOC',
    'ppt': 'PPT',
    'pptx': 'PPT',
    'xls': 'XLS',
    'xlsx': 'XLS',
    'zip': 'ZIP',
    'rar': 'RAR',
    'jpg': 'IMG',
    'jpeg': 'IMG',
    'png': 'IMG',
    'gif': 'IMG',
    'mp4': 'VID',
    'mp3': 'AUD',
    'default': 'FILE'
  }
  return iconMap[type?.toLowerCase()] || iconMap.default
}

// 获取类型标签类型
const getTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    'pdf': 'danger',
    'doc': 'primary',
    'docx': 'primary',
    'ppt': 'warning',
    'pptx': 'warning',
    'xls': 'success',
    'xlsx': 'success',
    'zip': 'info',
    'rar': 'info'
  }
  return typeMap[type?.toLowerCase()] || ''
}

// 获取类型文本
const getTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    'pdf': 'PDF',
    'doc': 'Word',
    'docx': 'Word',
    'ppt': 'PPT',
    'pptx': 'PPT',
    'xls': 'Excel',
    'xlsx': 'Excel',
    'zip': '压缩包',
    'rar': '压缩包',
    'jpg': '图片',
    'jpeg': '图片',
    'png': '图片',
    'gif': '图片',
    'mp4': '视频',
    'mp3': '音频'
  }
  return textMap[type?.toLowerCase()] || type?.toUpperCase() || '文件'
}

import dayjs from 'dayjs'

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
const formatFileSize = (size: number) => {
  if (!size) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(size) / Math.log(1024))
  return `${(size / Math.pow(1024, i)).toFixed(2)} ${units[i]}`
}

const isPdfFile = (type?: string) => (type || '').toLowerCase() === 'pdf'

const isImageFile = (type?: string) => ['jpg', 'jpeg', 'png', 'gif', 'webp'].includes((type || '').toLowerCase())

// 预览资料
const previewMaterial = async (row: MaterialVO) => {
  try {
    const fileType = (row.fileType || row.type)?.toLowerCase() || ''
    const res = await previewMaterialApi(row.id)
    if (res) {
      const materialWithAccessUrl = {
        ...row,
        fileUrl: resolveAccessUrl(res as unknown as string)
      }
      if (isImageFile(fileType) || isPdfFile(fileType)) {
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

// 下载当前资料 (从预览弹窗中下载)
const downloadCurrentMaterial = () => {
  if (currentMaterial.value) {
    downloadMaterial(currentMaterial.value)
  }
}

const openInNewWindow = (url: string) => {
  window.open(resolveAccessUrl(url), '_blank')
}

// 下载资料
const downloadMaterial = async (row: MaterialVO) => {
  try {
    const response = await downloadMaterialFile(row.id)
    const blobUrl = window.URL.createObjectURL(response.data)
    const link = document.createElement('a')
    link.style.display = 'none'
    link.href = blobUrl
    link.download = row.fileName || row.title
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(blobUrl)
    ElMessage.success(`开始下载：${row.title}`)
    loadMaterials()
  } catch (error: any) {
    if (error.response?.data?.code === 404) {
      ElMessage.error('文件已删除或已过期')
    } else {
      ElMessage.error('获取文件失败，请稍后重试')
    }
  }
}

// 格式化日期
const formatDate = (date: string | undefined) => {
  if (!date) return '暂无数据'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

onMounted(() => {
  loadCourses()
  // 从路由参数获取课程ID
  const courseId = route.query.courseId
  if (courseId) {
    queryParams.courseId = Number(courseId)
  }
  loadMaterials()
})
</script>

<style scoped>
.student-material {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.material-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-count {
      font-size: 14px;
      color: #909399;
    }
  }
}

.material-name {
  display: flex;
  align-items: center;
  gap: 8px;

  .file-icon {
    font-size: 20px;
  }

  .name-text {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.preview-content {
  min-height: 600px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  overflow: hidden;

  iframe {
    border: none;
    display: block;
  }
}

.preview-empty {
  min-height: 600px;
}

.preview-footer {
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>
