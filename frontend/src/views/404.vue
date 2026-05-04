<template>
  <div class="not-found">
    <div class="content">
      <div class="icon-wrapper">
        <el-icon :size="80" color="#e6a23c"><WarningFilled /></el-icon>
      </div>
      <h1 class="code">404</h1>
      <p class="message">抱歉，您访问的页面不存在</p>
      <p class="hint" v-if="path">当前路径：{{ path }}</p>
      <el-button type="primary" size="large" @click="goHome">
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { WarningFilled } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const path = computed(() => {
  const match = route.path
  return match === '/:pathMatch(.*)*' ? '' : match
})

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.not-found {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
}

.content {
  text-align: center;
  padding: 60px;
}

.icon-wrapper {
  margin-bottom: 24px;
}

.code {
  font-size: 72px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 16px;
  letter-spacing: 8px;
}

.message {
  font-size: 18px;
  color: #606266;
  margin: 0 0 12px;
}

.hint {
  font-size: 14px;
  color: #909399;
  margin: 0 0 32px;
  word-break: break-all;
}
</style>
