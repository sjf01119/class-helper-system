<template>
  <div class="page-shell student-dashboard">
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
      <article class="panel-card module-card">
        <div class="module-card__header">
          <div class="module-card__title">
            <span class="module-card__icon">
              <el-icon><School /></el-icon>
            </span>
            我的班级
          </div>
          <el-tag :type="myClass ? 'success' : 'info'" round>{{ myClass ? '已加入' : '待加入' }}</el-tag>
        </div>
        <div class="module-card__count">{{ myClass ? 1 : 0 }}</div>
        <div class="module-card__desc">
          {{ myClass ? `当前已加入 ${myClass.className}，可继续查看班级信息与成员情况。` : '当前尚未加入班级，可使用邀请码快速加入。' }}
        </div>
        <div class="module-card__footer">
          <span class="module-card__trend">班级信息会同步影响课程与公告展示</span>
          <el-button link type="primary" @click="myClass ? goToMyClass() : (showJoinClass = true)">
            {{ myClass ? '查看班级' : '加入班级' }}
          </el-button>
        </div>
      </article>

      <article class="panel-card module-card">
        <div class="module-card__header">
          <div class="module-card__title">
            <span class="module-card__icon">
              <el-icon><DocumentChecked /></el-icon>
            </span>
            待提交作业
          </div>
          <el-tag type="warning" round>任务</el-tag>
        </div>
        <div class="module-card__count">{{ stats.pendingHomework }}</div>
        <div class="module-card__desc">统一查看截止时间、提交状态和补交流程，让作业处理路径更清晰。</div>
        <div class="module-card__footer">
          <span class="module-card__trend">建议优先处理即将截止的任务</span>
          <el-button link type="primary" @click="goToHomework">立即查看</el-button>
        </div>
      </article>

      <article class="panel-card module-card">
        <div class="module-card__header">
          <div class="module-card__title">
            <span class="module-card__icon">
              <el-icon><Bell /></el-icon>
            </span>
            公告通知
          </div>
          <el-tag type="danger" round>通知</el-tag>
        </div>
        <div class="module-card__count">{{ stats.announcementCount }}</div>
        <div class="module-card__desc">集中查看班级公告与最新通知，减少切页查找重要消息的成本。</div>
        <div class="module-card__footer">
          <span class="module-card__trend">公告入口统一了详情与时间展示</span>
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
          <el-tag type="primary" round>课程</el-tag>
        </div>
        <div class="module-card__count">{{ stats.courseCount }}</div>
        <div class="module-card__desc">从统一课程入口进入学习内容、资料与课堂信息，页面节奏更加一致。</div>
        <div class="module-card__footer">
          <span class="module-card__trend">配合课程卡片和列表页一起升级</span>
          <el-button link type="primary" @click="goToMyCourse">进入课程</el-button>
        </div>
      </article>
    </section>

    <el-dialog v-model="showJoinClass" title="加入班级" width="480px">
      <el-form :model="joinForm" label-width="88px">
        <el-form-item label="邀请码" required>
          <el-input v-model="joinForm.inviteCode" placeholder="请输入班级邀请码" maxlength="8" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showJoinClass = false">取消</el-button>
        <el-button type="primary" :loading="joinLoading" @click="handleJoinClass">确认加入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { joinClassByInviteCode, type ClassVO } from '@/api/class'
import { getStudentDashboardOverview } from '@/api/dashboard'
import {
  Reading,
  School,
  EditPen,
  Bell,
  DataLine,
  DocumentChecked
} from '@element-plus/icons-vue'

const router = useRouter()

const stats = reactive({
  courseCount: 0,
  pendingHomework: 0,
  submittedHomework: 0,
  announcementCount: 0
})

const myClass = ref<ClassVO>()
const showJoinClass = ref(false)
const joinLoading = ref(false)

const joinForm = reactive({
  inviteCode: ''
})

const statCards = computed(() => ([
  {
    label: '我的课程',
    value: stats.courseCount,
    hint: '课程数与当前学生课程绑定保持一致',
    icon: Reading,
    type: 'is-primary',
    tag: '课程',
    tagType: 'primary'
  },
  {
    label: '加入班级',
    value: myClass.value ? 1 : 0,
    hint: myClass.value ? `已加入 ${myClass.value.className}` : '输入邀请码即可加入班级',
    icon: School,
    type: 'is-success',
    tag: '班级',
    tagType: myClass.value ? 'success' : 'info'
  },
  {
    label: '待提交作业',
    value: stats.pendingHomework,
    hint: '按截止时间与提交状态统一查看',
    icon: EditPen,
    type: 'is-warning',
    tag: '作业',
    tagType: 'warning'
  },
  {
    label: '班级公告',
    value: stats.announcementCount,
    hint: '及时查看公告通知与课程消息',
    icon: DataLine,
    type: 'is-danger',
    tag: '公告',
    tagType: 'danger'
  }
]))

const loadData = async () => {
  try {
    const overview = await getStudentDashboardOverview({
      pendingLimit: 5,
      announcementLimit: 5
    })

    Object.assign(stats, {
      courseCount: overview?.stats?.courseCount ?? 0,
      pendingHomework: overview?.stats?.pendingHomework ?? 0,
      submittedHomework: overview?.stats?.submittedHomework ?? 0,
      announcementCount: overview?.stats?.announcementCount ?? 0
    })
    myClass.value = overview?.myClass || undefined
  } catch (error) {
    console.error('加载数据失败', error)
  }
}

const handleJoinClass = async () => {
  if (!joinForm.inviteCode) {
    ElMessage.warning('请输入邀请码')
    return
  }
  joinLoading.value = true
  try {
    await joinClassByInviteCode(joinForm.inviteCode)
    ElMessage.success('加入班级成功')
    showJoinClass.value = false
    joinForm.inviteCode = ''
    loadData()
  } catch (error) {
    console.error('加入班级失败', error)
  } finally {
    joinLoading.value = false
  }
}

const goToMyClass = () => router.push('/student/my-class')
const goToMyCourse = () => router.push('/student/my-course')
const goToHomework = () => router.push('/student/homework')
const goToAnnouncement = () => router.push('/student/announcement')

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.student-dashboard .module-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

@media (max-width: 768px) {
  .student-dashboard .module-grid {
    grid-template-columns: 1fr;
  }
}
</style>
