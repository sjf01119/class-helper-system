<template>
  <AuthPageLayout
    title="学生注册"
    subtitle="填写基础信息，完成学生账号注册"
    link-text="已有账号？"
    link-label="返回登录"
    link-to="/login"
    visual-type="register"
  >
    <el-form
      ref="registerFormRef"
      :model="registerForm"
      :rules="registerRules"
      class="auth-form"
      :show-message="false"
      @keyup.enter="handleRegister"
    >
      <el-form-item prop="username" required class="compact-form-item">
        <div class="field-control is-required">
          <span class="field-marker" aria-hidden="true">*</span>
          <el-input
            v-model="registerForm.username"
            placeholder="请输入账号"
            :prefix-icon="User"
            size="large"
          />
        </div>
      </el-form-item>
      <el-form-item prop="password" required class="compact-form-item">
        <div class="field-control is-required">
          <span class="field-marker" aria-hidden="true">*</span>
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </div>
      </el-form-item>
      <el-form-item prop="confirmPassword" required class="compact-form-item">
        <div class="field-control is-required">
          <span class="field-marker" aria-hidden="true">*</span>
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </div>
      </el-form-item>
      <el-form-item prop="phone" class="compact-form-item">
        <div class="field-control">
          <span class="field-marker field-marker--placeholder" aria-hidden="true">*</span>
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号（选填）"
            size="large"
          />
        </div>
      </el-form-item>
      <el-form-item prop="inviteCode" class="compact-form-item">
        <div class="field-control">
          <span class="field-marker field-marker--placeholder" aria-hidden="true">*</span>
          <el-input
            v-model="registerForm.inviteCode"
            placeholder="请输入班级邀请码（选填）"
            size="large"
          />
        </div>
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          size="large"
          class="auth-submit-button"
          :loading="loading"
          @click="handleRegister"
        >
          {{ loading ? '注册中...' : '注册' }}
        </el-button>
      </el-form-item>
    </el-form>
  </AuthPageLayout>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import AuthPageLayout from '@/components/auth/AuthPageLayout.vue'
import request from '@/utils/request'

const router = useRouter()
const loading = ref(false)
const registerFormRef = ref<FormInstance>()

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  inviteCode: ''
})

const validateConfirmPassword = (_rule: any, value: string, callback: Function) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validatePhone = (_rule: any, value: string, callback: Function) => {
  if (!value) {
    callback()
    return
  }
  const normalized = value.trim()
  if (!/^1\d{10}$/.test(normalized)) {
    callback(new Error('请输入正确的手机号'))
    return
  }
  callback()
}

const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度为2-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  inviteCode: [
    { trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await request.post('/auth/register', {
          username: registerForm.username,
          password: registerForm.password,
          confirmPassword: registerForm.confirmPassword,
          realName: registerForm.username,
          phone: registerForm.phone?.trim() || undefined,
          inviteCode: registerForm.inviteCode?.trim() || undefined
        })
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } catch (error: any) {
        ElMessage.error(error.message || '注册失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.compact-form-item {
  margin-bottom: 14px;
}

.field-control {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.field-marker {
  flex: 0 0 12px;
  text-align: center;
  font-size: 16px;
  font-weight: 700;
  color: #f53f3f;
}

.field-marker--placeholder {
  color: transparent;
}

.field-control :deep(.el-input) {
  flex: 1;
}
</style>
