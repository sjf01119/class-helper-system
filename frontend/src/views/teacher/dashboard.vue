<template>
  <div class="page-shell teacher-dashboard">
    <section class="stats-grid">
      <article v-for="item in statCards" :key="item.label" :class="['metric-card', item.type]">
        <div class="metric-card__top">
          <span class="metric-card__icon">
            <el-icon><component :is="item.icon" /></el-icon>
          </span>
          <el-tag :type="item.tagType" effect="dark" round>{{ item.tag }}</el-tag>
        </div>
        <div class="metric-card__value">{{ item.value }}</div>
        <div class="metric-card__label">{{ item.label }}</div>
        <div class="metric-card__hint">{{ item.hint }}</div>
      </article>
    </section>

    <section class="module-grid">
      <article v-if="userStore.isHeadTeacher" class="panel-card module-card">
        <div class="module-card__header">
          <div class="module-card__title">
            <span class="module-card__icon">
              <el-icon><School /></el-icon>
            </span>
            我的班级
          </div>
          <el-tag type="primary" round>班主任</el-tag>
        </div>
        <div class="module-card__count">{{ dashboard.classCount }}</div>
        <div class="module-card__desc">当前负责管理的班级数量，可继续查看班级成员与邀请码信息。</div>
        <div class="module-card__footer">
          <span class="module-card__trend">适合快速进入班级日常管理</span>
          <el-button link type="primary" @click="goToMyClass">进入班级管理</el-button>
        </div>
      </article>

      <article class="panel-card module-card">
        <div class="module-card__header">
          <div class="module-card__title">
            <span class="module-card__icon">
              <el-icon><DocumentChecked /></el-icon>
            </span>
            待批改作业
          </div>
          <el-tag type="warning" round>批改</el-tag>
        </div>
        <div class="module-card__count">{{ dashboard.pendingReviewCount }}</div>
        <div class="module-card__desc">统一追踪待处理提交记录，减少重复进入页面查找待批改作业的成本。</div>
        <div class="module-card__footer">
          <span class="module-card__trend">支持按课程和班级继续筛选</span>
          <el-button link type="primary" @click="goToHomework">去批改</el-button>
        </div>
      </article>

      <article class="panel-card module-card">
        <div class="module-card__header">
          <div class="module-card__title">
            <span class="module-card__icon">
              <el-icon><Bell /></el-icon>
            </span>
            公告管理
          </div>
          <el-tag type="danger" round>通知</el-tag>
        </div>
        <div class="module-card__count">{{ dashboard.announcementCount }}</div>
        <div class="module-card__desc">集中维护班级公告，配合置顶、发布时间和空态反馈提升消息触达体验。</div>
        <div class="module-card__footer">
          <span class="module-card__trend">适合高频通知发布场景</span>
          <el-button link type="primary" @click="goToAnnouncement">查看公告</el-button>
        </div>
      </article>

      <article class="panel-card module-card">
        <div class="module-card__header">
          <div class="module-card__title">
            <span class="module-card__icon">
              <el-icon><Reading /></el-icon>
            </span>
            我的课程
          </div>
          <el-tag type="success" round>课程</el-tag>
        </div>
        <div class="module-card__count">{{ stats.courseCount }}</div>
        <div class="module-card__desc">快速进入课程视图，衔接资料、成绩和作业管理，保持统一的页面视觉语言。</div>
        <div class="module-card__footer">
          <span class="module-card__trend">课程入口与教学任务更清晰</span>
          <el-button link type="primary" @click="goToCourse">管理课程</el-button>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getTeacherDashboardOverview } from '@/api/dashboard'
import { useUserStore } from '@/stores/user'
import {
  UserFilled,
  Reading,
  EditPen,
  FolderOpened,
  School,
  Bell,
  DocumentChecked
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const stats = reactive({
  studentCount: 0,
  courseCount: 0,
  homeworkCount: 0,
  materialCount: 0
})

const dashboard = reactive({
  classCount: 0,
  pendingReviewCount: 0,
  announcementCount: 0
})

const statCards = computed(() => ([
  {
    label: '班级学生',
    value: stats.studentCount,
    hint: '班级成员数量与教学覆盖范围同步更新',
    icon: UserFilled,
    type: 'is-primary',
    tag: '学生',
    tagType: 'primary'
  },
  {
    label: '我的课程',
    value: stats.courseCount,
    hint: '课程信息与教师授课范围保持一致',
    icon: Reading,
    type: 'is-success',
    tag: '课程',
    tagType: 'success'
  },
  {
    label: '布置作业',
    value: stats.homeworkCount,
    hint: '延续现有发布与批改逻辑，仅升级视觉呈现',
    icon: EditPen,
    type: 'is-warning',
    tag: '作业',
    tagType: 'warning'
  },
  {
    label: '学习资料',
    value: stats.materialCount,
    hint: '支持资料上传、预览和统一管理入口',
    icon: FolderOpened,
    type: 'is-danger',
    tag: '资料',
    tagType: 'danger'
  }
]))

const loadData = async () => {
  try {
    const overviewRes = await getTeacherDashboardOverview()
    stats.studentCount = overviewRes?.studentCount || 0
    stats.courseCount = overviewRes?.courseCount || 0
    stats.homeworkCount = overviewRes?.homeworkCount || 0
    stats.materialCount = overviewRes?.materialCount || 0
    dashboard.classCount = overviewRes?.classCount || 0
    dashboard.pendingReviewCount = overviewRes?.pendingReviewCount || 0
    dashboard.announcementCount = overviewRes?.announcementCount || 0
  } catch (error) {
    console.error('加载数据失败', error)
    stats.studentCount = 0
    stats.courseCount = 0
    stats.homeworkCount = 0
    stats.materialCount = 0
    dashboard.classCount = 0
    dashboard.pendingReviewCount = 0
    dashboard.announcementCount = 0
  }
}

const goToMyClass = () => router.push('/teacher/my-class')
const goToCourse = () => router.push('/teacher/my-course')
const goToHomework = () => router.push('/teacher/homework')
const goToAnnouncement = () => router.push('/teacher/announcement')

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.teacher-dashboard .module-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

@media (max-width: 768px) {
  .teacher-dashboard .module-grid {
    grid-template-columns: 1fr;
  }
}
</style>
