<template>
  <div class="page-shell admin-dashboard">
    <section class="stats-grid">
      <article v-for="item in statCards" :key="item.label" class="metric-card">
        <span class="metric-card__icon">
          <el-icon><component :is="item.icon" /></el-icon>
        </span>
        <div class="metric-card__content">
          <div class="metric-card__value">{{ item.value }}</div>
          <div class="metric-card__label">{{ item.label }}</div>
        </div>
      </article>
    </section>

    <section class="dashboard-row dashboard-row--charts">
        <section class="panel-card chart-panel">
          <div class="chart-panel__header">
            <div class="chart-panel__title">
              <el-icon><TrendCharts /></el-icon>
              用户增长趋势
            </div>
          </div>
          <div ref="userChartRef" class="chart-panel__body"></div>
        </section>

        <section class="panel-card chart-panel">
          <div class="chart-panel__header">
            <div class="chart-panel__title">
              <el-icon><PieChart /></el-icon>
              班级分布统计
            </div>
          </div>
          <div ref="classChartRef" class="chart-panel__body"></div>
        </section>
    </section>

    <section class="dashboard-row dashboard-row--lists">
        <section class="panel-card table-panel">
          <div class="table-panel__header">
            <div class="table-panel__title">
              <el-icon><Bell /></el-icon>
              最新公告
            </div>
            <el-button link type="primary" @click="router.push('/admin/announcement-manage')">查看更多</el-button>
          </div>

          <div v-if="recentAnnouncements.length > 0" class="data-list">
            <div v-for="item in recentAnnouncements" :key="item.id" class="data-list__item">
              <div class="data-list__title" @click="showAnnouncementDetail(item)">
                <span v-if="item.isTop === 1 || item.priority === 1" class="data-list__flag">置顶</span>
                <span class="data-list__text">{{ item.title }}</span>
              </div>
              <span class="data-list__time">{{ formatTime(item.publishTime) }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无公告" />
        </section>

        <section class="panel-card table-panel">
          <div class="table-panel__header">
            <div class="table-panel__title">
              <el-icon><Clock /></el-icon>
              最近登录
            </div>
            <el-button link type="primary" @click="router.push('/admin/login-log')">查看更多</el-button>
          </div>

          <el-table v-if="recentLoginLogs.length > 0" :data="recentLoginLogs" size="small">
            <el-table-column prop="username" label="用户" min-width="90" />
            <el-table-column prop="ip" label="IP 地址" min-width="130" />
            <el-table-column label="状态" width="86">
              <template #default="{ row }">
                <el-tag :type="row.loginStatus === 1 ? 'success' : 'danger'" size="small">
                  {{ row.loginStatus === 1 ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="时间" min-width="156">
              <template #default="{ row }">
                {{ formatTime(row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无登录记录" />
        </section>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onActivated, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getAnnouncementList } from '@/api/announcement'
import { getLoginLogPage } from '@/api/log'
import { getClassDistribution, getDashboardOverview, getLatestAnnouncements, getRecentLoginLogs, getUserGrowthTrend } from '@/api/dashboard'
import type { Announcement } from '@/api/announcement'
import type { LoginLog } from '@/api/log'
import type { ClassDistributionItem, UserGrowthItem } from '@/api/dashboard'
import {
  UserFilled,
  User,
  School,
  Bell,
  TrendCharts,
  PieChart,
  Clock
} from '@element-plus/icons-vue'

const router = useRouter()
const stats = ref({
  teacherCount: 0,
  studentCount: 0,
  classCount: 0,
  announcementCount: 0
})

const recentAnnouncements = ref<Announcement[]>([])
const recentLoginLogs = ref<LoginLog[]>([])

const userChartRef = ref<HTMLElement>()
const classChartRef = ref<HTMLElement>()
let userChart: any = null
let classChart: any = null
const userGrowthData = ref<UserGrowthItem[]>([])
const classDistributionData = ref<ClassDistributionItem[]>([])

const statCards = computed(() => ([
  {
    label: '教师人数',
    value: stats.value.teacherCount,
    icon: UserFilled
  },
  {
    label: '学生人数',
    value: stats.value.studentCount,
    icon: User
  },
  {
    label: '班级数量',
    value: stats.value.classCount,
    icon: School
  },
  {
    label: '公告数量',
    value: stats.value.announcementCount,
    icon: Bell
  }
]))

const formatTime = (time?: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const showAnnouncementDetail = (_item: Announcement) => {
  // 可以添加查看详情逻辑
}

const loadStats = async () => {
  try {
    const overview = await getDashboardOverview()
    stats.value.teacherCount = overview?.teacherCount || 0
    stats.value.studentCount = overview?.studentCount || 0
    stats.value.classCount = overview?.classCount || 0
    stats.value.announcementCount = overview?.announcementCount || 0
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

const loadRecentData = async () => {
  try {
    const [announcementRes, loginRes] = await Promise.all([
      getLatestAnnouncements(2),
      getRecentLoginLogs(5)
    ])
    recentAnnouncements.value = announcementRes || []
    recentLoginLogs.value = loginRes || []
  } catch (error) {
    console.warn('新仪表盘接口不可用，回退旧接口', error)
    try {
      const [announcementRes, logRes] = await Promise.all([
        getAnnouncementList({ status: 1 }),
        getLoginLogPage({ current: 1, size: 5 })
      ])
      recentAnnouncements.value = (announcementRes || []).slice(0, 2)
      recentLoginLogs.value = logRes.records || []
    } catch (fallbackError) {
      console.error('加载最近数据失败', fallbackError)
      recentAnnouncements.value = []
      recentLoginLogs.value = []
    }
  }
}

const loadChartData = async () => {
  try {
    const [growthRes, distributionRes] = await Promise.all([
      getUserGrowthTrend(),
      getClassDistribution()
    ])
    userGrowthData.value = growthRes || []
    classDistributionData.value = distributionRes || []
    renderUserChart()
    renderClassChart()
  } catch (error) {
    console.error('加载图表数据失败', error)
    userGrowthData.value = []
    classDistributionData.value = []
    renderUserChart()
    renderClassChart()
  }
}

const renderUserChart = () => {
  if (!userChart) return
  const hasData = userGrowthData.value.some(item => Number(item.value) > 0)
  userChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: 24, left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: userGrowthData.value.map(item => item.month),
      boundaryGap: false,
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#e5e6eb' } },
      axisLabel: { color: '#86909c' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#86909c' },
      splitLine: { lineStyle: { color: '#e5e6eb', type: 'dashed' } }
    },
    series: [{
      data: userGrowthData.value.map(item => item.value),
      type: 'line',
      smooth: true,
      symbolSize: 7,
      lineStyle: { width: 3, color: '#165dff' },
      itemStyle: {
        color: '#165dff',
        borderColor: '#ffffff',
        borderWidth: 2
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(22, 93, 255, 0.16)' },
          { offset: 1, color: 'rgba(22, 93, 255, 0.02)' }
        ])
      }
    }],
    title: hasData
      ? undefined
      : {
          text: '暂无数据',
          left: 'center',
          top: 'center',
          textStyle: { color: '#86909c', fontSize: 14, fontWeight: 'normal' }
        }
  }, true)
}

const renderClassChart = () => {
  if (!classChart) return
  const hasData = classDistributionData.value.some(item => Number(item.value) > 0)
  const pieColors = ['#165dff', '#4080ff', '#69b1ff', '#94d4ff', '#bedaff', '#86909c', '#b8c0cc', '#d0d7e2']
  classChart.setOption({
    tooltip: { trigger: 'item' },
    legend: {
      bottom: '3%',
      left: 'center',
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      textStyle: { color: '#86909c', fontSize: 12 }
    },
    series: [{
      type: 'pie',
      radius: ['48%', '72%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 6,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: { show: false },
      labelLine: { show: false },
      data: classDistributionData.value.map((item, index) => ({
        value: item.value,
        name: item.name,
        itemStyle: { color: pieColors[index % pieColors.length] }
      }))
    }],
    title: hasData
      ? undefined
      : {
          text: '暂无数据',
          left: 'center',
          top: 'center',
          textStyle: { color: '#86909c', fontSize: 14, fontWeight: 'normal' }
        }
  }, true)
}

const initCharts = () => {
  if (userChartRef.value) {
    userChart = echarts.init(userChartRef.value)
    renderUserChart()
  }

  if (classChartRef.value) {
    classChart = echarts.init(classChartRef.value)
    renderClassChart()
  }
}

const loadDashboardData = async () => {
  await Promise.all([
    loadStats(),
    loadRecentData(),
    loadChartData()
  ])
}

const handleResize = () => {
  userChart?.resize()
  classChart?.resize()
}

onMounted(() => {
  initCharts()
  loadDashboardData()
  window.addEventListener('resize', handleResize)
})

onActivated(() => {
  loadDashboardData()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  userChart?.dispose()
  classChart?.dispose()
})
</script>

<style scoped>
.admin-dashboard {
  gap: 24px;
  padding-top: 0;
}

.admin-dashboard > section,
.admin-dashboard > div {
  margin-top: 0;
}

.admin-dashboard .stats-grid {
  margin: 0;
  padding: 0;
  align-self: stretch;
}

.admin-dashboard .dashboard-row {
  display: grid;
  gap: 24px;
  margin: 0;
  align-items: start;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.admin-dashboard .metric-card,
.admin-dashboard .panel-card {
  border: none;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 4px 16px rgba(29, 33, 41, 0.06);
}

.admin-dashboard .metric-card {
  display: flex;
  align-items: center;
  gap: 16px;
  min-height: 100px;
  padding: 16px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.admin-dashboard .metric-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 24px rgba(29, 33, 41, 0.1);
}

.admin-dashboard .metric-card__icon {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  font-size: 18px;
  color: #165dff;
  background: rgba(22, 93, 255, 0.08);
}

.metric-card__content {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
  padding-right: 4px;
}

.admin-dashboard .metric-card__value {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  line-height: 1;
  color: #1d2129;
}

.admin-dashboard .metric-card__label {
  margin: 0;
  font-size: 14px;
  line-height: 1.4;
  color: #86909c;
}

.admin-dashboard .chart-panel,
.admin-dashboard .table-panel {
  padding: 24px;
}

.admin-dashboard .chart-panel__header,
.admin-dashboard .table-panel__header {
  margin-bottom: 16px;
}

.admin-dashboard .chart-panel__title,
.admin-dashboard .table-panel__title {
  gap: 8px;
  font-size: 18px;
}

.admin-dashboard .chart-panel__title .el-icon,
.admin-dashboard .table-panel__title .el-icon {
  color: #165dff;
}

.admin-dashboard .chart-panel__body {
  height: 300px;
  margin-top: 0;
}

.data-list__title {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.data-list__flag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 22px;
  padding: 0 8px;
  border-radius: 999px;
  color: #165dff;
  background: rgba(22, 93, 255, 0.08);
  font-size: 12px;
  flex-shrink: 0;
}

.admin-dashboard .data-list__item {
  padding: 16px 0;
}

.admin-dashboard .data-list__text {
  max-width: none;
}

.admin-dashboard :deep(.el-table) {
  --el-table-header-bg-color: #f7f8fa;
  --el-table-row-hover-bg-color: #f5f7fa;
  border-radius: 8px;
}

.admin-dashboard :deep(.el-table th.el-table__cell) {
  color: #1d2129;
}

.admin-dashboard :deep(.el-table td.el-table__cell),
.admin-dashboard :deep(.el-table .cell) {
  color: #4e5969;
}

@media (max-width: 1320px) {
  .admin-dashboard .dashboard-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .admin-dashboard .metric-card {
    min-height: auto;
  }
}
</style>
