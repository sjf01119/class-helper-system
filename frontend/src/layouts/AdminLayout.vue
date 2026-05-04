<template>
  <el-container class="app-shell admin-shell">
    <el-aside :width="isCollapse ? '92px' : '248px'" :class="['app-sidebar', { 'is-collapse': isCollapse }]">
      <div class="app-sidebar__panel">
        <div class="app-brand">
          <span class="app-brand__mark">
            <el-icon><Reading /></el-icon>
          </span>
          <div class="app-brand__body">
            <span class="app-brand__title">学习辅助系统</span>
            <span class="app-brand__subtitle">Campus Management Suite</span>
          </div>
        </div>

        <div class="app-nav">
          <el-menu
            :default-active="activeMenu"
            :collapse="isCollapse"
            :collapse-transition="true"
            router
            unique-opened
          >
            <el-menu-item index="/admin/dashboard">
              <el-icon><HomeFilled /></el-icon>
              <span>系统首页</span>
            </el-menu-item>

            <el-sub-menu index="/admin/user">
              <template #title>
                <el-icon><UserFilled /></el-icon>
                <span>用户管理</span>
              </template>
              <el-menu-item index="/admin/teacher-manage">教师管理</el-menu-item>
              <el-menu-item index="/admin/student-manage">学生管理</el-menu-item>
            </el-sub-menu>

            <el-menu-item index="/admin/class-manage">
              <el-icon><School /></el-icon>
              <span>班级管理</span>
            </el-menu-item>

            <el-menu-item index="/admin/announcement-manage">
              <el-icon><Bell /></el-icon>
              <span>系统公告</span>
            </el-menu-item>

            <el-sub-menu index="/admin/log">
              <template #title>
                <el-icon><Document /></el-icon>
                <span>日志管理</span>
              </template>
              <el-menu-item index="/admin/login-log">登录日志</el-menu-item>
              <el-menu-item index="/admin/operation-log">操作日志</el-menu-item>
            </el-sub-menu>
          </el-menu>
        </div>

        <div class="layout-footer">
          <span class="app-role-badge">管理员端</span>
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
              <span class="app-page-meta__title">{{ pageTitle }}</span>
              <span class="app-page-meta__subtitle">{{ pageSubtitle }}</span>
            </div>
          </div>

          <div class="app-header__right">
            <el-dropdown @command="handleCommand">
              <div class="app-user-trigger">
                <el-avatar :size="44" :src="userStore.userInfo?.avatarUrl || defaultAvatar">
                  {{ userStore.userInfo?.realName?.charAt(0) || 'A' }}
                </el-avatar>
                <div class="app-user-trigger__meta">
                  <span class="app-user-trigger__name">{{ userStore.userInfo?.realName || '管理员' }}</span>
                  <span class="app-user-trigger__role">系统管理角色</span>
                </div>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item disabled>当前角色：管理员</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <el-main class="app-content">
        <div class="app-content__inner">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
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
  Bell,
  Document,
  ArrowDown,
  Fold,
  Expand,
  Reading
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => String(route.meta.title || '系统首页'))
const pageSubtitle = computed(() => {
  if (route.path === '/admin/dashboard') {
    return '统一查看教师、学生、班级、公告与系统运行概览'
  }
  return '统一管理用户、班级、公告与系统运行数据'
})

const handleCommand = (command: string) => {
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
.admin-shell :deep(.app-header) {
  min-height: 68px;
  padding-top: 18px;
}

.admin-shell :deep(.app-header__panel) {
  min-height: 64px;
  padding: 12px 16px;
  border-radius: 12px;
}

.admin-shell :deep(.app-header__left),
.admin-shell :deep(.app-header__right) {
  gap: 12px;
}

.admin-shell :deep(.app-page-meta) {
  gap: 4px;
}

.admin-shell :deep(.app-page-meta__title) {
  line-height: 1.3;
}

.admin-shell :deep(.app-page-meta__subtitle) {
  font-size: 13px;
  line-height: 1.5;
}

.admin-shell :deep(.app-user-trigger) {
  gap: 10px;
  max-width: 220px;
  padding: 6px 10px 6px 8px;
  border-radius: 12px;
}

.admin-shell :deep(.app-user-trigger__name) {
  max-width: 88px;
}

.admin-shell :deep(.app-user-trigger__role) {
  font-size: 11px;
}

.admin-shell :deep(.el-avatar) {
  width: 44px;
  height: 44px;
}

.layout-footer {
  padding: 18px 12px 6px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.24s ease, transform 0.24s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

@media (max-width: 768px) {
  .admin-shell :deep(.app-header__panel) {
    padding: 12px 14px;
  }

  .admin-shell :deep(.app-user-trigger) {
    max-width: 100%;
  }
}
</style>
