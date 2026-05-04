<template>
  <div class="student-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的班级学生</span>
        </div>
      </template>

      <el-table :data="studentList" v-loading="loading" border>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="真实姓名" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column prop="className" label="班级" min-width="120" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- 教师无权编辑学生信息，去除编辑入口 -->
      </el-table>

      <el-empty v-if="!loading && studentList.length === 0" description="暂无学生数据" />
    </el-card>

    <!-- 教师无权编辑学生信息 -->
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMyStudents, type UserVO } from '@/api/user'

const loading = ref(false)
const studentList = ref<UserVO[]>([])

// 教师端不提供编辑表单

const loadStudentList = async () => {
  loading.value = true
  try {
    const res = await getMyStudents()
    studentList.value = res
  } catch (error) {
    console.error('加载学生列表失败', error)
  } finally {
    loading.value = false
  }
}

// 去除编辑与提交逻辑

onMounted(() => {
  loadStudentList()
})
</script>

<style scoped>
.student-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
}
</style>
