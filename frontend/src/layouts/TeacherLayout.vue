<template>
  <el-container class="app-shell teacher-shell">
    <el-aside :width="isCollapse ? '92px' : '248px'" :class="['app-sidebar', { 'is-collapse': isCollapse }]">
      <div class="app-sidebar__panel">
        <div class="app-brand">
          <span class="app-brand__mark">
            <el-icon><Notebook /></el-icon>
          </span>
          <div class="app-brand__body">
            <span class="app-brand__title">教师工作台</span>
            <span class="app-brand__subtitle">Teaching Management</span>
          </div>
        </div>

        <div class="app-nav">
          <el-menu
            :default-active="activeMenu"
            :collapse="isCollapse"
            :collapse-transition="true"
            router
          >
            <el-menu-item index="/teacher/dashboard">
              <el-icon><HomeFilled /></el-icon>
              <span>教师首页</span>
            </el-menu-item>
            <el-menu-item v-if="userStore.isHeadTeacher" index="/teacher/my-class">
              <el-icon><School /></el-icon>
              <span>我的班级</span>
            </el-menu-item>
            <el-menu-item index="/teacher/my-course">
              <el-icon><Reading /></el-icon>
              <span>我的课程</span>
            </el-menu-item>
            <el-menu-item index="/teacher/homework">
              <el-icon><EditPen /></el-icon>
              <span>作业管理</span>
            </el-menu-item>
            <el-menu-item index="/teacher/material">
              <el-icon><Folder /></el-icon>
              <span>学习资料</span>
            </el-menu-item>
            <el-menu-item index="/teacher/announcement">
              <el-icon><Bell /></el-icon>
              <span>公告管理</span>
            </el-menu-item>
            <el-menu-item index="/teacher/grade">
              <el-icon><DataLine /></el-icon>
              <span>成绩管理</span>
            </el-menu-item>
            <el-menu-item index="/teacher/profile">
              <el-icon><UserFilled /></el-icon>
              <span>个人中心</span>
            </el-menu-item>
          </el-menu>
        </div>

        <div class="layout-footer">
          <span class="app-role-badge">{{ userStore.isHeadTeacher ? '班主任权限已启用' : '授课教师视图' }}</span>
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
              <span class="app-page-meta__role">教师工作台</span>
              <span class="app-page-meta__title">{{ pageTitle }}</span>
              <span class="app-page-meta__subtitle">{{ pageSubtitle }}</span>
            </div>
          </div>

          <div class="app-header__right">
            <el-dropdown @command="handleCommand">
              <div class="app-user-trigger">
                <el-avatar :size="44" :src="userStore.userInfo?.avatarUrl || defaultAvatar">
                  {{ userStore.userInfo?.realName?.charAt(0) || 'T' }}
                </el-avatar>
                <div class="app-user-trigger__meta">
                  <span class="app-user-trigger__name">{{ userStore.userInfo?.realName || '教师用户' }}</span>
                  <span class="app-user-trigger__role">
                    {{ userStore.isHeadTeacher ? '班主任 / 教师' : '授课教师' }}
                  </span>
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
  School,
  Reading,
  EditPen,
  Folder,
  Bell,
  ArrowDown,
  DataLine,
  UserFilled,
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
const pageTitle = computed(() => String(route.meta.title || '教师首页'))
const pageSubtitle = computed(() => {
  if (route.path.includes('/teacher/homework')) return '集中处理作业发布、批改与提交跟踪'
  if (route.path.includes('/teacher/material')) return '统一管理课程资料上传、预览与分发'
  if (route.path.includes('/teacher/grade')) return '查看学生成绩、统计表现与课程评分结果'
  if (route.path.includes('/teacher/announcement')) return '维护班级公告发布节奏与消息触达'
  if (route.path.includes('/teacher/my-class')) return '查看班级成员、邀请码与班主任信息'
  return '围绕课程、作业、班级与资料进行日常教学管理'
})

const handleCommand = (command: string) => {
  if (command === 'profile') {
    router.push('/teacher/profile')
    return
  }
  if (command === 'logout') {
    ElMessageBox.confirm('确定退出登录吗？', '提示', {
      type: 'warning'
    }).then(() => {
      userStore.logout()
      ElMessage.success('退出成功')
      router.push('/login')
    }).catch(() => {})
  }
}
</script>

<style scoped>
.teacher-shell :deep(.app-header) {
  min-height: 68px;
  padding-top: 18px;
}

.teacher-shell :deep(.app-header__panel) {
  min-height: 64px;
  padding: 12px 16px;
  border-radius: 12px;
}

.teacher-shell :deep(.app-header__left),
.teacher-shell :deep(.app-header__right) {
  gap: 12px;
}

.teacher-shell :deep(.app-page-meta) {
  gap: 4px;
}

.teacher-shell :deep(.app-page-meta__title) {
  line-height: 1.3;
}

.teacher-shell :deep(.app-page-meta__subtitle) {
  font-size: 13px;
  line-height: 1.5;
}

.teacher-shell :deep(.app-user-trigger) {
  gap: 10px;
  max-width: 220px;
  padding: 6px 10px 6px 8px;
  border-radius: 12px;
}

.teacher-shell :deep(.app-user-trigger__name) {
  max-width: 88px;
}

.teacher-shell :deep(.app-user-trigger__role) {
  font-size: 11px;
}

.teacher-shell :deep(.el-avatar) {
  width: 44px;
  height: 44px;
}

.layout-footer {
  padding: 18px 12px 6px;
}

@media (max-width: 768px) {
  .teacher-shell :deep(.app-header__panel) {
    padding: 12px 14px;
  }

  .teacher-shell :deep(.app-user-trigger) {
    max-width: 100%;
  }
}
</style>
