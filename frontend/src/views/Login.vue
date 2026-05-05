<template>
  <AuthPageLayout
    title="学习辅助系统"
    subtitle="校园学习协同平台登录入口"
    link-text="没有账号？"
    link-label="学生注册"
    link-to="/register"
    visual-type="login"
  >
    <el-form
      ref="loginFormRef"
      :model="loginForm"
      :rules="loginRules"
      class="auth-form"
      @keyup.enter="handleLogin"
    >
      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
          placeholder="请输入用户名"
          :prefix-icon="User"
          size="large"
        />
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          placeholder="请输入密码"
          :prefix-icon="Lock"
          size="large"
          show-password
        />
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          size="large"
          class="auth-submit-button"
          :loading="loading"
          @click="handleLogin"
        >
          {{ loading ? '登录中...' : '登录' }}
        </el-button>
      </el-form-item>
    </el-form>

   
  </AuthPageLayout>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import AuthPageLayout from '@/components/auth/AuthPageLayout.vue'
import { useUserStore } from '@/stores/user'
import { getDefaultHomePathByScope, resolveLoginRoleScope } from '@/utils/auth'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const loginFormRef = ref<FormInstance>()

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res: any = await request.post('/auth/login', {
          username: loginForm.username,
          password: loginForm.password
        })
        
        // 合并用户信息，包含roles
        const userInfoWithRoles = {
          ...res.userInfo,
          roles: res.roles || []
        }
        const redirect = route.query.redirect as string | undefined
        const loginScope = resolveLoginRoleScope(userInfoWithRoles.roles, redirect)

        userStore.setToken(res.token, loginScope)
        userStore.setUserInfo(userInfoWithRoles)
        
        ElMessage.success('登录成功')

        if (redirect) {
          router.push(redirect)
        } else {
          router.push(getDefaultHomePathByScope(loginScope))
        }
      } catch (error: any) {
        ElMessage.error(error.message || '登录失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>
