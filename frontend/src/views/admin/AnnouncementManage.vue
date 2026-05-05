<template>
  <div class="announcement-manage">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="标题">
          <el-input 
            v-model="queryParams.title" 
            placeholder="请输入标题" 
            clearable 
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryParams.type" placeholder="全部" clearable style="width: 120px">
            <el-option label="系统公告" :value="1" />
            <el-option label="班级公告" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="置顶">
          <el-select v-model="queryParams.isTop" placeholder="全部" clearable style="width: 140px">
            <el-option label="置顶公告" :value="1" />
            <el-option label="普通公告" :value="0" />
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
            <el-icon><Plus /></el-icon>发布公告
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table 
        :data="announcementList" 
        v-loading="loading" 
        border 
        stripe
        highlight-current-row
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="标题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="title-cell">
              <el-tag v-if="row.isTop === 1" type="danger" size="small" effect="dark" class="top-tag">置顶</el-tag>
              <el-tag :type="row.publishScope === 0 ? 'primary' : 'success'" size="small" class="type-tag">
                {{ row.publishScope === 0 ? '系统' : '班级' }}
              </el-tag>
              <span class="title-text">{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="发布范围" width="150">
          <template #default="{ row }">
            <span>{{ row.publishScope === 1 ? '班级公告' : '系统公告' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="90" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(val: number) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              <el-icon><View /></el-icon>查看
            </el-button>
            <el-button link type="warning" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          :current-page="queryParams.current"
          :page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog 
      :title="dialogTitle" 
      v-model="dialogVisible" 
      width="700px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="90px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入公告标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="公告类型" prop="type">
              <el-radio-group v-model="formData.publishScope" @change="onScopeChange">
                <el-radio :label="0">系统公告</el-radio>
                <el-radio :label="1">班级公告</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否置顶" prop="isTop">
              <el-switch
                v-model="formData.isTop"
                :active-value="1"
                :inactive-value="0"
                active-text="置顶"
                inactive-text="普通"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="发布班级" prop="classId" v-if="formData.publishScope === 1">
          <el-select v-model="formData.classId" placeholder="请选择班级" style="width: 100%">
            <el-option v-for="cls in classList" :key="cls.id" :label="cls.className" :value="cls.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input 
            v-model="formData.content" 
            type="textarea" 
            :rows="10" 
            placeholder="请输入公告内容"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">立即发布</el-radio>
            <el-radio :label="0">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看弹窗 -->
    <el-dialog title="公告详情" v-model="viewDialogVisible" width="700px">
      <div class="announcement-detail">
        <h2 class="detail-title">
          <el-tag v-if="currentAnnouncement?.isTop === 1" type="danger" size="small" effect="dark">置顶</el-tag>
          {{ currentAnnouncement?.title }}
        </h2>
        <div class="detail-meta">
          <span class="meta-item">
            <el-icon><Timer /></el-icon>
            {{ formatTime(currentAnnouncement?.publishTime) }}
          </span>
          <span class="meta-item">
            <el-icon><View /></el-icon>
            浏览 {{ currentAnnouncement?.viewCount || 0 }} 次
          </span>
          <span class="meta-item">
            <el-tag :type="currentAnnouncement?.publishScope === 0 ? 'primary' : 'success'" size="small">
              {{ currentAnnouncement?.publishScope === 0 ? '系统公告' : '班级公告' }}
            </el-tag>
          </span>
        </div>
        <div class="detail-content">{{ currentAnnouncement?.content }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshRight, Plus, Edit, Delete, View, Timer } from '@element-plus/icons-vue'
import { getAnnouncementPage, addAnnouncement, updateAnnouncement, deleteAnnouncement, type Announcement } from '@/api/announcement'
import { getAllClasses, type ClassVO } from '@/api/class'

const loading = ref(false)
const submitLoading = ref(false)
const announcementList = ref<Announcement[]>([])
const classList = ref<ClassVO[]>([])
const total = ref(0)

const queryParams = reactive({
  current: 1,
  size: 10,
  title: '',
  type: undefined as number | undefined,
  isTop: undefined as number | undefined
})

const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑公告' : '发布公告')
const formRef = ref()
const currentAnnouncement = ref<Announcement>()

const formData = reactive<Announcement>({
  title: '',
  content: '',
  type: 1,
  publishScope: 0,
  classId: undefined,
  isTop: 0,
  status: 1
})

const formRules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' },
    { min: 5, max: 2000, message: '长度在 5 到 2000 个字符', trigger: 'blur' }
  ],
  classId: [
    { required: true, message: '请选择发布班级', trigger: 'change', validator: (_rule: any, value: any, callback: any) => {
      if (formData.publishScope === 1 && !value) {
        callback(new Error('请选择发布班级'))
      } else {
        callback()
      }
    }}
  ]
}

const formatTime = (time?: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const onScopeChange = (val: number) => {
  if (val === 0) {
    formData.classId = undefined
  }
  formData.type = val === 1 ? 2 : 1
}

const loadAnnouncementList = async () => {
  loading.value = true
  try {
    const res = await getAnnouncementPage(queryParams)
    announcementList.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const loadClassList = async () => {
  try {
    const res = await getAllClasses()
    classList.value = res || []
  } catch (error) {
    console.error('加载班级列表失败', error)
  }
}

const handleSearch = () => {
  queryParams.current = 1
  loadAnnouncementList()
}

const handleReset = () => {
  queryParams.title = ''
  queryParams.type = undefined
  queryParams.isTop = undefined
  queryParams.current = 1
  loadAnnouncementList()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(formData, {
    id: undefined,
    title: '',
    content: '',
    type: 1,
    publishScope: 0,
    classId: undefined,
    isTop: 0,
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row: Announcement) => {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    title: row.title,
    content: row.content,
    type: row.type,
    publishScope: row.publishScope ?? (row.type === 2 ? 1 : 0),
    classId: row.classId,
    isTop: row.isTop ?? (row.priority === 1 ? 1 : 0),
    status: row.status
  })
  dialogVisible.value = true
}

const handleView = (row: Announcement) => {
  currentAnnouncement.value = row
  viewDialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateAnnouncement(formData)
      ElMessage.success('更新成功')
    } else {
      await addAnnouncement(formData)
      ElMessage.success('发布成功')
    }
    dialogVisible.value = false
    loadAnnouncementList()
  } catch (error) {
    console.error('保存失败', error)
  } finally {
    submitLoading.value = false
  }
}

const handleStatusChange = async (row: Announcement, val: number) => {
  try {
    await updateAnnouncement({
      id: row.id,
      title: row.title,
      content: row.content,
      type: row.type,
      publishScope: row.publishScope,
      classId: row.classId,
      isTop: row.isTop,
      status: val
    } as Announcement)
    if (val === 1) {
      ElMessage.success('已发布')
    } else {
      ElMessage.error('已下架')
    }
  } catch (error) {
    row.status = val === 1 ? 0 : 1
    console.error('状态更新失败', error)
  }
}

const handleDelete = (row: Announcement) => {
  ElMessageBox.confirm(`确定删除公告 "${row.title}" 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteAnnouncement(row.id!)
    ElMessage.error('删除成功')
    loadAnnouncementList()
  }).catch(() => {})
}

const handleSizeChange = (val: number) => {
  queryParams.size = val
  loadAnnouncementList()
}

const handleCurrentChange = (val: number) => {
  queryParams.current = val
  loadAnnouncementList()
}

onMounted(() => {
  loadAnnouncementList()
  loadClassList()
})
</script>

<style scoped>
.announcement-manage {
  padding: 0;
}

.search-card {
  margin-bottom: 16px;
  border-radius: 12px;
}

.table-card {
  border-radius: 12px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.top-tag {
  font-weight: bold;
}

.type-tag {
  flex-shrink: 0;
}

.title-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.announcement-detail {
  padding: 20px;
}

.detail-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
  color: #909399;
  font-size: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.detail-content {
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}
</style>
