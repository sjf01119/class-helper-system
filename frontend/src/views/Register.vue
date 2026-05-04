<template>
  <AuthPageLayout
    title="学生注册"
    subtitle="填写账号信息并通过邀请码加入班级"
    note-title="注册说明"
    link-text="已有账号？"
    link-label="返回登录"
    link-to="/login"
    visual-type="register"
    wide
  >
    <el-form
      ref="registerFormRef"
      :model="registerForm"
      :rules="registerRules"
      class="auth-form"
      @keyup.enter="handleRegister"
    >
      <el-form-item prop="username">
        <el-input
          v-model="registerForm.username"
          placeholder="请输入用户名（账号）"
          :prefix-icon="User"
          size="large"
        />
      </el-form-item>
      <el-form-item prop="realName">
        <el-input
          v-model="registerForm.realName"
          placeholder="请输入真实姓名"
          :prefix-icon="UserFilled"
          size="large"
        />
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="registerForm.password"
          type="password"
          placeholder="请输入密码（至少6位）"
          :prefix-icon="Lock"
          size="large"
          show-password
        />
      </el-form-item>
      <el-form-item prop="confirmPassword">
        <el-input
          v-model="registerForm.confirmPassword"
          type="password"
          placeholder="请再次输入密码"
          :prefix-icon="Lock"
          size="large"
          show-password
        />
      </el-form-item>
      <el-form-item prop="inviteCode">
        <el-input
          v-model="registerForm.inviteCode"
          placeholder="请输入班级邀请码（必填）"
          :prefix-icon="Key"
          size="large"
        />
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

    <template #note>
      <p>仅支持学生端自助注册，邀请码为必填项。</p>
      <p>注册成功后会自动关联到对应班级，并可直接登录进入学生首页。</p>
    </template>
  </AuthPageLayout>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, UserFilled, Lock, Key } from '@element-plus/icons-vue'
import AuthPageLayout from '@/components/auth/AuthPageLayout.vue'
import request from '@/utils/request'

const router = useRouter()
const loading = ref(false)
const registerFormRef = ref<FormInstance>()

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  inviteCode: ''
})

const validateConfirmPassword = (_rule: any, value: string, callback: Function) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度为2-20个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  inviteCode: [
    { required: true, message: '请输入班级邀请码', trigger: 'blur' }
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
          realName: registerForm.realName,
          inviteCode: registerForm.inviteCode
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
