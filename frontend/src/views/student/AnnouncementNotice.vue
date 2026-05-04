<template>
  <div class="announcement-notice">
    <el-card shadow="never" class="toolbar-card">
      <el-radio-group v-model="scope" @change="applyFilter">
        <el-radio-button label="all">全部公告</el-radio-button>
        <el-radio-button label="system">系统公告</el-radio-button>
        <el-radio-button label="class">班级公告</el-radio-button>
        <el-radio-button label="top">置顶公告</el-radio-button>
      </el-radio-group>
    </el-card>

    <el-card shadow="never">
      <el-table :data="filteredList" v-loading="loading" border stripe>
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="title" label="标题" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="title-wrap">
              <el-tag v-if="isTop(row)" type="danger" size="small">置顶</el-tag>
              <span>{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="范围" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="isClass(row) ? 'warning' : 'success'" size="small">
              {{ isClass(row) ? '班级' : '系统' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.publishTime || row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="100" align="center" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && filteredList.length === 0" description="暂无公告数据" />
    </el-card>

    <el-dialog v-model="detailVisible" title="公告详情" width="720px">
      <div v-if="current">
        <h3 class="detail-title">
          <el-tag v-if="isTop(current)" type="danger" size="small">置顶</el-tag>
          {{ current.title }}
        </h3>
        <div class="detail-meta">
          <span>发布人：{{ current.publisherName || '暂无数据' }}</span>
          <span>发布时间：{{ formatDateTime(current.publishTime || current.createdAt) }}</span>
          <span>浏览量：{{ current.viewCount ?? 0 }}</span>
        </div>
        <div class="detail-content">{{ current.content || '暂无公告内容' }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAnnouncementById, getAnnouncementList, type Announcement } from '@/api/announcement'

type ScopeType = 'all' | 'system' | 'class' | 'top'

const loading = ref(false)
const detailVisible = ref(false)
const scope = ref<ScopeType>('all')
const list = ref<Announcement[]>([])
const filteredList = ref<Announcement[]>([])
const current = ref<Announcement>()

const isTop = (item: Announcement) => (item.isTop ?? item.priority ?? 0) === 1
const isClass = (item: Announcement) => (item.publishScope ?? (item.type === 2 ? 1 : 0)) === 1
const formatDateTime = (value?: string) => (value ? value.replace('T', ' ').slice(0, 19) : '暂无数据')

const sortList = (items: Announcement[]) => {
  return [...items].sort((a, b) => {
    const topDiff = Number(isTop(b)) - Number(isTop(a))
    if (topDiff !== 0) return topDiff
    const bTime = new Date(b.publishTime || b.createdAt || 0).getTime()
    const aTime = new Date(a.publishTime || a.createdAt || 0).getTime()
    return bTime - aTime
  })
}

const applyFilter = () => {
  let next = [...list.value]
  if (scope.value === 'system') next = next.filter(item => !isClass(item))
  if (scope.value === 'class') next = next.filter(item => isClass(item))
  if (scope.value === 'top') next = next.filter(item => isTop(item))
  filteredList.value = sortList(next)
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await getAnnouncementList({ status: 1 })
    list.value = (res || []) as Announcement[]
    applyFilter()
  } catch (error) {
    console.error('加载公告失败', error)
    list.value = []
    filteredList.value = []
    ElMessage.error('加载公告失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const openDetail = async (item: Announcement) => {
  if (!item.id) return
  try {
    const detail = await getAnnouncementById(item.id)
    current.value = detail as Announcement
    detailVisible.value = true
    await loadList()
  } catch (error) {
    console.error('加载公告详情失败', error)
    ElMessage.error('加载公告详情失败，请稍后重试')
  }
}

onMounted(loadList)
</script>

<style scoped>
.announcement-notice {
  padding: 0;
}

.toolbar-card {
  margin-bottom: 16px;
}

.title-wrap {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.detail-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
}

.detail-meta {
  margin: 12px 0;
  color: #909399;
  font-size: 13px;
  display: flex;
  gap: 20px;
}

.detail-content {
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
}
</style>
