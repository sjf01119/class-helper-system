<template>
  <el-container class="app-shell student-shell">
    <el-aside :width="isCollapse ? '92px' : '248px'" :class="['app-sidebar', { 'is-collapse': isCollapse }]">
      <div class="app-sidebar__panel">
        <div class="app-brand">
          <span class="app-brand__mark">
            <el-icon><Notebook /></el-icon>
          </span>
          <div class="app-brand__body">
            <span class="app-brand__title">学生学习台</span>
            <span class="app-brand__subtitle">Student Learning Hub</span>
          </div>
        </div>

        <div class="app-nav">
          <el-menu
            :default-active="activeMenu"
            :collapse="isCollapse"
            :collapse-transition="true"
            router
          >
            <el-menu-item index="/student/dashboard">
              <el-icon><HomeFilled /></el-icon>
              <span>学生首页</span>
            </el-menu-item>
            <el-menu-item index="/student/my-class">
              <el-icon><School /></el-icon>
              <span>我的班级</span>
            </el-menu-item>
            <el-menu-item index="/student/my-course">
              <el-icon><Reading /></el-icon>
              <span>我的课程</span>
            </el-menu-item>
            <el-menu-item index="/student/homework">
              <el-icon><EditPen /></el-icon>
              <span>作业中心</span>
            </el-menu-item>
            <el-menu-item index="/student/material">
              <el-icon><Folder /></el-icon>
              <span>学习资料</span>
            </el-menu-item>
            <el-menu-item index="/student/announcement">
              <el-icon><Bell /></el-icon>
              <span>公告通知</span>
            </el-menu-item>
            <el-menu-item index="/student/grade">
              <el-icon><DataLine /></el-icon>
              <span>成绩查询</span>
            </el-menu-item>
            <el-menu-item index="/student/profile">
              <el-icon><UserFilled /></el-icon>
              <span>个人中心</span>
            </el-menu-item>
          </el-menu>
        </div>

        <div class="layout-footer">
          <span class="app-role-badge">学生视图</span>
        </div>
      </div>
    </el-aside>

    <el-container class="app-main">
      <el-header class="app-header">
        <div class="app-header__panel">
          <div class="app-header__left">
            <el-button class="app-collapse-btn" circle @click="isCollapse = !isCollapse">
              <el-icon>
                <Expand v-if="isCollapse" />
                <Fold v-else />
              </el-icon>
            </el-button>
            <div class="app-page-meta">
              <span class="app-page-meta__role">学生学习台</span>
              <span class="app-page-meta__title">{{ pageTitle }}</span>
              <span class="app-page-meta__subtitle">{{ pageSubtitle }}</span>
            </div>
          </div>

          <div class="app-header__right">
            <el-dropdown @command="handleCommand">
              <div class="app-user-trigger">
                <el-avatar :size="44" :src="userStore.userInfo?.avatarUrl || defaultAvatar">
                  {{ userStore.userInfo?.realName?.charAt(0) || 'S' }}
                </el-avatar>
                <div class="app-user-trigger__meta">
                  <span class="app-user-trigger__name">{{ userStore.userInfo?.realName || '学生用户' }}</span>
                  <span class="app-user-trigger__role">{{ userStore.userInfo?.className || '学习空间' }}</span>
                </div>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <el-main class="app-content">
        <div class="app-content__inner">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  HomeFilled,
  UserFilled,
  School,
  Reading,
  ArrowDown,
  EditPen,
  Folder,
  Bell,
  DataLine,
  Fold,
  Expand,
  Notebook
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => String(route.meta.title || '学生首页'))
const pageSubtitle = computed(() => {
  if (route.path.includes('/student/homework')) return '查看待提交作业、提交记录与截止时间安排'
  if (route.path.includes('/student/material')) return '按课程与班级查阅学习资料，支持在线预览与下载'
  if (route.path.includes('/student/announcement')) return '集中接收班级通知与最新公告内容'
  if (route.path.includes('/student/grade')) return '快速查看课程成绩和学习结果反馈'
  if (route.path.includes('/student/my-course')) return '以更清晰的课程卡片浏览学习内容'
  return '围绕班级、课程、作业与成绩构建统一学习体验'
})

const handleCommand = (command: string) => {
  if (command === 'profile') {
    router.push('/student/profile')
    return
  }
  if (command === 'logout') {
    ElMessageBox.confirm('确定退出登录吗？', '提示', {
      type: 'warning'
    }).then(() => {
      userStore.logout()
      ElMessage.error('退出成功')
      router.push('/login')
    }).catch(() => {})
  }
}
</script>

<style scoped>
.student-shell :deep(.app-header) {
  min-height: 68px;
  padding-top: 18px;
}

.student-shell :deep(.app-header__panel) {
  min-height: 64px;
  padding: 12px 16px;
  border-radius: 12px;
}

.student-shell :deep(.app-header__left),
.student-shell :deep(.app-header__right) {
  gap: 12px;
}

.student-shell :deep(.app-page-meta) {
  gap: 4px;
}

.student-shell :deep(.app-page-meta__title) {
  line-height: 1.3;
}

.student-shell :deep(.app-page-meta__subtitle) {
  font-size: 13px;
  line-height: 1.5;
}

.student-shell :deep(.app-brand__subtitle) {
  font-size: 10px;
}

.student-shell :deep(.app-user-trigger) {
  gap: 10px;
  max-width: 220px;
  padding: 6px 10px 6px 8px;
  border-radius: 12px;
}

.student-shell :deep(.app-user-trigger__name) {
  max-width: 88px;
}

.student-shell :deep(.app-user-trigger__role) {
  font-size: 11px;
}

.student-shell :deep(.el-avatar) {
  width: 44px;
  height: 44px;
}

.layout-footer {
  padding: 18px 12px 6px;
}

@media (max-width: 768px) {
  .student-shell :deep(.app-header__panel) {
    padding: 12px 14px;
  }

  .student-shell :deep(.app-user-trigger) {
    max-width: 100%;
  }
}
</style>
